package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

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

	private static final String INTERACTION_TYPE = "Transcriptional regulation";

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
		String sourceGeneId = cols[0];
		String pubmedId = cols[2].replaceAll("\\.geneid\\.txt", "");

		Item intExp = createItem("ProteinDNAExperiment");
		intExp.setReference("publication", getPublication(pubmedId));
		intExp.setAttribute("name", cols[1]);
		intExp.setAttribute("description", expMap.get(cols[1]));

		BufferedReader br = new BufferedReader(reader);
		String ncbiGeneId;
		while ((ncbiGeneId = br.readLine()) != null) {
			if (ncbiGeneId.equals(cols[0])) {
				// create Interaction
				createInteraction(sourceGeneId, sourceGeneId, "source&target", intExp);

			} else {
				// create Interaction for source
				createInteraction(sourceGeneId, ncbiGeneId, "source", intExp);

				// create Interaction for target
				createInteraction(ncbiGeneId, sourceGeneId, "target", intExp);
			}
		}
		reader.close();

		store(intExp);

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

	private void createInteraction(String masterId, String slaveId, String role,
			Item interactionExperiment) throws ObjectStoreException {
		Item item = createItem("ProteinDNAInteraction");
		item.setReference("gene", getGene(masterId));
		item.setReference("interactWith", getGene(slaveId));

		item.setAttribute("interactionType", INTERACTION_TYPE);
		item.setAttribute("name", String.format("AMADEUS_G%s_G%s", masterId, slaveId));
		item.setAttribute("role", role);
		item.setReference("experiment", interactionExperiment);
		store(item);
	}

	private String getGene(String ncbiGeneId) throws ObjectStoreException {
		String ret = geneMap.get(ncbiGeneId);
		if (ret == null) {
			Item item = createItem("Gene");
			item.setAttribute("ncbiGeneId", ncbiGeneId);
			store(item);
			ret = item.getIdentifier();
			geneMap.put(ncbiGeneId, ret);
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
