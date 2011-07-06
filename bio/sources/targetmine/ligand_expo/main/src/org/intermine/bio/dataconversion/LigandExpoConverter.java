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
 * @author Chen Yian
 * @since 2009/6/24
 */
public class LigandExpoConverter extends BioFileConverter {

	protected static final Logger LOG = Logger.getLogger(LigandExpoConverter.class);

	//
	private static final String DATASET_TITLE = "Ligand Expo";
	private static final String DATA_SOURCE_NAME = "Protein Data Bank";

	private Map<String, String> structureMap = new HashMap<String, String>();

	private Map<String, Item> hetGroupMap = new HashMap<String, Item>();

	private Map<String, String> compoundGroupMap = new HashMap<String, String>();

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
		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			Item het = getHetGroup(cols[0]);

			String allPdbId = cols[1];
			StringUtils.chomp(allPdbId);
			String[] pdbIds = StringUtils.split(allPdbId, " ");
			for (String pdbId : pdbIds) {
				if (pdbId.length() > 4) {
					LOG.error("Illeagel pdbId: '" + pdbId + "' found at compound '" + cols[0]
							+ "'.");
					continue;
				}
				het.addToCollection("structures", getProteinStructure(pdbId));
			}
		}
		readInchiKeyFile();
	}

	private Item getHetGroup(String hetId) {
		Item ret = hetGroupMap.get(hetId);
		if (ret == null) {
			ret = createItem("HetGroup");
			ret.setAttribute("hetId", hetId);
			ret.setAttribute("identifier", String.format("HetGroup: %s", hetId));
			hetGroupMap.put(hetId, ret);
		}
		return ret;
	}

	private String getProteinStructure(String pdbId) throws ObjectStoreException {
		String ret = structureMap.get(pdbId);
		if (ret == null) {
			Item item = createItem("ProteinStructure");
			item.setAttribute("pdbId", pdbId);
			store(item);
			ret = item.getIdentifier();
			structureMap.put(pdbId, ret);
		}
		return ret;
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

	private File inchiKeyFile;

	public void setInchiKeyFile(File inchiKeyFile) {
		this.inchiKeyFile = inchiKeyFile;
	}

	private void readInchiKeyFile() throws Exception {
		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(new FileReader(inchiKeyFile)));

		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			
			LOG.info(cols[0]);
			if (StringUtils.isEmpty(cols[0])) {
				LOG.info("Empty InChIKey for id :" + cols[1]);
				continue;
			}
			Item hetGroup = getHetGroup(cols[1]);
			String inchiKey = cols[0].substring(0, cols[0].indexOf("-"));
			if (inchiKey.length() != 14) {
				LOG.info(String.format("Bad InChIKey value: %s, %s .", cols[1], cols[0]));
				continue;
			}
			hetGroup.setReference("compoundGroup", getCompoundGroup(inchiKey));
		}
	}
	
	@Override
	public void close() throws Exception {
		store(hetGroupMap.values());
	}

}
