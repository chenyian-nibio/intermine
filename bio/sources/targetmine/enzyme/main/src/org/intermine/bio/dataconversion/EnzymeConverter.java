package org.intermine.bio.dataconversion;

/*
 * Copyright (C) 2002-2009 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.intermine.dataconversion.FileConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.StringUtil;
import org.intermine.xml.full.Item;

/**
 * This converter parse enzyme info from 'ENZYME nomenclature database' <br/>
 * The file could be get at ftp of expasy (http://www.expasy.org/enzyme/)
 * 
 * <pre>
 * wget ftp://ftp.expasy.org/databases/enzyme/enzyme.dat
 * </pre>
 * 
 * one option should be contained in project.xml setting for organisms.<br/>
 * the values are the codes used in uniprot protein name (primaryIdentifier), <br/>
 * such as HUMAN in 1433B_HUMAN
 * 
 * <pre>
 * &lt;property name=&quot;enzyme.organisms&quot; value=&quot;HUMAN RAT MOUSE DROME&quot;/&gt;
 * </pre>
 * 
 * @author chenyian
 */
public class EnzymeConverter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(EnzymeConverter.class);

	//
	private static final String DATASET_TITLE = "ENZYME";
	private static final String DATA_SOURCE_NAME = "ENZYME nomenclature database";

	private Set<String> organismNames;

	public void setEnzymeOrganisms(String organismNames) {
		this.organismNames = new HashSet<String>(Arrays
				.asList(StringUtil.split(organismNames, " ")));
		LOG.info("Setting list of organisms to " + this.organismNames);
	}

	private Map<String, String> proteinMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public EnzymeConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		BufferedReader br = new BufferedReader(reader);
		String l;
		boolean isEntry = false;
		EnzymeEntry ee = new EnzymeEntry();
		while ((l = br.readLine()) != null) {
			if (l.trim().equals("//")) {
				// skip header part
				if (!isEntry) {
					isEntry = true;
					continue;
				}
				createNewEnzyme(ee);
				ee = new EnzymeEntry();
			}

			if (l.startsWith("DR")) {
				Set<String> accs = parsePrimaryAcc(l.substring(5));
				if (!accs.isEmpty()) {
					ee.proteins.addAll(accs);
				}
			} else if (l.startsWith("ID")) {
				ee.ecNumber = l.substring(5);
			} else if (l.startsWith("DE")) {
				ee.description += l.substring(5);
			} else if (l.startsWith("CF")) {
				ee.cofactor += l.substring(5);
			} else if (l.startsWith("CA")) {
				ee.catalyticActivity += l.substring(5);
			} else if (l.startsWith("AN")) {
				ee.synonyms.add(l.substring(5));
			}
		}
		br.close();
	}

	private Set<String> parsePrimaryAcc(String proteinString) {
		Pattern pattern = Pattern.compile("(\\w+)_(\\w+)");
		Matcher matcher = pattern.matcher(proteinString);
		Set<String> ret = new HashSet<String>();
		while (matcher.find()) {
			if (organismNames.contains(matcher.group(2))) {
				ret.add(matcher.group(0));
			}
		}
		return ret;
	}

	private void createNewEnzyme(EnzymeEntry enzymeEntry) throws ObjectStoreException {
		Item enzyme = createItem("Enzyme");
		if (enzymeEntry.ecNumber.equals("")) {
			LOG.error("failed to parse the enzyme entry." + enzymeEntry.toString());
			return;
		}
		// for BioEntity
		enzyme.setAttribute("primaryIdentifier", enzymeEntry.ecNumber);
		enzyme.setAttribute("ecNumber", enzymeEntry.ecNumber);
		if (!enzymeEntry.description.equals("")) {
			enzyme.setAttribute("description", enzymeEntry.description);
		}
		if (!enzymeEntry.catalyticActivity.equals("")) {
			enzyme.setAttribute("catalyticActivity", enzymeEntry.catalyticActivity);
		}
		if (!enzymeEntry.cofactor.equals("")) {
			enzyme.setAttribute("cofactor", enzymeEntry.cofactor);
		}

		for (String proteinName : enzymeEntry.proteins) {
			String refId = getProtein(proteinName);
			if (refId != null) {
				enzyme.addToCollection("proteins", refId);
			}
		}

		for (String aliasName : enzymeEntry.synonyms) {
			Item item = createItem("Synonym");
			item.setReference("subject", enzyme.getIdentifier());
			// type is deprecated from v0.94
//			item.setAttribute("type", "alias");
			item.setAttribute("value", aliasName);
			store(item);
			enzyme.addToCollection("synonyms", item);
		}

		// add EC number to synonyms, for quick searching
		Item item = createItem("Synonym");
		item.setReference("subject", enzyme.getIdentifier());
		// type is deprecated from v0.94
//		item.setAttribute("type", "identifier");
		item.setAttribute("value", enzymeEntry.ecNumber);
		store(item);
		enzyme.addToCollection("synonyms", item);

		store(enzyme);
	}

	private String getProtein(String proteinName) throws ObjectStoreException {
		String ret = proteinMap.get(proteinName);
		if (ret == null) {
			Item protein = createItem("Protein");
			protein.setAttribute("primaryIdentifier", proteinName);
			ret = protein.getIdentifier();
			proteinMap.put(proteinName, ret);
			store(protein);
		}
		return ret;
	}

	private class EnzymeEntry {
		public String ecNumber = "";
		public String description = "";
		public String catalyticActivity = "";
		public String cofactor = "";

		public List<String> synonyms = new ArrayList<String>();
		public Set<String> proteins = new HashSet<String>();
	}

}
