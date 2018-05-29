package org.intermine.bio.dataconversion;

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
 * 
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
		readInchiFile();
		readSmilesFile();

		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		while (iterator.hasNext()) {
			String[] cols = iterator.next();

			String identifier = cols[0];
			Item het = createItem("PDBCompound");
			het.setAttribute("originalId", identifier);
			String name = hetNameMap.get(identifier);
			if (name == null || name.equals("")) {
				name = String.format("HETID %s", identifier);
			}
			// if the length of the name is greater than 40 characters,
			// use id instead and save the long name as the synonym
			if (name.length() > 40) {
				setSynonyms(het, name);
				name = String.format("HETID %s", identifier);
			}
			het.setAttribute("name", name);

			String inchiKey = inchiKeyMap.get(identifier);
			if (inchiKey != null) {
				het.setAttribute("inchiKey", inchiKey);
				
				String compoundGroupId = inchiKey.substring(0, inchiKey.indexOf("-"));
				if (compoundGroupId.length() == 14) {
					het.setReference("compoundGroup", getCompoundGroup(compoundGroupId, name));
				} else {
					LOG.info(String.format("Bad InChIKey value: %s, %s .", cols[1], identifier));
				}
			}
			het.setAttribute("identifier", String.format("PDBCompound:%s", identifier));

			String allPdbId = cols[1];
			StringUtils.chomp(allPdbId);
			String[] pdbIds = StringUtils.split(allPdbId, " ");
			for (String pdbId : pdbIds) {
				if (pdbId.length() > 4) {
					LOG.error("Illeagel pdbId: '" + pdbId + "' found at compound '" + identifier
							+ "'.");
					continue;
				}
				het.addToCollection("pdbStructures", getProteinStructure(pdbId));
			}
			
			String inchi = inchiMap.get(identifier);
			if (inchi != null) {
				Item structure = createItem("CompoundStructure");
				structure.setAttribute("type", "InChI");
				structure.setAttribute("value", inchi);
				structure.setReference("compound", het);
				store(structure);
			}

			String smiles = smilesMap.get(identifier);
			if (smiles != null) {
				Item structure = createItem("CompoundStructure");
				structure.setAttribute("type", "SMILES");
				structure.setAttribute("value", smiles);
				structure.setReference("compound", het);
				store(structure);
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
			item.setAttribute("name", name);
			store(item);
			ret = item.getIdentifier();
			compoundGroupMap.put(inchiKey, ret);
		}
		return ret;
	}

	private Map<String, String> hetNameMap = new HashMap<String, String>();
	private Map<String, String> inchiKeyMap = new HashMap<String, String>();
	private Map<String, String> inchiMap = new HashMap<String, String>();
	private Map<String, String> smilesMap = new HashMap<String, String>();

	private File inchiKeyFile;
	private File inchiFile;
	private File smilesFile;

	public void setInchiKeyFile(File inchiKeyFile) {
		this.inchiKeyFile = inchiKeyFile;
	}
	public void setInchiFile(File inchiFile) {
		this.inchiFile = inchiFile;
	}
	public void setSmilesFile(File smilesFile) {
		this.smilesFile = smilesFile;
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

	private void readInchiFile() throws Exception {
		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(new FileReader(inchiFile)));
		
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			
			if (cols.length < 3) {
				LOG.error(StringUtils.join(cols,"\t"));
				continue;
			}
			if (StringUtils.isEmpty(cols[0])) {
				LOG.info("Empty InChI for id :" + cols[1]);
				continue;
			}
			inchiMap.put(cols[1], cols[0]);
		}
	}

	private void readSmilesFile() throws Exception {
		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(new FileReader(smilesFile)));
		
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			
			if (cols.length < 3) {
				LOG.error(StringUtils.join(cols,"\t"));
				continue;
			}
			if (StringUtils.isEmpty(cols[0])) {
				LOG.info("Empty SMILES for id :" + cols[1]);
				continue;
			}
			smilesMap.put(cols[1], cols[0]);
		}
	}
	
	private void setSynonyms(Item subject, String value) throws ObjectStoreException {
		Item syn = createItem("CompoundSynonym");
		syn.setAttribute("value", value);
		syn.setReference("subject", subject);
		store(syn);
	}

}
