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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.FileConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * Parse Organism information. Data sources were download from NCBI taxonomy using following script
 * 
 * <pre>
 * wget ftp://ftp.ncbi.nih.gov/pub/taxonomy/taxdump.tar.gz 
 * tar -zxv -f taxdump.tar.gz names.dmp 
 * grep &quot;scientific name&quot; names.dmp &gt; taxno.name
 * </pre>
 * 
 * @author chenyian
 */
public class OrganismConverter extends FileConverter {
	private static final Logger LOG = Logger.getLogger(OrganismConverter.class);

	//
	private String osAlias;

	private String processClass;

	private List<String> hasShortName;

	private List<String> allTaxonIds;

	private Map<String, String> organismMap = new HashMap<String, String>();

	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

	public void setProcessClass(String processClass) {
		this.processClass = processClass;
	}

	public void setHasShortName(String taxonIds) {
		this.hasShortName = Arrays.asList(taxonIds.split(" "));
		LOG.info("Only the following organisms contain 'shortName': "
				+ StringUtils.join(hasShortName, ","));
	}

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public OrganismConverter(ItemWriter writer, Model model) {
		super(writer, model);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		if (processClass == null || osAlias == null) {
			throw new RuntimeException("check your setting for processClass and osAlias.");
		}

		allTaxonIds = getAllTaxonIds();

		int orgNum = allTaxonIds.size();
		LOG.info(String.format("%d %s object(s) to be processed.", orgNum, processClass));

		Iterator<String[]> iterator = FormattedTextParser.parseDelimitedReader(new BufferedReader(
				reader), '|');

		while (orgNum > organismMap.size() && iterator.hasNext()) {
			String[] cols = iterator.next();

			String taxonId = cols[0].trim();
			String sciName = cols[1].trim();
			if (allTaxonIds.contains(taxonId)) {
				if (organismMap.get(taxonId) == null) {
					String refId = createOrganism(taxonId, sciName);
					organismMap.put(taxonId, refId);
				}
			}
		}
	}

	private String createOrganism(String taxonId, String sciName) throws ObjectStoreException {
		Item item = createItem(processClass);
		item.setAttribute("taxonId", taxonId);
		item.setAttribute("name", sciName);
		int spaceIndex = sciName.indexOf(" ");
		if (spaceIndex == -1) {
			item.setAttribute("genus", sciName);
		} else {
			item.setAttribute("genus", sciName.substring(0, spaceIndex));
			item.setAttribute("species", sciName.substring(spaceIndex + 1));
			if (hasShortName.contains(taxonId)) {
				item.setAttribute("shortName", sciName.charAt(0) + ". "
						+ sciName.substring(spaceIndex + 1));
			}
		}
		store(item);
//		LOG.info(String.format("taxonId: %s; shortName: %s; created.", taxonId, sciName.charAt(0)
//				+ ". " + sciName.substring(spaceIndex + 1)));
		return item.getIdentifier();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<String> getAllTaxonIds() throws Exception {
		Query q = new Query();
		QueryClass c = new QueryClass(Class.forName("org.intermine.model.bio." + processClass));
		QueryField f1 = new QueryField(c, "taxonId");
		q.addFrom(c);
		q.addToSelect(f1);
		q.setDistinct(true);

		ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);

		List<String> ret = new ArrayList<String>();
		Iterator iterator = os.execute(q).iterator();
		while (iterator.hasNext()) {
			ResultsRow<Integer> rr = (ResultsRow<Integer>) iterator.next();
			ret.add(rr.get(0).toString());
		}
		return ret;
	}

}
