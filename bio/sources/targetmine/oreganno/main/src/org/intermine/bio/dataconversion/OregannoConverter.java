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
public class OregannoConverter extends BioFileConverter
{
//	private static final Logger LOG = Logger.getLogger(OregannoConverter.class);

	//
    private static final String DATASET_TITLE = "ORegAnno";
    private static final String DATA_SOURCE_NAME = "ORegAnno";

	private static final String INTERACTION_TYPE = "Transcriptional regulation";

	private Map<String, String> geneMap = new HashMap<String, String>();
	
	
    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
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
				createInteraction(sourceId, sourceId, "source&target", bindingStie);

			} else {
				// create Interaction for source
				createInteraction(sourceId, targetId, "source", bindingStie);

				// create Interaction for target
				createInteraction(targetId, sourceId, "target", bindingStie);
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

	private void createInteraction(String masterId, String slaveId, String role,
			Item bindingSite) throws ObjectStoreException {
		Item item = createItem("ProteinDNAInteraction");
		item.setReference("gene", getGene(masterId));
		item.setReference("interactWith", getGene(slaveId));

		item.setAttribute("interactionType", INTERACTION_TYPE);
		item.setAttribute("name", String.format("AMADEUS_G%s_G%s", masterId, slaveId));
		item.setAttribute("role", role);
		item.setReference("bindingSite", bindingSite);
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

}
