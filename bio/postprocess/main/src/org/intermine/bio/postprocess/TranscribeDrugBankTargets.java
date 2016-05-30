package org.intermine.bio.postprocess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;

/**
 * 
 * @author chenyian
 *
 */
public class TranscribeDrugBankTargets {

	private static final Logger LOG = Logger.getLogger(TranscribeDrugBankTargets.class);
	
	protected ObjectStoreWriter osw;

	private Model model;

	public TranscribeDrugBankTargets(ObjectStoreWriter osw) {
		this.osw = osw;
		model = Model.getInstanceByName("genomic");
	}
	
	@SuppressWarnings("unchecked")
	public void transcribe() {
		Results results = getDrugCompounds();
		
		System.out.println(String.format("found %d drug compounds in database", results.size()));
		LOG.info(String.format("found %d drug compounds in database", results.size()));
		
		Iterator<?> iterator = results.iterator();
		
		try {
			osw.beginTransaction();

			List<InterMineObject> keggDrugItems = new ArrayList<InterMineObject>();
			Map<String, InterMineObject> drugBankItems = new HashMap<String, InterMineObject>();
			while (iterator.hasNext()) {
				ResultsRow<?> result = (ResultsRow<?>) iterator.next();
				InterMineObject compound = (InterMineObject) result.get(0);
				
				String identifier = (String) compound.getFieldValue("identifier");
				
				if (identifier.startsWith("DrugBank:")) {
					String dbid = identifier.substring(identifier.indexOf(":") + 2);
					drugBankItems.put(dbid, compound);

					// chenyian: some kegg entries still reference to deprecated drugbank ids, 
					// "D00002" -> "DB01907"; "D00304" -> "DB00510"; "D00954" -> "DB00506" (identified on May, 2016)
					// try to find from synonyms ...
					// maybe I have considered too much... 3 entries may not be important
					Collection<InterMineObject> synonyms = (Collection<InterMineObject>) compound.getFieldValue("synonyms");
					for (InterMineObject syn : synonyms) {
						String name = (String) syn.getFieldValue("value");
						if (name.length() == 7 && name.startsWith("DB")) {
							if (drugBankItems.get(name) == null) {
								drugBankItems.put(name, compound);
							} else {
								System.out.println("duplicated drugbank id! " + name);
								LOG.info("duplicated drugbank id! " + name);
							}
						}
					}
				} else {
					keggDrugItems.add(compound);
				}
				
			}
			
//			System.out.println("toBeProcessed.size(): " + keggDrugItems.size());
//			System.out.println("drugBankItems.size(): " + drugBankItems.size());
			
			int count = 0;
			for (InterMineObject kdi : keggDrugItems) {
				String dbid = (String) kdi.getFieldValue("drugBankId");
				if (dbid != null) {
					InterMineObject compound = drugBankItems.get(dbid);
					if (compound != null) {
						Collection<InterMineObject> cpis = (Collection<InterMineObject>) compound.getFieldValue("targetProteins");
						for (InterMineObject cpi : cpis) {
							InterMineObject copyObj = PostProcessUtil.copyInterMineObject(cpi);
							copyObj.setFieldValue("compound", kdi);
							osw.store(copyObj);
						}
					} else {
						System.out.println("unable to find drugbank entry " + dbid);
						LOG.info("unable to find drugbank entry " + dbid);
					}
					count++;
				}
			}
			System.out.println("processed number: " + count);
			
//			osw.abortTransaction();
			osw.commitTransaction();

		} catch (ObjectStoreException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	private Results getDrugCompounds() {
		Query q = new Query();
		QueryClass qcCompound = new QueryClass(model.getClassDescriptorByName(
				"DrugCompound").getType());
		
		q.addFrom(qcCompound);
		q.addToSelect(qcCompound);
		
		ObjectStore os = osw.getObjectStore();

		return os.execute(q);
	}

}
