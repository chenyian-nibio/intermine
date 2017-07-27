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
 * 
 * @author chenyian
 *
 */
public class PredictedTftConverter extends BioFileConverter
{
//	private static final Logger LOG = Logger.getLogger(PredictedTftConverter.class);

	//
	private static final String DATASET_TITLE = "ENCODE ChIP-seq data";
	private static final String DATA_SOURCE_NAME = "ENCODE";

	private Map<String, String> geneMap = new HashMap<String, String>();
	private Map<String, String> chromosomeMap = new HashMap<String, String>();
	private Map<String, String> interactionMap = new HashMap<String, String>();

    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public PredictedTftConverter(ItemWriter writer, Model model) {
        super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
    }

    /**
     * 
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
    	Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
    	while (iterator.hasNext()) {
    		String[] cols = iterator.next();
    		
    		String sourceId = cols[3];
    		String targetId = cols[9];
    		
    		String chrRef = getChromosome(cols[0], cols[4]);
    		
			Item location = createItem("Location");
			Integer startValue = Integer.valueOf(cols[5]);
			location.setAttribute("start", String.valueOf(startValue));
			Integer endValue = Integer.valueOf(cols[6]);
			location.setAttribute("end", String.valueOf(endValue));
			if (chrRef != null) {
				location.setReference("locatedOn", chrRef);
			}
			store(location);

			String tfRef = getInteraction(sourceId, targetId);
			
			Item chipSeqData = createItem("ChipSeqData");
			chipSeqData.setAttribute("epigenomeName", cols[1]);
			chipSeqData.setAttribute("distance", cols[8]);
			chipSeqData.setReference("bindingLocation", location);
			chipSeqData.setReference("transcriptionalRegulation", tfRef);
			store(chipSeqData);
    	}

    }
    
    private String getInteraction(String tfGeneId, String targetGeneId) throws ObjectStoreException {
    	String key = tfGeneId + "->" + targetGeneId;
		String ret = interactionMap.get(key);
		if (ret == null) {
			Item item = createItem("TranscriptionalRegulation");
			item.setAttribute("name", key);
			item.setReference("transcriptionFactor", getGene(tfGeneId));
			item.setReference("targetGene", getGene(targetGeneId));
			store(item);
			ret = item.getIdentifier();
			interactionMap.put(key, ret);
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

	private String getChromosome(String taxonId, String symbol) throws ObjectStoreException {
		String key = taxonId + "-" + symbol;
		String ret = chromosomeMap.get(key);
		if (ret == null) {
			Item chromosome = createItem("Chromosome");
			chromosome.setReference("organism", getOrganism(taxonId));
			chromosome.setAttribute("symbol", symbol);
			store(chromosome);
			ret = chromosome.getIdentifier();
			chromosomeMap.put(key, ret);
		}
		return ret;
	}

}
