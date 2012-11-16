package org.intermine.bio.web.widget;

import java.util.Arrays;

import org.intermine.api.profile.InterMineBag;
import org.intermine.objectstore.ObjectStore;
import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.OrderDirection;
import org.intermine.pathquery.PathException;
import org.intermine.pathquery.PathQuery;
import org.intermine.web.logic.widget.WidgetURLQuery;

public class StructuralDomainURLQuery implements WidgetURLQuery {

	private ObjectStore os;
	private InterMineBag bag;
	private String key;

	/***
	 * @param os
	 * @param bag
	 * @param key
	 */
	public StructuralDomainURLQuery(ObjectStore os, InterMineBag bag, String key) {
		super();
		this.os = os;
		this.bag = bag;
		this.key = key;
	}

	@Override
	public PathQuery generatePathQuery(boolean showAll) throws PathException {
		String bagType = bag.getType();

		String prefix = (bagType.equals("Protein") ? "Protein" : "Gene.proteins");

		PathQuery q = new PathQuery(os.getModel());
		if (bagType.equals("Gene")) {
			q.addViews("Gene.ncbiGeneNumber", "Gene.symbol");
		}
		q.addViews(prefix + ".primaryAccession", prefix + ".name", prefix + ".organism.shortName",
				prefix + ".structuralDomains.cathClassification.cathCode", prefix
						+ ".structuralDomains.cathClassification.description");
		q.addConstraint(Constraints.in(bag.getType(), bag.getName()));
		if (!showAll) {
			String[] keys = key.split(",");
			q.addConstraint(Constraints.oneOfValues(prefix
					+ ".structuralDomains.cathClassification.cathCode", Arrays.asList(keys)));
		}

		q.addOrderBy(prefix + ".structuralDomains.cathClassification.cathCode",
				OrderDirection.ASC);

		return q;
	}

}
