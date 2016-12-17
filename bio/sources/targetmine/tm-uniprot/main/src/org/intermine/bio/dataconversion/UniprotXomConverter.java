package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.metadata.Util;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;

/**
 * A new UniProt XML parser using XOM library Some settings were hard-coded which may be only
 * suitable for TargetMine
 * 
 * @author chenyian
 * 
 */
public class UniprotXomConverter extends BioFileConverter {

	private static final Logger LOG = Logger.getLogger(UniprotXomConverter.class);

	private static final String DATA_SOURCE_NAME = "UniProt";
	private static final int POSTGRES_INDEX_SIZE = 2712;
	private static final String FEATURE_TYPES = "initiator methionine, signal peptide, transit peptide, propeptide, chain, peptide, topological domain, transmembrane region, intramembrane region, domain, repeat, calcium-binding region, zinc finger region, DNA-binding region, nucleotide phosphate-binding region, region of interest, coiled-coil region, short sequence motif, compositionally biased region, active site, metal ion-binding site, binding site, site, non-standard amino acid, modified residue, lipid moiety-binding region, glycosylation site, disulfide bond, cross-link";

	private String dataSource;
	private Set<String> featureTypes = new HashSet<String>(Arrays.asList(FEATURE_TYPES
			.split(",\\s*")));

	private Map<String, String> keywords = new HashMap<String, String>();
	private Map<String, String> ontologies = new HashMap<String, String>();
	private Map<String, String> genes = new HashMap<String, String>();
	private Map<String, String> publications = new HashMap<String, String>();
	private Map<String, String> allSequences = new HashMap<String, String>();

	// 
	private Map<String, String> ptmListMap = new HashMap<String, String>();

	private Set<String> doneEntries = new HashSet<String>();

	private Set<Item> synonymsAndXrefs = new HashSet<Item>();

	// for logging
	private int numOfNewEntries = 0;

	public UniprotXomConverter(ItemWriter writer, Model model) {
		super(writer, model);
		dataSource = getDataSource(DATA_SOURCE_NAME);
		try {
			setOntology("UniProtKeyword");
		} catch (ObjectStoreException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public void process(Reader reader) throws Exception {
		if (ptmListMap == null || ptmListMap.isEmpty()) {
			loadPtmListFile();
		}
		
		try {
			BufferedReader br = new BufferedReader(reader);

			StringBuffer sb = new StringBuffer();
			String line;
			boolean flag = false;
			long lineNum = 0;
			while ((line = br.readLine()) != null) {
				lineNum++;
				if (lineNum % 20000000 == 0) {
					System.out.println(String.format("%,d lines were processed", lineNum));
					LOG.info(String.format("%,d lines were processed", lineNum));
				}
				if (line.startsWith("<entry")) {
					flag = true;
					sb = new StringBuffer();
				}
				if (flag) {
					sb.append(line + "\n");
				}
				if (line.startsWith("</entry>")) {
					// process
					Builder parser = new Builder();
					Document doc = parser.build(new ByteArrayInputStream(sb.toString().getBytes()));
					Element entry = doc.getRootElement();

					Elements accessions = entry.getChildElements("accession");
					String accession = accessions.get(0).getValue();
					Set<String> otherAccessions = new HashSet<String>();
					for (int i = 1; i < accessions.size(); i++) {
						otherAccessions.add(accessions.get(i).getValue());
					}
					// TODO actually, should not find duplicated primary accessions
					if (!doneEntries.contains(accession)) {
						// create Protein items
						Item protein = createItem("Protein");

						protein.addToCollection(
								"dataSets",
								getDataSet(entry.getAttributeValue("dataset") + " data set",
										dataSource));

						/* primaryAccession, primaryIdentifier, name, etc */
						String primaryIdentifier = entry.getFirstChildElement("name").getValue();

						Element proteinElement = entry.getFirstChildElement("protein");
						Elements nameElements = proteinElement.getChildElements();
						String proteinName = nameElements.get(0).getFirstChildElement("fullName")
								.getValue();
						protein.setAttribute("name", proteinName);
						for (int i = 0; i < nameElements.size(); i++) {
							// these are synonyms; there are two types:
							// recommendedName; submittedName; alternativeName --> fullName,
							// shortName
							// allergenName; biotechName; cdAntigenName; innName
							Element e = nameElements.get(i);
							if (e.getLocalName().endsWith("Name")) {
								Elements childElements = e.getChildElements();
								if (childElements.size() > 0) {
									for (int c = 0; c < childElements.size(); c++) {
										String value = childElements.get(c).getValue();
										if (!proteinName.equals(value)) {
											addSynonym(protein.getIdentifier(), value);
										}
									}
								} else {
									String value = e.getValue();
									addSynonym(protein.getIdentifier(), value);
								}
							}
						}

						protein.setAttribute("uniprotAccession", accession);
						protein.setAttribute("primaryAccession", accession);
						for (String acc : otherAccessions) {
							// other accessions are synonyms
							addSynonym(protein.getIdentifier(), acc);
						}

						protein.setAttribute("primaryIdentifier", primaryIdentifier);
						protein.setAttribute("uniprotName", primaryIdentifier);

						protein.setAttribute("isUniprotCanonical", "true");

						/* sequence */
						Element sequence = entry.getFirstChildElement("sequence");
						protein.setAttribute("isFragment",
								sequence.getAttributeValue("fragment") == null ? "false" : "true");
						String length = sequence.getAttributeValue("length");
						protein.setAttribute("length", length);
						protein.setAttribute("molecularWeight", sequence.getAttributeValue("mass"));

						String aaSeq = sequence.getValue();
						String md5Checksum = getSequence(aaSeq);
						protein.setReference("sequence", allSequences.get(md5Checksum));
						protein.setAttribute("md5checksum", md5Checksum);

						String taxonId = entry.getFirstChildElement("organism")
								.getFirstChildElement("dbReference").getAttributeValue("id");
						protein.setReference("organism", getOrganism(taxonId));

						/* publications */
						Elements publications = entry.getChildElements("reference");
						for (int i = 0; i < publications.size(); i++) {
							Elements dbRefs = publications.get(i).getFirstChildElement("citation")
									.getChildElements("dbReference");
							for (int d = 0; d < dbRefs.size(); d++) {
								if ("PubMed".equals(dbRefs.get(d).getAttributeValue("type"))) {
									String pubMedId = dbRefs.get(d).getAttributeValue("id");
									protein.addToCollection("publications",
											getPublication(pubMedId));
								}
							}
						}

						/* comments */
						Elements comments = entry.getChildElements("comment");
						for (int i = 0; i < comments.size(); i++) {
							Element comment = comments.get(i);
							Element text = comment.getFirstChildElement("text");
							if (text != null) {
								String commentText = text.getValue();
								Item item = createItem("Comment");
								item.setAttribute("type", comment.getAttributeValue("type"));
								if (commentText.length() > POSTGRES_INDEX_SIZE) {
									// comment text is a string
									String ellipses = "...";
									String choppedComment = commentText.substring(0,
											POSTGRES_INDEX_SIZE - ellipses.length());
									item.setAttribute("description", choppedComment + ellipses);
								} else {
									item.setAttribute("description", commentText);
								}
								// TODO add publications for comments?
								store(item);
								protein.addToCollection("comments", item);
							}
						}

						/* keywords */
						Elements keywordElements = entry.getChildElements("keyword");
						for (int i = 0; i < keywordElements.size(); i++) {
							String title = keywordElements.get(i).getValue();
							String id = keywordElements.get(i).getAttributeValue("id");
							String refId = keywords.get(id);
							if (refId == null) {
								Item item = createItem("OntologyTerm");
								item.setAttribute("identifier", id);
								item.setAttribute("name", title);
								item.setReference("ontology", ontologies.get("UniProtKeyword"));
								refId = item.getIdentifier();
								keywords.put(id, refId);
								store(item);
							}
							protein.addToCollection("keywords", refId);
						}

						/* dbrefs */
						Set<String> geneIds = new HashSet<String>();
						Elements dbReferences = entry.getChildElements("dbReference");
						for (int i = 0; i < dbReferences.size(); i++) {
							Element dbRef = dbReferences.get(i);
							String type = dbRef.getAttributeValue("type");
							String id = dbRef.getAttributeValue("id");
							// if (xrefs.contains(type)) {
							// Item item = createCrossReference(protein.getIdentifier(),
							// id, type, false);
							// if (item != null) {
							// synonymsAndXrefs.add(item);
							// }
							// }
							if (type.equals("GeneID")) {
								geneIds.add(id);
							} else if (type.equals("Ensembl")) {
								Elements properties = dbRef.getChildElements("property");
								for (int p = 0; p < properties.size(); p++) {
									if (properties.get(p).getAttributeValue("type")
											.equals("protein sequence ID")) {
										addSynonym(protein.getIdentifier(), properties.get(p)
												.getAttributeValue("value"));
									}
								}
							} else if (type.equals("RefSeq")) {
								addSynonym(protein.getIdentifier(), id);
							}
						}

						/* genes */

						if (geneIds.isEmpty()) {
							LOG.error("no valid gene identifiers found for " + accession);
						} else {
							for (String identifier : geneIds) {
								if (StringUtils.isEmpty(identifier)) {
									continue;
								}
								String geneRefId = genes.get(identifier);
								if (geneRefId == null) {
									Item gene = createItem("Gene");
									gene.setAttribute("primaryIdentifier", identifier);
									gene.setReference("organism", getOrganism(taxonId));
									geneRefId = gene.getIdentifier();
									genes.put(identifier, geneRefId);
									store(gene);
								}
								protein.addToCollection("genes", geneRefId);
							}
						}

						// TODO evidence?

						store(protein);
						// actually, the main accession should not be duplicated
						doneEntries.add(accession);

						/* features */
						Elements features = entry.getChildElements("feature");
						Set<String> modificationSet = new HashSet<String>();
						for (int i = 0; i < features.size(); i++) {
							Element feature = features.get(i);
							String type = feature.getAttributeValue("type");
							if (!featureTypes.contains(type)) {
								continue;
							}
							String description = feature.getAttributeValue("description");
							String status = feature.getAttributeValue("status");

							// TODO process feature at this step?
							Item featureItem = createItem("UniProtFeature");
							featureItem.setAttribute("type", type);
//							String keywordRefId = getKeyword(type);
//							featureItem.setReference("feature", keywordRefId);
							String featureDescription = description;
							if (status != null) {
								featureDescription = (description == null ? status : description
										+ " (" + status + ")");
							}
							if (!StringUtils.isEmpty(featureDescription)) {
								featureItem.setAttribute("description", featureDescription);
							}
							Element location = feature.getFirstChildElement("location");
							Element position = location.getFirstChildElement("position");
							String modiPos = null;
							if (position != null) {
								modiPos = position.getAttributeValue("position");
								featureItem.setAttribute("begin", modiPos);
								featureItem.setAttribute("end", modiPos);
							} else {
								Element beginElement = location.getFirstChildElement("begin");
								Element endElement = location.getFirstChildElement("end");
								if (beginElement != null && endElement != null) {
									// beware that some entries contain unknown position
									// e.g. <end status="unknown"/>
									String begin = beginElement.getAttributeValue("position");
									if (begin != null) {
										featureItem.setAttribute("begin", begin);
									}
									String end = endElement.getAttributeValue("position");
									if (end != null) {
										featureItem.setAttribute("end", end);
									}
									if (begin != null && begin.equals(end)) {
										modiPos = begin;
										// should not happen?
										LOG.info("Protein " + accession + " contains the same begin and end values.");
									}
								}
							}
							featureItem.setReference("protein", protein);
							
							if (modiPos != null) {
								String kw = ptmListMap.get(description);
								
								if (kw == null) {
									// TODO slightly tricky?
									if (type.equals("glycosylation site")) {
										kw = "Glycosylation";
									} else if (!StringUtils.isEmpty(description)) {
										kw = searchPtmListMap(description);
									}
								}
								
								if (kw != null) {
									for (String modType: kw.split("; ")) {
										if (modType.equals("Phosphoprotein")) {
											modType = "Phosphorylation";
										}
										
										String key = String.format("%s-%s", modiPos, modType);
										if (!modificationSet.contains(key)) {
											Item modification = createItem("Modification");
											modification.setReference("protein", protein);
											modification.setAttribute("type", modType);
											modification.setAttribute("position", modiPos);
											
											int pos = Integer.valueOf(modiPos).intValue();
											modification.setAttribute("residue", aaSeq.substring(pos - 1, pos));
											
											modification.addToCollection(
													"dataSets",
													getDataSet(entry.getAttributeValue("dataset")
															+ " data set", dataSource));
											store(modification);
											modificationSet.add(key);
										}
									}
								}
							}
							
							store(featureItem);
						}

						/* components */
						Elements components = proteinElement.getChildElements("component");
						for (int i = 0; i < components.size(); i++) {
							Element ele = components.get(i).getFirstChildElement("recommendedName");
							if (ele != null) {
								Item item = createItem("Component");
								item.setAttribute("name", ele.getFirstChildElement("fullName")
										.getValue());
								item.setReference("protein", protein);
								store(item);
							}
						}

						for (String acc : otherAccessions) {
							// other accessions are synonyms
							Item item = createItem("ProteinAccession");
							item.setAttribute("accession", acc);
							item.setReference("protein", protein);
							store(item);
						}

						numOfNewEntries++;
						LOG.info("Entry " + accession + " created.");

						// store all synonyms
						for (Item item : synonymsAndXrefs) {
							if (item == null) {
								continue;
							}
							store(item);
						}

						// reset
						synonymsAndXrefs = new HashSet<Item>();
					}

				}

			}

			br.close();

		} catch (ParsingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String info = "Create " + numOfNewEntries + " entries.";
		System.out.println(info);
		LOG.info(info);

	}

	private String searchPtmListMap(String description) {
		for (String key : ptmListMap.keySet()) {
			if (description.startsWith(key)) {
				return ptmListMap.get(key);
			}
		}
		return null;
	}

	private File ptmlistFile;

	public void setptmlistFile(File fileName) {
		this.ptmlistFile = fileName;
	}

	private void loadPtmListFile() {
		System.out.println("Processing ptmlist.txt ...");
		ptmListMap.clear();
		try {
			BufferedReader in = new BufferedReader(new FileReader(ptmlistFile));
			String line;
			String id = "";
			while ((line = in.readLine()) != null) {
				if (line.startsWith("ID")) {
					id = line.substring(5);
					if (id.contains(" (")) {
						id = id.substring(0, id.indexOf(" ("));
					}
				} else if (line.startsWith("KW")) {
					ptmListMap.put(id, line.substring(5).replaceAll("\\.$", ""));
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("The file 'ptmlist.txt' is not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	private void addSynonym(String refId, String synonym) throws ObjectStoreException {
		Item item = createSynonym(refId, synonym, false);
		if (item != null) {
			synonymsAndXrefs.add(item);
		}
	}

	private String setOntology(String title) throws ObjectStoreException {
		String refId = ontologies.get(title);
		if (refId == null) {
			Item ontology = createItem("Ontology");
			ontology.setAttribute("name", title);
			ontologies.put(title, ontology.getIdentifier());
			store(ontology);
		}
		return refId;
	}

	/**
	 * Create Sequence item if not existed.
	 * 
	 * @param residues
	 * @return md5Checksum as the key of the Sequence item in the allSequences map.
	 * @throws ObjectStoreException
	 */
	private String getSequence(String residues) throws ObjectStoreException {
		String md5Checksum = Util.getMd5checksum(residues);
		if (!allSequences.containsKey(md5Checksum)) {
			Item item = createItem("Sequence");
			item.setAttribute("residues", residues);
			item.setAttribute("length", String.valueOf(residues.length()));
			item.setAttribute("md5checksum", md5Checksum);
			store(item);
			allSequences.put(md5Checksum, item.getIdentifier());
		}
		return md5Checksum;
	}

//	private String getKeyword(String title) throws ObjectStoreException {
//		String refId = keywords.get(title);
//		if (refId == null) {
//			Item item = createItem("OntologyTerm");
//			item.setAttribute("name", title);
//			item.setReference("ontology", ontologies.get("UniProtKeyword"));
//			refId = item.getIdentifier();
//			keywords.put(title, refId);
//			store(item);
//		}
//		return refId;
//	}

	private String getPublication(String pubMedId) throws ObjectStoreException {
		String refId = publications.get(pubMedId);

		if (refId == null) {
			Item item = createItem("Publication");
			item.setAttribute("pubMedId", pubMedId);
			publications.put(pubMedId, item.getIdentifier());
			store(item);
			refId = item.getIdentifier();
		}

		return refId;
	}

}
