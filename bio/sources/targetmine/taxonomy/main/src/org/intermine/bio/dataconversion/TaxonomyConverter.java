package org.intermine.bio.dataconversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;


/**
 * 
 * @author chenyian
 */
public class TaxonomyConverter extends BioFileConverter
{
//	private static final Logger LOG = Logger.getLogger(TaxonomyConverter.class);
	//
    private static final String DATASET_TITLE = "Taxonomy";
    private static final String DATA_SOURCE_NAME = "NCBI";

    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public TaxonomyConverter(ItemWriter writer, Model model) {
        super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
    }
    
//    private Map<String, String> nameMap;

	private File nameFile;
	public void setNameFile(File nameFile) {
		this.nameFile = nameFile;
	}

    /**
     * 
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
    	Map<String, String> nameMap = readNameMap();
    	
    	Iterator<String[]> iterator = FormattedTextParser.parseDelimitedReader(reader, '|');
    	while (iterator.hasNext()) {
    		String[] cols = iterator.next();
    		String id = cols[0].trim();
			TaxonEntry entry = getTaxonEntry(id);
    		String parentId = cols[1].trim();
    		if (!id.equals(parentId)) {
    			entry.setParent(getTaxonEntry(parentId));
    		}
    		entry.setRank(cols[2].trim());
    	}
    	
    	System.out.println("Sorting entries...");
    	List<TaxonEntry> allEntries = new ArrayList<TaxonEntry>(taxonMap.values());
    	Collections.sort(allEntries, new Comparator<TaxonEntry>() {
			@Override
			public int compare(TaxonEntry o1, TaxonEntry o2) {
				return Integer.valueOf(o1.getAllParents().size()).compareTo(Integer.valueOf(o2.getAllParents().size()));
			}
		});

    	System.out.println("Creating entries...");
		for (TaxonEntry entry : allEntries) {
			String taxonId = entry.getTaxonId();
    		Item item = createItem("Taxonomy");
    		item.setAttribute("taxonId", taxonId);
    		item.setAttribute("name", nameMap.get(taxonId));
    		item.setAttribute("rank", entry.getRank());
    		TaxonEntry parent = entry.getParent();
    		if (parent != null) {
    			item.setReference("parent", taxonRefMap.get(parent.getTaxonId()));
    		}
    		Set<TaxonEntry> allParents = entry.getAllParents();
        	for (TaxonEntry taxonEntry : allParents) {
        		item.addToCollection("allParents", taxonRefMap.get(taxonEntry.getTaxonId()));
        	}
        	store(item);
        	taxonRefMap.put(taxonId, item.getIdentifier());
    	}
    }
    
    private Map<String, String> taxonRefMap = new HashMap<String, String>();
    
    private Map<String, TaxonEntry> taxonMap = new HashMap<String, TaxonEntry>();
    private TaxonEntry getTaxonEntry(String taxonId) {
    	TaxonEntry entry = taxonMap.get(taxonId);
    	if (entry == null) {
    		entry = new TaxonEntry(taxonId);
    		taxonMap.put(taxonId, entry);
    	}
    	return entry;
    }
    
    private Map<String, String> readNameMap() {
		System.out.println("Processing the file names.dmp......");
		Map<String,String> ret = new HashMap<String, String>();
		try {
			Iterator<String[]> iterator = FormattedTextParser.parseDelimitedReader(new FileReader(nameFile), '|');
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				String taxonId = cols[0].trim();
				String name = cols[1].trim();
				String nameClass = cols[3].trim();
				if (ret.get(taxonId) == null) {
					ret.put(taxonId, name);
				} else if (nameClass.equals("scientific name")) {
					ret.put(taxonId, name);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("The file 'names.dmp' is not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		return ret;
    }
    
    private class TaxonEntry {
    	private String taxonId;
    	private String rank;
    	private TaxonEntry parent;
    	
		public TaxonEntry(String taxonId) {
			this.taxonId = taxonId;
		}

		TaxonEntry getParent() {
			return parent;
		}

		void setParent(TaxonEntry parent) {
			this.parent = parent;
		}

		void setRank(String rank) {
			this.rank = rank;
		}

		String getTaxonId() {
			return taxonId;
		}

		String getRank() {
			return rank;
		}
		
		Set<TaxonEntry> getAllParents() {
			Set<TaxonEntry> ret = new HashSet<TaxonEntry>();
			if (parent != null) {
				ret.add(parent);
				ret.addAll(parent.getAllParents());
			}
			return ret;
		}
    }
}
