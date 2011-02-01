package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.intermine.dataconversion.FileConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author ishikawa
 * @author chenyian
 */
public class ScopConverter extends FileConverter {

	private static Logger m_oLogger = Logger.getLogger(ScopConverter.class);

	// <sunid, Item of ScopEntry's subclasses or ProteinChainRegion>
	private Map<Integer, Item> m_oScopEntryMap = new TreeMap<Integer, Item>();

	// <PDB ID, Item of ProteinStructure>
	private Map<String, Item> m_oPdbMap = new TreeMap<String, Item>();

	// <PDBID_chain, ProteinChain Item>
	private Map<String, Item> m_oProteinChainMap = new TreeMap<String, Item>();

	private Map<String, String> sunIdScopClsMap = new HashMap<String, String>();

	private Pattern regionPattern = Pattern.compile("([-]?\\d+)([A-Z]?)-([-]?\\d+)([A-Z]?)");

	// dir.hie.scop.txt
	private File m_oHieFile;

	// dir.cla.scop.txt
	private File claFile;

	// pdb_chain_scop_uniprot.lst (from MSD)
	// private File m_oScopUniprotFile;
	public ScopConverter(ItemWriter writer, Model model) {
		super(writer, model);
	}

	@Override
	public void process(Reader reader) throws Exception {

		importDesFileToItems(reader);

		readClsFile();

		importHieFile();

		// register items
		store(m_oPdbMap.values());
		store(m_oScopEntryMap.values());
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

			Integer iSunId = Integer.parseInt(a_strFields[0]);
			String strType = a_strFields[1];
			String strSccs = a_strFields[2];
			String strDescription = a_strFields[4];

			Item oItem = null;

			if ("cl".equals(strType)) {
				// ScopClass
				oItem = createItem("ScopClass");

			} else if ("cf".equals(strType)) {
				// ScopFold
				oItem = createItem("ScopFold");

			} else if ("sf".equals(strType)) {
				// ScopSuperfamily
				oItem = createItem("ScopSuperFamily");

			} else if ("fa".equals(strType)) {
				// ScopFamily
				oItem = createItem("ScopFamily");

			} else if ("dm".equals(strType)) {
				// ScopProtein
				oItem = createItem("ScopDomain");

			} else if ("sp".equals(strType)) {
				// ScopSpecies
				oItem = createItem("ScopSpecies");

			} else if ("px".equals(strType)) {
				// ScopDomain
				oItem = createItem("ScopProtein");

			}

			oItem.setAttribute("sunid", a_strFields[0]);
			oItem.setAttribute("sccs", strSccs);
			oItem.setAttribute("description", strDescription);

			m_oScopEntryMap.put(iSunId, oItem);
		}
	}

	/**
	 * Read dir.hie.scop.txt file. Set item reference using hierarchical information
	 * 
	 * @throws IOException
	 * @throws ObjectStoreException
	 */
	private void importHieFile() throws IOException, ObjectStoreException {

		if (null == m_oHieFile) {
			throw new NullPointerException("hieFile property not set");
		}

		BufferedReader oBr = new BufferedReader(new FileReader(m_oHieFile));

		int iLineCount = 0;

		while (oBr.ready()) {
			iLineCount++;
			String strLine = oBr.readLine();

			if (null == strLine || "".equals(strLine) || strLine.startsWith("#")) {
				continue;
			}

			m_oLogger.debug("hieline:" + strLine);

			String[] a_strFields = strLine.split("\t");

			if (a_strFields.length != 3) {
				throw new IOException("dir.hie.scop.txt is invalid. line:" + iLineCount + " "
						+ strLine);
			}

			Item oChildItem = m_oScopEntryMap.get(Integer.parseInt(a_strFields[0]));

			Item oParentItem = null;

			try {
				oParentItem = m_oScopEntryMap.get(Integer.parseInt(a_strFields[1]));
			} catch (NumberFormatException e) {
				continue;
			}

			String strClassName = oChildItem.getClassName();

			m_oLogger.debug("ScopClassName:" + strClassName);

			if (strClassName.endsWith("ScopFold")) {
				// ScopFold
				oChildItem.setReference("scopClass", oParentItem);

			} else if (strClassName.endsWith("SuperFamily")) {
				// ScopSuperFamily
				oChildItem.setReference("scopFold", oParentItem);

			} else if (strClassName.endsWith("ScopFamily")) {
				// ScopFamily
				oChildItem.setReference("scopSuperFamily", oParentItem);

			} else if (strClassName.endsWith("ScopDomain")) {
				// ScopProtein
				oChildItem.setReference("scopFamily", oParentItem);

			} else if (strClassName.endsWith("ScopSpecies")) {
				// ScopSpecies
				oChildItem.setReference("scopDomain", oParentItem);

			} else if (strClassName.endsWith("ScopProtein")) {
				// ScopDomain
				oChildItem.setReference("scopSpecies", oParentItem);

				String strDescription = oChildItem.getAttribute("description").getValue();

				m_oLogger.debug("protein description:" + strDescription);

				// 0=pdb id, 1=chain:residues
				String[] strFields = strDescription.split(" ");

				Item oProteinStructure = getProteinStructure(strFields[0]);

				String[] strChains = strFields[1].split(",");

				List<String> scopRegionRefList = new ArrayList<String>();
				for (String strChain : strChains) {

					String[] strChainFields = strChain.split(":");

					// chenyian: change to ScopRegion
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
							throw new RuntimeException("Unexpected region format: "
									+ strDescription);
						}
					}

					Item oProteinChain = getProteinChain(strFields[0], strChainFields[0]);

					scopRegion.setReference("scopProtein", oChildItem);
					scopRegion.setReference("chain", oProteinChain);
					oProteinChain.setReference("structure", oProteinStructure);

					String sunid = oChildItem.getAttribute("sunid").getValue();
					scopRegion.setReference("scopClassification", sunIdScopClsMap.get(sunid));

					store(scopRegion);
					scopRegionRefList.add(scopRegion.getIdentifier());
				}
			}
		}

		store(m_oProteinChainMap.values());
	}

	/**
	 * Read clsFile to create StructureClassification class
	 * 
	 * @throws IOException
	 * 
	 * @throws ObjectStoreException
	 * 
	 *             added by chenyian
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
			Item sc = createItem("ScopClassification");
			sc.setAttribute("sid", cols[0]);
			sc.setAttribute("sccs", cols[3]);

			Matcher matcher = pattern.matcher(cols[5]);
			if (matcher.matches()) {
				sc
						.setReference("scopClass", m_oScopEntryMap.get(Integer.valueOf(matcher
								.group(1))));
				sc.setReference("scopFold", m_oScopEntryMap.get(Integer.valueOf(matcher.group(2))));
				sc.setReference("scopSuperFamily", m_oScopEntryMap.get(Integer.valueOf(matcher
						.group(3))));
				sc.setReference("scopFamily", m_oScopEntryMap
						.get(Integer.valueOf(matcher.group(4))));
				sc.setReference("scopDomain", m_oScopEntryMap
						.get(Integer.valueOf(matcher.group(5))));
				sc.setReference("scopSpecies", m_oScopEntryMap.get(Integer
						.valueOf(matcher.group(6))));
				sc.setReference("scopProtein", m_oScopEntryMap.get(Integer
						.valueOf(matcher.group(7))));
			} else {
				throw new RuntimeException("Unexpected string format: " + cols[5]);
			}

			store(sc);
			sunIdScopClsMap.put(cols[4], sc.getIdentifier());
		}

	}

	/**
	 * Get ProteinStructure Item of pdb id argument
	 * 
	 * @param strPdbId
	 * @return
	 */
	private Item getProteinStructure(String strPdbId) {

		if (!m_oPdbMap.containsKey(strPdbId)) {
			Item oStructure = createItem("ProteinStructure");
			oStructure.setAttribute("pdbId", strPdbId);
			m_oPdbMap.put(strPdbId, oStructure);
		}

		return m_oPdbMap.get(strPdbId);
	}

	private Item getProteinChain(String strPdbId, String strChain) throws ObjectStoreException {

		String strPdbIdChain = strPdbId + strChain;

		if (!m_oProteinChainMap.containsKey(strPdbIdChain)) {

			Item oProteinChain = createItem("ProteinChain");
			oProteinChain.setAttribute("pdbId", strPdbId);
			oProteinChain.setAttribute("chain", strChain);
			m_oProteinChainMap.put(strPdbIdChain, oProteinChain);

		}

		return m_oProteinChainMap.get(strPdbIdChain);
	}

	public void setHieFile(File oHieFile) {
		this.m_oHieFile = oHieFile;
	}

	public void setClsFile(File claFile) {
		this.claFile = claFile;
	}

}
