package org.intermine.bio.web.widget;

import java.util.List;

import org.intermine.api.profile.InterMineBag;
import org.intermine.bio.util.BioUtil;
import org.intermine.model.bio.Disease;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.Organism;
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
import org.intermine.web.logic.widget.EnrichmentWidgetLdr;

/**
 * 
 * @author chenyian
 *
 */
public class OmimDataLoader extends EnrichmentWidgetLdr {

	public OmimDataLoader(InterMineBag bag, ObjectStore os, String extraAttribute) {
		this.bag = bag;
		organisms = BioUtil.getOrganisms(os, bag, false);
		//  having attributes lowercase increases the chances the indexes will be used
		for (String s : organisms) {
			organismsLower.add(s.toLowerCase());
		}
	}
	
	@Override
	public Query getQuery(String action, List<String> keys) {
		// classes for FROM clause
		QueryClass qcGene = new QueryClass(Gene.class);
		QueryClass qcDisease = new QueryClass(Disease.class);
		QueryClass qcOrganism = new QueryClass(Organism.class);

		// fields for SELECT clause
		QueryField qfGeneId = new QueryField(qcGene, "id");
		QueryField qfOrganismName = new QueryField(qcOrganism, "name");
		QueryField qfOmimId = new QueryField(qcDisease, "omimId");
		QueryField qfOmimTitle = new QueryField(qcDisease, "title");
//		QueryField qfPrimaryIdentifier = new QueryField(qcGene, "primaryIdentifier");
		QueryField qfNcbiGeneNumber = new QueryField(qcGene, "ncbiGeneNumber");

		// constraints
		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		// constrain genes to be in subset of list the user selected
		if (keys != null) {
			cs.addConstraint(new BagConstraint(qfOmimId, ConstraintOp.IN, keys));
		}

		// constrain genes to be in list
		if (!action.startsWith("population")) {
			cs.addConstraint(new BagConstraint(qfGeneId, ConstraintOp.IN, bag.getOsb()));
		}

		// organism in our list
		QueryExpression qe = new QueryExpression(QueryExpression.LOWER, qfOrganismName);
		cs.addConstraint(new BagConstraint(qe, ConstraintOp.IN, organismsLower));

		// gene.organism = organism
		QueryObjectReference qor = new QueryObjectReference(qcGene, "organism");
		cs.addConstraint(new ContainsConstraint(qor, ConstraintOp.CONTAINS, qcOrganism));

		// gene.diseases = disease
		QueryCollectionReference qcr = new QueryCollectionReference(qcGene, "diseases");
		cs.addConstraint(new ContainsConstraint(qcr, ConstraintOp.CONTAINS, qcDisease));

		Query q = new Query();
		q.setDistinct(true);

		// from statement
		q.addFrom(qcGene);
		q.addFrom(qcDisease);
		q.addFrom(qcOrganism);

		// add constraints to query
		q.setConstraint(cs);

		// needed for the 'not analysed' number
		if (action.equals("analysed")) {
			q.addToSelect(qfGeneId);
			// export query
			// needed for export button on widget
		} else if (action.equals("export")) {
			q.addToSelect(qfOmimId);
			q.addToSelect(qfNcbiGeneNumber);
			q.addToOrderBy(qfOmimId);
			// total queries
			// needed for enrichment calculations
		} else if (action.endsWith("Total")) {
			q.addToSelect(qfGeneId);
			Query subQ = q;
			q = new Query();
			q.addFrom(subQ);
			q.addToSelect(new QueryFunction()); // gene count
			// needed for enrichment calculations
		} else {
			q.addToSelect(qfOmimId);
			q.addToGroupBy(qfOmimId);
			q.addToSelect(new QueryFunction()); // gene count
			if (action.equals("sample")) {
				q.addToSelect(qfOmimTitle);
				q.addToGroupBy(qfOmimTitle);
			}
		}
		return q;
	}

	
	
}
