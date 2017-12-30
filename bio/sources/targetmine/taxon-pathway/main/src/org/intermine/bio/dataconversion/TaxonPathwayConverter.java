package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
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
public class TaxonPathwayConverter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(TaxonPathwayConverter.class);
    //
    private static final String DATASET_TITLE = "KEGG Pathway";
    private static final String DATA_SOURCE_NAME = "KEGG";
    
    private Map<String, Set<String>> pathwayTaxonRefMap = new HashMap<String, Set<String>>();

	private Map<String, String> mainClassMap = new HashMap<String, String>();
	private Map<String, String> subClassMap = new HashMap<String, String>();
	private Map<String, String> pathwayNameMap = new HashMap<String, String>();
	private Map<String, String> pathwayDescMap = new HashMap<String, String>();
	private Map<String, String> taxonCodeIdMap = new HashMap<String, String>();

	private File taxonMapFile;
	private File pathwayClassFile;
	private File pathwayDescFile;

	public void setTaxonMapFile(File taxonMapFile) {
		this.taxonMapFile = taxonMapFile;
	}

	public void setPathwayClassFile(File pathwayClassFile) {
		this.pathwayClassFile = pathwayClassFile;
	}

	public void setPathwayDescFile(File pathwayDescFile) {
		this.pathwayDescFile = pathwayDescFile;
	}

    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public TaxonPathwayConverter(ItemWriter writer, Model model) {
        super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
    }

    /**
     * 
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
//    	if (mainClassMap.isEmpty() || subClassMap.isEmpty() || pathwayNameMap.isEmpty()) {
//    		processPathwayClassFile();
//    	}
//    	if (pathwayDescMap.isEmpty()) {
//    		processPathwayDescFile();
//    	}
		if (taxonCodeIdMap.isEmpty()) {
			processTaxonMapFile();
		}
    	
		String fileName = getCurrentFile().getName();
		int index = fileName.indexOf(".");
		String taxonCode = fileName.substring(0, index);
		String taxonId = taxonCodeIdMap.get(taxonCode);
		if (taxonId == null) {
			LOG.info("CANNOT FIND the taxonCode: " + taxonCode + "; skip the file: " + fileName);
		} else {
			// have to ignore the duplicated taxonIds, for example
			// aal     589873  589873  226             Alteromonas australica H 17     Alteromonas australica  Alteromonas
			// aaus    589873  589873  226             Alteromonas australica DE170    Alteromonas australica  Alteromonas
			
			String taxonomyRef = getTaxonomy(taxonId);
			
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				String pathwayId = cols[0].substring(index + 5);
				if (pathwayTaxonRefMap.get(pathwayId) == null) {
					pathwayTaxonRefMap.put(pathwayId, new HashSet<String>());
				}
				pathwayTaxonRefMap.get(pathwayId).add(taxonomyRef);
			}
		}
    }
    
    @Override
    public void close() throws Exception {
		processPathwayClassFile();
		processPathwayDescFile();
		
		for (String pathwayId : pathwayTaxonRefMap.keySet()) {
			Set<String> taxonRefSet = pathwayTaxonRefMap.get(pathwayId);
			
			Item pathway = createItem("Pathway");
			pathway.setAttribute("identifier", "map" + pathwayId);
			if (pathwayNameMap.get(pathwayId) != null) {
				pathway.setAttribute("name", pathwayNameMap.get(pathwayId));
				String subClass = subClassMap.get(pathwayId);
				pathway.setAttribute("label2", subClass);
				pathway.setAttribute("label1", mainClassMap.get(subClass));
			} else {
				LOG.info("Unknown pathway name: " + pathwayId);
			}
			
			String desc = pathwayDescMap.get("map" + pathwayId);
			if (desc != null) {
				pathway.setAttribute("description", desc);
			}
			pathway.setCollection("taxonomys", new ArrayList<String>(taxonRefSet));
			store(pathway);
		}
		LOG.info(pathwayTaxonRefMap.keySet().size() + " pathways were processed.");
    }
    
    private Map<String, String> taxonItemRefMap = new HashMap<String, String>();
    private String getTaxonomy(String taxonId) throws ObjectStoreException {
    	String ret = taxonItemRefMap.get(taxonId);
    	if (ret == null) {
    		Item item = createItem("Taxonomy");
    		item.setAttribute("taxonId", taxonId);
    		store(item);
    		ret = item.getIdentifier();
    		taxonItemRefMap.put(taxonId, ret);
    	}
		return ret;
    }
    
	private void processTaxonMapFile() {
		if (taxonMapFile == null) {
			throw new NullPointerException("taxonMapFile property is missing");
		}
		try {
			taxonCodeIdMap.clear();
			BufferedReader reader = new BufferedReader(new FileReader(taxonMapFile));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#")) {
					continue;
				}
				String[] cols = line.split("\t");
				String code = cols[0];
				String taxonId = cols[1];
				taxonCodeIdMap.put(code, taxonId);
			}
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void processPathwayClassFile() {
		if (pathwayClassFile == null) {
			throw new NullPointerException("pathwayClassFile property is missing");
		}
		try {
			mainClassMap.clear();
			subClassMap.clear();
			pathwayNameMap.clear();
			
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
