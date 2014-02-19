package org.intermine.bio.postprocess;

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
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.Organism;
import org.intermine.model.bio.Pathway;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.query.ConstraintOp;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.QueryObjectReference;
import org.intermine.objectstore.query.QueryValue;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.SimpleConstraint;

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
	
	public void doClustering(){
		List<String> species = Arrays.asList("9606", "10090", "10116");
		for (String taxonId : species) {
			Results pathwayGenes = queryPathwayGenes(taxonId);
			// TODO format for next step
			Map<String, Set<String>> allPathwayGene = new HashMap<String, Set<String>>();
			
			Map<String, Set<String>> filteredPathwayGene = filterSubsets(allPathwayGene);
			
			Map<String, List<Double>> similarityIndex = calculateSimilarityIndex(filteredPathwayGene);
			
			Map<String, Map<String, Double>> matrix = calculateCorrelationMatrix(similarityIndex);
			
			HierarchicalClustering hc = new HierarchicalClustering(matrix);
			
			List<String> clusters = hc.clusteringByAverageLinkage(0.7d);
			
			createClusters(clusters);
		}
	}
	
	public void testQuery() {
		
	}


	// TODO to be implemented
	private Results queryPathwayGenes(String taxonId) {
		Query q = new Query();
		QueryClass qcGene = new QueryClass(Gene.class);
		QueryClass qcPathway = new QueryClass(Pathway.class);
		QueryClass qcOrganism = new QueryClass(Organism.class);
		QueryField qfTaxonId = new QueryField(qcOrganism, "taxonId");

		q.addFrom(qcGene);
		q.addFrom(qcPathway);
		q.addFrom(qcOrganism);
		q.addToSelect(qcGene);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);
		QueryObjectReference qor1 = new QueryObjectReference(qcGene, "organism");
		cs.addConstraint(new ContainsConstraint(qor1, ConstraintOp.CONTAINS, qcOrganism));
		cs.addConstraint(new SimpleConstraint(qfTaxonId, ConstraintOp.EQUALS, new QueryValue(
				Integer.valueOf(taxonId))));
		QueryObjectReference qor2 = new QueryObjectReference(qcPathway, "organism");
		cs.addConstraint(new ContainsConstraint(qor2, ConstraintOp.CONTAINS, qcOrganism));
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

	private void createClusters(List<String> clusters) {
		Collections.sort(clusters, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.split("=").length>=o2.split("=").length?-1:1;
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

	// TODO to be finished
	private void createGeneSetClusters(final Map<String, Set<String>> allPathwayGenes,
			Map<String, String> pathwayNames, List<String> clusters) {
		Collections.sort(clusters, new Comparator<String>() {
			
			@Override
			public int compare(String o1, String o2) {
				return Ints.compare(o2.split("=").length, o1.split("=").length);
			}
		});
		
		int clusterNo = 1;
		for (String cluster : clusters) {
			String[] pathways = cluster.split("=");
			
			Set<String> allGeneIds = new HashSet<String>();
			for (String p : pathways) {
				allGeneIds.addAll(allPathwayGenes.get(p));
			}
			List<String> list = Arrays.asList(pathways);
			Collections.sort(list, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					return Ints.compare(allPathwayGenes.get(o2).size(),
							allPathwayGenes.get(o1).size());
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
				accumulate.addAll(allPathwayGenes.get(pathway));
				double accPercent = (double) Math.round((double) accumulate.size() / (double) count
						* 10000) / 100;
				autoName.add(pathwayNames.get(pathway));
				if (name == null && accPercent >= 50) {
					name = StringUtils.join(autoName, "|");
					numName = autoName.size();
				}
			}
			String clusterId = String.format("no%03d", clusterNo);
//			nameWriter.write(clusterId + "\t" + numName + "\t" + name + "\n");
			
			Set<String> allPathways = addSubsets(pathways);
//			writer.write(clusterId + "\t" + allPathways.size() + "\t"
//					+ StringUtils.join(allPathways, ",") + "\n");
			
			clusterNo++;
		}
		
	}

	private Map<String, Map<String, Double>> calculateCorrelationMatrix(Map<String, List<Double>> similarityIndex) {
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
	
	private Map<String, List<Double>> calculateSimilarityIndex(final Map<String, Set<String>> pathwayGene) {
		List<String> pathways = new ArrayList<String>(pathwayGene.keySet());
		Map<String, List<Double>> ret = new HashMap<String, List<Double>>();
		for (String p1 : pathways) {
			ret.put(p1, new ArrayList<Double>());
			Set<String> geneSet1 = pathwayGene.get(p1);
			for (String p2 : pathways) {
				Set<String> geneSet2 = pathwayGene.get(p2);
				double intersect = (double) Sets.intersection(geneSet1, geneSet2).size();
				double min = (double) Math.min(geneSet1.size(), geneSet2.size());
				ret.get(p1).add(Double.valueOf(intersect/min));
			}
		}
		return ret;
	}

	Map<String,GeneSet> map = new HashMap<String,GeneSet>();

	private Map<String, Set<String>> filterSubsets(final Map<String, Set<String>> pathwayGene) {
		List<String> pathways = new ArrayList<String>(pathwayGene.keySet());
		Collections.sort(pathways, new Comparator<String>(){
			
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
			for (int j = i+1; j < pathways.size(); j++) {
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
				for (GeneSet gs: children) {
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
}
