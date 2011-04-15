package org.intermine.bio.web.widget;

import org.intermine.api.profile.InterMineBag;
import org.intermine.objectstore.ObjectStore;
import org.intermine.web.logic.widget.EnrichmentWidgetLdr;

public class OmimDataSetLoader extends EnrichmentGraphDataSetLoader {

	public OmimDataSetLoader(InterMineBag bag, ObjectStore os, String extra) {
		super();
		EnrichmentWidgetLdr dataLoader = new OmimDataLoader(bag, os, null);
		buildDataSets(bag, os, dataLoader, EnrichmentGraphDataSetLoader.DISPLAY_GROUP_NUMBER);
	}

}
