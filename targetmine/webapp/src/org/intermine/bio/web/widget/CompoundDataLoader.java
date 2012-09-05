package org.intermine.bio.web.widget;

import java.util.List;

import org.apache.log4j.Logger;
import org.intermine.api.profile.InterMineBag;
import org.intermine.bio.util.BioUtil;
import org.intermine.metadata.Model;
import org.intermine.model.bio.DataSet;
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
import org.intermine.objectstore.query.QueryValue;
import org.intermine.objectstore.query.SimpleConstraint;
import org.intermine.web.logic.widget.EnrichmentWidgetLdr;

public class CompoundDataLoader extends EnrichmentWidgetLdr {

	private static final Logger LOG = Logger.getLogger(CompoundDataLoader.class);
	private Model model;
	private String dataset;

	public CompoundDataLoader(InterMineBag bag, ObjectStore os, String extraAttribute) {
		this.bag = bag;
		organisms = BioUtil.getOrganisms(os, bag, false);
		// having attributes lowercase increases the chances the indexes will be used
		for (String s : organisms) {
			organismsLower.add(s.toLowerCase());
		}
		model = os.getModel();
		dataset = extraAttribute;
	}

	@Override
	public Query getQuery(String action, List<String> keys) {
		
		String bagType = bag.getType();

		// classes for FROM clause
		QueryClass qcGene = new QueryClass(Gene.class);
		QueryClass qcProtein = new QueryClass(Protein.class);
		QueryClass qcOrganism = new QueryClass(Organism.class);
		QueryClass qcCompoundProteinInteraction;
		QueryClass qcCompound;

		try {
			qcCompoundProteinInteraction = new QueryClass(Class.forName(model.getPackageName()
					+ ".CompoundProteinInteraction"));
			qcCompound = new QueryClass(Class.forName(model.getPackageName() + ".Compound"));
		} catch (ClassNotFoundException e) {
			LOG.error("Error rendering compound enrichment widget", e);
			return null;
		}

		// fields for SELECT clause
		QueryField qfId = null;
		QueryField qfDisplayId = null;
//		QueryField qfProteinId = new QueryField(qcProtein, "id");
		QueryField qfOrganismName = new QueryField(qcOrganism, "name");
		QueryField qfCompoundId = new QueryField(qcCompound, "identifier");
		QueryField qfCompoundName = new QueryField(qcCompound, "name");
//		QueryField qfPrimaryAccession = new QueryField(qcProtein, "primaryAccession");

		if ("Protein".equals(bagType)) {
			qfId = new QueryField(qcProtein, "id");
			qfDisplayId = new QueryField(qcProtein, "primaryAccession");
		} else if ("Gene".equals(bagType)) {
			qfId = new QueryField(qcGene, "id");
			qfDisplayId = new QueryField(qcGene, "ncbiGeneNumber");
		}

		// constraints
		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		// constrain genes to be in subset of list the user selected
		if (keys != null) {
			cs.addConstraint(new BagConstraint(qfCompoundId, ConstraintOp.IN, keys));
		}

		// constrain genes to be in list
		if (!action.startsWith("population")) {
			cs.addConstraint(new BagConstraint(qfId, ConstraintOp.IN, bag.getOsb()));
		}

		// organism in our list
		QueryExpression qe = new QueryExpression(QueryExpression.LOWER, qfOrganismName);
		cs.addConstraint(new BagConstraint(qe, ConstraintOp.IN, organismsLower));

		// gene.organism = organism
		QueryObjectReference qor = new QueryObjectReference(qcProtein, "organism");
		cs.addConstraint(new ContainsConstraint(qor, ConstraintOp.CONTAINS, qcOrganism));

		// gene.publication = publication
		QueryCollectionReference qcr = new QueryCollectionReference(qcProtein, "compounds");
		cs.addConstraint(new ContainsConstraint(qcr, ConstraintOp.CONTAINS,
				qcCompoundProteinInteraction));

		QueryObjectReference qorCmp = new QueryObjectReference(qcCompoundProteinInteraction,
				"compound");
		cs.addConstraint(new ContainsConstraint(qorCmp, ConstraintOp.CONTAINS, qcCompound));

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
		q.addFrom(qcCompoundProteinInteraction);
		q.addFrom(qcCompound);
		q.addFrom(qcOrganism);

		// constraint for dataset
		if (!dataset.equals("All datasets")) {
			String dataSetName = "";
			if (dataset.equals("ChEMBL")) {
				dataSetName = "ChEMBL";
			} else if (dataset.equals("DrugBank")) {
				dataSetName = "DrugBank";
			} else if (dataset.equals("Stitch")) {
				dataSetName = "STITCH";
			} else if (dataset.equals("PDB")) {
				dataSetName = "Ligand Expo";
			}

			QueryClass qcDataset = new QueryClass(DataSet.class);
			QueryField qfDataset = new QueryField(qcDataset, "name");

			QueryObjectReference qor2 = new QueryObjectReference(
					qcCompoundProteinInteraction, "dataSet");
			cs.addConstraint(new ContainsConstraint(qor2, ConstraintOp.CONTAINS, qcDataset));

			QueryExpression qe2 = new QueryExpression(QueryExpression.LOWER, qfDataset);
			cs.addConstraint(new SimpleConstraint(qe2, ConstraintOp.EQUALS, new QueryValue(
					dataSetName.toLowerCase())));

			q.addFrom(qcDataset);
		}

		// add constraints to query
		q.setConstraint(cs);

		// needed for the 'not analysed' number
		if (action.equals("analysed")) {
			q.addToSelect(qfId);
			// export query
			// needed for export button on widget
		} else if (action.equals("export")) {
			q.addToSelect(qfCompoundId);
			q.addToSelect(qfDisplayId);
			q.addToOrderBy(qfCompoundId);
			// total queries
			// needed for enrichment calculations
		} else if (action.endsWith("Total")) {
			q.addToSelect(qfId);
			Query subQ = q;
			q = new Query();
			q.addFrom(subQ);
			q.addToSelect(new QueryFunction()); // gene count
			// needed for enrichment calculations
		} else {
			q.addToSelect(qfCompoundId);
			q.addToGroupBy(qfCompoundId);
			q.addToSelect(new QueryFunction()); // gene count
			if (action.equals("sample")) {
				q.addToSelect(qfCompoundName);
				q.addToGroupBy(qfCompoundName);
			}
		}
		return q;
	}
}
