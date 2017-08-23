package org.intermine.bio.postprocess;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.intermine.metadata.ConstraintOp;
import org.intermine.model.bio.Gene;
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
 * Dump the primaryIdentifier to ncbiGeneId filed if it is empty.
 * 
 * @author chenyian
 *
 */
public class TranscribeNcbiGeneId {
	private static final Logger LOG = Logger.getLogger(TranscribeNcbiGeneId.class);
	
	protected ObjectStoreWriter osw;

	public TranscribeNcbiGeneId(ObjectStoreWriter osw) {
		this.osw = osw;
	}
	
	public void transcribeIdentifeir() {
		Results results = getGenesWithoutNcbiGeneId();
		
		System.out.println(String.format("found %d genes with no ncbiGeneId", results.size()));
		LOG.info(String.format("found %d genes with no ncbiGeneId", results.size()));
		
		Iterator<?> iterator = results.iterator();
		
		try {
			osw.beginTransaction();

			while (iterator.hasNext()) {
				ResultsRow<?> result = (ResultsRow<?>) iterator.next();
				Gene gene = (Gene) result.get(0);
				gene.setFieldValue("ncbiGeneId", gene.getPrimaryIdentifier());
				osw.store(gene);
			}
			
			// osw.abortTransaction();
			osw.commitTransaction();

		} catch (ObjectStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Results getGenesWithoutNcbiGeneId() {
		Query q = new Query();
		QueryClass qcGene = new QueryClass(Gene.class);
		QueryField qfGeneId = new QueryField(qcGene, "ncbiGeneId");
		
		q.addFrom(qcGene);
		q.addToSelect(qcGene);
		q.setConstraint(new SimpleConstraint(qfGeneId,ConstraintOp.IS_EMPTY));
		
		ObjectStore os = osw.getObjectStore();

		return os.execute(q);
	}

}
