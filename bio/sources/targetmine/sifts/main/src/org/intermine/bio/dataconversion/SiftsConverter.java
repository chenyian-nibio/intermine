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
 * Parse protein chain from 'pdb_chain_uniprot.lst'
 * 
 * @author chenyian
 */
public class SiftsConverter extends BioFileConverter {
//	private static Logger LOG = Logger.getLogger(SiftsConverter.class);
	//
	private static final String DATASET_TITLE = "SIFTS";
	private static final String DATA_SOURCE_NAME = "PDBe";

	private Map<String, String> proteinMap = new HashMap<String, String>();
	private Map<String, String> proteinStructureMap = new HashMap<String, String>();
	private Map<String, String> proteinChainMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public SiftsConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		// skip header
		iterator.next();
		while (iterator.hasNext()) {
			String[] cols = iterator.next();

			Item item = createItem("ProteinStructureRegion");
			item.setAttribute("seqresStart", cols[4]);
			item.setAttribute("seqresEnd", cols[5]);
			item.setAttribute("pdbSeqStart", cols[6]);
			item.setAttribute("pdbSeqEnd", cols[7]);
			item.setAttribute("start", cols[8]);
			item.setAttribute("end", cols[9]);
			item.setReference("protein", getProtein(cols[2]));
			item.setReference("chain", getProteinChain(cols[0], cols[1]));
			
			store(item);
		}
	}

	private String getProtein(String primaryAccession) throws ObjectStoreException {
		String ret = proteinMap.get(primaryAccession);
		if (ret == null) {
			Item item = createItem("Protein");
			item.setAttribute("primaryAccession", primaryAccession);
			store(item);
			ret = item.getIdentifier();
			proteinMap.put(primaryAccession, ret);
		}
		return ret;
	}

	private String getProteinChain(String pdbId, String chainId) throws ObjectStoreException {
		String identifier = pdbId + chainId;
		String ret = proteinChainMap.get(identifier);
		if (ret == null) {
			Item item = createItem("ProteinChain");
			item.setAttribute("pdbId", pdbId);
			item.setAttribute("chain", chainId);
			item.setAttribute("identifier", identifier);
			item.setReference("structure", getProteinStructure(pdbId));
			store(item);
			ret = item.getIdentifier();
			proteinChainMap.put(identifier, ret);
		}
		return ret;
	}

	private String getProteinStructure(String pdbId) throws ObjectStoreException {
		String ret = proteinStructureMap.get(pdbId);
		if (ret == null) {
			Item item = createItem("ProteinStructure");
			item.setAttribute("pdbId", pdbId);
			store(item);
			ret = item.getIdentifier();
			proteinStructureMap.put(pdbId, ret);
		}
		return ret;
	}

}
