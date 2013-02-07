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

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * Parse protein chain from 'pdb_chain_uniprot.lst', 'pdb_chain_taxonomy.lst' and 'pdb_pubmed.lst'
 * 
 * @author chenyian
 */
public class SiftsConverter extends BioFileConverter {
	private static Logger LOG = Logger.getLogger(SiftsConverter.class);
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
		if (chainOrganismMap == null || chainMolTypeMap == null) {
			readPdbChainTaxonFile();
		}
		if (pdbIdPubmedIdMap==null) {
			readPdbPubmedFile();
		}

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
			
			String type = chainMolTypeMap.get(identifier);
			if (type != null) {
				item.setAttribute("moleculeType", type);
			}

			Set<String> organism = chainOrganismMap.get(identifier);
			if (organism != null) {
				for (String taxId : organism) {
					if (taxId != null && taxId.length() > 0 && StringUtils.isNumeric(taxId)) {
						item.addToCollection("organism", getOrganism(taxId));
					}
				}
			}
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
			if (pdbIdPubmedIdMap.get(pdbId) != null) {
				for (String pubmedId : pdbIdPubmedIdMap.get(pdbId)) {
					item.addToCollection("publications", getPublication(pubmedId));
				}
			}
			store(item);
			ret = item.getIdentifier();
			proteinStructureMap.put(pdbId, ret);
		}
		return ret;
	}

	private File pdbChainTaxonFile;

	public void setPdbChainTaxonFile(File pdbChainTaxonFile) {
		this.pdbChainTaxonFile = pdbChainTaxonFile;
	}

	private HashMap<String, Set<String>> chainOrganismMap;
	private HashMap<String, String> chainMolTypeMap;

	private void readPdbChainTaxonFile() throws Exception {
		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(new FileReader(
				pdbChainTaxonFile));

		chainOrganismMap = new HashMap<String, Set<String>>();
		chainMolTypeMap = new HashMap<String, String>();

		// skip header
		iterator.next();
		while (iterator.hasNext()) {
			// there are more than one tab between columns, for example,
			// "101m\tA\t9755\t\tPROTEIN\t\t\tPhyseter catodon"
			String[] cols = iterator.next();
			String identifier = cols[0] + cols[1];
			String taxId = cols[2];
			String molType = cols[4];

			if (!StringUtils.isEmpty(taxId) && StringUtils.isNumeric(taxId)) {
				if (chainOrganismMap.get(identifier) == null) {
					chainOrganismMap.put(identifier, new HashSet<String>());
				}
				chainOrganismMap.get(identifier).add(taxId);
			}

			if (!StringUtils.isEmpty(molType)){
				chainMolTypeMap.put(identifier, molType);
			}

		}
	}

	private File pdbPubmedFile;

	public void setPdbPubmedFile(File pdbPubmedFile) {
		this.pdbPubmedFile = pdbPubmedFile;
	}

	private Map<String, List<String>> pdbIdPubmedIdMap;
	private Map<String, String> publicationMap = new HashMap<String, String>();

	private void readPdbPubmedFile() throws Exception {
		if (pdbPubmedFile == null) {
			throw new NullPointerException("pdbPubmedFile property not set");
		}
		
		pdbIdPubmedIdMap = new HashMap<String, List<String>>();

		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(new FileReader(
				pdbPubmedFile));

		// skip header
		iterator.next();
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String pdbId = cols[0].toLowerCase();
			String pubmedId = cols[2];
			if (pdbIdPubmedIdMap.get(pdbId) == null) {
				pdbIdPubmedIdMap.put(pdbId, new ArrayList<String>());
			}
			pdbIdPubmedIdMap.get(pdbId).add(pubmedId);
		}
	}

	private String getPublication(String pubmedId) throws ObjectStoreException {
		String ret = publicationMap.get(pubmedId);
		if (ret == null) {
			Item item = createItem("Publication");
			item.setAttribute("pubMedId", pubmedId);
			store(item);
			ret = item.getIdentifier();
			publicationMap.put(pubmedId, ret);
		}
		return ret;
	}
}
