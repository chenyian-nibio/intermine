package org.intermine.bio.dataconversion;

import java.io.Reader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class WwpdbConverter extends BioFileConverter {
	protected static final Logger LOG = Logger.getLogger(WwpdbConverter.class);
	//
	private static final String DATASET_TITLE = "wwPDB";
	private static final String DATA_SOURCE_NAME = "World Wide Protein Data Bank";

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
	public WwpdbConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {

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

			store(proteinStructure);
			pdbIds.add(pdbid);
		}

	}
}
