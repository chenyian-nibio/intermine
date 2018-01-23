package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;


/**
 * 
 * @author chenyian
 */
public class EfoMeshConverter extends BioFileConverter
{
	private static final Logger LOG = Logger.getLogger(EfoMeshConverter.class);
    //
//    private static final String DATASET_TITLE = "Add DataSet.title here";
//    private static final String DATA_SOURCE_NAME = "Add DataSource.name here";
	
    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public EfoMeshConverter(ItemWriter writer, Model model) {
        super(writer, model);
    }

    /**
     * 
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
    	BufferedReader in = new BufferedReader(reader);
    	String line;
    	boolean isTerm = false;
    	Item efoTerm = null;
    	while ((line = in.readLine()) != null) {
    		if (line.equals("[Term]")) {
    			isTerm = true;
    		} else if (line.startsWith("id:")) {
    			if (isTerm) {
    				String identifier = line.substring(4);
    				efoTerm = getEFOTerm(identifier);
    			}
    		} else if (line.startsWith("property_value: http://www.ebi.ac.uk/efo/MSH_definition_citation")) {
    			String meshIdentifier = line.substring(line.indexOf("MSH:") + 4, line.indexOf("MSH:") + 11);
    			if (!"".equals(meshIdentifier)) {
    				efoTerm.addToCollection("crossReferences", getMeshTerm(meshIdentifier));
    			}
    		} else if ("".equals(line.trim())) {
    			if (efoTerm != null) {
    				efoTerm = null;
    				isTerm = false;
    			}
    		}
    		
    	}
    }
    
    @Override
    public void close() throws Exception {
    	store(efoTermMap.values());
    }
    
    private Map<String, Item> efoTermMap = new HashMap<String, Item>();
    private Item getEFOTerm(String identifier) throws ObjectStoreException {
    	Item ret = efoTermMap.get(identifier);
    	if (ret == null) {
    		ret = createItem("EFOTerm");
    		ret.setAttribute("identifier", identifier);
    		ret.setReference("ontology", getOntology("EFO"));
    		efoTermMap.put(identifier, ret);
    	}
    	return ret;
    }

    private Map<String, String> meshTermMap = new HashMap<String, String>();
    private String getMeshTerm(String meshIdentifier) throws ObjectStoreException {
    	String ret = meshTermMap.get(meshIdentifier);
    	if (ret == null) {
    		Item item = createItem("MeshTerm");
    		item.setAttribute("identifier", meshIdentifier);
    		item.setReference("ontology", getOntology("MeSH"));
    		store(item);
    		ret = item.getIdentifier();
    		meshTermMap.put(meshIdentifier, ret);
    	}
    	return ret;
    }

    private Map<String, String> ontologyMap = new HashMap<String, String>();
    private String getOntology(String name) throws ObjectStoreException {
    	String ret = ontologyMap.get(name);
    	if (ret == null) {
    		Item item = createItem("Ontology");
    		item.setAttribute("name", name);
    		store(item);
    		ret = item.getIdentifier();
    		ontologyMap.put(name, ret);
    	}
    	return ret;
    }
    
}
