package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
public class KeggDrugConverter extends BioFileConverter {
	protected static final Logger LOG = Logger.getLogger(KeggDrugConverter.class);
	//
	private static final String DATASET_TITLE = "KEGG drug data set";
	private static final String DATA_SOURCE_NAME = "KEGG";

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public KeggDrugConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	private File inchikeyFile;

	public void setInchikeyFile(File inchikeyFile) {
		this.inchikeyFile = inchikeyFile;
	}
	
	private Map<String, String> inchikeyMap = new HashMap<String, String>();

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		readInchikeyFile();

		BufferedReader in = null;
		try {
			in = new BufferedReader(reader);
			String line;
			String keggDrugId = "";
			String name = "";
			String atcCodes = "";
			String casNumber = "";
			while ((line = in.readLine()) != null) {
				if (line.startsWith("ENTRY")) {
					String[] split = line.split("\\s+");
					keggDrugId = split[1];
				} else if (line.startsWith("NAME")) {
					name = line.substring(12).replaceAll(";$", "").replaceAll("\\(.+\\)$","").trim();
				} else if (line.contains("ATC code:")) {
					atcCodes = line.substring(line.indexOf(":") + 2);
				} else if (line.contains("CAS:")) {
					casNumber = line.substring(line.indexOf(":") + 2);
				} else if (line.startsWith("///")) {
//					LOG.info(String.format("%s; %s; %s; %s", keggDrugId,name,atcCodes,casNumber));
					Item drugItem = createItem("DrugCompound");
					drugItem.setAttribute("keggDrugId",keggDrugId);
//					drugItem.setAttribute("drugBankId", drugBankId);
					drugItem.setAttribute("primaryIdentifier", String.format("KEGG: %s", keggDrugId));
					
					if (!atcCodes.equals("")) {
						for (String atcCode : atcCodes.split(" ")) {
							drugItem.addToCollection("atcCodes", getAtcClassification(atcCode, name));
						}
					}
					
					if (!casNumber.equals("")) {
						drugItem.setAttribute("casRegistryNumber", casNumber);
					}
					
					String inchiKey = inchikeyMap.get(keggDrugId);
					if (inchiKey != null) {
						drugItem.setAttribute("inchiKey", inchiKey);
						drugItem.setReference(
								"compoundGroup",
								getCompoundGroup(inchiKey.substring(0, inchiKey.indexOf("-")), name));
					}
					
					store(drugItem);
					
					// clear current entry
					keggDrugId = "";
					name = "";
					atcCodes = "";
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
	
	private void readInchikeyFile() {
		try {
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(new FileReader(inchikeyFile));
			
			while(iterator.hasNext()) {
				String[] cols = iterator.next();
				inchikeyMap.put(cols[0], cols[1]);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private Map<String, String> atcMap = new HashMap<String, String>();

	private String getAtcClassification(String atcCode, String name) throws ObjectStoreException {
		String ret = atcMap.get(atcCode);
		if (ret == null) {
			Item item = createItem("AtcClassification");
			item.setAttribute("atcCode", atcCode);
			item.setAttribute("name", name);
			// TODO add parent
			String parentCode = atcCode.substring(0, 5);
			item.setReference("parent", getParent(parentCode));

			store(item);
			ret = item.getIdentifier();
			atcMap.put(atcCode, ret);
		}
		return ret;
	}

	private String getParent(String parentCode) throws ObjectStoreException {
		String ret = atcMap.get(parentCode);
		if (ret == null) {
			Item item = createItem("AtcClassification");
			item.setAttribute("atcCode", parentCode);
			store(item);
			ret = item.getIdentifier();
			atcMap.put(parentCode, ret);
		}
		return ret;
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

}
