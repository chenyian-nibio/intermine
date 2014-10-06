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
 * @author
 */
public class DrpiasConverter extends BioFileConverter
{
    //
    private static final String DATASET_TITLE = "Dr. PIAS";
    private static final String DATA_SOURCE_NAME = "Dr. PIAS";

    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public DrpiasConverter(ItemWriter writer, Model model) {
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
    		Item item = createItem("PpiDruggability");
    		item.setAttribute("identifier", cols[0]);
    		item.setReference("gene1", getGene(cols[1]));
    		item.setReference("gene2", getGene(cols[2]));
			item.setAttribute("structuralScore", cols[3]);
			item.setAttribute("drugChemicalScore", cols[4]);
			item.setAttribute("functionalScore", cols[5]);
			item.setAttribute("allScore", cols[6]);
			store(item);
    	}

    }

    Map<String, String> geneMap = new HashMap<String, String>();
    
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
}
