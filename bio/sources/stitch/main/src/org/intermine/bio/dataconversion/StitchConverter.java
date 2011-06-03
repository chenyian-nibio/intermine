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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.model.bio.Protein;
import org.intermine.model.bio.Synonym;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.query.ConstraintOp;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryCollectionReference;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.QueryValue;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.objectstore.query.SimpleConstraint;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class StitchConverter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(StitchConverter.class);
	//
	private static final String DATASET_TITLE = "STITCH";
	private static final String DATA_SOURCE_NAME = "STITCH: Chemical-Protein Interactions";

	private Map<String, String> proteinMap = new HashMap<String, String>();
	private Map<String, String> compoundMap = new HashMap<String, String>();

	// get id map from integrated data
	private Map<String, Set<String>> primaryIdMap;
	private String osAlias = null;

	/**
	 * Set the ObjectStore alias.
	 * 
	 * @param osAlias
	 *            The ObjectStore alias
	 */
	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public StitchConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {

		// should only generate once
		if (primaryIdMap == null) {
			getPrimaryIdMap();
			// readUniprotIdMap();
		}
		
		readCompoundMap();

		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(reader));

		int create = 0;
		int skip = 0;

		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String cid = cols[0];
//			String ensemblId = cols[1].substring(cols[1].indexOf(".") + 1);
			String ensemblId = StringUtils.substringAfter(cols[1], ".");
			Set<String> ids = primaryIdMap.get(ensemblId);

			// get evidence
			String evidence;
			if (!cols[2].equals("0")) {
				evidence = "experimental";
			} else if (!cols[3].equals("0")) {
				evidence = "database";
			} else if (!cols[4].equals("0")) {
				evidence = "textmining";
			} else {
				throw new RuntimeException("Unexpected evidence record, " + "check the entry: "
						+ cid + "_" + cols[1]);
			}

			if (ids != null) {
				Set<String> chebiIds = chebiIdMap.get(cid);
				if (chebiIds != null) {
					for (String primaryIdentifier : ids) {
						for (String chebiId : chebiIds) {
							Item pci = createItem("ProteinCompoundInteraction");
							pci.setAttribute("name", cid + "_" + cols[1]);
							pci.setAttribute("score", cols[5]);
							pci.setAttribute("evidence", evidence);
							pci.setReference("compound", getCompound(chebiId));
							pci.setReference("protein", getProtein(primaryIdentifier));
							store(pci);
							create++;
						}
					}
				} else {
					skip++;
					LOG.info(String.format("compound: %s cannot map to a ChEBI id.", cid));
				}
			} else {
				skip++;
				LOG.info(String.format("Uniprot ID for '%s' was not found.", ensemblId));
			}
		}

		LOG.info(String.format("%d interactions were created, and %d were skipped.", create, skip));
	}

	private String getProtein(String primaryIdentifier) throws ObjectStoreException {
		String ret = proteinMap.get(primaryIdentifier);
		if (ret == null) {
			Item item = createItem("Protein");
			item.setAttribute("primaryIdentifier", primaryIdentifier);
			store(item);
			ret = item.getIdentifier();
			proteinMap.put(primaryIdentifier, ret);
		}
		return ret;
	}

	private String getCompound(String cid) throws ObjectStoreException {
		String ret = compoundMap.get(cid);
		if (ret == null) {
			Item item = createItem("Compound");
			item.setAttribute("chebiId", cid);
			store(item);
			ret = item.getIdentifier();
			compoundMap.put(cid, ret);
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	private void getPrimaryIdMap() throws Exception {
		primaryIdMap = new HashMap<String, Set<String>>();

		Query q = new Query();
		QueryClass qcSynonym = new QueryClass(Synonym.class);
		QueryClass qcProtein = new QueryClass(Protein.class);
		QueryField qfValue = new QueryField(qcSynonym, "value");
		QueryField qfPrimaryId = new QueryField(qcProtein, "primaryIdentifier");
		q.addFrom(qcSynonym);
		q.addFrom(qcProtein);
		q.addToSelect(qfValue);
		q.addToSelect(qfPrimaryId);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		QueryCollectionReference synRef = new QueryCollectionReference(qcProtein, "synonyms");
		cs.addConstraint(new ContainsConstraint(synRef, ConstraintOp.CONTAINS, qcSynonym));

		cs
				.addConstraint(new SimpleConstraint(qfValue, ConstraintOp.MATCHES, new QueryValue(
						"ENS%")));

		q.setConstraint(cs);

		ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);

		Results results = os.execute(q);
		Iterator<Object> iterator = results.iterator();
		while (iterator.hasNext()) {
			ResultsRow<String> rr = (ResultsRow<String>) iterator.next();
			// LOG.info(String.format("ens: %s; uni: %s...", rr.get(0), rr.get(1)));
			if (primaryIdMap.get(rr.get(0)) == null) {
				primaryIdMap.put(rr.get(0), new HashSet<String>());
			}
			primaryIdMap.get(rr.get(0)).add(rr.get(1));
		}
	}

	// read in chebi id mapping file
	private File compoundMapFile;
	private Map<String, Set<String>> chebiIdMap = new HashMap<String, Set<String>>();

	public void setCompoundMapFile(File file) {
		this.compoundMapFile = file;
	}

	private void readCompoundMap() {
		if (compoundMapFile == null) {
			throw new NullPointerException("compoundMapFile property not set");
		}

		try {
			Reader reader = new BufferedReader(new FileReader(compoundMapFile));
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);

			// skip header
			iterator.next();

			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				if (chebiIdMap.get(cols[0]) == null) {
					chebiIdMap.put(cols[0], new HashSet<String>());
				}
				chebiIdMap.get(cols[0]).add(StringUtils.substringAfter(cols[2], ":"));
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// For compare... temporary
	private File uniprotIdMapFile;

	// private Map<String, String> uniprotIdMap;
	private void readUniprotIdMap() {
		if (uniprotIdMapFile == null) {
			throw new NullPointerException("uniprotIdMapFile property not set");
		}
		primaryIdMap = new HashMap<String, Set<String>>();

		try {
			Reader reader = new BufferedReader(new FileReader(uniprotIdMapFile));
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);

			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				if (primaryIdMap.get(cols[1]) == null) {
					primaryIdMap.put(cols[1], new HashSet<String>());
				}
				primaryIdMap.get(cols[1]).add(cols[2]);
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setUniprotIdMapFile(File file) {
		this.uniprotIdMapFile = file;
	}
}
