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
 */
public class PredictedDbpConverter extends BioFileConverter
{
    private static final String ANNOTATION_TYPE = "DNA binding";
	//

	private Map<String, String> proteinMap = new HashMap<String, String>();
	
	
    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public PredictedDbpConverter(ItemWriter writer, Model model) {
        super(writer, model);
    }

    /**
     * 
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
    	Iterator<String[]> iterator = FormattedTextParser.parseDelimitedReader(reader, ' ');
    	// skip header ..
    	iterator.next();
    	while (iterator.hasNext()) {
    		String[] cols = iterator.next();
    		String accession = cols[0];
    		Float consensus2 = Float.valueOf(cols[4]);
    		Float precision = Float.valueOf(cols[12]);
    		
    		if (consensus2 >= 0.12f) {
    			String confidence = "medium";
    			if (consensus2 >= 0.22) {
    				confidence = "high";
    			}
    			Item item = createItem("PredictedAnnotation");
    			item.setAttribute("type", ANNOTATION_TYPE);
    			item.setAttribute("confidence", confidence);
    			item.setAttribute("score", precision.toString());
    			item.setReference("protein", getProtein(accession));
    			store(item);
    		}
    	}
    }
    
	private String getProtein(String primaryAccession) throws ObjectStoreException {
		String ret = proteinMap.get(primaryAccession);
		if (ret == null) {
			Item item = createItem("Protein");
			item.setAttribute("primaryAccession", primaryAccession);
			store(item);
			ret = item.getIdentifier();
			proteinMap.put(primaryAccession, ret);
		}
		return ret;
	}

}
