package org.intermine.bio.web.displayer;

import java.util.ArrayList;
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

public class TissueSampleDisplayer extends ReportDisplayer {
	
	public TissueSampleDisplayer(ReportDisplayerConfig config, InterMineAPI im) {
		super(config, im);
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	@Override
	public void display(HttpServletRequest request, ReportObject reportObject) {
		InterMineObject imo = (InterMineObject) reportObject.getObject();
		
		try {
			Set<InterMineObject> samples = (Set<InterMineObject>) imo.getFieldValue("samples");
			request.setAttribute("total", samples.size());
			
			Map<InterMineObject, List<InterMineObject>> platformMap = new HashMap<InterMineObject, List<InterMineObject>>();
			for (InterMineObject sample : samples) {
				InterMineObject platform = (InterMineObject) sample.getFieldValue("platform");
				if (platformMap.get(platform) == null) {
					platformMap.put(platform, new ArrayList<InterMineObject>());
				}
				platformMap.get(platform).add(sample);
			}

			request.setAttribute("platformMap", platformMap);
			request.setAttribute("platformSet", platformMap.keySet());
			
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
