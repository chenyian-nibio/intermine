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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.intermine.dataconversion.FileConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
@Deprecated
public class TfactorConverter extends FileConverter {
	private static final String INTERACTION_TYPE = "Transcriptional regulation";

	private static final Logger LOG = Logger.getLogger(TfactorConverter.class);
	//
	private static final String ID_PERFIX = "ORA";

	private Map<String, Item> geneIdMap = new HashMap<String, Item>();
	// For further application 
//	private Map<String, Item> pubMedIdMap = new HashMap<String, Item>();
	private Item dataset;

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public TfactorConverter(ItemWriter writer, Model model) {
		super(writer, model);
		dataset = creatDataSet();
	}

	private Item creatDataSet() {
		Item dataSource = createItem("DataSource");
		dataSource.setAttribute("name", "ORegAnno");

		Item dataSet = createItem("DataSet");
		dataSet.setAttribute("name", "ORegAnno");
		dataSet.setReference("dataSource", dataSource);
//		dataSet.setAttribute("url", "http://www.oreganno.org/oregano/");
//		dataSet.setAttribute("description", "Open regulatory annotation database");

		try {
			store(dataSource);
			store(dataSet);
		} catch (ObjectStoreException e) {
			LOG.error("failed to store DataSource/DataSet of ORegAnno");
		}

		return dataSet;
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(reader));

		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			// source target sequence start end chromosome strand genomeBuild stableId 
			String sourceId = cols[0].trim();
			String targetId = cols[1].trim();
			if (sourceId == null || sourceId.equals("")) {
				continue;
			}
			Item sourceGene = getGeneByNcbiGeneId(sourceId);
			Item bindingStie = getBindingSite(cols);

			if (targetId.equals(sourceId)) {
				// create Interaction
				createInteraction(sourceGene, sourceGene, "source&target", bindingStie);

			} else {
				// create Interaction for source
				Item targetGene = getGeneByNcbiGeneId(targetId);
				createInteraction(sourceGene, targetGene, "source", bindingStie);

				// create Interaction for target
				createInteraction(targetGene, sourceGene, "target", bindingStie);
			}
			
			store(bindingStie);
		}
		reader.close();
		store(geneIdMap.values());
	}

	private Item getBindingSite(String[] cols) throws ObjectStoreException {
		Item bs = createItem("BindingSiteInfo");
		bs.setAttribute("sequence", cols[2]);
		if (!cols[3].equals("N/A")) {
			bs.setAttribute("start", cols[3]);
		}
		if (!cols[4].equals("N/A")) {
			bs.setAttribute("end", cols[4]);
		}
		if (!cols[5].equals("N/A")) {
			bs.setAttribute("chromosome", cols[5]);
		}
		if (!cols[6].equals("N/A")) {
			bs.setAttribute("strand", cols[6]);
		}
		if (!cols[7].equals("N/A")) {
			bs.setAttribute("genomeBuild", cols[7]);
		}
		bs.setAttribute("stableId", cols[8]);
		return bs;
	}

	private Item getGeneByNcbiGeneId(String ncbiGeneId) throws ObjectStoreException {
		if (geneIdMap.containsKey(ncbiGeneId)) {
			return geneIdMap.get(ncbiGeneId);
		} else {
			Item gene = createItem("Gene");
			gene.setAttribute("ncbiGeneId", ncbiGeneId);
			geneIdMap.put(ncbiGeneId, gene);
			return gene;
		}
	}

	private Item createInteraction(Item master, Item slave, String role, Item bindingSite)
			throws ObjectStoreException {
		Item ret = createItem("ProteinDNAInteraction");
		ret.setReference("gene", master);
		ret.setReference("interactWith", slave);

		ret.setAttribute("interactionType", INTERACTION_TYPE);
		ret.addToCollection("dataSets", dataset);
		ret.setAttribute("name", String.format("%s_G%s_G%s", TfactorConverter.ID_PERFIX, master
				.getAttribute("ncbiGeneId").getValue(), slave.getAttribute("ncbiGeneId")
				.getValue()));
		ret.setAttribute("role", role);
		
		ret.setReference("bindingSite", bindingSite);
		
		store(ret);
		master.addToCollection("proteinDNAInteractions", ret);
		return ret;
	}

	// For further application 
//	private Item getPublication(String pubMedId) throws ObjectStoreException {
//		if (pubMedIdMap.containsKey(pubMedId)) {
//			return pubMedIdMap.get(pubMedId);
//		} else {
//			Item pub = createItem("Publication");
//			pub.setAttribute("pubMedId", pubMedId);
//			store(pub);
//			LOG.info(String.format("Publication id:%s was created.", pubMedId));
//			pubMedIdMap.put(pubMedId, pub);
//			return pub;
//		}
//	}
}
