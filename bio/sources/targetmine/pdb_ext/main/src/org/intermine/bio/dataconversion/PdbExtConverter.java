package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public class PdbExtConverter extends BioFileConverter {

	protected static final Logger LOG = Logger.getLogger(PdbExtConverter.class);

	//
	private static final String DATASET_TITLE = "Protein Data Bank";
	private static final String DATA_SOURCE_NAME = "Protein Data Bank";

	private File pdbPubmedFile;

	private Map<String, List<String>> pdbIdPubmedIdMap = new HashMap<String, List<String>>();
	private Map<String, String> pubmedMap = new HashMap<String, String>();

	// column index
	private static final int PDB_ID = 0;
	private static final int HEADER = 1;
	private static final int COMPOUND = 3;
	private static final int SOURCE = 4;
	private static final int RESOLUTION = 6;
	private static final int EXPERIMENT_TYPE = 7;

	// For prevent duplicated data.
	private Set<String> pdbIds = new HashSet<String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public PdbExtConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);

	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		// create pdbId pubmedId first
		createPdbIdPubmedIdMap();

		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);

		// skip first 2 lines (header)
		iterator.next();
		iterator.next();

		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String pdbid = cols[PDB_ID].toLowerCase();
			if (pdbIds.contains(pdbid)) {
				LOG.error("Duplicated pdbId found: '" + pdbid + "', this line will be skipped!");
				continue;
			}
			Item proteinStructure = createItem("ProteinStructure");
			proteinStructure.setAttribute("pdbId", pdbid);
			if (!cols[RESOLUTION].equals("NOT")) {
				proteinStructure.setAttribute("resolution", cols[RESOLUTION]);
			}
			proteinStructure.setAttribute("experimentType", cols[EXPERIMENT_TYPE]);

			proteinStructure.setAttribute("name", cols[COMPOUND]);
			if (cols[HEADER].length() != 0) {
				proteinStructure.setAttribute("classification", cols[HEADER]);
			}
			if (cols[SOURCE].length() != 0) {
				proteinStructure.setAttribute("source", cols[SOURCE]);
			}

			if (pdbIdPubmedIdMap.get(pdbid) != null) {
				for (String refId : pdbIdPubmedIdMap.get(pdbid)) {
					proteinStructure.addToCollection("publications", refId);
				}
			}

			store(proteinStructure);
			pdbIds.add(pdbid);
		}

	}

	private void createPdbIdPubmedIdMap() throws IOException, ObjectStoreException {
		if (pdbPubmedFile == null) {
			throw new NullPointerException("pdbPubmedFile property not set");
		}

		BufferedReader reader = new BufferedReader(new FileReader(pdbPubmedFile));

		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);

		// skip header
		iterator.next();
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String pdbid = cols[0].toLowerCase();
			String pubmedid = cols[2];
			if (pdbIdPubmedIdMap.get(pdbid) == null) {
				pdbIdPubmedIdMap.put(pdbid, new ArrayList<String>());
			}
			pdbIdPubmedIdMap.get(pdbid).add(getPublication(pubmedid));
		}
	}

	private String getPublication(String pubmedid) throws ObjectStoreException {
		String ret = pubmedMap.get(pubmedid);
		if (ret == null) {
			Item publication = createItem("Publication");
			publication.setAttribute("pubMedId", pubmedid);
			store(publication);
			ret = publication.getIdentifier();
			pubmedMap.put(pubmedid, ret);
		}
		return ret;
	}

	public void setPdbPubmedFile(File pdbPubmedFile) {
		this.pdbPubmedFile = pdbPubmedFile;
	}

}