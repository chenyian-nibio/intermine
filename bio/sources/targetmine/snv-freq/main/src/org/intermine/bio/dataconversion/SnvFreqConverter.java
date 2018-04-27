package org.intermine.bio.dataconversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;


/**
 * 
 * @author
 */
public class SnvFreqConverter extends BioFileConverter
{
    //
//    private static final String DATASET_TITLE = "Add DataSet.title here";
//    private static final String DATA_SOURCE_NAME = "Add DataSource.name here";

	private File populationFile;
	
    public void setPopulationFile(File populationFile) {
		this.populationFile = populationFile;
	}
    
    private Map<String, String> populationRefMap = new HashMap<String, String>();

    private Map<String, String> dataSetMap = new HashMap<String, String>();
    
	/**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public SnvFreqConverter(ItemWriter writer, Model model) {
        super(writer, model);
    }

    /**
     * 
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
    	if (populationRefMap.isEmpty()) {
    		getPopulationRefMap();
    	}
    	
    	String fileName = getCurrentFile().getName();
    	if (fileName.startsWith("1kgp")) {
    		process1KGPFrequencyFile(reader);
    	} else {
    		processSnpFrequencyFile(fileName.substring(0, fileName.indexOf(".")), reader);
    	}
    }

	private void getPopulationRefMap() throws ObjectStoreException {
		try {
			FileReader reader = new FileReader(populationFile);
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);

			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				String popId = cols[0];
				String code = cols[1];
				String title = cols[2];
				Item item = createItem("Population");
				item.setAttribute("code", code);
				item.setAttribute("title", title);
				store(item);
				String refId = item.getIdentifier();
				populationRefMap.put(code, refId);
				populationRefMap.put(popId, refId);
			}
			reader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			String fn = populationFile.getName();
			throw new RuntimeException("Cannot found pmidFile: " + fn);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void processSnpFrequencyFile(String subsetName, Reader reader) throws IOException, ObjectStoreException {
		if (dataSetMap.isEmpty()) {
			dataSetMap.put("1kjpn", getDataSet("1KJPN", getDataSource("integrative Japanese Genome Variation Database")));
			dataSetMap.put("esp", getDataSet("Exome Variant Server", getDataSource("NHLBI Exome Sequencing Project")));
			dataSetMap.put("hgvd", getDataSet("Human Genetic Variation Database", getDataSource("Human Genetic Variation Database")));
		}
		
		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String rsid = cols[3];
//			String ref = cols[4];
			String alt = cols[5];
			String frequency = cols[6];
			String popId = cols[7];
			String population = populationRefMap.get(popId);
			if (population == null) {
				throw new RuntimeException(String.format("Cannot found pop code: ", popId));
			}
			Item item = createItem("Frequency");
			item.setAttribute("allele", alt);
			item.setAttribute("frequency", frequency);
			item.setReference("population", population);
			String dataSet = dataSetMap.get(subsetName);
			if (dataSet == null) {
				throw new RuntimeException(String.format("Cannot found data set code: ", subsetName));
			}
			item.setReference("dataSet", dataSet);
			item.setReference("snp", getSnp(rsid));
			store(item);
		}

	}

	private void process1KGPFrequencyFile(Reader reader) throws IOException, ObjectStoreException {
		String dataSet = getDataSet("1000 Genome Project", getDataSource("1000 Genome Project"));
		
		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String rsid = cols[1];
			String a1 = cols[2];
//			String a2 = cols[3];
			String frequency = cols[4];
			if (frequency.equals("NA")) {
				continue;
			}
//			String popSize = cols[5];
			String popCode = cols[6];
			String population = populationRefMap.get(popCode);
			if (population == null) {
				throw new RuntimeException(String.format("Cannot found pop code: ", popCode));
			}
			Item item = createItem("Frequency");
			item.setAttribute("allele", a1);
			item.setAttribute("frequency", frequency);
			item.setReference("population", population);
			item.setReference("dataSet", dataSet);
			item.setReference("snp", getSnp(rsid));
			store(item);
		}
	}
	
	private Map<String, String> snpMap = new HashMap<String, String>();
	
	private String getSnp(String identifier) throws ObjectStoreException {
		String ret = snpMap.get(identifier);
		if (ret == null) {
			Item item = createItem("SNP");
			item.setAttribute("identifier", identifier);
			store(item);
			ret = item.getIdentifier();
			snpMap.put(identifier, ret);
		}
		return ret;
	}

}
