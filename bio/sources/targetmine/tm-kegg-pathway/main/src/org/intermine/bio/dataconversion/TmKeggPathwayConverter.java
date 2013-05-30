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

	private Map<String, String> mainClassMap = new HashMap<String, String>();
	private Map<String, String> subClassMap = new HashMap<String, String>();
	private Map<String, String> pathwayNameMap = new HashMap<String, String>();

	private Map<String, String> pathwayMap = new HashMap<String, String>();

	private File pathwayClassFile;
	private boolean readClass;

	public void setPathwayClassFile(File pathwayClassFile) {
		this.pathwayClassFile = pathwayClassFile;
	}

	private void readPathwayClassification() {
		if (pathwayClassFile == null) {
			throw new NullPointerException("pathwayClassFile property not set");
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(pathwayClassFile));
			String line;
			String mainClass = "";
			String subClass = "";
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("##")) {
					if (mainClass.equals("")) {
						reader.close();
						throw new RuntimeException("Missing main class when processing the line: "
								+ line);
					}
					subClass = line.substring(2).trim();
					mainClassMap.put(subClass, mainClass);
				} else if (line.startsWith("#")) {
					mainClass = line.substring(1).trim();
				} else {
					if (subClass.equals("")) {
						reader.close();
						throw new RuntimeException("Missing sub class when processing the line: "
								+ line);
					}
					String[] cols = line.split("\\t");
					String pathwayId = cols[0].trim();
					subClassMap.put(pathwayId, subClass);
					pathwayNameMap.put(pathwayId, cols[1].trim());
				}
			}
			reader.close();
			readClass = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		String organism = fileName.substring(0, 3);
		String taxonId = config.get(organism)[0];
		while (iterator.hasNext()) {
			String[] line = iterator.next();

			if (line.length <= 1) {
				continue;
			}
			
			Item item = createItem("Gene");
			item.setAttribute(config.get(organism)[1], line[0]);
			item.setReference("organism", getOrganism(taxonId));
			String[] pathwayIds = line[1].split("\\s");
			for (String pid : pathwayIds) {
				item.addToCollection("pathways", getPathway(pid, organism));
			}
			store(item);
			
			
		}
	}

	@Override
	public void close() throws Exception {
	}

	private String getPathway(String pathwayId, String organism) throws ObjectStoreException {
		String fullId = organism + pathwayId;
		String ret = pathwayMap.get(fullId);
		String taxonId = config.get(organism)[0];
		if (ret == null) {
			Item item = createItem("Pathway");
			item.setAttribute("name", pathwayNameMap.get(pathwayId));
			String subClass = subClassMap.get(pathwayId);
			item.setAttribute("subClass", subClass);
			item.setAttribute("mainClass", mainClassMap.get(subClass));
			item.setAttribute("identifier", fullId);
			item.setReference("organism", getOrganism(taxonId));
			store(item);
			ret = item.getIdentifier();
			pathwayMap.put(fullId, ret);
		}
		return ret;
	}

}
