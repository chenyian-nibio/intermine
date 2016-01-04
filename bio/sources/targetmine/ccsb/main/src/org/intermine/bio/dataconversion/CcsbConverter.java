package org.intermine.bio.dataconversion;

/*
 * Copyright (C) 2002-2015 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

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
 * @author chenyian
 */
public class CcsbConverter extends BioFileConverter
{
	//
    private static final String DATASET_TITLE = "Human Interactome Database";
    private static final String DATA_SOURCE_NAME = "CCSB";

    // Only human interactions in this source
    private static final String TAXON_ID = "9606";
    
    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public CcsbConverter(ItemWriter writer, Model model) {
        super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
    }

    /**
     * 
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
    	Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
    	
		// skip header
		iterator.next();

		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			
			Item interaction = createItem("Interaction");
			interaction.setReference("gene1", getGene(cols[0], TAXON_ID));
			interaction.setReference("gene2", getGene(cols[2], TAXON_ID));
			store(interaction);
			Item confidence = createItem("InteractionConfidence");
			confidence.setAttribute("type", "HCDP");
			confidence.setReference("interaction", interaction);
			store(confidence);
			
			if (!cols[0].equals(cols[2])) {
				Item interaction2 = createItem("Interaction");
				interaction2.setReference("gene1", getGene(cols[2], TAXON_ID));
				interaction2.setReference("gene2", getGene(cols[0], TAXON_ID));
				store(interaction2);
				Item confidence2 = createItem("InteractionConfidence");
				confidence2.setAttribute("type", "HCDP");
				confidence2.setReference("interaction", interaction2);
				store(confidence2);
			}
		}
    }

	private Map<String, String> geneMap = new HashMap<String, String>();

	private String getGene(String geneId, String taxonId) throws ObjectStoreException {
		String ret = geneMap.get(geneId);

		if (ret == null) {
			Item gene = createItem("Gene");
			gene.setAttribute("primaryIdentifier", geneId);
			gene.setAttribute("ncbiGeneId", geneId);
			gene.setReference("organism", getOrganism(taxonId));
			ret = gene.getIdentifier();
			geneMap.put(geneId, ret);
			store(gene);
		}
		return ret;
	}

}
