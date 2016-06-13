package org.intermine.bio.web.displayer;

import java.util.ArrayList;
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

public class ProteinModificationDisplayer extends ReportDisplayer {

	public ProteinModificationDisplayer(ReportDisplayerConfig config, InterMineAPI im) {
		super(config, im);
	}

	@Override
	public void display(HttpServletRequest request, ReportObject reportObject) {
		InterMineObject imo = (InterMineObject) reportObject.getObject();
		try {
			Set<InterMineObject> modifications = (Set<InterMineObject>) imo.getFieldValue("modifications");
			Map<String, List<InterMineObject>> modificationMap = new HashMap<String, List<InterMineObject>>();
			for (InterMineObject mod : modifications) {
				String type = (String) mod.getFieldValue("type");
				if (null == modificationMap.get(type)) {
					modificationMap.put(type, new ArrayList<InterMineObject>());
				}
				InterMineObject feature = (InterMineObject) mod.getFieldValue("feature");
				modificationMap.get(type).add(feature);
			}
			for (String key : modificationMap.keySet()) {
				modificationMap.put(key, sortByRegion(modificationMap.get(key)));
			}
			List<String> typeList = new ArrayList<String>(modificationMap.keySet());
			Collections.sort(typeList);
			request.setAttribute("modificationMap", modificationMap);
			request.setAttribute("typeList", typeList);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private List<InterMineObject> sortByRegion(List<InterMineObject> list) {
		Collections.sort(list, new Comparator<InterMineObject>() {

			@Override
			public int compare(InterMineObject o1, InterMineObject o2) {
				try {
					Integer begin1 = (Integer) o1.getFieldValue("begin");
					Integer begin2 = (Integer) o2.getFieldValue("begin");
					if (begin1 == null) {
						return 1;
					}
					if (begin2 == null) {
						return -1;
					}
					return begin1.compareTo(begin2);
					
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				return 0;
			}

		});
		return list;
	}
}
