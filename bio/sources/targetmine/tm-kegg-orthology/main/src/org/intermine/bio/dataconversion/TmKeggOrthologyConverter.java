package org.intermine.bio.dataconversion;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * This parser is modified from intermine default one, because free kegg archive file is no longer
 * available and the data source is retrieved by using api thus the data format is different now 
 * Some unused attribute are removed, and paralogue/orthologue tag is introduced
 * 
 * @author chenyian
 */
public class TmKeggOrthologyConverter extends BioFileConverter {
	protected static final Logger LOG = Logger.getLogger(TmKeggOrthologyConverter.class);
	private static final String PROP_FILE = "kegg_config.properties";
	//
	private static final String DATASET_TITLE = "KEGG orthologues data set";
	private static final String DATA_SOURCE_NAME = "GenomeNet";

	private Map<String, String[]> config = new HashMap<String, String[]>();
	private Set<String> taxonIds = new HashSet<String>();

	private Map<String, String> geneMap = new HashMap<String, String>();

	public void setKeggOrganisms(String taxonIds) {
		this.taxonIds = new HashSet<String>(Arrays.asList(StringUtils.split(taxonIds, " ")));
		LOG.info("Setting list of organisms to " + this.taxonIds);
	}

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public TmKeggOrthologyConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
		readConfig();
	}

	private void readConfig() {
		Properties props = new Properties();
		try {
			props.load(getClass().getClassLoader().getResourceAsStream(PROP_FILE));
		} catch (IOException e) {
			throw new RuntimeException("Problem loading properties '" + PROP_FILE + "'", e);
		}

		for (Map.Entry<Object, Object> entry : props.entrySet()) {

			String key = (String) entry.getKey();
			String value = ((String) entry.getValue()).trim();

			String[] attributes = key.split("\\.");
			if (attributes.length == 0) {
				throw new RuntimeException("Problem loading properties '" + PROP_FILE
						+ "' on line " + key);
			}
			String organism = attributes[0];

			if (config.get(organism) == null) {
				String[] configs = new String[2];
				configs[1] = "primaryIdentifier";
				config.put(organism, configs);
			}
			if ("taxonId".equals(attributes[1])) {
				config.get(organism)[0] = value;
			} else if ("identifier".equals(attributes[1])) {
				config.get(organism)[1] = value;
			} else {
				String msg = "Problem processing properties '" + PROP_FILE + "' on line " + key
						+ ".  This line has not been processed.";
				LOG.error(msg);
			}
		}
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		List<String> interestedOrganism = getOrganismCodes();
		if (interestedOrganism.isEmpty()) {
			throw new RuntimeException("No organism assigned.");
		}

		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			if (cols.length <= 1 || cols[0].startsWith("#")) {
				continue;
			}
			String[] homologues = cols[1].split(",");
			List<String> selected = new ArrayList<String>();
			for (String geneEntry : homologues) {
				if (StringUtils.isEmpty(geneEntry)){
					continue;
				}
				String organism = geneEntry.substring(0, 3);
				// There are some strange ids for D. melanogaster, the rest start with
				// Dmel_, ignore any D. melanogaster ids without Dmel_ and strip this
				// off the rest
				if (organism.equals("dme") && !geneEntry.startsWith("dme:Dmel_")) {
					continue;
				}
				if (interestedOrganism.contains(organism)) {
					selected.add(geneEntry);
				}
			}
			
			for (String geneEntry1 : selected) {
				for (String geneEntry2 : selected) {
					if (!geneEntry1.equals(geneEntry2)){
						createHomologue(geneEntry1, geneEntry2);
					}
				}
			}

		}

	}

	private List<String> getOrganismCodes() {
		List<String> ret = new ArrayList<String>();
		for (String organism : config.keySet()) {
			if (taxonIds.contains(config.get(organism)[0])) {
				ret.add(organism);
			}
		}
		return ret;
	}

	private void createHomologue(String gene1, String gene2) throws ObjectStoreException {
		// examples: dme:Dmel_CG3481; hsa:10327; mmu:14555; rno:24788
		Item homologue = createItem("Homologue");
		String org1 = gene1.substring(0, 3);
		String org2 = gene2.substring(0, 3);
		homologue.setReference("gene", getGene(gene1.substring(4), org1));
		homologue.setReference("homologue", getGene(gene2.substring(4), org2));
		// determine para or ortho
		String type = "orthologue";
		if (org1.equals(org2)){
			type = "paralogue";
		}
		homologue.setAttribute("type", type);
		store(homologue);
	}

	private String getGene(String geneId, String organism) throws ObjectStoreException {
		if (geneId.startsWith("Dmel_")) {
			geneId = geneId.substring(5);
		}
		String ret = geneMap.get(geneId);
		String taxonId = config.get(organism)[0];
		if (ret == null) {
			Item item = createItem("Gene");
			item.setAttribute(config.get(organism)[1], geneId);
			item.setReference("organism", getOrganism(taxonId));
			ret = item.getIdentifier();
			store(item);
			geneMap.put(geneId, ret);
		}
		return ret;
	}
}
