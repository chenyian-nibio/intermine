package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
public class KeggCompoundConverter extends BioFileConverter
{
	protected static final Logger LOG = Logger.getLogger(KeggCompoundConverter.class);
	//
	private static final String DATASET_TITLE = "KEGG Compound";
	private static final String DATA_SOURCE_NAME = "KEGG";

	private Map<String, String> inchiKeyMap = new HashMap<String, String>();
	private Map<String, String> inchiMap = new HashMap<String, String>();
	private Map<String, Set<String>> compoundPathwayMap = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> compoundEnzymeMap = new HashMap<String, Set<String>>();

	private File inchikeyFile;
	private File inchiFile;

	public void setInchikeyFile(File inchikeyFile) {
		this.inchikeyFile = inchikeyFile;
	}
	public void setInchiFile(File inchiFile) {
		this.inchiFile = inchiFile;
	}

	private File pathwayFile;
	private File enzymeFile;

	public void setPathwayFile(File pathwayFile) {
		this.pathwayFile = pathwayFile;
	}
	public void setEnzymeFile(File enzymeFile) {
		this.enzymeFile = enzymeFile;
	}

    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public KeggCompoundConverter(ItemWriter writer, Model model) {
        super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
    }

    /**
     * 
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
		readInchikeyFile();
		readInchiFile();
		readPathwayFile();
		readEnzymeFile();
		processPathwayClassFile();
		processPathwayDescFile();
    	
		BufferedReader in = null;
		try {
			in = new BufferedReader(reader);
			String line;
			String keggCompoundId = "";
			String name = "";
			List<String> allNames = new ArrayList<String>();
			String casNumber = "";
			
			boolean isName = false;
			
			while ((line = in.readLine()) != null) {
				if (isName) {
					if (line.startsWith(" ")) {
						allNames.add(line.substring(12).trim());
					} else {
						isName = false;
					}
				}
				
				if (line.startsWith("ENTRY")) {
					String[] split = line.split("\\s+");
					keggCompoundId = split[1];
				} else if (line.startsWith("NAME")) {
					// TODO to be refined
					name = line.substring(12).replaceAll(";$", "").replaceAll("\\s\\(.+?\\)$","").trim();
					allNames.add(line.substring(12).trim());
					isName = true;
				} else if (line.contains("CAS:")) {
					casNumber = line.substring(line.indexOf(":") + 2);
				} else if (line.startsWith("///")) {
					
					Item item = createItem("KeggCompound");
					item.setAttribute("identifier", String.format("KEGG Compound: %s", keggCompoundId));
					item.setAttribute("originalId", keggCompoundId);
					
					if (StringUtils.isEmpty(name)) {
						name = "KEGG Compound " + keggCompoundId;
						LOG.info("No name for KEGG Compound: " + keggCompoundId);
					}
					item.setAttribute("name", name);

					if (!StringUtils.isEmpty(casNumber)) {
						item.setAttribute("casRegistryNumber", casNumber);
					}
					String inchiKey = inchiKeyMap.get(keggCompoundId);
					if (!StringUtils.isEmpty(inchiKey)) {
						item.setAttribute("inchiKey", inchiKey);
						item.setReference("compoundGroup",
								getCompoundGroup(inchiKey.substring(0, inchiKey.indexOf("-")), name));
					}
					
					if (!allNames.isEmpty()) {
						for (String entryName: allNames) {
							Item syn = createItem("CompoundSynonym");
							syn.setAttribute("value", entryName.replaceAll(";$", "").trim());
							syn.setReference("subject", item);
							store(syn);
						}
					}
					
					Set<String> pathways = compoundPathwayMap.get(keggCompoundId);
					if (pathways != null) {
						for (String pathwayId : pathways) {
							// path:mapxxxxx
							item.addToCollection("pathways", getPathway(pathwayId.substring(5)));
						}
					}

					Set<String> enzymes = compoundEnzymeMap.get(keggCompoundId);
					if (enzymes != null) {
						for (String ecNumber : enzymes) {
							item.addToCollection("enzymes", getEnzyme(ecNumber));
						}
					}
					
					store(item);
					
					// clear current entry
					keggCompoundId = "";
					name = "";
					allNames = new ArrayList<String>();
					casNumber = "";
				}
			}
		} catch (FileNotFoundException e) {
			LOG.error(e);
		} catch (IOException e) {
			LOG.error(e);
		} finally {
			if (in != null)
				in.close();
		}

    }
    
	private Map<String, String> compoundGroupMap = new HashMap<String, String>();
	
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

	private void readInchikeyFile() {
		try {
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(new FileReader(inchikeyFile));
			
			while(iterator.hasNext()) {
				String[] cols = iterator.next();
				inchiKeyMap.put(cols[0], cols[1]);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void readInchiFile() {
		try {
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(new FileReader(inchiFile));
			
			while(iterator.hasNext()) {
				String[] cols = iterator.next();
				inchiMap.put(cols[0], cols[1]);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void readPathwayFile() {
		try {
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(new FileReader(pathwayFile));
			
			while(iterator.hasNext()) {
				String[] cols = iterator.next();
				String cid = cols[0].substring(4);
				if (compoundPathwayMap.get(cid) == null) {
					compoundPathwayMap.put(cid, new HashSet<String>());
				}
				compoundPathwayMap.get(cid).add(cols[1]);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void readEnzymeFile() {
		try {
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(new FileReader(enzymeFile));
			
			while(iterator.hasNext()) {
				String[] cols = iterator.next();
				String cid = cols[0].substring(4);
				if (compoundEnzymeMap.get(cid) == null) {
					compoundEnzymeMap.put(cid, new HashSet<String>());
				}
				compoundEnzymeMap.get(cid).add(cols[1]);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private Map<String, String> pathwayMap = new HashMap<String, String>();
	private String getPathway(String pathwayId) throws ObjectStoreException {
		String ret = pathwayMap.get(pathwayId);
		if (ret == null) {
			Item pathway = createItem("Pathway");
			pathway.setAttribute("identifier", pathwayId);

			// pathwayId = mapxxxxx
			String key = pathwayId.substring(3);
			pathway.setAttribute("name", pathwayNameMap.get(key));
			String subClass = subClassMap.get(key);
			pathway.setAttribute("label2", subClass);
			pathway.setAttribute("label1", mainClassMap.get(subClass));
			pathway.setReference("organism", getOrganism("0"));
			
			String desc = pathwayDescMap.get(pathwayId);
			if (desc != null) {
				pathway.setAttribute("description", desc);
			}
			pathway.addToCollection("dataSets", getDataSet("KEGG Pathway", getDataSource(DATA_SOURCE_NAME)));
			
			store(pathway);
			
			ret = pathway.getIdentifier();
			pathwayMap.put(pathwayId, ret);
		}
		return ret;
	}

	private Map<String, String> enzymeMap = new HashMap<String, String>();
	private String getEnzyme(String ecNumber) throws ObjectStoreException {
		String ret = enzymeMap.get(ecNumber);
		if (ret == null) {
			Item enzyme = createItem("Enzyme");
			String identifier = ecNumber.substring(3);
			enzyme.setAttribute("primaryIdentifier", identifier);
			enzyme.setAttribute("ecNumber", identifier);
			
			store(enzyme);
			
			ret = enzyme.getIdentifier();
			enzymeMap.put(ecNumber, ret);
		}
		return ret;
	}
	
	private Map<String, String> mainClassMap = new HashMap<String, String>();
	private Map<String, String> subClassMap = new HashMap<String, String>();
	private Map<String, String> pathwayNameMap = new HashMap<String, String>();
	private Map<String, String> pathwayDescMap = new HashMap<String, String>();

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
