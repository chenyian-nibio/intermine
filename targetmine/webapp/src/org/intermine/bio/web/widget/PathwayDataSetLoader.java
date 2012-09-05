package org.intermine.bio.web.widget;

import org.intermine.api.profile.InterMineBag;
import org.intermine.objectstore.ObjectStore;
import org.intermine.web.logic.widget.EnrichmentWidgetLdr;

public class PathwayDataSetLoader extends EnrichmentGraphDataSetLoader {

//	private static final Logger LOG = Logger.getLogger(PathwayDataSetLoader.class);

	public PathwayDataSetLoader(InterMineBag bag, ObjectStore os, String extra) {
		super();
		EnrichmentWidgetLdr dataLoader = new PathwayDataLoader(bag, os, null);
		buildDataSets(bag, os, dataLoader, EnrichmentGraphDataSetLoader.DISPLAY_GROUP_NUMBER);
	}

}
