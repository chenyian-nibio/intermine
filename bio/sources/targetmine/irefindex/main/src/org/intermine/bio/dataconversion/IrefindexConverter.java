package org.intermine.bio.dataconversion;

/*
 * Copyright (C) 2002-2009 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * Parser for iRefIndex (http://wodaklab.org/iRefWeb/)
 * 
 * @author chenyian
 */
public class IrefindexConverter extends BioFileConverter {
	//
	private static final String DATASET_TITLE = "iRefIndex";
	private static final String DATA_SOURCE_NAME = "iRefIndex interaction data set";
	private static final String TYPE_FILE = "interactiontype.txt";

	private static final Logger LOG = Logger.getLogger(IrefindexConverter.class);

	private Map<String, String> interactionTypeMap = new HashMap<String, String>();

	// define accepted taxon id
	private List<String> acceptedTaxonIds = Arrays.asList("9606", "7227", "10090", "10116");

	private Map<String, String> geneMap = new HashMap<String, String>();
	private Map<String, String> pubMap = new HashMap<String, String>();
	private Map<String, String> miMap = new HashMap<String, String>();
	private Map<String, String> organismMap = new HashMap<String, String>();
	private Map<MultiKey, String> expMap = new HashMap<MultiKey, String>();

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
	@SuppressWarnings("unchecked")
	public void process(Reader reader) throws Exception {
		readInteractionType();

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

			String geneA = processAltIdentifier(cols[2]);
			if (geneA == null) {
				continue;
			}
			String geneB = processAltIdentifier(cols[3]);
			if (geneB == null) {
				continue;
			}

			// some data from OPHID are not tagged in the author column
			String sourceDb = getMiDesc(cols[12]);
			if (sourceDb.equals("ophid")) {
				continue;
			}
			String[] ids = cols[13].split("\\|");

			String expRefId = getExperiment(cols[8].substring(7), cols[7], cols[6], cols[28],
					sourceDb, ids[0]);

			String intKey = String.format("%s_%s_%s", geneA, geneB, expRefId);

			if (interactions.contains(intKey)) {
				continue;
			}
			
			String conf = cols[14].split("\\|PSICQUIC")[0].trim();

			createInteraction(getMiDesc(cols[18]), geneA, cols[9], geneB, cols[10], cols[11],
					expRefId, getMiDesc(cols[16]), getMiDesc(cols[20]), conf);

			if (!geneA.equals(geneB)) {
				createInteraction(getMiDesc(cols[19]), geneB, cols[10], geneA, cols[9], cols[11],
						expRefId, getMiDesc(cols[17]), getMiDesc(cols[21]), conf);
			}
			interactions.add(intKey);
		}

	}

	private String getMiDesc(String s) {
		return s.substring(8, s.length() - 1);
	}

	private void createInteraction(String role, String geneId, String geneTaxon,
			String interactingGeneId, String interactingGeneTaxon, String miType, String expRefId,
			String bioRole, String interactor, String confidence) throws ObjectStoreException {
		Item interaction = createItem("Interaction");
		interaction.setAttribute("role", role);
		interaction.setReference("gene", getGene(geneId, geneTaxon));
		interaction.addToCollection("interactingGenes", getGene(interactingGeneId,
				interactingGeneTaxon));
		if (!miType.equals("-")) {
			miType = miType.substring(0, 7);
			interaction.setReference("type", getInteractionTerm(miType));
			String intType = interactionTypeMap.get(miType);
			if (intType != null) {
				interaction.setAttribute("interactionType", intType);
			} else {
				LOG.error(String.format("Cannot resolve interaction type: %s", miType));
			}
		}
		interaction.setReference("experiment", expRefId);
		String interactionName = "iRef:" + geneId + "_" + interactingGeneId;
		interaction.setAttribute("name", interactionName);
		interaction.setAttribute("shortName", interactionName);
		
		if (!confidence.isEmpty()) {
			interaction.setAttribute("confidenceText", confidence);
		}

		// 2 new attributes
		interaction.setAttribute("biologicalRole", bioRole);
		interaction.setAttribute("interactorType", interactor);

		store(interaction);
	}

	private void readInteractionType() throws IOException {
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
		if (!taxonField.equals("-")) {
			taxonId = taxonField.substring(6, taxonField.indexOf("("));
		}

		if (itemId == null) {
			Item gene = createItem("Gene");
			gene.setAttribute("ncbiGeneNumber", geneId);
			if (acceptedTaxonIds.contains(taxonId)) {
				gene.setReference("organism", getOrganism(taxonId));
			} else {
				gene.setAttribute("description", "Integrated from iRef dataset, " + taxonField);
			}
			itemId = gene.getIdentifier();
			geneMap.put(geneId, itemId);
			store(gene);
		}
		return itemId;
	}

	private String getOrganism(String taxonId) throws ObjectStoreException {
		String ret = organismMap.get(taxonId);
		if (ret == null) {
			Item item = createItem("Organism");
			item.setAttribute("taxonId", taxonId);
			ret = item.getIdentifier();
			organismMap.put(taxonId, ret);
			store(item);
		}
		return ret;
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
		MultiKey key = new MultiKey(pubMedId, detectioniMethod, sourceId);
		String ret = expMap.get(key);
		if (ret == null) {
			Item exp = createItem("InteractionExperiment");
			if (!author.equals("-")) {
				exp.setAttribute("name", author);
			}
			exp.setReference("publication", getPublication(pubMedId));
			if (!detectioniMethod.equals("-")) {
				exp.addToCollection("interactionDetectionMethods",
						getInteractionTerm(detectioniMethod.substring(0, 7)));
			}

			// new attributes
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

	// private String processAltIdentifier(String altIdentifier) {
	// String ret = null;
	//		
	// Pattern p = Pattern.compile("\\|entrezgene\\/locuslink:(\\d+)\\|");
	// Matcher m = p.matcher(altIdentifier);
	//		
	// if (m.find()) {
	// ret = m.group(1);
	// }
	// return ret;
	// }

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
