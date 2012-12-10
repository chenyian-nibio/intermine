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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.intermine.dataconversion.FileConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.util.StringUtil;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class GenePubmedConverter extends FileConverter {
	private static final Logger LOG = Logger.getLogger(GenePubmedConverter.class);

	//
	private Set<String> taxonIds;
	private Map<String, String> publicationiMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public GenePubmedConverter(ItemWriter writer, Model model) {
		super(writer, model);
	}

	public void setPubmedOrganisms(String taxonIds) {
		this.taxonIds = new HashSet<String>(Arrays.asList(StringUtil.split(taxonIds, " ")));
		LOG.info("Setting list of organisms to " + this.taxonIds);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {

		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(reader));

		// parse geneId pubmedId to map
		Map<String, Set<String>> genePubmedMap = new HashMap<String, Set<String>>();
		while (iterator.hasNext()) {
			String[] values = iterator.next();
			String taxId = values[0].trim();
			String geneId = values[1].trim();
			String pubmedId = values[2].trim();
			if (!taxonIds.contains(taxId)) {
				continue;
			}
			if (genePubmedMap.get(geneId) == null) {
				genePubmedMap.put(geneId, new HashSet<String>());
			}
			String pubRefId = getPublication(pubmedId);
			genePubmedMap.get(geneId).add(pubRefId);
		}
		// create Genes and Publications
		for (String geneId : genePubmedMap.keySet()) {
			Item gene = createItem("Gene");
			gene.setAttribute("ncbiGeneId", geneId);
			gene.setCollection("publications", new ArrayList<String>(genePubmedMap.get(geneId)));
			store(gene);
		}

	}

	private String getPublication(String pubmedId) throws ObjectStoreException {
		String ret = publicationiMap.get(pubmedId);
		if (ret == null) {
			Item publication = createItem("Publication");
			publication.setAttribute("pubMedId", pubmedId);
			store(publication);
			ret = publication.getIdentifier();
			publicationiMap.put(pubmedId, ret);
		}
		return ret;
	}
}
