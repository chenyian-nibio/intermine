package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class OregannoConverter extends BioFileConverter {
	// private static final Logger LOG = Logger.getLogger(OregannoConverter.class);

	//
	private static final String DATASET_TITLE = "ORegAnno";
	private static final String DATA_SOURCE_NAME = "ORegAnno";

	private static final String INTERACTION_TYPE = "Transcriptional regulation";

	private Map<String, String> geneMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public OregannoConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(reader));

		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			// source target sequence start end chromosome strand genomeBuild stableId
			String sourceId = cols[0].trim();
			String targetId = cols[1].trim();
			if (sourceId == null || sourceId.equals("")) {
				continue;
			}
			Item bindingStie = getBindingSite(cols);

			if (targetId.equals(sourceId)) {
				// create Interaction
				getInteraction(sourceId, sourceId, "source&target").addToCollection("bindingSites",
						bindingStie);

			} else {
				// create Interaction for source
				getInteraction(sourceId, targetId, "source").addToCollection("bindingSites",
						bindingStie);

				// create Interaction for target
				getInteraction(targetId, sourceId, "target").addToCollection("bindingSites",
						bindingStie);
			}

		}
		reader.close();
	}

	private Item getBindingSite(String[] cols) throws ObjectStoreException {
		// TODO chenyian to be refined; use the default sequence ontology (SO) model
		Item ret = createItem("BindingSiteInfo");
		ret.setAttribute("sequence", cols[2]);
		if (!cols[3].equals("N/A")) {
			ret.setAttribute("start", cols[3]);
		}
		if (!cols[4].equals("N/A")) {
			ret.setAttribute("end", cols[4]);
		}
		if (!cols[5].equals("N/A")) {
			ret.setAttribute("chromosome", cols[5]);
		}
		if (!cols[6].equals("N/A")) {
			ret.setAttribute("strand", cols[6]);
		}
		if (!cols[7].equals("N/A")) {
			ret.setAttribute("genomeBuild", cols[7]);
		}
		ret.setAttribute("stableId", cols[8]);
		store(ret);
		return ret;
	}

	Map<String, Item> interactionMap = new HashMap<String, Item>();

	private Item getInteraction(String masterId, String slaveId, String role)
			throws ObjectStoreException {
		String key = slaveId + role + masterId;
		Item ret = interactionMap.get(key);
		if (ret == null) {
			ret = createItem("ProteinDNAInteraction");
			ret.setReference("gene", getGene(masterId));
			ret.setReference("interactWith", getGene(slaveId));

			ret.setAttribute("interactionType", INTERACTION_TYPE);
			ret.setAttribute("role", role);
			interactionMap.put(key, ret);
		}
		return ret;
	}

	private String getGene(String ncbiGeneId) throws ObjectStoreException {
		String ret = geneMap.get(ncbiGeneId);
		if (ret == null) {
			Item item = createItem("Gene");
			item.setAttribute("primaryIdentifier", ncbiGeneId);
			item.setAttribute("ncbiGeneId", ncbiGeneId);
			store(item);
			ret = item.getIdentifier();
			geneMap.put(ncbiGeneId, ret);
		}
		return ret;
	}

	@Override
	public void close() throws Exception {
		store(interactionMap.values());
	}

}
