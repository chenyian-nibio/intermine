package org.intermine.bio.web.logic;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionMessage;
import org.intermine.api.InterMineAPI;
import org.intermine.api.profile.InterMineBag;
import org.intermine.api.profile.Profile;
import org.intermine.api.query.PathQueryExecutor;
import org.intermine.api.results.ExportResultsIterator;
import org.intermine.api.results.ResultElement;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.OrderDirection;
import org.intermine.pathquery.PathQuery;
import org.intermine.web.logic.bag.BagConverter;
import org.intermine.web.logic.config.WebConfig;

/**
 * 
 * @author chenyian
 *
 * Used for Protein class, modified from the OrthologueConverter.java
 */
public class ProteinOrthologConverter extends BagConverter {

    private Model model;

	public ProteinOrthologConverter(InterMineAPI im, WebConfig webConfig) {
        super(im, webConfig);
        model = im.getModel();
	}

    private PathQuery constructPathQuery(String organismName) {
        PathQuery q = new PathQuery(model);

        if (StringUtils.isNotEmpty(organismName)) {
            q.addConstraint(Constraints.eq("Protein.orthologProteins.organism.shortName",
                organismName));
        }

        return q;
    }
    
	@Override
	public ActionMessage getActionMessage(String externalids, int convertedSize, String type,
			String parameters) throws ObjectStoreException, UnsupportedEncodingException {
        if (StringUtils.isEmpty(parameters)) {
            return null;
        }

        PathQuery q = new PathQuery(model);

        // add columns to the output
        q.addViewSpaceSeparated("Protein.primaryIdentifier "
                + "Protein.organism.shortName "
                + "Protein.orthologProteins.primaryIdentifier "
                + "Protein.orthologProteins.organism.shortName ");

        // organism
        q.addConstraint(Constraints.lookup("Protein.organism", parameters, ""));

        // if the XML is too long, the link generates "HTTP Error 414 - Request URI too long"
        if (externalids.length() < 4000) {
            q.addConstraint(Constraints.lookup("Protein.orthologProteins", externalids, ""));
        }

        String query = q.toXml(PathQuery.USERPROFILE_VERSION);
        String encodedurl = URLEncoder.encode(query, "UTF-8");

        String[] values = new String[] {
            String.valueOf(convertedSize),
            parameters,
            String.valueOf(externalids.split(",").length),
            type,
            encodedurl };
        ActionMessage am = new ActionMessage("portal.orthologues", values);
        return am;
	}

	@Override
	public List<Integer> getConvertedObjectIds(Profile profile, String bagType,
			List<Integer> bagList, String constraintValue) throws ObjectStoreException {
        PathQuery pathQuery = constructPathQuery(constraintValue);
        pathQuery.addConstraint(Constraints.inIds("Protein", bagList));
        pathQuery.addView("Protein.orthologProteins.id");
        PathQueryExecutor executor = im.getPathQueryExecutor(profile);
        ExportResultsIterator it = executor.execute(pathQuery);
        List<Integer> ids = new ArrayList<Integer>();
        while (it.hasNext()) {
            List<ResultElement> row = it.next();
            ids.add((Integer) row.get(0).getField());
        }
        return ids;
	}

	@Override
	public Map<String, String> getCounts(Profile profile, InterMineBag bag)
			throws ObjectStoreException {
        PathQuery pathQuery = constructPathQuery(null);
        pathQuery.addConstraint(Constraints.inIds("Protein", bag.getContentsAsIds()));
        pathQuery.addView("Protein.orthologProteins.organism.shortName");
        pathQuery.addView("Protein.orthologProteins.id");
        pathQuery.addOrderBy("Protein.orthologProteins.organism.shortName", OrderDirection.ASC);
        PathQueryExecutor executor = im.getPathQueryExecutor(profile);
        ExportResultsIterator it = executor.execute(pathQuery);
        Map<String, String> results = new LinkedHashMap<String, String>();
        while (it.hasNext()) {
            List<ResultElement> row = it.next();
            String homologue = (String) row.get(0).getField();
            String count = results.get(homologue);
            if (count == null) {
                count = "0";
            }
            int plusOne = Integer.parseInt(count);
            results.put(homologue, String.valueOf(++plusOne));
        }
        return results;
	}

}
