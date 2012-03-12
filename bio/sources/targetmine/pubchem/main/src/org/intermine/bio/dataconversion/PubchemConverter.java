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
import java.io.File;
import java.io.FileReader;
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

	private Map<String, Item> compoundGroupMap = new HashMap<String, Item>();

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

	private File meshFile;

	public void setMeshFile(File file) {
		this.meshFile = file;
	}

	private Map<String, String> meshNameMap = new HashMap<String, String>();

	private void getMeshName() throws Exception {
		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(new FileReader(meshFile)));
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			meshNameMap.put(cols[0], cols[1]);
		}
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		getMeshName();
		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(reader));

		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String cid = cols[0];
			if (StringUtils.isEmpty(cols[1])) {
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
			String name = meshNameMap.get(cid);
			// TODO chenyian: if name is not available, use identifier instead
			if (name != null) {
//				item.setAttribute("name", name + " (MeSH)");
				item.setAttribute("name", name);
			}
			item.setReference("compoundGroup", getCompoundGroup(inchiKey, name));
			store(item);
		}
	}

	private Map<String, String> nameMap = new HashMap<String, String>();

	private Item getCompoundGroup(String inchiKey, String name) throws ObjectStoreException {
		Item ret = compoundGroupMap.get(inchiKey);
		if (ret == null) {
			ret = createItem("CompoundGroup");
			ret.setAttribute("identifier", inchiKey);
			compoundGroupMap.put(inchiKey, ret);
		}
		// randomly pick one name
		// TODO chenyian: if name is not available, use identifier instead
		if (nameMap.get(inchiKey) == null && name != null) {
			nameMap.put(inchiKey, name);
			ret.setAttribute("name", name);
		}
		return ret;
	}

	@Override
	public void close() throws Exception {
		store(compoundGroupMap.values());
	}

}
