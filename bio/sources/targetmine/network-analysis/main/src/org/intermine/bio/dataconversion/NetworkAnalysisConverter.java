package org.intermine.bio.dataconversion;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.Reader;

import org.intermine.dataconversion.FileConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;

/**
 * 
 * @author
 */
public class NetworkAnalysisConverter extends FileConverter {
	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public NetworkAnalysisConverter(ItemWriter writer, Model model) {
		super(writer, model);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {}
//		if (allTaxonIds == null || osAlias == null) {
//			throw new RuntimeException("check your setting for network.organisms and osAlias.");
//		}
////		getPhysicalInteractions();
//		LOG.info("processing network......");
//	}
//
//	private List<String> getPhysicalInteractions() throws Exception {
//		Query q = new Query();
//		QueryClass qcGene1 = new QueryClass(Gene.class);
//		QueryClass qcGene2 = new QueryClass(Gene.class);
//		QueryClass qcInteraction = new QueryClass(
//				Class.forName("org.intermine.model.bio.Interaction"));
//		QueryClass qcInteractionDetail = new QueryClass(
//				Class.forName("org.intermine.model.bio.InteractionDetail"));
//		QueryClass qcRelationshipType = new QueryClass(
//				Class.forName("org.intermine.model.bio.InteractionTerm"));
//		QueryClass qcInteractionDetectionMethods = new QueryClass(
//				Class.forName("org.intermine.model.bio.InteractionTerm"));
//		QueryClass qcInteractionExperiment = new QueryClass(
//				Class.forName("org.intermine.model.bio.InteractionExperiment"));
//		QueryClass qcOrganism1= new QueryClass(Organism.class);
//		QueryClass qcOrganism2= new QueryClass(Organism.class);
//		QueryClass qcPublication= new QueryClass(Publication.class);
//		
//		q.addFrom(qcGene1);
//		q.addFrom(qcInteraction);
//		q.addFrom(qcGene2);
//		q.addFrom(qcOrganism1);
//		q.addFrom(qcOrganism2);
////		q.addFrom(qcInteractionDetail);
////		q.addFrom(qcRelationshipType);
////		q.addFrom(qcInteractionDetectionMethods);
////		q.addFrom(qcInteractionExperiment);
////		q.addFrom(qcPublication);
//		
//		q.addToSelect(qcGene1);
//		
//		QueryField qfGeneId1 = new QueryField(qcGene1, "ncbiGeneId");
//		QueryField qfGeneId2 = new QueryField(qcGene2, "ncbiGeneId");
//		QueryField qfTaxonId1 = new QueryField(qcOrganism1, "taxonId");
//		QueryField qfTaxonId2 = new QueryField(qcOrganism2, "taxonId");
//		
//		q.addToSelect(qfGeneId1);
//		q.addToSelect(qfGeneId2);
//		q.setDistinct(true);
//		
//		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);
//		cs.addConstraint(new SimpleConstraint(qfTaxonId1, ConstraintOp.EQUALS, new QueryValue("9606")));
//		cs.addConstraint(new SimpleConstraint(qfTaxonId2, ConstraintOp.EQUALS, new QueryValue("9606")));
//
//
//		ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);
//
//		List<String> ret = new ArrayList<String>();
//		Results results = os.execute(q);
//		LOG.info("There are " + results.size() + " rows.");
////		Iterator iterator = results.iterator();
////		while (iterator.hasNext()) {
////			ResultsRow<Integer> rr = (ResultsRow<Integer>) iterator.next();
////			ret.add(rr.get(0).toString());
////		}
//		return ret;
//	}

}
