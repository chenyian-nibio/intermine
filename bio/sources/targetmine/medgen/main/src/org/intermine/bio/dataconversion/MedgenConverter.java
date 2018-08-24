package org.intermine.bio.dataconversion;

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

import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class MedgenConverter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(MedgenConverter.class);
	//
	private static final String DATASET_TITLE = "MedGen";
	private static final String DATA_SOURCE_NAME = "NCBI";
	
	Map<String, Set<String>> pubmedIdMap = new HashMap<String, Set<String>>();
	Map<String, String> definitionMap = new HashMap<String, String>();
	
	private File pubmedFile;
	private File definitionFile;
	
	public void setPubmedFile(File pubmedFile) {
		this.pubmedFile = pubmedFile;
	}

	public void setDefinitionFile(File definitionFile) {
		this.definitionFile = definitionFile;
	}

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public MedgenConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 *
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		if (diseaseTermIdSet.isEmpty()) {
			getDiseaseTermIds();
		}
		// if the property pubmedFile is not set, we will not read the pubmed mapping
		// remove the pubmedFile property to skip loading the publication associations.
		// so far it dramatically slows down the whole database integration (chenyian, 2018.8.24)
		if (pubmedFile != null && pubmedIdMap.isEmpty()) {
			readPubmedFile();
		}
		if (definitionMap.isEmpty()) {
			readDefinitionFile();
		}
		
		try {
			System.out.println("parsing...");
			
			Iterator<String[]> iterator = FormattedTextParser.parseCsvDelimitedReader(reader);
			// ignore header
			iterator.next();
			int i = 1;
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				String cui = cols[0];
				String name = cols[1];
				
				if (!diseaseTermIdSet.contains(cui)) {
					continue;
				}
				
				Item item = createItem("DiseaseTerm");
				item.setAttribute("identifier", cui);
				item.setAttribute("name", name);
				
				String def = definitionMap.get(cui);
				if (def != null) {
					item.setAttribute("description", def);
				}
				
				if (pubmedIdMap.get(cui) != null) {
					for (String pmid : pubmedIdMap.get(cui)) {
						item.addToCollection("publications", getPublication(pmid));
					}
				}
				
				store(item);
				i++;
				
				if (i%1000 == 0) {
					System.out.println(String.format("%d line processed...", i));
				}
			}
			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}
	
	private void readPubmedFile() {
		String fileName = pubmedFile.getName();
		LOG.info(String.format("Parsing the file %s......", fileName));
		System.out.println(String.format("Parsing the file %s......", fileName));

		try {
			FileReader reader = new FileReader(pubmedFile);
			Iterator<String[]> iterator = FormattedTextParser.parseDelimitedReader(reader, '|');
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				String cui = cols[1];
				String pubmedId = cols[3];
				if (pubmedIdMap.get(cui) == null) {
					pubmedIdMap.put(cui, new HashSet<String>());
				}
				pubmedIdMap.get(cui).add(pubmedId);
			}
			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(String.format("The file '%s' not found.", fileName));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void readDefinitionFile() {
		String fileName = definitionFile.getName();
		LOG.info(String.format("Parsing the file %s......", fileName));
		System.out.println(String.format("Parsing the file %s......", fileName));
		
		try {
			FileReader reader = new FileReader(definitionFile);
			Iterator<String[]> iterator = FormattedTextParser.parseCsvDelimitedReader(reader);
			// ignore header
			iterator.next();
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				String cui = cols[0];
				String def = cols[1];
				definitionMap.put(cui, def);
			}
			reader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(String.format("The file '%s' not found.", fileName));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private Map<String, String> publicationMap = new HashMap<String, String>();

	private String getPublication(String pubmedId) throws ObjectStoreException {
		String ret = publicationMap.get(pubmedId);
		if (ret == null) {
			Item item = createItem("Publication");
			item.setAttribute("pubMedId", pubmedId);
			store(item);
			ret = item.getIdentifier();
			publicationMap.put(pubmedId, ret);
		}
		return ret;
	}

	private String osAlias = null;

	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

    Set<String> diseaseTermIdSet = new HashSet<String>();

    @SuppressWarnings("unchecked")
	private void getDiseaseTermIds() throws Exception {
		ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);

		Query q = new Query();
		QueryClass qcDiseaseTerm = new QueryClass(os.getModel().getClassDescriptorByName("DiseaseTerm").getType());

		QueryField qfIdentifier = new QueryField(qcDiseaseTerm, "identifier");

		q.addFrom(qcDiseaseTerm);
		q.addToSelect(qfIdentifier);

		Results results = os.execute(q);
		Iterator<Object> iterator = results.iterator();
		while (iterator.hasNext()) {
			ResultsRow<String> rr = (ResultsRow<String>) iterator.next();
			diseaseTermIdSet.add(rr.get(0));
		}
	}

}
