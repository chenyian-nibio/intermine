package org.intermine.bio.web.displayer;

import java.util.HashMap;
import java.util.HashSet;
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

	@Override
	public void display(HttpServletRequest request, ReportObject reportObject) {
		// TODO Auto-generated method stub
		InterMineObject imo = (InterMineObject) reportObject.getObject();
		try {
			Set<InterMineObject> features = (Set<InterMineObject>) imo.getFieldValue("features");
			Map<String, Set<InterMineObject>> featureMap = new HashMap<String, Set<InterMineObject>>();
			for (InterMineObject fe : features) {
				String type = (String) fe.getFieldValue("type");
				String subs = SUBSECTIONS.get(type);
				if (null == featureMap.get(subs)) {
					featureMap.put(subs, new HashSet<InterMineObject>());
				}
				featureMap.get(subs).add(fe);
			}
			request.setAttribute("featureMap", featureMap);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
