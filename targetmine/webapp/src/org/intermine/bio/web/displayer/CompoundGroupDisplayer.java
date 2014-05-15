package org.intermine.bio.web.displayer;

import javax.servlet.http.HttpServletRequest;

import org.intermine.api.InterMineAPI;
import org.intermine.web.displayer.ReportDisplayer;
import org.intermine.web.logic.config.ReportDisplayerConfig;
import org.intermine.web.logic.results.ReportObject;

public class CompoundGroupDisplayer extends ReportDisplayer {

	public CompoundGroupDisplayer(ReportDisplayerConfig config, InterMineAPI im) {
		super(config, im);
	}

	@Override
	public void display(HttpServletRequest request, ReportObject reportObject) {
//		Profile profile = SessionMethods.getProfile(request.getSession());
//		reportObject.getId();
	}

//	private boolean getInteractProteins(String id, Profile profile) {
//		PathQuery q = new PathQuery(im.getModel());
//		PathQueryExecutor executor = im.getPathQueryExecutor(profile);
//
//		q.addView("CompoundGroup.compounds.targetProteins.protein.primaryAccession");
//		q.addConstraint(Constraints.eq("CompoundGroup.id", id));
//
//		ExportResultsIterator it;
//		try {
//			it = executor.execute(q);
//		} catch (ObjectStoreException e) {
//			return false;
//		}
//        while (it.hasNext()) {
//            List<ResultElement> row = it.next();
//            String identifier = (String) row.get(0).getField();
//            String secondaryIdentifier = (String) row.get(1).getField();
//            if (!StringUtils.isEmpty(identifier)) {
//                orthologues.add(identifier);
//            } else if (!StringUtils.isEmpty(secondaryIdentifier)) {
//                orthologues.add(secondaryIdentifier);
//            }
//        }
//
//		return it.hasNext();
//	}

}
