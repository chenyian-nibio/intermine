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
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;


/**
 * 
 * @author chenyian
 */
public class TmClinvarConverter extends BioFileConverter
{
	private static final Logger LOG = Logger.getLogger(TmClinvarConverter.class);
    //
    private static final String DATASET_TITLE = "ClinVar";
    private static final String DATA_SOURCE_NAME = "NCBI";
    
	private static final String HUMAN_TAXON_ID = "9606";

	private File submissionSummaryFile;
    private File variationCitationsFile;
    private File variationAlleleFile;
    private File crossReferencesFile;
    private File alleleGeneFile;

    public void setSubmissionSummaryFile(File submissionSummaryFile) {
		this.submissionSummaryFile = submissionSummaryFile;
	}

	public void setVariationCitationsFile(File variationCitationsFile) {
		this.variationCitationsFile = variationCitationsFile;
	}

	public void setVariationAlleleFile(File variationAlleleFile) {
		this.variationAlleleFile = variationAlleleFile;
	}

	public void setCrossReferencesFile(File crossReferencesFile) {
		this.crossReferencesFile = crossReferencesFile;
	}

	public void setAlleleGeneFile(File alleleGeneFile) {
		this.alleleGeneFile = alleleGeneFile;
	}

	/**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public TmClinvarConverter(ItemWriter writer, Model model) {
        super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
    }

    /**
     * 
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
    	processVariationAlleleFile();
    	processAlleleGeneFile();
    	processVariationCitationsFile();
    	processSubmissionSummaryFile();
    	
		try {
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				if (!cols[9].equals("-1") && cols[16].equals("GRCh38")) {
					Item allele = createItem("Allele");
					allele.setAttribute("identifier", cols[0]);
					allele.setAttribute("type", cols[1]);
					allele.setAttribute("name", cols[2]);
					allele.setAttribute("clinicalSignificance", cols[6]);
					allele.setAttribute("reviewStatus", cols[24]);
					
//					String type = snpTypeMap.get(cols[0] + "-" + cols[3]);
					
					String snp = getSnp("rs" + cols[9]);
					allele.addToCollection("snps", snp);
					Set<String> variations = allelVariationMap.get(cols[0]);
					if (variations != null) {
						for (String varId : variations) {
							String refId = variationMap.get(varId);
							if (refId != null) {
								allele.addToCollection("variations", refId);
							}
						}
					}
					allele.setReference("organism", getOrganism(HUMAN_TAXON_ID));
					
					store(allele);
				}
			}
			reader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("The file 'cross_references.txt' not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

    }

	private void processSubmissionSummaryFile() throws ObjectStoreException {
		LOG.info("Parsing the file submission_summary.txt......");
		System.out.println("Parsing the file submission_summary.txt......");

		try {
			FileReader reader = new FileReader(submissionSummaryFile);
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				
				Item item = createItem("ClinicalAssertion");
				item.setAttribute("clinicalSignificance", cols[1]);
				item.setAttribute("description", cols[3]);
				// NOTE: remove MedGen identifier or ?
				item.setAttribute("reportedPhenotypeInfo", cols[5]);
				item.setAttribute("reviewStatus", cols[6]);
				item.setAttribute("collectionMethod", cols[7]);
				item.setAttribute("originCounts", cols[8]);
				item.setAttribute("submitter", cols[9]);
				item.setAttribute("accession", cols[10]);
				if (!StringUtils.isEmpty(cols[11])) {
					item.setAttribute("submittedGeneSymbol", cols[11]);
				}
				item.setReference("variation", getVariation(cols[0]));
				store(item);
			}
			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("The file 'submission_summary.txt' not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}
	private void processVariationCitationsFile() throws ObjectStoreException {
		LOG.info("Parsing the file var_citations.txt......");
		System.out.println("Parsing the file var_citations.txt......");
		
		try {
			FileReader reader = new FileReader(variationCitationsFile);
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
			Map<String, Set<String>> varPubMap = new HashMap<String, Set<String>>();
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				if (cols[4].equals("PubMed")) {
					if (varPubMap.get(cols[1]) == null) {
						varPubMap.put(cols[1], new HashSet<String>());
					}
					varPubMap.get(cols[1]).add(cols[5]);
				}
			}
			reader.close();
			
			for (String varId : varPubMap.keySet()) {
				Item item = createItem("Variantion");
				item.setAttribute("identifier", varId);
				for (String pubmedId : varPubMap.get(varId)) {
					item.addToCollection("publications", getPublication(pubmedId));
				}
				String type = variationTypeMap.get(varId);
				if (type != null) {
					item.setAttribute("type", type);
				}
				store(item);
				variationMap.put(varId, item.getIdentifier());
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("The file 'var_citations.txt' not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
	}
	
	Map<String, String> variationTypeMap = new HashMap<String, String>();
	Map<String, Set<String>> allelVariationMap = new HashMap<String, Set<String>>();
	private void processVariationAlleleFile() {
		LOG.info("Parsing the file variation_allele.txt......");
		System.out.println("Parsing the file variation_allele.txt......");
		
		try {
			FileReader reader = new FileReader(variationAlleleFile);
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				variationTypeMap.put(cols[0], cols[1]);
				if (allelVariationMap.get(cols[2]) == null) {
					allelVariationMap.put(cols[2], new HashSet<String>());
				}
				allelVariationMap.get(cols[2]).add(cols[0]);
			}
			reader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("The file 'variation_allele.txt' not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
	}
	@SuppressWarnings("unused")
	private void processCrossReferencesFile() {
		// TODO insufficient SNP information, skip these contents at the moment (chenyian, 2018.2.7)
		LOG.info("Parsing the file cross_references.txt......");
		System.out.println("Parsing the file cross_references.txt......");
		
		try {
			FileReader reader = new FileReader(crossReferencesFile);
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
			}
			reader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("The file 'cross_references.txt' not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
	}
	
	Map<String, String> snpTypeMap = new HashMap<String, String>();
	private void processAlleleGeneFile() {
		// TODO unable to resolve the model conflict with SO, skip these contents at the moment (chenyian, 2018.2.7)
		// NOTE: allele and gene is many-to-many but many-to-one in the sequence ontology(SO)
		// use to get snp and gene relation; within the gene, upstream or downstream (temporary solution) 
		LOG.info("Parsing the file allele_gene.txt......");
		System.out.println("Parsing the file allele_gene.txt......");
		
		try {
			FileReader reader = new FileReader(alleleGeneFile);
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				snpTypeMap.put(cols[0] + "-" + cols[1], cols[5]);
			}
			reader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("The file 'allele_gene.txt' not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
	}
	
//	private Map<String, String> geneMap = new HashMap<String, String>();
	private Map<String, String> snpMap = new HashMap<String, String>();
	private Map<String, String> publicationMap = new HashMap<String, String>();
	private Map<String, String> variationMap = new HashMap<String, String>();
//	private Map<String, String> chromosomeMap = new HashMap<String, String>();
//	private String getGene(String primaryIdentifier) throws ObjectStoreException {
//		String ret = geneMap.get(primaryIdentifier);
//		if (ret == null) {
//			Item item = createItem("Gene");
//			item.setAttribute("primaryIdentifier", primaryIdentifier);
//			item.setAttribute("ncbiGeneId", primaryIdentifier);
//			store(item);
//			ret = item.getIdentifier();
//			geneMap.put(primaryIdentifier, ret);
//		}
//		return ret;
//	}
	
	private String getSnp(String identifier) throws ObjectStoreException {
		String ret = snpMap.get(identifier);
		if (ret == null) {
			Item item = createItem("SNP");
			item.setAttribute("identifier", identifier);
			store(item);
			ret = item.getIdentifier();
			snpMap.put(identifier, ret);
		}
		return ret;
	}
//	private String getChromosome(String chr, String taxonId) throws ObjectStoreException {
//		String key = chr + ":" + taxonId;
//		String ret = chromosomeMap.get(key);
//		if (ret == null) {
//			Item item = createItem("Chromosome");
//			String chrId = chr;
//			if (chr.toLowerCase().startsWith("chr")) {
//				chrId = chr.substring(3);
//			}
//			item.setAttribute("symbol", chrId);
//			if (!StringUtils.isEmpty(taxonId)) {
//				item.setReference("organism", getOrganism(taxonId));
//			}
//			store(item);
//			ret = item.getIdentifier();
//			chromosomeMap.put(key, ret);
//		}
//		return ret;
//	}
	
	// TODO this part should be covered by dbSNP; have to be deprecated in the future
	/**
	 * This is a temporary method since this part should be covered by dbSNP
	 */
//	private String getSnp(String identifier, String chromosome, String start, String end,
//			String reference, String alternate, String region, String type, String geneId) throws ObjectStoreException {
//		String ret = snpMap.get(identifier);
//		if (ret == null) {
//			Item snpItem = createItem("SNP");
//			snpItem.setAttribute("identifier", identifier);
//			snpItem.setAttribute("reference", reference);
//			snpItem.setAttribute("alternate", alternate);
//			snpItem.setAttribute("region", region);
//
//			Item location = createItem("Location");
//			location.setAttribute("start", start);
//			location.setAttribute("end", end);
//			location.setReference("locatedOn", getChromosome(chromosome, HUMAN_TAXON_ID));
//			store(location);
//			snpItem.setReference("location", location);
//			
//			Item gvItem = createItem("GenomicVariation");
//			// these information should come from dbsnp. this is just a temporary solution.
//			if (type != null) {
//				if (type.endsWith("upstream")) {
//					gvItem.setAttribute("type", "upstream");
//					gvItem.setAttribute("distance", "-1");
//				} else if (type.endsWith("downstream")) {
//					gvItem.setAttribute("type", "downstream");
//					gvItem.setAttribute("distance", "-1");
//				} else if (type.startsWith("within") || type.equals("genes overlapped by variant")) {
//					gvItem.setAttribute("type", "gene");
//					gvItem.setAttribute("distance", "0");
//				} else {
//					gvItem.setAttribute("type", "unknown");
//					gvItem.setAttribute("distance", "-1");
//				}
//			} else {
//				gvItem.setAttribute("type", "unknown");
//				gvItem.setAttribute("distance", "-1");
//			}
//			gvItem.setReference("gene", getGene(geneId));
//			gvItem.setReference("snp", snpItem);
//			store(gvItem);
//			snpItem.addToCollection("relatedGenes", gvItem);
//			
//			store(snpItem);
//			ret = snpItem.getIdentifier();
//			snpMap.put(identifier, ret);
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
	private String getVariation(String identifier) throws ObjectStoreException {
		String ret = variationMap.get(identifier);
		if (ret == null) {
			Item item = createItem("Variantion");
			item.setAttribute("identifier", identifier);
			String type = variationTypeMap.get(identifier);
			if (type != null) {
				item.setAttribute("type", type);
			}
			store(item);
			ret = item.getIdentifier();
			variationMap.put(identifier, ret);
		}
		return ret;
	}

}
