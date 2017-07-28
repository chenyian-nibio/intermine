package org.intermine.bio.dataconversion;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;


/**
 * 
 * @author chenyian
 */
public class PredictedDbpConverter extends BioFileConverter {
	private static final String ANNOTATION_TYPE = "DNA binding";
	//

	private Map<String, Item> proteinMap = new HashMap<String, Item>();

	/**
	 * Constructor
	 * 
	 * @param writer the ItemWriter used to handle the resultant items
	 * @param model the Model
	 */
	public PredictedDbpConverter(ItemWriter writer, Model model) {
		super(writer, model);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		// create PredictedAnnotation first
		processPredictedDBP();

		// read-in the predicted binding site, and associated with PredictedAnnotation if available
		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String accession = cols[0];
			String sites = cols[1];
			
			for (String site: sites.split(",")) {
				String position = site.substring(1);
				Item item = createItem("PredictedRegion");
				item.setAttribute("start", position);
				item.setAttribute("end", position);
				item.setAttribute("regionType", "predicted");
				item.setAttribute("type", ANNOTATION_TYPE);
				Item protein = getProtein(accession);
				item.setReference("protein", protein);
				
				if (predictedAnnotationMap.get(accession) != null) {
					item.setReference("prediction", predictedAnnotationMap.get(accession));
				}
				
				store(item);
				
				protein.addToCollection("predictedRegions", item);
			}
		}
    }

	@Override
	public void close() throws Exception {
		store(proteinMap.values());
	}

	private Item getProtein(String primaryAccession) throws ObjectStoreException {
		Item ret = proteinMap.get(primaryAccession);
		if (ret == null) {
			ret = createItem("Protein");
			ret.setAttribute("primaryAccession", primaryAccession);
			proteinMap.put(primaryAccession, ret);
		}
		return ret;
	}

	private File dbpScoreFile;

	public void setDbpScoreFile(File dbpScoreFile) {
		this.dbpScoreFile = dbpScoreFile;
	}

	private Map<String, String> predictedAnnotationMap = new HashMap<String, String>();
    
    private void processPredictedDBP() throws ObjectStoreException, IOException {
		if (dbpScoreFile == null) {
			throw new RuntimeException("pathwayClassFile property not set");
		}
		
		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(new FileReader(
				dbpScoreFile));
    	// skip header ..
		iterator.next();
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String accession = cols[1];
			Float consensus2 = Float.valueOf(cols[5]);
//			Float precision = Float.valueOf(cols[12]);

			if (consensus2 >= 0.12f) {
				String confidence = "medium";
				if (consensus2 >= 0.22) {
					confidence = "high";
				}
				Item item = createItem("PredictedAnnotation");
				item.setAttribute("type", ANNOTATION_TYPE);
				item.setAttribute("confidence", confidence);
//				item.setAttribute("score", precision.toString());
				item.setReference("protein", getProtein(accession));
				store(item);
				predictedAnnotationMap.put(accession, item.getIdentifier());
			}
		}
    }

}
