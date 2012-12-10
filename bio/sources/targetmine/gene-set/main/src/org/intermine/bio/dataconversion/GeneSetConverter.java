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
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.intermine.dataconversion.FileConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chneyian
 */
public class GeneSetConverter extends FileConverter {
	//
	// private static final String DATASET_TITLE = "Gene set clustering";
	// private static final String DATA_SOURCE_NAME = "TargetMine";
	
	private Item dataSource;

	private Map<String, String> clusterNameMap;
	
	private Map<String, Item> pathwayMap = new HashMap<String, Item>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public GeneSetConverter(ItemWriter writer, Model model) {
		super(writer, model);
		dataSource = createItem("DataSource");
		dataSource.setAttribute("name", "TargetMine");
		try {
			store(dataSource);
		} catch (ObjectStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		
		if (clusterNameMap == null) {
			readClusterNameMap();
		}
		
		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		String[] header = iterator.next();
		if (!header[0].equals("DataSet") || StringUtils.isEmpty(header[1])) {
			throw new RuntimeException("Data set name is not set properly. "
					+ "Check your data source. The first line should be 'DataSet\t[data set name]' " +
					"but your first line is: " + StringUtils.join(header, "\t"));
		}
		Item dataSet = createItem("DataSet");
		dataSet.setAttribute("name", header[1]);
		dataSet.setReference("dataSource", dataSource);
		store(dataSet);
		
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			if (StringUtils.isEmpty(cols[0])) {
				continue;
			}
			Item item = createItem("GeneSetCluster");
			item.setAttribute("identifier", cols[0]);
			item.setAttribute("name", clusterNameMap.get(cols[0]));
			String[] pathwayIds = cols[2].split(",");
			for (String pid : pathwayIds) {
				item.addToCollection("pathways", getPathway(pid));
			}
			item.setReference("dataSet", dataSet);
			store(item);
		}
	}

	private void readClusterNameMap() throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(clusterName));

		clusterNameMap = new HashMap<String, String>();
		
		String line = reader.readLine();
		while (line != null) {
			String[] cols = line.split("\\t");
			clusterNameMap.put(cols[0], cols[2]);
			line = reader.readLine();
		}
		
	}

	private Item getPathway(String pathwayId) throws ObjectStoreException {
		Item ret = pathwayMap.get(pathwayId);
		if (ret == null) {
			ret = createItem("Pathway");
			ret.setAttribute("identifier", pathwayId);
			store(ret);
			pathwayMap.put(pathwayId, ret);
		}
		return ret;
	}
	
	private File clusterName;

	public void setClusterName(File clusterName) {
		this.clusterName = clusterName;
	}
	

}
