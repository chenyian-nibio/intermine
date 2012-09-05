package org.intermine.bio.web.widget;

import java.util.List;

import org.intermine.api.profile.InterMineBag;
import org.intermine.bio.util.BioUtil;
import org.intermine.model.bio.Organism;
import org.intermine.model.bio.Protein;
import org.intermine.model.bio.Publication;
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

/**
 * Publication data loader for protein list. Modified from PublicationLdr.
 * 
 * @author chenyian
 */
@Deprecated
public class ProteinPublicationDataLoader extends EnrichmentWidgetLdr {

	/**
	 * Constructor
	 * 
	 * @param bag
	 *            the bag
	 * @param os
	 *            the ObjectStore
	 * @param extraAttribute
	 *            an extra attribute, probably organism
	 */
	public ProteinPublicationDataLoader(InterMineBag bag, ObjectStore os, String extraAttribute) {
		this.bag = bag;
		organisms = BioUtil.getOrganisms(os, bag, false);
		//  having attributes lowercase increases the chances the indexes will be used
		for (String s : organisms) {
			organismsLower.add(s.toLowerCase());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Query getQuery(String action, List<String> keys) {

		// classes for FROM clause
		QueryClass qcProtein = new QueryClass(Protein.class);
		QueryClass qcPublication = new QueryClass(Publication.class);
		QueryClass qcOrganism = new QueryClass(Organism.class);

		// fields for SELECT clause
		QueryField qfProteinId = new QueryField(qcProtein, "id");
		QueryField qfOrganismName = new QueryField(qcOrganism, "name");
		QueryField qfPubmedId = new QueryField(qcPublication, "pubMedId");
		QueryField qfPubTitle = new QueryField(qcPublication, "title");
		QueryField qfPrimaryAccession = new QueryField(qcProtein, "primaryAccession");

		// constraints
		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		cs.addConstraint(new SimpleConstraint(qfPubmedId, ConstraintOp.IS_NOT_NULL));

		// constrain genes to be in subset of list the user selected
		if (keys != null) {
			cs.addConstraint(new BagConstraint(qfPubmedId, ConstraintOp.IN, keys));
		}

		// constrain genes to be in list
		if (!action.startsWith("population")) {
			cs.addConstraint(new BagConstraint(qfProteinId, ConstraintOp.IN, bag.getOsb()));
		}

		// organism in our list
		QueryExpression qe = new QueryExpression(QueryExpression.LOWER, qfOrganismName);
		cs.addConstraint(new BagConstraint(qe, ConstraintOp.IN, organismsLower));

		// gene.organism = organism
		QueryObjectReference qor = new QueryObjectReference(qcProtein, "organism");
		cs.addConstraint(new ContainsConstraint(qor, ConstraintOp.CONTAINS, qcOrganism));

		// gene.publication = publication
		QueryCollectionReference qcr = new QueryCollectionReference(qcProtein, "publications");
		cs.addConstraint(new ContainsConstraint(qcr, ConstraintOp.CONTAINS, qcPublication));

		Query q = new Query();
		q.setDistinct(true);

		// from statement
		q.addFrom(qcProtein);
		q.addFrom(qcPublication);
		q.addFrom(qcOrganism);

		// add constraints to query
		q.setConstraint(cs);

		// needed for the 'not analysed' number
		if (action.equals("analysed")) {
			q.addToSelect(qfProteinId);
			// export query
			// needed for export button on widget
		} else if (action.equals("export")) {
			q.addToSelect(qfPubmedId);
			q.addToSelect(qfPrimaryAccession);
			q.addToOrderBy(qfPubmedId);
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
			q.addToSelect(qfPubmedId);
			q.addToGroupBy(qfPubmedId);
			q.addToSelect(new QueryFunction()); // gene count
			if (action.equals("sample")) {
				q.addToSelect(qfPubTitle);
				q.addToGroupBy(qfPubTitle);
			}
		}
		return q;
	}

}
