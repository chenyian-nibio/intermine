package org.intermine.bio.dataconversion;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Parse KGML file, see http://www.kegg.jp/kegg/xml/docs/ for the file format definition.
 * 
 * @author chenyian
 */
public class KeggReactionConverter extends BioFileConverter {
	//
	private static final String DATASET_TITLE = "KEGG Pathway";
	private static final String DATA_SOURCE_NAME = "KEGG";

	// private Set<String> foundReactions = new HashSet<String>();
	private Map<String, Item> reactionMap = new HashMap<String, Item>();
	private Map<String, String> reactionTypeMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public KeggReactionConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {

		String fileName = getCurrentFile().getName();
		String currentOrganismCode = fileName.substring(0, 3);
		String pathwayId = fileName.substring(0, fileName.indexOf("."));

		Builder parser = new Builder();
		Document doc = parser.build(reader);

		Element rootElement = doc.getRootElement();

		// System.out.println(rootElement.getAttributeValue("name"));

		Elements entryElements = rootElement.getChildElements("entry");
		Map<String, String> entryMap = new HashMap<String, String>();
		for (int i = 0; i < entryElements.size(); i++) {
			Element entry = entryElements.get(i);
			String id = entry.getAttributeValue("id");
			String name = entry.getAttributeValue("name");
			entryMap.put(id, name);
		}

		Elements relationElements = rootElement.getChildElements("relation");
		for (int i = 0; i < relationElements.size(); i++) {
			Element relation = relationElements.get(i);
			String entry1Id = relation.getAttributeValue("entry1");
			String entry2Id = relation.getAttributeValue("entry2");
			String relationType = relation.getAttributeValue("type");
			if (relationType.equals("ECrel")) {
				continue;
			}

			Elements subtypes = relation.getChildElements("subtype");
			List<String> names = new ArrayList<String>();
			for (int k = 0; k < subtypes.size(); k++) {
				String name = subtypes.get(k).getAttributeValue("name");
				if (name.equals("compound")) {
					continue;
				}
				names.add(name);
			}
			if (names.isEmpty()) {
				continue;
			}

			String[] geneIds1 = entryMap.get(entry1Id).split("\\s");
			String[] geneIds2 = entryMap.get(entry2Id).split("\\s");
			for (String geneId1 : geneIds1) {
				String[] id1 = geneId1.split(":");
				if (!id1[0].equals(currentOrganismCode)) {
					continue;
				}
				for (String geneId2 : geneIds2) {
					String[] id2 = geneId2.split(":");
					if (!id2[0].equals(currentOrganismCode)) {
						continue;
					}
					Collections.sort(names);
					String key = String.format("%s-%s-%s", id1[1], id2[1],
							StringUtils.join(names, "-"));
					if (reactionMap.get(key) == null) {
						reactionMap.put(key,
								createReaction(id1[1], id2[1], names));
					}
					reactionMap.get(key).addToCollection("pathways", getPathway(pathwayId));
				}
			}
		}

	}

	@Override
	public void close() throws Exception {
		store(reactionMap.values());
	}

	private Map<String, String> geneMap = new HashMap<String, String>();
	private Map<String, String> pathwayMap = new HashMap<String, String>();

	private String getGene(String geneId) throws ObjectStoreException {
		String ret = geneMap.get(geneId);
		if (ret == null) {
			Item item = createItem("Gene");
			item.setAttribute("primaryIdentifier", geneId);
			store(item);
			ret = item.getIdentifier();
			geneMap.put(geneId, ret);
		}
		return ret;
	}

	private String getPathway(String pathwayId) throws ObjectStoreException {
		String ret = pathwayMap.get(pathwayId);
		if (ret == null) {
			Item item = createItem("Pathway");
			item.setAttribute("identifier", pathwayId);
			store(item);
			ret = item.getIdentifier();
			pathwayMap.put(pathwayId, ret);
		}
		return ret;
	}

	private Item createReaction(String geneId1, String geneId2, List<String> names)
			throws ObjectStoreException {
		Item item = createItem("Reaction");
		item.setReference("gene1", getGene(geneId1));
		item.setReference("gene2", getGene(geneId2));
		item.setAttribute("name", String.format("%s->%s", geneId1, geneId2));
		item.setAttribute("text", StringUtils.join(names, ", "));
		for (String reactionType : names) {
			item.addToCollection("types", getReactionType(reactionType));
		}
		return item;
	}

	private String getReactionType(String name) throws ObjectStoreException {
		String ret = reactionTypeMap.get(name);
		if (ret == null) {
			Item item = createItem("ReactionType");
			item.setAttribute("name", name);
			store(item);
			ret = item.getIdentifier();
			reactionTypeMap.put(name, ret);
		}
		return ret;
	}

}
