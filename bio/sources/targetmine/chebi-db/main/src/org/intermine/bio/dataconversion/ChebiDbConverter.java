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

import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.sql.Database;
import org.intermine.xml.full.Item;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 
 * @author
 */
public class ChebiDbConverter extends BioDBConverter
{
	private static final Logger LOG = Logger.getLogger(ChebiDbConverter.class);
	
	// 
    private static final String DATASET_TITLE = "ChEBI";
    private static final String DATA_SOURCE_NAME = "ChEBI";


    /**
     * Construct a new ChebiDbConverter.
     * @param database the database to read from
     * @param model the Model used by the object store we will write to with the ItemWriter
     * @param writer an ItemWriter used to handle Items created
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
         String query = "SELECT compound_id, structure FROM structures WHERE type = 'InChIKey';";
         ResultSet res = stmt.executeQuery(query);
         while (res.next()) {
        	 LOG.info(String.format("id: %s, %s", res.getInt("compound_id"), res.getString("structure")));
         }   
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDataSetTitle(int taxonId) {
        return DATASET_TITLE;
    }
}
