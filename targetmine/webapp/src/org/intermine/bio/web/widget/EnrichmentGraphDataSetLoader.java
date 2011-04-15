package org.intermine.bio.web.widget;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.intermine.api.profile.InterMineBag;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.web.logic.widget.DataSetLdr;
import org.intermine.web.logic.widget.EnrichmentWidgetLdr;
import org.intermine.web.logic.widget.ErrorCorrection;
import org.intermine.web.logic.widget.Hypergeometric;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;

public abstract class EnrichmentGraphDataSetLoader implements DataSetLdr {

	private static final Logger LOG = Logger.getLogger(EnrichmentGraphDataSetLoader.class);

	private static String FG_LABEL = "List";
	private static String BG_LABEL = "Background";

	public static int DISPLAY_GROUP_NUMBER = 10;

	private DefaultCategoryDataset dataSet;
	private Results results;
	private int widgetTotal = 0;

	@SuppressWarnings("unchecked")
	protected void buildDataSets(InterMineBag bag, ObjectStore os, EnrichmentWidgetLdr dataLoader,
			int displayGroupNumber) {

		List<Map> resultMaps = statsCalc(os, dataLoader, bag, 1.0, null);

		if (!resultMaps.isEmpty()) {
			widgetTotal = ((Integer) (resultMaps.get(3)).get("widgetTotal")).intValue();

			int count = 0;

			Map<String, BigDecimal> sortedMap = resultMaps.get(0);
			Map<String, Integer[]> graphMap = resultMaps.get(4);
			Map<String, String> idMap = resultMaps.get(2);

			Set<String> keySet = sortedMap.keySet();
			Iterator<String> iterator = keySet.iterator();

			dataSet = new DefaultCategoryDataset();

			while (iterator.hasNext() && count < displayGroupNumber) {
				String key = iterator.next();
				Integer[] knMN = graphMap.get(key);
				String label = idMap.get(key);
				// shorten the label. difficult to do constraint further.
//				label = (label.length() > 16) ? label.substring(0, 12) + "..." : label;
				label = (label.length() > 20) ? label.substring(0, 16) + "..." : label;
				double fg = Math.round(knMN[0].doubleValue() / knMN[1].doubleValue() * 1000) / 10.0;
				double bg = Math.round(knMN[2].doubleValue() / knMN[3].doubleValue() * 1000) / 10.0;

				dataSet.addValue(fg, FG_LABEL, label);
				dataSet.addValue(bg, BG_LABEL, label);
				// LOG.info(String.format("k:%d, n:%d, N:%d, N:%d, pathway: %s", knMN, key));

				count++;
			}

		} else {
			LOG.info("resultMaps isEmpty !");
		}

	}

	// modified from WidgetUtil.java
	private Map<String, List> statsCalcCache = new HashMap<String, List>();

	private List<Map> maps;

	/**
	 * Runs both queries and compares the results.
	 * 
	 * @param os
	 *            the object store
	 * @param ldr
	 *            the loader
	 * @param bag
	 *            the bag we are analysing
	 * @param maxValue
	 *            (TO BE REMOVED) maximum value to return - for display purposes only
	 * @param errorCorrection
	 *            (TO BE REMOVED) which error correction algorithm to use, Bonferroni or Benjamini
	 *            Hochberg or none
	 * @return array of three results maps
	 */
	public List<Map> statsCalc(ObjectStore os, EnrichmentWidgetLdr ldr, InterMineBag bag,
			Double maxValue, String errorCorrection) {

		maps = new ArrayList<Map>();

		int populationTotal = calcTotal(os, ldr, true); // objects annotated in database
		int sampleTotal = calcTotal(os, ldr, false); // objects annotated in bag
		int testCount = 0; // number of tests

		// sample query
		Query q = ldr.getSampleQuery(false);

		Results r = null;

		HashMap<String, Long> countMap = new HashMap<String, Long>();
		HashMap<String, String> idMap = new HashMap<String, String>();
		HashMap<String, BigDecimal> resultsMap = new HashMap<String, BigDecimal>();
		Map<String, Integer> dummy = new HashMap<String, Integer>();
		Map<String, BigDecimal> sortedMap = new LinkedHashMap<String, BigDecimal>();
		Map<String, Integer[]> graphMap = new HashMap<String, Integer[]>();

		// if the model has changed, the query might not be valid
		if (q != null) {
			r = os.execute(q, 20000, true, true, true);

			Iterator iter = r.iterator();

			while (iter.hasNext()) {

				// extract results
				ResultsRow rr = (ResultsRow) iter.next();

				// id of annotation item (eg. GO term)
				String id = (String) rr.get(0);

				// count of item
				Long count = (Long) rr.get(1);

				// id & count
				countMap.put(id, count);

				// id & label
				idMap.put(id, (String) rr.get(2));

			}

			// run population query
			List rAll = statsCalcCache.get(ldr.getPopulationQuery(false).toString());
			if (rAll == null) {
				rAll = os.execute(ldr.getPopulationQuery(false), 20000, true, true, true);
				rAll = new ArrayList(rAll);
				statsCalcCache.put(ldr.getPopulationQuery(false).toString(), rAll);
			}

			Iterator itAll = rAll.iterator();

			// loop through results again to calculate p-values
			while (itAll.hasNext()) {

				ResultsRow rrAll = (ResultsRow) itAll.next();

				String id = (String) rrAll.get(0);
				testCount++;

				if (countMap.containsKey(id)) {

					Long countBag = countMap.get(id);
					Long countAll = (java.lang.Long) rrAll.get(1);

					// (k,n,M,N)
					int k = countBag.intValue();
					int n = sampleTotal;
					int bigM = countAll.intValue();
					int bigN = populationTotal;
					double p = Hypergeometric.calculateP(k, n, bigM, bigN);

					try {
						resultsMap.put(id, new BigDecimal(p));
						// LOG.info(String.format("k:%d, n:%d, N:%d, N:%d, pathway: %s", k, n, bigM,
						// bigN, id));
						graphMap.put(id, Arrays.asList(k, n, bigM, bigN).toArray(new Integer[4]));
					} catch (Exception e) {
						String msg = p + " isn't a double.  calculated for " + id + " using "
								+ " k: " + countBag + ", n: " + sampleTotal + ", M: " + countAll
								+ ", N: " + populationTotal + ".  k query: "
								+ ldr.getSampleQuery(false).toString() + ".  n query: "
								+ ldr.getSampleQuery(true).toString() + ".  M query: "
								+ ldr.getPopulationQuery(false).toString() + ".  N query: "
								+ ldr.getPopulationQuery(true).toString();

						throw new RuntimeException(msg, e);
					}
				}
			}

			if (resultsMap.isEmpty()) {
				// no results
				dummy.put("widgetTotal", new Integer(0));
			} else {
				// maybe this step is unnecessary?
				sortedMap = ErrorCorrection.adjustPValues(errorCorrection, resultsMap, maxValue,
						testCount);
				dummy.put("widgetTotal", new Integer(sampleTotal));
			}
		} else {
			// no results
			dummy.put("widgetTotal", new Integer(0));
		}

		maps.add(0, sortedMap);
		maps.add(1, countMap);
		maps.add(2, idMap);
		maps.add(3, dummy);
		maps.add(4, graphMap);

		// just for hasResults() 
		results = r;

		return maps;
	}

	private int calcTotal(ObjectStore os, EnrichmentWidgetLdr ldr, boolean calcTotal) {
		Query q = new Query();
		if (calcTotal) {
			q = ldr.getPopulationQuery(true);
		} else {
			q = ldr.getSampleQuery(true);
		}
		if (q == null) {
			// bad query, model probably changed. no results
			return 0;
		}
		Object[] o = os.executeSingleton(q).toArray();
		if (o.length == 0) {
			// no results
			return 0;
		}
		return ((java.lang.Long) o[0]).intValue();
	}

	@Override
	public Dataset getDataSet() {
		// LOG.info("get data set, row:" + dataSet.getRowCount() + ", column:"
		// + dataSet.getColumnCount());
		return dataSet;
	}

	@Override
	public int getWidgetTotal() {
		return widgetTotal;
	}

	@Override
	// Because the results are calculated, not directly queried,
	// the return value will not reflect the real results
	public Results getResults() {
		return results;
	}

	// public boolean hasResult() {
	// return (maps.get(0) != null || maps.get(0).size() > 0);
	// }
}
