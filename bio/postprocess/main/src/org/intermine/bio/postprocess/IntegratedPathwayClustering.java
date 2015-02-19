package org.intermine.bio.postprocess;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.log4j.Logger;
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.Organism;
import org.intermine.model.bio.Pathway;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
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

import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

/**
 * 
 * @author chenyian
 * 
 */
public class IntegratedPathwayClustering {
	private static final Logger LOG = Logger.getLogger(IntegratedPathwayClustering.class);

	protected ObjectStoreWriter osw;

	private Model model;

	public IntegratedPathwayClustering(ObjectStoreWriter osw) {
		this.osw = osw;
		model = Model.getInstanceByName("genomic");
	}

	public void doClustering() {
		List<String> speciesIds = Arrays.asList("9606", "10090", "10116");
		List<String> speciesCodes = Arrays.asList("hsa", "mmu", "rno");
		for (int i = 0; i < speciesIds.size(); i++) {
			String taxonId = speciesIds.get(i);

			queryAllPathwayGenes(taxonId);

			System.out.println("All pathways (" + taxonId + "): " + allPathwayGenes.size());

			Map<String, Set<String>> filteredPathwayGene = filterSubsets(allPathwayGenes);

			System.out.println("Filtered (" + taxonId + "): " + filteredPathwayGene.size());

			Map<String, List<Double>> similarityIndex = calculateSimilarityIndex(filteredPathwayGene);

			Map<String, Map<String, Double>> matrix = calculateCorrelationMatrix(similarityIndex);

			HierarchicalClustering hc = new HierarchicalClustering(matrix);

			List<String> clusters = hc.clusteringByAverageLinkage(0.7d);

			createIntegratedPathwayClusters(filteredPathwayGene, clusters, speciesCodes.get(i));
		}
	}

	Map<String, Set<String>> allPathwayGenes;
	Map<String, Pathway> pathwayMap;

	public void queryAllPathwayGenes(String taxonId) {
		System.out.println("Starting the testQuery... taxonId: " + taxonId);
		Results results = queryPathwaysToGenes(taxonId);
		Iterator<?> iterator = results.iterator();

		HashMap<String, Set<String>> pathwayGeneMap = new HashMap<String, Set<String>>();
		pathwayMap = new HashMap<String, Pathway>();
		while (iterator.hasNext()) {
			ResultsRow<?> result = (ResultsRow<?>) iterator.next();
			Gene gene = (Gene) result.get(0);
			Pathway pathway = (Pathway) result.get(1);
			String pathwayIdentifier = pathway.getIdentifier();
			if (!pathwayGeneMap.containsKey(pathwayIdentifier)) {
				pathwayGeneMap.put(pathwayIdentifier, new HashSet<String>());
			}
			pathwayGeneMap.get(pathwayIdentifier).add(gene.getPrimaryIdentifier());

			if (!pathwayMap.containsKey(pathwayIdentifier)) {
				pathwayMap.put(pathwayIdentifier, pathway);
			}

		}

		allPathwayGenes = new HashMap<String, Set<String>>();
		for (String pathway : pathwayGeneMap.keySet()) {
			Set<String> geneSet = pathwayGeneMap.get(pathway);
			if (geneSet.size() < 600) {
				allPathwayGenes.put(pathway, geneSet);
			}
		}

	}

	private Results queryPathwaysToGenes(String taxonId) {
		Query q = new Query();
		QueryClass qcGene = new QueryClass(Gene.class);
		QueryClass qcPathway = new QueryClass(Pathway.class);
		QueryClass qcOrganism1 = new QueryClass(Organism.class);
		QueryClass qcOrganism2 = new QueryClass(Organism.class);

		QueryField qfTaxonId1 = new QueryField(qcOrganism1, "taxonId");
		QueryField qfTaxonId2 = new QueryField(qcOrganism2, "taxonId");

		q.addFrom(qcGene);
		q.addFrom(qcPathway);
		q.addFrom(qcOrganism1);
		q.addFrom(qcOrganism2);
		q.addToSelect(qcGene);
		q.addToSelect(qcPathway);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);
		QueryCollectionReference qcr1 = new QueryCollectionReference(qcPathway, "genes");
		cs.addConstraint(new ContainsConstraint(qcr1, ConstraintOp.CONTAINS, qcGene));
		QueryObjectReference qor1 = new QueryObjectReference(qcGene, "organism");
		cs.addConstraint(new ContainsConstraint(qor1, ConstraintOp.CONTAINS, qcOrganism1));
		cs.addConstraint(new SimpleConstraint(qfTaxonId1, ConstraintOp.EQUALS, new QueryValue(
				Integer.valueOf(taxonId))));
		QueryObjectReference qor2 = new QueryObjectReference(qcPathway, "organism");
		cs.addConstraint(new ContainsConstraint(qor2, ConstraintOp.CONTAINS, qcOrganism2));
		cs.addConstraint(new SimpleConstraint(qfTaxonId2, ConstraintOp.EQUALS, new QueryValue(
				Integer.valueOf(taxonId))));
		q.setConstraint(cs);

		ObjectStore os = osw.getObjectStore();

		return os.execute(q);
	}

	private Set<String> addSubsets(String[] clusters) {
		Set<String> ret = new HashSet<String>();
		for (String cluster : clusters) {
			Set<GeneSet> allChildren = map.get(cluster).getAllChildren();
			for (GeneSet geneSet : allChildren) {
				ret.add(geneSet.getIdentifier());
			}
			ret.add(cluster);
		}
		return ret;
	}

	@SuppressWarnings("unused")
	private void createClusters(List<String> clusters) {
		Collections.sort(clusters, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.split("=").length >= o2.split("=").length ? -1 : 1;
			}
		});

		int clusterNo = 1;
		for (String cluster : clusters) {
			String[] pIndex = cluster.split("=");
			Set<String> allPathways = addSubsets(pIndex);
			String clusterId = String.format("no%03d", clusterNo);

			clusterNo++;
		}

	}

	private void createIntegratedPathwayClusters(final Map<String, Set<String>> pathwayGenes,
			List<String> clusters, String speciesCode) {
		Collections.sort(clusters, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return Ints.compare(o2.split("=").length, o1.split("=").length);
			}
		});

		StringBuffer sb = new StringBuffer();
		CytoJSON json = new CytoJSON();

		try {
			osw.beginTransaction();
			int clusterNo = 1;
			for (String cluster : clusters) {
				String[] pathwayIds = cluster.split("=");

				Set<String> allGeneIds = new HashSet<String>();
				for (String p : pathwayIds) {
					allGeneIds.addAll(pathwayGenes.get(p));
				}
				List<String> list = Arrays.asList(pathwayIds);
				Collections.sort(list, new Comparator<String>() {

					@Override
					public int compare(String o1, String o2) {
						return Ints.compare(pathwayGenes.get(o2).size(), pathwayGenes.get(o1)
								.size());
					}

				});

				int count = allGeneIds.size();

				Set<String> accumulate = new HashSet<String>();
				List<String> autoName = new ArrayList<String>();
				String name = null;
				int numName = 0;
				for (Iterator<String> iterator2 = list.iterator(); iterator2.hasNext();) {
					String pathway = iterator2.next();
					// calculate accumulated percentage
					accumulate.addAll(pathwayGenes.get(pathway));
					double accPercent = (double) Math.round((double) accumulate.size()
							/ (double) count * 10000) / 100;
					autoName.add(pathwayMap.get(pathway).getName());
					if (name == null && accPercent >= 50) {
						name = StringUtils.join(autoName, "|");
						numName = autoName.size();
					}
				}
				String clusterId = String.format("%s%03d", speciesCode.substring(0, 1)
						.toUpperCase(), clusterNo);

				// Start of exporting the intermediate data
				sb.append(clusterId + "\t" + pathwayIds.length + "\t"
						+ StringUtils.join(pathwayIds, ",") + "\n");

				json.addCluster(clusterId, pathwayIds, speciesCode);
				// End of exporting the intermediate data

				Set<String> allPathwayIds = addSubsets(pathwayIds);
				LOG.info(clusterId + " (" + numName + ") " + name + ": (" + allPathwayIds.size()
						+ ") " + StringUtils.join(allPathwayIds, ","));

				InterMineObject item = (InterMineObject) DynamicUtil.simpleCreateObject(model
						.getClassDescriptorByName("IntegratedPathwayCluster").getType());
				item.setFieldValue("identifier", clusterId);
				item.setFieldValue("name", name);
				Set<Pathway> pathways = new HashSet<Pathway>();
				for (String pId : allPathwayIds) {
					pathways.add(pathwayMap.get(pId));
				}
				item.setFieldValue("pathways", pathways);

				osw.store(item);

				clusterNo++;
			}

			osw.commitTransaction();
			// osw.abortTransaction();

		} catch (ObjectStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			// FileWriter writer = new FileWriter("lt600.hie.co70." + speciesCode);
			// writer.write(sb.toString());
			// writer.close();
			FileWriter writer = new FileWriter(speciesCode + ".json.txt");
			writer.write(json.exportString());
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Map<String, Map<String, Double>> calculateCorrelationMatrix(
			Map<String, List<Double>> similarityIndex) {
		Map<String, Map<String, Double>> matrix = new HashMap<String, Map<String, Double>>();
		PearsonsCorrelation pc = new PearsonsCorrelation();
		List<String> pathways = new ArrayList<String>(similarityIndex.keySet());
		for (String p : pathways) {
			matrix.put(p, new HashMap<String, Double>());
		}
		for (int i = 1; i < pathways.size(); i++) {
			String p1 = pathways.get(i);
			matrix.get(p1).put(p1, Double.valueOf(0d));
			double[] array1 = Doubles.toArray(similarityIndex.get(p1));
			for (int j = 0; j < i; j++) {
				String p2 = pathways.get(j);
				double[] array2 = Doubles.toArray(similarityIndex.get(p2));
				Double d = Double.valueOf(1d - pc.correlation(array1, array2));
				matrix.get(p1).put(p2, d);
				matrix.get(p2).put(p1, d);
			}
		}
		return matrix;
	}

	private Map<String, List<Double>> calculateSimilarityIndex(
			final Map<String, Set<String>> pathwayGene) {
		List<String> pathways = new ArrayList<String>(pathwayGene.keySet());
		Map<String, List<Double>> ret = new HashMap<String, List<Double>>();
		for (String p1 : pathways) {
			ret.put(p1, new ArrayList<Double>());
			Set<String> geneSet1 = pathwayGene.get(p1);
			for (String p2 : pathways) {
				Set<String> geneSet2 = pathwayGene.get(p2);
				double intersect = (double) Sets.intersection(geneSet1, geneSet2).size();
				double min = (double) Math.min(geneSet1.size(), geneSet2.size());
				ret.get(p1).add(Double.valueOf(intersect / min));
			}
		}
		return ret;
	}

	Map<String, GeneSet> map = new HashMap<String, GeneSet>();

	private Map<String, Set<String>> filterSubsets(final Map<String, Set<String>> pathwayGene) {
		List<String> pathways = new ArrayList<String>(pathwayGene.keySet());
		Collections.sort(pathways, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return Ints.compare(pathwayGene.get(o2).size(), pathwayGene.get(o1).size());
			}

		});

		Set<String> subset = new HashSet<String>();
		for (int i = 0; i < pathways.size() - 1; i++) {
			String p1 = pathways.get(i);
			Set<String> set1 = pathwayGene.get(p1);
			map.put(p1, getGeneSet(p1));
			for (int j = i + 1; j < pathways.size(); j++) {
				String p2 = pathways.get(j);
				Set<String> set2 = pathwayGene.get(p2);
				if (set1.containsAll(set2)) {
					subset.add(p2);
					map.get(p1).addChildren(getGeneSet(p2));
				}
			}
		}
		Map<String, Set<String>> ret = new HashMap<String, Set<String>>();

		for (String string : pathways) {
			if (!subset.contains(string)) {
				ret.put(string, pathwayGene.get(string));
			}
		}

		return ret;
	}

	private GeneSet getGeneSet(String identifier) {
		if (map.get(identifier) == null) {
			map.put(identifier, new GeneSet(identifier));
		}
		return map.get(identifier);
	}

	private static class GeneSet {
		private String identifier;
		private Set<GeneSet> children;

		public GeneSet(String identifier) {
			this.identifier = identifier;
			children = new HashSet<GeneSet>();
		}

		public Set<GeneSet> getAllChildren() {
			Set<GeneSet> ret = new HashSet<GeneSet>();
			if (children.size() > 0) {
				ret.addAll(children);
				for (GeneSet gs : children) {
					ret.addAll(gs.getAllChildren());
				}
			}
			return ret;
		}

		public void addChildren(GeneSet child) {
			this.children.add(child);
		}

		public String getIdentifier() {
			return identifier;
		}

	}

	private class CytoJSON {

		private int nodeId = 1;
		private Map<String, String> nodeIdMap = new HashMap<String, String>();
		private StringBuffer dataSb = new StringBuffer();
		private StringBuffer clusterSb = new StringBuffer();

		public CytoJSON() {
		}

		public void addCluster(String clusterId, String[] pathwayIds, String speciesCode) {
			dataSb.append("\t\t" + clusterId + ": [ \n");
			Arrays.sort(pathwayIds);
			Set<String> allGenes = new HashSet<String>();
			for (String pid : pathwayIds) {
				String nid = nodeIdMap.get(pid);
				if (nid == null) {
					nid = String.valueOf(nodeId);
					nodeId++;
					nodeIdMap.put(pid, nid);
				}
				String db = "N";
				if (pid.startsWith("REACT_")) {
					db = "R";
				} else if (pid.startsWith(speciesCode)) {
					db = "K";
				}
				dataSb.append(String
						.format("\t\t\t{ group: \"nodes\", data: { id: \"%s\", label: \"%s\", desc: \"%s\", db: \"%s\", num: %d } },\n",
								nid, pid, pathwayMap.get(pid).getName(), db,
								allPathwayGenes.get(pid).size()));
				allGenes.addAll(allPathwayGenes.get(pid));
			}

			for (int i = 0; i < pathwayIds.length - 1; i++) {
				for (int j = i + 1; j < pathwayIds.length; j++) {
					int intersect = Sets.intersection(allPathwayGenes.get(pathwayIds[i]),
							allPathwayGenes.get(pathwayIds[j])).size();
					if (intersect > 0) {
						dataSb.append(String
								.format("\t\t\t{ group: \"edges\", data: { target: \"%s\", source: \"%s\", label: \"%d\", share: %d } },\n",
										nodeIdMap.get(pathwayIds[i]), nodeIdMap.get(pathwayIds[j]),
										intersect, intersect));
					}
				}
			}

			dataSb.append("\t\t],\n");

			clusterSb.append(String.format("\t\t%s: { label: \"%s\", num: %d, pnum: %d },\n",
					clusterId, clusterId, allGenes.size(), pathwayIds.length));
		}

		public String exportString() {
			StringBuffer export = new StringBuffer();
			export.append("{\n");
			export.append("\tdata: {\n");
			export.append(dataSb.toString());
			export.append("\t},\n");
			export.append("\tclusters: {\n");
			export.append(clusterSb.toString());
			export.append("\t}\n");
			export.append("}\n");
			return export.toString();
		}
	}

}
