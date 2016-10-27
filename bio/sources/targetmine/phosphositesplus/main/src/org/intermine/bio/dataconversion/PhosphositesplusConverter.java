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
import org.intermine.model.bio.Synonym;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryCollectionReference;
import org.intermine.objectstore.query.QueryField;
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
					accession = getgetPrimaryAccession(proteinId);
					if (StringUtils.isEmpty(accession)) {
						LOG.info("Unable to find the accession: " + proteinId);
						continue;
					}
					LOG.info(proteinId + " -> " + accession);
				} else if (proteinId.contains("_")) {
					accession = getgetPrimaryAccession(proteinId);
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

				String modifiedResidue = cols[4];
				String position = modifiedResidue.substring(1, modifiedResidue.indexOf('-'));
				String key = String.format("%s-%s", accession, position);

				// different methylation (e.g. m1, m2) may be annotated to the same position
				if (modiSet.contains(key)) {
					continue;
				}

				Item modification = createItem("Modification");
				modification.setReference("protein", getProtein(accession));
				modification.setAttribute("type", modificationType);
				modification.setAttribute("position", position);
				store(modification);

				modiSet.add(key);
			} else {
				if (cols[0].equals("GENE")) {
					flag = true;
				}
			}
		}

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

	// get accession from integrated uniprot data
	private Map<String, String> primaryAccessionMap = new HashMap<String, String>();

	private String osAlias = null;

	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

	@SuppressWarnings("unchecked")
	private String getgetPrimaryAccession(String qs) throws Exception {
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

}
