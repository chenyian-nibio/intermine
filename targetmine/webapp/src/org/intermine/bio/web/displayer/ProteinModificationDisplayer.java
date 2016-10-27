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
			@SuppressWarnings("unchecked")
			Set<InterMineObject> modifications = (Set<InterMineObject>) imo.getFieldValue("modifications");
			Map<String, List<InterMineObject>> modificationMap = new HashMap<String, List<InterMineObject>>();
			boolean containPspData = false;
			for (InterMineObject mod : modifications) {
				String type = (String) mod.getFieldValue("type");
				if (null == modificationMap.get(type)) {
					modificationMap.put(type, new ArrayList<InterMineObject>());
				}
				modificationMap.get(type).add(mod);
				
				if (!containPspData) {
					@SuppressWarnings("unchecked")
					Set<InterMineObject> dataSets = (Set<InterMineObject>) mod.getFieldValue("dataSets");
					for (InterMineObject ds : dataSets) {
						String title = (String) ds.getFieldValue("name");
						if (title.equals("PhosphoSitePlus")) {
							containPspData = true;
							break;
						}
					}
				}
			}
			for (String key : modificationMap.keySet()) {
				modificationMap.put(key, sortByPosition(modificationMap.get(key)));
			}
			List<String> typeList = new ArrayList<String>(modificationMap.keySet());
			Collections.sort(typeList);
			request.setAttribute("modificationMap", modificationMap);
			request.setAttribute("typeList", typeList);
			
			if (containPspData) {
				request.setAttribute("pageNote", true);
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param list A list of Modification instance
	 * @return
	 */
	private List<InterMineObject> sortByPosition(List<InterMineObject> list) {
		Collections.sort(list, new Comparator<InterMineObject>() {

			@Override
			public int compare(InterMineObject o1, InterMineObject o2) {
				try {
					Integer pos1 = (Integer) o1.getFieldValue("position");
					Integer pos2 = (Integer) o2.getFieldValue("position");
					if (pos1 == null) {
						return 1;
					}
					if (pos2 == null) {
						return -1;
					}
					return pos1.compareTo(pos2);
					
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				return 0;
			}

		});
		return list;
	}
}
