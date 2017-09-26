package org.intermine.bio.dataconversion;

import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.ConstraintOp;
import org.intermine.metadata.Model;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.Organism;
import org.intermine.model.bio.Protein;
import org.intermine.model.bio.Sequence;
import org.intermine.model.bio.Synonym;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.query.ClobAccess;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryCollectionReference;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.QueryObjectReference;
import org.intermine.objectstore.query.QueryValue;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.objectstore.query.SimpleConstraint;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;


/**
 * Processing the file 'Kinase_Substrate_Dataset' shared by PhosphoSitePlus(http://www.phosphosite.org/).
 * 
 * @author chenyian
 */
public class PspRelationConverter extends BioFileConverter
{
	private static final Logger LOG = Logger.getLogger(PspRelationConverter.class);
	//
	private static final String PHOSPHORYLATION = "Phosphorylation";
	private static final String DATASET_TITLE = "PhosphoSitePlus";
	private static final String DATA_SOURCE_NAME = "PhosphoSitePlus";

    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public PspRelationConverter(ItemWriter writer, Model model) {
        super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
    }

    /**
     * 
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		boolean flag = false;
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			if (flag) {
				if (!cols[3].equals(cols[8])) {
					LOG.info(String.format("Cross species: %s, %s -> %s", cols[0], cols[3], cols[8]));
					continue;
				}
				
				String kinaseGeneId = getGeneId(cols[0], cols[3], cols[2]);
				if (StringUtils.isEmpty(kinaseGeneId)) {
					LOG.info(String.format("Kinase Gene ID not found: %s, %s", cols[0], cols[3]));
					continue;
				}
//				LOG.info(String.format("%s, %s -> %s", cols[0], cols[3], kinaseGeneId));
				String targetGeneId = cols[5];
//				LOG.info(String.format("%s->%s", kinaseGeneId, targetGeneId));
				
				// TODO should also check synonyms 
				if (StringUtils.isEmpty(targetGeneId)) {
					targetGeneId = getGeneIdByAccession(cols[6]);
					if (StringUtils.isEmpty(targetGeneId)) {
						LOG.info(String.format("Target Gene ID not found: %s, %s, %s", cols[6], cols[7], cols[8]));
						continue;
					}
				}
				Item relation = createRelation(kinaseGeneId, targetGeneId);
				
				// identify the target protein accession
				String targetProteinId = cols[6];
				String accession = null;
				if (targetProteinId.startsWith("ENS")) {
					accession = getPrimaryAccession(targetProteinId);
					if (StringUtils.isEmpty(accession)) {
						LOG.info("Unable to find the accession: " + targetProteinId);
					}
					LOG.info(targetProteinId + " -> " + accession);
				} else if (targetProteinId.contains("_")) {
					accession = getPrimaryAccession(targetProteinId);
					if (StringUtils.isEmpty(accession)) {
						LOG.info("Unable to find the accession: " + targetProteinId);
					}
					LOG.info(targetProteinId + " -> " + accession);
				} else if (targetProteinId.contains("-")) {
					LOG.info("Skip isoform: " + targetProteinId);
				} else {
					accession = targetProteinId;
				}
				
				if (!StringUtils.isEmpty(accession)) {
					String sequence = getProteinSequence(accession);
					if (!StringUtils.isEmpty(sequence)) {
						String modifiedResidue = cols[9];
						int position = Integer.valueOf(modifiedResidue.substring(1)).intValue();
						String residue = modifiedResidue.substring(0, 1);
						
						if (position > sequence.length()) {
							LOG.info(String.format("Position is greater than the length of the sequence, skip the entry. %s: %s-%d.", targetProteinId, residue, position));
							continue;
						}
						
						String expectedResidue = sequence.substring(position - 1, position);
						if (!expectedResidue.equals(residue)) {
							if (position + 1 > sequence.length()) {
//								throw new RuntimeException(String.format("Inconsistence residue. Unable to correct the position, skip the entry. %s: %s-%d.", proteinId, residue, position)); 
								LOG.info(String.format("Inconsistence residue. Unable to correct the position, skip the entry. %s: %s-%d.", targetProteinId, residue, position));
								continue;
							}
							expectedResidue = sequence.substring(position, position + 1);
							if (expectedResidue.equals(residue)) {
								LOG.info(String.format("Inconsistence residue. Automatically correct the position. %s: %d -> %d.", targetProteinId, position, position + 1));
								position = position + 1;
							} else {
								LOG.info(String.format("Inconsistence residue. Unable to correct the position, skip the entry. %s: %s-%d.", targetProteinId, residue, position));
								continue;
							}
						}
						
						relation.addToCollection("modifications", getModification(accession, String.valueOf(position), residue));
					}
					
				}

			} else {
				if (cols[0].equals("GENE")) {
					flag = true;
				}
			}
		}
    }

	@Override
	public void close() throws Exception {
		store(relationMap.values());
	}
	
	private Map<String, String> modificationMap = new HashMap<String, String>();
	private String getModification(String accession, String position, String residue) throws ObjectStoreException {
		String key = String.format("%s-%s", accession, position);
		String ret = modificationMap.get(key);
		if (ret == null) {
			Item item = createItem("Modification");
			item.setReference("protein", getProtein(accession));
			item.setAttribute("type", PHOSPHORYLATION);
			item.setAttribute("position", position);
			item.setAttribute("residue", residue);
			store(item);

			ret = item.getIdentifier();
			modificationMap.put(key, ret);
		}
		return ret;
	}

	private Map<String, Item> relationMap = new HashMap<String, Item>();
	private Item createRelation(String geneId1, String geneId2) throws ObjectStoreException {
		String name = String.format("%s->%s", geneId1, geneId2);
		Item ret = relationMap.get(name);
		if (ret == null) {
			ret = createItem("Relation");
			ret.setReference("gene1", getGene(geneId1));
			ret.setReference("gene2", getGene(geneId2));
			ret.setAttribute("name", name);
			ret.setAttribute("text", PHOSPHORYLATION.toLowerCase());
			ret.addToCollection("types", getRelationType(PHOSPHORYLATION.toLowerCase()));
			
			relationMap.put(name, ret);
		}
		return ret;
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

	private Map<String, String> proteinMap = new HashMap<String, String>();
	private String getProtein(String uniprotAcc) throws ObjectStoreException {
		String ret = proteinMap.get(uniprotAcc);
		if (ret == null) {
			Item item = createItem("Protein");
			item.setAttribute("primaryAccession", uniprotAcc);
			store(item);
			ret = item.getIdentifier();
			proteinMap.put(uniprotAcc, ret);
		}
		return ret;
	}
	
	private Map<String, String> relationTypeMap = new HashMap<String, String>();
	private String getRelationType(String name) throws ObjectStoreException {
		String ret = relationTypeMap.get(name);
		if (ret == null) {
			Item item = createItem("RelationType");
			item.setAttribute("name", name);
			store(item);
			ret = item.getIdentifier();
			relationTypeMap.put(name, ret);
		}
		return ret;
	}

	private String osAlias = null;
	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

	private Map<String, String> geneIdMap = new HashMap<String, String>();
	@SuppressWarnings("unchecked")
	private String getGeneId(String symbol, String organism, String proteinAccession) throws Exception {
		String ret = geneIdMap.get(symbol + "-" + organism);
		
		if (ret == null) {
			Integer taxonId;
			if (organism.equals("human")) {
				taxonId = 9606;
			} else if (organism.equals("mouse")) {
				taxonId = 10090;
			} else if (organism.equals("rat")) {
				taxonId = 10116;
			} else {
				return null;
			}
			
			Query q = new Query();
			QueryClass qcGene = new QueryClass(Gene.class);
			QueryClass qcOrganism = new QueryClass(Organism.class);
			QueryField qfPrimaryIdentifier = new QueryField(qcGene, "primaryIdentifier");
			QueryField qfOrganismTaxonId = new QueryField(qcOrganism, "taxonId");
			QueryField qfSymbol = new QueryField(qcGene, "symbol");
			q.addFrom(qcGene);
			q.addFrom(qcOrganism);
			q.addToSelect(qfPrimaryIdentifier);

			ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

	        // Gene.organism
			QueryObjectReference qor = new QueryObjectReference(qcGene, "organism");
			cs.addConstraint(new ContainsConstraint(qor, ConstraintOp.CONTAINS, qcOrganism));
			cs.addConstraint(new SimpleConstraint(qfOrganismTaxonId, ConstraintOp.EQUALS, new QueryValue(taxonId)));
			cs.addConstraint(new SimpleConstraint(qfSymbol, ConstraintOp.EQUALS, new QueryValue(symbol)));
			q.setConstraint(cs);

			// LOG.info("querying for " + qs + " ......");
			ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);
			Results results = os.execute(q);
			Iterator<Object> iterator = results.iterator();
			if (iterator.hasNext()) {
				ResultsRow<String> rr = (ResultsRow<String>) iterator.next();
				ret = rr.get(0);
			} else {
				// TODO try synonyms here ...
				ret = getGeneIdByAccession(proteinAccession);
			}
			geneIdMap.put(symbol + "-" + organism, ret);
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private String getGeneIdByAccession(String proteinAccession) throws Exception {
		String ret = null;
		if (ret == null) {
			Query q = new Query();
			QueryClass qcProtein = new QueryClass(Protein.class);
			QueryClass qcGene = new QueryClass(Gene.class);
			QueryField qfPrimaryAcc = new QueryField(qcProtein, "primaryAccession");
			QueryField qfPrimaryIdentifier = new QueryField(qcGene, "primaryIdentifier");
			q.addFrom(qcProtein);
			q.addFrom(qcGene);
			q.addToSelect(qfPrimaryIdentifier);
			
			ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);
			
			QueryCollectionReference qcr = new QueryCollectionReference(qcProtein, "genes");
			cs.addConstraint(new ContainsConstraint(qcr, ConstraintOp.CONTAINS, qcGene));
			cs.addConstraint(new SimpleConstraint(qfPrimaryAcc, ConstraintOp.EQUALS, new QueryValue(proteinAccession)));
			q.setConstraint(cs);
			
			// LOG.info("querying for " + qs + " ......");
			ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);
			Results results = os.execute(q);
			Iterator<Object> iterator = results.iterator();
			if (iterator.hasNext()) {
				ResultsRow<String> rr = (ResultsRow<String>) iterator.next();
				ret = rr.get(0);
			} else {
				ret = "";
			}
		}
		return ret;
	}

	private Map<String, String> primaryAccessionMap = new HashMap<String, String>();
	@SuppressWarnings("unchecked")
	private String getPrimaryAccession(String qs) throws Exception {
		String ret = primaryAccessionMap.get(qs);

		if (ret == null) {
			Query q = new Query();
			QueryClass qcProtein = new QueryClass(Protein.class);
			QueryClass qcSynonym = new QueryClass(Synonym.class);
			QueryField qfPrimaryAcc = new QueryField(qcProtein, "primaryAccession");
			QueryField qfValue = new QueryField(qcSynonym, "value");
			q.addFrom(qcProtein);
			q.addFrom(qcSynonym);
			q.addToSelect(qfPrimaryAcc);

			ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

			QueryCollectionReference synRef = new QueryCollectionReference(qcProtein, "synonyms");
			cs.addConstraint(new ContainsConstraint(synRef, ConstraintOp.CONTAINS, qcSynonym));
			cs.addConstraint(new SimpleConstraint(qfValue, ConstraintOp.MATCHES, new QueryValue(qs
					+ "%")));
			q.setConstraint(cs);

			// LOG.info("querying for " + qs + " ......");
			ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);
			Results results = os.execute(q);
			Iterator<Object> iterator = results.iterator();
			if (iterator.hasNext()) {
				ResultsRow<String> rr = (ResultsRow<String>) iterator.next();
				ret = rr.get(0);
			} else {
				ret = "";
			}
			primaryAccessionMap.put(qs, ret);
		}

		return ret;
	}

	private Map<String, String> proteinSequenceMap = new HashMap<String, String>();

	@SuppressWarnings("unchecked")
	private String getProteinSequence(String proteinAccession) throws Exception {
		String ret = proteinSequenceMap.get(proteinAccession);

		if (ret == null) {
			Query q = new Query();
			QueryClass qcProtein = new QueryClass(Protein.class);
			QueryClass qcSequence = new QueryClass(Sequence.class);
			QueryField qfPrimaryAcc = new QueryField(qcProtein, "primaryAccession");
			QueryField qfResidues = new QueryField(qcSequence, "residues");
			q.addFrom(qcProtein);
			q.addFrom(qcSequence);
			q.addToSelect(qfResidues);

			ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

			QueryObjectReference synRef = new QueryObjectReference(qcProtein, "sequence");
			cs.addConstraint(new ContainsConstraint(synRef, ConstraintOp.CONTAINS, qcSequence));
			cs.addConstraint(new SimpleConstraint(qfPrimaryAcc, ConstraintOp.EQUALS, new QueryValue(proteinAccession)));
			q.setConstraint(cs);

			// LOG.info("querying for " + qs + " ......");
			ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);
			Results results = os.execute(q);
			Iterator<Object> iterator = results.iterator();
			if (iterator.hasNext()) {
				ResultsRow<ClobAccess> rr = (ResultsRow<ClobAccess>) iterator.next();
				ret = rr.get(0).toString();
			} else {
				ret = "";
			}
			proteinSequenceMap.put(proteinAccession, ret);
		}

		return ret;
	}

}
