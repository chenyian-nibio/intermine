package org.intermine.bio.dataconversion;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private Map<String, Set<String>> reactionMap = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> reactionTypeMap = new HashMap<String, Set<String>>();

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
			// System.out.println(String.format("%s, %s: %s", entryMap.get(entry1Id),
			// entryMap.get(entry2Id), StringUtils.join(names, "|")));

			// empty subtype, do nothing ...
			// Collections.sort(names);
			// String subtype = StringUtils.join(names, "|");
			// if (StringUtils.isEmpty(subtype)) {
			// continue;
			// }
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
					// prevent redundant reactions
					// String key = String.format("%s-%s-%s-%s",id1[1], id2[1], subtype, pathwayId);
					// if (!foundReactions.contains(key)) {
					// createReaction(id1[1], id2[1], subtype, pathwayId);
					// foundReactions.add(key);
					// }
					for (String subtype : names) {
						String key = String.format("%s-%s", id1[1], id2[1]);
						if (reactionMap.get(key) == null) {
							reactionMap.put(key, new HashSet<String>());
						}
						reactionMap.get(key).add(subtype);

						String keyType = String.format("%s-%s-%s", id1[1], id2[1], subtype);
						if (reactionTypeMap.get(keyType) == null) {
							reactionTypeMap.put(keyType, new HashSet<String>());
						}
						reactionTypeMap.get(keyType).add(pathwayId);
					}
				}
			}
		}

	}

	@Override
	public void close() throws Exception {
		for (String key : reactionMap.keySet()) {
			String[] reaction = key.split("-");
			createReaction(reaction[0], reaction[1], reactionMap.get(key));
		}
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

	private void createReaction(String geneId1, String geneId2, Set<String> reactionTypes)
			throws ObjectStoreException {
		Item item = createItem("Reaction");
		item.setReference("gene1", getGene(geneId1));
		item.setReference("gene2", getGene(geneId2));
		item.setAttribute("name", String.format("%s->%s", geneId1, geneId2));
		store(item);
		for (String reactionType : reactionTypes) {
			String keyType = String.format("%s-%s-%s", geneId1, geneId2, reactionType);
			createReactionType(item.getIdentifier(), reactionType, reactionTypeMap.get(keyType));
		}
	}

	private void createReactionType(String reactionRefId, String reactionType, Set<String> pathwayIds)
			throws ObjectStoreException {
		Item item = createItem("ReactionType");
		item.setAttribute("name", reactionType);
		for (String pathwayId : pathwayIds) {
			item.addToCollection("pathways", getPathway(pathwayId));
		}
		item.setReference("reaction", reactionRefId);
		store(item);
	}

}
