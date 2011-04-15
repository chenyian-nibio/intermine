package org.intermine.bio.web.widget;

import java.util.Arrays;

import org.intermine.api.profile.InterMineBag;
import org.intermine.objectstore.ObjectStore;
import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.OrderDirection;
import org.intermine.pathquery.PathQuery;
import org.intermine.web.logic.widget.WidgetURLQuery;

public class DrugURLQuery implements WidgetURLQuery {

	private ObjectStore os;
	private InterMineBag bag;
	private String key;

	/**
	 * @param os
	 * @param bag
	 * @param key
	 */
	public DrugURLQuery(ObjectStore os, InterMineBag bag, String key) {
		super();
		this.os = os;
		this.bag = bag;
		this.key = key;
	}

	public PathQuery generatePathQuery(boolean showAll) {
		String bagType = bag.getType();

		PathQuery q = new PathQuery(os.getModel());
		if (bagType.equals("Gene")) {
			q.addViews("Gene.ncbiGeneNumber", "Gene.symbol", "Gene.name",
					"Gene.proteins.drugs.drug.drugBankId", "Gene.proteins.drugs.drug.hetId",
					"Gene.proteins.drugs.drug.genericName");
			q.addConstraint(Constraints.in(bag.getType(), bag.getName()));
			if (!showAll) {
				String[] keys = key.split(",");
				q.addConstraint(Constraints.oneOfValues("Gene.proteins.drugs.drug.drugBankId",
						Arrays.asList(keys)));
			}
			q.addOrderBy("Gene.ncbiGeneNumber", OrderDirection.ASC);

		} else if (bagType.equals("Protein")) {
			q.addViews("Protein.primaryAccession", "Protein.name", "Protein.organism.name",
					"Protein.drugs.drug.drugBankId", "Protein.drugs.drug.hetId",
					"Protein.drugs.drug.genericName");
			q.addConstraint(Constraints.in(bag.getType(), bag.getName()));
			if (!showAll) {
				String[] keys = key.split(",");
				q.addConstraint(Constraints.oneOfValues("Protein.drugs.drug.drugBankId", Arrays
						.asList(keys)));
			}
			q.addOrderBy("Protein.primaryAccession", OrderDirection.ASC);
		} else {
			throw new RuntimeException("Unexpected bagType: " + bagType);
		}

		return q;
	}

}
