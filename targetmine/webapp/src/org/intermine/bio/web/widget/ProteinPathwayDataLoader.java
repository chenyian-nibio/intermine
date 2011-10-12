package org.intermine.bio.web.widget;

import java.util.List;

import org.intermine.api.profile.InterMineBag;
import org.intermine.bio.util.BioUtil;
import org.intermine.model.bio.DataSet;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.Organism;
import org.intermine.model.bio.Pathway;
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
import org.intermine.objectstore.query.QueryValue;
import org.intermine.objectstore.query.SimpleConstraint;
import org.intermine.web.logic.widget.EnrichmentWidgetLdr;

public class ProteinPathwayDataLoader extends EnrichmentWidgetLdr {

	private String dataset;

	public ProteinPathwayDataLoader(InterMineBag bag, ObjectStore os, String extraAttribute) {
		this.bag = bag;
		organisms = BioUtil.getOrganisms(os, bag, false);
		//  having attributes lowercase increases the chances the indexes will be used
		for (String s : organisms) {
			organismsLower.add(s.toLowerCase());
		}
		dataset = extraAttribute;
	}

	@Override
	public Query getQuery(String action, List<String> keys) {
		// classes for FROM clause
		QueryClass qcProtein = new QueryClass(Protein.class);
		QueryClass qcGene = new QueryClass(Gene.class);
		QueryClass qcPathway = new QueryClass(Pathway.class);
		QueryClass qcOrganism = new QueryClass(Organism.class);

		// fields for SELECT clause
//		QueryField qfGeneId = new QueryField(qcGene, "id");
		QueryField qfProteinId = new QueryField(qcProtein, "id");
		QueryField qfOrganismName = new QueryField(qcOrganism, "name");
		QueryField qfPathwayId = new QueryField(qcPathway, "identifier");
		QueryField qfPathwayName = new QueryField(qcPathway, "name");
//		QueryField qfPrimaryIdentifier = new QueryField(qcGene, "primaryIdentifier");
		QueryField qfPrimaryIdentifier = new QueryField(qcProtein, "primaryIdentifier");

		// constraints
		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		// constrain genes to be in subset of list the user selected
		if (keys != null) {
			cs.addConstraint(new BagConstraint(qfPathwayId, ConstraintOp.IN, keys));
		}

		// constrain genes to be in list
		if (!action.startsWith("population")) {
			cs.addConstraint(new BagConstraint(qfProteinId, ConstraintOp.IN, bag.getOsb()));
		}

		// organism in our list
		QueryExpression qe = new QueryExpression(QueryExpression.LOWER, qfOrganismName);
		cs.addConstraint(new BagConstraint(qe, ConstraintOp.IN, organismsLower));

		// gene.organism = organism
		QueryObjectReference qor = new QueryObjectReference(qcGene, "organism");
		cs.addConstraint(new ContainsConstraint(qor, ConstraintOp.CONTAINS, qcOrganism));

		// gene.publication = pathway
		QueryCollectionReference qcr = new QueryCollectionReference(qcGene, "pathways");
		cs.addConstraint(new ContainsConstraint(qcr, ConstraintOp.CONTAINS, qcPathway));

		// constrain on proteins?
		QueryCollectionReference c10 = new QueryCollectionReference(qcProtein, "genes");
		cs.addConstraint(new ContainsConstraint(c10, ConstraintOp.CONTAINS, qcGene));

		Query q = new Query();
		q.setDistinct(true);

		// from statement
		q.addFrom(qcProtein);
		q.addFrom(qcGene);
		q.addFrom(qcPathway);
		q.addFrom(qcOrganism);

		// constraint for dataset
		if (!dataset.equals("All datasets")) {
			String dataSetName = "";
			if (dataset.equals("KEGG")){
				dataSetName = "KEGG pathways data set";
			} else if (dataset.equals("Reactome")){
				dataSetName = "Reactome data set";
			} else if (dataset.equals("NCI")){
				dataSetName = "NCI-Nature data set";
			}
			
            QueryClass qcDataset = new QueryClass(DataSet.class);
            QueryField qfDataset = new QueryField(qcDataset, "name");

            QueryCollectionReference qcr2 = new QueryCollectionReference(qcPathway, "dataSets");
            cs.addConstraint(new ContainsConstraint(qcr2, ConstraintOp.CONTAINS, qcDataset));

            QueryExpression qe2 = new QueryExpression(QueryExpression.LOWER, qfDataset);
            cs.addConstraint(new SimpleConstraint(qe2, ConstraintOp.EQUALS,
                                                  new QueryValue(dataSetName.toLowerCase())));

            q.addFrom(qcDataset);
		}

		// add constraints to query
		q.setConstraint(cs);

		// needed for the 'not analysed' number
		if (action.equals("analysed")) {
			q.addToSelect(qfProteinId);
			// export query
			// needed for export button on widget
		} else if (action.equals("export")) {
			q.addToSelect(qfPathwayId);
			q.addToSelect(qfPrimaryIdentifier);
			q.addToOrderBy(qfPathwayId);
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
			q.addToSelect(qfPathwayId);
			q.addToGroupBy(qfPathwayId);
			q.addToSelect(new QueryFunction()); // gene count
			if (action.equals("sample")) {
				q.addToSelect(qfPathwayName);
				q.addToGroupBy(qfPathwayName);
			}
		}
		return q;
	}
}
