package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * Read data from the flat file 'drug' from kegg drug. This parser is  
 * supposed to be run after drugbank, if both of them are required. 
 * 
 * @author chenyian
 * 
 */
public class KeggDrugConverter extends BioFileConverter {
	protected static final Logger LOG = Logger.getLogger(KeggDrugConverter.class);
	//
	private static final String DATASET_TITLE = "KEGG Drug";
	private static final String DATA_SOURCE_NAME = "KEGG";

	private Set<String> synonymTypeSet = new HashSet<String>();
	
	private Set<String> therapeuticLabels = new HashSet<String>();
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
//		JAN - Japanese Approved Name
//		USAN - United States Adopted Name
//		NF - National Formulary drug name
//		INN - International Nonproprietary Name
//		BAN - British Approved Name
//		DCF - Denomination commune francaise
//		JP17 - Japanese Pharmacopoeia, 17th edition
//		USP - United States Pharmacopeia
//		non-JP - Japanese standards for non-Pharmacopoeial crude drugs
		synonymTypeSet.addAll(Arrays.asList("JAN", "USAN", "NF", "INN", "BAN", "DCF", "JP17",
				"USP", "non-JP"));
		
	}

	private File inchikeyFile;
	private File inchiFile;

	public void setInchikeyFile(File inchikeyFile) {
		this.inchikeyFile = inchikeyFile;
	}
	public void setInchiFile(File inchiFile) {
		this.inchiFile = inchiFile;
	}
	
	private Map<String, String> inchiKeyMap = new HashMap<String, String>();
	private Map<String, String> inchiMap = new HashMap<String, String>();

	// chenyian: for keeping unique drug compound entries
	private Set<String> foundDrugBankId = new HashSet<String>();

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		readInchikeyFile();
		readInchiFile();
		getDrugBankIdMap();
		createAllTherapeuticClassifications();

		BufferedReader in = null;
		try {
			in = new BufferedReader(reader);
			String line;
			String keggDrugId = "";
			String dblDrugBankId = "";
			String name = "";
			List<String> allNames = new ArrayList<String>();
			String atcCodes = "";
			String casNumber = "";
			Map<String, Set<String>> metabolisms = new HashMap<String, Set<String>>();
			Map<String, Set<String>> interactions = new HashMap<String, Set<String>>();
			
			boolean isName = false;
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
				
				if (isName) {
					if (line.startsWith(" ")) {
						allNames.add(line.substring(12).trim());
					} else {
						isName = false;
					}
				}
				
				if (line.startsWith("ENTRY")) {
					String[] split = line.split("\\s+");
					keggDrugId = split[1];
				} else if (line.startsWith("NAME")) {
					// TODO to be refined
					name = line.substring(12).replaceAll(";$", "").replaceAll("\\s\\(.+?\\)$","").trim();
					allNames.add(line.substring(12).trim());
					isName = true;
				} else if (line.contains("ATC code:")) {
					atcCodes = line.substring(line.indexOf(":") + 2);
				} else if (line.contains("DrugBank:")) {
					dblDrugBankId = line.substring(line.indexOf(":") + 2);
				} else if (line.contains("CAS:")) {
					casNumber = line.substring(line.indexOf(":") + 2);
				} else if (line.contains(".html]")) {
					String refHtml = line.substring(line.indexOf("[br") + 1, line.indexOf(".html]"));
					if (therapeuticClassMap.keySet().contains(refHtml)) {
						therapeuticLabels.add(refHtml);
					}
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
								} else {
									// if the inchikey could be found in another kegg drug, that one may be a better candidate.
									// thus minus one. This is to prevent the multiple mapping in kegg drug.
									score--;
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
					List<Item> drugCompounds = new ArrayList<Item>();
					if (!drugBankIds.isEmpty()) {
						for (String drugBankId : drugBankIds) {
							if (foundDrugBankId.contains(drugBankId)) {
								drugCompounds.add(createDrugCompound(
										String.format("KEGG DRUG: %s", keggDrugId), drugBankId,
										keggDrugId, name, atcCodes, casNumber, inchiKey, keggDrugId));

							} else {
								drugCompounds.add(createDrugCompound(
										String.format("DrugBank: %s", drugBankId), drugBankId,
										keggDrugId, name, atcCodes, casNumber, inchiKey, drugBankId));
								foundDrugBankId.add(drugBankId);
							}
						}
						
					} else {
						drugCompounds.add(createDrugCompound(
								String.format("KEGG DRUG: %s", keggDrugId), dblDrugBankId, keggDrugId, name,
								atcCodes, casNumber, inchiKey, keggDrugId));
					}
					// add metabolisms & interactions
					for (Item drugItem : drugCompounds) {
						if (!metabolisms.isEmpty()) {
							for (String key : metabolisms.keySet()) {
								for (String geneId : metabolisms.get(key)) {
									saveMetabolismAndInteraction("DrugMetabolism", key, geneId, drugItem);
								}
							}
						}
						if (!interactions.isEmpty()) {
							for (String key : interactions.keySet()) {
								for (String geneId : interactions.get(key)) {
									saveMetabolismAndInteraction("DrugInteraction", key, geneId, drugItem);
								}
							}
						}
						if (!allNames.isEmpty()) {
							for (String drugName: allNames) {
								// example: Dexamethasone (JP17/USP/INN);
								if (drugName.contains("(")) {
									Pattern pattern = Pattern.compile("(.+) \\((.+?)\\);?$");
									Matcher matcher = pattern.matcher(drugName);
									
									if (matcher.find()) {
										String value = matcher.group(1);
										String types = matcher.group(2);
										for (String type: types.split("/")) {
											Item syn = createItem("CompoundSynonym");
											syn.setAttribute("value", value);
											if (synonymTypeSet.contains(type) ||
													isSynonymType(type)) {
												syn.setAttribute("type", type);
											}
											syn.setReference("subject", drugItem);
											store(syn);
										}
									}
									
								} else {
									Item syn = createItem("CompoundSynonym");
									syn.setAttribute("value", drugName.replaceAll(";$", "").trim());
									syn.setReference("subject", drugItem);
									store(syn);
								}
							}
						}
					}
					
					// clear current entry
					keggDrugId = "";
					dblDrugBankId = "";
					name = "";
					allNames = new ArrayList<String>();
					atcCodes = "";
					casNumber = "";
					
					therapeuticLabels.clear();
					
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
	
	private boolean isSynonymType(String type) {
		for (String st : synonymTypeSet) {
			if (type.startsWith(st) || type.endsWith(st)) {
				return true;
			}
		}
		return false;
	}

	private Item createDrugCompound(String identifier, String drugBankId, String keggDrugId,
			String name, String atcCodes, String casNumber, String inchiKey, String originalId)
			throws ObjectStoreException {
		Item drugItem = createItem("DrugCompound");
		// the most important, the primary key
		drugItem.setAttribute("identifier", identifier);
		drugItem.setAttribute("keggDrugId", keggDrugId);
		if (drugBankId != null && !drugBankId.equals("")) {
			drugItem.setAttribute("drugBankId", drugBankId);
		}
		if (StringUtils.isEmpty(name)) {
			name = "KEGG Drug " + keggDrugId;
			LOG.info("No name for KEGG Drug: " + keggDrugId);
		}
		drugItem.setAttribute("name", name);
		drugItem.setAttribute("genericName", name);
		drugItem.setAttribute("originalId", originalId);

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
			drugItem.setReference("compoundGroup",
					getCompoundGroup(inchiKey.substring(0, inchiKey.indexOf("-")), name));
		}
		
		for (String label : therapeuticLabels) {
			drugItem.addToCollection("therapeuticClassifications", therapeuticClassMap.get(label));
		}
		
		store(drugItem);

		String inchi = inchiMap.get(keggDrugId);
		if (inchi != null) {
			Item structure = createItem("CompoundStructure");
			structure.setAttribute("type", "InChI");
			structure.setAttribute("value", inchi);
			structure.setReference("compound", drugItem);
			store(structure);
		}
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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void readInchiFile() {
		try {
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(new FileReader(inchiFile));
			
			while(iterator.hasNext()) {
				String[] cols = iterator.next();
				inchiMap.put(cols[0], cols[1]);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
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

			// TODO create parents; to be improved
			item.addToCollection("allParents", getParent(parentCode));
			item.addToCollection("allParents", getParent(parentCode.substring(0, 4)));
			item.addToCollection("allParents", getParent(parentCode.substring(0, 3)));
			item.addToCollection("allParents", getParent(parentCode.substring(0, 1)));

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
	
	private void saveMetabolismAndInteraction(String itemName, String key, String geneId,
			Item drugItem) throws ObjectStoreException {
		Item item = createItem(itemName);
		item.setAttribute("type", key);
		item.setReference("gene", getGene(geneId));
		item.setReference("drug", drugItem);
		store(item);

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

		@SuppressWarnings("unused")
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
	
	Map<String, String> therapeuticClassMap = new HashMap<String, String>(); 
	
	private void createTherapeuticClassification(String identifier, String name) throws ObjectStoreException {
		Item item = createItem("TherapeuticClassification");
		item.setAttribute("identifier", identifier);
		item.setAttribute("name", name);
		store(item);
		therapeuticClassMap.put(identifier, item.getIdentifier());
	}
	
	private void createAllTherapeuticClassifications() throws ObjectStoreException {
		createTherapeuticClassification("br08340", "Antineoplastics");
		createTherapeuticClassification("br08350", "Antibacterials");
		createTherapeuticClassification("br08351", "Antivirals");
		createTherapeuticClassification("br08352", "Antifungals");
		createTherapeuticClassification("br08353", "Antiparasitics");
		createTherapeuticClassification("br08361", "Antidiabetics");
		createTherapeuticClassification("br08365", "Hypolipidemic agents");
		createTherapeuticClassification("br08368", "Osteoporosis drugs");
		createTherapeuticClassification("br08364", "Cardiovascular agents");
		createTherapeuticClassification("br08363", "Psychiatric agents");
		createTherapeuticClassification("br08367", "Neurological agents");
		createTherapeuticClassification("br08362", "Anti-allergic agents");
		createTherapeuticClassification("br08366", "Antirheumatic and antigout drugs");
		createTherapeuticClassification("br08369",
				"Anesthetics, analgesics and anti-inflammatory drugs");
		createTherapeuticClassification("br08372", "Respiratory agents");
		createTherapeuticClassification("br08371", "Gastrointestinal agents");
		createTherapeuticClassification("br08373", "Endocrine and hormonal agents");
		createTherapeuticClassification("br08370", "Dermatological agents");
	}	 

}
