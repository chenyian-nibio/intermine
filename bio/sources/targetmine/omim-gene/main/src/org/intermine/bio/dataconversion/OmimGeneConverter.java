package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * This parser integrates OMIM data from the file 'mim2gene_medgen' in the NCBI FTP site 
 * (ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/mim2gene_medgen). 
 * The OMIM phenotype titles are retrieved using OMIM API in a tab separated 3-column format.
 * <pre>
 * omimId, title, synonyms.
 * </pre>
 * Please contact targetmine[at]nibiohn.go.jp if you are interested in the script. 
 * 
 * @author chenyian
 */
public class OmimGeneConverter extends BioFileConverter {
	//
	private static final String DATASET_TITLE = "OMIM data set";
	private static final String DATA_SOURCE_NAME = "OMIM";

	private Map<String, String> titleMap = new HashMap<String, String>();
	private Map<String, String> diseaseTermMap = new HashMap<String, String>();

	private static final Logger LOG = Logger.getLogger(OmimGeneConverter.class);

	// omim and geneId mapping file
	private File titleFile;

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public OmimGeneConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * Read data from the file 'omim_phenotype_name'
	 * The format are as follow : omim_id[tab]title[tab]synonyms
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		
		readTitleFile();

		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		
		// generate gene -> omims map
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			// we only want phenotypes, not genes
			String type = cols[2];
			if (type.equals("gene") || "-".equals(cols[1])) {
				continue;
			}
			String omimId = cols[0];
			String geneId = cols[1];
//			String cui = cols[4];
			
    		Item item = createItem("Disease");
    		item.setReference("diseaseTerm", getDiseaseTerm(omimId));
    		item.setReference("gene", getGene(geneId));
    		item.addToCollection("sources", getDataSource("OMIM"));
    		store(item);

		}

	}
	
	private void readTitleFile() throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(titleFile));
		String line;
		
		titleMap.clear();
		while ((line = reader.readLine()) != null) {  
			String[] cols = line.split("\t");
			titleMap.put(cols[0], line);
		}
		
		reader.close();
	}

	public File getTitleFile() {
		return titleFile;
	}

	public void setTitleFile(File titleFile) {
		this.titleFile = titleFile;
	}

	private Map<String, String> geneMap = new HashMap<String, String>();
	private String getGene(String geneId) throws ObjectStoreException {
		String ret = geneMap.get(geneId);
		if (ret == null) {
			Item item = createItem("Gene");
			item.setAttribute("primaryIdentifier", geneId);
			ret = item.getIdentifier();
			store(item);
			geneMap.put(geneId, ret);
		}
		return ret;
	}

	private String getDiseaseTerm(String omimId) throws ObjectStoreException {
		String ret = diseaseTermMap.get(omimId);
		if (ret == null) {
			Item diseaseTerm = createItem("DiseaseTerm");
			diseaseTerm.setAttribute("identifier", omimId);

			String line = titleMap.get(omimId);
			if (line != null) {
				String[] cols = line.split("\t", 3);
				diseaseTerm.setAttribute("name", cols[1]);
				String aliasString = cols[2];
				if (!StringUtils.isEmpty(aliasString)){
					String[] alias = aliasString.split(";;");
					for (String name : alias) {
						Item synonym = createItem("DiseaseSynonym");
						synonym.setAttribute("name", name);
						synonym.setReference("diseaseTerm", diseaseTerm);
						store(synonym);
						diseaseTerm.addToCollection("synonyms", synonym);
					}
				}
			} else {
				LOG.info("Unable to find the title. omimId: " + omimId);
			}
			ret = diseaseTerm.getIdentifier();
			store(diseaseTerm);
			
			diseaseTermMap.put(omimId, ret);
		}
		return ret;
	}

}
