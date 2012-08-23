package org.intermine.bio.postprocess;

import java.util.Collections;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.intermine.bio.util.Constants;
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.model.bio.DataSet;
import org.intermine.model.bio.Protein;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.intermine.ObjectStoreInterMineImpl;
import org.intermine.objectstore.query.ConstraintOp;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryCollectionReference;
import org.intermine.objectstore.query.QueryExpression;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.QueryObjectReference;
import org.intermine.objectstore.query.QueryValue;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.objectstore.query.SimpleConstraint;
import org.intermine.postprocess.PostProcessor;
import org.intermine.util.DynamicUtil;

public class ChemblDbPostProcess extends PostProcessor {
	
	private static final String DATA_SET_NAME = "ChEMBL";
	private static final Logger LOG = Logger.getLogger(ChemblDbPostProcess.class);
	private DataSet dataSet = null;
	private Model model;

	public ChemblDbPostProcess(ObjectStoreWriter osw) {
		super(osw);
		model = Model.getInstanceByName("genomic");
	}

	@Override
	public void postProcess() throws ObjectStoreException {
		createProteinCompoundGroupInteractions();
	}

	private void createProteinCompoundGroupInteractions() throws ObjectStoreException {

		dataSet = (DataSet) DynamicUtil.createObject(Collections.singleton(DataSet.class));
		dataSet.setName(DATA_SET_NAME);
		dataSet = (DataSet) osw.getObjectByExample(dataSet, Collections.singleton("name"));
		if (dataSet == null) {
			LOG.error("Failed to find ChEMBL DataSet object");
			return;
		}

		Results results = findProteinCompoundGroup(osw.getObjectStore());
		osw.beginTransaction();

		Iterator<?> resIter = results.iterator();
		int i = 0;
		while (resIter.hasNext()) {
			ResultsRow<?> rr = (ResultsRow<?>) resIter.next();
			InterMineObject protein = (InterMineObject) rr.get(0);
			InterMineObject compoundGroup = (InterMineObject) rr.get(1);

			InterMineObject cgi = (InterMineObject) DynamicUtil.simpleCreateObject(model
					.getClassDescriptorByName("ProteinCompoundGroupInteraction").getType());

			cgi.setFieldValue("protein", protein);
			cgi.setFieldValue("compoundGroup", compoundGroup);
			cgi.setFieldValue("dataSet", dataSet);
			try {
				String primaryAcc = (String) protein.getFieldValue("primaryAccession");
				String inchiKey = (String) compoundGroup.getFieldValue("identifier");
				cgi.setFieldValue("identifier", String.format("%s_%s", primaryAcc, inchiKey));

			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}

			osw.store(cgi);
			i++;
		}

		osw.commitTransaction();
		System.out.println(i + " ProteinCompoundGroupInteraction created.");
	}

	protected static Results findProteinCompoundGroup(ObjectStore os) throws ObjectStoreException {
		Query q = new Query();
		QueryClass qcProtein = new QueryClass(Protein.class);
		QueryClass qcCompoundProteinInteraction = new QueryClass(os.getModel()
				.getClassDescriptorByName("CompoundProteinInteraction").getType());
		QueryClass qcCompound = new QueryClass(os.getModel().getClassDescriptorByName("Compound")
				.getType());
		QueryClass qcDataSet = new QueryClass(os.getModel().getClassDescriptorByName("DataSet")
				.getType());
		QueryClass qcCompoundGroup = new QueryClass(os.getModel()
				.getClassDescriptorByName("CompoundGroup").getType());

		QueryField qfDataSet = new QueryField(qcDataSet, "name");

		q.addFrom(qcProtein);
		q.addFrom(qcCompoundProteinInteraction);
		q.addFrom(qcCompound);
		q.addFrom(qcCompoundGroup);
		q.addFrom(qcDataSet);

		q.addToSelect(qcProtein);
		q.addToSelect(qcCompoundGroup);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		// Protein.compounds.compound.compoundGroup
		QueryCollectionReference c1 = new QueryCollectionReference(qcProtein, "compounds");
		cs.addConstraint(new ContainsConstraint(c1, ConstraintOp.CONTAINS,
				qcCompoundProteinInteraction));

		QueryObjectReference r2 = new QueryObjectReference(qcCompoundProteinInteraction, "compound");
		cs.addConstraint(new ContainsConstraint(r2, ConstraintOp.CONTAINS, qcCompound));

		QueryObjectReference r4 = new QueryObjectReference(qcCompoundProteinInteraction, "dataSet");
		cs.addConstraint(new ContainsConstraint(r4, ConstraintOp.CONTAINS, qcDataSet));

		QueryExpression qe2 = new QueryExpression(QueryExpression.LOWER, qfDataSet);
		cs.addConstraint(new SimpleConstraint(qe2, ConstraintOp.EQUALS, new QueryValue(DATA_SET_NAME
				.toLowerCase())));

		QueryObjectReference r3 = new QueryObjectReference(qcCompound, "compoundGroup");
		cs.addConstraint(new ContainsConstraint(r3, ConstraintOp.CONTAINS, qcCompoundGroup));

		q.setConstraint(cs);

		ObjectStoreInterMineImpl osimi = (ObjectStoreInterMineImpl) os;
		osimi.precompute(q, Constants.PRECOMPUTE_CATEGORY);
		Results res = os.execute(q);

		return res;
	}
}
