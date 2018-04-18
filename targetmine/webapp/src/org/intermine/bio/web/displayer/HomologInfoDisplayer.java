package org.intermine.bio.web.displayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.intermine.api.InterMineAPI;
import org.intermine.model.InterMineObject;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.Protein;
import org.intermine.web.displayer.ReportDisplayer;
import org.intermine.web.logic.config.ReportDisplayerConfig;
import org.intermine.web.logic.results.ReportObject;

public class HomologInfoDisplayer extends ReportDisplayer {
	protected static final Logger LOG = Logger.getLogger(HomologInfoDisplayer.class);

	public HomologInfoDisplayer(ReportDisplayerConfig config, InterMineAPI im) {
		super(config, im);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void display(HttpServletRequest request, ReportObject reportObject) {
		InterMineObject gene = reportObject.getObject();
		Map<String, Gene> allOrthologs = new HashMap<String, Gene>();
		try {
			Set<Protein> proteins =  (Set<Protein>) gene.getFieldValue("proteins");
			for (Protein protein : proteins) {
				Set<Protein> orthologs =  (Set<Protein>) protein.getFieldValue("orthologProteins");
				for (Protein ortholog : orthologs) {
					Set<Gene> genes =  (Set<Gene>) ortholog.getFieldValue("genes");
					for (Gene entry : genes) {
						allOrthologs.put(entry.getPrimaryIdentifier(), entry);
					}
				}
			}
			request.setAttribute("orthologs", allOrthologs.values());
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	

}
