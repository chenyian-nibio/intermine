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

import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * Parse Gene3D to Uniprot file 'uniprot_assignments.csv'. Due to the comma in the species name
 * column, the file has to be processed in advance.
 * 
 * @author chenyian
 */
public class Gene3dConverter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(Gene3dConverter.class);
	//
	private static final String DATASET_TITLE = "Gene3D";
	private static final String DATA_SOURCE_NAME = "Gene3D";

	// private Item dataSet;

	private Map<String, String> proteinMap = new HashMap<String, String>();
	private Map<String, String> cathNodeMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public Gene3dConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		// discard the header
		iterator.next();
		while (iterator.hasNext()) {
			String[] cols = (String[]) iterator.next();
			Item item = createItem("StructuralDomainRegion");
			item.setAttribute("start", cols[4]);
			item.setAttribute("end", cols[5]);
			item.setReference("protein", getProtein(cols[0], cols[1], cols[2]));
			item.setReference("cathClassification", getCathClassification(cols[3]));
			// item.addToCollection("dataSets", getDataset());
			store(item);
		}
	}

	private String getCathClassification(String nodeNumber) throws ObjectStoreException {
		String ret = cathNodeMap.get(nodeNumber);
		if (ret == null) {
			Item item = createItem("CathClassification");
			item.setAttribute("cathCode", nodeNumber);
			store(item);
			ret = item.getIdentifier();
			cathNodeMap.put(nodeNumber, ret);
		}
		return ret;
	}

	private String getProtein(String primaryAccession, String primaryIdentifier, String taxonId)
			throws ObjectStoreException {
		String ret = proteinMap.get(primaryAccession);
		if (ret == null) {
			Item item = createItem("Protein");
			item.setAttribute("primaryAccession", primaryAccession);
//			item.setAttribute("primaryIdentifier", primaryIdentifier);
			item.setReference("organism", getOrganism(taxonId));
			store(item);
			ret = item.getIdentifier();
			proteinMap.put(primaryAccession, ret);
		}
		return ret;
	}
}
