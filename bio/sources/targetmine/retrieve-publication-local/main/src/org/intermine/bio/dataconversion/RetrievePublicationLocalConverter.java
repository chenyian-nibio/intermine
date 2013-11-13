package org.intermine.bio.dataconversion;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.DBConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.model.bio.Publication;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.query.ConstraintOp;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.SimpleConstraint;
import org.intermine.sql.Database;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class RetrievePublicationLocalConverter extends DBConverter {
	private static final Logger LOG = Logger.getLogger(RetrievePublicationLocalConverter.class);
	//
	private String osAlias;

	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

	/**
	 * Construct a new RetrievePublicationLocalConverter.
	 * 
	 * @param database
	 *            the database to read from
	 * @param model
	 *            the Model used by the object store we will write to with the ItemWriter
	 * @param writer
	 *            an ItemWriter used to handle Items created
	 */
	public RetrievePublicationLocalConverter(Database database, Model model, ItemWriter writer) {
		super(database, model, writer);
	}

	/**
	 * {@inheritDoc}
	 */
	public void process() throws Exception {
		// query publication
		ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);
		List<Publication> publications = getPublications(os);

		System.out.println(publications.size() + " publications found.");
		
		Iterator<Publication> iterator = publications.iterator();
		Set<String> pubmedIds = new HashSet<String>();
		while (iterator.hasNext()) {
			Publication publication = iterator.next();
			pubmedIds.add(publication.getPubMedId());
		}
		System.out.println(pubmedIds.size() + " identifiers found.");

		// a database has been initialised from properties starting with
		// db.retrieve-publication-local

		Connection connection = getDatabase().getConnection();

		// process data with direct SQL queries on the source database, for example:

		Statement stmt = connection.createStatement();
		String query = "select * from publication ";
		ResultSet res = stmt.executeQuery(query);
		int i = 0;
		while (res.next()) {
			String pubMedId = res.getString("pubmedid");
			if (pubmedIds.contains(pubMedId)) {
				String pages = res.getString("pages");
				String volume = res.getString("volume");
				String issue = res.getString("issue");
				String journal = res.getString("journal");
				String title = res.getString("title");
				String firstAuthor = res.getString("firstauthor");
				String year = String.valueOf(res.getInt("intermine_year"));
				
				if (StringUtils.isEmpty(title) || StringUtils.isEmpty(year) || StringUtils.isEmpty(firstAuthor)) {
					continue;
				}
				
				Item publication = createItem("Publication");
				publication.setAttribute("title", title);
				publication.setAttribute("firstAuthor", firstAuthor);
				publication.setAttribute("year", year);
				
				String queryAuthor = "select a.name "
						+ " from publication as p "
						+ " join authorspublications as ap on ap.publications= p.id "
						+ " join author as a on a.id=ap.authors "
						+ " where p.pubmedid = '" + pubMedId + "'";
				Statement stmtAuthor = connection.createStatement();
				ResultSet resAuthor = stmtAuthor.executeQuery(queryAuthor);
				while (resAuthor.next()) {
					publication.addToCollection("authors", getAuthor(resAuthor.getString("name")));
				}

				publication.setAttribute("pubMedId", pubMedId);
				if (!StringUtils.isEmpty(pages)){
					publication.setAttribute("pages", pages);
				}
				if (!StringUtils.isEmpty(volume)){
					publication.setAttribute("volume", volume);
				}
				if (!StringUtils.isEmpty(issue)){
					publication.setAttribute("issue", issue);
				}
				if (!StringUtils.isEmpty(journal)){
					publication.setAttribute("journal", journal);
				}
				store(publication);
				i++;
			}
		}
		System.out.println(String.format("%d publication objects were created.",i));
		System.out.println(String.format("%d author objects were created.",numAuthor));
	}
	
	private int numAuthor = 0;
	private Map<String, String> authorMap = new HashMap<String, String>();
	private String getAuthor(String name) throws ObjectStoreException {
		String ret = authorMap.get(name);
		if (ret == null) {
			Item item = createItem("Author");
			item.setAttribute("name", name);
			store(item);
			ret = item.getIdentifier();
			authorMap.put(name, ret);
			numAuthor++;
		}
		return ret;
	}

	/**
	 * Retrieve the publications to be updated
	 * 
	 * @param os
	 *            The ObjectStore to read from
	 * @return a List of publications
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected List<Publication> getPublications(ObjectStore os) {
		Query q = new Query();
		QueryClass qc = new QueryClass(Publication.class);
		q.addFrom(qc);
		q.addToSelect(qc);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.OR);

		SimpleConstraint scTitle = new SimpleConstraint(new QueryField(qc, "title"),
				ConstraintOp.IS_NULL);
		cs.addConstraint(scTitle);

		SimpleConstraint scYear = new SimpleConstraint(new QueryField(qc, "year"),
				ConstraintOp.IS_NULL);
		cs.addConstraint(scYear);

		SimpleConstraint scFirstAuthor = new SimpleConstraint(new QueryField(qc, "firstAuthor"),
				ConstraintOp.IS_NULL);
		cs.addConstraint(scFirstAuthor);

		q.setConstraint(cs);

		List<Publication> retval = (List<Publication>) ((List) os.executeSingleton(q));
		return retval;
	}

}
