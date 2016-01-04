package org.intermine.bio.postprocess;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.intermine.bio.util.Constants;
import org.intermine.metadata.ClassDescriptor;
import org.intermine.metadata.ConstraintOp;
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.Pathway;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.intermine.ObjectStoreInterMineImpl;
import org.intermine.objectstore.intermine.ObjectStoreWriterInterMineImpl;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryCollectionReference;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.sql.DatabaseUtil;

/**
 * 
 * @author chenyian
 *
 */
public class AssociateGeneAndIPC {

	private static final Logger LOG = Logger.getLogger(AssociateGeneAndIPC.class);

	protected ObjectStoreWriter osw;

	private Model model;

	public AssociateGeneAndIPC(ObjectStoreWriter osw) {
		this.osw = osw;
		model = Model.getInstanceByName("genomic");
	}

	public void doAssociation() throws ObjectStoreException, IllegalAccessException,
			SQLException {

		Results results = findGeneIntegratedPathwayCluster(osw.getObjectStore());
		int count = 0;
		Gene lastGene = null;
		Set<InterMineObject> newCollection = new HashSet<InterMineObject>();

		osw.beginTransaction();

		Iterator<?> resIter = results.iterator();
		while (resIter.hasNext()) {
			ResultsRow<?> rr = (ResultsRow<?>) resIter.next();
			Gene thisGene = (Gene) rr.get(0);
			InterMineObject gsc = (InterMineObject) rr.get(1);
			
//			LOG.info(String.format("Gene: %s - GSC: %s", thisGene.getPrimaryIdentifier(), gsc.getFieldValue("identifier")));

			if (lastGene == null || !thisGene.getId().equals(lastGene.getId())) {
				if (lastGene != null) {
					// clone so we don't change the ObjectStore cache
					Gene tempGene = PostProcessUtil.cloneInterMineObject(lastGene);
					tempGene.setFieldValue("integratedPathwayClusters", newCollection);
					osw.store(tempGene);
					count++;
				}
				newCollection = new HashSet<InterMineObject>();
			}
			newCollection.add(gsc);
			lastGene = thisGene;
		}

		if (lastGene != null) {
			// clone so we don't change the ObjectStore cache
			Gene tempGene = PostProcessUtil.cloneInterMineObject(lastGene);
			tempGene.setFieldValue("integratedPathwayClusters", newCollection);
			osw.store(tempGene);
			count++;
		}
		LOG.info(count + " Genes have been processed.");
		
		osw.commitTransaction();

		// now ANALYSE tables relating to class that has been altered - may be rows added
		// to indirection tables
		if (osw instanceof ObjectStoreWriterInterMineImpl) {
			ClassDescriptor cld = model.getClassDescriptorByName(Gene.class.getName());
			DatabaseUtil.analyse(((ObjectStoreWriterInterMineImpl) osw).getDatabase(), cld, false);
		}

	}

	/**
	 * Run a query that returns all genes, and associated integrated pathway clusters.
	 * 
	 * @param os
	 *            the objectstore
	 * @return the Results object
	 * @throws ObjectStoreException
	 *             if there is an error while reading from the ObjectStore
	 */
	protected Results findGeneIntegratedPathwayCluster(ObjectStore os) throws ObjectStoreException {
		Query q = new Query();
		QueryClass qcGene = new QueryClass(Gene.class);
		QueryClass qcPathway = new QueryClass(Pathway.class);
		QueryClass qcGsc = new QueryClass(model.getClassDescriptorByName("IntegratedPathwayCluster")
				.getType());

		q.addFrom(qcGene);
		q.addFrom(qcPathway);
		q.addFrom(qcGsc);

		q.addToSelect(qcGene);
		q.addToSelect(qcGsc);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		QueryCollectionReference c1 = new QueryCollectionReference(qcGene, "pathways");
		cs.addConstraint(new ContainsConstraint(c1, ConstraintOp.CONTAINS, qcPathway));

		QueryCollectionReference c2 = new QueryCollectionReference(qcPathway, "integratedPathwayClusters");
		cs.addConstraint(new ContainsConstraint(c2, ConstraintOp.CONTAINS, qcGsc));

		q.setConstraint(cs);
		
		q.addToOrderBy(qcGene);

		ObjectStoreInterMineImpl osimi = (ObjectStoreInterMineImpl) os;
		osimi.precompute(q, Constants.PRECOMPUTE_CATEGORY);
		Results res = os.execute(q);

		return res;
	}

}
