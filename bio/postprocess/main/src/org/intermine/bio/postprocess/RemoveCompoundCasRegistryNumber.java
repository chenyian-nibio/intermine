package org.intermine.bio.postprocess;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.intermine.metadata.ConstraintOp;
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.objectstore.query.SimpleConstraint;

/**
 * For removing cas-registry-number in compound 
 * 
 * @author chenyian
 *
 */
public class RemoveCompoundCasRegistryNumber {

	private static final Logger LOG = Logger.getLogger(RemoveCompoundCasRegistryNumber.class);
	
	protected ObjectStoreWriter osw;

	private Model model;

	public RemoveCompoundCasRegistryNumber(ObjectStoreWriter osw) {
		this.osw = osw;
		model = Model.getInstanceByName("genomic");
	}
	
	public void removeCasNumber() {
		Results results = getCompoundsContainCasNumber();
		
		System.out.println(String.format("found %d compounds with CAS Registry Number", results.size()));
		LOG.info(String.format("found %d compounds with CAS Registry Number", results.size()));
		
		Iterator<?> iterator = results.iterator();
		
		try {
			osw.beginTransaction();

			while (iterator.hasNext()) {
				ResultsRow<?> result = (ResultsRow<?>) iterator.next();
				InterMineObject compound = (InterMineObject) result.get(0);
				compound.setFieldValue("casRegistryNumber", "");
				osw.store(compound);
			}
			
			osw.commitTransaction();

		} catch (ObjectStoreException e) {
			e.printStackTrace();
		}
	}
	
	private Results getCompoundsContainCasNumber() {
		Query q = new Query();
		QueryClass qcCompound = new QueryClass(model.getClassDescriptorByName(
				"Compound").getType());
		QueryField qfCasNumber = new QueryField(qcCompound, "casRegistryNumber");
		
		q.addFrom(qcCompound);
		q.addToSelect(qcCompound);
		q.setConstraint(new SimpleConstraint(qfCasNumber,ConstraintOp.IS_NOT_EMPTY));
		
		ObjectStore os = osw.getObjectStore();

		return os.execute(q);
	}

}
