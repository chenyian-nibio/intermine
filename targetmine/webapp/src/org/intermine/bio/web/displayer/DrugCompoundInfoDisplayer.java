package org.intermine.bio.web.displayer;

import javax.servlet.http.HttpServletRequest;

import org.intermine.api.InterMineAPI;
import org.intermine.web.displayer.ReportDisplayer;
import org.intermine.web.logic.config.ReportDisplayerConfig;
import org.intermine.web.logic.results.ReportObject;

/**
 * At the moment, this class is the same as GenericReportDisplayer.java. 
 * Just for preventing to call the same displayer in the DrugCompound class which will cause a problem.
 * 
 * @author chenyian
 *
 */
public class DrugCompoundInfoDisplayer extends ReportDisplayer {

	public DrugCompoundInfoDisplayer(ReportDisplayerConfig config, InterMineAPI im) {
		super(config, im);
	}

	@Override
	public void display(HttpServletRequest request, ReportObject reportObject) {

	}

}
