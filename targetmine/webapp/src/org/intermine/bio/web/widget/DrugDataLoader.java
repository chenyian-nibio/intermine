package org.intermine.bio.web.widget;

import java.util.List;

import org.apache.log4j.Logger;
import org.intermine.api.profile.InterMineBag;
import org.intermine.bio.util.BioUtil;
import org.intermine.metadata.Model;
import org.intermine.model.bio.Organism;
import org.intermine.model.bio.Protein;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.query.BagConstraint;
import org.intermine.objectstore.query.ConstraintOp;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryCollectionReference;
import org.intermine.objectstore.query.QueryExpression;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.QueryFunction;
import org.intermine.objectstore.query.QueryObjectReference;
import org.intermine.objectstore.query.SimpleConstraint;
import org.intermine.web.logic.widget.EnrichmentWidgetLdr;

public class DrugDataLoader extends EnrichmentWidgetLdr {
	private static final Logger LOG = Logger.getLogger(DrugDataLoader.class);
    private Model model;

	public DrugDataLoader(InterMineBag bag, ObjectStore os, String extraAttribute) {
		this.bag = bag;
		organisms = BioUtil.getOrganisms(os, bag, false);
		//  having attributes lowercase increases the chances the indexes will be used
		for (String s : organisms) {
			organismsLower.add(s.toLowerCase());
		}
		model = os.getModel();
	}

	@Override
	public Query getQuery(String action, List<String> keys) {

		// classes for FROM clause
		QueryClass qcProtein = new QueryClass(Protein.class);
		QueryClass qcDrugProteinInteraction;
		QueryClass qcDrug;
		QueryClass qcOrganism = new QueryClass(Organism.class);

        try {
        	qcDrugProteinInteraction = new QueryClass(Class.forName(model.getPackageName() + ".DrugProteinInteraction"));
        	qcDrug = new QueryClass(Class.forName(model.getPackageName() + ".Drug"));
        } catch (ClassNotFoundException e) {
            LOG.error("Error rendering drug enrichment widget", e);
            // don't throw an exception, return NULL instead.  The widget will display 'no
            // results'. the javascript that renders widgets assumes a valid widget and thus
            // can't handle an exception thrown here.
            return null;
        }

		// fields for SELECT clause
		QueryField qfProteinId = new QueryField(qcProtein, "id");
		QueryField qfOrganismName = new QueryField(qcOrganism, "name");
		QueryField qfDrugId = new QueryField(qcDrug, "drugBankId");
		QueryField qfDrugName = new QueryField(qcDrug, "genericName");
		QueryField qfPrimaryAccession = new QueryField(qcProtein, "primaryAccession");

		// constraints
		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		cs.addConstraint(new SimpleConstraint(qfDrugId, ConstraintOp.IS_NOT_NULL));

		// constrain genes to be in subset of list the user selected
		if (keys != null) {
			cs.addConstraint(new BagConstraint(qfDrugId, ConstraintOp.IN, keys));
		}

		// constrain genes to be in list
		if (!action.startsWith("population")) {
			cs.addConstraint(new BagConstraint(qfProteinId, ConstraintOp.IN, bag.getOsb()));
		}

		// organism in our list
		QueryExpression qe = new QueryExpression(QueryExpression.LOWER, qfOrganismName);
		cs.addConstraint(new BagConstraint(qe, ConstraintOp.IN, organismsLower));

		// protein.organism = organism
		QueryObjectReference qor = new QueryObjectReference(qcProtein, "organism");
		cs.addConstraint(new ContainsConstraint(qor, ConstraintOp.CONTAINS, qcOrganism));

		// protein.drugs.drug = drug
		QueryCollectionReference qcr = new QueryCollectionReference(qcProtein, "drugs");
		cs.addConstraint(new ContainsConstraint(qcr, ConstraintOp.CONTAINS,
				qcDrugProteinInteraction));
		QueryObjectReference qordrug = new QueryObjectReference(qcDrugProteinInteraction, "drug");
		cs.addConstraint(new ContainsConstraint(qordrug, ConstraintOp.CONTAINS, qcDrug));

		Query q = new Query();
		q.setDistinct(true);

		// from statement
		q.addFrom(qcProtein);
		q.addFrom(qcDrugProteinInteraction);
		q.addFrom(qcDrug);
		q.addFrom(qcOrganism);

		// add constraints to query
		q.setConstraint(cs);

		// needed for the 'not analysed' number
		if (action.equals("analysed")) {
			q.addToSelect(qfProteinId);
			// export query
			// needed for export button on widget
		} else if (action.equals("export")) {
			q.addToSelect(qfDrugId);
			q.addToSelect(qfPrimaryAccession);
			q.addToOrderBy(qfDrugId);
			// total queries
			// needed for enrichment calculations
		} else if (action.endsWith("Total")) {
			q.addToSelect(qfProteinId);
			Query subQ = q;
			q = new Query();
			q.addFrom(subQ);
			q.addToSelect(new QueryFunction()); // gene count
			// needed for enrichment calculations
		} else {
			q.addToSelect(qfDrugId);
			q.addToGroupBy(qfDrugId);
			q.addToSelect(new QueryFunction()); // gene count
			if (action.equals("sample")) {
				q.addToSelect(qfDrugName);
				q.addToGroupBy(qfDrugName);
			}
		}
		return q;
	}
}
