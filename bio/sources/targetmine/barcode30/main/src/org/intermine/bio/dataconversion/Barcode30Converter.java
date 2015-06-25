package org.intermine.bio.dataconversion;

import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class Barcode30Converter extends BioFileConverter {
	//
	private static final String DATASET_TITLE = "Barcode 3.0";
	private static final String DATA_SOURCE_NAME = "Barcode";

	private Map<String, String> probeSetMap = new HashMap<String, String>();
	private Map<String, String> platformMap = new HashMap<String, String>();
	private Map<String, String> tissueMap = new HashMap<String, String>();

	private Map<String, String> platformInfo = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public Barcode30Converter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		// HGU133a (Human) v3 abc-gpl96-formatted_v3.csv
		// HGU133plus2 (Human) tissues v3 abc-tis-gpl570-formatted_v3.csv
		// HGU133plus2 (Human) cells v3 abc-cell-gpl570-formatted_v3.csv
		// Mouse4302 (Mouse) v3 abc-gpl1261-formatted_v3.csv
		// HGU133a2 (Human) v3 abc-gpl571-formatted_v3.csv

		// identify the platform
		String fn = getCurrentFile().getName();
		String platformId = "";
		for (String part : fn.split("-")) {
			if (part.startsWith("gpl")) {
				platformId = part.toUpperCase();
			}
		}
		if (platformId.equals("")) {
			throw new RuntimeException("Unexpected file name: " + fn
					+ ". Unable to identify the platform.");
		}
		String platform = getPlatform(platformId);

		// start to parse the file
		Iterator<String[]> iterator = FormattedTextParser.parseCsvDelimitedReader(reader);
		String[] headers = iterator.next();
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String probeSetId = cols[0];

			if (probeSetId.startsWith("AFFX")) {
				// skip the internal probe
				continue;
			}

			for (int i = 1; i < cols.length; i++) {
				if (Float.valueOf(cols[i]) > 0) {
					// new Expression
					Item item = createItem("Expression");
					item.setAttribute("value", cols[i]);
					item.setAttribute("isExpressed", (Float.valueOf(cols[i]) >= 0.5 ?"true":"false"));
					String tissueName = headers[i];
					// TODO to be confirmed
					// "centrocytes:centrocytes" should be save as "centrocytes"
					// but "t_cells:central_memory_t_cell" should remain full string
					String[] splits = tissueName.split(":");
					if (splits.length == 2 && splits[0].equals(splits[1])) {
						tissueName = splits[0];
					}
					item.setReference("tissue", getTissue(tissueName));
					item.setReference("probeSet", getProbSet(probeSetId));
					item.setReference("platform", platform);
					store(item);
				}
			}
		}
	}

	private String getProbSet(String probeSetId) throws ObjectStoreException {
		String ret = probeSetMap.get(probeSetId);
		if (ret == null) {
			Item item = createItem("ProbeSet");
			item.setAttribute("probeSetId", probeSetId);
			store(item);
			ret = item.getIdentifier();
			probeSetMap.put(probeSetId, ret);
		}
		return ret;
	}

	private String getTissue(String identifier) throws ObjectStoreException {
		String ret = tissueMap.get(identifier);
		if (ret == null) {
			Item item = createItem("Tissue");
			item.setAttribute("identifier", identifier);
			item.setAttribute("name", identifier.replaceAll("_", " "));
			store(item);
			ret = item.getIdentifier();
			tissueMap.put(identifier, ret);
		}
		return ret;
	}

	private String getPlatform(String platformId) throws ObjectStoreException {
		String ret = platformMap.get(platformId);
		if (ret == null) {
			Item item = createItem("MicroarrayPlatform");
			item.setAttribute("identifier", platformId);
			String title = platformInfo.get(platformId);
			if (title == null) {
				throw new RuntimeException("Title should not be null: " + platformId);
			}
			item.setAttribute("title", title);
			if (title.contains("Human")) {
				item.setReference("organism", getOrganism("9606"));
			} else if (title.contains("Mouse")) {
				item.setReference("organism", getOrganism("10090"));
			} 
			store(item);
			ret = item.getIdentifier();
			platformMap.put(platformId, ret);
		}
		return ret;
	}

	{
		platformInfo.put("GPL96", "[HG-U133A] Affymetrix Human Genome U133A Array");
		platformInfo.put("GPL570", "[HG-U133_Plus_2] Affymetrix Human Genome U133 Plus 2.0 Array");
		platformInfo.put("GPL1261", "[Mouse430_2] Affymetrix Mouse Genome 430 2.0 Array");
		platformInfo.put("GPL571", "[HG-U133A_2] Affymetrix Human Genome U133A 2.0 Array");
	}
}
