package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class OmimGeneConverter extends BioFileConverter {
	//
	private static final String DATASET_TITLE = "OMIM data set";
	private static final String DATA_SOURCE_NAME = "NCBI";

	private Map<String, String> omimMap = new HashMap<String, String>();
	private Map<String, String> geneMap = new HashMap<String, String>();

	private Map<String, Set<String>> geneOminMap = new HashMap<String, Set<String>>();
//	private Set<String> phenotypeOmimIds = new HashSet<String>();

	private static final Logger LOG = Logger.getLogger(OmimGeneConverter.class);

	// omim and geneId mapping file
	private File mim2geneFile;

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
	 * Read data from pre-retrieved file on ncbi 'omim_phenotype_name'
	 * The format are as follow : omim_id[tab]title
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {

		// parse omim and geneId mapping file
		readMim2geneMedgen();

		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(new BufferedReader(reader));
		
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			getDisease(cols[0], cols[1], cols[2]);
		}

		// create gene items
		for (String geneId : geneOminMap.keySet()) {
			getGene(geneId, geneOminMap.get(geneId));
		}

	}

	@Deprecated
	private void readMim2gene() throws Exception {

		Reader reader = new BufferedReader(new FileReader(mim2geneFile));
		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);

		// generate gene -> omims map
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			// we only want phenotypes, not genes
			String type = cols[2];
			if (type.equals("gene")) {
				continue;
			}
			String geneId = cols[1];
			String omimId = cols[0];
			if (!geneOminMap.keySet().contains(geneId)) {
				geneOminMap.put(geneId, new HashSet<String>());
			}
			geneOminMap.get(geneId).add(omimId);
//			phenotypeOmimIds.add(omimId);
		}

	}
	
	private void readMim2geneMedgen() throws Exception {
		
		Reader reader = new BufferedReader(new FileReader(mim2geneFile));
		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		
		// generate gene -> omims map
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			// we only want phenotypes, not genes
			String type = cols[2];
			if (type.equals("gene") || "-".equals(cols[1])) {
				continue;
			}
			String geneId = cols[1];
			String omimId = cols[0];
			if (!geneOminMap.keySet().contains(geneId)) {
				geneOminMap.put(geneId, new HashSet<String>());
			}
			geneOminMap.get(geneId).add(omimId);
//			phenotypeOmimIds.add(omimId);
		}
		
	}

	private String getDisease(String omimId, String title, String aliasString) throws Exception {
		String ret = omimMap.get(omimId);
		if (ret == null) {
			Item disease = createItem("Disease");
			disease.setAttribute("omimId", omimId);
			disease.setAttribute("title", title);
			if (!StringUtils.isEmpty(aliasString)){
				String[] alias = aliasString.split(";;");
				for (String name : alias) {
					Item synonym = createItem("DiseaseSynonym");
					synonym.setAttribute("name", name);
					synonym.setReference("disease", disease);
					store(synonym);
				}
			}
			ret = disease.getIdentifier();
			omimMap.put(omimId, ret);

			store(disease);
		}
		return ret;
	}

	private String getGene(String geneId, Set<String> omimIds) throws Exception {
		String ret = geneMap.get(geneId);
		if (ret == null) {
			Item gene = createItem("Gene");
			gene.setAttribute("ncbiGeneId", geneId);

			for (String omimId : omimIds) {
				String diseaseRef = omimMap.get(omimId);
				if (diseaseRef == null) {
					LOG.error("Unknown OMIM id: " + omimId + "; Gene: " + geneId + "; Skip!");
				} else {
					gene.addToCollection("diseases", diseaseRef);
				}
			}
			store(gene);
			ret = gene.getIdentifier();
			geneMap.put(geneId, ret);
		}
		return ret;
	}

	public File getMim2geneFile() {
		return mim2geneFile;
	}

	public void setMim2geneFile(File mim2geneFile) {
		this.mim2geneFile = mim2geneFile;
	}

}
