package org.intermine.bio.web.displayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.intermine.api.InterMineAPI;
import org.intermine.model.InterMineObject;
import org.intermine.model.bio.Compound;
import org.intermine.model.bio.CompoundProteinInteraction;
import org.intermine.model.bio.Protein;
import org.intermine.web.displayer.ReportDisplayer;
import org.intermine.web.logic.config.ReportDisplayerConfig;
import org.intermine.web.logic.results.ReportObject;

public class CompoundGroupDisplayer extends ReportDisplayer {
	protected static final Logger LOG = Logger.getLogger(CompoundGroupDisplayer.class);

	public CompoundGroupDisplayer(ReportDisplayerConfig config, InterMineAPI im) {
		super(config, im);
	}

	@Override
	public void display(HttpServletRequest request, ReportObject reportObject) {
		InterMineObject compoundGroup = reportObject.getObject();
		Map<String, Protein> proteins = new HashMap<String, Protein>();
		try {
			@SuppressWarnings("unchecked")
			Set<Compound> compounds =  (Set<Compound>) compoundGroup.getFieldValue("compounds");
			for (Compound compound : compounds) {
				Set<CompoundProteinInteraction> targetProteins = compound.getTargetProteins();
				for (CompoundProteinInteraction interaction : targetProteins) {
					proteins.put(interaction.getProtein().getPrimaryIdentifier(), interaction.getProtein());
				}
			}
		} catch (IllegalAccessException e) {
			LOG.info(e.getMessage());
		}
		request.setAttribute("proteins", proteins.values());
	}

}
