package org.intermine.bio.dataconversion;

import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.intermine.dataconversion.FileConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;


/**
 * 
 * @author chenyian
 */
public class ProteinOrthologConverter extends FileConverter
{
    //
	
	Map<String, Item> proteinMap = new HashMap<String, Item>();
	
    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public ProteinOrthologConverter(ItemWriter writer, Model model) {
        super(writer, model);
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
			Item itemA = getProtein(cols[0]);
			Item itemB = getProtein(cols[1]);
			itemA.addToCollection("orthologProteins", itemB);
			itemB.addToCollection("orthologProteins", itemA);
		}
    }
    
	private Item getProtein(String primaryIdentifier) throws ObjectStoreException {
		Item ret = proteinMap.get(primaryIdentifier);
		if (ret == null) {
			ret = createItem("Protein");
			ret.setAttribute("primaryIdentifier", primaryIdentifier);
			proteinMap.put(primaryIdentifier, ret);
		}
		return ret;
	}
	
	@Override
	public void close() throws Exception {
		store(proteinMap.values());
	}
    
}
