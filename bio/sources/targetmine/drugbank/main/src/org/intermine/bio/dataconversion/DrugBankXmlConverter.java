package org.intermine.bio.dataconversion;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import org.apache.commons.lang.StringUtils;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;

/**
 * Since the so called drugcard format was deprecated by DrugBank themselves, the old parser which
 * was developed by Ishikawa-san was replaced with this one.
 * 
 * This parser will parse DrugBank data from xml file.
 * 
 * @author chenyian
 * 
 * @since 2012/8/15
 */
public class DrugBankXmlConverter extends BioFileConverter {

	// private static Logger LOG = Logger.getLogger(DrugBankConverter.class);

	private static final String DATASET_TITLE = "DrugBank";
	private static final String DATA_SOURCE_NAME = "DrugBank";

	private static final String NAMESPACE_URI = "http://drugbank.ca";

	private Map<String, String> proteinMap = new HashMap<String, String>();
	private Map<String, String> publicationMap = new HashMap<String, String>();
	private Map<String, String> drugTypeMap = new HashMap<String, String>();
	private Map<String, String> compoundGroupMap = new HashMap<String, String>();

	public DrugBankXmlConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	@Override
	public void process(Reader reader) throws Exception {
		Builder parser = new Builder();
		Document doc = parser.build(reader);

		Map<String, String> idMap = new HashMap<String, String>();
		Elements partnerElements = doc.getRootElement().getChildElements("partners", NAMESPACE_URI)
				.get(0).getChildElements();
		for (int i = 0; i < partnerElements.size(); i++) {
			Element part = partnerElements.get(i);
			String refId = part.getAttribute("id").getValue();
			String uniprotId = null;
			Elements extIds = part.getFirstChildElement("external-identifiers", NAMESPACE_URI)
					.getChildElements("external-identifier", NAMESPACE_URI);
			for (int j = 0; j < extIds.size(); j++) {
				Element e = extIds.get(j);
				if (e.getFirstChildElement("resource", NAMESPACE_URI).getValue().toLowerCase()
						.equals("uniprotkb")) {
					uniprotId = e.getFirstChildElement("identifier", NAMESPACE_URI).getValue();
				}
			}
			if (uniprotId != null) {
				idMap.put(refId, uniprotId);
			}
		}

		Elements drugElements = doc.getRootElement().getChildElements("drug", NAMESPACE_URI);
		for (int i = 0; i < drugElements.size(); i++) {
			Element drug = drugElements.get(i);
			Item drugItem = createItem("DrugCompound");
			String drugBankId = drug.getFirstChildElement("drugbank-id", NAMESPACE_URI).getValue();
			drugItem.setAttribute("drugBankId", drugBankId);
			drugItem.setAttribute("identifier", String.format("DrugBank: %s", drugBankId));
			String name = drug.getFirstChildElement("name", NAMESPACE_URI).getValue();
			drugItem.setAttribute("name", name);
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
						inchiKey = inchiKey.substring(inchiKey.indexOf("=") + 1);
						drugItem.setAttribute("inchiKey", inchiKey);
						drugItem.setReference(
								"compoundGroup",
								getCompoundGroup(inchiKey.substring(0, inchiKey.indexOf("-")), name));
					}
				}
			}

			// get target proteins
			Elements targets = drug.getFirstChildElement("targets", NAMESPACE_URI)
					.getChildElements("target", NAMESPACE_URI);
			for (int j = 0; j < targets.size(); j++) {
				Element t = targets.get(j);
				String id = t.getAttribute("partner").getValue();
				if (idMap.get(id) != null) {
					Item interaction = createItem("DrugBankInteraction");
					interaction.setReference("protein", getProtein(idMap.get(id)));
					interaction.setReference("compound", drugItem);
					// interaction.setReference("dataSet", dat);
					// get reference (pubmed id)
					String ref = t.getFirstChildElement("references", NAMESPACE_URI).getValue();
					Pattern pattern = Pattern
							.compile("\"Pubmed\":http://www.ncbi.nlm.nih.gov/pubmed/(\\d+)");
					Matcher matcher = pattern.matcher(ref);
					while (matcher.find()) {
						interaction.addToCollection("publications",
								getPublication(matcher.group(1)));
					}
					store(interaction);
				}
			}

			// get brand names
			Elements brands = drug.getFirstChildElement("brands", NAMESPACE_URI).getChildElements(
					"brand", NAMESPACE_URI);
			for (int j = 0; j < brands.size(); j++) {
				String brandName = brands.get(j).getValue();
				Item bn = createItem("BrandName");
				bn.setAttribute("name", brandName);
				bn.setReference("compound", drugItem);
				store(bn);
			}

			drugItem.addToCollection("drugTypes", getDrugType(drug.getAttribute("type").getValue()));
			// get groups (for DrugType)
			Elements groups = drug.getFirstChildElement("groups", NAMESPACE_URI).getChildElements(
					"group", NAMESPACE_URI);
			for (int j = 0; j < groups.size(); j++) {
				String group = groups.get(j).getValue();
				drugItem.addToCollection("drugTypes", getDrugType(group));
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
				}
			}
			store(drugItem);

		}

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

}
