package org.intermine.bio.web.displayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.intermine.api.InterMineAPI;
import org.intermine.model.InterMineObject;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.Organism;
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
		Gene gene = (Gene) reportObject.getObject();
		Map<String, Gene> allOrthologs = new HashMap<String, Gene>();
		try {
			Set<Protein> proteins =  gene.getProteins();
			for (Protein protein : proteins) {
				Set<Protein> orthologs =  protein.getOrthologProteins();
				for (Protein ortholog : orthologs) {
					Set<Gene> genes =  ortholog.getGenes();
					for (Gene entry : genes) {
						allOrthologs.put(entry.getPrimaryIdentifier(), entry);
					}
				}
			}
			request.setAttribute("orthologs", allOrthologs.values());
			
			// pre-process KO to identify orthologues and paralogues
			Organism org = (Organism) gene.getFieldValue("organism");
			Set<InterMineObject> keggOrthology =  (Set<InterMineObject>) gene.getFieldValue("keggOrthology");
			Map<InterMineObject, Map<String, Set<InterMineObject>>> koMap = new HashMap<InterMineObject, Map<String, Set<InterMineObject>>>();
			for (InterMineObject ko : keggOrthology) {
				HashMap<String, Set<InterMineObject>> orthologMap = new HashMap<String, Set<InterMineObject>>();
				Set<Gene> genes = (Set<Gene>) ko.getFieldValue("genes");
				for (Gene ortholog : genes) {
					if (gene.getPrimaryIdentifier().equals(ortholog.getPrimaryIdentifier())) {
						continue;
					}
					String type = "orthologue";
					if (org.getTaxonId().equals(((Organism) ortholog.getFieldValue("organism")).getTaxonId())) {
						type = "paralogue";
					}
					if (orthologMap.get(type) == null) {
						orthologMap.put(type, new HashSet<InterMineObject>());
					}
					orthologMap.get(type).add(ortholog);
				}
				koMap.put(ko, orthologMap);
			}
			request.setAttribute("koMap", koMap);
			request.setAttribute("koSet", koMap.keySet());
			
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			LOG.error(e.getMessage());
		}
		
	}
	
	

}
