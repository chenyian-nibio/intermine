package org.intermine.bio.dataconversion;

/*
 * Copyright (C) 2002-2016 FlyMine
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.sql.Database;
import org.intermine.xml.full.Item;

/**
 * 
 * @author
 */
public class ChemblHierarchyConverter extends BioDBConverter
{
    // 
	private static final String DATASET_TITLE = "ChEMBL";
	private static final String DATA_SOURCE_NAME = "ChEMBL";

	private Map<String, Item> compoundMap = new HashMap<String, Item>();

    /**
     * Construct a new ChemblHierarchyConverter.
     * @param database the database to read from
     * @param model the Model used by the object store we will write to with the ItemWriter
     * @param writer an ItemWriter used to handle Items created
     */
    public ChemblHierarchyConverter(Database database, Model model, ItemWriter writer) {
        super(database, model, writer, DATA_SOURCE_NAME, DATASET_TITLE);
    }


    /**
     * {@inheritDoc}
     */
    public void process() throws Exception {
		Connection connection = getDatabase().getConnection();

		Statement stmt = connection.createStatement();
		String queryHie = " select md.chembl_id, md2.chembl_id as parent "
				+ " from molecule_dictionary as md "
				+ " join molecule_hierarchy as ms on md.molregno=ms.molregno "
				+ " join molecule_dictionary as md2 on md2.molregno=ms.parent_molregno ";
		ResultSet result = stmt.executeQuery(queryHie);
		Map<String, Set<String>> parentMap = new HashMap<String, Set<String>>();
		while (result.next()) {
			String chemblId = result.getString("chembl_id");
			String parentId = result.getString("parent");
			if (parentMap.get(parentId) == null) {
				parentMap.put(parentId, new HashSet<String>());
			}
			parentMap.get(parentId).add(chemblId);
		}
		
		for (String parentId: parentMap.keySet()) {
			Set<String> set = parentMap.get(parentId);
//			getChemblCompound(parentId, null);
			for (String id1 : set) {
				for (String id2 : set) {
					if (!id1.equals(id2)) {
						Item compound = getChemblCompound(id1, parentId);
						compound.addToCollection("alternateForms", getChemblCompound(id2, parentId));
					}
				}
			}
		}
		
		
		stmt.close();
		connection.close();
    }
    
    @Override
    public void close() throws Exception {
    	store(compoundMap.values());
    }
    
	private Item getChemblCompound(String chemblId, String parentId) throws ObjectStoreException {
		Item ret = compoundMap.get(chemblId);
		if (ret == null) {
			ret = createItem("ChemblCompound");
			ret.setAttribute("originalId", chemblId);
//			ret.setAttribute("identifier", String.format("ChEMBL:%s", chemblId));
			if (parentId != null && !parentId.equals(chemblId)) {
				ret.setReference("parent", getChemblCompound(parentId, null));
			}
			compoundMap.put(chemblId, ret);
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
