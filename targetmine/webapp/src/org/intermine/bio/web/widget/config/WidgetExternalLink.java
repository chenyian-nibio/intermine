package org.intermine.bio.web.widget.config;

public abstract class WidgetExternalLink {
	/**
	 * The identifier should be distinguishable for different data source
	 * 
	 * @param identifier
	 * @return constructed html tag for external link
	 */
	public abstract String getExternalLink(String identifier);
}
