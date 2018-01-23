package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;


/**
 * This parser is for MeSH Descriptor data only. Not applicable for the Supplementary Concept Records and the Qualifier
 * 
 * @author chenyian
 */
public class MeshTermConverter extends BioFileConverter
{
	private static final Logger LOG = Logger.getLogger(MeshTermConverter.class);
	//
    private static final String DATASET_TITLE = "MeSH";
    private static final String DATA_SOURCE_NAME = "NLM";

	private Map<String, Item> meshTreeItemMap = new HashMap<String, Item>();
	
    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public MeshTermConverter(ItemWriter writer, Model model) {
        super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
    }
    
    /**
     * 
     *
     * {@inheritDoc}
     */
	public void process(Reader reader) throws Exception {

		BufferedReader in = null;
		try {
			in = new BufferedReader(reader);
			
			String meshHeading = "";
			String meshIdentifier = "";
			String meshScopeNote = "";
			Set<String> synonyms = new HashSet<String>();
			Set<String> treeNums = new HashSet<String>();
			
			String line;
			int count = 0;
			while ((line = in.readLine()) != null) {
				if ("".equals(line.trim())) {
					if (!"".equals(meshIdentifier) && !"".equals(meshHeading)) {
						Item item = createItem("MeshTerm");
						item.setAttribute("identifier", meshIdentifier);
						item.setAttribute("name", meshHeading);
						if (!"".equals(meshScopeNote)) {
							item.setAttribute("description", meshScopeNote);
						}
			    		item.setReference("ontology", getOntology("MeSH"));
						for (String name : synonyms) {
							Item synonymItem = createItem("OntologyTermSynonym");
							synonymItem.setAttribute("name", name);
							synonymItem.setAttribute("type", "synonym");
							store(synonymItem);
							item.addToCollection("synonyms", synonymItem);
						}
						store(item);
						
						for (String tn : treeNums) {
							Item meshTree = createItem("MeshTree");
							meshTree.setAttribute("number", tn);
							meshTree.setReference("meshTerm", item);
							meshTreeItemMap.put(tn, meshTree);
						}
						count++;
					}
					
					meshHeading = "";
					meshIdentifier = "";
					meshScopeNote = "";
					synonyms = new HashSet<String>();
					treeNums = new HashSet<String>();
					
				} else if (line.startsWith("MH =")) {
					meshHeading = line.trim().substring(5);
				} else if (line.startsWith("MN =")) {
					treeNums.add(line.trim().substring(5));
				} else if (line.startsWith("MS =")) {
					meshScopeNote = line.trim().substring(5);
				} else if (line.startsWith("ENTRY =")) {
					if (line.contains("|")) {
						synonyms.add(line.split("\\|")[0].trim().substring(8));
					} else {
						synonyms.add(line.trim().substring(8));
					}
				} else if (line.startsWith("UI =")) {
					meshIdentifier = line.trim().substring(5);
				}
			}
			System.out.println(String.format("create %d MeSH terms.", count));
			LOG.info(String.format("create %d MeSH terms.", count));
			
		} catch (FileNotFoundException e) {
			LOG.error(e);
		} catch (IOException e) {
			LOG.error(e);
		} finally {
			if (in != null)
				in.close();
		}
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
	
	private Map<String, String> meshTreeRefMap = new HashMap<String, String>();
	
	@Override
	public void close() throws Exception {
		List<String> tnList = new ArrayList<String>(meshTreeItemMap.keySet());
		tnList.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return Integer.valueOf(o1.length()).compareTo(Integer.valueOf(o2.length()));
			}
		});
		
		for (String tn : tnList) {
			Item item = meshTreeItemMap.get(tn);
			String[] split = tn.split("\\.");
			String num = "";
			for (int i = 0; i < split.length - 1; i++) {
				num += split[i];
				String ref = meshTreeRefMap.get(num);
				if (ref != null) {
					item.addToCollection("parents", ref);
				} else {
					LOG.error("Unable to find the reference of the MeshTree: " + tn);
				}
				num += ".";
			}
			store(item);
			meshTreeRefMap.put(tn, item.getIdentifier());
		}
	}
}
