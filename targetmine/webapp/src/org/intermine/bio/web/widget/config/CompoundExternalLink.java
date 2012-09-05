package org.intermine.bio.web.widget.config;

public class CompoundExternalLink extends WidgetExternalLink {

	@Override
	public String getExternalLink(String identifier) {
		String dbId = identifier.split(": ")[1];
		String ret = "";
		if (identifier.startsWith("DrugBank:")) {
			ret = " <a href=\"" + "http://www.drugbank.ca/drugs/" + dbId
					+ "\" target=\"_new\" class=\"extlink\">[" + "DrugBank:" + dbId + "]</a>";
		} else if (identifier.startsWith("ChEMBL:")) {
			ret = " <a href=\"" + "https://www.ebi.ac.uk/chembldb/compound/inspect/" + dbId
					+ "\" target=\"_new\" class=\"extlink\">[" + "ChEMBL:" + dbId + "]</a>";
		} else if (identifier.startsWith("CHEBI:")) {
			ret = " <a href=\"" + "http://www.ebi.ac.uk/chebi/searchId.do?chebiId=CHEBI:" + dbId
					+ "\" target=\"_new\" class=\"extlink\">[" + "ChEBI:" + dbId + "]</a>";
		} else if (identifier.startsWith("PubChem:")) {
			ret = " <a href=\"" + "http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid=" + dbId
					+ "\" target=\"_new\" class=\"extlink\">[" + "PubChem:" + dbId + "]</a>";
		} else if (identifier.startsWith("PDBCompound:")) {
			ret = " <a href=\"" + "http://www.rcsb.org/pdb/ligand/ligandsummary.do?hetId=" + dbId
					+ "\" target=\"_new\" class=\"extlink\">[" + "RCSB:" + dbId + "]</a>";
		}
		return ret;
	}

}
