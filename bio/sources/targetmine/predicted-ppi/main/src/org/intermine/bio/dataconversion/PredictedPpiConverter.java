package org.intermine.bio.dataconversion;

import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.ConstraintOp;
import org.intermine.metadata.Model;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.Organism;
import org.intermine.model.bio.Protein;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.query.BagConstraint;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryCollectionReference;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.QueryObjectReference;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;


/**
 * 
 * @author chenyian
 */
public class PredictedPpiConverter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(PredictedPpiConverter.class);
    //

    // Only human low confidence interactions are available
    private static final String TAXON_ID = "9606";
    
	private String osAlias = null;

	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public PredictedPpiConverter(ItemWriter writer, Model model) {
        super(writer, model);
    }

    /**
     * 
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
    	getProteinGeneMap();
    	
    	Iterator<String[]> iterator = FormattedTextParser.parseCsvDelimitedReader(reader);
    	
    	while (iterator.hasNext()) {
    		String[] cols = iterator.next();
    		Set<String> geneSet1 = proteinGeneMap.get(cols[0]);
    		Set<String> geneSet2 = proteinGeneMap.get(cols[1]);
    		if (geneSet1 == null || geneSet2 == null) {
//    			LOG.info(String.format("null values: %s(%s) -- %s(%s)", cols[0], geneSet1, cols[1], geneSet2));
    			continue;
    		}
    		if (geneSet1.size() > 1 || geneSet2.size() > 1) {
//    			LOG.info(String.format("multiple results: %s(%s) -- %s(%s)", cols[0], geneSet1.toString(), cols[1], geneSet2.toString()));
    			continue;
    		}
    		String gene1 = geneSet1.iterator().next();
    		String gene2 = geneSet2.iterator().next();
    		if (gene1.equals(gene2)) {
			continue;
		}
		createInteraction(gene1, gene2, cols[2]);
    	}
    	
    }
    
    private Set<String> processedInteractions = new HashSet<String>();
    private void createInteraction(String gene1, String gene2, String score) throws ObjectStoreException {
    	if (!processedInteractions.contains(gene1 + "-" + gene2) && !processedInteractions.contains(gene2 + "-" + gene1)) {
    		Item interaction1 = createItem("Interaction");
    		interaction1.setReference("gene1", getGene(gene1));
    		interaction1.setReference("gene2", getGene(gene2));
    		interaction1.setAttribute("psopiaScore", score);
    		store(interaction1);
    		
    		Item interaction2 = createItem("Interaction");
    		interaction2.setReference("gene1", getGene(gene2));
    		interaction2.setReference("gene2", getGene(gene1));
    		interaction2.setAttribute("psopiaScore", score);
    		store(interaction2);
    		
    		processedInteractions.add(gene1 + "-" + gene2);
    	} else {
    		LOG.info(String.format("duplicated pairs: %s -- %s", gene1, gene2));
    	}
    }
    
	private Map<String, String> geneMap = new HashMap<String, String>();
	private String getGene(String geneId) throws ObjectStoreException {
		String ret = geneMap.get(geneId);
		if (ret == null) {
			Item item = createItem("Gene");
			item.setAttribute("primaryIdentifier", geneId);
			store(item);
			ret = item.getIdentifier();
			geneMap.put(geneId, ret);
		}
		return ret;
	}

    private Map<String, Set<String>> proteinGeneMap = new HashMap<String, Set<String>>();
    
	@SuppressWarnings("unchecked")
	private void getProteinGeneMap() throws Exception {

		Query q = new Query();
		QueryClass qcProtein = new QueryClass(Protein.class);
		QueryClass qcGene = new QueryClass(Gene.class);
		QueryClass qcOrganism = new QueryClass(Organism.class);

		QueryField qfGeneId = new QueryField(qcGene, "primaryIdentifier");
		QueryField qfPrimaryAcc = new QueryField(qcProtein, "primaryAccession");
		QueryField qfOrganismTaxonId = new QueryField(qcOrganism, "taxonId");

		q.addFrom(qcProtein);
		q.addFrom(qcOrganism);
		q.addFrom(qcGene);
		q.addToSelect(qfPrimaryAcc);
		q.addToSelect(qfGeneId);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		// organism in our list
		cs.addConstraint(new BagConstraint(qfOrganismTaxonId, ConstraintOp.IN, Arrays.asList(Integer.valueOf(TAXON_ID))));

		QueryObjectReference qor = new QueryObjectReference(qcProtein, "organism");
		cs.addConstraint(new ContainsConstraint(qor, ConstraintOp.CONTAINS, qcOrganism));
		QueryCollectionReference qcr = new QueryCollectionReference(qcProtein, "genes");
		cs.addConstraint(new ContainsConstraint(qcr, ConstraintOp.CONTAINS, qcGene));

		q.setConstraint(cs);

		ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);

		Results results = os.execute(q);
		Iterator<Object> iterator = results.iterator();
		while (iterator.hasNext()) {
			ResultsRow<String> rr = (ResultsRow<String>) iterator.next();
			String proteinAcc = rr.get(0);
			if (proteinGeneMap.get(proteinAcc) == null) {
				proteinGeneMap.put(proteinAcc, new HashSet<String>());
			}
			proteinGeneMap.get(proteinAcc).add(rr.get(1));
		}
	}

}
