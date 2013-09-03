package org.intermine.bio.dataconversion;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;

/**
 * Parse CATH names from the CathNames file
 * 
 * @author chenyian
 */
public class CathNameConverter extends BioFileConverter {
	private static final String SEGMENT_DELIMITER = ";";
	private static final String REGION_DELIMITER = "==";

	private static final Logger LOG = Logger.getLogger(CathNameConverter.class);

	private List<String> levelList = Arrays.asList("Class", "Architecture", "Topology",
			"Homologous Superfamily");

	//
	private static final String DATASET_TITLE = "CATH";
	private static final String DATA_SOURCE_NAME = "CATH";

	private Map<String, Item> cathMap = new HashMap<String, Item>();
	private Map<String, String> proteinChainMap = new HashMap<String, String>();

	private Map<String, String> domainRegioinMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public CathNameConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		BufferedReader br = new BufferedReader(reader);
		String line;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("#")) {
				continue;
			}
			Pattern pattern = Pattern.compile("^([\\d|\\.]+)\\s+(\\w+)\\s+:(.*)$");
			Matcher matcher = pattern.matcher(line);
			while (matcher.find()) {
				String nodeNumber = matcher.group(1);
				String cathDomainName = matcher.group(2);
				String description = matcher.group(3);
				createCathClassification(nodeNumber, cathDomainName, description);
			}

		}
		store(cathMap.values());

		parseDomainRegion();
		parseCathDomainList();
	}

	private void createCathClassification(String nodeNumber, String cathDomainName,
			String description) {
		Item item = cathMap.get(nodeNumber);
		if (item == null) {
			item = createItem("CathClassification");
			item.setAttribute("type", "CATH");
			item.setAttribute("level", levelList.get(nodeNumber.split("\\.").length - 1));
			item.setAttribute("code", nodeNumber);
			item.setAttribute("cathCode", nodeNumber);
		}
		if (description != null && !description.equals("")) {
			item.setAttribute("description", description);
		} else {
			// item.setAttribute("description", String.format(
			// "Not available. Representative protein domain: %s", cathDomainName));
			item.setAttribute("description", String.format("CATH Superfamily %s", nodeNumber));
		}
		// logical error here!!
		// item.addToCollection("parents", item);
		String code = nodeNumber;
		while (code.lastIndexOf(".") != -1) {
			code = code.substring(0, code.lastIndexOf("."));
			item.addToCollection("parents", getCathParents(code));
		}

		cathMap.put(nodeNumber, item);
	}

	private Item getCathParents(String code) {
		Item ret = cathMap.get(code);
		if (ret == null) {
			ret = createItem("CathClassification");
			ret.setAttribute("cathCode", code);
			cathMap.put(code, ret);
			LOG.info("parent appears after child: " + code);
		}
		return ret;
	}

	private void parseCathDomainList() throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(domainList));

		String line;
		while ((line = reader.readLine()) != null) {
			if (!line.startsWith("#")) {
				String[] cols = line.split("\\s+");
				String cathDomainName = cols[0];

				String chainId = cathDomainName.substring(4, 5);
				if (chainId.equals("0")) {
					continue;
				}

				String cathCode = String.format("%s.%s.%s.%s", cols[1], cols[2], cols[3], cols[4]);
				String cathId = String.format("%s.%s.%s.%s.%s.%s.%s.%s", cols[1], cols[2], cols[3],
						cols[4], cols[5], cols[6], cols[7], cols[8]);
				String domainLength = cols[10];

				Item item = createItem("CathClassification");
				item.setAttribute("type", "CATH");
				item.setAttribute("level", "Domain");
				item.setAttribute("code", cathId);
				String code = cathCode;
				item.addToCollection("parents", getCathParents(code));
				while (code.lastIndexOf(".") != -1) {
					code = code.substring(0, code.lastIndexOf("."));
					item.addToCollection("parents", getCathParents(code));
				}
				item.setAttribute("description", String.format("CATH domain: %s", cathDomainName));

				item.setAttribute("domainLength", domainLength);

				store(item);

				String pdbId = cathDomainName.substring(0, 4);
				String chainRegion = domainRegioinMap.get(cathDomainName);
				if (chainRegion == null) {
					throw new RuntimeException(cathDomainName);
				}
				createStructuralRegion(pdbId, chainId, chainRegion, item.getIdentifier());
			}
		}
	}

	private File domainList;
	private File domall;

	public void setDomainList(File domainList) {
		this.domainList = domainList;
	}

	public void setDomall(File domall) {
		this.domall = domall;
	}

	private void parseDomainRegion() throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(domall));
		// skip the header
		reader.readLine();
		String line;
		while ((line = reader.readLine()) != null) {
			// 1cnsA D02 F00 2 A 1 - A 87 - A 146 - A 243 - 1 A 88 - A 145 -
			// 1bcmA D02 F02 1 A 257 - A 487 - 1 A 492 - A 559 - A 488 - A 491 - (4) A 560 - A 560 -
			// (1)
			if (!line.startsWith("#")) {
				String[] cols = line.split("\\s+");

				Integer d = Integer.valueOf(cols[1].substring(1));
				// Integer f = Integer.valueOf(cols[2].substring(1));

				int idx = 3;
				for (int j = 0; j < d.intValue(); j++) {
					int s = Integer.valueOf(cols[idx]).intValue();
					String[] segments = new String[s];
					int flag = idx + 2;
					for (int k = 0; k < s; k++) {
						// Note: cols[4]: chain; cols[7]: chain; cols[4] eq cols[7]
						idx = 6 * k + flag;
						segments[k] = cols[idx] + REGION_DELIMITER + cols[idx + 3];
					}
					String domainId;
					if (d < 2) {
						domainId = cols[0] + "00";
						// Naming of domain id is not regular... there are some exceptions
						domainRegioinMap.put(cols[0] + "01",
								StringUtils.join(segments, SEGMENT_DELIMITER));
					} else {
						domainId = String.format("%s%02d", cols[0], j + 1);
					}
					// System.out.println(domainId + ": " + StringUtils.join(segments,"; "));
					domainRegioinMap.put(domainId, StringUtils.join(segments, SEGMENT_DELIMITER));
					idx = idx + 5;
				}

			}
		}

	}

	private void createStructuralRegion(String pdbId, String chainId, String chainRegion,
			String referenceId) throws ObjectStoreException {

		String[] regions = chainRegion.split(SEGMENT_DELIMITER);
		for (String region : regions) {
			// LOG.info(region);

			String[] cols = region.split(REGION_DELIMITER);
			Item item = createItem("StructuralRegion");
			item.setAttribute("start", cols[0]);
			item.setAttribute("end", cols[1]);

			item.setReference("structuralDomain", referenceId);
			item.setReference("proteinChain", getProteinChain(pdbId, chainId));

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

}
