package org.intermine.bio.postprocess;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.intermine.metadata.ConstraintOp;
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.model.bio.Gene;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryObjectReference;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;

/**
 * 
 * @author chenyian
 *
 */
public class PpiDruggability {
	private static final Logger LOG = Logger.getLogger(PpiDruggability.class);

	protected ObjectStoreWriter osw;

	private Model model;

	private Map<String, InterMineObject> ppiDruggabilityMap = new HashMap<String, InterMineObject>();

	public PpiDruggability(ObjectStoreWriter osw) {
		this.osw = osw;
		model = Model.getInstanceByName("genomic");
	}

	private void getPpiDruggabilityMap() {
		Results results = queryPpiDruggability();
		System.out.println(results.size() + " PpiDruggabilities found.");
		LOG.info(results.size() + " PpiDruggabilities found.");

		Iterator<?> iterator = results.iterator();
		while (iterator.hasNext()) {
			ResultsRow<?> result = (ResultsRow<?>) iterator.next();
			InterMineObject item = (InterMineObject) result.get(0);
			Gene gene1 = (Gene) result.get(1);
			Gene gene2 = (Gene) result.get(2);

			ppiDruggabilityMap.put(
					String.format("%s-%s", gene1.getPrimaryIdentifier(),
							gene2.getPrimaryIdentifier()), item);
			// for the convenience ...
			ppiDruggabilityMap.put(
					String.format("%s-%s", gene2.getPrimaryIdentifier(),
							gene1.getPrimaryIdentifier()), item);
		}
	}

	public void annotatePpiDruggabilities() {
		getPpiDruggabilityMap();

		Results results = queryInteractions();

		System.out.println(results.size() + " interactions found.");
		LOG.info(results.size() + " interactions found.");

		Iterator<?> iterator = results.iterator();

		int count = 0;
		try {
			osw.beginTransaction();
			while (iterator.hasNext()) {
				ResultsRow<?> result = (ResultsRow<?>) iterator.next();
				InterMineObject interaction = (InterMineObject) result.get(0);
				Gene gene1 = (Gene) result.get(1);
				Gene gene2 = (Gene) result.get(2);

				InterMineObject ppiDruggability = ppiDruggabilityMap.get(String.format("%s-%s",
						gene1.getPrimaryIdentifier(), gene2.getPrimaryIdentifier()));

				if (ppiDruggability != null) {
					interaction.setFieldValue("ppiDruggability", ppiDruggability);
					osw.store(interaction);

					count++;
				} else {
					LOG.info(String.format("Unfound pair: %s - %s", gene1.getPrimaryIdentifier(),
							gene2.getPrimaryIdentifier()));
				}

			}
			// osw.abortTransaction();
			osw.commitTransaction();

		} catch (ObjectStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(String.format(
				"There were %d interactions annotated with ppi druggability.", count));
		LOG.info(String
				.format("There were %d interactions annotated with ppi druggability.", count));

	}

	private Results queryInteractions() {
		Query q = new Query();
		QueryClass qcGene1 = new QueryClass(Gene.class);
		QueryClass qcGene2 = new QueryClass(Gene.class);
		QueryClass qcInteraction = new QueryClass(model.getClassDescriptorByName(
				"Interaction").getType());

		q.addFrom(qcInteraction);
		q.addFrom(qcGene1);
		q.addFrom(qcGene2);
		q.addToSelect(qcInteraction);
		q.addToSelect(qcGene1);
		q.addToSelect(qcGene2);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);
		QueryObjectReference qor3 = new QueryObjectReference(qcInteraction, "gene1");
		cs.addConstraint(new ContainsConstraint(qor3, ConstraintOp.CONTAINS, qcGene1));
		QueryObjectReference qor4 = new QueryObjectReference(qcInteraction, "gene2");
		cs.addConstraint(new ContainsConstraint(qor4, ConstraintOp.CONTAINS, qcGene2));
		q.setConstraint(cs);

		ObjectStore os = osw.getObjectStore();

		return os.execute(q);
	}

	private Results queryPpiDruggability() {
		Query q = new Query();
		QueryClass qcGene1 = new QueryClass(Gene.class);
		QueryClass qcGene2 = new QueryClass(Gene.class);
		QueryClass qcPpiDruggability = new QueryClass(model.getClassDescriptorByName(
				"PpiDruggability").getType());

		q.addFrom(qcPpiDruggability);
		q.addFrom(qcGene1);
		q.addFrom(qcGene2);
		q.addToSelect(qcPpiDruggability);
		q.addToSelect(qcGene1);
		q.addToSelect(qcGene2);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);
		QueryObjectReference qor3 = new QueryObjectReference(qcPpiDruggability, "gene1");
		cs.addConstraint(new ContainsConstraint(qor3, ConstraintOp.CONTAINS, qcGene1));
		QueryObjectReference qor4 = new QueryObjectReference(qcPpiDruggability, "gene2");
		cs.addConstraint(new ContainsConstraint(qor4, ConstraintOp.CONTAINS, qcGene2));
		q.setConstraint(cs);

		ObjectStore os = osw.getObjectStore();

		return os.execute(q);
	}
}
