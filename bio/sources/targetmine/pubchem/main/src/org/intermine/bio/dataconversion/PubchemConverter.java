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

import org.apache.commons.lang.StringUtils;
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
public class PubchemConverter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(PubchemConverter.class);
	//
	private static final String DATASET_TITLE = "PubChem";
	private static final String DATA_SOURCE_NAME = "PubChem";

	private Map<String, String> compoundGroupMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public PubchemConverter(ItemWriter writer, Model model) {
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
			String cid = cols[0];
			if (StringUtils.isEmpty(cols[1])){
				LOG.info("Empty InChIKey for id :" + cid);
				continue;
			}
			String inchiKey = cols[1].substring(0, cols[1].indexOf("-"));
			if (inchiKey.length() != 14) {
				LOG.info(String.format("Bad InChIKey value: %s, %s .", cid, cols[0]));
				continue;
			}
			Item item = createItem("PubChemCompound");
			item.setAttribute("identifier", String.format("PubChem: %s", cid));
			item.setAttribute("pubChemCid", cid);
			item.setReference("compoundGroup", getCompoundGroup(inchiKey));
			store(item);
		}
	}

	private String getCompoundGroup(String inchiKey) throws ObjectStoreException {
		String ret = compoundGroupMap.get(inchiKey);
		if (ret == null) {
			Item item = createItem("CompoundGroup");
			item.setAttribute("inchiKey", inchiKey);
			store(item);
			ret = item.getIdentifier();
			compoundGroupMap.put(inchiKey, ret);
		}
		return ret;
	}

}
