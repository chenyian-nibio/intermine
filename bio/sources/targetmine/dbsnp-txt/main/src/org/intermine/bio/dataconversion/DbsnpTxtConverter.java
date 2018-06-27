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

import org.apache.commons.lang.StringUtils;
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
public class DbsnpTxtConverter extends BioFileConverter
{
	private static final Logger LOG = Logger.getLogger(DbsnpTxtConverter.class);
	//
    private static final String DATASET_TITLE = "dbSNP";
    private static final String DATA_SOURCE_NAME = "NCBI";
    
//	private static final int HUMAN_TAXON_ID = 9606;
	
//	private boolean subsetOnly = false;
    private String tableType = "";
    public void setTableType(String tableType) {
    	this.tableType = tableType;
    }

//    private File snpListFile;
//    public void setSnpListFile(File snpListFile) {
//    	this.snpListFile = snpListFile;
//    	subsetOnly = true;
//    }
	private File snpFunctionFile;
	public void setSnpFunctionFile(File snpFunctionFile) {
		this.snpFunctionFile = snpFunctionFile;
	}

    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public DbsnpTxtConverter(ItemWriter writer, Model model) {
        super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
    }

    /**
     * 
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
    	getSnpIds();
    	LOG.info(String.format("Found %d SNPs", snpIdSet.size()));
    	System.out.println(String.format("Found %d SNPs", snpIdSet.size()));
    	
		if (tableType.equals("info")) {
			System.out.println("Processing SNP information......");
			processDbsnpInfo(reader);
		} else if (tableType.equals("gene")) {
			System.out.println("Processing SNP gene association......");
			processDbsnpGene(reader);
		} else if (tableType.equals("transcript")) {
			System.out.println("Processing SNP transcript association......");
			processDbsnpTranscript(reader);
		}
    }
    
    Set<String> snpIdSet = new HashSet<String>();
    
//    private void loadSnpSet() {
//    	System.out.println("Loading necessary SNP ids...");
//    	LOG.info("Loading necessary SNP ids...");
//		Iterator<String[]> iterator;
//		try {
//			iterator = FormattedTextParser.parseTabDelimitedReader(new FileReader(snpListFile));
//			while (iterator.hasNext()) {
//				String[] cols = iterator.next();
//				String rsId = cols[0].trim();
//				snpIdSet.add(rsId);
//			}
//			System.out.println(String.format("Load %d SNP ids.", snpIdSet.size()));
//			LOG.info(String.format("Load %d SNP ids.", snpIdSet.size()));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//			throw new RuntimeException("The snpListFile was not found.");
//		} catch (IOException e) {
//			e.printStackTrace();
//			throw new RuntimeException(e);
//		}
//	}

	private void processDbsnpInfo(Reader reader) throws Exception {
		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		int line = 1;
		int storedSnp = 0;
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String rsId = cols[0];
			String pubmedIdString = cols[6];
			if (snpIdSet.contains(rsId) || !StringUtils.isEmpty(pubmedIdString)) {
				String allele = cols[1];
				String chr = cols[2];
				String orient = cols[3];
				String locationString = cols[4];
//				String group = cols[5];
				
				Item item = createItem("SNP");
				item.setAttribute("identifier", rsId);
				if (!StringUtils.isEmpty(orient)) {
					item.setAttribute("orientation", orient);
				}
				if (!StringUtils.isEmpty(chr)) {
					item.setAttribute("chromosome", chr);
				}
				if (!StringUtils.isEmpty(allele)) {
					item.setAttribute("refSnpAllele", allele);
				}
				if (!StringUtils.isEmpty(locationString)) {
					item.setAttribute("location", locationString);
					
//					String[] split = locationString.split(";");
//					for (String l : split) {
//						String chromosome = getChromosome(l.substring(0, l.indexOf(":")));
//						String pos = l.substring(l.indexOf(":") + 1);
//						Item location = createItem("Location");
//						if (pos.contains("..")) {
//							location.setAttribute("start", pos.substring(0, pos.indexOf("..")));
//							location.setAttribute("end", pos.substring(pos.indexOf("..") + 2));
//						} else {
//							location.setAttribute("start", pos);
//							location.setAttribute("end", pos);
//						}
//						location.setReference("locatedOn", chromosome);
//						store(location);
//						item.addToCollection("locations", location);
//					}
				}
				if (!StringUtils.isEmpty(pubmedIdString)) {
					String[] ids = pubmedIdString.split(";");
					for (String id : ids) {
						item.addToCollection("publications", getPublication(id));
					}
				}
				store(item);
				storedSnp++;
			}
			line++;
			
			if (line % 5000000 == 0) {
				System.out.println(String.format("Process %d lines...", line));
			}
		}
		System.out.println("Finish reading the file...");
		
		System.out.println(String.format("Stored %d SNPs", storedSnp));
		LOG.info(String.format("Stored %d SNPs", storedSnp));
	}
	
	private void processDbsnpGene(Reader reader) throws Exception {
		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		int count = 1;
		String prevSNPId = "";
		String prevSNPRef = "";
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String snpId = "rs" + cols[0];
			if (snpIdSet.contains(snpId)) {
				String geneId = cols[1];
				Item vaItem = createItem("VariationAnnotation");
				vaItem.setAttribute("identifier", snpId + "-" + geneId);
				vaItem.setReference("gene", getGene(geneId));
				if (snpId.equals(prevSNPId)) {
					vaItem.setReference("snp", prevSNPRef);
				} else {
					Item item = createItem("SNP");
					item.setAttribute("identifier", snpId);
					store(item);
					vaItem.setReference("snp", item);
					prevSNPId = snpId;
					prevSNPRef = item.getIdentifier();
				}
				store(vaItem);
			}
			count++;
			if (count % 5000000 == 0) {
				System.out.println(String.format("Process %d lines...", count));
			}
		}
	}

	private void processDbsnpTranscript(Reader reader) throws Exception {
		Map<Integer, String> functionMap = getAnpFunctionMap();
		Map<String, String> variAnnotMap = new HashMap<String, String>();
		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		int count = 1;
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String snpId = "rs" + cols[0];
			if (snpIdSet.contains(snpId)) {
				String geneId = cols[1];
				String mrnaAcc = cols[2];
				String mrnaPos = cols[3];
				String orientation = cols[4];
				String allele = cols[5];
				String codon = cols[6];
				String proteinAcc = cols[7];
				String aaPos = cols[8];
				String residue = cols[9];

				String fxn = cols[10];
				String funcRef = null; // should not be null; but there are some strange case, e.g. rs1126437
				if (!fxn.equals("null")) {
					funcRef = functionMap.get(Integer.valueOf(fxn));
				}
				
				String key = snpId + "-" + geneId;
				String vaItemRef = variAnnotMap.get(key);
				if (vaItemRef == null) {
					Item vaItem = createItem("VariationAnnotation");
					vaItem.setAttribute("identifier", snpId + "-" + geneId);
					// take the first one
					if (funcRef != null) {
						vaItem.setReference("function", funcRef);
					}
					store(vaItem);
					vaItemRef = vaItem.getIdentifier();
					variAnnotMap.put(key, vaItemRef);
				}
				createSNPReference(mrnaAcc, mrnaPos, orientation, allele, codon, proteinAcc, Integer.valueOf(aaPos), residue, funcRef, vaItemRef);
			}
			count++;
			if (count % 5000000 == 0) {
				System.out.println(String.format("Process %d lines...", count));
			}
		}
	}
	
	private Map<Integer, String> getAnpFunctionMap() throws Exception {
		Map<Integer, String> functionMap = new HashMap<Integer, String>();
		System.out.println("Parsing the file SnpFunctionFile......");
		try {
			Iterator<String[]> iterator = FormattedTextParser
					.parseCsvDelimitedReader(new FileReader(snpFunctionFile));
			// skip header
			iterator.next();
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				int classId = Integer.valueOf(cols[0]);
				Item item = createItem("SNPFunction");
				item.setAttribute("name", cols[1]);
				item.setAttribute("description", cols[2]);
				store(item);
				functionMap.put(Integer.valueOf(classId), item.getIdentifier());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("The snpFunctionFile was not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return functionMap;
	}

	private Map<String, String> geneMap = new HashMap<String, String>();
//	private Map<String, String> snpMap = new HashMap<String, String>();
//	private Map<String, String> chromosomeMap = new HashMap<String, String>();
	private Map<String, String> publicationMap = new HashMap<String, String>();
	private String getGene(String primaryIdentifier) throws ObjectStoreException {
		String ret = geneMap.get(primaryIdentifier);
		if (ret == null) {
			Item item = createItem("Gene");
			item.setAttribute("primaryIdentifier", primaryIdentifier);
			store(item);
			ret = item.getIdentifier();
			geneMap.put(primaryIdentifier, ret);
		}
		return ret;
	}
//	private String getSnp(String identifier) throws ObjectStoreException {
//		String ret = snpMap.get(identifier);
//		if (ret == null) {
//			Item item = createItem("SNP");
//			item.setAttribute("identifier", identifier);
//			store(item);
//			ret = item.getIdentifier();
//			snpMap.put(identifier, ret);
//		}
//		return ret;
//	}
//	private String getChromosome(String chr) throws ObjectStoreException {
//		String ret = chromosomeMap.get(chr);
//		if (ret == null) {
//			Item item = createItem("Chromosome");
//			String chrId = chr;
//			if (chr.toLowerCase().startsWith("chr")) {
//				chrId = chr.substring(3);
//			}
//			item.setAttribute("symbol", chrId);
//			item.setReference("organism", getOrganism(String.valueOf(HUMAN_TAXON_ID)));
//			store(item);
//			ret = item.getIdentifier();
//			chromosomeMap.put(chr, ret);
//		}
//		return ret;
//	}
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

	private String createSNPReference(String mrnaAcc, String mrnaPos, String orientation,
			String allele, String codon, String proteinAcc, int aaPos, String residue,
			String funcRef, String vaItemRef) throws ObjectStoreException {
		Item item = createItem("SNPReference");
		item.setAttribute("mrnaAccession", mrnaAcc);
		if (!StringUtils.isEmpty(mrnaPos)) {
			item.setAttribute("mrnaPosition", mrnaPos);
		}
		if (!StringUtils.isEmpty(orientation)) {
			item.setAttribute("orientation", orientation);
		}
		if (!StringUtils.isEmpty(allele)) {
			item.setAttribute("mrnaAllele", allele);
		}
		if (!StringUtils.isEmpty(codon)) {
			item.setAttribute("mrnaCodon", codon);
		}
		if (!StringUtils.isEmpty(proteinAcc)) {
			item.setAttribute("proteinAccession", proteinAcc);
		}
		if (aaPos > 0) {
			item.setAttribute("proteinPosition", String.valueOf(aaPos));
		}
		if (!StringUtils.isEmpty(residue)) {
			item.setAttribute("residue", residue);
		}
		if (funcRef != null) {
			item.setReference("function", funcRef);
		}
		item.setReference("annotation", vaItemRef);
		store(item);

		return item.getIdentifier();
	}

	private String osAlias = null;

	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

	@SuppressWarnings("unchecked")
	private void getSnpIds() throws Exception {
		ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);

		Query q = new Query();
		QueryClass qcSnp = new QueryClass(os.getModel().getClassDescriptorByName("SNP").getType());

		QueryField qfSnpId = new QueryField(qcSnp, "identifier");

		q.addFrom(qcSnp);
		q.addToSelect(qfSnpId);

		Results results = os.execute(q);
		Iterator<Object> iterator = results.iterator();
		while (iterator.hasNext()) {
			ResultsRow<String> rr = (ResultsRow<String>) iterator.next();
			snpIdSet.add(rr.get(0));
		}
	}

}
