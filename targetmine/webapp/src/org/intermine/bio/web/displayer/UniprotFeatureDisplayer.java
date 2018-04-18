package org.intermine.bio.web.displayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.intermine.api.InterMineAPI;
import org.intermine.model.InterMineObject;
import org.intermine.web.displayer.ReportDisplayer;
import org.intermine.web.logic.config.ReportDisplayerConfig;
import org.intermine.web.logic.results.ReportObject;

public class UniprotFeatureDisplayer extends ReportDisplayer {

	private static final Map<String, String> SUBSECTIONS = new HashMap<String, String>();
	private static final List<String> SUBSECTION_ORDER = Arrays.asList("Molecule processing", "Regions",
			"Sites", "Amino acid modifications");

	private static final List<String> TYPE_ORDER = Arrays.asList("initiator methionine",
			"signal peptide", "transit peptide", "propeptide", "chain", "peptide",
			"topological domain", "transmembrane region", "intramembrane region", "domain",
			"repeat", "calcium-binding region", "zinc finger region", "DNA-binding region",
			"nucleotide phosphate-binding region", "region of interest", "coiled-coil region",
			"short sequence motif", "compositionally biased region", "active site",
			"metal ion-binding site", "binding site", "site", "non-standard amino acid",
			"modified residue", "lipid moiety-binding region", "glycosylation site",
			"disulfide bond", "cross-link");

	static {
		SUBSECTIONS.put("initiator methionine", "Molecule processing");
		SUBSECTIONS.put("signal peptide", "Molecule processing");
		SUBSECTIONS.put("transit peptide", "Molecule processing");
		SUBSECTIONS.put("propeptide", "Molecule processing");
		SUBSECTIONS.put("chain", "Molecule processing");
		SUBSECTIONS.put("peptide", "Molecule processing");
		SUBSECTIONS.put("topological domain", "Regions");
		SUBSECTIONS.put("transmembrane region", "Regions");
		SUBSECTIONS.put("intramembrane region", "Regions");
		SUBSECTIONS.put("domain", "Regions");
		SUBSECTIONS.put("repeat", "Regions");
		SUBSECTIONS.put("calcium-binding region", "Regions");
		SUBSECTIONS.put("zinc finger region", "Regions");
		SUBSECTIONS.put("DNA-binding region", "Regions");
		SUBSECTIONS.put("nucleotide phosphate-binding region", "Regions");
		SUBSECTIONS.put("region of interest", "Regions");
		SUBSECTIONS.put("coiled-coil region", "Regions");
		SUBSECTIONS.put("short sequence motif", "Regions");
		SUBSECTIONS.put("compositionally biased region", "Regions");
		SUBSECTIONS.put("active site", "Sites");
		SUBSECTIONS.put("metal ion-binding site", "Sites");
		SUBSECTIONS.put("binding site", "Sites");
		SUBSECTIONS.put("site", "Sites");
		SUBSECTIONS.put("non-standard amino acid", "Amino acid modifications");
		SUBSECTIONS.put("modified residue", "Amino acid modifications");
		SUBSECTIONS.put("lipid moiety-binding region", "Amino acid modifications");
		SUBSECTIONS.put("glycosylation site", "Amino acid modifications");
		SUBSECTIONS.put("disulfide bond", "Amino acid modifications");
		SUBSECTIONS.put("cross-link", "Amino acid modifications");
	}

	public UniprotFeatureDisplayer(ReportDisplayerConfig config, InterMineAPI im) {
		super(config, im);
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	@Override
	public void display(HttpServletRequest request, ReportObject reportObject) {
		// TODO Auto-generated method stub
		InterMineObject imo = (InterMineObject) reportObject.getObject();
		try {
			Set<InterMineObject> features = (Set<InterMineObject>) imo.getFieldValue("features");
			Map<String, List<InterMineObject>> featureMap = new HashMap<String, List<InterMineObject>>();
			for (InterMineObject fe : features) {
				String type = (String) fe.getFieldValue("type");
				String subs = SUBSECTIONS.get(type);
				if (null == featureMap.get(subs)) {
					featureMap.put(subs, new ArrayList<InterMineObject>());
				}
				featureMap.get(subs).add(fe);
			}
			for (String key : featureMap.keySet()) {
				featureMap.put(key, sortByRegion(featureMap.get(key)));
			}
			request.setAttribute("featureMap", featureMap);
			request.setAttribute("subsectionOrder", SUBSECTION_ORDER);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<InterMineObject> sortByRegion(List<InterMineObject> list) {
		Collections.sort(list, new Comparator<InterMineObject>() {

			@Override
			public int compare(InterMineObject o1, InterMineObject o2) {
				try {
					Integer order1 = Integer.valueOf(TYPE_ORDER.indexOf((String) o1
							.getFieldValue("type")));
					Integer order2 = Integer.valueOf(TYPE_ORDER.indexOf((String) o2
							.getFieldValue("type")));
					if (order1.equals(order2)) {
						Integer begin1 = (Integer) o1.getFieldValue("start");
						Integer begin2 = (Integer) o2.getFieldValue("start");
						if (begin1 == null) {
							return 1;
						}
						if (begin2 == null) {
							return -1;
						}
						if (begin1.equals(begin2)) {
							Integer end1 = (Integer) o1.getFieldValue("end");
							Integer end2 = (Integer) o2.getFieldValue("end");
							if (end1 == null) {
								return 1;
							}
							if (end2 == null) {
								return -1;
							}
							return end1.compareTo(end2);
						} else {
							return begin1.compareTo(begin2);
						}
					} else {
						return order1.compareTo(order2);
					}

				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return 0;
			}

		});
		return list;
	}

}
