package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class AmadeusConverter extends BioFileConverter {
//	private static final Logger LOG = Logger.getLogger(AmadeusConverter.class);

	//
	private static final String DATASET_TITLE = "Amadeus";
	private static final String DATA_SOURCE_NAME = "Amadeus";

	private Map<String, String> expMap;

	private Map<String, String> geneMap = new HashMap<String, String>();
	private Map<String, String> publicationMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public AmadeusConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		if (expMap == null) {
			expMap = createExpMap();
		}

		String[] cols = getCurrentFile().getName().split("_");
		String sourceGeneId = cols[0].trim();
		String pubmedId = cols[2].replaceAll("\\.geneid\\.txt", "");

		String intExp = getExperiment(cols[1], pubmedId);

		Set<String> targetGeneIds = new HashSet<String>();
		BufferedReader br = new BufferedReader(reader);
		String line;
		while ((line = br.readLine()) != null) {
			String targetGeneId = line.trim();
			if (targetGeneIds.contains(targetGeneId)) {
				continue;
			}
			createInteraction(sourceGeneId, targetGeneId, intExp);
			targetGeneIds.add(targetGeneId);
		}
		reader.close();
	}

	private Map<String, String> createExpMap() {
		HashMap<String, String> ret = new HashMap<String, String>();
		ret.put("Ex", "gene expression microarrays");
		ret.put("CC", "ChIP-chip");
		ret.put("C-DSL", "ChIP-DSL");
		ret.put("DamID", "DamID");
		ret.put("GO", "Gene Ontology database");
		return ret;
	}

	private void createInteraction(String tfGeneId, String targetGeneId, String extRefId)
			throws ObjectStoreException {
		Item item = createItem("TranscriptionalRegulation");
		item.setAttribute("name", tfGeneId + "->" + targetGeneId);
		item.setReference("transcriptionFactor", getGene(tfGeneId));
		item.setReference("targetGene", getGene(targetGeneId));
		item.addToCollection("experiments", extRefId);
		store(item);
	}

	Map<String, String> expItemMap = new HashMap<String, String>();
	private String getExperiment(String type, String pubmedId) throws ObjectStoreException {
		String key = type + "-" + pubmedId;
		String ret = expItemMap.get(key);
		if (ret == null) {
			Item item = createItem("TFRegulationExperiment");
			item.setReference("publication", getPublication(pubmedId));
			item.setAttribute("title", expMap.get(type));
			store(item);
			ret = item.getIdentifier();
			expItemMap.put(key, ret);
		}
		return ret;
	}

	private String getGene(String primaryIdentifier) throws ObjectStoreException {
		String ret = geneMap.get(primaryIdentifier);
		if (ret == null) {
			Item item = createItem("Gene");
			item.setAttribute("primaryIdentifier", primaryIdentifier);
			item.setAttribute("ncbiGeneId", primaryIdentifier);
			store(item);
			ret = item.getIdentifier();
			geneMap.put(primaryIdentifier, ret);
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

}
