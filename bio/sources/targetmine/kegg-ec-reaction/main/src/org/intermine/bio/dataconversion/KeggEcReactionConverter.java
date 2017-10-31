package org.intermine.bio.dataconversion;

import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * 
 * @author chenyian
 */
public class KeggEcReactionConverter extends BioFileConverter {
	protected static final Logger LOG = Logger.getLogger(KeggEcReactionConverter.class);
	//
	private static final String DATASET_TITLE = "KEGG Pathway";
	private static final String DATA_SOURCE_NAME = "KEGG";

	private Map<String, Item> compoundMap = new HashMap<String, Item>();
	private Map<String, String> enzymeMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public KeggEcReactionConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
//		if (reactionNameMap.isEmpty()) {
//			readReactionNameFile();
//		}

		String fileName = getCurrentFile().getName();
		// String currentOrganismCode = fileName.substring(0, 3);
		String pathwayId = fileName.substring(0, fileName.indexOf("."));
		Item pathwayItem = createItem("Pathway");
		pathwayItem.setAttribute("identifier", pathwayId);
		store(pathwayItem);
		String pathwayRef = pathwayItem.getIdentifier();

		// To suppress the accessing to kegg's DTD.
		// Due to the number of the kgml files, sometimes the parsing process may gets 403 forbidden
		// from kegg.
		XMLReader xmlreader = XMLReaderFactory.createXMLReader();
		xmlreader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
				false);
		Builder parser = new Builder(xmlreader);
		Document doc = parser.build(reader);

		Element rootElement = doc.getRootElement();

		// System.out.println(rootElement.getAttributeValue("name"));

		Elements entryElements = rootElement.getChildElements("entry");
		Map<String, Set<String>> enzymeMap = new HashMap<String, Set<String>>();
		for (int i = 0; i < entryElements.size(); i++) {
			Element entry = entryElements.get(i);
			String name = entry.getAttributeValue("name");
			String type = entry.getAttributeValue("type");
			String reaction = entry.getAttributeValue("reaction");
			if (type.equals("enzyme") && reaction != null) {
				if (enzymeMap.get(reaction) == null) {
					enzymeMap.put(reaction, new HashSet<String>());
				}
				String[] split = name.split("\\s");
				for (String ecString : split) {
					enzymeMap.get(reaction).add(ecString.substring(3));
				}
			}
		}

		Elements reactionElements = rootElement.getChildElements("reaction");
		for (int i = 0; i < reactionElements.size(); i++) {
			Element reaction = reactionElements.get(i);
			Item reactionItem = createItem("Reaction");
			// rn:Rxxxxx, used to get the enzyme
			String rnId = reaction.getAttributeValue("name");
//			String reactionName = reactionNameMap.get(rnId.substring(3));
//			if (reactionName != null) {
//				reactionItem.setAttribute("name", reactionName);
//			}
			// reversible or irreversible
			String reactionType = reaction.getAttributeValue("type");
			reactionItem.setAttribute("reactionType", reactionType);

			Set<String> compoundIds = new HashSet<String>();

			Set<String> substrateIds = new HashSet<String>();
			Elements substrates = reaction.getChildElements("substrate");
			for (int k = 0; k < substrates.size(); k++) {
				// name="cpd:Cxxxxx"; other cases: "dr:D04197 cpd:C11736" or "gl:G01391"
				String name = substrates.get(k).getAttributeValue("name");
				String[] cids = name.split("\\s");
				boolean flag = true;
				for (String cid : cids) {
					if (cid.startsWith("cpd:")) {
						String id = cid.substring(4);
						reactionItem.addToCollection("substrates", getKeggCompound(id));
						compoundIds.add(id);
						if (flag) {
							substrateIds.add(id);
							flag = false;
						}
					}
				}
			}

			Set<String> productIds = new HashSet<String>();
			Elements products = reaction.getChildElements("product");
			for (int k = 0; k < products.size(); k++) {
				// name="cpd:Cxxxxx"; other cases: "dr:D04197 cpd:C11736" or "gl:G01391"
				String name = products.get(k).getAttributeValue("name");
				String[] cids = name.split("\\s");
				boolean flag = true;
				for (String cid : cids) {
					if (cid.startsWith("cpd:")) {
						String id = cid.substring(4);
						reactionItem.addToCollection("products", getKeggCompound(id));
						compoundIds.add(id);
						if (flag) {
							productIds.add(id);
							flag = false;
						}
					}
				}
			}

			if (enzymeMap.get(rnId) != null) {
				Iterator<String> iterator = enzymeMap.get(rnId).iterator();
				while (iterator.hasNext()) {
					String ecNumber = iterator.next();
					reactionItem.addToCollection("enzymes", getEnzyme(ecNumber));
				}

			} else {
				LOG.info(String.format("Cannot get ec number: %s", rnId));
			}
			reactionItem.setReference("pathway", pathwayRef);
			
			String reactionName = String.format("%s %s %s", StringUtils.join(substrateIds, " + "),
					reactionType.equals("reversible") ? "<=>" : "=>",
					StringUtils.join(productIds, " + "));
			reactionItem.setAttribute("name", reactionName);

			store(reactionItem);

			for (String cid : compoundIds) {
				Item compound = getKeggCompound(cid);
				compound.addToCollection("reactions", reactionItem);
			}
		}
	}

	@Override
	public void close() throws Exception {
		store(compoundMap.values());
	}

	private String getEnzyme(String ecNumber) throws ObjectStoreException {
		String ret = enzymeMap.get(ecNumber);
		if (ret == null) {
			Item enzyme = createItem("Enzyme");
			enzyme.setAttribute("ecNumber", ecNumber);
			store(enzyme);
			ret = enzyme.getIdentifier();
			enzymeMap.put(ecNumber, ret);
		}
		return ret;
	}

	private Item getKeggCompound(String keggCompoundId) throws ObjectStoreException {
		Item ret = compoundMap.get(keggCompoundId);
		if (ret == null) {
			ret = createItem("KeggCompound");
			ret.setAttribute("identifier", String.format("KEGG Compound: %s", keggCompoundId));
			ret.setAttribute("originalId", keggCompoundId);
			compoundMap.put(keggCompoundId, ret);
		}
		return ret;
	}

//	private File reactionNameFile;
//
//	public void setReactionNameFile(File reactionNameFile) {
//		this.reactionNameFile = reactionNameFile;
//	}
//
//	private Map<String, String> reactionNameMap = new HashMap<String, String>();
//
//	private void readReactionNameFile() throws Exception {
//		reactionNameMap.clear();
//
//		BufferedReader in = null;
//		try {
//			in = new BufferedReader(new FileReader(reactionNameFile));
//			String line;
//			while ((line = in.readLine()) != null) {
//				String[] cols = line.split(": ", 2);
//				reactionNameMap.put(cols[0], cols[1]);
//			}
//		} catch (FileNotFoundException e) {
//			LOG.error(e);
//		} catch (IOException e) {
//			LOG.error(e);
//		} finally {
//			if (in != null)
//				in.close();
//		}
//
//	}
}
