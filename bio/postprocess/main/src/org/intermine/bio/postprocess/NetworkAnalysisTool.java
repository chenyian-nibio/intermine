package org.intermine.bio.postprocess;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
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
import java.util.Set;

import org.apache.log4j.Logger;
import org.intermine.metadata.ConstraintOp;
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.Organism;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.intermine.ObjectStoreWriterInterMineImpl;
import org.intermine.objectstore.query.BagConstraint;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.QueryObjectReference;
import org.intermine.objectstore.query.QueryValue;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.objectstore.query.SimpleConstraint;
import org.intermine.sql.Database;
import org.intermine.util.DynamicUtil;

import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.filters.FilterUtils;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;

/**
 * 
 * @author chenyian
 * 
 */
public class NetworkAnalysisTool {
	private static final Logger LOG = Logger.getLogger(NetworkAnalysisTool.class);

	private static final int CUT_OFF_PERCENTAGE = 10; // top 10 percent as bottle and hub

	protected ObjectStoreWriter osw;

	private Model model;

	public NetworkAnalysisTool(ObjectStoreWriter osw) {
		this.osw = osw;
		model = Model.getInstanceByName("genomic");
	}

	public void doAnalysis() {
		// create datasource & dataset
		InterMineObject dataSource = (InterMineObject) DynamicUtil.simpleCreateObject(model
				.getClassDescriptorByName("DataSource").getType());
		dataSource.setFieldValue("name", "TargetMine");
		// dataSource.setFieldValue("url","http://targetmine.mizuguchilab.org");
		InterMineObject dataSet = (InterMineObject) DynamicUtil.simpleCreateObject(model
				.getClassDescriptorByName("DataSet").getType());
		dataSet.setFieldValue("name", "TargetMine");
		dataSet.setFieldValue("dataSource", dataSource);
		dataSet.setFieldValue("description", "TargetMine interactome analysis");
		// dataSet.setFieldValue("url","http://targetmine.mizuguchilab.org");
		try {
			osw.store(dataSource);
			osw.store(dataSet);
		} catch (ObjectStoreException e) {
			e.printStackTrace();
		}

		int i = 0;
		long[] currentTime = new long[100];
		currentTime[i] = System.currentTimeMillis();
		i++;

		LOG.info("Start to do network analysis......");
		System.out.println("Start to do network analysis......");
		readDirectMiTerm();

		currentTime[i] = System.currentTimeMillis();
		System.out.println("Spent " + (currentTime[i] - currentTime[i - 1]) / 1000 + " seconds");
		i++;

		List<String> species = Arrays.asList("9606", "10090", "10116");
		for (String taxonId : species) {

			Collection<InteractionData> interactions = getPhysicalInteractions(taxonId);

			Iterator<InteractionData> iterator = interactions.iterator();
			Set<InteractionData> hcdp = new HashSet<InteractionData>();
			Set<InteractionData> hc = new HashSet<InteractionData>();
			// distinguish HC, HCDP
			while (iterator.hasNext()) {
				InteractionData intData = iterator.next();
				if (intData.isHighConfident()) {
					hc.add(intData);
					if (intData.hasDirectInt) {
						hcdp.add(intData);
					}
				}
			}
			System.out.println("HCDP contains " + hcdp.size() + " interactions (" + taxonId + ").");
			System.out.println("HC contains " + hc.size() + " interactions (" + taxonId + ").");

			Graph<String, String> hcdplcc = findLargestConnectiveNetwork(hcdp);
			System.out.println("HCDPLCC contains " + hcdplcc.getEdgeCount() + " interactions ("
					+ taxonId + ").");
			Graph<String, String> hclcc = findLargestConnectiveNetwork(hc);
			System.out.println("HCLCC contains " + hclcc.getEdgeCount() + " interactions ("
					+ taxonId + ").");

			Results intResults = queryInteractionByTaxonId(taxonId);
			System.out.println("There are " + intResults.size() + " interactions (" + taxonId
					+ ").");

			Set<String> hcdpPairs = getInteractionPairs(hcdp);
			Set<String> hcPairs = getInteractionPairs(hc);

			Iterator<?> resIter = intResults.iterator();
			try {
				osw.beginTransaction();
				int x = 0;
				int y = 0;
				while (resIter.hasNext()) {
					ResultsRow<?> rr = (ResultsRow<?>) resIter.next();
					InterMineObject interaction = (InterMineObject) rr.get(0);
					Gene gene1 = (Gene) rr.get(1);
					Gene gene2 = (Gene) rr.get(2);
					String gene1Id = gene1.getPrimaryIdentifier();
					String gene2Id = gene2.getPrimaryIdentifier();
					InterMineObject item = (InterMineObject) DynamicUtil.simpleCreateObject(model
							.getClassDescriptorByName("InteractionConfidence").getType());
					if (hcdpPairs.contains(gene1Id + "-" + gene2Id)
							|| hcdpPairs.contains(gene2Id + "-" + gene1Id)) {
						item.setFieldValue("type", "HCDP");
						x++;
					} else if (hcPairs.contains(gene1Id + "-" + gene2Id)
							|| hcPairs.contains(gene2Id + "-" + gene1Id)) {
						item.setFieldValue("type", "HC");
						y++;
					} else {
						continue;
					}
					item.setFieldValue("interaction", interaction);
					item.setFieldValue("dataSet", dataSet);
					osw.store(item);
				}
				System.out.println("There are " + x + " interactions tag with HCDP. (" + taxonId
						+ ").");
				System.out.println("There are " + y + " interactions tag with HC. (" + taxonId
						+ ").");
				osw.commitTransaction();

				// For tracing
				currentTime[i] = System.currentTimeMillis();
				System.out.println("Spent " + (currentTime[i] - currentTime[i - 1]) / 1000
						+ " seconds");
				i++;

				System.out.println("calculateNetworkProperties(hcdplcc)...");

				Map<String, NetworkData> hcdplccNp = calculateNetworkProperties(hcdplcc);

				// For tracing
				currentTime[i] = System.currentTimeMillis();
				System.out.println("Spent " + (currentTime[i] - currentTime[i - 1]) / 1000
						+ " seconds");
				i++;

				Set<String> geneIds = hcdplccNp.keySet();

				System.out
						.println("Querying by " + geneIds.size() + " gene IDs (" + taxonId + ").");
				Results geneResults = queryGenesByGeneIdList(geneIds);
				System.out.println("Retrieve " + geneResults.size() + " genes (" + taxonId + ").");

				osw.beginTransaction();

				Iterator<?> geneIter = geneResults.iterator();
				while (geneIter.hasNext()) {
					ResultsRow<?> rr = (ResultsRow<?>) geneIter.next();
					Gene gene = (Gene) rr.get(0);
					String geneId = gene.getPrimaryIdentifier();
					NetworkData hcdpData = hcdplccNp.get(geneId);
					if (hcdpData != null) {
						InterMineObject item = (InterMineObject) DynamicUtil
								.simpleCreateObject(model.getClassDescriptorByName(
										"NetworkProperty").getType());
						item.setFieldValue("networkType", "HCDPLCC");
						item.setFieldValue("isBottleneck", hcdpData.isBottleneck());
						item.setFieldValue("isHub", hcdpData.isHub());
						item.setFieldValue("betweenness", hcdpData.getBetweenness());
						item.setFieldValue("closeness", hcdpData.getCloseness());
						item.setFieldValue("degree", hcdpData.getDegree());
						item.setFieldValue("gene", gene);

						osw.store(item);
					}
				}
				osw.commitTransaction();

				// For tracing
				currentTime[i] = System.currentTimeMillis();
				System.out.println("Spent " + (currentTime[i] - currentTime[i - 1]) / 1000
						+ " seconds");
				i++;

			} catch (ObjectStoreException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unused")
	private Set<String> getInteractionPairs(Graph<String, String> lcc) {
		Set<String> ret = new HashSet<String>();
		Collection<String> edges = lcc.getEdges();
		for (String edge : edges) {
			// A-B, B-A have already been filtered
			ret.add(edge);
		}
		return ret;
	}

	private Set<String> getInteractionPairs(Set<InteractionData> hcppi) {
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

	private Results queryInteractionByTaxonId(String taxonId) {
		Query q = new Query();
		QueryClass qcGene1 = new QueryClass(Gene.class);
		QueryClass qcGene2 = new QueryClass(Gene.class);
		QueryClass qcOrganism1 = new QueryClass(Organism.class);
		QueryClass qcOrganism2 = new QueryClass(Organism.class);
		QueryClass qcInteraction = new QueryClass(model.getClassDescriptorByName("Interaction")
				.getType());

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
		QueryField qfGeneId = new QueryField(qcGene, "primaryIdentifier");

		q.addFrom(qcGene);
		q.addToSelect(qcGene);

		q.setConstraint(new BagConstraint(qfGeneId, ConstraintOp.IN, geneIds));

		ObjectStore os = osw.getObjectStore();

		return os.execute(q);
	}


	public Collection<InteractionData> getPhysicalInteractions(String taxonId) {
		if (directMiTerms == null || directMiTerms.isEmpty()) {
			throw new RuntimeException("the direct MI term should be read first!");
		}

		Map<String, InteractionData> interactionMap = new HashMap<String, InteractionData>();
		if (osw instanceof ObjectStoreWriterInterMineImpl) {
			Database db = ((ObjectStoreWriterInterMineImpl) osw).getDatabase();
			Connection connection;
			try {
				connection = db.getConnection();

				Statement statement = connection.createStatement();
				// TODO check if CCSB data is excluded; theoretically details without experiment and publication will be excluded...  
				ResultSet resultSet = statement
						.executeQuery("select g1.primaryidentifier, g2.primaryidentifier, rtype.identifier, dm.identifier, dm.name, pub.pubmedid "
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
								+ " and ( intd.type = 'physical' or intd.type = 'unspecified')");
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

				statement.close();
				connection.close();

			} catch (SQLException e) {
				e.printStackTrace();
			}

		} else {
			throw new RuntimeException("the ObjectStoreWriter is not an "
					+ "ObjectStoreWriterInterMineImpl");
		}

		return interactionMap.values();
	}

	private Set<String> directMiTerms = new HashSet<String>();

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

	private Map<String, NetworkData> calculateNetworkProperties(Graph<String, String> graph) {
		Collection<String> vertices = graph.getVertices();
		Set<String> geneIds = new HashSet<String>();
		for (String id : vertices) {
			geneIds.add(id);
		}

		BetweennessCentrality<String, String> bc = new BetweennessCentrality<String, String>(graph);

		ClosenessCentrality<String, String> cc = new ClosenessCentrality<String, String>(graph);

		int n = graph.getVertexCount();
		double nor = (n - 1d) * (n - 2d);
		int pos = n - n / CUT_OFF_PERCENTAGE;

		List<Integer> allDegree = new ArrayList<Integer>();
		List<Double> allBetweenness = new ArrayList<Double>();
		Map<String, NetworkData> ret = new HashMap<String, NetworkData>();
		for (String id : geneIds) {
			Double b = bc.getVertexScore(id) / nor;
			ret.put(id, new NetworkData(id, graph.degree(id), b, cc.getVertexScore(id)));
			allBetweenness.add(b);
			allDegree.add(graph.degree(id));
		}
		Collections.sort(allDegree);
		Collections.sort(allBetweenness);
		Integer minHub = allDegree.get(pos);
		Double minBn = allBetweenness.get(pos);
		Set<String> keys = ret.keySet();
		for (String key : keys) {
			if (ret.get(key).getBetweenness() >= minBn) {
				ret.get(key).setAsBottleneck();
			}
			if (ret.get(key).getDegree() >= minHub) {
				ret.get(key).setAsHub();
			}
		}
		return ret;
	}

	public static class NetworkData {
		private String geneId;
		private Integer degree;
		private Double betweenness;
		private Double closeness;
		private Boolean isBottleneck;
		private Boolean isHub;

		public NetworkData(String geneId, Integer degree, Double betweenness, Double closeness) {
			super();
			this.geneId = geneId;
			this.degree = degree;
			this.betweenness = betweenness;
			this.closeness = closeness;
			this.isBottleneck = Boolean.FALSE;
			this.isHub = Boolean.FALSE;
		}

		public String getGeneId() {
			return geneId;
		}

		public Integer getDegree() {
			return degree;
		}

		public Double getBetweenness() {
			return betweenness;
		}

		public Double getCloseness() {
			return closeness;
		}

		public Boolean isBottleneck() {
			return isBottleneck;
		}

		public void setAsBottleneck() {
			this.isBottleneck = Boolean.TRUE;
		}

		public Boolean isHub() {
			return isHub;
		}

		public void setAsHub() {
			this.isHub = Boolean.TRUE;
		}

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
