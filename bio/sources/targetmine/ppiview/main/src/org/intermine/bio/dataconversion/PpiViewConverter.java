package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;

/**
 * 
 * @author ishikawa
 * @author chenyian (refactor)
 * 
 */
public class PpiViewConverter extends BioFileConverter {

	public static final String DATASET_TITLE = "PPI view";
	private static final String DATA_SOURCE_NAME = "H-InvDB";

//	private static Logger LOG = Logger.getLogger(PpiViewConverter.class);

	// <PrimaryAcc, ProteinRefId>
	private Map<String, String> proteinMap = new HashMap<String, String>();

	// hip2uniprot.out file
	private File hip2UniProtFile;

	public void setHip2UniProtFile(File hip2UniProtFile) {
		this.hip2UniProtFile = hip2UniProtFile;
	}

	// <HIP_ID, Set<Accession>>
	private Map<String, Set<String>> hipAccessionMap = new HashMap<String, Set<String>>();

	// Key is "dbname_id". e.g. MINT_MINT-2837333
	// Value is Item ProteinInteractionSource
	private TreeMap<String, Item> piSourceMap = new TreeMap<String, Item>();

	public PpiViewConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	@Override
	/**
	 * Each line of a input file should be in the following format(tab-delimited)
	 * <protein_name>	<tool_name>	<start_pos>	<end_pos>	<comment>	<score>
	 * And file MUST be sorted by <protein_name>
	 */
	public void process(Reader reader) throws Exception {

		if (null == hip2UniProtFile) {
			throw new NullPointerException("hip2UniProtFile property not set");
		}

		try {
			readHip2UniProt(new FileReader(hip2UniProtFile));
		} catch (IOException e) {
			throw new RuntimeException("error reading hip2UniProtFile", e);
		}

		BufferedReader in = null;
		in = new BufferedReader(reader);
		// Skip a header line.
		in.readLine();

		String line;
		while ((line = in.readLine()) != null) {

			if (StringUtils.isEmpty(line)) {
				continue;
			}

			String[] fields = line.split("\t", -1);
			if (fields.length < 9) {
				continue;
			}
			String intId = fields[0];

			List<String> proteinRefIdListA = getProteinRefIdList(hipAccessionMap.get(fields[1]));
			List<String> proteinRefIdListB = getProteinRefIdList(hipAccessionMap.get(fields[2]));

			// Nothing to do
			if (proteinRefIdListA.size() == 0 || proteinRefIdListB.size() == 0) {
				continue;
			}

			List<Item> piSourceList = new ArrayList<Item>();
			piSourceList.addAll(getProteinInteractionSource("BIND", fields[3]));
			piSourceList.addAll(getProteinInteractionSource("DIP", fields[4]));
			piSourceList.addAll(getProteinInteractionSource("MINT", fields[5]));
			piSourceList.addAll(getProteinInteractionSource("HPRD", fields[6]));
			piSourceList.addAll(getProteinInteractionSource("IntAct", fields[7]));
			piSourceList.addAll(getProteinInteractionSource("GNP_Y2H", fields[8]));

			// arbitrary choose one as represented protein
			// not worth to spend time here
			String repRefIdA = proteinRefIdListA.get(0);
			String repRefIdB = proteinRefIdListB.get(0);

			for (String proteinRefId : proteinRefIdListA) {
				registerInteraction(intId, proteinRefId, repRefIdB, proteinRefIdListB, piSourceList);
			}

			for (String proteinRefId : proteinRefIdListB) {
				registerInteraction(intId, proteinRefId, repRefIdA, proteinRefIdListA, piSourceList);
			}
		}
	}

	/**
	 * Register ProteinInteraction
	 * 
	 * @param intId
	 * @param proteinRefId
	 * @param repProteinRefId
	 * @param proteinRefIdList
	 * @param piSourceList
	 * @throws ObjectStoreException
	 */
	private void registerInteraction(String intId, String proteinRefId, String repProteinRefId,
			List<String> proteinRefIdList, List<Item> piSourceList) throws ObjectStoreException {

		Item item = createItem("ProteinInteraction");

		item.setAttribute("intId", intId);
		item.setReference("protein", proteinRefId);
		item.setReference("representativePartner", repProteinRefId);

		for (String partner : proteinRefIdList) {
			item.addToCollection("allPartners", partner);
		}

		for (Item piSource : piSourceList) {
			item.addToCollection("piSources", piSource);
		}

		store(item);
	}

	/**
	 * Get ProteinInteractionSource item
	 * 
	 * @param dbName
	 * @param stringIds
	 * @return List of ProteinInteractionSources' identifier
	 */
	private List<Item> getProteinInteractionSource(String dbName, String stringIds)
			throws ObjectStoreException {

		if (null == dbName || "".equals(dbName) || null == stringIds || "".equals(stringIds)) {
			return new ArrayList<Item>();
		}

		List<Item> piSourceList = new ArrayList<Item>();

		for (String identifier : stringIds.split(",")) {

			String key = dbName + "_" + identifier;
			if (!piSourceMap.containsKey(key)) {

				Item item = createItem("ProteinInteractionSource");
				item.setAttribute("dbName", dbName);
				item.setAttribute("identifier", identifier);
				store(item);
				piSourceMap.put(key, item);

			}
			piSourceList.add(piSourceMap.get(key));
		}

		return piSourceList;
	}

	private void readHip2UniProt(Reader reader) throws IOException {

		BufferedReader in = null;
		in = new BufferedReader(reader);
		String line;
		while ((line = in.readLine()) != null) {
			String[] fields = line.split("\t", -1);

			if (StringUtils.isEmpty(fields[1])) {
				continue;
			}

			String[] accessions = fields[1].split(",");

			hipAccessionMap.put(fields[0], new HashSet<String>(Arrays.asList(accessions)));
		}
		in.close();
	}

	/**
	 * Get protein item
	 * 
	 * @param primaryAccession
	 *            primary accession number for protein
	 * @return protein item
	 * @throws ObjectStoreException
	 */
	private String getProtein(String primaryAccession) throws ObjectStoreException {

		String ret = proteinMap.get(primaryAccession);

		if (ret == null) {
			Item protein = createItem("Protein");
			protein.setAttribute("primaryAccession", primaryAccession);
			protein.setReference("organism", getOrganism("9606"));
			store(protein);
			ret = protein.getIdentifier();

			proteinMap.put(primaryAccession, ret);
		}
		return ret;
	}

	/**
	 * Get reference ID list of protein items
	 * 
	 * @return
	 * @throws ObjectStoreException
	 */
	private List<String> getProteinRefIdList(Set<String> proteinAccessions)
			throws ObjectStoreException {

		List<String> ret = new ArrayList<String>();

		if (null == proteinAccessions) {
			return ret;
		}

		for (String accession : proteinAccessions) {
			ret.add(getProtein(accession));
		}

		return ret;

	}

}
