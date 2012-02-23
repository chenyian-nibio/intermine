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
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
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

	}

	private void createCathClassification(String nodeNumber, String cathDomainName,
			String description) {
		Item item = cathMap.get(nodeNumber);
		if (item == null) {
			item = createItem("CathClassification");
			item.setAttribute("cathCode", nodeNumber);
		}
		item.setAttribute("cathDomainName", cathDomainName);
		if (description != null && !description.equals("")){
			item.setAttribute("description", description);
		} else {
			item.setAttribute("description", String.format("No name: %s", cathDomainName));
		}
		// logical error here!!
		item.addToCollection("parents", item);
		String code = nodeNumber;
		while (code.lastIndexOf(".") != -1){
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

	public static void main(String[] args) {
		String line = "1.20.1000    1f5nA01    :Signaling Protein - Interferon-induced Guanylate-binding Protein 1; Chain A, domain 1";
//		String line = "3.40.50.10210    1l5oA02    :";
		Pattern pattern = Pattern.compile("^([\\d|\\.]+)\\s+(\\w+)\\s+:(.*)$");
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {
			String nodeNumber = matcher.group(1);
			String cathDomainName = matcher.group(2);
			String description = matcher.group(3);
			System.out.println(nodeNumber + " / " + cathDomainName + " / " + description);
		}

	}
}
