package org.intermine.bio.web.displayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.intermine.api.InterMineAPI;
import org.intermine.model.InterMineObject;
import org.intermine.model.bio.Compound;
import org.intermine.model.bio.CompoundProteinInteraction;
import org.intermine.model.bio.Protein;
import org.intermine.web.displayer.ReportDisplayer;
import org.intermine.web.logic.config.ReportDisplayerConfig;
import org.intermine.web.logic.results.ReportObject;

public class CompoundGroupDisplayer extends ReportDisplayer {
	protected static final Logger LOG = Logger.getLogger(CompoundGroupDisplayer.class);

	public CompoundGroupDisplayer(ReportDisplayerConfig config, InterMineAPI im) {
		super(config, im);
	}

	@Override
	public void display(HttpServletRequest request, ReportObject reportObject) {
		InterMineObject compoundGroup = reportObject.getObject();
		Map<String, Protein> proteins = new HashMap<String, Protein>();
		Map<String, Set<String>> dataSetMap = new HashMap<String, Set<String>>();
		try {
			@SuppressWarnings("unchecked")
			Set<Compound> compounds =  (Set<Compound>) compoundGroup.getFieldValue("compounds");
			for (Compound compound : compounds) {
				Set<CompoundProteinInteraction> targetProteins = compound.getTargetProteins();
				for (CompoundProteinInteraction interaction : targetProteins) {
					proteins.put(interaction.getProtein().getPrimaryIdentifier(), interaction.getProtein());
					if (dataSetMap.get(interaction.getProtein().getPrimaryIdentifier()) == null) {
						dataSetMap.put(interaction.getProtein().getPrimaryIdentifier(), new HashSet<String>());
					}
					dataSetMap.get(interaction.getProtein().getPrimaryIdentifier()).add(interaction.getDataSet().getName().substring(0, 1));
				}
			}
		} catch (IllegalAccessException e) {
			LOG.info(e.getMessage());
		}
		request.setAttribute("proteins", proteins.values());
//		HashMap<String, String> dataSets = new HashMap<String, String>();
//		for (String id : dataSetMap.keySet()) {
//			List<String> arrayList = new ArrayList<String>(dataSetMap.get(id));
//			Collections.sort(arrayList);
//			dataSets.put(id, StringUtils.join(arrayList, ", "));
//		}
		request.setAttribute("dataSets", dataSetMap);
		
		Map<String,String> colorMap = new HashMap<String, String>();
		colorMap.put("B", "#336699");
		colorMap.put("C", "#66CCCC");
		colorMap.put("D", "#DD33FF");
		colorMap.put("L", "#999999");
		Map<String,String> nameMap = new HashMap<String, String>();
		nameMap.put("B", "BioAssay");
		nameMap.put("C", "ChEMBL");
		nameMap.put("D", "DrugBank");
		nameMap.put("L", "Ligand Expo");
		
		request.setAttribute("colorMap", colorMap);
		request.setAttribute("nameMap", nameMap);
	}

}
