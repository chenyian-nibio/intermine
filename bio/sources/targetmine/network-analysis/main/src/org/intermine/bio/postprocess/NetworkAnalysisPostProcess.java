package org.intermine.bio.postprocess;

import java.util.ArrayList;
import java.util.List;

import org.intermine.metadata.Model;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.Organism;
import org.intermine.model.bio.Publication;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.query.ConstraintOp;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.QueryValue;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.SimpleConstraint;
import org.intermine.postprocess.PostProcessor;

public class NetworkAnalysisPostProcess extends PostProcessor {
	
	private Model model;


	public NetworkAnalysisPostProcess(ObjectStoreWriter osw) {
		super(osw);
		model = Model.getInstanceByName("genomic");
	}

	@Override
	public void postProcess() throws ObjectStoreException {
		// TODO Auto-generated method stub

	}
	
	private List<String> getPhysicalInteractions() throws Exception {
	Query q = new Query();
	QueryClass qcGene1 = new QueryClass(Gene.class);
	QueryClass qcGene2 = new QueryClass(Gene.class);
	QueryClass qcInteraction = new QueryClass(
			Class.forName("org.intermine.model.bio.Interaction"));
	QueryClass qcInteractionDetail = new QueryClass(
			Class.forName("org.intermine.model.bio.InteractionDetail"));
	QueryClass qcRelationshipType = new QueryClass(
			Class.forName("org.intermine.model.bio.InteractionTerm"));
	QueryClass qcInteractionDetectionMethods = new QueryClass(
			Class.forName("org.intermine.model.bio.InteractionTerm"));
	QueryClass qcInteractionExperiment = new QueryClass(
			Class.forName("org.intermine.model.bio.InteractionExperiment"));
	QueryClass qcOrganism1= new QueryClass(Organism.class);
	QueryClass qcOrganism2= new QueryClass(Organism.class);
	QueryClass qcPublication= new QueryClass(Publication.class);
	
	q.addFrom(qcGene1);
	q.addFrom(qcInteraction);
	q.addFrom(qcGene2);
	q.addFrom(qcOrganism1);
	q.addFrom(qcOrganism2);
//	q.addFrom(qcInteractionDetail);
//	q.addFrom(qcRelationshipType);
//	q.addFrom(qcInteractionDetectionMethods);
//	q.addFrom(qcInteractionExperiment);
//	q.addFrom(qcPublication);
	
	q.addToSelect(qcGene1);
	
	QueryField qfGeneId1 = new QueryField(qcGene1, "ncbiGeneId");
	QueryField qfGeneId2 = new QueryField(qcGene2, "ncbiGeneId");
	QueryField qfTaxonId1 = new QueryField(qcOrganism1, "taxonId");
	QueryField qfTaxonId2 = new QueryField(qcOrganism2, "taxonId");
	
	q.addToSelect(qfGeneId1);
	q.addToSelect(qfGeneId2);
	q.setDistinct(true);
	
	ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);
	cs.addConstraint(new SimpleConstraint(qfTaxonId1, ConstraintOp.EQUALS, new QueryValue("9606")));
	cs.addConstraint(new SimpleConstraint(qfTaxonId2, ConstraintOp.EQUALS, new QueryValue("9606")));


	ObjectStore os = osw.getObjectStore();

	List<String> ret = new ArrayList<String>();
	Results results = os.execute(q);
	System.out.println("There are " + results.size() + " rows.");
//	Iterator iterator = results.iterator();
//	while (iterator.hasNext()) {
//		ResultsRow<Integer> rr = (ResultsRow<Integer>) iterator.next();
//		ret.add(rr.get(0).toString());
//	}
	return ret;
}

}
