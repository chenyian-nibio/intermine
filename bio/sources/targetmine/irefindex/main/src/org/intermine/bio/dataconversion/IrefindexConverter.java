package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * Parser for PPI data from iRefIndex (http://irefindex.org/wiki/index.php?title=iRefIndex);
 * Column definition could be found at
 * http://irefindex.org/wiki/index.php?title=README_MITAB2.6_for_iRefIndex .
 * 
 * @author chenyian
 */
public class IrefindexConverter extends BioFileConverter {
	//
	private static final String DATASET_TITLE = "iRefIndex";
	private static final String DATA_SOURCE_NAME = "iRefIndex";
	private static final String TYPE_FILE = "interactiontype.txt";

	private static final Logger LOG = Logger.getLogger(IrefindexConverter.class);

	private Map<String, String> interactionTypeMap;

	private Map<String, String> geneMap = new HashMap<String, String>();
	private Map<String, String> pubMap = new HashMap<String, String>();
	private Map<String, String> miMap = new HashMap<String, String>();
	private Map<MultiKey, String> expMap = new HashMap<MultiKey, String>();
	private Map<MultiKey, Item> intMap = new HashMap<MultiKey, Item>();

	// to prevent duplications
	private Map<String, String> detailMap = new HashMap<String, String>();
	private Map<String, Item> sourceMap = new HashMap<String, Item>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public IrefindexConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		if (interactionTypeMap == null) {
			readInteractionType();
		}

		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(reader));

		// skip header
		iterator.next();

		while (iterator.hasNext()) {
			String[] cols = iterator.next();

			if (cols[7].equals("OPHID Predicted Protein Interaction")
					|| cols[7].equals("HPRD Text Mining Confirmation")
					|| cols[7].equals("MINT Text Mining Confirmation")) {
				continue;
			}

			// some data from OPHID are not tagged in the author column
			String sourceDb = getMiDesc(cols[12]);
			if (sourceDb.equals("ophid")) {
				continue;
			}
			if (sourceDb.equals("biogrid")) {
				continue;
			}
			String[] ids = cols[13].split("\\|");
			String sourceId = ids[0];

			Set<String> geneASet = processAltIdentifier(cols[2]);
			if (geneASet.isEmpty()) {
				continue;
			}
			Set<String> geneBSet = processAltIdentifier(cols[3]);
			if (geneBSet.isEmpty()) {
				continue;
			}
			
			// in case there are redundant entries
			String[] pmids = cols[8].split("\\|");
			
			Set<String> expRefIdSet = new HashSet<String>();
			String detectioniMethod = cols[6];
			// these terms were deprecated
			if (detectioniMethod.equals("MI:0492(in vitro)") || detectioniMethod.equals("MI:0493(in vivo)")) {
				detectioniMethod = "-";
			}
			for (String pmid : pmids) {
				expRefIdSet.add(getExperiment(pmid, cols[7], detectioniMethod, cols[28]));
				// , sourceDb, ids[0]
			}
			List<String> expRefIds = new ArrayList<String>(expRefIdSet);
			Collections.sort(expRefIds);

			String role1 = getMiDesc(cols[18]);
			String role2 = getMiDesc(cols[19]);
			
			if (cols[0].equals(cols[1]) || StringUtils.join(geneASet, "_").equals(StringUtils.join(geneBSet, "_"))) {
				// self-interaction
				for (String geneA : geneASet) {
					String geneARef = getGene(geneA, cols[9]);
					
					Item interaction = getInteraction(geneARef, geneARef);
					
					for (String expRefId : expRefIds) {
						String intKey = String.format("%s_%s_%s", geneA, geneA, expRefId);
						if (detailMap.get(intKey) != null) {
							if (sourceMap.get(sourceId) == null) {
								Item source = createItem("InteractionSource");
								source.setAttribute("sourceDb", sourceDb);
								source.setAttribute("sourceId", sourceId);
								source.addToCollection("details", detailMap.get(intKey));
								sourceMap.put(sourceId, source);
							} else {
								sourceMap.get(sourceId).addToCollection("details", detailMap.get(intKey));
							}
							continue;
						}
						Item detail = createItem("InteractionDetail");
						
						if (role1 != null) {
							detail.setAttribute("role1", role1);
						}
						if (role2 != null) {
							detail.setAttribute("role2", role2);
						}
						String relationshipType = null;
						String intType = null;
						if (!cols[11].equals("-")) {
							String miType = cols[11].substring(0, 7);
							if (cols[11].startsWith("psi-mi:")) {
								miType = cols[11].substring(8, 15);
							}
							relationshipType = getInteractionTerm(miType);
							detail.setReference("relationshipType", relationshipType);
							// physical or genetic
							String interactionType = interactionTypeMap.get(miType);
							if (interactionType != null) {
								intType = interactionType;
							} else {
								LOG.error(String.format("Cannot resolve interaction type: %s", miType));
							}
						}
						if (intType == null) {
							intType = "unspecified";
						}
						detail.setAttribute("type", intType);
						
						detail.setReference("experiment", expRefId);
						detail.setAttribute("name", String.format("iRef:%s-%s", geneA, geneA));
						
						detail.addToCollection("allInteractors", geneARef);
						
						detail.setReference("interaction", interaction);
						
						store(detail);
						detailMap.put(intKey, detail.getIdentifier());
						
						if (sourceMap.get(sourceId) == null) {
							Item source = createItem("InteractionSource");
							source.setAttribute("sourceDb", sourceDb);
							source.setAttribute("sourceId", sourceId);
							source.addToCollection("details", detailMap.get(intKey));
							sourceMap.put(sourceId, source);
						} else {
							sourceMap.get(sourceId).addToCollection("details", detailMap.get(intKey));
						}
					}
				}
				
			} else {
				for (String geneA : geneASet) {
					for (String geneB : geneBSet) {
						String geneARef = getGene(geneA, cols[9]);
						String geneBRef = getGene(geneB, cols[10]);
						
						Item interaction = getInteraction(geneARef, geneBRef);
						
						for (String expRefId : expRefIds) {
							String intKey = String.format("%s_%s_%s", geneA, geneB, expRefId);
							String intKey2 = String.format("%s_%s_%s", geneB, geneA, expRefId);
							if (detailMap.get(intKey) != null) {
								if (sourceMap.get(sourceId) == null) {
									Item source = createItem("InteractionSource");
									source.setAttribute("sourceDb", sourceDb);
									source.setAttribute("sourceId", sourceId);
									source.addToCollection("details", detailMap.get(intKey));
									source.addToCollection("details", detailMap.get(intKey2));
									sourceMap.put(sourceId, source);
								} else {
									sourceMap.get(sourceId).addToCollection("details", detailMap.get(intKey));
									sourceMap.get(sourceId).addToCollection("details", detailMap.get(intKey2));
								}
								continue;
							}
							
							Item detail = createItem("InteractionDetail");
							
							if (role1 != null) {
								detail.setAttribute("role1", role1);
							}
							if (role2 != null) {
								detail.setAttribute("role2", role2);
							}
							String relationshipType = null;
							String intType = null;
							if (!cols[11].equals("-")) {
								String miType = cols[11].substring(0, 7);
								if (cols[11].startsWith("psi-mi:")) {
									miType = cols[11].substring(8, 15);
								}
								relationshipType = getInteractionTerm(miType);
								detail.setReference("relationshipType", relationshipType);
								// physical or genetic
								String interactionType = interactionTypeMap.get(miType);
								if (interactionType != null) {
									intType = interactionType;
								} else {
									LOG.error(String.format("Cannot resolve interaction type: %s", miType));
								}
							}
							if (intType == null) {
								intType = "unspecified";
							}
							detail.setAttribute("type", intType);
							
							detail.setReference("experiment", expRefId);
							detail.setAttribute("name", String.format("iRef:%s-%s", geneA, geneB));
							
							detail.addToCollection("allInteractors", geneARef);
							detail.addToCollection("allInteractors", geneBRef);
							
							detail.setReference("interaction", interaction);
							
							store(detail);
							detailMap.put(intKey, detail.getIdentifier());
							
							Item interaction2 = getInteraction(geneBRef, geneARef);
							Item detail2 = createItem("InteractionDetail");
							
							if (role1 != null) {
								detail2.setAttribute("role2", role1);
							}
							if (role2 != null) {
								detail2.setAttribute("role1", role2);
							}
							
							if (relationshipType != null) {
								detail2.setReference("relationshipType",relationshipType);
							}
							detail2.setAttribute("type", intType);
							detail2.setReference("experiment", expRefId);
							detail2.setAttribute("name", String.format("iRef:%s-%s", geneB, geneA));
							
							detail2.addToCollection("allInteractors", geneARef);
							detail2.addToCollection("allInteractors", geneBRef);
							
							detail2.setReference("interaction", interaction2);
							
							store(detail2);
							detailMap.put(intKey2, detail2.getIdentifier());
							
							if (sourceMap.get(sourceId) == null) {
								Item source = createItem("InteractionSource");
								source.setAttribute("sourceDb", sourceDb);
								source.setAttribute("sourceId", sourceId);
								source.addToCollection("details", detailMap.get(intKey));
								source.addToCollection("details", detailMap.get(intKey2));
								sourceMap.put(sourceId, source);
							} else {
								sourceMap.get(sourceId).addToCollection("details", detailMap.get(intKey));
								sourceMap.get(sourceId).addToCollection("details", detailMap.get(intKey2));
							}
						}
						
					}
				
			}
			
			
			}
			
		}

	}

	private String getMiDesc(String s) {
		return s.substring(8, s.length() - 1);
	}

    private Item getInteraction(String refId, String gene2RefId) throws ObjectStoreException {
        MultiKey key = new MultiKey(refId, gene2RefId);
        Item interaction = intMap.get(key);
        if (interaction == null) {
            interaction = createItem("Interaction");
            interaction.setReference("gene1", refId);
            interaction.setReference("gene2", gene2RefId);
            intMap.put(key, interaction);
            store(interaction);
        }
        return interaction;
    }

	private void readInteractionType() throws IOException {
		interactionTypeMap = new HashMap<String, String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(getClass()
				.getClassLoader().getResourceAsStream(TYPE_FILE)));
		String line;
		String type = "";
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}
			if (line.startsWith("//")) {
				continue;
			}
			if (line.startsWith("#")) {
				type = line.substring(1);
			} else {
				interactionTypeMap.put(line, type);
			}
		}

	}

	private String getPublication(String pubMedId) throws ObjectStoreException {
		String itemId = pubMap.get(pubMedId);
		if (itemId == null) {
			Item pub = createItem("Publication");
			pub.setAttribute("pubMedId", pubMedId);
			itemId = pub.getIdentifier();
			pubMap.put(pubMedId, itemId);
			store(pub);
		}
		return itemId;
	}

	private String getGene(String geneId, String taxonField) throws ObjectStoreException {
		String itemId = geneMap.get(geneId);

		String taxonId = taxonField;
		if (itemId == null) {
			Item gene = createItem("Gene");
			gene.setAttribute("primaryIdentifier", geneId);
			if (!taxonField.equals("-")) {
				taxonId = taxonField.substring(6, taxonField.indexOf("("));
				gene.setReference("organism", getOrganism(taxonId));
			}
			itemId = gene.getIdentifier();
			geneMap.put(geneId, itemId);
			store(gene);
		}
		return itemId;
	}

	private String getInteractionTerm(String miId) throws ObjectStoreException {
		String itemId = miMap.get(miId);
		if (itemId == null) {
			Item term = createItem("InteractionTerm");
			term.setAttribute("identifier", miId);
			itemId = term.getIdentifier();
			miMap.put(miId, itemId);
			store(term);
		}
		return itemId;
	}

	private String getExperiment(String pubMedId, String author, String detectioniMethod,
			String host) throws ObjectStoreException {
		// chenyian: seems that one experiment only associates with one detection method so far (2015.4.15) 
		MultiKey key = new MultiKey(pubMedId, detectioniMethod);
		String ret = expMap.get(key);
		if (ret == null) {
			Item exp = createItem("InteractionExperiment");
			if (!author.equals("-")) {
				exp.setAttribute("name", author);
			}
			
			if (!pubMedId.equals("-")) {
				exp.setReference("publication", getPublication(pubMedId.substring(7)));
			}
			if (!detectioniMethod.equals("-")) {
				exp.addToCollection("interactionDetectionMethods",
						getInteractionTerm(detectioniMethod.substring(0, 7)));
			}

			// extra attributes
			if (host.startsWith("taxid:")) {
				String desc = host.substring(host.indexOf("(") + 1, host.length() - 1);
				if (desc.equals("-")) {
					desc = host.substring(0, host.indexOf("("));
				}
				exp.setAttribute("hostOrganism", desc);
			}

			ret = exp.getIdentifier();
			expMap.put(key, ret);
			store(exp);
		}
		return ret;
	}

	private Set<String> processAltIdentifier(String altIdentifier) {
		Set<String> ret = new HashSet<String>();
		String[] ids = altIdentifier.split("\\|");
		for (String id : ids) {
			if (id.startsWith("entrezgene/locuslink:")) {
				ret.add(id.substring(id.indexOf(":") + 1));
			}
		}
		return ret;
	}

	@Override
	public void close() throws Exception {
		store(sourceMap.values());
	}
}
