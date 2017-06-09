package org.intermine.bio.dataconversion;

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
 * Parser for HtriDB (http://www.lbbc.ibb.unesp.br/htri)
 * 
 * @author chenyian
 */
public class HtridbConverter extends BioFileConverter
{
    //
    private static final String DATASET_TITLE = "HTRI Database";
    private static final String DATA_SOURCE_NAME = "HTRI Database";

	private Map<String, String> geneMap = new HashMap<String, String>();
	private Map<String, String> publicationMap = new HashMap<String, String>();

	/**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public HtridbConverter(ItemWriter writer, Model model) {
        super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
    }

    /**
     * 
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
    	// the csv file is not really comma separated, the delimiter is semicolon
    	Iterator<String[]> iterator = FormattedTextParser.parseDelimitedReader(reader, ';');
    	// ignore first header line
    	iterator.next();
    	while (iterator.hasNext()) {
    		String[] cols = iterator.next();
    		String expRefId = getExperiment(cols[5], cols[6]);
    		String sourceId = cols[1];
    		String targetId = cols[3];
    		
    		getInteraction(sourceId, targetId).addToCollection("experiments", expRefId);
    	}
    }

	Map<String,Item> interactionMap = new HashMap<String, Item>();

	private Item getInteraction(String tfGeneId, String targetGeneId) throws ObjectStoreException {
		String key = tfGeneId + "->" + targetGeneId;
		Item ret = interactionMap.get(key);
		if (ret == null) {
			ret = createItem("TranscriptionalRegulation");
			ret.setAttribute("name", key);
			ret.setReference("transcriptionFactor", getGene(tfGeneId));
			ret.setReference("targetGene", getGene(targetGeneId));
			
			interactionMap.put(key, ret);
		}
		return ret;
	}

	Map<String, String> expItemMap = new HashMap<String, String>();
	private String getExperiment(String title, String pubmedId) throws ObjectStoreException {
		String key = title + "-" + pubmedId;
		String ret = expItemMap.get(key);
		if (ret == null) {
			Item item = createItem("TFRegulationExperiment");
			item.setReference("publication", getPublication(pubmedId));
			item.setAttribute("title", title);
			store(item);
			ret = item.getIdentifier();
			expItemMap.put(key, ret);
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

	@Override
	public void close() throws Exception {
		store(interactionMap.values());
	}
}
