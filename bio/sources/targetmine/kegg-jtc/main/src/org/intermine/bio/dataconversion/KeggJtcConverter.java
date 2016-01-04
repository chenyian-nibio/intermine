package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
public class KeggJtcConverter extends FileConverter
{
    //

    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public KeggJtcConverter(ItemWriter writer, Model model) {
        super(writer, model);
    }

    /**
     * 
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
    	getDrugBankIdMap();
    	
		BufferedReader in = new BufferedReader(reader);
		String line;
		String currentClassId = "";
		while ((line = in.readLine()) != null) {
			if (line.startsWith("A")) {
				String replaceAll = line.replaceAll("<\\/?b>", " ");
				String[] split = replaceAll.split("\\s+", 3);
				createJtcClassification(split[1], split[2], null);
			} else if (line.startsWith("D") || line.startsWith("C") || line.startsWith("B")) {
				String[] split = line.split("\\s+", 3);
				String id = split[1];
				String parentId = id.substring(0, id.length()-1);
				createJtcClassification(id, split[2], parentId);
				currentClassId = id;
			} else if (line.startsWith("E")) {
				String keggDrugId = line.substring(9, 15);
				if (currentClassId.length() == 4) {
					Set<String> drugBankIds = drugBankIdMap.get(keggDrugId);
					if (drugBankIds != null) {
						for (String drugBankId : drugBankIds) {
							Item drugCompound = getDrugBankCompound(drugBankId);
							drugCompound.addToCollection("jtcCodes", jtcMap.get(currentClassId));
						}
					} else {
						Item drugCompound = getKeggDrugCompound(keggDrugId);
						drugCompound.addToCollection("jtcCodes", jtcMap.get(currentClassId));
					}

				} else {
					throw new RuntimeException("illeagle format at line: " + line);
				}
			}
		}
		in.close();
    }

    @Override
    public void close() throws Exception {
    	store(drugCompoundMap.values());
    }
    
    private Map<String, Item> drugCompoundMap = new HashMap<String, Item>();
    
    private Item getKeggDrugCompound(String keggDrugId) throws ObjectStoreException {
    	Item ret = drugCompoundMap.get(keggDrugId);
    	if (ret == null) {
    		ret = createItem("DrugCompound");
    		ret.setAttribute("keggDrugId", keggDrugId);
    		drugCompoundMap.put(keggDrugId, ret);
    	}
    	return ret;
    }
    private Item getDrugBankCompound(String drugBankId) throws ObjectStoreException {
    	Item ret = drugCompoundMap.get(drugBankId);
    	if (ret == null) {
    		ret = createItem("DrugCompound");
    		ret.setAttribute("drugBankId", drugBankId);
    		drugCompoundMap.put(drugBankId, ret);
    	}
    	return ret;
    }

    private Map<String, String> jtcMap = new HashMap<String, String>();
    
    private String createJtcClassification(String jtcCode, String name, String parentId) throws ObjectStoreException {
    	String ret = jtcMap.get(jtcCode);
    	if (ret == null) {
    		Item item = createItem("JtcClassification");
    		item.setAttribute("jtcCode", jtcCode);
    		item.setAttribute("name", name);
    		if (parentId != null) {
    			item.setReference("parent", jtcMap.get(parentId));
    		}
    		store(item);
    		ret = item.getIdentifier();
    		jtcMap.put(jtcCode, ret);
    	}
    	return ret;
    }

	private String osAlias = null;

	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

	private Map<String, Set<String>> drugBankIdMap;

	@SuppressWarnings("unchecked")
	private void getDrugBankIdMap() throws Exception {
		drugBankIdMap = new HashMap<String, Set<String>>();

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

			String drugBankId = (String) p.getFieldValue("drugBankId");
			String keggDrugId = (String) p.getFieldValue("keggDrugId");
			
			if (drugBankId != null && keggDrugId != null) {
				if (drugBankIdMap.get(keggDrugId) == null) {
					drugBankIdMap.put(keggDrugId, new HashSet<String>());
				}
				drugBankIdMap.get(keggDrugId).add(drugBankId);
			}

		}
	}
}
