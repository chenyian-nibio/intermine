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
import java.util.HashMap;
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
	private static final Logger LOG = Logger.getLogger(CathNameConverter.class);
	//
	private static final String DATASET_TITLE = "CATH";
	private static final String DATA_SOURCE_NAME = "CATH";

	private Map<String, Item> cathMap = new HashMap<String, Item>();
	private Map<String, String> structureMap = new HashMap<String, String>();

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

		parseCathDomainList();
	}

	private void createCathClassification(String nodeNumber, String cathDomainName,
			String description) {
		Item item = cathMap.get(nodeNumber);
		if (item == null) {
			item = createItem("CathClassification");
			item.setAttribute("cathCode", nodeNumber);
		}
		if (description != null && !description.equals("")) {
			item.setAttribute("description", description);
		} else {
			item.setAttribute("description", String.format(
					"Not available. Representative protein domain: %s", cathDomainName));
		}
		// logical error here!!
		item.addToCollection("parents", item);
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
		
		String line = reader.readLine();
		while (line != null) {
			if (!line.startsWith("#")) {
				String[] cols = line.split("\\s+");
				String cathDomainName = cols[0];
				String cathCode = String.format("%s.%s.%s.%s", cols[1], cols[2], cols[3], cols[4]);
				String domainLength = cols[10];
				Item item = createItem("CathDomain");
				item.setAttribute("cathDomainName", cathDomainName);
				item.setAttribute("domainLength", domainLength);
				item.setReference("cathSuperfamily", cathMap.get(cathCode));
				
				String pdbId = cathDomainName.substring(0, 4);
				if (!StringUtils.isEmpty(pdbId)){
					item.setReference("proteinStructure", getProteinStructure(pdbId));
				}
				
				store(item);
			}
			line = reader.readLine();
		}
	}
	
	private File domainList;

	public void setDomainList(File domainList) {
		this.domainList = domainList;
	}
	
	private String getProteinStructure(String identifier) throws ObjectStoreException {
		String ret = structureMap.get(identifier);
		if (ret == null) {
			Item item = createItem("ProteinStructure");
			item.setAttribute("pdbId", identifier);
			ret = item.getIdentifier();
			store(item);
			structureMap.put(identifier, ret);
		}
		return ret;
	}
	
}
