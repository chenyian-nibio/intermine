package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.model.bio.Protein;
import org.intermine.model.bio.Synonym;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.query.ConstraintOp;
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
 * 
 * @author chenyian
 * 
 *         refined the compound model on 2012/8/10
 */
public class StitchConverter extends BioFileConverter {
	// Threshold of evidence score (experimental) , 700 means 0.7
	private static int THRESHOLD = 700;

	private static final Logger LOG = Logger.getLogger(StitchConverter.class);
	//
	private static final String DATASET_TITLE = "STITCH";
	private static final String DATA_SOURCE_NAME = "STITCH: Chemical-Protein Interactions";

	private Map<String, String> proteinMap = new HashMap<String, String>();
	private Map<String, String> pubChemCompoundMap = new HashMap<String, String>();

	private Set<String> interactiondSet = new HashSet<String>();

	// get id map from integrated data
	private Map<String, Set<String>> primaryIdMap;
	private String osAlias = null;

	/**
	 * Set the ObjectStore alias.
	 * 
	 * @param osAlias
	 *            The ObjectStore alias
	 */
	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public StitchConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {

		// should only generate once
		if (primaryIdMap == null) {
			getPrimaryIdMap();
		}
		if (primaryIdMap.isEmpty()) {
			throw new RuntimeException(
					"No primary id mapping found. Forget to load uniprot before loading stitch?");
		}

		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(reader));

		int count = 0;

		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			// example CID010939283 CID110928783
			// the first digit is not a part of pubchem identifier
			String cid = cols[0].substring(4).replaceAll("^0*", "");
			// String ensemblId = cols[1].substring(cols[1].indexOf(".") + 1);
			String ensemblId = StringUtils.substringAfter(cols[1], ".");
			Set<String> uniprotIds = primaryIdMap.get(ensemblId);

			if (uniprotIds != null) {
				// only experiment data will be integrated
				String evidence = "experimental";
				if (Integer.valueOf(cols[2]).intValue() < THRESHOLD) {
					count++;
					continue;
				}
				if (interactiondSet.contains(ensemblId + "-" + cid)) {
					count++;
					continue;
				}
				for (String primaryIdentifier : uniprotIds) {
					Item si = createItem("StitchInteraction");
					si.setAttribute("identifier", cols[0] + "_" + cols[1]);
					si.setReference("compound", getPubChemCompound(cid));
					si.setAttribute("score", cols[2]);
					si.setAttribute("evidence", evidence);
					si.setReference("protein", getProtein(primaryIdentifier));
					store(si);
				}

				interactiondSet.add(ensemblId + "-" + cid);
			} else {
				count++;
				// LOG.info(String.format("Uniprot ID for '%s' was not found.", ensemblId));
			}
		}
		LOG.info(String.format("%d interactions were skipped.", count));
	}

	private String getProtein(String primaryIdentifier) throws ObjectStoreException {
		String ret = proteinMap.get(primaryIdentifier);
		if (ret == null) {
			Item item = createItem("Protein");
			item.setAttribute("primaryIdentifier", primaryIdentifier);
			store(item);
			ret = item.getIdentifier();
			proteinMap.put(primaryIdentifier, ret);
		}
		return ret;
	}

	private String getPubChemCompound(String cid) throws ObjectStoreException {
		String ret = pubChemCompoundMap.get(cid);
		if (ret == null) {
			Item item = createItem("PubChemCompound");
			item.setAttribute("pubChemCid", cid);
			item.setAttribute("identifier", String.format("PubChem: %s", cid));

			store(item);
			ret = item.getIdentifier();
			pubChemCompoundMap.put(cid, ret);
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	private void getPrimaryIdMap() throws Exception {
		primaryIdMap = new HashMap<String, Set<String>>();

		Query q = new Query();
		QueryClass qcSynonym = new QueryClass(Synonym.class);
		QueryClass qcProtein = new QueryClass(Protein.class);
		QueryField qfValue = new QueryField(qcSynonym, "value");
		QueryField qfPrimaryId = new QueryField(qcProtein, "primaryIdentifier");
		q.addFrom(qcSynonym);
		q.addFrom(qcProtein);
		q.addToSelect(qfValue);
		q.addToSelect(qfPrimaryId);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		QueryCollectionReference synRef = new QueryCollectionReference(qcProtein, "synonyms");
		cs.addConstraint(new ContainsConstraint(synRef, ConstraintOp.CONTAINS, qcSynonym));

		cs.addConstraint(new SimpleConstraint(qfValue, ConstraintOp.MATCHES, new QueryValue("ENS%")));

		q.setConstraint(cs);

		ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);

		Results results = os.execute(q);
		Iterator<Object> iterator = results.iterator();
		while (iterator.hasNext()) {
			ResultsRow<String> rr = (ResultsRow<String>) iterator.next();
			// LOG.info(String.format("ens: %s; uni: %s...", rr.get(0), rr.get(1)));
			if (primaryIdMap.get(rr.get(0)) == null) {
				primaryIdMap.put(rr.get(0), new HashSet<String>());
			}
			primaryIdMap.get(rr.get(0)).add(rr.get(1));
		}
	}

}
