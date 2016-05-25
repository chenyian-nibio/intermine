package org.intermine.bio.dataconversion;

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
public class AffyProbeAnnotConverter extends BioFileConverter {
	
	private static final Logger LOG = Logger.getLogger(AffyProbeAnnotConverter.class);
	//
	private static final String DATASET_TITLE = "NetAffx Annotation Files";
	private static final String DATA_SOURCE_NAME = "Affymetrix";

	private Map<String, String> geneMap = new HashMap<String, String>();

	private Map<String, String> taxonIdMap = new HashMap<String, String>();

    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
	public AffyProbeAnnotConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
		taxonIdMap.put("Homo sapiens", "9606");
		taxonIdMap.put("Mus musculus", "10090");
		taxonIdMap.put("Rattus norvegicus", "10116");
	}

    /**
     * 
     *
     * {@inheritDoc}
     */
	public void process(Reader reader) throws Exception {
		Iterator<String[]> iterator = FormattedTextParser.parseCsvDelimitedReader(reader);
		// skip the column header
		iterator.next();
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String probeSetId = cols[0];
			String speciesName = cols[2];
			String geneIds = cols[18];
			
			if (probeSetId.startsWith("AFFX")) {
				// skip the internal probe
				continue;
			}
			
			Item item = createItem("ProbeSet");
			item.setAttribute("probeSetId", probeSetId);
			String taxonId = taxonIdMap.get(speciesName);
			if (taxonId != null) {
				item.setReference("organism", getOrganism(taxonId));
			} else {
				LOG.info("Unidentified organism found for the probe " + probeSetId);
			}
			if (geneIds.equals("---")){
				item.setAttribute("mappingType", "none");
			} else {
				if (geneIds.contains("///")) {
					for (String geneId : geneIds.split(" /// ")) {
						item.addToCollection("genes", getGene(geneId));
					}
					item.setAttribute("mappingType", "multiple");
				} else {
					item.addToCollection("genes", getGene(geneIds));
					item.setAttribute("mappingType", "single");
				}
			}
			store(item);
		}

	}
    
	private String getGene(String primaryIdentifier) throws ObjectStoreException {
		String ret = geneMap.get(primaryIdentifier);
		if (ret == null) {
			Item item = createItem("Gene");
			item.setAttribute("primaryIdentifier", primaryIdentifier);
			item.setAttribute("ncbiGeneId", primaryIdentifier);
			store(item);
			ret = item.getIdentifier();
			geneMap.put(primaryIdentifier, ret);
		}
		return ret;
	}

    
}
