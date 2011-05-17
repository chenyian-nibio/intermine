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
import org.intermine.model.bio.DataSet;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.Interaction;
import org.intermine.model.bio.InteractionExperiment;
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
	protected ObjectStore os;

	private Map<MultiKey, InteractionExperiment> expMap = new HashMap<MultiKey, InteractionExperiment>();
	private DataSet dataSet;

	public PpiViewPostprocess(ObjectStoreWriter osw) {
		super(osw);
		this.os = osw.getObjectStore();
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

		int count = 0;
		Gene lastGene = null;
		Set<Interaction> interactions = new HashSet<Interaction>();

		while (resIter.hasNext()) {
			ResultsRow<?> rr = (ResultsRow<?>) resIter.next();

			Gene thisGene = (Gene) rr.get(0);
			Protein thisProtein = (Protein) rr.get(1);
			ProteinInteractionSource thisSource = (ProteinInteractionSource) rr.get(2);
			Protein partProtein = (Protein) rr.get(3);
			Gene partGene = (Gene) rr.get(4);

			// System.out.println(String.format(
			// "gene_a:%s, protein_a:%s, gene_b:%s, protein_b:%s, source:%s; %s.", thisGene
			// .getNcbiGeneNumber(), thisProtein.getPrimaryAccession(), partGene
			// .getNcbiGeneNumber(), partProtein.getPrimaryAccession(), thisSource
			// .getDbName(), thisSource.getIdentifier()));

			if (thisGene.getNcbiGeneNumber() == null || partGene.getNcbiGeneNumber() == null) {
				continue;
			}

			if (lastGene != null && !(lastGene.equals(thisGene))) {
				lastGene.setInteractions(interactions);
				osw.store(lastGene);

				interactions = new HashSet<Interaction>();
			}
			Interaction interaction = (Interaction) DynamicUtil.createObject(Collections
					.singleton(Interaction.class));
			interaction.setGene(thisGene);
			interaction.addInteractingGenes(partGene);
			String intName;
			String sName;
			if (thisGene.getNcbiGeneNumber().equals(partGene.getNcbiGeneNumber())) {
				intName = "PPIView:" + thisProtein.getPrimaryAccession();
				sName = "PPIView:" + thisGene.getNcbiGeneNumber();
			} else {
				intName = "PPIView:" + thisProtein.getPrimaryAccession() + "_"
						+ partProtein.getPrimaryAccession();
				sName = "PPIView:" + thisGene.getNcbiGeneNumber() + "_"
						+ partGene.getNcbiGeneNumber();
			}
			interaction.setShortName(sName);
			interaction.setName(intName);
			interaction.setInteractionType("physical");
			interaction.setExperiment(getExperiment(thisSource.getDbName(), thisSource
					.getIdentifier()));
			interaction.addDataSets(dataSet);

			osw.store(interaction);
			interactions.add(interaction);

			lastGene = thisGene;

			count++;
		}
		if (lastGene != null) {
			lastGene.setInteractions(interactions);
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

		((ObjectStoreInterMineImpl) os).precompute(q, Constants.PRECOMPUTE_CATEGORY);
		Results results = os.execute(q, 5000, true, true, true);

		System.out.println("Query executed.");

		return results.iterator();
	}

	/***
	 * Get InteractionExperiment, to be modified.
	 * 
	 * @param dbName
	 * @param identifier
	 * @return
	 * @throws ObjectStoreException
	 */
	private InteractionExperiment getExperiment(String dbName, String identifier)
			throws ObjectStoreException {
		MultiKey key = new MultiKey(dbName, identifier);

		InteractionExperiment ret = expMap.get(key);
		if (ret == null) {

			ret = (InteractionExperiment) DynamicUtil.createObject(Collections
					.singleton(InteractionExperiment.class));
			ret.setDescription("Converted from PPIview data");

			ret.setSourceDb(dbName);
			ret.setSourceId(identifier);

			osw.store(ret);
		}
		return ret;
	}

}
