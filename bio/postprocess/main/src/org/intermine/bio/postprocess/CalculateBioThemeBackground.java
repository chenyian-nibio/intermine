package org.intermine.bio.postprocess;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.model.bio.GOAnnotation;
import org.intermine.model.bio.GOEvidence;
import org.intermine.model.bio.GOEvidenceCode;
import org.intermine.model.bio.GOTerm;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.Organism;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.intermine.ObjectStoreWriterInterMineImpl;
import org.intermine.objectstore.query.BagConstraint;
import org.intermine.objectstore.query.ConstraintOp;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryCollectionReference;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.QueryObjectReference;
import org.intermine.objectstore.query.QueryValue;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.objectstore.query.SimpleConstraint;
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

	private Model model;

	private Map<Integer, InterMineObject> organismMap = new HashMap<Integer, InterMineObject>();

	public CalculateBioThemeBackground(ObjectStoreWriter osw) {
		this.osw = osw;
		model = Model.getInstanceByName("genomic");

		getOrganism(PROCESS_TAXONIDS);
	}

	private String getSqlQueryForGOClass(Integer taxonId, String namespace, boolean withIEA) {
		String sql = " select count(distinct(g.id)) " + " from gene as g "
				+ " join goannotation as goa on goa.subjectid = g.id "
				+ " join evidencegoannotation as egoa on egoa.goannotation = goa.id "
				+ " join goevidence as goe on goe.id = egoa.evidence "
				+ " join goevidencecode as goec on goec.id = goe.codeid "
				+ " join goterm as got on got.id=goa.ontologytermid "
				+ " join organism as org on org.id = g.organismid " + " where org.taxonId = "
				+ taxonId + " " + " and got.namespace = '" + namespace + "' "
				+ " and got.identifier not in ('GO:0008150','GO:0003674','GO:0005575') "
				+ " and goa.qualifier is null " + (withIEA ? "" : " and goec.code <> 'IEA' ");

		return sql;
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

	private String getSqlQueryForGOTestNumber(Integer taxonId, String namespace, boolean withIEA) {
		// String sqlQuery = " select count(distinct(pgot.identifier)) " + " from gene as g "
		// + " join goannotation as goa on goa.subjectid = g.id "
		// + " join evidencegoannotation as egoa on egoa.goannotation = goa.id "
		// + " join goevidence as goe on goe.id = egoa.evidence "
		// + " join goevidencecode as goec on goec.id = goe.codeid "
		// + " join goterm as got on got.id = goa.ontologytermid "
		// + " join ontologytermparents as otp on otp.ontologyterm = got.id"
		// + " join goterm as pgot on pgot.id = otp.parents "
		// + " join organism as org on org.id = g.organismid " + " where org.taxonId = "
		// + taxonId + " " + " and pgot.namespace = '" + namespace + "' "
		// + " and pgot.identifier not in ('GO:0008150','GO:0003674','GO:0005575') "
		// + " and goa.qualifier is null " + (withIEA ? "" : " and goec.code <> 'IEA' ");

		// this one is faster!
		String sqlQuery = " select count(distinct(pgot.id)) " + " from goterm as got "
				+ " join ontologytermparents as otp on otp.ontologyterm = got.id "
				+ " join goterm as pgot on pgot.id = otp.parents "
				+ " where pgot.identifier not in ('GO:0008150','GO:0003674','GO:0005575') "
				+ " and got.id in ( select got.id " + " from gene as g "
				+ " join goannotation as goa on goa.subjectid = g.id "
				+ " join evidencegoannotation as egoa on egoa.goannotation = goa.id "
				+ " join goevidence as goe on goe.id = egoa.evidence "
				+ " join goevidencecode as goec on goec.id = goe.codeid "
				+ " join goterm as got on got.id = goa.ontologytermid "
				+ " join organism as org on org.id = g.organismid " + " where org.taxonId = "
				+ taxonId + " " + " and got.namespace = '" + namespace + "' "
				+ " and goa.qualifier is null " + (withIEA ? "" : " and goec.code <> 'IEA' ")
				+ " ) ";

		return sqlQuery;
	}

	private String getSqlQueryForGOTestNumber(Integer taxonId, boolean withIEA) {
		// String sqlQuery = " select count(distinct(pgot.identifier)), pgot.namespace "
		// + " from gene as g " + " join goannotation as goa on goa.subjectid = g.id "
		// + " join evidencegoannotation as egoa on egoa.goannotation = goa.id "
		// + " join goevidence as goe on goe.id = egoa.evidence "
		// + " join goevidencecode as goec on goec.id = goe.codeid "
		// + " join goterm as got on got.id = goa.ontologytermid "
		// + " join ontologytermparents as otp on otp.ontologyterm = got.id "
		// + " join goterm as pgot on pgot.id = otp.parents "
		// + " join organism as org on org.id = g.organismid " + " where org.taxonId = "
		// + taxonId + " "
		// + " and pgot.identifier not in ('GO:0008150','GO:0003674','GO:0005575') "
		// + " and goa.qualifier is null  " + (withIEA ? "" : " and goec.code <> 'IEA' ")
		// + " group by pgot.namespace ";

		// this one is faster!
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

	public void calculateGOBackground() {
		if (osw instanceof ObjectStoreWriterInterMineImpl) {
			Database db = ((ObjectStoreWriterInterMineImpl) osw).getDatabase();
			Connection con;

			try {
				con = db.getConnection();
				// con.setAutoCommit(false);

				// maybe we can reuse the statement?
				Statement statement = con.createStatement();

				// these name spaces could be got by a SQL query
				List<String> nameSpaces = Arrays.asList("biological_process", "molecular_function",
						"cellular_component");

				osw.beginTransaction();

				for (Integer taxonId : PROCESS_TAXONIDS) {
					// String sqlQuery = "select pgot.identifier, count(distinct(g.id)) "
					// + " from gene as g "
					// + " join goannotation as goa on goa.subjectid = g.id "
					// + " join evidencegoannotation as egoa on egoa.goannotation = goa.id "
					// + " join goevidence as goe on goe.id = egoa.evidence "
					// + " join goevidencecode as goec on goec.id = goe.codeid "
					// + " join goterm as got on got.id = goa.ontologytermid "
					// + " join ontologytermparents as otp on otp.ontologyterm = got.id"
					// + " join goterm as pgot on pgot.id = otp.parents "
					// + " join organism as org on org.id = g.organismid "
					// + " where org.taxonId = '" + taxonId + "' "
					// + " and goa.qualifier is null " + " and goec.code <> 'IEA' "
					// + " group by pgot.identifier ";

					for (String nameSpace : nameSpaces) {

						String[] chars = nameSpace.split("_");
						String ns = "GO" + chars[0].substring(0, 1).toUpperCase()
								+ chars[1].substring(0, 1).toUpperCase();

						ResultSet resultSet = statement.executeQuery(getSqlQueryForGOTerm(taxonId,
								nameSpace, false));
						// int i = 1;
						while (resultSet.next()) {
							String id = resultSet.getString("identifier");
							int count = resultSet.getInt("count");
							// LOG.info(String.format("(%d) %s --> %d", i, id, count));

							// InterMineObject item = (InterMineObject) DynamicUtil
							// .simpleCreateObject(model
							// .getClassDescriptorByName("Statistics").getType());
							// item.setFieldValue("identifier", id);
							// // TODO define a proper tag here
							// item.setFieldValue("tag", ns + "_wo_IEA");
							// item.setFieldValue("number", Integer.valueOf(count));
							// item.setFieldValue("organism", organismMap.get(taxonId));

							osw.store(createStatisticsItem(id, ns + "_wo_IEA", count, taxonId));

							// i++;

						}
						// calculate N
						// ResultSet resultN = statement.executeQuery(getSqlQueryForGOClass(taxonId,
						// nameSpace, false));
						// resultN.next();
						// int count = resultN.getInt("count");
						// osw.store(createStatisticsItem("GOAnnotation N", ns + "_wo_IEA", count,
						// taxonId));
						// System.out.println(String.format("(%d) %s - %s: %d", taxonId,
						// "GOAnnotation class", ns + "_w_IEA", count));

						// calculate Test number
						// ResultSet resultTn = statement.executeQuery(getSqlQueryForGOTestNumber(
						// taxonId, nameSpace, false));
						// resultTn.next();
						// int testNumber = resultTn.getInt("count");
						// osw.store(createStatisticsItem("GOAnnotation test number", ns +
						// "_wo_IEA",
						// testNumber, taxonId));
					}

					// do same calculation for IEA
					for (String nameSpace : nameSpaces) {

						String[] chars = nameSpace.split("_");
						String ns = "GO" + chars[0].substring(0, 1).toUpperCase()
								+ chars[1].substring(0, 1).toUpperCase();

						ResultSet resultSet = statement.executeQuery(getSqlQueryForGOTerm(taxonId,
								nameSpace, true));
						while (resultSet.next()) {
							String id = resultSet.getString("identifier");
							int count = resultSet.getInt("count");

							// InterMineObject item = (InterMineObject) DynamicUtil
							// .simpleCreateObject(model
							// .getClassDescriptorByName("Statistics").getType());
							// item.setFieldValue("identifier", id);
							// item.setFieldValue("tag", ns + "_w_IEA");
							// item.setFieldValue("number", Integer.valueOf(count));
							// item.setFieldValue("organism", organismMap.get(taxonId));

							osw.store(createStatisticsItem(id, ns + "_w_IEA", count, taxonId));
						}
						// calculate N
						// ResultSet resultN = statement.executeQuery(getSqlQueryForGOClass(taxonId,
						// nameSpace, true));
						// resultN.next();
						// int count = resultN.getInt("count");
						// osw.store(createStatisticsItem("GOAnnotation N", ns + "_w_IEA", count,
						// taxonId));
						// System.out.println(String.format("(%d) %s - %s: %d", taxonId,
						// "GOAnnotation class", ns + "_w_IEA", count));

						// calculate Test number
						// ResultSet resultTn = statement.executeQuery(getSqlQueryForGOTestNumber(
						// taxonId, nameSpace, true));
						// resultTn.next();
						// int testNumber = resultTn.getInt("count");
						// osw.store(createStatisticsItem("GOAnnotation test number", ns + "_w_IEA",
						// testNumber, taxonId));
					}

					// calculate N
					ResultSet resultN = statement
							.executeQuery(getSqlQueryForGOClass(taxonId, false));
					while (resultN.next()) {
						int testNumber = resultN.getInt("count");
						String namespace = resultN.getString("namespace");
						if (StringUtils.isEmpty(namespace)) {
							continue;
						}
						String[] chars = namespace.split("_");
						String ns = "GO" + chars[0].substring(0, 1).toUpperCase()
								+ chars[1].substring(0, 1).toUpperCase();
						osw.store(createStatisticsItem("GOAnnotation N", ns + "_wo_IEA",
								testNumber, taxonId));
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
				con.close();

				// osw.abortTransaction();
				osw.commitTransaction();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ObjectStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			throw new RuntimeException("the ObjectStoreWriter is not an "
					+ "ObjectStoreWriterInterMineImpl");
		}
	}

	// private void processGoBackground(Integer taxonId, ResultSet result) throws SQLException,
	// ObjectStoreException {
	// while (result.next()) {
	// int testNumber = result.getInt("count");
	// String namespace = result.getString("namespace");
	// if (StringUtils.isEmpty(namespace)) {
	// continue;
	// }
	// String[] chars = namespace.split("_");
	// String ns = "GO" + chars[0].substring(0, 1).toUpperCase()
	// + chars[1].substring(0, 1).toUpperCase();
	// osw.store(createStatisticsItem("GOAnnotation N", ns + "_wo_IEA", testNumber,
	// taxonId));
	// }
	// }

	public void calculatePathwayBackground() {
		if (osw instanceof ObjectStoreWriterInterMineImpl) {
			Database db = ((ObjectStoreWriterInterMineImpl) osw).getDatabase();
			Connection con;

			try {
				con = db.getConnection();

				Statement statement = con.createStatement();

				// these data set names could be got by a SQL query
				List<String> dataSets = Arrays.asList("KEGG Pathway", "Reactome",
						"NCI Pathway Interaction Database");

				osw.beginTransaction();

				for (Integer taxonId : PROCESS_TAXONIDS) {

					for (String dataSetName : dataSets) {

						ResultSet resultSet = statement.executeQuery(getSqlQueryForPathwayTerm(
								taxonId, dataSetName));
						int i = 1;
						LOG.info(dataSetName + " (" + taxonId + "):");
						while (resultSet.next()) {
							String id = resultSet.getString("identifier");
							int count = resultSet.getInt("count");
							// LOG.info(String.format("(%d) %s --> %d", i, id, count));
							osw.store(createStatisticsItem(id, dataSetName, count, taxonId));
							i++;
						}

						// calculate N
						ResultSet resultN = statement.executeQuery(getSqlQueryForPathwayClass(
								taxonId, dataSetName));
						resultN.next();
						int count = resultN.getInt("count");
						osw.store(createStatisticsItem("Pathway N", dataSetName, count, taxonId));
						// System.out.println(String.format("(%d) %s - %s: %d", taxonId,
						// "Pathway class", dataSetName, count));
					}

					ResultSet resultN = statement.executeQuery(getSqlQueryForPathwayClass(taxonId,
							null));
					resultN.next();
					int count = resultN.getInt("count");
					osw.store(createStatisticsItem("Pathway N", "All", count, taxonId));
					// System.out.println(String.format("(%d) %s - %s: %d", taxonId,
					// "Pathway class",
					// "All", count));

					ResultSet result = statement
							.executeQuery(getSqlQueryForPathwayTestNumber(taxonId));
					int total = 0;
					while (result.next()) {
						int testNumber = result.getInt("count");
						String dataSetName = result.getString("name");
						if (StringUtils.isEmpty(dataSetName)) {
							continue;
						}
						osw.store(createStatisticsItem("Pathway test number", dataSetName, testNumber, taxonId));
						total += testNumber;
					}
					osw.store(createStatisticsItem("Pathway test number", "All", total, taxonId));
				}

				statement.close();
				con.close();

				// osw.abortTransaction();
				osw.commitTransaction();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ObjectStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			throw new RuntimeException("the ObjectStoreWriter is not an "
					+ "ObjectStoreWriterInterMineImpl");
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

	public void calculateGOBackgroundOld() {
		// getAllGoTerms("9606");
		// getAllGoTerms("10090");
		// getAllGoTerms("10116");

		// List<String> ids = Arrays.asList("GO:1901137","GO:0070201");
		// for (String termId : ids) {
		// int num = queryNumberOfAssociatedGOTerm("10090", termId);
		// LOG.info(String.format("%s --> %d", termId, num));
		// }

		// for (String termId : allGoIds) {
		// int num = queryNumberOfAssociatedGOTerm("9606", termId);
		// LOG.info(String.format("%s --> %d", termId, num));
		// }

		// Results results = queryGenesToGOTerm("10090", ids);
		LOG.info("Start querying genes to GO terms...");
		// Results results = queryGenesToGOTerm("10090");
		Set<String> allGoTerms = getAllGoTerms("10090");
		LOG.info("Finish querying genes to GO terms...");

		LOG.info("Start processing the results...");
		count = 1;
		Set<String> subset = new HashSet<String>();
		for (String goId : allGoTerms) {
			subset.add(goId);
			if (subset.size() == 1000) {
				processGOTermsBackground("10090", subset);
				subset.clear();
			}
		}
		if (subset.size() > 0) {
			processGOTermsBackground("10090", subset);
		}
		LOG.info("End processing the results...");

		// LOG.info("Start processing the results...");
		// Iterator<?> iterator = results.iterator();
		// Map<String,Set<String>> geneGoMap = new HashMap<String,Set<String>>();
		// while (iterator.hasNext()) {
		// ResultsRow<?> result = (ResultsRow<?>) iterator.next();
		// Gene gene = (Gene) result.get(0);
		// GOTerm goTerm = (GOTerm) result.get(1);
		// String termId = goTerm.getIdentifier();
		// Set<String> set = geneGoMap.get(termId);
		// if (set == null) {
		// geneGoMap.put(termId, new HashSet<String>());
		// }
		// geneGoMap.get(termId).add(gene.getPrimaryIdentifier());
		// }
		// LOG.info("End processing the results...");

		// LOG.info("Start printing the results...");
		// int i = 1;
		// for (String termId: geneGoMap.keySet()) {
		// Set<String> geneIds = geneGoMap.get(termId);
		// LOG.info(String.format("(%d) %s --> %d", i, termId, geneIds.size()));
		// LOG.info(termId + ": " + StringUtils.join(geneIds, ","));
		// i++;
		// }
		// LOG.info("End printing the results...");

	}

	private void processGOTermsBackground(String taxonId, Collection<String> termIds) {
		LOG.info("Start a new batch...");
		Results results = queryGenesToGOTerm(taxonId, termIds);
		Iterator<?> iterator = results.iterator();
		Map<String, Set<String>> geneGoMap = new HashMap<String, Set<String>>();
		while (iterator.hasNext()) {
			ResultsRow<?> result = (ResultsRow<?>) iterator.next();
			Gene gene = (Gene) result.get(0);
			GOTerm goTerm = (GOTerm) result.get(1);
			String termId = goTerm.getIdentifier();
			Set<String> set = geneGoMap.get(termId);
			if (set == null) {
				geneGoMap.put(termId, new HashSet<String>());
			}
			geneGoMap.get(termId).add(gene.getPrimaryIdentifier());
		}
		for (String termId : geneGoMap.keySet()) {
			Set<String> geneIds = geneGoMap.get(termId);
			LOG.info(String.format("(%d) %s --> %d", count, termId, geneIds.size()));
			// LOG.info(termId + ": " + StringUtils.join(geneIds, ","));
			count++;
		}

	}

	private int count = 1;

	private Set<String> getAllGoTerms(String taxonId) {
		System.out.println(String.format("Querying alll GO Terms ... (%s)", taxonId));
		Results results = queryAllGoTerms(taxonId);
		Iterator<?> iterator = results.iterator();
		Set<String> allGoIds = new HashSet<String>();
		while (iterator.hasNext()) {
			ResultsRow<?> result = (ResultsRow<?>) iterator.next();
			GOTerm goTerm = (GOTerm) result.get(0);
			allGoIds.add(goTerm.getIdentifier());
		}
		System.out.println(String.format("Number of GO Terms (%s): %d", taxonId, allGoIds.size()));

		return allGoIds;
	}

	private Results queryAllGoTerms() {
		Query q = new Query();
		QueryClass qcOntologyTerm = new QueryClass(GOTerm.class);

		q.addFrom(qcOntologyTerm);
		q.addToSelect(qcOntologyTerm);

		ObjectStore os = osw.getObjectStore();

		return os.execute(q);
	}

	private Results queryAllGoTerms(String taxonId) {
		Query q = new Query();
		QueryClass qcGene = new QueryClass(Gene.class);
		QueryClass qcAnnotation = new QueryClass(GOAnnotation.class);
		QueryClass qcEvidence = new QueryClass(GOEvidence.class);
		QueryClass qcCode = new QueryClass(GOEvidenceCode.class);
		QueryClass qcOntologyTerm = new QueryClass(GOTerm.class);
		QueryClass qcParentTerm = new QueryClass(GOTerm.class);
		QueryClass qcOrganism = new QueryClass(Organism.class);

		QueryField qfTaxonId = new QueryField(qcOrganism, "taxonId");
		QueryField qfEvidenceCode = new QueryField(qcCode, "code");
		QueryField qfQualifier = new QueryField(qcAnnotation, "qualifier");

		q.addFrom(qcGene);
		q.addFrom(qcAnnotation);
		q.addFrom(qcEvidence);
		q.addFrom(qcCode);
		q.addFrom(qcOntologyTerm);
		q.addFrom(qcParentTerm);
		q.addFrom(qcOrganism);
		q.addToSelect(qcParentTerm);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);
		QueryObjectReference qor1 = new QueryObjectReference(qcGene, "organism");
		cs.addConstraint(new ContainsConstraint(qor1, ConstraintOp.CONTAINS, qcOrganism));
		cs.addConstraint(new SimpleConstraint(qfTaxonId, ConstraintOp.EQUALS, new QueryValue(
				Integer.valueOf(taxonId))));
		QueryCollectionReference qcr1 = new QueryCollectionReference(qcGene, "goAnnotation");
		cs.addConstraint(new ContainsConstraint(qcr1, ConstraintOp.CONTAINS, qcAnnotation));
		QueryCollectionReference qcr2 = new QueryCollectionReference(qcAnnotation, "evidence");
		cs.addConstraint(new ContainsConstraint(qcr2, ConstraintOp.CONTAINS, qcEvidence));
		QueryObjectReference qor2 = new QueryObjectReference(qcEvidence, "code");
		cs.addConstraint(new ContainsConstraint(qor2, ConstraintOp.CONTAINS, qcCode));
		cs.addConstraint(new SimpleConstraint(qfEvidenceCode, ConstraintOp.NOT_EQUALS,
				new QueryValue("IEA")));
		cs.addConstraint(new SimpleConstraint(qfQualifier, ConstraintOp.IS_NULL));
		QueryObjectReference qor3 = new QueryObjectReference(qcAnnotation, "ontologyTerm");
		cs.addConstraint(new ContainsConstraint(qor3, ConstraintOp.CONTAINS, qcOntologyTerm));
		QueryCollectionReference qcr3 = new QueryCollectionReference(qcOntologyTerm, "parents");
		cs.addConstraint(new ContainsConstraint(qcr3, ConstraintOp.CONTAINS, qcParentTerm));

		q.setConstraint(cs);

		ObjectStore os = osw.getObjectStore();

		return os.execute(q);
	}

	private int queryNumberOfAssociatedGOTerm(String taxonId, String termId) {
		Query q = new Query();
		QueryClass qcGene = new QueryClass(Gene.class);
		QueryClass qcAnnotation = new QueryClass(GOAnnotation.class);
		QueryClass qcEvidence = new QueryClass(GOEvidence.class);
		QueryClass qcCode = new QueryClass(GOEvidenceCode.class);
		QueryClass qcOntologyTerm = new QueryClass(GOTerm.class);
		QueryClass qcParentTerm = new QueryClass(GOTerm.class);
		QueryClass qcOrganism = new QueryClass(Organism.class);

		QueryField qfTaxonId = new QueryField(qcOrganism, "taxonId");
		QueryField qfTermId = new QueryField(qcParentTerm, "identifier");
		QueryField qfEvidenceCode = new QueryField(qcCode, "code");
		QueryField qfQualifier = new QueryField(qcAnnotation, "qualifier");

		q.addFrom(qcGene);
		q.addFrom(qcAnnotation);
		q.addFrom(qcEvidence);
		q.addFrom(qcCode);
		q.addFrom(qcOntologyTerm);
		q.addFrom(qcParentTerm);
		q.addFrom(qcOrganism);
		q.addToSelect(qcGene);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);
		QueryObjectReference qor1 = new QueryObjectReference(qcGene, "organism");
		cs.addConstraint(new ContainsConstraint(qor1, ConstraintOp.CONTAINS, qcOrganism));
		cs.addConstraint(new SimpleConstraint(qfTaxonId, ConstraintOp.EQUALS, new QueryValue(
				Integer.valueOf(taxonId))));
		QueryCollectionReference qcr1 = new QueryCollectionReference(qcGene, "goAnnotation");
		cs.addConstraint(new ContainsConstraint(qcr1, ConstraintOp.CONTAINS, qcAnnotation));
		QueryCollectionReference qcr2 = new QueryCollectionReference(qcAnnotation, "evidence");
		cs.addConstraint(new ContainsConstraint(qcr2, ConstraintOp.CONTAINS, qcEvidence));
		QueryObjectReference qor2 = new QueryObjectReference(qcEvidence, "code");
		cs.addConstraint(new ContainsConstraint(qor2, ConstraintOp.CONTAINS, qcCode));
		cs.addConstraint(new SimpleConstraint(qfEvidenceCode, ConstraintOp.NOT_EQUALS,
				new QueryValue("IEA")));
		cs.addConstraint(new SimpleConstraint(qfQualifier, ConstraintOp.IS_NULL));
		QueryObjectReference qor3 = new QueryObjectReference(qcAnnotation, "ontologyTerm");
		cs.addConstraint(new ContainsConstraint(qor3, ConstraintOp.CONTAINS, qcOntologyTerm));
		QueryCollectionReference qcr3 = new QueryCollectionReference(qcOntologyTerm, "parents");
		cs.addConstraint(new ContainsConstraint(qcr3, ConstraintOp.CONTAINS, qcParentTerm));
		cs.addConstraint(new SimpleConstraint(qfTermId, ConstraintOp.EQUALS, new QueryValue(termId)));

		q.setConstraint(cs);

		ObjectStore os = osw.getObjectStore();

		Results results = os.execute(q);

		Iterator<?> iterator = results.iterator();
		Set<String> geneIds = new HashSet<String>();
		while (iterator.hasNext()) {
			ResultsRow<?> result = (ResultsRow<?>) iterator.next();
			Gene gene = (Gene) result.get(0);
			geneIds.add(gene.getPrimaryIdentifier());
		}
		LOG.info(termId + ": " + StringUtils.join(geneIds, ","));

		return results.size();
	}

	private Results queryGenesToGOTerm(String taxonId, Collection<String> termIds) {
		Query q = new Query();
		QueryClass qcGene = new QueryClass(Gene.class);
		QueryClass qcAnnotation = new QueryClass(GOAnnotation.class);
		QueryClass qcEvidence = new QueryClass(GOEvidence.class);
		QueryClass qcCode = new QueryClass(GOEvidenceCode.class);
		QueryClass qcOntologyTerm = new QueryClass(GOTerm.class);
		QueryClass qcParentTerm = new QueryClass(GOTerm.class);
		QueryClass qcOrganism = new QueryClass(Organism.class);

		QueryField qfTaxonId = new QueryField(qcOrganism, "taxonId");
		QueryField qfTermId = new QueryField(qcParentTerm, "identifier");
		QueryField qfEvidenceCode = new QueryField(qcCode, "code");
		QueryField qfQualifier = new QueryField(qcAnnotation, "qualifier");

		q.addFrom(qcGene);
		q.addFrom(qcAnnotation);
		q.addFrom(qcEvidence);
		q.addFrom(qcCode);
		q.addFrom(qcOntologyTerm);
		q.addFrom(qcParentTerm);
		q.addFrom(qcOrganism);
		q.addToSelect(qcGene);
		q.addToSelect(qcParentTerm);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);
		QueryObjectReference qor1 = new QueryObjectReference(qcGene, "organism");
		cs.addConstraint(new ContainsConstraint(qor1, ConstraintOp.CONTAINS, qcOrganism));
		cs.addConstraint(new SimpleConstraint(qfTaxonId, ConstraintOp.EQUALS, new QueryValue(
				Integer.valueOf(taxonId))));
		QueryCollectionReference qcr1 = new QueryCollectionReference(qcGene, "goAnnotation");
		cs.addConstraint(new ContainsConstraint(qcr1, ConstraintOp.CONTAINS, qcAnnotation));
		QueryCollectionReference qcr2 = new QueryCollectionReference(qcAnnotation, "evidence");
		cs.addConstraint(new ContainsConstraint(qcr2, ConstraintOp.CONTAINS, qcEvidence));
		QueryObjectReference qor2 = new QueryObjectReference(qcEvidence, "code");
		cs.addConstraint(new ContainsConstraint(qor2, ConstraintOp.CONTAINS, qcCode));
		cs.addConstraint(new SimpleConstraint(qfEvidenceCode, ConstraintOp.NOT_EQUALS,
				new QueryValue("IEA")));
		cs.addConstraint(new SimpleConstraint(qfQualifier, ConstraintOp.IS_NULL));
		QueryObjectReference qor3 = new QueryObjectReference(qcAnnotation, "ontologyTerm");
		cs.addConstraint(new ContainsConstraint(qor3, ConstraintOp.CONTAINS, qcOntologyTerm));
		QueryCollectionReference qcr3 = new QueryCollectionReference(qcOntologyTerm, "parents");
		cs.addConstraint(new ContainsConstraint(qcr3, ConstraintOp.CONTAINS, qcParentTerm));
		cs.addConstraint(new BagConstraint(qfTermId, ConstraintOp.IN, termIds));

		q.setConstraint(cs);

		ObjectStore os = osw.getObjectStore();

		return os.execute(q);
	}

	private Results queryGenesToGOTerm(String taxonId) {
		Query q = new Query();
		QueryClass qcGene = new QueryClass(Gene.class);
		QueryClass qcAnnotation = new QueryClass(GOAnnotation.class);
		QueryClass qcEvidence = new QueryClass(GOEvidence.class);
		QueryClass qcCode = new QueryClass(GOEvidenceCode.class);
		QueryClass qcOntologyTerm = new QueryClass(GOTerm.class);
		QueryClass qcParentTerm = new QueryClass(GOTerm.class);
		QueryClass qcOrganism = new QueryClass(Organism.class);

		QueryField qfTaxonId = new QueryField(qcOrganism, "taxonId");
		QueryField qfEvidenceCode = new QueryField(qcCode, "code");
		QueryField qfQualifier = new QueryField(qcAnnotation, "qualifier");

		q.addFrom(qcGene);
		q.addFrom(qcAnnotation);
		q.addFrom(qcEvidence);
		q.addFrom(qcCode);
		q.addFrom(qcOntologyTerm);
		q.addFrom(qcParentTerm);
		q.addFrom(qcOrganism);
		q.addToSelect(qcGene);
		q.addToSelect(qcParentTerm);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);
		QueryObjectReference qor1 = new QueryObjectReference(qcGene, "organism");
		cs.addConstraint(new ContainsConstraint(qor1, ConstraintOp.CONTAINS, qcOrganism));
		cs.addConstraint(new SimpleConstraint(qfTaxonId, ConstraintOp.EQUALS, new QueryValue(
				Integer.valueOf(taxonId))));
		QueryCollectionReference qcr1 = new QueryCollectionReference(qcGene, "goAnnotation");
		cs.addConstraint(new ContainsConstraint(qcr1, ConstraintOp.CONTAINS, qcAnnotation));
		QueryCollectionReference qcr2 = new QueryCollectionReference(qcAnnotation, "evidence");
		cs.addConstraint(new ContainsConstraint(qcr2, ConstraintOp.CONTAINS, qcEvidence));
		QueryObjectReference qor2 = new QueryObjectReference(qcEvidence, "code");
		cs.addConstraint(new ContainsConstraint(qor2, ConstraintOp.CONTAINS, qcCode));
		cs.addConstraint(new SimpleConstraint(qfEvidenceCode, ConstraintOp.NOT_EQUALS,
				new QueryValue("IEA")));
		cs.addConstraint(new SimpleConstraint(qfQualifier, ConstraintOp.IS_NULL));
		QueryObjectReference qor3 = new QueryObjectReference(qcAnnotation, "ontologyTerm");
		cs.addConstraint(new ContainsConstraint(qor3, ConstraintOp.CONTAINS, qcOntologyTerm));
		QueryCollectionReference qcr3 = new QueryCollectionReference(qcOntologyTerm, "parents");
		cs.addConstraint(new ContainsConstraint(qcr3, ConstraintOp.CONTAINS, qcParentTerm));

		q.setConstraint(cs);

		ObjectStore os = osw.getObjectStore();

		return os.execute(q);
	}

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

}
