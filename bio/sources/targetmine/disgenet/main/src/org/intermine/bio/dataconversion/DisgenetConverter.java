package org.intermine.bio.dataconversion;

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
 * parse the file 'curated_gene_disease_associations.tsv' downloaded from DisGeNET
 * 
 * @author chenyian
 */
public class DisgenetConverter extends BioFileConverter
{
    //
	private static final String DATASET_TITLE = "DisGeNET";
	private static final String DATA_SOURCE_NAME = "DisGeNET";
	
	private Map<String, String> sourceNameMap = new HashMap<String, String>();

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
    }

    /**
     * 
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
    	Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
    	
    	while (iterator.hasNext()) {
    		String[] cols = iterator.next();
    		if (!cols[0].startsWith("umls:")) {
    			continue;
    		}
    		
    		Item item = createItem("Disease");
    		item.setReference("diseaseTerm", getDiseaseTerm(cols[0], cols[5]));
    		item.setReference("gene", getGene(cols[1]));
    		for (String sourceId: cols[6].split(",")) {
    			String name = sourceNameMap.get(sourceId);
    			if (name != null) {
    				item.addToCollection("sources", getDataSource(name));
    			}
    		}
    		store(item);
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
			item.setAttribute("title", title);
			ret = item.getIdentifier();
			store(item);
			diseaseTermMap.put(identifier, ret);
		}
		return ret;
	}

}
