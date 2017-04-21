package org.intermine.bio.web.displayer;

import javax.servlet.http.HttpServletRequest;

import org.intermine.api.InterMineAPI;
import org.intermine.model.InterMineObject;
import org.intermine.web.displayer.ReportDisplayer;
import org.intermine.web.logic.config.ReportDisplayerConfig;
import org.intermine.web.logic.results.ReportObject;

public class ChemblAlternateFormDisplayer extends ReportDisplayer {

	public ChemblAlternateFormDisplayer(ReportDisplayerConfig config, InterMineAPI im) {
		super(config, im);
	}

	@Override
	public void display(HttpServletRequest request, ReportObject reportObject) {
		InterMineObject imo = (InterMineObject) reportObject.getObject();
		try {
			InterMineObject parent = (InterMineObject) imo.getFieldValue("parent");
			if (parent != null) {
				request.setAttribute("parentCompound", parent);
			} else {
				request.setAttribute("parentCompound", imo);
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}


}
