package org.intermine.bio.dataconversion;

import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.ConstraintOp;
import org.intermine.metadata.Model;
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
 * Processing the file shared by PhosphoSitePlus(http://www.phosphosite.org/). At the moment, only
 * the phosphorylation and methylation data are used.
 * 
 * @author chenyian
 */
public class PhosphositesplusConverter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(PhosphositesplusConverter.class);
	//
	private static final String DATASET_TITLE = "PhosphoSitePlus";
	private static final String DATA_SOURCE_NAME = "PhosphoSitePlus";

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public PhosphositesplusConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		String modificationType = "";
		String currentFileName = getCurrentFile().getName();
		if (currentFileName.equals("Phosphorylation_site_dataset")) {
			modificationType = "Phosphorylation";
		} else if (currentFileName.equals("Methylation_site_dataset")) {
			modificationType = "Methylation";
		} else if (currentFileName.equals("Acetylation_site_dataset")) {
			modificationType = "Acetylation";
		} else if (currentFileName.equals("Ubiquitination_site_dataset")) {
			modificationType = "Ubiquitination";
		} else if (currentFileName.equals("Sumoylation_site_dataset")) {
			modificationType = "Sumoylation";
		} else {
			System.out.println("Unexpected file: " + currentFileName + ", skip it.");
			return;
		}

		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		boolean flag = false;
		Set<String> modiSet = new HashSet<String>();
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			if (flag) {
				String proteinId = cols[2];
				String accession = null;
				if (proteinId.startsWith("ENS")) {
					accession = getPrimaryAccession(proteinId);
					if (StringUtils.isEmpty(accession)) {
						LOG.info("Unable to find the accession: " + proteinId);
						continue;
					}
					LOG.info(proteinId + " -> " + accession);
				} else if (proteinId.contains("_")) {
					accession = getPrimaryAccession(proteinId);
					if (StringUtils.isEmpty(accession)) {
						LOG.info("Unable to find the accession: " + proteinId);
						continue;
					}
					LOG.info(proteinId + " -> " + accession);
				} else if (proteinId.contains("-")) {
					LOG.info("Skip isoform: " + proteinId);
					continue;
				} else {
					accession = proteinId;
				}
				
				String sequence = getProteinSequence(accession);
				if (!StringUtils.isEmpty(sequence)) {
					String modifiedResidue = cols[4];
					int position = Integer.valueOf(modifiedResidue.substring(1, modifiedResidue.indexOf('-'))).intValue();
					String residue = modifiedResidue.substring(0, 1);
					
					if (position > sequence.length()) {
						LOG.info(String.format("Position is greater than the length of the sequence, skip the entry. %s: %s-%d.", proteinId, residue, position));
						continue;
					}
					
					String expectedResidue = sequence.substring(position - 1, position);
					if (!expectedResidue.equals(residue)) {
						if (position + 1 > sequence.length()) {
//							throw new RuntimeException(String.format("Inconsistence residue. Unable to correct the position, skip the entry. %s: %s-%d.", proteinId, residue, position)); 
							LOG.info(String.format("Inconsistence residue. Unable to correct the position, skip the entry. %s: %s-%d.", proteinId, residue, position));
							continue;
						}
						expectedResidue = sequence.substring(position, position + 1);
						if (expectedResidue.equals(residue)) {
							LOG.info(String.format("Inconsistence residue. Automatically correct the position. %s: %d -> %d.", proteinId, position, position + 1));
							position = position + 1;
						} else {
							LOG.info(String.format("Inconsistence residue. Unable to correct the position, skip the entry. %s: %s-%d.", proteinId, residue, position));
							continue;
						}
					}
					
					String key = String.format("%s-%d", accession, position);
					
					// different methylation (e.g. m1, m2) may be annotated to the same position
					if (modiSet.contains(key)) {
						continue;
					}
					
					Item protein = getProtein(accession);
					Item modification = createItem("Modification");
					modification.setReference("protein", protein);
					modification.setAttribute("type", modificationType);
					modification.setAttribute("position", String.valueOf(position));
					modification.setAttribute("residue", residue);
					modification.setAttribute("start", String.valueOf(position));
					modification.setAttribute("end", String.valueOf(position));
					modification.setAttribute("regionType", "modification");
					store(modification);
					protein.addToCollection("modifications", modification);
					
					modiSet.add(key);
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
		store(proteinMap.values());
	}

	private Map<String, Item> proteinMap = new HashMap<String, Item>();

	private Item getProtein(String uniprotAcc) throws ObjectStoreException {
		Item ret = proteinMap.get(uniprotAcc);
		if (ret == null) {
			ret = createItem("Protein");
			ret.setAttribute("primaryAccession", uniprotAcc);
			proteinMap.put(uniprotAcc, ret);
		}
		return ret;
	}

	// get accession from integrated uniprot data
	private Map<String, String> primaryAccessionMap = new HashMap<String, String>();

	private String osAlias = null;

	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

	
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
