package org.intermine.bio.dataconversion;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang.StringUtils;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class GwasConverter extends BioFileConverter {
//	private static final Logger LOG = Logger.getLogger(GwasConverter.class);

	private static final String DATASET_TITLE = "GWAS Catalog";
	private static final String DATA_SOURCE_NAME = "GWAS Catalog";

//	private static final String HUMAN_TAXON_ID = "9606";

//	private Map<String, String> geneMap = new HashMap<String, String>();
	private Map<String, String> publicationMap = new HashMap<String, String>();
	private Map<String, String> efoMap = new HashMap<String, String>();
	private Map<String, String> snpMap = new HashMap<String, String>();
//	private Map<String, String> chromosomeMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public GwasConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	private static String ENCODING = "utf-8";

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		List<String> lines = IOUtils.readLines(new ReaderInputStream(reader, ENCODING), ENCODING);
		// sikp header
//		iterator.next();
		for (int i = 1; i < lines.size(); i++) {
			String[] cols = lines.get(i).split("\t");
			if (cols.length < 2) {
				continue;
			}
			Item gwaItem = createItem("GenomeWideAssociation");
			gwaItem.setAttribute("accession", cols[36]);
			gwaItem.setAttribute("date", cols[0]);
			gwaItem.setAttribute("study", cols[6]);
			gwaItem.setAttribute("trait", cols[7]);
			if (!StringUtils.isEmpty(cols[13])) {
				gwaItem.setAttribute("reportedGenes", cols[13]);
			}
			gwaItem.setAttribute("pvalue", cols[27]);
			if (!StringUtils.isEmpty(cols[29])) {
				gwaItem.setAttribute("pvalueNote", cols[29]);
			}
			if (!StringUtils.isEmpty(cols[26]) && !cols[26].equals("NR")) {
				// TODO need refactoring... 
				if (cols[26].contains("(")) {
					String[] split = cols[26].split(" ");
					if (split[0].contains("-")) {
						gwaItem.setAttribute("frequencyNote", cols[26]);
					} else {
						gwaItem.setAttribute("frequency", split[0].replaceAll("\\xA0", ""));
						gwaItem.setAttribute("frequencyNote", split[1].replaceAll("\\(|\\)", ""));
					}
				} else if (cols[26].contains("-")) {
					gwaItem.setAttribute("frequencyNote", cols[26]);
				} else {
					gwaItem.setAttribute("frequency", cols[26].replaceAll("\\xA0", ""));
				}
			}
			if (!StringUtils.isEmpty(cols[30])) {
				gwaItem.setAttribute("orBeta", cols[30]);
			}
			if (!StringUtils.isEmpty(cols[31])) {
				gwaItem.setAttribute("confidenceInterval", cols[31]);
			}
			if (!StringUtils.isEmpty(cols[32])) {
				gwaItem.setAttribute("platform", cols[32]);
			}
			if (!StringUtils.isEmpty(cols[34])) {
				gwaItem.setAttribute("mappedTrait", cols[34]);
			}
			if (!StringUtils.isEmpty(cols[20])) {
				gwaItem.setAttribute("riskAllele", cols[20]);
			}

			gwaItem.setReference("publication", getPublication(cols[1]));
			
			if (!StringUtils.isEmpty(cols[8])) {
				gwaItem.setAttribute("initialSampleDescription", cols[8]);
				String[] initial = cols[8].split(", ");
				for (String sampleSize : initial) {
					String[] parts = sampleSize.split(" ", 2);
					if (parts.length > 1) {
						try {
							int number = Integer.parseInt(parts[0].replaceAll(",", ""));
							Item gssItem = createItem("GwasSampleSize");
							gssItem.setAttribute("size", String.valueOf(number));
							gssItem.setAttribute("population", parts[1]);
							gssItem.setAttribute("type", "initial");
							store(gssItem);
							gwaItem.addToCollection("sampleSizes", gssItem);
							
						} catch (NumberFormatException e) {
							continue;
						}
					}
				}
			}
			
			if (!StringUtils.isEmpty(cols[9])) {
				gwaItem.setAttribute("replicationSampleDescription", cols[9]);
				String[] replication = cols[9].split(", ");
				for (String sampleSize : replication) {
					String[] parts = sampleSize.split(" ", 2);
					if (parts.length > 1) {
						try {
							int number = Integer.parseInt(parts[0].replaceAll(",", ""));
							Item gssItem = createItem("GwasSampleSize");
							gssItem.setAttribute("size", String.valueOf(number));
							gssItem.setAttribute("population", parts[1]);
							gssItem.setAttribute("type", "replication");
							store(gssItem);
							gwaItem.addToCollection("sampleSizes", gssItem);
							
						} catch (NumberFormatException e) {
							continue;
						}
					}
				}
			}
			
			if (!StringUtils.isEmpty(cols[23])) {
				String dbSnpId = "rs" + cols[23];
				String snpItemRef = snpMap.get(dbSnpId);
				if (snpItemRef == null) {
					Item snpItem = createItem("SNP");
					snpItem.setAttribute("identifier", dbSnpId);
//					if (!StringUtils.isEmpty(cols[12])) {
//						Item location = createItem("Location");
//						location.setAttribute("start", cols[12]);
//						location.setAttribute("end", cols[12]);
//						if (!StringUtils.isEmpty(cols[11])) {
//							location.setReference("locatedOn", getChromosome(cols[11], HUMAN_TAXON_ID));
//						}
//						store(location);
//						snpItem.setReference("location", location);
//					}
					store(snpItem);
					snpItemRef = snpItem.getIdentifier();
					snpMap.put(dbSnpId, snpItemRef);
				}
				gwaItem.setReference("snp", snpItemRef);
				
				String[] efoUrls = cols[35].split(", ");
				for (String efoUrl: efoUrls) {
					if (efoUrl.contains("EFO_")) {
						String efoId = "EFO:" + efoUrl.substring(efoUrl.indexOf("EFO_") + 4);
						gwaItem.addToCollection("efoTerms", getEfoTerm(efoId));
					}
				}

				store(gwaItem);
			}

			
		}
	}
	
	private String getEfoTerm(String efoId) throws ObjectStoreException {
		String ret = efoMap.get(efoId);
		if (ret == null) {
			Item item = createItem("EFOTerm");
			item.setAttribute("identifier", efoId);
			store(item);
			ret = item.getIdentifier();
			efoMap.put(efoId, ret);
		}
		return ret;
	}

//	@Override
//	public void close() throws Exception {
//		store(snpMap.values());
//	}

//	private String getGene(String geneId) throws ObjectStoreException {
//		String ret = geneMap.get(geneId);
//		if (ret == null) {
//			Item item = createItem("Gene");
//			item.setAttribute("primaryIdentifier", geneId);
//			item.setAttribute("ncbiGeneId", geneId);
//			store(item);
//			ret = item.getIdentifier();
//			geneMap.put(geneId, ret);
//		}
//		return ret;
//	}

	private String getPublication(String pubMedId) throws ObjectStoreException {
		String ret = publicationMap.get(pubMedId);
		if (ret == null) {
			Item item = createItem("Publication");
			item.setAttribute("pubMedId", pubMedId);
			store(item);
			ret = item.getIdentifier();
			publicationMap.put(pubMedId, ret);
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

}
