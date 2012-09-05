package org.intermine.bio.web.widget;

import java.util.List;

import org.apache.log4j.Logger;
import org.intermine.api.profile.InterMineBag;
import org.intermine.bio.util.BioUtil;
import org.intermine.metadata.Model;
import org.intermine.model.bio.Gene;
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
import org.intermine.web.logic.widget.EnrichmentWidgetLdr;

/***
 * 
 * @author chenyian
 * 
 */
public class StructuralDomainDataLoader extends EnrichmentWidgetLdr {
	private static final Logger LOG = Logger.getLogger(StructuralDomainDataLoader.class);
	private Model model;

	public StructuralDomainDataLoader(InterMineBag bag, ObjectStore os, String extraAttribute) {
		this.bag = bag;
		organisms = BioUtil.getOrganisms(os, bag, false);
		for (String s : organisms) {
			organismsLower.add(s.toLowerCase());
		}
		model = os.getModel();
	}

	@Override
	public Query getQuery(String action, List<String> keys) {
		
		String bagType = bag.getType();

		// classes for FROM clause
		QueryClass qcGene = new QueryClass(Gene.class);
		QueryClass qcProtein = new QueryClass(Protein.class);
		QueryClass qcStructuralDomainRegion;
		QueryClass qcCathChild;
		QueryClass qcCathParent;
		QueryClass qcOrganism = new QueryClass(Organism.class);

		try {
			qcStructuralDomainRegion = new QueryClass(Class.forName(model.getPackageName()
					+ ".StructuralDomainRegion"));
			qcCathParent = new QueryClass(Class.forName(model.getPackageName()
					+ ".CathClassification"));
			qcCathChild = new QueryClass(Class.forName(model.getPackageName()
					+ ".CathClassification"));
		} catch (ClassNotFoundException e) {
			LOG.error("Error rendering structural domain enrichment widget", e);
			// don't throw an exception, return NULL instead. The widget will display 'no
			// results'. the javascript that renders widgets assumes a valid widget and thus
			// can't handle an exception thrown here.
			return null;
		}
		
		// fields for SELECT clause
		QueryField qfId = null;
		QueryField qfDisplayId = null;
		QueryField qfOrganismName = new QueryField(qcOrganism, "name");
		QueryField qfCathCode = new QueryField(qcCathParent, "cathCode");
		QueryField qfCathDescription = new QueryField(qcCathParent, "description");

		if ("Protein".equals(bagType)) {
			qfId = new QueryField(qcProtein, "id");
			qfDisplayId = new QueryField(qcProtein, "primaryAccession");
		} else if ("Gene".equals(bagType)) {
			qfId = new QueryField(qcGene, "id");
			qfDisplayId = new QueryField(qcGene, "ncbiGeneNumber");
		}

		// constraints
		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

//		 cs.addConstraint(new SimpleConstraint(qfCathCode, ConstraintOp.IS_NOT_NULL));

		// constrain genes to be in subset of list the user selected
		if (keys != null) {
			cs.addConstraint(new BagConstraint(qfCathCode, ConstraintOp.IN, keys));
		}

		// constrain genes to be in list
		if (!action.startsWith("population")) {
			cs.addConstraint(new BagConstraint(qfId, ConstraintOp.IN, bag.getOsb()));
		}

		// organism in our list
		QueryExpression qe = new QueryExpression(QueryExpression.LOWER, qfOrganismName);
		cs.addConstraint(new BagConstraint(qe, ConstraintOp.IN, organismsLower));

		// protein.organism = organism
		QueryObjectReference qor = new QueryObjectReference(qcProtein, "organism");
		cs.addConstraint(new ContainsConstraint(qor, ConstraintOp.CONTAINS, qcOrganism));

		// protein.structuralDomains.cathClassification.parents
		QueryCollectionReference qcr = new QueryCollectionReference(qcProtein, "structuralDomains");
		cs.addConstraint(new ContainsConstraint(qcr, ConstraintOp.CONTAINS,
				qcStructuralDomainRegion));
		QueryObjectReference qorChild = new QueryObjectReference(qcStructuralDomainRegion,
				"cathClassification");
		cs.addConstraint(new ContainsConstraint(qorChild, ConstraintOp.CONTAINS, qcCathChild));
		QueryCollectionReference qcrParent = new QueryCollectionReference(qcCathChild, "parents");
		cs.addConstraint(new ContainsConstraint(qcrParent, ConstraintOp.CONTAINS, qcCathParent));

        if ("Gene".equals(bagType)) {
            QueryCollectionReference qcr2 = new QueryCollectionReference(qcGene, "proteins");
            cs.addConstraint(new ContainsConstraint(qcr2, ConstraintOp.CONTAINS, qcProtein));
        }
        
		Query q = new Query();
		q.setDistinct(true);

		// from statement
        if ("Gene".equals(bagType)) {
        	q.addFrom(qcGene);
        }
		q.addFrom(qcProtein);
		q.addFrom(qcStructuralDomainRegion);
		q.addFrom(qcCathChild);
		q.addFrom(qcCathParent);
		q.addFrom(qcOrganism);

		// add constraints to query
		q.setConstraint(cs);

		// needed for the 'not analysed' number
		if (action.equals("analysed")) {
			q.addToSelect(qfId);
			// export query
			// needed for export button on widget
		} else if (action.equals("export")) {
			q.addToSelect(qfCathCode);
			q.addToSelect(qfDisplayId);
			q.addToOrderBy(qfCathCode);
			// total queries
			// needed for enrichment calculations
		} else if (action.endsWith("Total")) { // n and N
			q.addToSelect(qfId);
			Query subQ = q;
			q = new Query();
			q.addFrom(subQ);
			q.addToSelect(new QueryFunction()); // gene count
			// needed for enrichment calculations
		} else { // k and M
			
            /*
            the first query gets all of the protein --> cath parent relationships unique
            the second query then counts the proteins per each cath parent
             */

            // subquery
            Query subq = q;
            subq.addToSelect(qfId);
            subq.addToSelect(qfCathCode);

            QueryField qfName = null;
            if (action.equals("sample")) {
                subq.addToSelect(qfCathDescription);
                qfName = new QueryField(subq, qfCathDescription);
            }

            // needed so we can select this field in the parent query
            QueryField qfIdentifier = new QueryField(subq, qfCathCode);

            // main query
            q = new Query();
            q.setDistinct(false);
            q.addFrom(subq);
            q.addToSelect(qfIdentifier);
            q.addToSelect(new QueryFunction());
            if (action.equals("sample")) {
                q.addToSelect(qfName);
                q.addToGroupBy(qfName);
            }
            q.addToGroupBy(qfIdentifier);

		}
		return q;
	}
}
