package org.intermine.bio.web.displayer;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.intermine.api.InterMineAPI;
import org.intermine.api.profile.Profile;
import org.intermine.api.query.PathQueryExecutor;
import org.intermine.api.results.ExportResultsIterator;
import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.PathQuery;
import org.intermine.web.displayer.ReportDisplayer;
import org.intermine.web.logic.config.ReportDisplayerConfig;
import org.intermine.web.logic.results.ReportObject;
import org.intermine.web.logic.session.SessionMethods;

/**
 * @author chenyian
 */
public class ProteinDomainDisplayer extends ReportDisplayer {
	protected static final Logger LOG = Logger.getLogger(ProteinDomainDisplayer.class);

	public ProteinDomainDisplayer(ReportDisplayerConfig config, InterMineAPI im) {
		super(config, im);
	}

	@Override
	public void display(HttpServletRequest request, ReportObject reportObject) {
		Profile profile = SessionMethods.getProfile(request.getSession());

		int proteinId = reportObject.getId();
//		LOG.info("Display protein domain for id: " + proteinId);

		if (hasProteinDomain(String.valueOf(proteinId), profile)) {
			request.setAttribute("hasProteinDomain", "true");
		}

		request.setAttribute("proteinId", proteinId);
	}

	private boolean hasProteinDomain(String proteinId, Profile profile) {
		PathQuery q = new PathQuery(im.getModel());
		PathQueryExecutor executor = im.getPathQueryExecutor(profile);

		q.addViews("Protein.proteinDomainRegions.originalId");
		q.addConstraint(Constraints.eq("Protein.id", proteinId));

		ExportResultsIterator result = executor.execute(q);

		return result.hasNext();
	}

}
