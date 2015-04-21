package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
 * <p><u>2012.11.29</u> : Refactoring to fit the new Interaction model (InterMine 1.1)</p>
 * <p><u>2014.04.15</u> : Refactoring to reduce the redundant interactions</p>
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
	private List<String> interactions = new ArrayList<String>();

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
			String[] ids = cols[13].split("\\|");

			String geneA = processAltIdentifier(cols[2]);
			if (geneA == null) {
				continue;
			}
			String geneB = processAltIdentifier(cols[3]);
			if (geneB == null) {
				continue;
			}
			
			// in case there are redundant entries
			String[] pmids = cols[8].split("\\|");
			
			List<String> expRefIds = new ArrayList<String>();
			for (String pmid : pmids) {
				expRefIds.add(getExperiment(pmid, cols[7], cols[6], cols[28],
						sourceDb, ids[0]));
			}
			Collections.sort(expRefIds);
			String intKey = String.format("%s_%s_%s", geneA, geneB, StringUtils.join(expRefIds,"_"));
			if (interactions.contains(intKey)) {
				continue;
			}
			
			String geneARef = getGene(geneA, cols[9]);
			String geneBRef = getGene(geneB, cols[10]);
			String role1 = getMiDesc(cols[18]);
			String role2 = getMiDesc(cols[19]);

			Item interaction = getInteraction(geneARef, geneBRef);
			
			for (String expRefId : expRefIds) {
				Item detail = createItem("InteractionDetail");
				
				if (role1 != null) {
					detail.setAttribute("role1", role1);
				}
				if (role2 != null) {
					detail.setAttribute("role2", role2);
				}
				if (!cols[11].equals("-")) {
					String miType = cols[11].substring(0, 7);
					detail.setReference("relationshipType", getInteractionTerm(miType));
					// physical or genetic
					String intType = interactionTypeMap.get(miType);
					if (intType != null) {
						detail.setAttribute("type", intType);
					} else {
						LOG.error(String.format("Cannot resolve interaction type: %s", miType));
					}
				}
				detail.setReference("experiment", expRefId);
				detail.setAttribute("name", String.format("iRef:%s_%s", geneA, geneB));
				
				detail.addToCollection("allInteractors", geneARef);
				detail.addToCollection("allInteractors", geneBRef);
				
				// 2 extra attributes
				detail.setAttribute("biologicalRole", String.format("1:%s-2:%s", getMiDesc(cols[16]), getMiDesc(cols[17])));
				detail.setAttribute("interactorType", String.format("1:%s-2:%s", getMiDesc(cols[20]), getMiDesc(cols[21])));
				
				detail.setReference("interaction", interaction);
				
				store(detail);
				
				if (!geneA.equals(geneB)) {
					Item detail2 = createItem("InteractionDetail");
					
					if (role1 != null) {
						detail2.setAttribute("role2", role1);
					}
					if (role2 != null) {
						detail2.setAttribute("role1", role2);
					}
					if (!cols[11].equals("-")) {
						String miType = cols[11].substring(0, 7);
						detail2.setReference("relationshipType", getInteractionTerm(miType));
						// physical or genetic
						String intType = interactionTypeMap.get(miType);
						if (intType != null) {
							detail2.setAttribute("type", intType);
						} else {
							LOG.error(String.format("Cannot resolve interaction type: %s", miType));
						}
					}
					detail2.setReference("experiment", expRefId);
					detail2.setAttribute("name", String.format("iRef:%s_%s", geneB, geneA));
					
					detail2.addToCollection("allInteractors", geneARef);
					detail2.addToCollection("allInteractors", geneBRef);
					
					// 2 extra attributes
					detail2.setAttribute("biologicalRole", String.format("1:%s-2:%s", getMiDesc(cols[17]), getMiDesc(cols[16])));
					detail2.setAttribute("interactorType", String.format("1:%s-2:%s", getMiDesc(cols[21]), getMiDesc(cols[20])));
					
					detail2.setReference("interaction", interaction);
					
					store(detail2);
				}
			}
			
			interactions.add(intKey);
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

    @Deprecated
	private void createInteraction(String role1, String gene1Ref, String role2, String gene2Ref,
			String miType, List<String> expRefIds, String bioRole, String interactor, String name)
			throws ObjectStoreException {
		Item interaction = createItem("Interaction");
		interaction.setReference("gene1", gene1Ref);
		interaction.setReference("gene2", gene2Ref);
		store(interaction);
		
		for (String expRefId : expRefIds) {
			// create Detail item
			Item detail = createItem("InteractionDetail");
			
			detail.setAttribute("role1", role1);
			detail.setAttribute("role2", role2);
			
			if (!miType.equals("-")) {
				miType = miType.substring(0, 7);
				detail.setReference("relationshipType", getInteractionTerm(miType));
				String intType = interactionTypeMap.get(miType);
				// physical or genetic
				if (intType != null) {
					detail.setAttribute("type", intType);
				} else {
					LOG.error(String.format("Cannot resolve interaction type: %s", miType));
				}
			}
			detail.setReference("experiment", expRefId);
			detail.setAttribute("name", name);
			detail.setReference("interaction", interaction);
			
			detail.addToCollection("allInteractors", gene1Ref);
			detail.addToCollection("allInteractors", gene2Ref);
			
			// 2 extra attributes
			detail.setAttribute("biologicalRole", bioRole);
			detail.setAttribute("interactorType", interactor);
			
			store(detail);
			
		}

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
			gene.setAttribute("ncbiGeneId", geneId);
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
			String host, String sourceDb, String sourceId) throws ObjectStoreException {
		// chenyian: seems that one experiment only associates with on detection method so far (2015.4.15) 
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
			exp.setAttribute("sourceDb", sourceDb);
			exp.setAttribute("sourceId", sourceId);

			ret = exp.getIdentifier();
			expMap.put(key, ret);
			store(exp);
		}
		return ret;
	}

	private String processAltIdentifier(String altIdentifier) {
		String[] ids = altIdentifier.split("\\|");
		for (String id : ids) {
			if (id.startsWith("entrezgene/locuslink:")) {
				return id.substring(id.indexOf(":") + 1);
			}
		}
		return null;
	}

}
