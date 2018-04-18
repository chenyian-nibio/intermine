package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.intermine.dataconversion.FileConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.xml.full.Item;


/**
 * 
 * @author chenyian
 */
public class KeggJtcConverter extends FileConverter {
	
	protected static final Logger LOG = Logger.getLogger(KeggJtcConverter.class);
    //
	Map<String, Integer> codeMap = new HashMap<String, Integer>();
	
    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public KeggJtcConverter(ItemWriter writer, Model model) {
        super(writer, model);
        codeMap.put("A", 1);
        codeMap.put("B", 2);
        codeMap.put("C", 3);
        codeMap.put("D", 4);
    }

    /**
     * 
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
    	if (drugCompoundIdMap == null || drugCompoundIdMap.isEmpty()) {
    		getDrugBankIdMap();
    	}
    	
		String currentFileName = getCurrentFile().getName();
		if (currentFileName.equals("br08301.keg")) {
			BufferedReader in = new BufferedReader(reader);
			String line;
			String currentClassId = "";
			while ((line = in.readLine()) != null) {
				if (line.startsWith("A")) {
					String replaceAll = line.replaceAll("<\\/?b>", " ");
					String[] split = replaceAll.split("\\s+", 3);
					createJscClassification(split[1], split[2], null);
				} else if (line.startsWith("D") || line.startsWith("C") || line.startsWith("B")) {
					String[] split = line.split("\\s+", 3);
					String id = split[1];
					String parentId = id.substring(0, id.length()-1);
					createJscClassification(id, split[2], parentId);
					currentClassId = id;
				} else if (line.startsWith("E")) {
					String keggDrugId = line.substring(9, 15);
					if (currentClassId.length() == 4) {
						Set<String> identifiers = drugCompoundIdMap.get(keggDrugId);
						if (identifiers != null) {
							for (String drugCompoundId : identifiers) {
								Item drugCompound = getDrugCompound(drugCompoundId);
								drugCompound.addToCollection("jsccCodes", jsccMap.get(currentClassId));
							}
						} else {
							LOG.info("Unfound entry: " + keggDrugId);
							Item drugCompound = getKeggDrugCompound(keggDrugId);
							drugCompound.addToCollection("jsccCodes", jsccMap.get(currentClassId));
						}

					} else {
						throw new RuntimeException("illeagle format at line: " + line);
					}
				}
			}
			in.close();
		} else if (currentFileName.equals("br08302.keg")) {
			// #DEFINITION  USP Drug Classification
			BufferedReader in = new BufferedReader(reader);
			String line;
//			List<String> parents = new ArrayList<String>();
			Map<String, String> parentMap = new HashMap<String, String>();
			while ((line = in.readLine()) != null) {
				if (line.startsWith("A")) {
					// reset all parents
//					parents.clear();
					parentMap.clear();
					String classA = createUspClassification("A", line.substring(1), null);
//					parents.add(classA);
					parentMap.put("A", classA);
				} else if (line.substring(0, 1).matches("[BCD]")) {
					if (line.matches(".+\\sD\\d{5}\\s{2}.+")) {
						String[] split = line.split("\\s+", 3);
						String keggDrugId = split[1].trim();
						Set<String> identifiers = drugCompoundIdMap.get(keggDrugId);
						if (!parentMap.isEmpty()) {
				    		List<String> keys = new ArrayList<String>(parentMap.keySet());
				    		Collections.sort(keys);
				    		String parentId = parentMap.get(keys.get(keys.size() - 1));
							if (identifiers != null) {
								for (String drugCompoundId : identifiers) {
									Item drugCompound = getDrugCompound(drugCompoundId);
									drugCompound.addToCollection("uspClassifications", parentId);
								}
							} else {
								LOG.info("Unfound entry: " + keggDrugId);
								Item drugCompound = getKeggDrugCompound(keggDrugId);
								drugCompound.addToCollection("uspClassifications", parentId);
							}
						}
					} else {
						String[] split = line.split("\\s+", 2);
						if (split.length == 2) {
							String name = split[1];
							name = name.replaceAll("\\[DG:DG.+\\]", "");
							String code = line.substring(0, 1);
//							if (parentMap.containsKey(code)) {
//								parentMap.remove(code);
//							}
							String classId = createUspClassification(code, name.trim(), parentMap);
//							parents.add(classId);
							parentMap.put(code, classId);
						}
					}
				}	
			}
			in.close();
		} else {
			System.out.println("Unexpected file: " + currentFileName + ", skip it.");
			return;
		}
    	
    }

    @Override
    public void close() throws Exception {
    	store(drugCompoundMap.values());
    }
    
    private Map<String, Item> drugCompoundMap = new HashMap<String, Item>();

    // Though this should be never used
    private Item getKeggDrugCompound(String keggDrugId) throws ObjectStoreException {
    	Item ret = drugCompoundMap.get(keggDrugId);
    	if (ret == null) {
    		ret = createItem("DrugCompound");
    		ret.setAttribute("identifier", String.format("KEGG DRUG: %s", keggDrugId));
    		ret.setAttribute("keggDrugId", keggDrugId);
    		drugCompoundMap.put(keggDrugId, ret);
    	}
    	return ret;
    }
    
    private Item getDrugCompound(String identifier) throws ObjectStoreException {
    	Item ret = drugCompoundMap.get(identifier);
    	if (ret == null) {
    		ret = createItem("DrugCompound");
    		ret.setAttribute("identifier", identifier);
    		drugCompoundMap.put(identifier, ret);
    	}
    	return ret;
    }

    private Map<String, String> jsccMap = new HashMap<String, String>();
    
    private String createJscClassification(String jsccCode, String name, String parentId) throws ObjectStoreException {
    	String ret = jsccMap.get(jsccCode);
    	if (ret == null) {
    		Item item = createItem("JscClassification");
    		item.setAttribute("jsccCode", jsccCode);
    		item.setAttribute("name", name);
    		if (parentId != null) {
    			item.setReference("parent", jsccMap.get(parentId));
    			
    			for (int i = 0; i < parentId.length(); i++) {
    				item.addToCollection("allParents", jsccMap.get(parentId.substring(0, i + 1)));
				}
    			item.addToCollection("allParents", item);
    		}
    		store(item);
    		ret = item.getIdentifier();
    		jsccMap.put(jsccCode, ret);
    	}
    	return ret;
    }

    private String createUspClassification(String code, String name, Map<String, String> parentMap) throws ObjectStoreException {
    	Item item = createItem("UspClassification");
    	item.setAttribute("name", name);
    	Integer value = codeMap.get(code);
    	if (parentMap != null && parentMap.size() > 0) {
    		List<String> keys = new ArrayList<String>(parentMap.keySet());
    		Collections.sort(keys);
    		item.setReference("parent", parentMap.get(keys.get(value - 2)));
    		for (int i = 0; i < value - 1; i++) {
    			item.addToCollection("allParents", parentMap.get(keys.get(i)));
			}
    	}
		item.setAttribute("lv", value.toString());
    	store(item);
    	String ret = item.getIdentifier();
    	return ret;
    }

	private String osAlias = null;

	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

	private Map<String, Set<String>> drugCompoundIdMap;

	@SuppressWarnings("unchecked")
	private void getDrugBankIdMap() throws Exception {
		drugCompoundIdMap = new HashMap<String, Set<String>>();

		ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);

		Query q = new Query();
		QueryClass qcDrugCompound = new QueryClass(os.getModel()
				.getClassDescriptorByName("DrugCompound").getType());


		q.addFrom(qcDrugCompound);

		q.addToSelect(qcDrugCompound);

		Results results = os.execute(q);
		Iterator<Object> iterator = results.iterator();
		while (iterator.hasNext()) {
			ResultsRow<InterMineObject> rr = (ResultsRow<InterMineObject>) iterator.next();
			InterMineObject p = rr.get(0);

			String identifier = (String) p.getFieldValue("identifier");
			String keggDrugId = (String) p.getFieldValue("keggDrugId");
			
			if (identifier != null && keggDrugId != null) {
				if (drugCompoundIdMap.get(keggDrugId) == null) {
					drugCompoundIdMap.put(keggDrugId, new HashSet<String>());
				}
				drugCompoundIdMap.get(keggDrugId).add(identifier);
			}

		}
	}
}
