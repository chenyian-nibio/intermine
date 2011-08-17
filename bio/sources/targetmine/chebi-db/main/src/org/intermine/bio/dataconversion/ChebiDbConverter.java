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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.sql.Database;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class ChebiDbConverter extends BioDBConverter {
	private static final Logger LOG = Logger.getLogger(ChebiDbConverter.class);

	// 
	private static final String DATASET_TITLE = "ChEBI";
	private static final String DATA_SOURCE_NAME = "ChEBI";

	private Map<String, Item> compoundGroupMap = new HashMap<String, Item>();

	private Map<String, String> nameMap = new HashMap<String, String>();

	/**
	 * Construct a new ChebiDbConverter.
	 * 
	 * @param database
	 *            the database to read from
	 * @param model
	 *            the Model used by the object store we will write to with the ItemWriter
	 * @param writer
	 *            an ItemWriter used to handle Items created
	 */
	public ChebiDbConverter(Database database, Model model, ItemWriter writer) {
		super(database, model, writer, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * {@inheritDoc}
	 */
	public void process() throws Exception {
		// a database has been initialised from properties starting with db.chebi-db

		Connection connection = getDatabase().getConnection();

		// process data with direct SQL queries on the source database, for example:

		// Statement stmt = connection.createStatement();
		// String query = "select column from table;";
		// ResultSet res = stmt.executeQuery(query);
		// while (res.next()) {
		// }
		Statement stmt = connection.createStatement();
		// String query = "SELECT compound_id, structure FROM structures WHERE type = 'InChIKey';";
		String query = " select c1.name, c1.id, c1.parent_id, c2.name, s1.structure "
				+ " from compounds as c1 "
				+ " left join structures as s1 on c1.id = s1.compound_id "
				+ " left join compounds as c2 on c1.parent_id = c2.id "
				+ " where s1.type='InChIKey' ";
		ResultSet res = stmt.executeQuery(query);
		while (res.next()) {
			LOG.info(String
					.format("id: %s, %s", res.getInt("c1.id"), res.getString("s1.structure")));

			String chebiId = String.valueOf(res.getInt("c1.id"));
			String name = res.getString("c1.name");

			String structure = res.getString("s1.structure");
			String inchiKey = structure.substring(structure.indexOf("=") + 1, structure
					.indexOf("-"));
			if (inchiKey.length() != 14) {
				LOG.info(String.format("Bad InChIKey value: %s, %s .", chebiId, structure));
				continue;
			}

			if (StringUtils.isEmpty(name)) {
				name = res.getString("c2.name");
				chebiId = String.valueOf(res.getInt("c1.parent_id"));
			}

			Item item = createItem("ChebiCompound");
			item.setAttribute("identifier", String.format("CHEBI: %s", chebiId));
			item.setAttribute("chebiId", chebiId);
			item.setAttribute("name", name);

			item.setReference("compoundGroup", getCompoundGroup(inchiKey, name));
			store(item);

		}
	}

	private Item getCompoundGroup(String inchiKey, String name) throws ObjectStoreException {
		Item ret = compoundGroupMap.get(inchiKey);
		if (ret == null) {
			ret = createItem("CompoundGroup");
			ret.setAttribute("identifier", inchiKey);
			compoundGroupMap.put(inchiKey, ret);
		}
		if (nameMap.get(inchiKey) == null || nameMap.get(inchiKey).length() > name.length()) {
			nameMap.put(inchiKey, name);
			ret.setAttribute("name", name);
		}
		return ret;
	}
	
	@Override
	public void close() throws Exception {
		store(compoundGroupMap.values());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDataSetTitle(int taxonId) {
		return DATASET_TITLE;
	}
}
