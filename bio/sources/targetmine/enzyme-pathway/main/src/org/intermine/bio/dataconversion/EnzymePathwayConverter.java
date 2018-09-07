package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class EnzymePathwayConverter extends BioFileConverter {
	protected static final Logger LOG = Logger.getLogger(EnzymePathwayConverter.class);

	//
	private static final String DATASET_TITLE = "KEGG Pathway";
	private static final String DATA_SOURCE_NAME = "KEGG";

	private Map<String, String> pathwayMap = new HashMap<String, String>();
	private Map<String, String> pathwayDescMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public EnzymePathwayConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		if (mainClassMap.isEmpty() || subClassMap.isEmpty()) {
			System.out.println("processing pathwayClassFile....");
			LOG.info("processing pathwayClasscFile....");
			processPathwayClassFile();
		}
		if (pathwayDescMap.isEmpty()) {
			System.out.println("processing pathwayDescFile....");
			LOG.info("processing pathwayDescFile....");
			processPathwayDescFile();
		}

		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);

		Map<String, Set<String>> ecPathwayMap = new HashMap<String, Set<String>>();
		// Parse source data to map
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String pathway = cols[0].trim();
			if (!pathway.startsWith("path:ec")) {
				LOG.error("error data format: " + pathway);
				continue;
			}
			String ecNumber = cols[1].trim();
			if (ecNumber.startsWith("ec:")) {
				if (ecPathwayMap.get(ecNumber) == null) {
					ecPathwayMap.put(ecNumber, new HashSet<String>());
				}
//				ecPathwayMap.get(ecNumber).add(pathway.substring(7));
				ecPathwayMap.get(ecNumber).add(pathway.substring(5));
			}
		}
		// create Enzymes and Pathways
		for (String ec : ecPathwayMap.keySet()) {
			Item enzyme = createItem("Enzyme");
			String identifier = ec.substring(3);
			enzyme.setAttribute("primaryIdentifier", identifier);
			enzyme.setAttribute("ecNumber", identifier);
			for (String pathwayId : ecPathwayMap.get(ec)) {
				String refId = getPathway(pathwayId);
				enzyme.addToCollection("pathways", refId);
			}
			store(enzyme);
		}

	}

	private String getPathway(String pathwayId) throws ObjectStoreException {
		String ret = pathwayMap.get(pathwayId);
		if (ret == null) {
			Item pathway = createItem("Pathway");
			pathway.setAttribute("identifier", pathwayId);

			// identifier = ecxxxxx
			String key = pathwayId.substring(2);
			pathway.setAttribute("name", pathwayNameMap.get(key));
			String subClass = subClassMap.get(key);
			pathway.setAttribute("label2", subClass);
			pathway.setAttribute("label1", mainClassMap.get(subClass));
			pathway.setReference("organism", getOrganism("0"));
			
			String desc = pathwayDescMap.get(pathwayId);
			if (desc != null) {
				pathway.setAttribute("description", desc);
			}
			
			ret = pathway.getIdentifier();
			pathwayMap.put(pathwayId, ret);
			store(pathway);
		}
		return ret;
	}
	
	private Map<String, String> mainClassMap = new HashMap<String, String>();
	private Map<String, String> subClassMap = new HashMap<String, String>();
	private Map<String, String> pathwayNameMap = new HashMap<String, String>();

	private File pathwayClassFile;
	private File pathwayDescFile;

	public void setPathwayClassFile(File pathwayClassFile) {
		this.pathwayClassFile = pathwayClassFile;
	}

	public void setPathwayDescFile(File pathwayDescFile) {
		this.pathwayDescFile = pathwayDescFile;
	}

	private void processPathwayClassFile() {
		if (pathwayClassFile == null) {
			throw new NullPointerException("pathwayClassFile property is missing");
		}
		try {
			mainClassMap.clear();
			subClassMap.clear();
			
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void processPathwayDescFile() {
		if (pathwayDescFile == null) {
			throw new NullPointerException("pathwayDescFile property is missing");
		}
		try {
			pathwayDescMap.clear();
			
			BufferedReader reader = new BufferedReader(new FileReader(pathwayDescFile));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] split = line.split("\\t");
				if (split.length > 1) {
					pathwayDescMap.put(split[0].trim(), split[1].trim());
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
