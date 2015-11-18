package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
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
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 * 
 */
public class KeggDrugConverter extends BioFileConverter {
	protected static final Logger LOG = Logger.getLogger(KeggDrugConverter.class);
	//
	private static final String DATASET_TITLE = "KEGG Drug";
	private static final String DATA_SOURCE_NAME = "KEGG";

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public KeggDrugConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	private File inchikeyFile;

	public void setInchikeyFile(File inchikeyFile) {
		this.inchikeyFile = inchikeyFile;
	}
	
	private Map<String, String> inchiKeyMap = new HashMap<String, String>();

	// TODO to prevent duplication, may contain mistakes, but so far have no choice
	private Set<String> foundDrugBankId = new HashSet<String>();

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		readInchikeyFile();
		getDrugBankIdMap();

		BufferedReader in = null;
		try {
			in = new BufferedReader(reader);
			String line;
			String keggDrugId = "";
			String dblDrugBankId = "";
			String name = "";
			String atcCodes = "";
			String casNumber = "";
			Map<String, Set<String>> metabolisms = new HashMap<String, Set<String>>();
			Map<String, Set<String>> interactions = new HashMap<String, Set<String>>();
			
			boolean isMetabolism = false;
			boolean isInteraction = false;
			
			while ((line = in.readLine()) != null) {
				if (line.startsWith("METABOLISM")) {
					isMetabolism = true;
					isInteraction = false;
				} else if (line.startsWith("INTERACTION")) {
					isInteraction = true;
					isMetabolism = false;
				} else if (!line.startsWith(" ")) {
					isInteraction = false;
					isMetabolism = false;
				}
				
				if (line.startsWith("ENTRY")) {
					String[] split = line.split("\\s+");
					keggDrugId = split[1];
				} else if (line.startsWith("NAME")) {
					name = line.substring(12).replaceAll(";$", "").replaceAll("\\s\\(.+?\\)$","").trim();
				} else if (line.contains("ATC code:")) {
					atcCodes = line.substring(line.indexOf(":") + 2);
				} else if (line.contains("DrugBank:")) {
					dblDrugBankId = line.substring(line.indexOf(":") + 2);
				} else if (line.contains("CAS:")) {
					casNumber = line.substring(line.indexOf(":") + 2);
				} else if (isMetabolism) {
					String content = line.substring(12).trim();
					if (!StringUtils.isEmpty(content)) {
						String[] entries;
						Set<String> geneIds = new HashSet<String>();
						String type = "Undefined";
						if (content.contains(": ")) {
							String[] split = content.split(":\\s", 2);
							entries = split[1].split(",\\s");
							type = split[0];
						} else {
							entries = content.split(",\\s");
						}
						for (String entry : entries) {
							// suppose they are all human genes thus formatted like [HSA:xxx xxx]
							int startPos = entry.indexOf("A:");
							if (startPos != -1) {
								String substring = entry.substring(startPos+2, entry.length()-1);
								if (substring.contains(" ")) {
									geneIds.addAll(Arrays.asList(substring.split("\\s")));
								} else {
									geneIds.add(substring);
								}
							}
						}
						if (!geneIds.isEmpty()) {
							metabolisms.put(type, geneIds);
						}
					}
				} else if (isInteraction) {
					String content = line.substring(12).trim();
					if (!StringUtils.isEmpty(content)) {
						String[] entries;
						Set<String> geneIds = new HashSet<String>();
						String type = "Undefined";
						if (content.contains(": ")) {
							String[] split = content.split(":\\s", 2);
							entries = split[1].split(",\\s");
							type = split[0];
						} else {
							entries = content.split(",\\s");
						}
						for (String entry : entries) {
							// suppose they are all human genes thus formatted like [HSA:xxx xxx]
							int startPos = entry.indexOf("A:");
							if (startPos != -1) {
								String substring = entry.substring(startPos+2, entry.length()-1);
								if (substring.contains(" ")) {
									geneIds.addAll(Arrays.asList(substring.split("\\s")));
								} else {
									geneIds.add(substring);
								}
							}
						}
						if (!geneIds.isEmpty()) {
							interactions.put(type, geneIds);
						}
					}
					
				} else if (line.startsWith("///")) {
					String inchiKey = inchiKeyMap.get(keggDrugId);
					
					Set<String> drugBankIds = new HashSet<String>();;
					if (drugBankIdMap.get(keggDrugId) != null) {
						for (String id : drugBankIdMap.get(keggDrugId)) {
							DrugEntry drugEntry = drugEntryMap.get(id);
							int score = 0;
							if (id.equals(dblDrugBankId)) score++;
							if (drugEntry.getInchiKey() != null) {
								String key = drugEntry.getInchiKey();
								if (key.equals(inchiKey)) {
									score++;
								} else if (inchiKeyKeggDrugMap.get(key) == null) {
									score++;
								}
							}
							if (drugEntry.getName() != null && drugEntry.getName().toLowerCase().equals(name.toLowerCase())) score ++;
							if (drugEntry.getCasRegistryNumber() != null && drugEntry.getCasRegistryNumber().equals(casNumber)) score ++;
							// TODO introduce more attributes? e.g. formula, what else?
							if (score > 0) {
								drugBankIds.add(id);
							} else {
								LOG.info(String.format("WARNNING: %s and %s doesn't match; there is a miss-maping in DrugBank.", keggDrugId, id));
							}
						}
					} else if (inchiKey != null && drugBankInchiKeyMap.get(inchiKey) != null) {
						
						for (String id : drugBankInchiKeyMap.get(inchiKey)) {
							DrugEntry drugEntry = drugEntryMap.get(id);
							int score = 0;
							if (id.equals(dblDrugBankId)) score++;
							if (drugEntry.getKeggDrugId() != null && drugEntry.getKeggDrugId().equals(keggDrugId)) score ++;
							if (drugEntry.getName() != null && drugEntry.getName().toLowerCase().equals(name.toLowerCase())) score ++;
							if (drugEntry.getCasRegistryNumber() != null && drugEntry.getCasRegistryNumber().equals(casNumber)) score ++;
							// TODO introduce more attributes? e.g. formula, what else?
							if (score > 0) {
								drugBankIds.add(id);
								LOG.info(String.format("WARNNING(%d): %s and %s were merged together because they share the same InChIKey: %s.", score, keggDrugId, id, inchiKey));
							}
						}
						
					} else if (drugBankNameMap.get(name) != null) {
						// TODO may be dangerous ...
						String id = drugBankNameMap.get(name);
						DrugEntry drugEntry = drugEntryMap.get(id);
						int score = 0;
						if (drugEntry.getKeggDrugId() != null && drugEntry.getKeggDrugId().equals(keggDrugId)) score ++;
						if (drugEntry.getInchiKey() != null && drugEntry.getInchiKey().equals(inchiKey)) score ++;
						if (drugEntry.getCasRegistryNumber() != null && drugEntry.getCasRegistryNumber().equals(casNumber)) score ++;
						// TODO introduce more attributes? e.g. formula, what else?
						if (score > 0) {
							drugBankIds.add(id);
							LOG.info(String.format("WARNNING(%d): %s and %s were merged together because they share the same name: %s.", score, keggDrugId, id, name));
						}
						
					}
					if (!drugBankIds.isEmpty()) {
						for (String drugBankId : drugBankIds) {
							Item drugItem;
							if (foundDrugBankId.contains(drugBankId)) {
								drugItem = createNewDrugCompound(keggDrugId, name, atcCodes,
										casNumber, inchiKey);

							} else {
								drugItem = createItem("DrugCompound");
								drugItem.setAttribute("name", name);
								drugItem.setAttribute("genericName", name);
								drugItem.setAttribute("drugBankId", drugBankId);
								drugItem.setAttribute("keggDrugId", keggDrugId);
								
								if (!atcCodes.equals("")) {
									for (String atcCode : atcCodes.split(" ")) {
										drugItem.addToCollection("atcCodes", getAtcClassification(atcCode, name));
									}
								}
								
								if (!casNumber.equals("")) {
									drugItem.setAttribute("casRegistryNumber", casNumber);
								}
								
								if (inchiKey != null) {
									drugItem.setAttribute("inchiKey", inchiKey);
									drugItem.setReference(
											"compoundGroup",
											getCompoundGroup(inchiKey.substring(0, inchiKey.indexOf("-")), name));
								}
								
								store(drugItem);
								foundDrugBankId.add(drugBankId);
							}
							
							// add metabolisms & interactions
							if (!metabolisms.isEmpty()) {
								for (String key : metabolisms.keySet()) {
									for (String geneId : metabolisms.get(key)) {
										Item item = createItem("DrugMetabolism");
										item.setAttribute("type", key);
										item.setReference("gene", getGene(geneId));
										item.setReference("drug", drugItem);
										store(item);
									}
								}
							}
							if (!interactions.isEmpty()) {
								for (String key : interactions.keySet()) {
									for (String geneId : interactions.get(key)) {
										Item item = createItem("DrugInteraction");
										item.setAttribute("type", key);
										item.setReference("gene", getGene(geneId));
										item.setReference("drug", drugItem);
										store(item);
									}
								}
							}
						}
						
					} else {
						Item drugItem = createNewDrugCompound(keggDrugId, name, atcCodes,
								casNumber, inchiKey);
						
						// add metabolisms & interactions
						if (!metabolisms.isEmpty()) {
							for (String key : metabolisms.keySet()) {
								for (String geneId : metabolisms.get(key)) {
									Item item = createItem("DrugMetabolism");
									item.setAttribute("type", key);
									item.setReference("gene", getGene(geneId));
									item.setReference("drug", drugItem);
									store(item);
								}
							}
						}
						if (!interactions.isEmpty()) {
							for (String key : interactions.keySet()) {
								for (String geneId : interactions.get(key)) {
									Item item = createItem("DrugInteraction");
									item.setAttribute("type", key);
									item.setReference("gene", getGene(geneId));
									item.setReference("drug", drugItem);
									store(item);
								}
							}
						}

					}
					
					// clear current entry
					keggDrugId = "";
					name = "";
					atcCodes = "";
					casNumber = "";
					
					metabolisms = new HashMap<String, Set<String>>();
					interactions = new HashMap<String, Set<String>>();

					isMetabolism = false;
					isInteraction = false;
				}
			}
		} catch (FileNotFoundException e) {
			LOG.error(e);
		} catch (IOException e) {
			LOG.error(e);
		} finally {
			if (in != null)
				in.close();
		}

	}

	private Item createNewDrugCompound(String keggDrugId, String name, String atcCodes,
			String casNumber, String inchiKey) throws ObjectStoreException {
		Item drugItem = createItem("DrugCompound");
		drugItem.setAttribute("keggDrugId", keggDrugId);
		drugItem.setAttribute("name", name);
		drugItem.setAttribute("genericName", name);
		drugItem.setAttribute("identifier", String.format("KEGG DRUG: %s", keggDrugId));
		drugItem.setAttribute("originalId", keggDrugId);
		
		if (!atcCodes.equals("")) {
			for (String atcCode : atcCodes.split(" ")) {
				drugItem.addToCollection("atcCodes", getAtcClassification(atcCode, name));
			}
		}
		
		if (!casNumber.equals("")) {
			drugItem.setAttribute("casRegistryNumber", casNumber);
		}
		
		if (inchiKey != null) {
			drugItem.setAttribute("inchiKey", inchiKey);
			drugItem.setReference(
					"compoundGroup",
					getCompoundGroup(inchiKey.substring(0, inchiKey.indexOf("-")), name));
		}
		
		store(drugItem);
		return drugItem;
	}
	
	private Map<String, Set<String>> inchiKeyKeggDrugMap = new HashMap<String, Set<String>>();
	
	private void readInchikeyFile() {
		try {
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(new FileReader(inchikeyFile));
			
			while(iterator.hasNext()) {
				String[] cols = iterator.next();
				inchiKeyMap.put(cols[0], cols[1]);
				
				if (inchiKeyKeggDrugMap.get(cols[1]) == null) {
					inchiKeyKeggDrugMap.put(cols[1], new HashSet<String>());
				}
				inchiKeyKeggDrugMap.get(cols[1]).add(cols[0]);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private Map<String, String> atcMap = new HashMap<String, String>();

	private String getAtcClassification(String atcCode, String name) throws ObjectStoreException {
		String ret = atcMap.get(atcCode);
		if (ret == null) {
			Item item = createItem("AtcClassification");
			item.setAttribute("atcCode", atcCode);
			item.setAttribute("name", name);
			// TODO add parent
			String parentCode = atcCode.substring(0, 5);
			item.setReference("parent", getParent(parentCode));

			store(item);
			ret = item.getIdentifier();
			atcMap.put(atcCode, ret);
		}
		return ret;
	}

	private String getParent(String parentCode) throws ObjectStoreException {
		String ret = atcMap.get(parentCode);
		if (ret == null) {
			Item item = createItem("AtcClassification");
			item.setAttribute("atcCode", parentCode);
			store(item);
			ret = item.getIdentifier();
			atcMap.put(parentCode, ret);
		}
		return ret;
	}

	private Map<String, String> compoundGroupMap = new HashMap<String, String>();
	
	private String getCompoundGroup(String inchiKey, String name) throws ObjectStoreException {
		String ret = compoundGroupMap.get(inchiKey);
		if (ret == null) {
			Item item = createItem("CompoundGroup");
			item.setAttribute("identifier", inchiKey);
			item.setAttribute("name", name);
			store(item);
			ret = item.getIdentifier();
			compoundGroupMap.put(inchiKey, ret);
		}
		return ret;
	}

	private String osAlias = null;

	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

	private Map<String, Set<String>> drugBankIdMap = new HashMap<String, Set<String>>();;
	private Map<String, Set<String>> drugBankInchiKeyMap = new HashMap<String, Set<String>>();
	private Map<String, String> drugBankNameMap = new HashMap<String, String>();
	private Map<String, DrugEntry> drugEntryMap = new HashMap<String, DrugEntry>();

	@SuppressWarnings("unchecked")
	private void getDrugBankIdMap() throws Exception {
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
			String inchiKey = (String) p.getFieldValue("inchiKey");
			String name = (String) p.getFieldValue("name");
			String casRegistryNumber = (String) p.getFieldValue("casRegistryNumber");
			
			DrugEntry drugEntry = new DrugEntry(drugBankId, keggDrugId, name, inchiKey, casRegistryNumber);
			drugEntryMap.put(drugBankId, drugEntry);
			
			if (drugBankId != null) {
				if (keggDrugId != null) {
					if (drugBankIdMap.get(keggDrugId) == null) {
						drugBankIdMap.put(keggDrugId, new HashSet<String>());
					}
					drugBankIdMap.get(keggDrugId).add(drugBankId);
				}
				if (inchiKey != null) {
					if (drugBankInchiKeyMap.get(inchiKey) == null) {
						drugBankInchiKeyMap.put(inchiKey, new HashSet<String>());
					}
					drugBankInchiKeyMap.get(inchiKey).add(drugBankId);
//					if (drugBankInchiKeyMap.get(inchiKey) != null) {
//						LOG.info("Duplicated InChIKey, check the data sources: " + inchiKey + " (" + drugBankId + ")");
//						throw new RuntimeException("Duplicated InChIKey, check the data sources: " + inchiKey + " (" + drugBankId + ")");
//					}
//					drugBankInchiKeyMap.put(inchiKey, drugBankId);
				}
				if (name != null) {
					if (drugBankNameMap.get(name) != null) {
						LOG.info("Duplicated name, check the data sources: " + name + " (" + drugBankId + ")");
						throw new RuntimeException("Duplicated name, check the data sources: " + name + " (" + drugBankId + ")");
					}
					drugBankNameMap.put(name, drugBankId);
				}
			}

		}
	}

	private Map<String, String> geneMap = new HashMap<String, String>();
	
	private String getGene(String geneId) throws ObjectStoreException {
		String ret = geneMap.get(geneId);
		if (ret == null) {
			Item item = createItem("Gene");
			item.setAttribute("primaryIdentifier", geneId);
			item.setAttribute("ncbiGeneId", geneId);
			store(item);
			ret = item.getIdentifier();
			geneMap.put(geneId, ret);
		}
		return ret;
	}

	private static class DrugEntry {
		private String drugBankId;
		private String keggDrugId;
		private String name;
		private String inchiKey;
		private String casRegistryNumber;

		public DrugEntry(String drugBankId, String keggDrugId, String name, String inchiKey,
				String casRegistryNumber) {
			super();
			this.drugBankId = drugBankId;
			this.keggDrugId = keggDrugId;
			this.name = name;
			this.inchiKey = inchiKey;
			this.casRegistryNumber = casRegistryNumber;
		}

		String getDrugBankId() {
			return drugBankId;
		}

		String getKeggDrugId() {
			return keggDrugId;
		}

		String getName() {
			return name;
		}

		String getInchiKey() {
			return inchiKey;
		}

		String getCasRegistryNumber() {
			return casRegistryNumber;
		}

	}

}
