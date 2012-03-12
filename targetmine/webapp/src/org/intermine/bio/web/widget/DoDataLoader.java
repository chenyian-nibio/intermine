package org.intermine.bio.web.widget;

import java.util.ArrayList;
import java.util.Collection;
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
import org.intermine.objectstore.query.QueryValue;
import org.intermine.objectstore.query.SimpleConstraint;
import org.intermine.web.logic.widget.EnrichmentWidgetLdr;


/***
 * DO Enrichment Widget  -  not tested yet
 * 
 * @author chenyian
 *
 */
public class DoDataLoader extends EnrichmentWidgetLdr {

    private static final Logger LOG = Logger.getLogger(DoDataLoader.class);
    private Collection<String> taxonIds;
    private InterMineBag bag;
    private static final String NAMESPACE = "disease_ontology";
    private Model model;

    public DoDataLoader(InterMineBag bag, ObjectStore os, String extraAttribute) {
        this.bag = bag;
//        namespace = extraAttribute;
        taxonIds = BioUtil.getOrganisms(os, bag, false, "taxonId");
        model = os.getModel();
	}

    
    /**
     * {@inheritDoc}
     */
    public Query getQuery(String action, List<String> keys) {

        String bagType = bag.getType();

        QueryClass qcGene = new QueryClass(Gene.class);
        QueryClass qcDoAnnotation = null;
        QueryClass qcDoChild = null;
        QueryClass qcDoParent = null;

        try {
            qcDoAnnotation = new QueryClass(Class.forName(model.getPackageName()+ ".DOAnnotation"));
            qcDoParent = new QueryClass(Class.forName(model.getPackageName() + ".OntologyTerm"));
            qcDoChild = new QueryClass(Class.forName(model.getPackageName() + ".OntologyTerm"));
        } catch (ClassNotFoundException e) {
            LOG.error("Error rendering DO enrichment widget", e);
            // don't throw an exception, return NULL instead.  The widget will display 'no results'.
            // the javascript that renders widgets assumes a valid widget and thus can't handle
            // an exception thrown here.
            return null;
        }
        QueryClass qcProtein = new QueryClass(Protein.class);
        QueryClass qcOrganism = new QueryClass(Organism.class);

//        QueryField qfQualifier = new QueryField(qcDoAnnotation, "qualifier");
        QueryField qfGeneId = new QueryField(qcGene, "id");
        QueryField qfTaxonId = new QueryField(qcOrganism, "taxonId");
        QueryField qfProteinId = new QueryField(qcProtein, "id");
        QueryField qfPrimaryIdentifier = null;
        QueryField qfId = null;

        if (bagType.equals("Protein")) {
//            qfPrimaryIdentifier = new QueryField(qcProtein, "primaryIdentifier");
        	// chenyian: primaryAccession(UniProt id) is better
            qfPrimaryIdentifier = new QueryField(qcProtein, "primaryAccession");
            qfId = qfProteinId;
        } else {
//            qfPrimaryIdentifier = new QueryField(qcGene, "primaryIdentifier");
            // chenyian: change export filed to gene ids, 
        	// some of Genes do not have primaryIdentifier and my cause NullPointException
            qfPrimaryIdentifier = new QueryField(qcGene, "ncbiGeneNumber");
            qfId = qfGeneId;
        }

        QueryField qfNamespace = new QueryField(qcDoParent, "namespace");
        QueryField qfParentDoIdentifier = new QueryField(qcDoParent, "identifier");
        QueryField qfParentDoName = new QueryField(qcDoParent, "name");

        ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

        // gene.doAnnotations CONTAINS DOAnnotation
        QueryCollectionReference c1 = new QueryCollectionReference(qcGene, "doAnnotations");
        cs.addConstraint(new ContainsConstraint(c1, ConstraintOp.CONTAINS, qcDoAnnotation));

        // DO terms selected by user = gene.doAnnotations.ontologyTerm.identifier
        if (keys != null) {
            cs.addConstraint(new BagConstraint(qfParentDoIdentifier, ConstraintOp.IN, keys));
        }

        // do annotation contains do term
        QueryObjectReference c3 = new QueryObjectReference(qcDoAnnotation, "ontologyTerm");
        cs.addConstraint(new ContainsConstraint(c3, ConstraintOp.CONTAINS, qcDoChild));

        // do annotation contains do terms & parents
        QueryCollectionReference c4 = new QueryCollectionReference(qcDoChild, "parents");
        cs.addConstraint(new ContainsConstraint(c4, ConstraintOp.CONTAINS, qcDoParent));

        // do term is of the specified namespace; only 'disease_ontology'
        QueryExpression c7 = new QueryExpression(QueryExpression.LOWER, qfNamespace);
        cs.addConstraint(new SimpleConstraint(c7, ConstraintOp.EQUALS,
                new QueryValue(NAMESPACE)));

        Collection<Integer> taxonIdInts = new ArrayList();
        // constrained only for memory reasons
        for (String taxonId : taxonIds) {
            try {
                taxonIdInts.add(new Integer(taxonId));
            } catch (NumberFormatException e) {
                LOG.error("Error rendering DO widget, invalid taxonIds: " + taxonIds);
                // don't throw an exception, return NULL instead.  The widget will display 'no
                // results'. the javascript that renders widgets assumes a valid widget and thus
                // can't handle an exception thrown here.
                return null;
            }
        }
        cs.addConstraint(new BagConstraint(qfTaxonId, ConstraintOp.IN, taxonIdInts));

        // gene is from organism
        QueryObjectReference c9 = new QueryObjectReference(qcGene, "organism");
        cs.addConstraint(new ContainsConstraint(c9, ConstraintOp.CONTAINS, qcOrganism));

        if (!action.startsWith("population")) {
            cs.addConstraint(new BagConstraint(qfId, ConstraintOp.IN, bag.getOsb()));
        }

        if (bagType.equals("Protein")) {
            QueryCollectionReference c10 = new QueryCollectionReference(qcProtein, "genes");
            cs.addConstraint(new ContainsConstraint(c10, ConstraintOp.CONTAINS, qcGene));
        }

        Query q = new Query();
        q.setDistinct(true);
        q.addFrom(qcGene);
        q.addFrom(qcDoAnnotation);
        q.addFrom(qcOrganism);
        q.addFrom(qcDoParent);
        q.addFrom(qcDoChild);

        if (bagType.equals("Protein")) {
            q.addFrom(qcProtein);
        }
        q.setConstraint(cs);

        if (action.equals("analysed")) {
            q.addToSelect(qfId);
        } else if (action.equals("export")) {
            q.addToSelect(qfParentDoIdentifier);
            q.addToSelect(qfPrimaryIdentifier);
            q.addToOrderBy(qfParentDoIdentifier);
        } else if (action.endsWith("Total")) {
            q.addToSelect(qfId);
            Query subQ = q;
            q = new Query();
            q.addFrom(subQ);
            q.addToSelect(new QueryFunction());
        } else {    // calculating enrichment

            /*
            these need to be uniquified because there are duplicates.
            2 go terms can have multiple entries which just the relationship type being
            different.

            the first query gets all of the gene --> go term relationships unique
            the second query then counts the genes per each go term
             */

            // subquery
            Query subq = q;
            subq.addToSelect(qfId);
            subq.addToSelect(qfParentDoIdentifier);

            QueryField qfName = null;
            if (action.equals("sample")) {
                subq.addToSelect(qfParentDoName);
                qfName = new QueryField(subq, qfParentDoName);
            }

            // needed so we can select this field in the parent query
            QueryField qfIdentifier = new QueryField(subq, qfParentDoIdentifier);

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
