package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.FileConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class DatasetInfoConverter extends FileConverter {
	private static Logger LOG = Logger.getLogger(DatasetInfoConverter.class);

	//

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public DatasetInfoConverter(ItemWriter writer, Model model) {
		super(writer, model);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {

		Map<String, String> dataSetInfoMap = new HashMap<String, String>();
		
		BufferedReader in = null;
		try {
			in = new BufferedReader(reader);
			String line;
			while ((line = in.readLine()) != null) {
				if (StringUtils.isEmpty(line)) {
					continue;
				}
				String[] keyValue = line.split("=");
				dataSetInfoMap.put(keyValue[0], keyValue[1]);
			}
			
			if (dataSetInfoMap.get("data_set") != null) {
				Item item = createItem("DataSet");
				item.setAttribute("name", dataSetInfoMap.get("data_set"));
				item.setAttribute("code", dataSetInfoMap.get("code"));
				item.setAttribute("url", dataSetInfoMap.get("url"));
				item.setAttribute("description", dataSetInfoMap.get("description"));
				item.setAttribute("category", dataSetInfoMap.get("category"));
				if (dataSetInfoMap.get("date_type") != null) {
					item.setAttribute("dateType", dataSetInfoMap.get("date_type"));
				}
				if (dataSetInfoMap.get("date") != null) {
					item.setAttribute("date", dataSetInfoMap.get("date"));
				}
				if (dataSetInfoMap.get("version") != null) {
					item.setAttribute("version", dataSetInfoMap.get("version"));
				}
				if (dataSetInfoMap.get("data_source") != null) {
					item.setReference("dataSource", getDataSource(dataSetInfoMap.get("data_source")));
				}
				store(item);
			} else {
				LOG.error("Failed to read data set name from: " + getCurrentFile().getAbsolutePath());
			}
			
		} catch (FileNotFoundException e) {
			LOG.error(e);
		} catch (IOException e) {
			LOG.error(e);
		} finally {
			if (in != null)
				in.close();
		}

	}
	
	private Map<String,String> dataSourceMap = new HashMap<String, String>();
	
	private String getDataSource(String name) throws ObjectStoreException {
		String ret = dataSourceMap.get(name);
		if (ret == null) {
			Item item = createItem("DataSource");
			item.setAttribute("name", name);
			store(item);
			ret = item.getIdentifier();
			dataSourceMap.put(name, ret);
		}
		return ret;
	}
}
