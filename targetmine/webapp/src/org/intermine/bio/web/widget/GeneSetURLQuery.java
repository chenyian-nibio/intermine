package org.intermine.bio.web.widget;

import java.util.Arrays;

import org.intermine.api.profile.InterMineBag;
import org.intermine.objectstore.ObjectStore;
import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.OrderDirection;
import org.intermine.pathquery.PathException;
import org.intermine.pathquery.PathQuery;
import org.intermine.web.logic.widget.WidgetURLQuery;

public class GeneSetURLQuery implements WidgetURLQuery {
	private ObjectStore os;
	private InterMineBag bag;
	private String key;

	/**
	 * @param os
	 * @param bag
	 * @param key
	 */
	public GeneSetURLQuery(ObjectStore os, InterMineBag bag, String key) {
		super();
		this.os = os;
		this.bag = bag;
		this.key = key;
	}

	@Override
	public PathQuery generatePathQuery(boolean showAll) throws PathException {

		String bagType = bag.getType();

		PathQuery q = new PathQuery(os.getModel());
		if (bagType.equals("Gene")) {
			q.addViews("Gene.ncbiGeneNumber", "Gene.symbol", "Gene.name",
					"Gene.pathways.geneSetClusters.identifier", "Gene.pathways.identifier",
					"Gene.pathways.name", "Gene.pathways.dataSets.name");
			q.addConstraint(Constraints.in(bag.getType(), bag.getName()));
			if (!showAll) {
				String[] keys = key.split(",");
				q.addConstraint(Constraints.oneOfValues("Gene.pathways.geneSetClusters.identifier",
						Arrays.asList(keys)));
			}
			q.addOrderBy("Gene.pathways.geneSetClusters.identifier", OrderDirection.ASC);
			q.addOrderBy("Gene.pathways.identifier", OrderDirection.ASC);
			q.addOrderBy("Gene.ncbiGeneNumber", OrderDirection.ASC);

		} else if (bagType.equals("Protein")) {
			q.addViews("Protein.primaryAccession", "Protein.name", "Protein.genes.symbol",
					"Protein.genes.pathways.geneSetClusters.identifier",
					"Protein.genes.pathways.identifier", "Protein.genes.pathways.name",
					"Protein.genes.pathways.dataSets.name");
			q.addConstraint(Constraints.in(bag.getType(), bag.getName()));
			if (!showAll) {
				String[] keys = key.split(",");
				q.addConstraint(Constraints.oneOfValues("Protein.genes.pathways.geneSetClusters.identifier", Arrays
						.asList(keys)));
			}
			q.addOrderBy("Protein.genes.pathways.geneSetClusters.identifier", OrderDirection.ASC);
			q.addOrderBy("Protein.genes.pathways.identifier", OrderDirection.ASC);
			q.addOrderBy("Protein.primaryAccession", OrderDirection.ASC);

		} else {
			throw new RuntimeException("Unexpected bagType: " + bagType);
		}

		return q;
	}

}
