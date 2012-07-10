package org.intermine.bio.web.widget;

import java.util.List;

import org.apache.log4j.Logger;
import org.intermine.api.profile.InterMineBag;
import org.intermine.bio.util.BioUtil;
import org.intermine.metadata.Model;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.Organism;
import org.intermine.model.bio.Pathway;
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
public class GeneSetDataLoader extends EnrichmentWidgetLdr {
	private static final Logger LOG = Logger.getLogger(GeneSetDataLoader.class);

	private Model model;

	public GeneSetDataLoader(InterMineBag bag, ObjectStore os, String extraAttribute) {
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
		QueryClass qcGene = new QueryClass(Gene.class);
		QueryClass qcPathway = new QueryClass(Pathway.class);
		QueryClass qcOrganism = new QueryClass(Organism.class);
		QueryClass qcGeneSetCluster;
		try {
			qcGeneSetCluster = new QueryClass(Class.forName(model.getPackageName()
					+ ".GeneSetCluster"));
		} catch (ClassNotFoundException e) {
			LOG.error("Error rendering gene set enrichment widget", e);
			return null;
		}

		// fields for SELECT clause
		QueryField qfGeneId = new QueryField(qcGene, "id");
		QueryField qfOrganismName = new QueryField(qcOrganism, "name");
		QueryField qfPathwayId = new QueryField(qcPathway, "identifier");
		QueryField qfPathwayName = new QueryField(qcPathway, "name");
		QueryField qfGscId = new QueryField(qcGeneSetCluster, "identifier");
		QueryField qfNcbiGeneNumber= new QueryField(qcGene, "ncbiGeneNumber");

		// constraints
		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		// constrain genes to be in subset of list the user selected
		if (keys != null) {
			cs.addConstraint(new BagConstraint(qfGscId, ConstraintOp.IN, keys));
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

		// gene.pathways = pathway
		QueryCollectionReference qcr = new QueryCollectionReference(qcGene, "pathways");
		cs.addConstraint(new ContainsConstraint(qcr, ConstraintOp.CONTAINS, qcPathway));

		// pathway.geneSetClusters = GeneSetCluster
		QueryCollectionReference qcrGsc = new QueryCollectionReference(qcPathway, "geneSetClusters");
		cs.addConstraint(new ContainsConstraint(qcrGsc, ConstraintOp.CONTAINS, qcGeneSetCluster));

		Query q = new Query();
		q.setDistinct(true);

		// from statement
		q.addFrom(qcGene);
		q.addFrom(qcPathway);
		q.addFrom(qcOrganism);
		q.addFrom(qcGeneSetCluster);
		
		// add constraints to query
		q.setConstraint(cs);

		// needed for the 'not analysed' number
		if (action.equals("analysed")) {
			q.addToSelect(qfGeneId);
			// export query
			// needed for export button on widget
		} else if (action.equals("export")) {
			q.addToSelect(qfGscId);
			q.addToSelect(qfPathwayId);
			q.addToSelect(qfPathwayName);
			q.addToSelect(qfNcbiGeneNumber);
			q.addToOrderBy(qfGscId);
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
			q.addToSelect(qfGscId);
			q.addToGroupBy(qfGscId);
			q.addToSelect(new QueryFunction()); // gene count
			if (action.equals("sample")) {
				q.addToSelect(qfGscId);
				q.addToGroupBy(qfGscId);
			}
		}
		return q;
	}
}
