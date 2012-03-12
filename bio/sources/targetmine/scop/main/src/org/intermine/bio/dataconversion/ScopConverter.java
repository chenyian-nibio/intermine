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

import org.intermine.dataconversion.FileConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author ishikawa
 * @author chenyian 2012.2.23 refactoring again
 */
public class ScopConverter extends FileConverter {

//	private static Logger m_oLogger = Logger.getLogger(ScopConverter.class);

	// <sunid, Item of ScopEntry's subclasses or ProteinChainRegion>
	private Map<Integer, Item> m_oScopEntryMap = new HashMap<Integer, Item>();

	private HashMap<String, String> proteinChainMap = new HashMap<String, String>();

	private Set<String> savedEntries = new HashSet<String>();

	private Pattern regionPattern = Pattern.compile("([-]?\\d+)([A-Z]?)-([-]?\\d+)([A-Z]?)");

	// dir.cla.scop.txt
	private File claFile;

	public ScopConverter(ItemWriter writer, Model model) {
		super(writer, model);
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

		BufferedReader oBr = new BufferedReader(reader);

		int iLineCount = 0;

		while (oBr.ready()) {
			iLineCount++;
			String strLine = oBr.readLine();

			if (null == strLine || "".equals(strLine) || strLine.startsWith("#")) {
				continue;
			}

			String[] a_strFields = strLine.split("\t");

			if (a_strFields.length != 5) {
				throw new IOException("dir.des.scop.txt is invalid. line:" + iLineCount + " "
						+ strLine);
			}

			Integer iSunId = Integer.valueOf(a_strFields[0]);
			String strType = a_strFields[1];
			String strSccs = a_strFields[2];
			String strDescription = a_strFields[4];

			Item oItem = createItem("ScopEntry");

			if ("cl".equals(strType)) {
				// ScopClass
				oItem.setAttribute("type", "ScopClass");
			} else if ("cf".equals(strType)) {
				// ScopFold
				oItem.setAttribute("type", "ScopFold");
			} else if ("sf".equals(strType)) {
				// ScopSuperfamily
				oItem.setAttribute("type", "ScopSuperFamily");
			} else if ("fa".equals(strType)) {
				// ScopFamily
				oItem.setAttribute("type", "ScopFamily");
			} else if ("dm".equals(strType)) {
				// ScopProtein
				oItem.setAttribute("type", "ScopDomain");
			} else if ("sp".equals(strType)) {
				// ScopSpecies
				oItem.setAttribute("type", "ScopSpecies");
			} else if ("px".equals(strType)) {
				// ScopDomain
				oItem.setAttribute("type", "ScopProtein");
			}

			oItem.setAttribute("sunid", a_strFields[0]);
			oItem.setAttribute("sccs", strSccs);
			oItem.setAttribute("description", strDescription);

			m_oScopEntryMap.put(iSunId, oItem);
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
					Item item = m_oScopEntryMap.get(Integer.valueOf(matcher.group(i + 1)));
					if (i > 0) {
						parentRefIds.add(m_oScopEntryMap.get(Integer.valueOf(matcher.group(i)))
								.getIdentifier());
					}
					if (!savedEntries.contains(item.getIdentifier())) {
						item.setCollection("parents", parentRefIds);
						store(item);
						savedEntries.add(item.getIdentifier());
					}
					if (i == 6) {
						createScopRegion(cols[1], cols[2], item.getIdentifier());
					}
				}
			} else {
				throw new RuntimeException("Unexpected string format: " + cols[5]);
			}

		}

	}

	private void createScopRegion(String pdbid, String chainRegion, String scopProteinRefId)
			throws ObjectStoreException {
		String[] regions = chainRegion.split(",");
		for (String region : regions) {

			String[] strChainFields = region.split(":");
			Item scopRegion = createItem("ScopRegion");

			if (1 < strChainFields.length) {
				Matcher matcher = regionPattern.matcher(strChainFields[1]);
				if (matcher.matches()) {
					scopRegion.setAttribute("start", matcher.group(1));
					scopRegion.setAttribute("end", matcher.group(3));
					String sic = matcher.group(2);
					if (!sic.equals("")) {
						scopRegion.setAttribute("startInsertionCode", sic);
					}
					String eic = matcher.group(4);
					if (!eic.equals("")) {
						scopRegion.setAttribute("endInsertionCode", eic);
					}
				} else {
					throw new RuntimeException(String.format(
							"Unexpected region format: %s, at %s %s", region, pdbid, chainRegion));
				}
			}

			scopRegion.setReference("scopClassification", scopProteinRefId);
			scopRegion.setReference("chain", getProteinChain(pdbid, strChainFields[0]));

			store(scopRegion);
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
