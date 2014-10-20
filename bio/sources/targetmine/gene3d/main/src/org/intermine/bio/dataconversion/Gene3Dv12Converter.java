package org.intermine.bio.dataconversion;

import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.model.bio.Protein;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.query.ConstraintOp;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryCollectionReference;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * A new parser for Gene3D version 12
 * 
 * @author chenyian
 * 
 */
public class Gene3Dv12Converter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(Gene3Dv12Converter.class);
	//
	private static final String DATASET_TITLE = "Gene3D";
	private static final String DATA_SOURCE_NAME = "Gene3D";

	// private Item dataSet;

	private Map<String, String> proteinMap = new HashMap<String, String>();
	private Map<String, String> cathNodeMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public Gene3Dv12Converter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {

		if (primaryAccMap == null) {
			getPrimaryIdMap();
		}

		Set<String> annotations = new HashSet<String>();
		
		Iterator<String[]> iterator = FormattedTextParser.parseCsvDelimitedReader(reader);
		while (iterator.hasNext()) {
			String[] cols = (String[]) iterator.next();
			String nodeNumber = cols[1];
			Pattern pattern = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+");
			Matcher matcher = pattern.matcher(nodeNumber);
			if (matcher.matches()) {
				String[] regions = cols[2].split(":");
				String accession = cols[6];
				if (regions.length % 2 != 0) {
					LOG.error(String.format("Invalid region (%s): %s", accession, cols[2]));
					continue;
				}
				Set<String> primaryAccs = primaryAccMap.get(accession);
				if (primaryAccs == null) {
					for (int i = 0; i < regions.length; i = i + 2) {
						String key = String.format("%s_%s_%s_%s", regions[i], regions[i + 1], accession, nodeNumber);
						if (!annotations.contains(key)) {
							createStructuralDomainRegion(regions[i], regions[i + 1],
									getProtein(accession, cols[9]), getCathClassification(nodeNumber));
							annotations.add(key);
						}
					}
				} else {
					for (String pAcc : primaryAccs) {
						for (int i = 0; i < regions.length; i = i + 2) {
							String key = String.format("%s_%s_%s_%s", regions[i], regions[i + 1], pAcc, nodeNumber);
							if (!annotations.contains(key)) {
								createStructuralDomainRegion(regions[i], regions[i + 1],
										getProtein(pAcc, cols[9]), getCathClassification(nodeNumber));
								annotations.add(key);
							}
						}
					}
//					LOG.info(accession + " maps to " + primaryAccs.size() + " ids.");
				}
			}
		}
	}

	private void createStructuralDomainRegion(String start, String end, String proteinRefId,
			String cathRefId) throws ObjectStoreException {
		Item item = createItem("StructuralDomainRegion");
		item.setAttribute("start", start);
		item.setAttribute("end", end);
		item.setReference("protein", proteinRefId);
		item.setReference("cathClassification", cathRefId);
		store(item);
	}

	private String getCathClassification(String nodeNumber) throws ObjectStoreException {
		String ret = cathNodeMap.get(nodeNumber);
		if (ret == null) {
			Item item = createItem("CathClassification");
			item.setAttribute("cathCode", nodeNumber);
			store(item);
			ret = item.getIdentifier();
			cathNodeMap.put(nodeNumber, ret);
		}
		return ret;
	}

	private String getProtein(String primaryAccession, String taxonId) throws ObjectStoreException {
		String ret = proteinMap.get(primaryAccession);
		if (ret == null) {
			Item item = createItem("Protein");
			item.setAttribute("primaryAccession", primaryAccession);
			item.setReference("organism", getOrganism(taxonId));
			store(item);
			ret = item.getIdentifier();
			proteinMap.put(primaryAccession, ret);
		}
		return ret;
	}

	private String osAlias = null;

	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

	private Map<String, Set<String>> primaryAccMap;

	@SuppressWarnings("unchecked")
	private void getPrimaryIdMap() throws Exception {
		primaryAccMap = new HashMap<String, Set<String>>();

		ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);

		Query q = new Query();
		QueryClass qcProtein = new QueryClass(Protein.class);
		QueryClass qcProteinAccession = new QueryClass(os.getModel()
				.getClassDescriptorByName("ProteinAccession").getType());
		QueryField qfAccession = new QueryField(qcProteinAccession, "accession");
		QueryField qfPrimaryAcc = new QueryField(qcProtein, "primaryAccession");
		q.addFrom(qcProtein);
		q.addFrom(qcProteinAccession);

		q.addToSelect(qfPrimaryAcc);
		q.addToSelect(qfAccession);

		QueryCollectionReference qcr = new QueryCollectionReference(qcProtein, "otherAccessions");
		ContainsConstraint cc = new ContainsConstraint(qcr, ConstraintOp.CONTAINS,
				qcProteinAccession);

		q.setConstraint(cc);

		Results results = os.execute(q);
		Iterator<Object> iterator = results.iterator();
		while (iterator.hasNext()) {
			ResultsRow<String> rr = (ResultsRow<String>) iterator.next();
			String primaryAcc = rr.get(0);
			String accession = rr.get(1);

			// LOG.info(String.format("%s -> %s", primaryAcc, accession));

			if (primaryAccMap.get(accession) == null) {
				primaryAccMap.put(accession, new HashSet<String>());
			}
			primaryAccMap.get(accession).add(primaryAcc);
		}
	}

}
