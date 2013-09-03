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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.intermine.dataconversion.FileConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * Renewed for the new appication (2013/5/29)
 * Could be re-implemented in the post processing in the future. 
 * 
 * @author chneyian
 */
public class GeneSetConverter extends FileConverter {
	//
	// private static final String DATASET_TITLE = "Gene set clustering";
	// private static final String DATA_SOURCE_NAME = "TargetMine";
	
	private Map<String, String> clusterNameMap = new HashMap<String, String>();;
	
	private Map<String, String> pathwayMap = new HashMap<String, String>();

	private Map<String, Item> clusterMap = new HashMap<String, Item>();

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
	}

	/**
	 * {@inheritDoc}
	 * <br/>
	 * suppose the file name are as following: 
	 * autoname.xxx.[org] and 
	 * combined.xxx.[org] ;
	 * where [org] means the 3-letter code for the organism 
	 */
	public void process(Reader reader) throws Exception {
		
		String filename = getCurrentFile().getName();
		String org = filename.substring(filename.length()-3);
		if (filename.startsWith("autoname")) {
			readClusterNameMap(reader, org);
		} else {
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
			
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				if (StringUtils.isEmpty(cols[0])) {
					continue;
				}
				Item item = createItem("GeneSetCluster");
				String identifier = getClusterId(cols[0], org);
				item.setAttribute("identifier", identifier);
				String[] pathwayIds = cols[2].split(",");
				for (String pid : pathwayIds) {
					item.addToCollection("pathways", getPathway(pid));
				}
				clusterMap.put(identifier, item);
			}
		}
		
	}
	
	@Override
	public void close() throws Exception {
//		for (String key : clusterNameMap.keySet()) {
//			System.out.println(String.format("%s: %s", key, clusterNameMap.get(key)));
//		}
		for (String key : clusterMap.keySet()) {
			Item item = clusterMap.get(key);
			String name = clusterNameMap.get(key);
//			if (StringUtils.isEmpty(name)) {
//				System.out.println(key);
//			}
			item.setAttribute("name", name);
			store(item);
		}
	}

	private void readClusterNameMap(Reader reader, String organismCode) throws Exception {
		BufferedReader br = new BufferedReader(reader);

		String line = br.readLine();
		while (line != null) {
			String[] cols = line.split("\\t");
			clusterNameMap.put(getClusterId(cols[0], organismCode)
					, cols[2]);
			line = br.readLine();
		}
		br.close();
	}

	private String getPathway(String pathwayId) throws ObjectStoreException {
		String ret = pathwayMap.get(pathwayId);
		if (ret == null) {
			Item item = createItem("Pathway");
			item.setAttribute("identifier", pathwayId);
			store(item);
			ret = item.getIdentifier();
			pathwayMap.put(pathwayId, ret);
		}
		return ret;
	}
	
	/**
	 * 
	 * @param originalId original ID start with no
	 * @param organismCode 3-letter organism code
	 * @return unique ID
	 */
	private String getClusterId(String originalId, String organismCode) {
		return organismCode.substring(0, 1).toUpperCase() + originalId.substring(2);
	}
}
