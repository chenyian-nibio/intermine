package org.intermine.bio.postprocess;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.log4j.Logger;
import org.intermine.bio.dataconversion.PpiViewConverter;
import org.intermine.bio.util.Constants;
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.model.bio.DataSet;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.Protein;
import org.intermine.model.bio.ProteinInteraction;
import org.intermine.model.bio.ProteinInteractionSource;
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
import org.intermine.objectstore.query.QueryObjectReference;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.postprocess.PostProcessor;
import org.intermine.util.DynamicUtil;

/***
 * Take ProteinInteraction objects assigned to proteins and convert them to Interaction objects
 * assigned to corresponding genes.
 * 
 * @author chenyian
 * 
 */

public class PpiViewPostprocess extends PostProcessor {

	private static final Logger LOG = Logger.getLogger(PpiViewPostprocess.class);

	// MI code for "association"
	private static final String INTERACT_TYPE_MI = "MI:0914";

	private Map<MultiKey, InterMineObject> expMap = new HashMap<MultiKey, InterMineObject>();
	private DataSet dataSet;
	private Model model;

	public PpiViewPostprocess(ObjectStoreWriter osw) {
		super(osw);
		model = Model.getInstanceByName("genomic");
	}

	@Override
	public void postProcess() throws ObjectStoreException {
		long startTime = System.currentTimeMillis();

		osw.beginTransaction();

		System.out.println("Start PPIView post process......");

		dataSet = (DataSet) DynamicUtil.createObject(Collections.singleton(DataSet.class));
		dataSet.setName(PpiViewConverter.DATASET_TITLE);
		dataSet = (DataSet) osw.getObjectByExample(dataSet, Collections.singleton("name"));

		if (dataSet == null) {
			LOG.error(String.format("Failed to find %s DataSet object",
					PpiViewConverter.DATASET_TITLE));
			return;
		}

		Iterator<?> resIter = findProteinInteractions();

		System.out.println("Start iteration......");

		InterMineObject interactionTerm = getInteractionTerm();

		int count = 0;
		InterMineObject lastGene = null;
		Set<InterMineObject> interactions = new HashSet<InterMineObject>();

		while (resIter.hasNext()) {
			ResultsRow<?> rr = (ResultsRow<?>) resIter.next();

			InterMineObject thisGene = (InterMineObject) rr.get(0);
			InterMineObject thisProtein = (InterMineObject) rr.get(1);
			InterMineObject thisSource = (InterMineObject) rr.get(2);
			InterMineObject partProtein = (InterMineObject) rr.get(3);
			InterMineObject partGene = (InterMineObject) rr.get(4);

			// System.out.println(String.format(
			// "gene_a:%s, protein_a:%s, gene_b:%s, protein_b:%s, source:%s; %s.", thisGene
			// .getNcbiGeneNumber(), thisProtein.getPrimaryAccession(), partGene
			// .getNcbiGeneNumber(), partProtein.getPrimaryAccession(), thisSource
			// .getDbName(), thisSource.getIdentifier()));

			try {
				String thisGeneId = thisGene.getFieldValue("ncbiGeneId").toString();
				String partGeneId = partGene.getFieldValue("ncbiGeneId").toString();
				
				if (thisGeneId == null || partGeneId == null) {
					continue;
				}

				if (lastGene != null && !(lastGene.equals(thisGene))) {
					lastGene.setFieldValue("interactions", interactions);
					osw.store(lastGene);

					interactions = new HashSet<InterMineObject>();
				}
				InterMineObject interaction = (InterMineObject) DynamicUtil
						.simpleCreateObject(model.getClassDescriptorByName("Interaction").getType());

				interaction.setFieldValue("gene1", thisGene);
				interaction.setFieldValue("gene2", partGene);
				osw.store(interaction);

				InterMineObject detail = (InterMineObject) DynamicUtil.simpleCreateObject(model
						.getClassDescriptorByName("InteractionDetail").getType());

				String intName;
				if (thisGeneId.equals(partGeneId)) {
					intName = "PPIView:" + thisProtein.getFieldValue("primaryAccession");
				} else {
					intName = "PPIView:" + thisProtein.getFieldValue("primaryAccession") + "_"
							+ partProtein.getFieldValue("primaryAccession");
				}
				detail.setFieldValue("name", intName);
				detail.setFieldValue("type", "physical");
				detail.setFieldValue("relationshipType", interactionTerm);
				detail.setFieldValue(
						"experiment",
						getExperiment(thisSource.getFieldValue("dbName").toString(), thisSource
								.getFieldValue("identifier").toString()));
				HashSet<DataSet> dataSets = new HashSet<DataSet>();
				dataSets.add(dataSet);
				detail.setFieldValue("dataSets", dataSets);
				detail.setFieldValue("interaction", interaction);
				osw.store(detail);

				interactions.add(interaction);

				lastGene = thisGene;

			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			count++;
		}
		if (lastGene != null) {
			lastGene.setFieldValue("interactions", interactions);
			osw.store(lastGene);
		}
		System.out.println(count + "ppi processed" + " - took "
				+ (System.currentTimeMillis() - startTime) + " ms.");

		osw.commitTransaction();
	}

	/**
	 * Query Gene->Protein->ProteinInteraction->Protein->Gene and return an iterator over the Gene,
	 * Protein.
	 * 
	 */
	private Iterator<?> findProteinInteractions() throws ObjectStoreException {
		Query q = new Query();

		QueryClass qcGene = new QueryClass(Gene.class);
		QueryClass qcInteractingGene = new QueryClass(Gene.class);
		QueryClass qcProtein = new QueryClass(Protein.class);
		QueryClass qcRepresentativePartner = new QueryClass(Protein.class);
		QueryClass qcProteinInteraction = new QueryClass(ProteinInteraction.class);
		QueryClass qcProteinInteractionSource = new QueryClass(ProteinInteractionSource.class);

		q.addFrom(qcGene);
		q.addFrom(qcProtein);
		q.addFrom(qcProteinInteraction);
		q.addFrom(qcProteinInteractionSource);
		q.addFrom(qcRepresentativePartner); // representative partner
		q.addFrom(qcInteractingGene); // representative partner

		q.addToSelect(qcGene);
		q.addToSelect(qcProtein);
		// q.addToSelect(qcProteinInteraction);
		q.addToSelect(qcProteinInteractionSource);
		q.addToSelect(qcRepresentativePartner); // representative partner
		q.addToSelect(qcInteractingGene); // representative partner

		q.addToOrderBy(qcGene);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		QueryCollectionReference geneProtRef = new QueryCollectionReference(qcProtein, "genes");
		cs.addConstraint(new ContainsConstraint(geneProtRef, ConstraintOp.CONTAINS, qcGene));

		QueryCollectionReference p2PIRef = new QueryCollectionReference(qcProtein,
				"proteinInteractions");
		cs.addConstraint(new ContainsConstraint(p2PIRef, ConstraintOp.CONTAINS,
				qcProteinInteraction));

		QueryCollectionReference pI2PISRef = new QueryCollectionReference(qcProteinInteraction,
				"piSources");
		cs.addConstraint(new ContainsConstraint(pI2PISRef, ConstraintOp.CONTAINS,
				qcProteinInteractionSource));

		QueryObjectReference pI2RpRef = new QueryObjectReference(qcProteinInteraction,
				"representativePartner");
		cs.addConstraint(new ContainsConstraint(pI2RpRef, ConstraintOp.CONTAINS,
				qcRepresentativePartner));

		QueryCollectionReference geneRepRef = new QueryCollectionReference(qcRepresentativePartner,
				"genes");
		cs.addConstraint(new ContainsConstraint(geneRepRef, ConstraintOp.CONTAINS,
				qcInteractingGene));

		q.setConstraint(cs);

		System.out.println("Query compiled.");

		ObjectStore os = osw.getObjectStore();
		((ObjectStoreInterMineImpl) os).precompute(q, Constants.PRECOMPUTE_CATEGORY);
		Results results = os.execute(q, 5000, true, true, true);

		System.out.println("Query executed.");

		return results.iterator();
	}

	/***
	 * Get InteractionExperiment
	 * 
	 * @param dbName
	 * @param identifier
	 * @return An InterMineObject of InteractionExperiment
	 * @throws ObjectStoreException
	 */
	private InterMineObject getExperiment(String dbName, String identifier)
			throws ObjectStoreException {
		MultiKey key = new MultiKey(dbName, identifier);

		InterMineObject ret = expMap.get(key);
		if (ret == null) {

			ret = (InterMineObject) DynamicUtil.simpleCreateObject(model.getClassDescriptorByName(
					"InteractionExperiment").getType());
			ret.setFieldValue("description", "Converted from PPIview data");

			ret.setFieldValue("sourceDb", dbName);
			ret.setFieldValue("sourceId", identifier);

			osw.store(ret);
		}
		return ret;
	}

	private InterMineObject getInteractionTerm() throws ObjectStoreException {
		InterMineObject term = (InterMineObject) DynamicUtil.simpleCreateObject(model
				.getClassDescriptorByName("InteractionTerm").getType());
		term.setFieldValue("identifier", INTERACT_TYPE_MI);
		osw.store(term);
		return term;
	}
}
