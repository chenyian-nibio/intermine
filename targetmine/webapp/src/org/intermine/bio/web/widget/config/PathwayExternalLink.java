package org.intermine.bio.web.widget.config;

public class PathwayExternalLink extends WidgetExternalLink {

	@Override
	public String getExternalLink(String identifier) {
		String ret = "";
		if (identifier.startsWith("REACT_")) {
			// reactome
			ret = " <a href=\"http://www.reactome.org/cgi-bin/link?SOURCE=Reactome&ID="
					+ identifier + "\" target=\"_new\" class=\"extlink\">[Reactome:" + identifier
					+ "]</a>";
		} else if (identifier.matches("^\\w{3}\\d{5}$")) {
			// kegg
			ret = " <a href=\"http://www.genome.jp/dbget-bin/show_pathway?" + identifier
					+ "\" target=\"_new\" class=\"extlink\">[KEGG:" + identifier + "]</a>";
		} else {
			// nci pathway
			ret = " <a href=\"http://pid.nci.nih.gov/search/pathway_landing.shtml?"
					+ "source=NCI-Nature%20curated&what=graphic&jpg=on&ppage=1&pathway_id="
					+ identifier + "\" target=\"_new\" class=\"extlink\">[PID:" + identifier
					+ "]</a>";
		}
		return ret;
	}

}
