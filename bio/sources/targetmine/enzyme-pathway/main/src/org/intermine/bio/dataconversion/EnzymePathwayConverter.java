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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.intermine.dataconversion.FileConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class EnzymePathwayConverter extends FileConverter {
	protected static final Logger LOG = Logger.getLogger(EnzymePathwayConverter.class);

	//
	private Map<String, String> pathwayMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public EnzymePathwayConverter(ItemWriter writer, Model model) {
		super(writer, model);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {

		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(reader));

		Map<String, Set<String>> ecPathwayMap = new HashMap<String, Set<String>>();
		// Parse source data to map
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String pathway = cols[0].trim();
			if (!pathway.startsWith("path:ec")) {
				LOG.error("error data format: " + pathway);
				continue;
			}
			String ecNumber = cols[1].trim();
			if (ecNumber.startsWith("ec:")) {
				if (ecPathwayMap.get(ecNumber) == null) {
					ecPathwayMap.put(ecNumber, new HashSet<String>());
				}
//				ecPathwayMap.get(ecNumber).add(pathway.substring(7));
				ecPathwayMap.get(ecNumber).add(pathway.substring(5));
			}
		}
		// create Enzymes and Pathways
		for (String ec : ecPathwayMap.keySet()) {
			Item enzyme = createItem("Enzyme");
			enzyme.setAttribute("ecNumber", ec.substring(3));
			for (String pathwayId : ecPathwayMap.get(ec)) {
				String refId = getPathway(pathwayId);
				enzyme.addToCollection("pathways", refId);
			}
			store(enzyme);
		}

	}

	private String getPathway(String pathwayId) throws ObjectStoreException {
		String ret = pathwayMap.get(pathwayId);
		if (ret == null) {
			Item pathway = createItem("Pathway");
			pathway.setAttribute("identifier", pathwayId);
			ret = pathway.getIdentifier();
			pathwayMap.put(pathwayId, ret);
			store(pathway);
		}
		return ret;
	}
}
