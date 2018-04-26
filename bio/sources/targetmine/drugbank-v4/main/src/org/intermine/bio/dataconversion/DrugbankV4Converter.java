package org.intermine.bio.dataconversion;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;

/**
 * 
 * This parser will parse DrugBank xml file, full_database.xml. (version 5.0.1)</br>
 * 
 * 2016/9/29 refined the 'references' section parsing, due to the format changes in version 5.0.1
 * 
 * @author chenyian
 */
public class DrugbankV4Converter extends BioFileConverter {
	private static Logger LOG = Logger.getLogger(DrugbankV4Converter.class);
	//

	private static final String DATASET_TITLE = "DrugBank";
	private static final String DATA_SOURCE_NAME = "DrugBank";

	private static final String NAMESPACE_URI = "http://www.drugbank.ca";

	private Map<String, String> proteinMap = new HashMap<String, String>();
	private Map<String, String> publicationMap = new HashMap<String, String>();
	private Map<String, String> drugTypeMap = new HashMap<String, String>();
	private Map<String, String> compoundGroupMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public DrugbankV4Converter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		Builder parser = new Builder();
		Document doc = parser.build(reader);

		Elements drugElements = doc.getRootElement().getChildElements("drug", NAMESPACE_URI);
		for (int i = 0; i < drugElements.size(); i++) {
			Element drug = drugElements.get(i);

			// String drugBankId = drug.getFirstChildElement("drugbank-id",
			// NAMESPACE_URI).getValue();
			Elements idElements = drug.getChildElements("drugbank-id", NAMESPACE_URI);
			List<String> alias = new ArrayList<String>();
			String drugBankId = "";
			for (int j = 0; j < idElements.size(); j++) {
				String primary = idElements.get(j).getAttributeValue("primary");
				if (primary != null && primary.equals("true")) {
					drugBankId = idElements.get(j).getValue();
				} else {
					alias.add(idElements.get(j).getValue());
				}
			}
			if (drugBankId.equals("")) {
				continue;
			}
			
			Map<String,String> structureMap = new HashMap<String, String>();

			Item drugItem = createItem("DrugCompound");
			drugItem.setAttribute("drugBankId", drugBankId);
			drugItem.setAttribute("identifier", String.format("DrugBank: %s", drugBankId));
			drugItem.setAttribute("originalId", drugBankId);
			String name = drug.getFirstChildElement("name", NAMESPACE_URI).getValue();
			// if the length of the name is greater than 40 characters,
			// use id instead and save the long name as the synonym
			if (name.length() > 40) {
				setSynonyms(drugItem, name, "name");
				name = drugBankId;
			}

			for (String synonym : alias) {
				setSynonyms(drugItem, synonym, "DrugBank accession");
			}

			drugItem.setAttribute("name", name);
			drugItem.setAttribute("genericName", name);
			String casReg = drug.getFirstChildElement("cas-number", NAMESPACE_URI).getValue();
			if (!StringUtils.isEmpty(casReg)) {
				drugItem.setAttribute("casRegistryNumber", casReg);
			}
			String desc = drug.getFirstChildElement("description", NAMESPACE_URI).getValue().trim();
			if (!StringUtils.isEmpty(desc)) {
				drugItem.setAttribute("description", desc);
			}

			// inchikey
			Element cpNode = drug.getFirstChildElement("calculated-properties", NAMESPACE_URI);
			if (cpNode != null) {
				Elements properties = cpNode.getChildElements("property", NAMESPACE_URI);
				for (int j = 0; j < properties.size(); j++) {
					Element p = properties.get(j);
					if (p.getFirstChildElement("kind", NAMESPACE_URI).getValue().toLowerCase()
							.equals("inchikey")) {
						String inchiKey = p.getFirstChildElement("value", NAMESPACE_URI).getValue();
						// not necessary now... (2017.6.5)
//						inchiKey = inchiKey.substring(inchiKey.indexOf("=") + 1);
						drugItem.setAttribute("inchiKey", inchiKey);
						drugItem.setReference(
								"compoundGroup",
								getCompoundGroup(inchiKey.substring(0, inchiKey.indexOf("-")), name));

						// assign inchikey as synonym
//						setSynonyms(drugItem, inchiKey);

					} else if (p.getFirstChildElement("kind", NAMESPACE_URI).getValue().toLowerCase().equals("inchi")) {
						String value = p.getFirstChildElement("value", NAMESPACE_URI).getValue();
						structureMap.put("InChI", value);
					} else if (p.getFirstChildElement("kind", NAMESPACE_URI).getValue().toLowerCase().equals("smiles")) {
						String value = p.getFirstChildElement("value", NAMESPACE_URI).getValue();
						structureMap.put("SMILES", value);
					}
				}
			}
			// 4 types
			List<String> proteinTypes = Arrays.asList("target", "enzyme", "transporter", "carrier");
			for (String proteinType : proteinTypes) {
				Elements targets = drug.getFirstChildElement(proteinType + "s", NAMESPACE_URI)
						.getChildElements(proteinType, NAMESPACE_URI);
				for (int j = 0; j < targets.size(); j++) {
					Element t = targets.get(j);

					// retrieve actions
					Elements actions = t.getFirstChildElement("actions", NAMESPACE_URI)
							.getChildElements("action", NAMESPACE_URI);
					List<String> actionValues = new ArrayList<String>();
					for (int k = 0; k < actions.size(); k++) {
						String action = actions.get(k).getValue();
						actionValues.add(action);
					}

					Element polypeptide = t.getFirstChildElement("polypeptide", NAMESPACE_URI);
					// String id = t.getAttribute("partner").getValue();
					if (polypeptide != null) {
						String id = polypeptide.getAttributeValue("id");

						Item interaction = createItem("DrugBankInteraction");
						interaction.setReference("protein", getProtein(id));
						interaction.setReference("compound", drugItem);
						Elements articles = t.getFirstChildElement("references", NAMESPACE_URI)
								.getFirstChildElement("articles", NAMESPACE_URI)
								.getChildElements("article", NAMESPACE_URI);
						for (int x = 0; x < articles.size(); x++) {
							Element article = articles.get(x);
							String pubmedId = article.getFirstChildElement("pubmed-id", NAMESPACE_URI).getValue();
							if (!StringUtils.isEmpty(pubmedId)) {
								interaction.addToCollection("publications", getPublication(pubmedId));
							} else {
								LOG.info("pubmed-id not available: " + drugBankId);
							}
						}
						
						if (actionValues.size() > 0) {
							for (String action : actionValues) {
								interaction.addToCollection("actions", getDrugAction(action.trim()
										.toLowerCase()));
							}
							Collections.sort(actionValues);
							interaction.setAttribute("actionLabel", StringUtils.join(actionValues,", "));
						} else {
							interaction.addToCollection("actions", getDrugAction("unknown"));
						}
						interaction.setAttribute("proteinType", proteinType);
						store(interaction);
					}
				}

			}

			// get brand names
			// in v4.2 the collection "brands" has been changed to "international-brands" 
			Elements brands = drug.getFirstChildElement("international-brands", NAMESPACE_URI).getChildElements(
					"international-brand", NAMESPACE_URI);
			for (int j = 0; j < brands.size(); j++) {
				String brandName = brands.get(j).getFirstChildElement("name", NAMESPACE_URI).getValue();
				setSynonyms(drugItem, brandName, "brand name");
			}
			// get synonyms
			Elements synonyms = drug.getFirstChildElement("synonyms", NAMESPACE_URI)
					.getChildElements("synonym", NAMESPACE_URI);
			for (int j = 0; j < synonyms.size(); j++) {
				String synonym = synonyms.get(j).getValue();
				setSynonyms(drugItem, synonym, "synonym");
			}

			drugItem.addToCollection("drugTypes", getDrugType(drug.getAttribute("type").getValue()));
			// get groups (store as DrugType but in a different collection)
			Elements groups = drug.getFirstChildElement("groups", NAMESPACE_URI).getChildElements(
					"group", NAMESPACE_URI);
			for (int j = 0; j < groups.size(); j++) {
				String group = groups.get(j).getValue();
				drugItem.addToCollection("drugGroups", getDrugType(group));
			}

			// get ATC codes
			Elements atcCodes = drug.getFirstChildElement("atc-codes", NAMESPACE_URI)
					.getChildElements("atc-code", NAMESPACE_URI);
			for (int j = 0; j < atcCodes.size(); j++) {
				String atcCode = atcCodes.get(j).getAttributeValue("code");
				if (atcCode.length() != 7) {
					LOG.error(String.format("Invalid atc code, id: %s, code: %s", drugBankId,
							atcCode));
					continue;
				}
				drugItem.addToCollection("atcCodes", getAtcClassification(atcCode, name));
			}

			// get uiprot id if the drug is a protein
			Elements extIds = drug.getFirstChildElement("external-identifiers", NAMESPACE_URI)
					.getChildElements("external-identifier", NAMESPACE_URI);
			for (int j = 0; j < extIds.size(); j++) {
				Element e = extIds.get(j);
				if (e.getFirstChildElement("resource", NAMESPACE_URI).getValue().toLowerCase()
						.equals("uniprotkb")) {
					drugItem.setReference("protein",
							getProtein(e.getFirstChildElement("identifier", NAMESPACE_URI)
									.getValue()));
				} else if (e.getFirstChildElement("resource", NAMESPACE_URI).getValue()
						.toLowerCase().equals("kegg drug")) {
					drugItem.setAttribute("keggDrugId",
							e.getFirstChildElement("identifier", NAMESPACE_URI).getValue());
				}
			}
			store(drugItem);

			for (String key: structureMap.keySet()) {
				Item structure = createItem("CompoundStructure");
				structure.setAttribute("type", key);
				structure.setAttribute("value", structureMap.get(key));
				structure.setReference("compound", drugItem);
				store(structure);
			}
		}

	}

	private Map<String, String> atcMap = new HashMap<String, String>();

	private String getAtcClassification(String atcCode, String name) throws ObjectStoreException {
		String ret = atcMap.get(atcCode);
		if (ret == null) {
			Item item = createItem("AtcClassification");
			item.setAttribute("atcCode", atcCode);
			item.setAttribute("name", name);
			// add parent
			String parentCode = atcCode.substring(0, 5);
			item.setReference("parent", getParent(parentCode));
			
			// create parents; to be improved
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

	private Map<String, String> actionMap = new HashMap<String, String>();

	private String getDrugAction(String action) throws ObjectStoreException {
		String ret = actionMap.get(action);
		if (ret == null) {
			Item item = createItem("DrugAction");
			item.setAttribute("type", action);
			store(item);
			ret = item.getIdentifier();
			actionMap.put(action, ret);
		}
		return ret;
	}

	private String getProtein(String uniprotId) throws ObjectStoreException {
		String ret = proteinMap.get(uniprotId);
		if (ret == null) {
			Item item = createItem("Protein");
			item.setAttribute("primaryAccession", uniprotId);
			store(item);
			ret = item.getIdentifier();
			proteinMap.put(uniprotId, ret);
		}
		return ret;
	}

	private String getPublication(String pubMedId) throws ObjectStoreException {
		String ret = publicationMap.get(pubMedId);
		if (ret == null) {
			Item item = createItem("Publication");
			item.setAttribute("pubMedId", pubMedId);
			store(item);
			ret = item.getIdentifier();
			publicationMap.put(pubMedId, ret);
		}
		return ret;
	}

	private String getDrugType(String name) throws ObjectStoreException {
		String ret = drugTypeMap.get(name);
		if (ret == null) {
			Item item = createItem("DrugType");
			item.setAttribute("name", name);
			store(item);
			ret = item.getIdentifier();
			drugTypeMap.put(name, ret);
		}
		return ret;
	}

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

	private void setSynonyms(Item subject, String value, String type) throws ObjectStoreException {
		Item syn = createItem("CompoundSynonym");
		syn.setAttribute("value", value);
		syn.setAttribute("type", type);
		syn.setReference("subject", subject);
		store(syn);
	}

}
