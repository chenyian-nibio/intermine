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
 * 
 * @author Chen Yian
 * @since 2009/6/24
 */
public class LigandExpoConverter extends BioFileConverter {

	protected static final Logger LOG = Logger
			.getLogger(LigandExpoConverter.class);

	//
	private static final String DATASET_TITLE = "RCSB Protein Data Bank";
	private static final String DATA_SOURCE_NAME = "PDB";

	private Map<String, Item> structureMap = new HashMap<String, Item>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public LigandExpoConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(reader);
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			Item het = createItem("HetGroup");
			het.setAttribute("hetId", cols[0]);

			String allPdbId = cols[1];
			StringUtils.chomp(allPdbId);
			String[] pdbIds = StringUtils.split(allPdbId, " ");
			for (String pdbId : pdbIds) {
				if (pdbId.length() > 4) {
					LOG.error("Illeagel pdbId: '" + pdbId
							+ "' found at compound '" + cols[0] + "'.");
					continue;
				}
				Item structure = getProteinStructure(pdbId);
				het.addToCollection("structures", structure);
			}

			store(het);
		}

	}

	private Item getProteinStructure(String pdbId) throws ObjectStoreException {
		if (structureMap.containsKey(pdbId)) {
			return structureMap.get(pdbId);
		}
		Item ret = createItem("ProteinStructure");
		ret.setAttribute("pdbId", pdbId);
		store(ret);
		structureMap.put(pdbId, ret);
		return ret;
	}
}
