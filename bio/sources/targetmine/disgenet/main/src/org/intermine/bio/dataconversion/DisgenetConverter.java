package org.intermine.bio.dataconversion;

import java.io.File;
import java.io.FileNotFoundException;
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
 * parse the file 'curated_gene_disease_associations.tsv' downloaded from DisGeNET. </br>
 * refine the parser for version 5.0 (2018/3/27)
 * 
 * @author chenyian
 */
public class DisgenetConverter extends BioFileConverter {
	
	private static final Logger LOG = Logger.getLogger(DisgenetConverter.class);
    //
	private static final String DATASET_TITLE = "DisGeNET";
	private static final String DATA_SOURCE_NAME = "DisGeNET";

    private File pmidFile;

    public void setPmidFile(File pmidFile) {
		this.pmidFile = pmidFile;
	}

    private File diseaseMapFile;
    
    public void setDiseaseMapFile(File diseaseMapFile) {
    	this.diseaseMapFile = diseaseMapFile;
    }
    
	private Map<String, String> sourceNameMap = new HashMap<String, String>();
	private Map<String, Set<Integer>> pmidMap = new HashMap<String, Set<Integer>>();
	private Map<String, Set<String>> ontologyIdMap = new HashMap<String, Set<String>>();

    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public DisgenetConverter(ItemWriter writer, Model model) {
        super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
        sourceNameMap.put("CTD_human", "Comparative Toxicogenomics Database");
        sourceNameMap.put("GWASCAT", "NHGRI GWAS Catalog");
        sourceNameMap.put("UNIPROT", "UniProt");
        sourceNameMap.put("ORPHANET", "Orphanet");
        sourceNameMap.put("CLINVAR", "ClinVar");
        sourceNameMap.put("PSYGENET", "PsyGeNET");
        sourceNameMap.put("HPO", "Human Phenotype Ontology");
    }

    /**
     * 
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
    	readPmidMap();
    	readDiseaseMap();
    	
    	Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
    	
    	// first line is the header
    	iterator.next();
    	
    	while (iterator.hasNext()) {
    		String[] cols = iterator.next();
    		String diseaseId = cols[2];
    		String diseaseName = cols[3];
    		String geneId = cols[0];
    		String source = cols[7];
    		
    		Item item = createItem("Disease");
			item.setReference("diseaseTerm", getDiseaseTerm(diseaseId, diseaseName));
			item.setReference("gene", getGene(geneId));
			for (String sourceId: source.split(",")) {
    			String name = sourceNameMap.get(sourceId);
    			if (name != null) {
    				item.addToCollection("sources", getDataSource(name));
    			}
    		}
			String key = String.format("%s-%s", geneId, diseaseId);
			if (pmidMap.get(key) != null) {
				for (Integer pmid : pmidMap.get(key)) {
					item.addToCollection("publications", getPublication(pmid.toString()));
				}
			}
			
    		store(item);
    	}

    }

	private void readPmidMap() {
		String fn = pmidFile.getName();
		LOG.info(String.format("Parsing the file %s ......", fn));
		System.out.println(String.format("Parsing the file %s ......", fn));
		
		try {
			FileReader reader = new FileReader(pmidFile);
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);

			// first line is the header
	    	iterator.next();
	    	
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				String geneId = cols[0];
				String diseaseId = cols[1];
				String pmid = cols[4];
				try {
					String key = String.format("%s-%s", geneId, diseaseId);
					if (pmidMap.get(key) == null) {
						pmidMap.put(key, new HashSet<Integer>());
					}
					pmidMap.get(key).add(Integer.parseInt(pmid));
				} catch (NumberFormatException e) {
					LOG.info(String.format("Unable to process the pmid: %s (%s, %s)", pmid, geneId, diseaseId));
					continue;
				}
			}
			reader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot found pmidFile: " + fn);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void readDiseaseMap() {
		String fn = diseaseMapFile.getName();
		LOG.info(String.format("Parsing the file %s ......", fn));
		System.out.println(String.format("Parsing the file %s ......", fn));
		
		try {
			FileReader reader = new FileReader(diseaseMapFile);
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
			
			// first line is the header
			iterator.next();
			
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				String diseaseId = cols[0];
				String ontology = cols[2];
				String code = cols[3];
				
				String ontologyId = "";
				if (ontology.equals("DO")) {
					ontologyId = "DOID:" + code;
				} else if (ontology.equals("MSH")) {
					ontologyId = code;
				} else {
					continue;
				}
				
				if (ontologyIdMap.get(diseaseId) == null) {
					ontologyIdMap.put(diseaseId, new HashSet<String>());
				}
				ontologyIdMap.get(diseaseId).add(ontologyId);
			}
			reader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot found pmidFile: " + fn);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
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

	private Map<String, String> diseaseTermMap = new HashMap<String, String>();
	private String getDiseaseTerm(String identifier, String title) throws ObjectStoreException {
		String ret = diseaseTermMap.get(identifier);
		if (ret == null) {
			Item item = createItem("DiseaseTerm");
			item.setAttribute("identifier", identifier);
			item.setAttribute("name", title);
			
			if (ontologyIdMap.get(identifier) != null) {
				for (String ontologyId : ontologyIdMap.get(identifier)) {
					if (ontologyId.startsWith("DOID:")) {
						item.addToCollection("crossReferences", getDOTerm(ontologyId));
					} else {
						item.addToCollection("crossReferences", getMeshTerm(ontologyId));
					}
				}
			}
			
			store(item);
			
			ret = item.getIdentifier();
			diseaseTermMap.put(identifier, ret);
		}
		return ret;
	}

	private Map<String, String> publicationMap = new HashMap<String, String>();
	private String getPublication(String pubmedId) throws ObjectStoreException {
		String ret = publicationMap.get(pubmedId);
		if (ret == null) {
			Item item = createItem("Publication");
			item.setAttribute("pubMedId", pubmedId);
			store(item);
			ret = item.getIdentifier();
			publicationMap.put(pubmedId, ret);
		}
		return ret;
	}

	private Map<String, String> ontologyItemMap = new HashMap<String, String>();
	private String getDOTerm(String identifier) throws ObjectStoreException {
		String ret = ontologyItemMap.get(identifier);
		if (ret == null) {
			Item item = createItem("DOTerm");
			item.setAttribute("identifier", identifier);
			store(item);
			ret = item.getIdentifier();
			ontologyItemMap.put(identifier, ret);
		}
		return ret;
	}
	private String getMeshTerm(String meshId) throws ObjectStoreException {
		String ret = ontologyItemMap.get(meshId);
		if (ret == null) {
			Item item = createItem("MeshTerm");
			item.setAttribute("identifier", meshId);
			store(item);
			ret = item.getIdentifier();
			ontologyItemMap.put(meshId, ret);
		}
		return ret;
	}
	
	

}
