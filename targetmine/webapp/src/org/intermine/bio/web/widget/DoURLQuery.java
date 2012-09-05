package org.intermine.bio.web.widget;

import java.util.Arrays;

import org.intermine.api.profile.InterMineBag;
import org.intermine.objectstore.ObjectStore;
import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.OrderDirection;
import org.intermine.pathquery.PathException;
import org.intermine.pathquery.PathQuery;
import org.intermine.web.logic.widget.WidgetURLQuery;

public class DoURLQuery implements WidgetURLQuery {

	private ObjectStore os;
	private InterMineBag bag;
	private String key;

	public DoURLQuery(ObjectStore os, InterMineBag bag, String key) {
		this.os = os;
		this.bag = bag;
		this.key = key;
	}

	@Override
	public PathQuery generatePathQuery(boolean showAll) throws PathException {

		PathQuery q = new PathQuery(os.getModel());
		String bagType = bag.getType();

		String prefix = (bagType.equals("Protein") ? "Protein.genes" : "Gene");

		if (bagType.equals("Protein")) {
			q.addViews("Protein.primaryAccession");
		}

		q.addViews(prefix + ".ncbiGeneNumber", prefix + ".symbol", prefix + ".name", prefix
				+ ".organism.shortName", prefix + ".doAnnotations.ontologyTerm.identifier", prefix
				+ ".doAnnotations.ontologyTerm.name", prefix
				+ ".doAnnotations.ontologyTerm.parents.identifier", prefix
				+ ".doAnnotations.ontologyTerm.parents.name");

		q.addConstraint(Constraints.in(bagType, bag.getName()));

		if (!showAll) {
			String[] keys = key.split(",");
			q.addConstraint(Constraints.oneOfValues(prefix
					+ ".doAnnotations.ontologyTerm.parents.identifier", Arrays.asList(keys)));
		}

		q.addOrderBy(prefix + ".doAnnotations.ontologyTerm.parents.identifier", OrderDirection.ASC);

		return q;
	}

}
