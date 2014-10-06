package org.intermine.bio.dataconversion;

import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
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
		Iterator<String[]> iterator = FormattedTextParser.parseCsvDelimitedReader(reader);
		// discard the header
		iterator.next();
		while (iterator.hasNext()) {
			String[] cols = (String[]) iterator.next();
			String nodeNumber = cols[1];
			Pattern pattern = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+");
			Matcher matcher = pattern.matcher(nodeNumber);
			if (matcher.matches()) {
				String[] regions = cols[2].split(":");
				if (regions.length % 2 != 0) {
					LOG.error(String.format("Invalid region (%s): %s", cols[6], cols[2]));
					continue;
				}
				for (int i = 0; i < regions.length; i = i + 2) {
					Item item = createItem("StructuralDomainRegion");
					item.setAttribute("start", regions[i]);
					item.setAttribute("end", regions[i + 1]);
					item.setReference("protein", getProtein(cols[6], cols[7], cols[9]));
					item.setReference("cathClassification", getCathClassification(nodeNumber));
					// item.addToCollection("dataSets", getDataset());
					// LOG.info(nodeNumber);
					store(item);

				}
			}
		}
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

	private String getProtein(String primaryAccession, String primaryIdentifier, String taxonId)
			throws ObjectStoreException {
		String ret = proteinMap.get(primaryAccession);
		if (ret == null) {
			Item item = createItem("Protein");
			item.setAttribute("primaryAccession", primaryAccession);
			// item.setAttribute("primaryIdentifier", primaryIdentifier);
			item.setReference("organism", getOrganism(taxonId));
			store(item);
			ret = item.getIdentifier();
			proteinMap.put(primaryAccession, ret);
		}
		return ret;
	}

}
