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
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.model.bio.Protein;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * A new parser for Gene3D version 14.0 which parses data from 'arch_schema_cath.tsv'.
 * Need to load uniprot source in advance.
 * 
 * @author chenyian
 * 
 */
public class Gene3Dv14Converter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(Gene3Dv14Converter.class);
	//
	private static final String DATASET_TITLE = "Gene3D";
	private static final String DATA_SOURCE_NAME = "Gene3D";

	// private Item dataSet;

	private Map<String, Item> proteinMap = new HashMap<String, Item>();
	private Map<String, String> cathNodeMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public Gene3Dv14Converter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {

		// Query the existing proteins and generate id mapping.
		if (primaryAccMap == null) {
			getPrimaryIdMap();
		}
		Set<String> annotations = new HashSet<String>();

		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		int count = 0;
		while (iterator.hasNext()) {
			count++;
			String[] cols = (String[]) iterator.next();
			
			if (cols.length < 6) {
				LOG.info("Invalid line (line: " + count + "): " + StringUtils.join(cols, "\t"));
				continue;
			}
			
			String accession = cols[0];
//			String identifier = cols[1];
//			String domainId = cols[2];
			String nodeNumber = cols[3];
			String startPos = cols[4];
			String endPos = cols[5];
			
			Set<String> primaryAccs = primaryAccMap.get(accession);
			// We will only load the annotations for the existing (valid) entries.
			if (primaryAccs != null) {
				for (String pAcc : primaryAccs) {
					String key = String.format("%s_%s_%s_%s", startPos, endPos,
							pAcc, nodeNumber);
					if (!annotations.contains(key)) {
						Item protein = getProtein(pAcc);
						Item region = createStructuralDomainRegion(startPos, endPos,
								protein.getIdentifier(),
								getCathClassification(nodeNumber));
						annotations.add(key);
						
						protein.addToCollection("structuralDomains", region);
					}
				}
			}
		}
		LOG.info("There were " + annotations.size() + " StructuralDomainRegions have been created." );
		System.out.println("There were " + annotations.size() + " StructuralDomainRegions have been created.");
	}
	
	@Override
	public void close() throws Exception {
		store(proteinMap.values());
	}

	private Item getProtein(String primaryAccession) throws ObjectStoreException {
		Item ret = proteinMap.get(primaryAccession);
		if (ret == null) {
			ret = createItem("Protein");
			ret.setAttribute("primaryAccession", primaryAccession);
			proteinMap.put(primaryAccession, ret);
		}
		return ret;
	}

	private Item createStructuralDomainRegion(String start, String end, String proteinRefId,
			String cathRefId) throws ObjectStoreException {
		Item item = createItem("StructuralDomainRegion");
		item.setAttribute("start", start);
		item.setAttribute("end", end);
		item.setAttribute("regionType", "structural domain");
		item.setReference("protein", proteinRefId);
		item.setReference("cathClassification", cathRefId);
		store(item);
		
		return item;
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

		q.addFrom(qcProtein);

		q.addToSelect(qcProtein);

		Results results = os.execute(q);
		Iterator<Object> iterator = results.iterator();
		while (iterator.hasNext()) {
			ResultsRow<Protein> rr = (ResultsRow<Protein>) iterator.next();
			Protein p = rr.get(0);

			String primaryAccession = p.getPrimaryAccession();
			primaryAccMap.put(primaryAccession, new HashSet<String>());
			primaryAccMap.get(primaryAccession).add(primaryAccession);

			Set<InterMineObject> fieldValues = (Set<InterMineObject>) p
					.getFieldValue("otherAccessions");
			for (InterMineObject imo : fieldValues) {
				String accession = (String) imo.getFieldValue("accession");
				if (primaryAccMap.get(accession) == null) {
					primaryAccMap.put(accession, new HashSet<String>());
				}
				primaryAccMap.get(accession).add(primaryAccession);
			}
		}
	}

}
