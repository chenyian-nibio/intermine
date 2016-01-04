package org.intermine.bio.postprocess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implement using bottom-up method; the efficiency may not be good
 * 
 * @author chenyian
 *
 */
public class HierarchicalClustering {
	private Map<String, Map<String, Double>> matrix;
	private List<Double> distanceList;

	public List<Double> getDistanceList() {
		return distanceList;
	}

	public HierarchicalClustering(Map<String, Map<String, Double>> matrix) {
		this.matrix = matrix;
		distanceList = new ArrayList<Double>();
	}
	
	protected List<String> clusteringByAverageLinkage(double cutOff) {
		distanceList.clear();

		Map<String, Double> previousDistance = new HashMap<String, Double>();

		List<String> items = new ArrayList<String>(matrix.keySet());
		Collections.sort(items);
		while (1 < items.size()) {
			final Map<String, Double> distance = new HashMap<String, Double>();

			for (int i = 1; i < items.size(); i++) {
				for (int j = 0; j < i; j++) {
					String pair = String.format("%s--%s", items.get(i), items.get(j));
					if (previousDistance.containsKey(pair)) {
						distance.put(pair, previousDistance.get(pair));
					} else {
						distance.put(pair, getAverageDistance(items.get(i), items.get(j)));
					}
				}
			}

			List<String> pairs = new ArrayList<String>(distance.keySet());
			Collections.sort(pairs, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return distance.get(o1).compareTo(distance.get(o2));
				}
			});
			String closestItems = pairs.get(0);
			Double shortestDistance = distance.get(closestItems);

			if (shortestDistance.doubleValue() > cutOff) {
				break;
			}

			distanceList.add(shortestDistance);

			String[] tags = closestItems.split("--");
			items.remove(tags[0]);
			items.remove(tags[1]);

			items.add(String.format("%s=%s", tags[0], tags[1]));

			previousDistance = new HashMap<String, Double>(distance);
		}

		return items;
	}

	protected Double getAverageDistance(String tagA, String tagB) {
		if (tagA.equals(tagB)) {
			return Double.valueOf(0d);
		}
		String[] itemsA = tagA.split("=");
		String[] itemsB = tagB.split("=");
		double count = 0d;
		double sum = 0d;
		for (String ta : itemsA) {
			for (String tb : itemsB) {
				sum += matrix.get(ta).get(tb).doubleValue();
				count += 1d;
			}
		}
		return Double.valueOf(sum / count);
	}

}