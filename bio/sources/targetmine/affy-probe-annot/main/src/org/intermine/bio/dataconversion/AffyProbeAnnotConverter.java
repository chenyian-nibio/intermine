package org.intermine.bio.dataconversion;

/*
 * Copyright (C) 2002-2011 FlyMine
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
public class AffyProbeAnnotConverter extends BioFileConverter
{
    //
    private static final String DATASET_TITLE = "NetAffx Annotation Files";
    private static final String DATA_SOURCE_NAME = "Affymetrix";
    
    private Map<String, String> geneMap = new HashMap<String, String>();

    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public AffyProbeAnnotConverter(ItemWriter writer, Model model) {
        super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
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
			String geneIds = cols[18];
			
			if (probeSetId.startsWith("AFFX-")) {
				// skip the internal probe
				continue;
			}
			
			Item item = createItem("ProbeSet");
			item.setAttribute("probeSetId", probeSetId);
			if (!geneIds.equals("---")){
				for (String geneId : geneIds.split(" /// ")) {
					item.addToCollection("genes", getGene(geneId));
				}
			}
			store(item);
		}

    }
    
	private String getGene(String ncbiGeneId) throws ObjectStoreException {
		String ret = geneMap.get(ncbiGeneId);
		if (ret == null) {
			Item item = createItem("Gene");
			item.setAttribute("ncbiGeneId", ncbiGeneId);
			store(item);
			ret = item.getIdentifier();
			geneMap.put(ncbiGeneId, ret);
		}
		return ret;
	}

    
}
