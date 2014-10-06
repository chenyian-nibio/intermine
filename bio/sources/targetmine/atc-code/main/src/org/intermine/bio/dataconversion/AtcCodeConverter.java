package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.intermine.dataconversion.FileConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class AtcCodeConverter extends FileConverter {
	//
	private static Logger LOG = Logger.getLogger(AtcCodeConverter.class);
	
	private Map<String, String> atcMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public AtcCodeConverter(ItemWriter writer, Model model) {
		super(writer, model);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {

		BufferedReader in = null;
		try {
			in = new BufferedReader(reader);
			String line;
			while ((line = in.readLine()) != null) {
				
				String[] strings = line.split("\\s+", 2);
				
				int length = strings[0].length();
				if (length == 1) {
					Item item = createItem("AtcClassification");
					item.setAttribute("atcCode", strings[0]);
					item.setAttribute("name", strings[1].trim().toUpperCase());
					store(item);
					atcMap.put(strings[0], item.getIdentifier());
				} else if (length > 2 && length < 6) {
					Item item = createItem("AtcClassification");
					item.setAttribute("atcCode", strings[0]);
					item.setAttribute("name", strings[1].trim());
					
					String parentCode;
					if (length == 3) {
						parentCode = strings[0].substring(0, 1);
					} else {
						parentCode = strings[0].substring(0, length -1);
					}
//					LOG.info(String.format("'%s' -> '%s'", strings[0], parentCode));
					String parentRefId = atcMap.get(parentCode);
					if (parentRefId == null) {
						throw new RuntimeException(String.format("'%s' -> '%s'", strings[0], parentCode));
					}
					item.setReference("parent", parentRefId);
					
					store(item);
					atcMap.put(strings[0], item.getIdentifier());
				} else {
					LOG.error("Invalid code: " + strings[0]);
				}
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
}
