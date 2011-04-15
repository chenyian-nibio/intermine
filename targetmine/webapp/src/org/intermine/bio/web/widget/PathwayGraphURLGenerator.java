package org.intermine.bio.web.widget;

import org.intermine.api.profile.InterMineBag;
import org.intermine.objectstore.ObjectStore;
import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.OrderDirection;
import org.intermine.pathquery.PathQuery;
import org.intermine.web.logic.widget.GraphCategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;

public class PathwayGraphURLGenerator implements GraphCategoryURLGenerator {

    private String bagName;

    public PathwayGraphURLGenerator(String bagName, String organism) {
        super();
        this.bagName = bagName;
	}

    public PathwayGraphURLGenerator(String bagName) {
        super();
        this.bagName = bagName;
    }

	@Override
	public PathQuery generatePathQuery(ObjectStore os, InterMineBag bag, String category,
			String series) {

		PathQuery q = new PathQuery(os.getModel());
			q.addViews("Gene.ncbiGeneNumber", "Gene.symbol", "Gene.name",
					"Gene.pathways.identifier", "Gene.pathways.name", "Gene.pathways.mainClass",
					"Gene.pathways.subClass");
			q.addConstraint(Constraints.in(bag.getType(), bag.getName()));
			q.addOrderBy("Gene.pathways.identifier", OrderDirection.ASC);
			q.addOrderBy("Gene.ncbiGeneNumber", OrderDirection.ASC);

		return q;
	}

	@Override
	public String generateURL(CategoryDataset dataset, int series, int category) {
        StringBuffer sb = new StringBuffer("queryForGraphAction.do?bagName=" + bagName);

        String seriesName = (String) dataset.getRowKey(series);
        seriesName = seriesName.toLowerCase();
        Boolean expressed = Boolean.FALSE;
        if ("expressed".equals(seriesName)) {
            expressed = Boolean.TRUE;
        }

        sb = new StringBuffer("queryForGraphAction.do?bagName=" + bagName);
        sb.append("&category=" + dataset.getColumnKey(category));
        sb.append("&series=" + expressed);
        sb.append("&urlGen=org.intermine.bio.web.widget.PathwayGraphURLGenerator");

        return sb.toString();
	}

}
