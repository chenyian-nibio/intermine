package org.intermine.bio.postprocess;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.intermine.metadata.ConstraintOp;
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.Organism;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.QueryObjectReference;
import org.intermine.objectstore.query.QueryValue;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.objectstore.query.SimpleConstraint;

/**
 * 
 * @author chenyian
 *
 */
public class CoExpressionInteraction {
	
	private static final Logger LOG = Logger.getLogger(CoExpressionInteraction.class);
	
	protected ObjectStoreWriter osw;

	private Model model;
	
	private static final String COEXP_FILE_NAME = "/data/bio/db/Targetmine/coexp/coexp-genes-all-ranks.id.txt";

	public CoExpressionInteraction(ObjectStoreWriter osw) {
		this.osw = osw;
		model = Model.getInstanceByName("genomic");
	}
	
	public void addCoExpressionValue() {
		Results results = queryInteractionByTaxonId("9606");
		System.out.println(results.size() + " Interactions found.");
		LOG.info(results.size() + " Interactions found.");

		Map<String,InterMineObject> interactionMap = new HashMap<String, InterMineObject>();
		Iterator<?> iterator = results.iterator();
		while (iterator.hasNext()) {
			ResultsRow<?> result = (ResultsRow<?>) iterator.next();
			InterMineObject item = (InterMineObject) result.get(0);
			Gene gene1 = (Gene) result.get(1);
			Gene gene2 = (Gene) result.get(2);

			interactionMap.put(String.format("%s-%s", gene1.getPrimaryIdentifier(), gene2.getPrimaryIdentifier()), item);
		}
			  
		BufferedReader in = null;
		int count = 0;
		try {
			osw.beginTransaction();
			in = new BufferedReader(new FileReader(COEXP_FILE_NAME));
			String line;
			while ((line = in.readLine()) != null) {
				line.trim();
				String[] cols = line.split("\\t");
				if (interactionMap.get(cols[0] + "-" + cols[1]) != null) {
					InterMineObject interaction = interactionMap.get(cols[0] + "-" + cols[1]);
					interaction.setFieldValue("coexp", Float.valueOf(cols[2]));
					osw.store(interaction);
					count++;
				}
				if (interactionMap.get(cols[1] + "-" + cols[0]) != null) {
					InterMineObject interaction = interactionMap.get(cols[1] + "-" + cols[0]);
					interaction.setFieldValue("coexp", Float.valueOf(cols[2]));
					osw.store(interaction);
					count++;
				}
			}
			in.close();
			osw.commitTransaction();
			
			System.out.println(count + " interaction co-expression values were stored.");
			LOG.info(count + " interaction co-expression values were stored.");
		} catch (ObjectStoreException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			LOG.error(e);
		} catch (IOException e) {
			LOG.error(e);
		}

	}

	private Results queryInteractionByTaxonId(String taxonId) {
		Query q = new Query();
		QueryClass qcGene1 = new QueryClass(Gene.class);
		QueryClass qcGene2 = new QueryClass(Gene.class);
		QueryClass qcOrganism1 = new QueryClass(Organism.class);
		QueryClass qcOrganism2 = new QueryClass(Organism.class);
		QueryClass qcInteraction = new QueryClass(model.getClassDescriptorByName("Interaction")
				.getType());
//		QueryClass qcConfidence = new QueryClass(model.getClassDescriptorByName(
//				"InteractionConfidence").getType());

		QueryField qfTaxonId1 = new QueryField(qcOrganism1, "taxonId");
		QueryField qfTaxonId2 = new QueryField(qcOrganism2, "taxonId");
//		QueryField qfType = new QueryField(qcConfidence, "type");

		q.addFrom(qcInteraction);
//		q.addFrom(qcConfidence);
		q.addFrom(qcGene1);
		q.addFrom(qcGene2);
		q.addFrom(qcOrganism1);
		q.addFrom(qcOrganism2);
		
		q.addToSelect(qcInteraction);
		q.addToSelect(qcGene1);
		q.addToSelect(qcGene2);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);
		QueryObjectReference qor1 = new QueryObjectReference(qcGene1, "organism");
		cs.addConstraint(new ContainsConstraint(qor1, ConstraintOp.CONTAINS, qcOrganism1));
		QueryObjectReference qor2 = new QueryObjectReference(qcGene2, "organism");
		cs.addConstraint(new ContainsConstraint(qor2, ConstraintOp.CONTAINS, qcOrganism2));
		QueryObjectReference qor3 = new QueryObjectReference(qcInteraction, "gene1");
		cs.addConstraint(new ContainsConstraint(qor3, ConstraintOp.CONTAINS, qcGene1));
		QueryObjectReference qor4 = new QueryObjectReference(qcInteraction, "gene2");
		cs.addConstraint(new ContainsConstraint(qor4, ConstraintOp.CONTAINS, qcGene2));
//		QueryCollectionReference qcr1 = new QueryCollectionReference(qcInteraction, "confidences");
//		cs.addConstraint(new ContainsConstraint(qcr1, ConstraintOp.CONTAINS, qcConfidence));
		cs.addConstraint(new SimpleConstraint(qfTaxonId1, ConstraintOp.EQUALS, new QueryValue(
				Integer.valueOf(taxonId))));
		cs.addConstraint(new SimpleConstraint(qfTaxonId2, ConstraintOp.EQUALS, new QueryValue(
				Integer.valueOf(taxonId))));
//		cs.addConstraint(new SimpleConstraint(qfType, ConstraintOp.EQUALS, new QueryValue("HCDP")));
		q.setConstraint(cs);

		ObjectStore os = osw.getObjectStore();

		return os.execute(q);
	}


}
