package org.intermine.bio.postprocess;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.intermine.metadata.ConstraintOp;
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryCollectionReference;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.postprocess.PostProcessor;

/**
 * Copy the DiseaseTerms from ClinicalAssertion to Variation
 * 
 * @author chenyian
 *
 */
public class TmClinvarPostProcess extends PostProcessor {

	private static final Logger LOG = Logger.getLogger(TmClinvarPostProcess.class);

	private Model model;

	public TmClinvarPostProcess(ObjectStoreWriter osw) {
		super(osw);
		model = Model.getInstanceByName("genomic");
	}

	@Override
	public void postProcess() throws ObjectStoreException {
		Results results = getVariationAnnotationFunction(osw.getObjectStore());

		InterMineObject lastVar = null;
		Set<InterMineObject> diseaseSet = new HashSet<InterMineObject>();

		osw.beginTransaction();

		Iterator<?> resIter = results.iterator();
		int count = 0;
		while (resIter.hasNext()) {
			ResultsRow<?> rr = (ResultsRow<?>) resIter.next();
			InterMineObject thisVar = (InterMineObject) rr.get(0);
			InterMineObject disease = (InterMineObject) rr.get(1);

			if (lastVar == null || !thisVar.getId().equals(lastVar.getId())) {
				if (lastVar != null) {
					try {
						InterMineObject tempVar = PostProcessUtil.cloneInterMineObject(lastVar);
						tempVar.setFieldValue("diseaseTerms", diseaseSet);
						osw.store(tempVar);
						count++;
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				diseaseSet = new HashSet<InterMineObject>();
			}
			diseaseSet.add(disease);
			lastVar = thisVar;
		}
		// last entry...
		if (lastVar != null) {
			try {
				InterMineObject tempVar = PostProcessUtil.cloneInterMineObject(lastVar);
				tempVar.setFieldValue("diseaseTerms", diseaseSet);
				osw.store(tempVar);
				count++;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		osw.commitTransaction();

		LOG.info(String.format("%d Variations have been processed.", count));

	}

	private Results getVariationAnnotationFunction(ObjectStore os) throws ObjectStoreException {
		Query q = new Query();
		QueryClass qcVariation = new QueryClass(
				model.getClassDescriptorByName("Variation").getType());
		QueryClass qcClinicalAssertion = new QueryClass(
				model.getClassDescriptorByName("ClinicalAssertion").getType());
		QueryClass qcDiseaseTerm = new QueryClass(
				model.getClassDescriptorByName("DiseaseTerm").getType());

		q.addFrom(qcVariation);
		q.addFrom(qcClinicalAssertion);
		q.addFrom(qcDiseaseTerm);

		q.addToSelect(qcVariation);
		q.addToSelect(qcDiseaseTerm);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		QueryCollectionReference c1 = new QueryCollectionReference(qcVariation,
				"clinicalAssertions");
		cs.addConstraint(new ContainsConstraint(c1, ConstraintOp.CONTAINS, qcClinicalAssertion));
		QueryCollectionReference c2 = new QueryCollectionReference(qcClinicalAssertion,
				"diseaseTerms");
		cs.addConstraint(new ContainsConstraint(c2, ConstraintOp.CONTAINS, qcDiseaseTerm));

		q.setConstraint(cs);
		q.setDistinct(true);
		q.addToOrderBy(qcVariation);

		return os.execute(q);
	}
}
