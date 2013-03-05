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
 * @since 2009/6/24
 * 2012/8/15 modified
 */
public class LigandExpoConverter extends BioFileConverter {

	protected static final Logger LOG = Logger.getLogger(LigandExpoConverter.class);

	//
	private static final String DATASET_TITLE = "Ligand Expo";
	private static final String DATA_SOURCE_NAME = "RCSB Protein Data Bank";

	private Map<String, String> structureMap = new HashMap<String, String>();

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
		readInchiKeyFile();

		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		while (iterator.hasNext()) {
			String[] cols = iterator.next();

			Item het = createItem("PDBCompound");
			het.setAttribute("hetId", cols[0]);
			String name = hetNameMap.get(cols[0]);
			if (name != null) {
				het.setAttribute("name", name);
			}
			String inchiKey = inchiKeyMap.get(cols[0]);
			if (inchiKey != null) {
				het.setAttribute("inchiKey", inchiKey);

				String compoundGroupId = inchiKey.substring(0, inchiKey.indexOf("-"));
				if (compoundGroupId.length() == 14) {
					het.setReference("compoundGroup", getCompoundGroup(compoundGroupId, name));
				} else {
					LOG.info(String.format("Bad InChIKey value: %s, %s .", cols[1], cols[0]));
				}
			}
			het.setAttribute("identifier", String.format("PDBCompound: %s", cols[0]));

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
			store(het);
		}
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

	private String getCompoundGroup(String inchiKey, String name) throws ObjectStoreException {
		String ret = compoundGroupMap.get(inchiKey);
		if (ret == null) {
			Item item = createItem("CompoundGroup");
			item.setAttribute("identifier", inchiKey);
			if (name != null) {
				item.setAttribute("name", name);
			}
			store(item);
			ret = item.getIdentifier();
			compoundGroupMap.put(inchiKey, ret);
		}
		return ret;
	}

	private Map<String, String> hetNameMap = new HashMap<String, String>();
	private Map<String, String> inchiKeyMap = new HashMap<String, String>();

	private File inchiKeyFile;

	public void setInchiKeyFile(File inchiKeyFile) {
		this.inchiKeyFile = inchiKeyFile;
	}

	private void readInchiKeyFile() throws Exception {
		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(new FileReader(inchiKeyFile)));

		// Example:
		// CXHHBNMLPJOKQD-UHFFFAOYSA-N 000 methyl hydrogen carbonate
		while (iterator.hasNext()) {
			String[] cols = iterator.next();

			if (cols.length < 3) {
				LOG.error(StringUtils.join(cols,"\t"));
				continue;
			}
			// process het name
			String name = cols[2].trim();
			name = name.replaceAll("^[;|\"]", "");
			name = name.replaceAll("[;|\"]$", "");
			hetNameMap.put(cols[1].trim(), name);

			// LOG.info(cols[0]);
			if (StringUtils.isEmpty(cols[0])) {
				LOG.info("Empty InChIKey for id :" + cols[1]);
				continue;
			}
			String inchiKey = cols[0];
			inchiKeyMap.put(cols[1], inchiKey);
		}
	}

}
