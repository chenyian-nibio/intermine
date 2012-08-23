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

import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.model.bio.Publication;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.query.ConstraintOp;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.SimpleConstraint;
import org.intermine.sql.Database;
import org.intermine.xml.full.Item;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 
 * @author
 */
public class RetrievePublicationLocalConverter extends BioDBConverter
{
    // 
    private static final String DATASET_TITLE = "Add DataSet.title here";
    private static final String DATA_SOURCE_NAME = "Add DataSource.name here";


	private String osAlias;

	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

   /**
     * Construct a new RetrievePublicationLocalConverter.
     * @param database the database to read from
     * @param model the Model used by the object store we will write to with the ItemWriter
     * @param writer an ItemWriter used to handle Items created
     */
    public RetrievePublicationLocalConverter(Database database, Model model, ItemWriter writer) {
        super(database, model, writer, DATA_SOURCE_NAME, DATASET_TITLE);
    }


    /**
     * {@inheritDoc}
     */
    public void process() throws Exception {
    	// query publication
    	ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);
    	List<Publication> publications = getPublications(os);
    	
    	System.out.println(publications.size() + " publications found.");
    	
    	
    	
        // a database has been initialised from properties starting with db.retrieve-publication-local

        Connection connection = getDatabase().getConnection();

        // process data with direct SQL queries on the source database, for example:
        
        // Statement stmt = connection.createStatement();
        // String query = "select column from table;";
        // ResultSet res = stmt.executeQuery(query);
        // while (res.next()) {
        // }   
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDataSetTitle(int taxonId) {
        return DATASET_TITLE;
    }
    
    /**
     * Retrieve the publications to be updated
     * @param os The ObjectStore to read from
     * @return a List of publications
     */
    protected List<Publication> getPublications(ObjectStore os) {
        Query q = new Query();
        QueryClass qc = new QueryClass(Publication.class);
        q.addFrom(qc);
        q.addToSelect(qc);

        ConstraintSet cs = new ConstraintSet(ConstraintOp.OR);

        SimpleConstraint scTitle =
            new SimpleConstraint(new QueryField(qc, "title"), ConstraintOp.IS_NULL);
        cs.addConstraint(scTitle);

        SimpleConstraint scYear =
            new SimpleConstraint(new QueryField(qc, "year"), ConstraintOp.IS_NULL);
        cs.addConstraint(scYear);

        SimpleConstraint scFirstAuthor =
            new SimpleConstraint(new QueryField(qc, "firstAuthor"), ConstraintOp.IS_NULL);
        cs.addConstraint(scFirstAuthor);

        q.setConstraint(cs);

        @SuppressWarnings("unchecked") List<Publication> retval = (List<Publication>) ((List) os
                .executeSingleton(q));
        return retval;
    }

}
