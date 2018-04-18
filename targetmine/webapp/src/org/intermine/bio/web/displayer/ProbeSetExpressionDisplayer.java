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

public class ProbeSetExpressionDisplayer extends ReportDisplayer {
	
	public ProbeSetExpressionDisplayer(ReportDisplayerConfig config, InterMineAPI im) {
		super(config, im);
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	@Override
	public void display(HttpServletRequest request, ReportObject reportObject) {
		InterMineObject imo = (InterMineObject) reportObject.getObject();
		
		try {
			Set<InterMineObject> expressions = (Set<InterMineObject>) imo.getFieldValue("expressions");
			Map<InterMineObject, List<InterMineObject>> expressionMap = new HashMap<InterMineObject, List<InterMineObject>>();
			for (InterMineObject exp : expressions) {
				Float value= (Float) exp.getFieldValue("value");
				if (value > 0.5f) {
					InterMineObject platform = (InterMineObject) exp.getFieldValue("platform");
//					InterMineObject tissue = (InterMineObject) exp.getFieldValue("tissue");
					if (expressionMap.get(platform) == null) {
						expressionMap.put(platform, new ArrayList<InterMineObject>());
					}
					expressionMap.get(platform).add(exp);
				}
			}

			request.setAttribute("expressionMap", expressionMap);
			request.setAttribute("platformSet", expressionMap.keySet());
			
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
