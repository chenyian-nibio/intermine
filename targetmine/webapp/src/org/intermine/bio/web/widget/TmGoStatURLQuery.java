package org.intermine.bio.web.widget;

import java.util.Arrays;

import org.intermine.api.profile.InterMineBag;
import org.intermine.objectstore.ObjectStore;
import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.PathQuery;
import org.intermine.web.logic.widget.WidgetURLQuery;

/**
 * chenyian: This class is modified from the default GoStatURLQurey.class, just for preventing being
 * overwritten when there is any upgrades
 */
public class TmGoStatURLQuery implements WidgetURLQuery {
	// private static final Logger LOG = Logger.getLogger(TmGoStatURLQuery.class);
	private ObjectStore os;
	private InterMineBag bag;
	private String key;

	/**
	 * @param os
	 *            object store
	 * @param key
	 *            go terms user selected
	 * @param bag
	 *            bag page they were on
	 */
	public TmGoStatURLQuery(ObjectStore os, InterMineBag bag, String key) {
		this.bag = bag;
		this.key = key;
		this.os = os;
	}

	/**
	 * {@inheritDoc}
	 */
	public PathQuery generatePathQuery(boolean showAll) {

        PathQuery q = new PathQuery(os.getModel());
        String bagType = bag.getType();

        String prefix = ("Protein".equals(bagType) ? "Protein.genes" : "Gene");

        if ("Protein".equals(bagType)) {
            q.addViews("Protein.primaryAccession");
        }

        q.addViews(prefix + ".ncbiGeneNumber",
                prefix + ".symbol",
                prefix + ".organism.name",
//                prefix + ".goAnnotation.ontologyTerm.identifier",
//                prefix + ".goAnnotation.ontologyTerm.name",
                prefix + ".goAnnotation.ontologyTerm.parents.identifier",
                prefix + ".goAnnotation.ontologyTerm.parents.name");

        // ORDER: default is to order by all paths in view

        q.addConstraint(Constraints.in(bagType, bag.getName()));
        // can't be a NOT relationship!
        q.addConstraint(Constraints.isNull(prefix + ".goAnnotation.qualifier"));

        if (!showAll) {
            String[] keys = key.split(",");
            q.addConstraint(Constraints.oneOfValues(prefix
                    + ".goAnnotation.ontologyTerm.parents.identifier",
                    Arrays.asList(keys)));
        }
        return q;
    }

}
