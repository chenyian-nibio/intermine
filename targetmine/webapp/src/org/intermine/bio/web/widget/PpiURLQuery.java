package org.intermine.bio.web.widget;

import java.util.Arrays;

import org.intermine.api.profile.InterMineBag;
import org.intermine.objectstore.ObjectStore;
import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.OrderDirection;
import org.intermine.pathquery.PathQuery;
import org.intermine.web.logic.widget.WidgetURLQuery;

public class PpiURLQuery implements WidgetURLQuery {

	private ObjectStore os;
	private InterMineBag bag;
	private String key;

	/**
	 * @param os
	 * @param bag
	 * @param key
	 */
	public PpiURLQuery(ObjectStore os, InterMineBag bag, String key) {
		super();
		this.os = os;
		this.bag = bag;
		this.key = key;
	}

	public PathQuery generatePathQuery(boolean showAll) {
		PathQuery q = new PathQuery(os.getModel());
		q.addViews("Protein.primaryAccession", "Protein.name", "Protein.organism.name",
				"Protein.proteinInteractions.representativePartner.primaryAccession",
				"Protein.proteinInteractions.representativePartner.name");
		q.addConstraint(Constraints.in(bag.getType(), bag.getName()));
		if (!showAll) {
			String[] keys = key.split(",");
			q.addConstraint(Constraints.oneOfValues(
					"Protein.proteinInteractions.representativePartner.primaryAccession", Arrays
							.asList(keys)));
		}
		q.addOrderBy("Protein.primaryAccession", OrderDirection.ASC);
		return q;
	}

}
