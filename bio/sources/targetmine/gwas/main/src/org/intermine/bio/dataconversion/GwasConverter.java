package org.intermine.bio.dataconversion;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class GwasConverter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(GwasConverter.class);

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
			try {
				Double.parseDouble(cols[27]);
			} catch (NumberFormatException e) {
				throw new RuntimeException(String.format("Not a double value! accession: %s; pvalue: %s", cols[36], cols[27]));
			}
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
						String freqValue = split[0].replaceAll("\\xA0", "");
						try {
							Double.parseDouble(freqValue);
							gwaItem.setAttribute("frequency", freqValue);
							gwaItem.setAttribute("frequencyNote", split[1].replaceAll("\\(|\\)", ""));
						} catch (NumberFormatException e) {
							String value = processDouble(freqValue);
							if (value == null) {
								LOG.info(String.format("ERROR! Not a double value! accession: %s; frequency: %s", cols[36], freqValue));
//								throw new RuntimeException(String.format("Not a double value! accession: %s; frequency: %s", cols[36], freqValue));
							} else {
								LOG.info(String.format("Fix the wrong value! accession: %s; frequency: %s -> %s", cols[36], freqValue, value));
								freqValue = value;
								gwaItem.setAttribute("frequency", freqValue);
								gwaItem.setAttribute("frequencyNote", split[1].replaceAll("\\(|\\)", ""));
							}
						}
					}
				} else if (cols[26].contains("-")) {
					gwaItem.setAttribute("frequencyNote", cols[26]);
				} else {
					String freqValue = cols[26].replaceAll("\\xA0", "");
					try {
						Double.parseDouble(freqValue);
						gwaItem.setAttribute("frequency", freqValue);
					} catch (NumberFormatException e) {
						String value = processDouble(freqValue);
						if (value == null) {
							LOG.info(String.format("ERROR! Not a double value! accession: %s; frequency: %s", cols[36], freqValue));
//							throw new RuntimeException(String.format("Not a double value! accession: %s; frequency: %s", cols[36], freqValue));
						} else {
							LOG.info(String.format("Fix the wrong value! accession: %s; frequency: %s -> %s", cols[36], freqValue, value));
							freqValue = value;
							gwaItem.setAttribute("frequency", freqValue);
						}
					}
				}
			}
			String orBeta = cols[30];
			if (!StringUtils.isEmpty(orBeta)) {
				try {
					Double.parseDouble(orBeta);
					gwaItem.setAttribute("orBeta", orBeta);
				} catch (NumberFormatException e) {
					String value = processDouble(orBeta);
					if (value == null) {
						LOG.info(String.format("ERROR! Not a double value! accession: %s; orBeta: %s", cols[36], orBeta));
//						throw new RuntimeException(String.format("Not a double value! accession: %s; orBeta: %s", cols[36], orBeta));
					} else {
						LOG.info(String.format("Fix the wrong value! accession: %s; frequency: %s -> %s", cols[36], orBeta, value));
						orBeta = value;
						gwaItem.setAttribute("orBeta", orBeta);
					}
				}
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
			
			String snpIntId = cols[23];
			if (!StringUtils.isEmpty(snpIntId)) {
				try {
					Integer.parseInt(snpIntId);
				} catch (NumberFormatException e) {
					String value = processInteger(snpIntId);
					if (value != null) {
						LOG.info(String.format("Fix the wrong value! accession: %s; snp id: %s -> %s", cols[36], snpIntId, value));
						snpIntId = value;
					} else {
						throw new RuntimeException(String.format("Invalid SNP identifier! accession: %s; value: %s", cols[36], snpIntId));
					}
				}
				String dbSnpId = "rs" + snpIntId;

				String snpItemRef = snpMap.get(dbSnpId);
				if (snpItemRef == null) {
					Item snpItem = createItem("SNP");
					snpItem.setAttribute("identifier", dbSnpId);
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

	private String processDouble(String value) {
		Pattern pattern = Pattern.compile("(\\d+\\.\\d+).+");
		Matcher matcher = pattern.matcher(value);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return null;
	}

	private String processInteger(String value) {
		Pattern pattern = Pattern.compile("(\\d+).+");
		Matcher matcher = pattern.matcher(value);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return null;
	}
}
