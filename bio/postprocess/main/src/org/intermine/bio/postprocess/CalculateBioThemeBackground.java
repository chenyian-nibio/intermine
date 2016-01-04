package org.intermine.bio.postprocess;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.metadata.ConstraintOp;
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.model.bio.Organism;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.intermine.ObjectStoreWriterInterMineImpl;
import org.intermine.objectstore.query.BagConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.sql.Database;
import org.intermine.util.DynamicUtil;

/**
 * Be sure all biological themes are all integrated, including post-processing
 * 
 * @author chenyian
 * 
 */
public class CalculateBioThemeBackground {
	private static final Logger LOG = Logger.getLogger(CalculateBioThemeBackground.class);

	private static final List<Integer> PROCESS_TAXONIDS = Arrays.asList(9606, 10090, 10116);

	protected ObjectStoreWriter osw;

	protected Connection connection;

	private Model model;

	private Map<Integer, InterMineObject> organismMap = new HashMap<Integer, InterMineObject>();

	public CalculateBioThemeBackground(ObjectStoreWriter osw) {
		this.osw = osw;
		model = Model.getInstanceByName("genomic");

		getOrganism(PROCESS_TAXONIDS);

		if (osw instanceof ObjectStoreWriterInterMineImpl) {
			Database db = ((ObjectStoreWriterInterMineImpl) osw).getDatabase();
			try {
				connection = db.getConnection();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException("Unable to get a DB connection.");
			}
		} else {
			throw new RuntimeException("the ObjectStoreWriter is not an "
					+ "ObjectStoreWriterInterMineImpl");
		}
	}

	public void calculateGOBackground() {
		System.out.println("calculating GO Background...");
		try {
			// maybe we can reuse the statement?
			Statement statement = connection.createStatement();

			// these name spaces could be got by a SQL query
			List<String> nameSpaces = Arrays.asList("biological_process", "molecular_function",
					"cellular_component");

			osw.beginTransaction();

			for (Integer taxonId : PROCESS_TAXONIDS) {

				for (String nameSpace : nameSpaces) {

					String[] chars = nameSpace.split("_");
					String ns = "GO" + chars[0].substring(0, 1).toUpperCase()
							+ chars[1].substring(0, 1).toUpperCase();

					ResultSet resultSet = statement.executeQuery(getSqlQueryForGOTerm(taxonId,
							nameSpace, false));
					while (resultSet.next()) {
						String id = resultSet.getString("identifier");
						int count = resultSet.getInt("count");
						// LOG.info(String.format("(%d) %s --> %d", i, id, count));

						osw.store(createStatisticsItem(id, ns + "_wo_IEA", count, taxonId));

					}
				}

				// do the same calculation for IEA
				for (String nameSpace : nameSpaces) {

					String[] chars = nameSpace.split("_");
					String ns = "GO" + chars[0].substring(0, 1).toUpperCase()
							+ chars[1].substring(0, 1).toUpperCase();

					ResultSet resultSet = statement.executeQuery(getSqlQueryForGOTerm(taxonId,
							nameSpace, true));
					while (resultSet.next()) {
						String id = resultSet.getString("identifier");
						int count = resultSet.getInt("count");

						osw.store(createStatisticsItem(id, ns + "_w_IEA", count, taxonId));
					}
				}

				// calculate N
				ResultSet resultN = statement.executeQuery(getSqlQueryForGOClass(taxonId, false));
				while (resultN.next()) {
					int testNumber = resultN.getInt("count");
					String namespace = resultN.getString("namespace");
					if (StringUtils.isEmpty(namespace)) {
						continue;
					}
					String[] chars = namespace.split("_");
					String ns = "GO" + chars[0].substring(0, 1).toUpperCase()
							+ chars[1].substring(0, 1).toUpperCase();
					osw.store(createStatisticsItem("GOAnnotation N", ns + "_wo_IEA", testNumber,
							taxonId));
				}
				resultN = statement.executeQuery(getSqlQueryForGOClass(taxonId, true));
				while (resultN.next()) {
					int testNumber = resultN.getInt("count");
					String namespace = resultN.getString("namespace");
					if (StringUtils.isEmpty(namespace)) {
						continue;
					}
					String[] chars = namespace.split("_");
					String ns = "GO" + chars[0].substring(0, 1).toUpperCase()
							+ chars[1].substring(0, 1).toUpperCase();
					osw.store(createStatisticsItem("GOAnnotation N", ns + "_w_IEA", testNumber,
							taxonId));
				}

				// calculate Test number
				ResultSet resultTn = statement.executeQuery(getSqlQueryForGOTestNumber(taxonId,
						false));
				while (resultTn.next()) {
					int testNumber = resultTn.getInt("count");
					String[] chars = resultTn.getString("namespace").split("_");
					String ns = "GO" + chars[0].substring(0, 1).toUpperCase()
							+ chars[1].substring(0, 1).toUpperCase();
					osw.store(createStatisticsItem("GOAnnotation test number", ns + "_wo_IEA",
							testNumber, taxonId));
				}
				resultTn = statement.executeQuery(getSqlQueryForGOTestNumber(taxonId, true));
				while (resultTn.next()) {
					int testNumber = resultTn.getInt("count");
					String nameSpace = resultTn.getString("namespace");
					String[] chars = nameSpace.split("_");
					String ns = "GO" + chars[0].substring(0, 1).toUpperCase()
							+ chars[1].substring(0, 1).toUpperCase();
					osw.store(createStatisticsItem("GOAnnotation test number", ns + "_w_IEA",
							testNumber, taxonId));
				}

			}

			statement.close();

			// osw.abortTransaction();
			osw.commitTransaction();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ObjectStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getSqlQueryForGOClass(Integer taxonId, boolean withIEA) {
		String sql = " select count(distinct(g.id)), got.namespace " + " from gene as g "
				+ " join goannotation as goa on goa.subjectid = g.id "
				+ " join evidencegoannotation as egoa on egoa.goannotation = goa.id "
				+ " join goevidence as goe on goe.id = egoa.evidence "
				+ " join goevidencecode as goec on goec.id = goe.codeid "
				+ " join goterm as got on got.id=goa.ontologytermid "
				+ " join organism as org on org.id = g.organismid " + " where org.taxonId = "
				+ taxonId + " "
				+ " and got.identifier not in ('GO:0008150','GO:0003674','GO:0005575') "
				+ (withIEA ? "" : " and goec.code <> 'IEA' ") + " and goa.qualifier is null "
				+ " group by got.namespace ";

		return sql;
	}

	private String getSqlQueryForGOTerm(Integer taxonId, String namespace, boolean withIEA) {
		String sqlQuery = " select pgot.identifier, count(distinct(g.id)) " + " from gene as g "
				+ " join goannotation as goa on goa.subjectid = g.id "
				+ " join evidencegoannotation as egoa on egoa.goannotation = goa.id "
				+ " join goevidence as goe on goe.id = egoa.evidence "
				+ " join goevidencecode as goec on goec.id = goe.codeid "
				+ " join goterm as got on got.id = goa.ontologytermid "
				+ " join ontologytermparents as otp on otp.ontologyterm = got.id"
				+ " join goterm as pgot on pgot.id = otp.parents "
				+ " join organism as org on org.id = g.organismid " + " where org.taxonId = "
				+ taxonId + " " + " and pgot.namespace = '" + namespace + "' "
				+ " and pgot.identifier not in ('GO:0008150','GO:0003674','GO:0005575') "
				+ " and goa.qualifier is null " + (withIEA ? "" : " and goec.code <> 'IEA' ")
				+ " group by pgot.identifier ";

		return sqlQuery;
	}

	private String getSqlQueryForGOTestNumber(Integer taxonId, boolean withIEA) {
		String sqlQuery = " select count(distinct(pgot.id)), pgot.namespace "
				+ " from goterm as got "
				+ " join ontologytermparents as otp on otp.ontologyterm = got.id "
				+ " join goterm as pgot on pgot.id = otp.parents "
				+ " where got.id in ( select got.id " + " from gene as g "
				+ " join goannotation as goa on goa.subjectid = g.id "
				+ " join evidencegoannotation as egoa on egoa.goannotation = goa.id "
				+ " join goevidence as goe on goe.id = egoa.evidence "
				+ " join goevidencecode as goec on goec.id = goe.codeid "
				+ " join goterm as got on got.id = goa.ontologytermid "
				+ " join organism as org on org.id = g.organismid " + " where org.taxonId = "
				+ taxonId + " " + " and goa.qualifier is null  "
				+ (withIEA ? "" : " and goec.code <> 'IEA' ")
				+ " ) and pgot.identifier not in ('GO:0008150','GO:0003674','GO:0005575') "
				+ " group by pgot.namespace ";

		return sqlQuery;
	}

	public void calculatePathwayBackground() {
		System.out.println("calculating Pathway Background...");
		try {
			Statement statement = connection.createStatement();

			// these data set names could be got by a SQL query
			List<String> dataSets = Arrays.asList("KEGG Pathway", "Reactome",
					"NCI Pathway Interaction Database");

			osw.beginTransaction();

			for (Integer taxonId : PROCESS_TAXONIDS) {

				for (String dataSetName : dataSets) {

					ResultSet resultSet = statement.executeQuery(getSqlQueryForPathwayTerm(taxonId,
							dataSetName));
					LOG.info(dataSetName + " (" + taxonId + "):");
					while (resultSet.next()) {
						String id = resultSet.getString("identifier");
						int count = resultSet.getInt("count");
						osw.store(createStatisticsItem(id, dataSetName, count, taxonId));
					}

					// calculate N
					ResultSet resultN = statement.executeQuery(getSqlQueryForPathwayClass(taxonId,
							dataSetName));
					resultN.next();
					int count = resultN.getInt("count");
					osw.store(createStatisticsItem("Pathway N", dataSetName, count, taxonId));
					// System.out.println(String.format("(%d) %s - %s: %d", taxonId,
					// "Pathway class", dataSetName, count));
				}

				ResultSet resultN = statement
						.executeQuery(getSqlQueryForPathwayClass(taxonId, null));
				resultN.next();
				int count = resultN.getInt("count");
				osw.store(createStatisticsItem("Pathway N", "All", count, taxonId));
				// System.out.println(String.format("(%d) %s - %s: %d", taxonId,
				// "Pathway class",
				// "All", count));

				ResultSet result = statement.executeQuery(getSqlQueryForPathwayTestNumber(taxonId));
				int total = 0;
				while (result.next()) {
					int testNumber = result.getInt("count");
					String dataSetName = result.getString("name");
					if (StringUtils.isEmpty(dataSetName)) {
						continue;
					}
					osw.store(createStatisticsItem("Pathway test number", dataSetName, testNumber,
							taxonId));
					total += testNumber;
				}
				osw.store(createStatisticsItem("Pathway test number", "All", total, taxonId));
			}

			statement.close();

			// osw.abortTransaction();
			osw.commitTransaction();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ObjectStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private InterMineObject createStatisticsItem(String identifier, String tag, int count,
			Integer taxonId) {
		InterMineObject item = (InterMineObject) DynamicUtil.simpleCreateObject(model
				.getClassDescriptorByName("Statistics").getType());
		item.setFieldValue("identifier", identifier);
		item.setFieldValue("tag", tag);
		item.setFieldValue("number", Integer.valueOf(count));
		item.setFieldValue("organism", organismMap.get(taxonId));

		return item;
	}

	private String getSqlQueryForPathwayTerm(Integer taxonId, String dataSetName) {
		String sqlQuery = "select p.identifier, count(g.id)" + " from genespathways as gp "
				+ " join pathway as p on p.id=gp.pathways " + " join gene as g on g.id=genes "
				+ " join datasetspathway as dsp on dsp.pathway = p.id "
				+ " join dataset as ds on ds.id = dsp.datasets "
				+ " join organism as org on org.id = g.organismid " + " where org.taxonId = "
				+ taxonId + " " + " and ds.name = '" + dataSetName + "'"
				+ " group by p.identifier ";

		return sqlQuery;
	}

	private String getSqlQueryForPathwayClass(Integer taxonId, String dataSetName) {
		String sql = " select count(distinct(g.id)) " + " from genespathways as gp "
				+ " join pathway as p on p.id=gp.pathways " + " join gene as g on g.id=genes "
				+ " join datasetspathway as dsp on dsp.pathway = p.id "
				+ " join dataset as ds on ds.id = dsp.datasets "
				+ " join organism as org on org.id = g.organismid " + " where org.taxonId = "
				+ taxonId + " ";
		if (dataSetName != null) {
			sql += " and ds.name like '" + dataSetName + "'";
		}
		return sql;
	}

	private String getSqlQueryForPathwayTestNumber(Integer taxonId) {
		String sql = " select count(p.id), ds.name " + " from pathway as p "
				+ " join datasetspathway as dsp on dsp.pathway = p.id "
				+ " join dataset as ds on ds.id = dsp.datasets "
				+ " join organism as org on org.id = p.organismid " + " where org.taxonId = "
				+ taxonId + " " + " group by ds.name ";
		return sql;
	}

	public void calculateGOSlimBackground() {
		System.out.println("calculating GOSlim Background...");
		try {
			// maybe we can reuse the statement?
			Statement statement = connection.createStatement();

			// these name spaces could be got by a SQL query
			List<String> nameSpaces = Arrays.asList("biological_process", "molecular_function",
					"cellular_component");

			osw.beginTransaction();

			for (Integer taxonId : PROCESS_TAXONIDS) {

				for (String nameSpace : nameSpaces) {

					String[] chars = nameSpace.split("_");
					String ns = "GOS" + chars[0].substring(0, 1).toUpperCase()
							+ chars[1].substring(0, 1).toUpperCase();

					ResultSet resultSet = statement.executeQuery(getSqlQueryForGOSlimTerm(taxonId,
							nameSpace, false));
					while (resultSet.next()) {
						String id = resultSet.getString("identifier");
						int count = resultSet.getInt("count");
						// LOG.info(String.format("(%d) %s --> %d", i, id, count));

						osw.store(createStatisticsItem(id, ns + "_wo_IEA", count, taxonId));

					}
				}

				// do the same calculation for IEA
				for (String nameSpace : nameSpaces) {

					String[] chars = nameSpace.split("_");
					String ns = "GOS" + chars[0].substring(0, 1).toUpperCase()
							+ chars[1].substring(0, 1).toUpperCase();

					ResultSet resultSet = statement.executeQuery(getSqlQueryForGOSlimTerm(taxonId,
							nameSpace, true));
					while (resultSet.next()) {
						String id = resultSet.getString("identifier");
						int count = resultSet.getInt("count");

						osw.store(createStatisticsItem(id, ns + "_w_IEA", count, taxonId));
					}
				}

				// calculate N
				ResultSet resultN = statement
						.executeQuery(getSqlQueryForGOSlimClass(taxonId, false));
				while (resultN.next()) {
					int testNumber = resultN.getInt("count");
					String namespace = resultN.getString("namespace");
					if (StringUtils.isEmpty(namespace)) {
						continue;
					}
					String[] chars = namespace.split("_");
					String ns = "GOS" + chars[0].substring(0, 1).toUpperCase()
							+ chars[1].substring(0, 1).toUpperCase();
					osw.store(createStatisticsItem("GOSlim N", ns + "_wo_IEA", testNumber, taxonId));
				}
				resultN = statement.executeQuery(getSqlQueryForGOSlimClass(taxonId, true));
				while (resultN.next()) {
					int testNumber = resultN.getInt("count");
					String namespace = resultN.getString("namespace");
					if (StringUtils.isEmpty(namespace)) {
						continue;
					}
					String[] chars = namespace.split("_");
					String ns = "GOS" + chars[0].substring(0, 1).toUpperCase()
							+ chars[1].substring(0, 1).toUpperCase();
					osw.store(createStatisticsItem("GOSlim N", ns + "_w_IEA", testNumber, taxonId));
				}

				// calculate Test number
				ResultSet resultTn = statement.executeQuery(getSqlQueryForGOSlimTestNumber(taxonId,
						false));
				while (resultTn.next()) {
					int testNumber = resultTn.getInt("count");
					String[] chars = resultTn.getString("namespace").split("_");
					String ns = "GOS" + chars[0].substring(0, 1).toUpperCase()
							+ chars[1].substring(0, 1).toUpperCase();
					osw.store(createStatisticsItem("GOSlim test number", ns + "_wo_IEA",
							testNumber, taxonId));
				}
				resultTn = statement.executeQuery(getSqlQueryForGOSlimTestNumber(taxonId, true));
				while (resultTn.next()) {
					int testNumber = resultTn.getInt("count");
					String nameSpace = resultTn.getString("namespace");
					String[] chars = nameSpace.split("_");
					String ns = "GOS" + chars[0].substring(0, 1).toUpperCase()
							+ chars[1].substring(0, 1).toUpperCase();
					osw.store(createStatisticsItem("GOSlim test number", ns + "_w_IEA", testNumber,
							taxonId));
				}

			}

			statement.close();

			// osw.abortTransaction();
			osw.commitTransaction();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ObjectStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getSqlQueryForGOSlimClass(Integer taxonId, boolean withIEA) {
		String sql = " select count(distinct(g.id)), gost.namespace " + " from gene as g "
				+ " join goannotation as goa on goa.subjectid = g.id "
				+ " join evidencegoannotation as egoa on egoa.goannotation = goa.id "
				+ " join goevidence as goe on goe.id = egoa.evidence "
				+ " join goevidencecode as goec on goec.id = goe.codeid "
				+ " join goannotationgoslimterms as gogos on gogos.goannotation = goa.id "
				+ " join goslimterm as gost on gost.id = gogos.goslimterms "
				+ " join organism as org on org.id = g.organismid " + " where org.taxonId = "
				+ taxonId + " "
				+ " and gost.identifier not in ('GO:0008150','GO:0003674','GO:0005575') "
				+ (withIEA ? "" : " and goec.code <> 'IEA' ") + " and goa.qualifier is null "
				+ " group by gost.namespace ";

		return sql;
	}

	private String getSqlQueryForGOSlimTerm(Integer taxonId, String namespace, boolean withIEA) {
		String sqlQuery = " select pgost.identifier, count(distinct(g.id)) " + " from gene as g "
				+ " join goannotation as goa on goa.subjectid = g.id "
				+ " join evidencegoannotation as egoa on egoa.goannotation = goa.id "
				+ " join goevidence as goe on goe.id = egoa.evidence "
				+ " join goevidencecode as goec on goec.id = goe.codeid "
				+ " join goannotationgoslimterms as gogos on gogos.goannotation = goa.id "
				+ " join goslimterm as gost on gost.id = gogos.goslimterms "
				+ " join ontologytermparents as otp on otp.ontologyterm = gost.id "
				+ " join goslimterm as pgost on pgost.id = otp.parents "
				+ " join organism as org on org.id = g.organismid " + " where org.taxonId = "
				+ taxonId + " " + " and pgost.namespace = '" + namespace + "' "
				+ " and pgost.identifier not in ('GO:0008150','GO:0003674','GO:0005575') "
				+ " and goa.qualifier is null " + (withIEA ? "" : " and goec.code <> 'IEA' ")
				+ " group by pgost.identifier ";

		return sqlQuery;
	}

	private String getSqlQueryForGOSlimTestNumber(Integer taxonId, boolean withIEA) {
		String sqlQuery = " select count(distinct(pgost.id)), pgost.namespace "
				+ " from goslimterm as gost "
				+ " join ontologytermparents as otp on otp.ontologyterm = gost.id "
				+ " join goslimterm as pgost on pgost.id = otp.parents " + " where gost.id in ( "
				+ " select gost.id " + " from gene as g "
				+ " join goannotation as goa on goa.subjectid = g.id "
				+ " join evidencegoannotation as egoa on egoa.goannotation = goa.id "
				+ " join goevidence as goe on goe.id = egoa.evidence "
				+ " join goevidencecode as goec on goec.id = goe.codeid "
				+ " join goannotationgoslimterms as gogos on gogos.goannotation = goa.id "
				+ " join goslimterm as gost on gost.id = gogos.goslimterms "
				+ " join organism as org on org.id = g.organismid " + " where org.taxonId = "
				+ taxonId + " " + " and goa.qualifier is null "
				+ (withIEA ? "" : " and goec.code <> 'IEA' ")
				+ " ) and pgost.identifier not in ('GO:0008150','GO:0003674','GO:0005575') "
				+ " group by pgost.namespace ";

		return sqlQuery;
	}

	/**
	 * 
	 * @param taxonIds
	 */
	private void getOrganism(Collection<Integer> taxonIds) {
		Query q = new Query();
		QueryClass qcOrganism = new QueryClass(Organism.class);
		QueryField qfTaxonId = new QueryField(qcOrganism, "taxonId");

		q.addFrom(qcOrganism);
		q.addToSelect(qcOrganism);

		q.setConstraint(new BagConstraint(qfTaxonId, ConstraintOp.IN, taxonIds));

		ObjectStore os = osw.getObjectStore();
		Results results = os.execute(q);

		Iterator<?> iterator = results.iterator();
		while (iterator.hasNext()) {
			ResultsRow<?> result = (ResultsRow<?>) iterator.next();
			Organism organism = (Organism) result.get(0);
			organismMap.put(organism.getTaxonId(), organism);
			// System.out.println(String.format("%s (%d) ID: %d", organism.getShortName(),
			// organism.getTaxonId(), organism.getId()));
		}

	}

	public void closeDbConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to close the DB connection.");
		}
	}

}
