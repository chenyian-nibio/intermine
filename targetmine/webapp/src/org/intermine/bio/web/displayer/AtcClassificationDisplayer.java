package org.intermine.bio.web.displayer;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.intermine.api.InterMineAPI;
import org.intermine.model.InterMineObject;
import org.intermine.web.displayer.ReportDisplayer;
import org.intermine.web.logic.config.ReportDisplayerConfig;
import org.intermine.web.logic.results.ReportObject;

public class AtcClassificationDisplayer extends ReportDisplayer {

	public AtcClassificationDisplayer(ReportDisplayerConfig config, InterMineAPI im) {
		super(config, im);
	}

	@Override
	public void display(HttpServletRequest request, ReportObject reportObject) {
		// TODO Auto-generated method stub
		InterMineObject imo = (InterMineObject) reportObject.getObject();
		List<InterMineObject> allParents = getAllParents(imo);
		request.setAttribute("allParents", allParents);
	}
	
	private List<InterMineObject> getAllParents(InterMineObject imo) {
		List<InterMineObject> ret;
		try {
			InterMineObject parent = (InterMineObject) imo.getFieldValue("parent");
			if (parent != null) {
				ret = getAllParents(parent);
			} else {
				ret = new ArrayList<InterMineObject>();
			}
			ret.add(imo);
			return ret;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
