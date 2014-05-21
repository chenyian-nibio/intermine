package org.intermine.bio.dataconversion;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.sql.Database;
import org.intermine.xml.full.Item;

/**
 * Store ChEMBL protein IDs as the protein synonyms
 * 
 * @author chenyian
 */
public class ChemblProteinConverter extends BioDBConverter {
	//
	private static final String DATASET_TITLE = "ChEMBL";
	private static final String DATA_SOURCE_NAME = "EMBL-EBI";

	private Map<String, String> proteinMap = new HashMap<String, String>();

	/**
	 * Construct a new ChemblProteinConverter.
	 * 
	 * @param database
	 *            the database to read from
	 * @param model
	 *            the Model used by the object store we will write to with the ItemWriter
	 * @param writer
	 *            an ItemWriter used to handle Items created
	 */
	public ChemblProteinConverter(Database database, Model model, ItemWriter writer) {
		super(database, model, writer, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * {@inheritDoc}
	 */
	public void process() throws Exception {
		Connection connection = getDatabase().getConnection();

		Statement stmt = connection.createStatement();
		String queryName = "select td.chembl_id, accession " + " from target_dictionary as td "
				+ " join target_components as tc on tc.tid=td.tid "
				+ " join component_sequences as cseq on cseq.component_id=tc.component_id "
				+ " where accession is not null ";
		ResultSet resName = stmt.executeQuery(queryName);
		while (resName.next()) {
			String chemblId = resName.getString("chembl_id");
			String accession = resName.getString("accession");
			
			createSynonym(getProtein(accession), chemblId, true);
		}
	}

	private String getProtein(String accession) throws ObjectStoreException {
		String ret = proteinMap.get(accession);
		if (ret == null) {
			Item item = createItem("Protein");
			item.setAttribute("primaryAccession", accession);
			store(item);
			ret = item.getIdentifier();
			proteinMap.put(accession, ret);
		}
		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDataSetTitle(int taxonId) {
		return DATASET_TITLE;
	}
}
