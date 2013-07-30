package org.intermine.bio.postprocess;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.intermine.bio.util.Constants;
import org.intermine.model.InterMineObject;
import org.intermine.model.bio.GOEvidence;
import org.intermine.model.bio.GOEvidenceCode;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.OntologyTerm;
import org.intermine.model.bio.Protein;
import org.intermine.model.bio.Publication;
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
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.postprocess.PostProcessor;

public class GoSlimPostprocess extends PostProcessor {

	private static final Logger LOG = Logger.getLogger(GoSlimPostprocess.class);
	protected ObjectStore os;

	public GoSlimPostprocess(ObjectStoreWriter osw) {
		super(osw);
		this.os = osw.getObjectStore();
	}

	@Override
	public void postProcess() throws ObjectStoreException {

		long startTime = System.currentTimeMillis();

		osw.beginTransaction();

		Iterator<?> resIter = findProteinProperties();

		int count = 0;
		InterMineObject lastGene = null;
		Map<OntologyTerm, InterMineObject> annotations = new HashMap<OntologyTerm, InterMineObject>();

		while (resIter.hasNext()) {
			ResultsRow<?> rr = (ResultsRow<?>) resIter.next();
			InterMineObject thisGene = (InterMineObject) rr.get(0);
			InterMineObject annotation = (InterMineObject) rr.get(1);

			if (lastGene != null && !(lastGene.equals(thisGene))) {
				for (InterMineObject item : annotations.values()) {
					osw.store(item);
				}
				lastGene.setFieldValue("goSlimAnnotation", new HashSet(annotations.values()));
				osw.store(lastGene);

				lastGene = thisGene;
				annotations = new HashMap<OntologyTerm, InterMineObject>();
			}

			try {
				OntologyTerm term = (OntologyTerm) annotation.getFieldValue("ontologyTerm");
				Set<GOEvidence> evidence = (Set<GOEvidence>) annotation.getFieldValue("evidence");

				InterMineObject tempAnnotation;
				tempAnnotation = PostProcessUtil.copyInterMineObject(annotation);

				if (hasDupes(annotations, term, evidence, tempAnnotation)) {
					// if a dupe, merge with already created object instead of creating new
					continue;
				}
				tempAnnotation.setFieldValue("subject", thisGene);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}

			lastGene = thisGene;
			count++;
		}

		System.out.println("found " + count + " results.");

		if (lastGene != null) {
			for (InterMineObject item : annotations.values()) {
				osw.store(item);
			}
			lastGene.setFieldValue("goSlimAnnotation", new HashSet(annotations.values()));
			osw.store(lastGene);
		}

		LOG.info("Created " + count + " new GOAnnotation objects for Genes" + " - took "
				+ (System.currentTimeMillis() - startTime) + " ms.");
		 
		osw.commitTransaction();
	}

	private boolean hasDupes(Map<OntologyTerm, InterMineObject> annotations, OntologyTerm term,
			Set<GOEvidence> evidence, InterMineObject tempAnnotation) {
		boolean isDupe = false;
		InterMineObject alreadySeenAnnotation = annotations.get(term);
		if (alreadySeenAnnotation != null) {
			isDupe = true;
			mergeEvidence(evidence, alreadySeenAnnotation);
		} else {
			annotations.put(term, tempAnnotation);
		}
		return isDupe;
	}

	// we've seen this term, merge instead of storing new object
	private void mergeEvidence(Set<GOEvidence> evidence, InterMineObject alreadySeenAnnotation) {
		for (GOEvidence g : evidence) {
			GOEvidenceCode c = g.getCode();
			Set<Publication> pubs = g.getPublications();
			boolean foundMatch = false;
			try {
				Set<GOEvidence> evds = (Set<GOEvidence>) alreadySeenAnnotation
						.getFieldValue("evidence");
				for (GOEvidence alreadySeenEvidence : evds) {
					GOEvidenceCode alreadySeenCode = alreadySeenEvidence.getCode();
					Set<Publication> alreadySeenPubs = alreadySeenEvidence.getPublications();
					// we've already seen this evidence code, just merge pubs
					if (c.equals(alreadySeenCode)) {
						foundMatch = true;
						alreadySeenPubs = mergePubs(alreadySeenPubs, pubs);
					}
				}
				if (!foundMatch) {
					// we don't have this evidence code
					evds.add(g);
					alreadySeenAnnotation.setFieldValue("evidence", evds);
				}
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private Set<Publication> mergePubs(Set<Publication> alreadySeenPubs, Set<Publication> pubs) {
		Set<Publication> newPubs = new HashSet<Publication>();
		if (alreadySeenPubs != null) {
			newPubs.addAll(alreadySeenPubs);
		}
		if (pubs != null) {
			newPubs.addAll(pubs);
		}
		return newPubs;
	}

	/**
	 * Query Gene->Protein->Annotation->GOTerm and return an iterator over the Gene, Protein and
	 * GOTerm.
	 * 
	 * @param restrictToPrimaryGoTermsOnly
	 *            Only get primary Annotation items linking the gene and the go term.
	 */
	private Iterator<?> findProteinProperties() throws ObjectStoreException {
		Query q = new Query();

		q.setDistinct(false);

		QueryClass qcGene = new QueryClass(Gene.class);
		q.addFrom(qcGene);
		q.addToSelect(qcGene);
		q.addToOrderBy(qcGene);

		QueryClass qcProtein = new QueryClass(Protein.class);
		q.addFrom(qcProtein);

		QueryClass qcAnnotation = new QueryClass(os.getModel()
				.getClassDescriptorByName("GOSlimAnnotation").getType());
		q.addFrom(qcAnnotation);
		q.addToSelect(qcAnnotation);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		QueryCollectionReference qcr1 = new QueryCollectionReference(qcGene, "proteins");
		cs.addConstraint(new ContainsConstraint(qcr1, ConstraintOp.CONTAINS, qcProtein));

		QueryCollectionReference qcr2 = new QueryCollectionReference(qcProtein, "goSlimAnnotation");
		cs.addConstraint(new ContainsConstraint(qcr2, ConstraintOp.CONTAINS, qcAnnotation));

		q.setConstraint(cs);

		((ObjectStoreInterMineImpl) os).precompute(q, Constants.PRECOMPUTE_CATEGORY);
		Results res = os.execute(q, 5000, true, true, true);
		return res.iterator();
	}

}
