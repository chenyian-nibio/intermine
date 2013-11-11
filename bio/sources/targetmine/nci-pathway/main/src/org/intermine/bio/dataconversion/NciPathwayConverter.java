package org.intermine.bio.dataconversion;

/*
 * Copyright (C) 2002-2009 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.BufferedReader;
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
 * Parse NCI Nature Pathway from the flat file uniprot.tab
 * 
 * @author chenyian
 */
public class NciPathwayConverter extends BioFileConverter {
//	private static final Logger LOG = Logger.getLogger(NciPathwayConverter.class);
	//
	private static final String DATASET_TITLE = "NCI Pathway Interaction Database";
	private static final String DATA_SOURCE_NAME = "NCI-Nature";

	private Map<String, Item> proteinMap = new HashMap<String, Item>();
	private Map<String, String> pathwayMap = new HashMap<String, String>();

	// NCI Pathway only contains human data
	private static final String TAXONID = "9606";

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public NciPathwayConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);

	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(reader));
		
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			
			Item protein = getProtein(cols[0]);
			protein.addToCollection("pathways", getPathway(cols[2], cols[1]));
		}
		
		for (Item item : proteinMap.values()) {
			store(item);
		}

	}

	private Item getProtein(String primaryAccession) throws ObjectStoreException {
		// remove isoform tag
		if (primaryAccession.contains("-")) {
			primaryAccession = primaryAccession.split("-")[0];
		}
		Item ret = proteinMap.get(primaryAccession);
		if (ret == null) {
			ret = createItem("Protein");
			ret.setAttribute("primaryAccession", primaryAccession);
			ret.setReference("organism", getOrganism(TAXONID));
			proteinMap.put(primaryAccession, ret);
		}

		return ret;
	}

	private String getPathway(String pid, String name) throws ObjectStoreException {
		String ret = pathwayMap.get(pid);
		if (ret == null) {
			Item pathway = createItem("Pathway");
			pathway.setAttribute("name", name);
			pathway.setAttribute("identifier", pid);
			pathway.setReference("organism", getOrganism(TAXONID));
//			pathway.setAttribute("curated", "true");

			store(pathway);

			ret = pathway.getIdentifier();
			pathwayMap.put(pid, ret);
		}
		return ret;
	}
}
