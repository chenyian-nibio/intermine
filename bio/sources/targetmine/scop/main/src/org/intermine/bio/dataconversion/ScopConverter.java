package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author ishikawa
 * @author chenyian 2012.2.23 refactoring; 2012.11.5 refactoring
 */
public class ScopConverter extends BioFileConverter {

	// private static Logger LOG = Logger.getLogger(ScopConverter.class);

	private static final String DATASET_TITLE = "SCOP";
	private static final String DATA_SOURCE_NAME = "SCOP";

	private static Map<String, String> levelMap = new HashMap<String, String>();
	{
		levelMap.put("cl", "Class");
		levelMap.put("cf", "Fold");
		levelMap.put("sf", "Superfamily");
		levelMap.put("fa", "Family");
		levelMap.put("dm", "Protein");
		levelMap.put("sp", "Species");
		levelMap.put("px", "Domain");
	}

	private Map<Integer, Item> scopEntryMap = new HashMap<Integer, Item>();

//	private Map<Integer, String> domainIdMap = new HashMap<Integer, String>();

	private Map<String, String> proteinChainMap = new HashMap<String, String>();

	private Set<String> savedEntries = new HashSet<String>();

	private Pattern regionPattern = Pattern.compile("([-]?\\d+)([A-Z]?)-([-]?\\d+)([A-Z]?)");

	// dir.cla.scop.txt
	private File claFile;

	public ScopConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	@Override
	public void process(Reader reader) throws Exception {

		importDesFileToItems(reader);

		readClsFile();

	}

	/**
	 * Read dir.des.scop.txt file. Create Items and fill information to them.
	 * 
	 * @throws IOException
	 */
	private void importDesFileToItems(Reader reader) throws IOException {

		BufferedReader br = new BufferedReader(reader);

		int lineCount = 0;

		while (br.ready()) {
			lineCount++;
			String line = br.readLine();

			if (null == line || "".equals(line) || line.startsWith("#")) {
				continue;
			}

			String[] fields = line.split("\t");

			if (fields.length != 5) {
				throw new IOException("Error reading dir.des.scop.txt. line:" + lineCount + " "
						+ line);
			}

			Integer sunid = Integer.valueOf(fields[0]);
			Item oItem = createItem("ScopClassification");
			oItem.setAttribute("type", "SCOP");
			oItem.setAttribute("level", levelMap.get(fields[1]));

//			if (!fields[3].equals("-")) {
//				domainIdMap.put(sunid, fields[3]);
//			}

			oItem.setAttribute("code", fields[2]);
			oItem.setAttribute("sunid", fields[0]);
			oItem.setAttribute("sccs", fields[2]);
			oItem.setAttribute("description", fields[4]);

			scopEntryMap.put(sunid, oItem);
		}
	}

	/**
	 * Read clsFile to generate the hierarchy structure of scop entries
	 * 
	 * @throws IOException
	 * 
	 * @throws ObjectStoreException
	 * 
	 */
	private void readClsFile() throws IOException, ObjectStoreException {
		if (null == claFile) {
			throw new NullPointerException("claFile property not set");
		}

		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(new FileReader(claFile)));

		Pattern pattern = Pattern
				.compile("cl=(\\d+),cf=(\\d+),sf=(\\d+),fa=(\\d+),dm=(\\d+),sp=(\\d+),px=(\\d+)");
		// content start
		while (iterator.hasNext()) {
			String[] cols = iterator.next();

			Matcher matcher = pattern.matcher(cols[5]);
			if (matcher.matches()) {
				List<String> parentRefIds = new ArrayList<String>();
				for (int i = 0; i < 7; i++) {
					Integer identifier = Integer.valueOf(matcher.group(i + 1));
					Item item = scopEntryMap.get(identifier);
					if (savedEntries.contains(item.getIdentifier())) {
						continue;
					}
					if (i > 0) {
						parentRefIds.add(scopEntryMap.get(Integer.valueOf(matcher.group(i)))
								.getIdentifier());
						item.setCollection("parents", parentRefIds);
					}
					if (i == 6) {
						createStructuralRegion(cols[1], cols[2], item.getIdentifier());
					}
					store(item);
					savedEntries.add(item.getIdentifier());
				}
			} else {
				throw new RuntimeException("Unexpected string format: " + cols[5]);
			}

		}

	}

	private void createStructuralRegion(String pdbid, String chainRegion, String referencefId)
			throws ObjectStoreException {
		String[] regions = chainRegion.split(",");
		for (String region : regions) {

			String[] strChainFields = region.split(":");
			Item item = createItem("StructuralRegion");

			if (1 < strChainFields.length) {
				Matcher matcher = regionPattern.matcher(strChainFields[1]);
				if (matcher.matches()) {
					item.setAttribute("start", matcher.group(1));
					item.setAttribute("end", matcher.group(3));
					String sic = matcher.group(2);
					if (!sic.equals("")) {
						item.setAttribute("startInsertionCode", sic);
					}
					String eic = matcher.group(4);
					if (!eic.equals("")) {
						item.setAttribute("endInsertionCode", eic);
					}
				} else {
					throw new RuntimeException(String.format(
							"Unexpected region format: %s, at %s %s", region, pdbid, chainRegion));
				}
			}

			item.setReference("structuralDomain", referencefId);
			item.setReference("proteinChain", getProteinChain(pdbid, strChainFields[0]));

			store(item);
		}

	}

	private String getProteinChain(String pdbId, String chainId) throws ObjectStoreException {
		String identifier = pdbId + chainId;
		String ret = proteinChainMap.get(identifier);
		if (ret == null) {
			Item item = createItem("ProteinChain");
			item.setAttribute("pdbId", pdbId);
			item.setAttribute("chain", chainId);
			item.setAttribute("identifier", identifier);
			store(item);
			ret = item.getIdentifier();
			proteinChainMap.put(identifier, ret);
		}
		return ret;
	}

	public void setClsFile(File claFile) {
		this.claFile = claFile;
	}

}
