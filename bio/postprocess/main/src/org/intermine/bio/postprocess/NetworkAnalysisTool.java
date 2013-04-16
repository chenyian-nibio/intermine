package org.intermine.bio.postprocess;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.Interaction;
import org.intermine.model.bio.Organism;
import org.intermine.model.bio.Pathway;
import org.intermine.model.bio.Publication;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
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
import org.intermine.util.DynamicUtil;

import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.filters.FilterUtils;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;

public class NetworkAnalysisTool {
	private static final Logger LOG = Logger.getLogger(NetworkAnalysisTool.class);

	// the post used by PostgreSQL
	private static final String PORT = "5432";

	protected ObjectStoreWriter osw;
	protected ObjectStore os;

	private Model model;

	public NetworkAnalysisTool(ObjectStoreWriter osw) {
		this.osw = osw;
		model = Model.getInstanceByName("genomic");
	}

	public void doAnalysis() {
		LOG.info("Start to do network analysis......");
		System.out.println("Start to do network analysis......");
		readDbConfig();
		readDirectMiTerm();

		// String taxonId = "9606";
		List<String> species = Arrays.asList("9606", "10090", "10116");
		for (String taxonId : species) {

			Collection<InteractionData> interactions = getPhysicalInteractions(taxonId);

			Iterator<InteractionData> iterator = interactions.iterator();
			Set<InteractionData> hcppi = new HashSet<InteractionData>();
			// TODO distinguish HC, HCDP
			while (iterator.hasNext()) {
				InteractionData intData = iterator.next();
				if (intData.isHighConfidentDirect()) {
					hcppi.add(intData);
				}
			}
			System.out.println("HCPPI contains " + hcppi.size() + " interactions (" + taxonId
					+ ").");

			Graph<String, String> lcc = findLargestConnectiveNetwork(hcppi);
			System.out.println("LCC contains " + lcc.getEdgeCount() + " interactions (" + taxonId
					+ ").");

			Results intResults = queryInteractionByTaxonId(taxonId);
			System.out.println("There are " + intResults.size() + " interactions (" + taxonId
					+ ").");

			Set<String> hcppiPairs = getHcppiPairs(hcppi);
			Set<String> lccPairs = getLccPairs(lcc);

			Iterator<?> resIter = intResults.iterator();
			try {
				osw.beginTransaction();
				int i = 0;
				while (resIter.hasNext()) {
					ResultsRow<?> rr = (ResultsRow<?>) resIter.next();
					Interaction interaction = (Interaction) rr.get(0);
					Gene gene1 = (Gene) rr.get(1);
					Gene gene2 = (Gene) rr.get(2);
					String gene1Id = gene1.getNcbiGeneId();
					String gene2Id = gene2.getNcbiGeneId();
					if (hcppiPairs.contains(gene1Id + "-" + gene2Id)
							|| hcppiPairs.contains(gene2Id + "-" + gene1Id)) {
						interaction.setFieldValue("isHcppi", Boolean.TRUE);
						i++;
					} else {
						interaction.setFieldValue("isHcppi", Boolean.FALSE);
					}
					if (lccPairs.contains(gene1Id + "-" + gene2Id)
							|| lccPairs.contains(gene2Id + "-" + gene1Id)) {
						interaction.setFieldValue("isLcc", Boolean.TRUE);
						i++;
					} else {
						interaction.setFieldValue("isLcc", Boolean.FALSE);
					}
					osw.store(interaction);
				}
				System.out.println("There are " + i + " HCPPI interactions pairs. (" + taxonId
						+ ").");
				osw.commitTransaction();

				Map<String, Double> betweennessCentrality = calcBetweennessCentrality(lcc);
				System.out.println("Betweenness Centrality has been calculated (" + taxonId + ").");

				Collection<String> vertices = lcc.getVertices();
				Set<String> geneIds = new HashSet<String>();
				for (String id : vertices) {
					geneIds.add(id);
				}
				System.out
						.println("Querying by " + geneIds.size() + " gene IDs (" + taxonId + ").");
				Results geneResults = queryGenesByGeneIdList(geneIds);
				System.out.println("Retrieve " + geneResults.size() + " genes (" + taxonId + ").");

				osw.beginTransaction();

				Iterator<?> geneIter = geneResults.iterator();
				while (geneIter.hasNext()) {
					ResultsRow<?> rr = (ResultsRow<?>) geneIter.next();
					Gene gene = (Gene) rr.get(0);
					String geneId = gene.getNcbiGeneId();
					if (betweennessCentrality.get(geneId) != null) {
						InterMineObject item = (InterMineObject) DynamicUtil
								.simpleCreateObject(model.getClassDescriptorByName(
										"NetworkProperty").getType());
						item.setFieldValue("networkType", "HCPPI");
						item.setFieldValue("name", "Betweeness");
						item.setFieldValue("value", formatValue(betweennessCentrality.get(geneId)));
						item.setFieldValue("gene", gene);

						osw.store(item);

						item = (InterMineObject) DynamicUtil.simpleCreateObject(model
								.getClassDescriptorByName("NetworkProperty").getType());
						item.setFieldValue("networkType", "HCPPI");
						item.setFieldValue("name", "Degree");
						item.setFieldValue("value", String.valueOf(lcc.degree(geneId)));
						item.setFieldValue("gene", gene);

						osw.store(item);

					}

				}
				osw.commitTransaction();
			} catch (ObjectStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private Set<String> getLccPairs(Graph<String, String> lcc) {
		Set<String> ret = new HashSet<String>();
		Collection<String> edges = lcc.getEdges();
		for (String edge : edges) {
			// A-B, B-A have already been filtered
			ret.add(edge);
		}
		return ret;
	}

	private Set<String> getHcppiPairs(Set<InteractionData> hcppi) {
		Set<String> ret = new HashSet<String>();
		for (InteractionData data : hcppi) {
			List<String> genes = data.getGenes();
			if (genes.size() == 2) {
				// A-B, B-A have already been filtered
				ret.add(genes.get(0) + "-" + genes.get(1));
			} else {
				throw new RuntimeException("Invalid InteractionData" + data);
			}
		}

		return ret;
	}

	private void readDbConfig() {
		String configFileName = "intermine.properties";
		ClassLoader classLoader = NetworkAnalysisTool.class.getClassLoader();
		InputStream configStream = classLoader.getResourceAsStream(configFileName);
		if (configStream == null) {
			throw new RuntimeException("can't find resource: " + configFileName);
		}
		Properties config = new Properties();

		System.out.println("read intermine.properties");

		try {
			config.load(configStream);
			host = config.getProperty("db.production.datasource.serverName");
			dbname = config.getProperty("db.production.datasource.databaseName");
			username = config.getProperty("db.production.datasource.user");
			password = config.getProperty("db.production.datasource.password");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Results queryInteractionByTaxonId(String taxonId) {
		Query q = new Query();
		QueryClass qcGene1 = new QueryClass(Gene.class);
		QueryClass qcGene2 = new QueryClass(Gene.class);
		QueryClass qcOrganism1 = new QueryClass(Organism.class);
		QueryClass qcOrganism2 = new QueryClass(Organism.class);
		QueryClass qcInteraction = new QueryClass(Interaction.class);

		QueryField qfTaxonId1 = new QueryField(qcOrganism1, "taxonId");
		QueryField qfTaxonId2 = new QueryField(qcOrganism2, "taxonId");

		q.addFrom(qcInteraction);
		q.addFrom(qcGene1);
		q.addFrom(qcGene2);
		q.addFrom(qcOrganism1);
		q.addFrom(qcOrganism2);
		q.addToSelect(qcInteraction);
		q.addToSelect(qcGene1);
		q.addToSelect(qcGene2);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);
		QueryObjectReference qor1 = new QueryObjectReference(qcGene1, "organism");
		cs.addConstraint(new ContainsConstraint(qor1, ConstraintOp.CONTAINS, qcOrganism1));
		QueryObjectReference qor2 = new QueryObjectReference(qcGene2, "organism");
		cs.addConstraint(new ContainsConstraint(qor2, ConstraintOp.CONTAINS, qcOrganism2));
		QueryObjectReference qor3 = new QueryObjectReference(qcInteraction, "gene1");
		cs.addConstraint(new ContainsConstraint(qor3, ConstraintOp.CONTAINS, qcGene1));
		QueryObjectReference qor4 = new QueryObjectReference(qcInteraction, "gene2");
		cs.addConstraint(new ContainsConstraint(qor4, ConstraintOp.CONTAINS, qcGene2));
		cs.addConstraint(new SimpleConstraint(qfTaxonId1, ConstraintOp.EQUALS, new QueryValue(
				Integer.valueOf(taxonId))));
		cs.addConstraint(new SimpleConstraint(qfTaxonId2, ConstraintOp.EQUALS, new QueryValue(
				Integer.valueOf(taxonId))));
		q.setConstraint(cs);

		ObjectStore os = osw.getObjectStore();

		return os.execute(q);
	}

	private Results queryGenesByGeneIdList(Set<String> geneIds) {
		Query q = new Query();
		QueryClass qcGene = new QueryClass(Gene.class);
		QueryField qfGeneId = new QueryField(qcGene, "ncbiGeneId");

		q.addFrom(qcGene);
		q.addToSelect(qcGene);

		q.setConstraint(new BagConstraint(qfGeneId, ConstraintOp.IN, geneIds));

		ObjectStore os = osw.getObjectStore();

		return os.execute(q);
	}

	private Results queryGenesByTaxonId(String taxonId) {
		Query q = new Query();
		QueryClass qcGene = new QueryClass(Gene.class);
		QueryClass qcOrganism = new QueryClass(Organism.class);
		QueryField qfTaxonId = new QueryField(qcOrganism, "taxonId");

		q.addFrom(qcGene);
		q.addFrom(qcOrganism);
		q.addToSelect(qcGene);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);
		QueryObjectReference qor1 = new QueryObjectReference(qcGene, "organism");
		cs.addConstraint(new ContainsConstraint(qor1, ConstraintOp.CONTAINS, qcOrganism));
		cs.addConstraint(new SimpleConstraint(qfTaxonId, ConstraintOp.EQUALS, new QueryValue(
				Integer.valueOf(taxonId))));
		q.setConstraint(cs);

		ObjectStore os = osw.getObjectStore();

		return os.execute(q);
	}

	/**
	 * Outer join is difficult to implement, which is important for filtering HCPPI. (Some
	 * interactions derived from iRefIndex are not annotated with a interaction type MI term). <br/>
	 * Either make multiple queries or add an mock MI term when integrating iRefIndex, then this
	 * method could be re-implemented (even now itself was not finished)
	 * 
	 * @param taxonId
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private List<String> getOsPhysicalInteractions(String taxonId) throws Exception {
		Query q = new Query();
		QueryClass qcGene1 = new QueryClass(Gene.class);
		QueryClass qcGene2 = new QueryClass(Gene.class);
		QueryClass qcInteraction = new QueryClass(
				Class.forName("org.intermine.model.bio.Interaction"));
		QueryClass qcInteractionDetail = new QueryClass(
				Class.forName("org.intermine.model.bio.InteractionDetail"));
		QueryClass qcRelationshipType = new QueryClass(
				Class.forName("org.intermine.model.bio.InteractionTerm"));
		QueryClass qcInteractionDetectionMethods = new QueryClass(
				Class.forName("org.intermine.model.bio.InteractionTerm"));
		QueryClass qcInteractionExperiment = new QueryClass(
				Class.forName("org.intermine.model.bio.InteractionExperiment"));
		QueryClass qcOrganism1 = new QueryClass(Organism.class);
		QueryClass qcOrganism2 = new QueryClass(Organism.class);
		QueryClass qcPublication = new QueryClass(Publication.class);

		q.addFrom(qcGene1);
		q.addFrom(qcInteraction);
		q.addFrom(qcGene2);
		q.addFrom(qcOrganism1);
		q.addFrom(qcOrganism2);
		q.addFrom(qcInteractionDetail);
		q.addFrom(qcRelationshipType);
		q.addFrom(qcInteractionDetectionMethods);
		q.addFrom(qcInteractionExperiment);
		q.addFrom(qcPublication);

		q.addToSelect(qcGene1);

		QueryField qfGeneId1 = new QueryField(qcGene1, "ncbiGeneId");
		QueryField qfGeneId2 = new QueryField(qcGene2, "ncbiGeneId");
		QueryField qfTaxonId1 = new QueryField(qcOrganism1, "taxonId");
		QueryField qfTaxonId2 = new QueryField(qcOrganism2, "taxonId");
		// QueryField qfInteractType = new QueryField(qcInteractionDetail, "type");
		QueryField qfRelTypeId = new QueryField(qcRelationshipType, "identifier");
		QueryField qfPubmedId = new QueryField(qcPublication, "pubMedId");
		QueryField qfMethodId = new QueryField(qcInteractionDetectionMethods, "identifier");
		QueryField qfDesc = new QueryField(qcInteractionDetectionMethods, "name");

		q.addToSelect(qfGeneId1);
		q.addToSelect(qfGeneId2);
		q.addToSelect(qfRelTypeId);
		q.addToSelect(qfMethodId);
		q.addToSelect(qfDesc);
		q.addToSelect(qfPubmedId);
		// q.setDistinct(true);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		QueryObjectReference qor1 = new QueryObjectReference(qcGene1, "organism");
		cs.addConstraint(new ContainsConstraint(qor1, ConstraintOp.CONTAINS, qcOrganism1));
		QueryObjectReference qor2 = new QueryObjectReference(qcGene2, "organism");
		cs.addConstraint(new ContainsConstraint(qor2, ConstraintOp.CONTAINS, qcOrganism2));
		QueryObjectReference qor3 = new QueryObjectReference(qcInteraction, "gene1");
		cs.addConstraint(new ContainsConstraint(qor3, ConstraintOp.CONTAINS, qcGene1));
		QueryObjectReference qor4 = new QueryObjectReference(qcInteraction, "gene2");
		cs.addConstraint(new ContainsConstraint(qor4, ConstraintOp.CONTAINS, qcGene2));

		QueryCollectionReference qcr1 = new QueryCollectionReference(qcInteraction, "details");
		cs.addConstraint(new ContainsConstraint(qcr1, ConstraintOp.CONTAINS, qcInteractionDetail));

		ConstraintSet csType = new ConstraintSet(ConstraintOp.OR);
		QueryObjectReference qor5 = new QueryObjectReference(qcInteractionDetail,
				"relationshipType");
		cs.addConstraint(new ContainsConstraint(qor5, ConstraintOp.CONTAINS, qcRelationshipType));

		QueryObjectReference qor6 = new QueryObjectReference(qcInteractionDetail, "experiment");
		cs.addConstraint(new ContainsConstraint(qor6, ConstraintOp.CONTAINS,
				qcInteractionExperiment));

		QueryObjectReference qor7 = new QueryObjectReference(qcInteractionExperiment, "publication");
		cs.addConstraint(new ContainsConstraint(qor7, ConstraintOp.CONTAINS, qcPublication));
		QueryCollectionReference qcr2 = new QueryCollectionReference(qcInteractionExperiment,
				"interactionDetectionMethods");
		cs.addConstraint(new ContainsConstraint(qcr2, ConstraintOp.CONTAINS,
				qcInteractionDetectionMethods));

		cs.addConstraint(new SimpleConstraint(qfTaxonId1, ConstraintOp.EQUALS, new QueryValue(
				Integer.valueOf(taxonId))));
		cs.addConstraint(new SimpleConstraint(qfTaxonId2, ConstraintOp.EQUALS, new QueryValue(
				Integer.valueOf(taxonId))));
		q.setConstraint(cs);

		ObjectStore os = osw.getObjectStore();

		List<String> ret = new ArrayList<String>();
		// ((ObjectStoreInterMineImpl) os).precompute(q, Constants.PRECOMPUTE_CATEGORY);
		// Results results = os.execute(q, 5000, true, true, true);
		Results results = os.execute(q);

		LOG.info("There are " + results.size() + " rows.");
		System.out.println("There are " + results.size() + " rows.");
		// Iterator iterator = results.iterator();
		// while (iterator.hasNext()) {
		// ResultsRow<Integer> rr = (ResultsRow<Integer>) iterator.next();
		// ret.add(rr.get(0).toString());
		// }
		return ret;
	}

	public Collection<InteractionData> getPhysicalInteractions(String taxonId) {
		if (directMiTerms == null || directMiTerms.isEmpty()) {
			throw new RuntimeException("the direct MI term should be read first!");
		}

		Map<String, InteractionData> interactionMap = new HashMap<String, InteractionData>();
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(
					String.format("jdbc:postgresql://%s:%s/%s", host, PORT, dbname), username,
					password);

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement
					.executeQuery("select g1.ncbigeneid, g2.ncbigeneid, rtype.identifier, dm.identifier, dm.name, pub.pubmedid "
							+ " from interaction as int  "
							+ " join gene as g1 on gene1id = g1.id  "
							+ " join gene as g2 on gene2id = g2.id  "
							+ " join organism as o1 on g1.organismid = o1.id "
							+ " join organism as o2 on g2.organismid = o2.id "
							+ " join interactiondetail as intd on intd.interactionid = int.id "
							+ " left join interactionterm as rtype on rtype.id = intd.relationshiptypeid "
							+ " join interactionexperiment as exp on exp.id = intd.experimentid "
							+ " join interactiondetectionmethodsinteractionexperiment as expdm on expdm.interactionexperiment = exp.id "
							+ " join interactionterm as dm on dm.id = expdm.interactiondetectionmethods "
							+ " join publication as pub on pub.id = exp.publicationid "
							+ " where o1.taxonid = "
							+ taxonId
							+ " and o2.taxonid = "
							+ taxonId
							+ " and ( intd.type = 'physical' or intd.type is null)");
			// int count = 0;
			while (resultSet.next()) {
				// System.out.println(resultSet.getString("symbol"));
				String geneA = resultSet.getString(1);
				String geneB = resultSet.getString(2);
				String type = resultSet.getString(3);
				String method = resultSet.getString(4);
				String desc = resultSet.getString(5);
				String pubmedId = resultSet.getString(6);
				InteractionData intData = interactionMap.get(geneA + "-" + geneB);
				if (intData == null) {
					intData = interactionMap.get(geneB + "-" + geneA);
					if (intData == null) {
						intData = new InteractionData(geneA, geneB);
						interactionMap.put(geneA + "-" + geneB, intData);
					}
				}

				if (desc != null && desc != "") {
					intData.addMethod(method);
				}
				intData.addType(type);
				intData.addPubmedId(pubmedId);
				if (directMiTerms.contains(type)) {
					intData.setDirectInt();
				}
				// count++;
			}
			// System.out.println(count + " lines has been processed.");

			connection.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return interactionMap.values();
	}

	private Set<String> directMiTerms = new HashSet<String>();

	private String host;

	private String dbname;

	private String username;

	private String password;

	private void readDirectMiTerm() {
		ClassLoader classLoader = NetworkAnalysisTool.class.getClassLoader();
		InputStream dmIs = classLoader.getResourceAsStream("direct-mi.tsv");
		InputStreamReader reader = new InputStreamReader(dmIs);
		BufferedReader in = null;
		try {
			in = new BufferedReader(reader);
			String line;
			while ((line = in.readLine()) != null) {
				String[] cols = line.split("\t");
				directMiTerms.add(cols[0]);
			}
			in.close();
		} catch (FileNotFoundException e) {
			LOG.error(e);
		} catch (IOException e) {
			LOG.error(e);
		}

	}

	private Graph<String, String> findLargestConnectiveNetwork(Collection<InteractionData> data) {
		Graph<String, String> graph = generateNetworkGraph(data);

		WeakComponentClusterer<String, String> wcc = new WeakComponentClusterer<String, String>();
		Set<Set<String>> transform = wcc.transform(graph);

		List<Set<String>> list = new ArrayList<Set<String>>(transform);

		Collections.sort(list, new Comparator<Set<String>>() {

			@Override
			public int compare(Set<String> o1, Set<String> o2) {
				if (o2.size() == o1.size()) {
					return 0;
				}
				return (o2.size() > o1.size() ? 1 : -1);
			}
		});

		Graph<String, String> lcc = FilterUtils.createInducedSubgraph(list.get(0), graph);

		return lcc;
	}

	private Graph<String, String> generateNetworkGraph(Collection<InteractionData> data) {
		Graph<String, String> g = new SparseMultigraph<String, String>();
		Set<String> vertex = new HashSet<String>();
		Iterator<InteractionData> iterator = data.iterator();
		while (iterator.hasNext()) {
			InteractionData interactionData = iterator.next();
			List<String> genes = interactionData.getGenes();
			for (String gene : genes) {
				if (!vertex.contains(gene)) {
					g.addVertex(gene);
					vertex.add(gene);
				}
			}
			g.addEdge(String.format("%s-%s", genes.get(0), genes.get(1)), genes.get(0),
					genes.get(1));

		}
		return g;
	}

	private Map<String, Double> calcBetweennessCentrality(Graph<String, String> graph) {
		int n = graph.getVertexCount();
		// number of node pairs excluding n; for normalization
		double nor = (n - 1d) * (n - 2d);

		BetweennessCentrality<String, String> bc = new BetweennessCentrality<String, String>(graph);

		Collection<String> vertices = graph.getVertices();

		Map<String, Double> ret = new HashMap<String, Double>();
		for (String string : vertices) {
			// System.err.println(string + ": " + bc.getVertexScore(string));
			ret.put(string, bc.getVertexScore(string) / nor);
		}

		return ret;
	}

	public static class InteractionData {
		private List<String> genes;
		private Set<String> types = new HashSet<String>();
		private Set<String> methods = new HashSet<String>();
		private Set<String> pubmedIds = new HashSet<String>();
		private boolean hasDirectInt = false;

		public InteractionData(String geneA, String geneB) {
			this.genes = Arrays.asList(geneA, geneB);
		}

		public List<String> getGenes() {
			return genes;
		}

		public void addType(String type) {
			this.types.add(type);
		}

		public void addMethod(String method) {
			this.methods.add(method);
		}

		public void addPubmedId(String pubmedId) {
			this.pubmedIds.add(pubmedId);
		}

		public void setDirectInt() {
			this.hasDirectInt = true;
		}

		public boolean isHighConfident() {
			if (methods.size() > 1 || pubmedIds.size() > 1) {
				return true;
			}
			return false;
		}

		public boolean isHighConfidentDirect() {
			if (hasDirectInt && isHighConfident()) {
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			return String.format("%s\t%s\t%d\t%d", genes.get(0), genes.get(1), methods.size(),
					pubmedIds.size());
		}

	}

	public String formatValue(Double value) {
		if (value == 0) {
			return "0";
		} else if (value < 0.0001) {
			DecimalFormat df = new DecimalFormat("0.###E0");
			return df.format(value);
		}
		return String.format("%.6f", value);
	}

}
