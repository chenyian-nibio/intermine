package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
 * 
 * @author chenyian
 */
public class TmKeggPathwayConverter extends BioFileConverter {
	protected static final Logger LOG = Logger.getLogger(TmKeggPathwayConverter.class);
	private static final String PROP_FILE = "kegg_config.properties";
	//
	private static final String DATASET_TITLE = "KEGG pathways data set";
	private static final String DATA_SOURCE_NAME = "KEGG";

	private Map<String, String[]> config = new HashMap<String, String[]>();
	private Set<String> taxonIds = new HashSet<String>();

	private Map<String, String> mainClass = new HashMap<String, String>();
	private Map<String, String> subClass = new HashMap<String, String>();

	private Map<String, String> geneMap = new HashMap<String, String>();
	private Map<String, Item> pathwayMap = new HashMap<String, Item>();

	private File pathwayClassFile;
	private boolean readClass;

	public void setPathwayClassFile(File pathwayClassFile) {
		this.pathwayClassFile = pathwayClassFile;
	}

	private void readPathwayClassification() {
		if (pathwayClassFile == null) {
			throw new NullPointerException("pathwayClassFile property not set");
		}
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(pathwayClassFile));

			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);

			while (iterator.hasNext()) {
				String[] strings = iterator.next();
				mainClass.put(strings[1], strings[2]);
				subClass.put(strings[0], strings[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		readClass = true;
	}

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
	public TmKeggPathwayConverter(ItemWriter writer, Model model) {
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
		if (readClass == false) {
			readPathwayClassification();
		}

		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		File currentFile = getCurrentFile();
		String fileName = currentFile.getName();
		while (iterator.hasNext()) {
			String[] line = iterator.next();

			if (line.length <= 1 || line[0].startsWith("#")) {
				continue;
			}
			if (fileName.endsWith(".pathways")) {
				String pathwayName = line[1].substring(0, line[1].lastIndexOf(" - "));
				Item pathway = getPathway(line[0].substring(5));
				pathway.setAttribute("name", pathwayName);
			} else if (fileName.endsWith(".pathway_genes")) {
				String organism = fileName.substring(0, 3);
				String taxonId = config.get(organism)[0];

				// only process organisms set in project.xml
				if (!taxonIds.isEmpty() && !taxonIds.contains(taxonId)) {
					continue;
				}
				if (!StringUtils.isEmpty(taxonId)) {
					// eg. path:hsa00010
					String pathwayId = line[0].substring(5);

					String geneIds = line[1];
					String[] genes = geneIds.split(",");
					Item pathway = getPathway(pathwayId);

					pathway.setReference("organism", getOrganism(taxonId));

					// assign main and sub classes
					String subclass = subClass.get(pathwayId.substring(3));
					if (subclass == null) {
						LOG.error("No subclass found for " + pathwayId);
					} else {
						pathway.setAttribute("subClass", subclass);
						pathway.setAttribute("mainClass", mainClass.get(subclass));
					}

					for (String gene : genes) {
						String geneId = gene.substring(4);
						pathway.addToCollection("genes", getGene(geneId, organism));

					}
				}
			}
		}
	}

	@Override
	public void close() throws Exception {
		store(pathwayMap.values());
	}

	private String getGene(String geneId, String organism) throws ObjectStoreException {
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

	private Item getPathway(String pathwayId) {
		Item ret = pathwayMap.get(pathwayId);
		if (ret == null) {
			ret = createItem("Pathway");
			ret.setAttribute("identifier", pathwayId);
			pathwayMap.put(pathwayId, ret);
		}
		return ret;
	}

}
