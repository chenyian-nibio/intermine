package org.intermine.bio.dataconversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class TmClinvarConverter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(TmClinvarConverter.class);
	//
	private static final String DATASET_TITLE = "ClinVar";
	private static final String DATA_SOURCE_NAME = "NCBI";

	private static final String HUMAN_TAXON_ID = "9606";

	private File submissionSummaryFile;
	private File variationCitationsFile;
	private File variationAlleleFile;
	private File crossReferencesFile;
	private File alleleGeneFile;
	private File variationNameFile;
	private File clinicalAssertionPubmedFile;

	public void setSubmissionSummaryFile(File submissionSummaryFile) {
		this.submissionSummaryFile = submissionSummaryFile;
	}

	public void setVariationCitationsFile(File variationCitationsFile) {
		this.variationCitationsFile = variationCitationsFile;
	}

	public void setVariationAlleleFile(File variationAlleleFile) {
		this.variationAlleleFile = variationAlleleFile;
	}

	public void setCrossReferencesFile(File crossReferencesFile) {
		this.crossReferencesFile = crossReferencesFile;
	}

	public void setAlleleGeneFile(File alleleGeneFile) {
		this.alleleGeneFile = alleleGeneFile;
	}

	public void setVariationNameFile(File variationNameFile) {
		this.variationNameFile = variationNameFile;
	}

	public void setClinicalAssertionPubmedFile(File clinicalAssertionPubmedFile) {
		this.clinicalAssertionPubmedFile = clinicalAssertionPubmedFile;
	}

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public TmClinvarConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 *
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		readVariationNameMap();
		readAccPubmedIdMap();
		processVariationAlleleFile();
		processAlleleGeneFile();
		processVariationCitationsFile();
		processSubmissionSummaryFile();
		
		Set<String> processedAllele = new HashSet<String>();

		try {
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				String alleleId = cols[0];
				
				if (processedAllele.contains(alleleId)) {
					continue;
				}
				
				Item allele = createItem("Allele");
				allele.setAttribute("identifier", alleleId);
				allele.setAttribute("type", cols[1]);
				String name = cols[2];
				if (StringUtils.isEmpty(name)) {
					name = "NA";
				}
				allele.setAttribute("name", name);
				allele.setAttribute("clinicalSignificance", cols[6]);
				allele.setAttribute("reviewStatus", cols[24]);
				
				if (!cols[9].equals("-1")) {
					String snp = getSnp("rs" + cols[9]);
					allele.addToCollection("snps", snp);
				} else if (!cols[3].equals("-1") && cols[10].equals("-")) {
					String chr = null;
					String location = null;
					if (cols[16].equals("GRCh38") && !cols[18].equals("na")) {
						chr = cols[18];
						if (!cols[19].equals("-1") && !cols[20].equals("-1")) {
							if (cols[19].equals(cols[20])) {
								location = String.format("%s:%s", chr, cols[19]);
							} else {
								location = String.format("%s:%s..%s", chr, cols[19], cols[20]);
							}
						}
					}
					String refSnpAllele = null;
					if (!cols[21].equals("-") && !cols[22].equals("-")) {
						refSnpAllele = String.format("%s/%s", cols[21], cols[22]);
						if (refSnpAllele.equals("na/na")) {
							refSnpAllele = null;
						}
					}
					
					String snp = getMockSnp(alleleId, cols[3], chr, location, refSnpAllele);
					allele.addToCollection("snps", snp);
				} else {
					continue;
				}
				
				Set<String> variations = alleleVariationMap.get(alleleId);
				if (variations != null) {
					for (String varId : variations) {
						String refId = variationMap.get(varId);
						if (refId != null) {
							allele.addToCollection("variations", refId);
						}
					}
				}
				allele.setReference("organism", getOrganism(HUMAN_TAXON_ID));
				
				store(allele);
				
				processedAllele.add(alleleId);
			}
			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("The file 'cross_references.txt' not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	private void processSubmissionSummaryFile() throws ObjectStoreException {
		LOG.info("Parsing the file submission_summary.txt......");
		System.out.println("Parsing the file submission_summary.txt......");

		try {
			FileReader reader = new FileReader(submissionSummaryFile);
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
			while (iterator.hasNext()) {
				String[] cols = iterator.next();

				Item item = createItem("ClinicalAssertion");
				item.setAttribute("clinicalSignificance", cols[1]);
				item.setAttribute("description", cols[3]);
				// NOTE: remove MedGen identifier or ?
				String reportedPhenotypeInfo = cols[5];
				item.setAttribute("reportedPhenotypeInfo", reportedPhenotypeInfo);
				item.setAttribute("reviewStatus", cols[6]);
				item.setAttribute("collectionMethod", cols[7]);
				item.setAttribute("originCounts", cols[8]);
				item.setAttribute("submitter", cols[9]);
				item.setAttribute("accession", cols[10]);

				String accession = cols[10];
				if (accession.contains(".")) {
					accession = accession.substring(0, accession.indexOf("."));
				}
				if (accPubmedIdMap.containsKey(accession)) {
					String[] pubmedIds = accPubmedIdMap.get(accession).split(",");
					for (String pubmedId : pubmedIds) {
						item.addToCollection("publications", getPublication(pubmedId));
					}
					item.setAttribute("numOfPublications", String.valueOf(pubmedIds.length));
				} else {
					item.setAttribute("numOfPublications", "0");
				}

				if (!StringUtils.isEmpty(cols[11])) {
					item.setAttribute("submittedGeneSymbol", cols[11]);
				}
				item.setReference("variation", getVariation(cols[0]));

				if (!reportedPhenotypeInfo.equals("-")) {
					for (String info : reportedPhenotypeInfo.split(";")) {
						try {
							String id = info.substring(0, info.indexOf(":"));
							// if (id.matches("C\\d+")) {
							// item.addToCollection("diseaseTerms", getDiseaseTerm(id,
							// info.substring(info.indexOf(":") + 1)));
							// }
							if (!id.equals("na")) {
								item.addToCollection("diseaseTerms",
										getDiseaseTerm(id, info.substring(info.indexOf(":") + 1)));
							}
						} catch (StringIndexOutOfBoundsException e) {
							System.out.println(info);
							System.out.println(reportedPhenotypeInfo);
							throw new RuntimeException(
									"StringIndexOutOfBoundsException: " + reportedPhenotypeInfo);
						}
					}
				}

				store(item);
			}
			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("The file 'submission_summary.txt' not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	private void processVariationCitationsFile() throws ObjectStoreException {
		LOG.info("Parsing the file var_citations.txt......");
		System.out.println("Parsing the file var_citations.txt......");

		try {
			FileReader reader = new FileReader(variationCitationsFile);
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
			Map<String, Set<String>> varPubMap = new HashMap<String, Set<String>>();
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				if (cols[4].equals("PubMed")) {
					if (varPubMap.get(cols[1]) == null) {
						varPubMap.put(cols[1], new HashSet<String>());
					}
					varPubMap.get(cols[1]).add(cols[5]);
				}
			}
			reader.close();

			for (String varId : varPubMap.keySet()) {
				Item item = createItem("Variation");
				item.setAttribute("identifier", varId);
				String name = variationNameMap.get(varId);
				if (name != null) {
					item.setAttribute("name", name);
				} else {
					LOG.info(String.format("Variation name not available: %s", varId));
					// these variation are deprecated entries, theoretically
					continue;
				}
				for (String pubmedId : varPubMap.get(varId)) {
					item.addToCollection("publications", getPublication(pubmedId));
				}
				String type = variationTypeMap.get(varId);
				if (type != null) {
					item.setAttribute("type", type);
				}
				store(item);
				variationMap.put(varId, item.getIdentifier());
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("The file 'var_citations.txt' not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	Map<String, String> variationTypeMap = new HashMap<String, String>();
	Map<String, Set<String>> alleleVariationMap = new HashMap<String, Set<String>>();

	private void processVariationAlleleFile() {
		LOG.info("Parsing the file variation_allele.txt......");
		System.out.println("Parsing the file variation_allele.txt......");

		try {
			FileReader reader = new FileReader(variationAlleleFile);
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				variationTypeMap.put(cols[0], cols[1]);
				if (alleleVariationMap.get(cols[2]) == null) {
					alleleVariationMap.put(cols[2], new HashSet<String>());
				}
				alleleVariationMap.get(cols[2]).add(cols[0]);
			}
			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("The file 'variation_allele.txt' not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	@SuppressWarnings("unused")
	private void processCrossReferencesFile() {
		// TODO insufficient SNP information, skip these contents at the moment (chenyian, 2018.2.7)
		LOG.info("Parsing the file cross_references.txt......");
		System.out.println("Parsing the file cross_references.txt......");

		try {
			FileReader reader = new FileReader(crossReferencesFile);
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
			}
			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("The file 'cross_references.txt' not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	Map<String, String> snpTypeMap = new HashMap<String, String>();

	private void processAlleleGeneFile() {
		// TODO unable to resolve the model conflict with SO, skip these contents at the moment
		// (chenyian, 2018.2.7)
		// NOTE: allele and gene is many-to-many but many-to-one in the sequence ontology(SO)
		// use to get snp and gene relation; within the gene, upstream or downstream (temporary
		// solution)
		LOG.info("Parsing the file allele_gene.txt......");
		System.out.println("Parsing the file allele_gene.txt......");

		try {
			FileReader reader = new FileReader(alleleGeneFile);
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				snpTypeMap.put(cols[0] + "-" + cols[1], cols[5]);
			}
			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("The file 'allele_gene.txt' not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	private Map<String, String> snpMap = new HashMap<String, String>();
	private Map<String, String> publicationMap = new HashMap<String, String>();
	private Map<String, String> variationMap = new HashMap<String, String>();

	private String getSnp(String identifier) throws ObjectStoreException {
		String ret = snpMap.get(identifier);
		if (ret == null) {
			Item item = createItem("SNP");
			item.setAttribute("identifier", identifier);
			store(item);
			ret = item.getIdentifier();
			snpMap.put(identifier, ret);
		}
		return ret;
	}

	private String getMockSnp(String alleleId, String geneId, String chromosome, String location,
			String refSnpAllele) throws ObjectStoreException {
		String ret = snpMap.get("cv-" + alleleId);
		if (ret == null) {
			Item snpItem = createItem("SNP");
			snpItem.setAttribute("identifier", alleleId);
			if (chromosome != null) {
				snpItem.setAttribute("chromosome", chromosome);
				if (location != null) {
					snpItem.setAttribute("location", location);
				}
			}
			if (refSnpAllele != null) {
				snpItem.setAttribute("refSnpAllele", refSnpAllele);
			}
			store(snpItem);
			
			Item vaItem = createItem("VariationAnnotation");
			vaItem.setAttribute("identifier", alleleId + "-" + geneId);
			vaItem.setReference("snp", snpItem);
			vaItem.setReference("gene", getGene(geneId));
			store(vaItem);
			
			ret = snpItem.getIdentifier();
			snpMap.put("cv-" + alleleId, ret);
		}
		return ret;
	}
	
	private Map<String, String> geneMap = new HashMap<String, String>();
	private String getGene(String geneId) throws ObjectStoreException {
		String ret = geneMap.get(geneId);
		if (ret == null) {
			Item item = createItem("Gene");
			item.setAttribute("primaryIdentifier", geneId);
			store(item);
			ret = item.getIdentifier();
			geneMap.put(geneId, ret);
		}
		return ret;
	}

	private String getPublication(String pubmedId) throws ObjectStoreException {
		String ret = publicationMap.get(pubmedId);
		if (ret == null) {
			Item item = createItem("Publication");
			item.setAttribute("pubMedId", pubmedId);
			store(item);
			ret = item.getIdentifier();
			publicationMap.put(pubmedId, ret);
		}
		return ret;
	}

	private String getVariation(String identifier) throws ObjectStoreException {
		String ret = variationMap.get(identifier);
		if (ret == null) {
			Item item = createItem("Variation");
			item.setAttribute("identifier", identifier);
			String name = variationNameMap.get(identifier);
			if (name != null) {
				item.setAttribute("name", name);
			} else {
				LOG.info(String.format("Cannot find the variation name: %s", identifier));
			}
			String type = variationTypeMap.get(identifier);
			if (type != null) {
				item.setAttribute("type", type);
			}
			store(item);
			ret = item.getIdentifier();
			variationMap.put(identifier, ret);
		}
		return ret;
	}

	private Map<String, String> diseaseTermMap = new HashMap<String, String>();

	private String getDiseaseTerm(String identifier, String title) throws ObjectStoreException {
		String ret = diseaseTermMap.get(identifier);
		if (ret == null) {
			Item item = createItem("DiseaseTerm");
			item.setAttribute("identifier", identifier);
			item.setAttribute("name", title);
			store(item);
			ret = item.getIdentifier();
			diseaseTermMap.put(identifier, ret);
		}
		return ret;
	}

	private Map<String, String> variationNameMap = new HashMap<String, String>();

	private void readVariationNameMap() {
		String fileName = variationNameFile.getName();
		LOG.info(String.format("Parsing the file %s......", fileName));
		System.out.println(String.format("Parsing the file %s......", fileName));

		try {
			FileReader reader = new FileReader(variationNameFile);
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				String vid = cols[0];
				String name = cols[1];
				if (name.contains("|||")) {
					name = name.split("\\|\\|\\|")[0];
				}
				variationNameMap.put(vid, name);
			}
			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(String.format("The file '%s' not found.", fileName));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private Map<String, String> accPubmedIdMap = new HashMap<String, String>();

	private void readAccPubmedIdMap() {
		String fileName = clinicalAssertionPubmedFile.getName();
		LOG.info(String.format("Parsing the file %s......", fileName));
		System.out.println(String.format("Parsing the file %s......", fileName));

		try {
			FileReader reader = new FileReader(clinicalAssertionPubmedFile);
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				String acc = cols[0];
				String pids = cols[2];
				accPubmedIdMap.put(acc, pids);
			}
			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(String.format("The file '%s' not found.", fileName));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
