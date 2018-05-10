package org.intermine.bio.web.displayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.api.InterMineAPI;
import org.intermine.model.InterMineObject;
import org.intermine.web.displayer.ReportDisplayer;
import org.intermine.web.logic.config.ReportDisplayerConfig;
import org.intermine.web.logic.results.ReportObject;

public class GeneDiseaseDisplayer extends ReportDisplayer {
	protected static final Logger LOG = Logger.getLogger(GeneDiseaseDisplayer.class);
	
	private static final List<String> IGNORED_DISEASE_NAMES = Arrays.asList("not specified", "not provided"); 

	public GeneDiseaseDisplayer(ReportDisplayerConfig config, InterMineAPI im) {
		super(config, im);
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	@Override
	public void display(HttpServletRequest request, ReportObject reportObject) {
		InterMineObject gene = (InterMineObject) reportObject.getObject();
		
		HashSet<String> ignoredNames = new HashSet<String>(IGNORED_DISEASE_NAMES);
		
		List<List<String>> ret = new ArrayList<List<String>>();
		
		try {
//			Set<InterMineObject> diseases = (Set<InterMineObject>) gene.getFieldValue("diseases");
//			for (InterMineObject disease : diseases) {
//				String diseaseTitle = (String) ((InterMineObject) disease.getFieldValue("diseaseTerm")).getFieldValue("title");
//			}
			
			Map<String, Set<String>> diseaseSnpMap = new HashMap<String, Set<String>>();
			Map<String, List<String>> snpInfoMap = new HashMap<String, List<String>>();
			Map<String, Set<InterMineObject>> snpGwasMap = new HashMap<String, Set<InterMineObject>>();
			
			Set<InterMineObject> snps = (Set<InterMineObject>) gene.getFieldValue("snps");
			for (InterMineObject vaItem : snps) {
				String fc = (String) ((InterMineObject) vaItem.getFieldValue("function")).getFieldValue("name");
				InterMineObject snp = (InterMineObject) vaItem.getFieldValue("snp");
				String snpId = (String) snp.getFieldValue("identifier");
				snpGwasMap.put(snpId, (Set<InterMineObject>) snp.getFieldValue("genomeWideAssociations"));
				Set<InterMineObject> alleles = (Set<InterMineObject>) snp.getFieldValue("alleles");
				Set<String> csSet = new HashSet<String>();
				for (InterMineObject allele : alleles) {
					String cs = (String) allele.getFieldValue("clinicalSignificance");
					csSet.add(String.format("<a href=\"report.do?id=%s\">%s</a>", allele.getId().toString(), cs));
					Set<InterMineObject> variations = (Set<InterMineObject>) allele.getFieldValue("variations");
					for (InterMineObject var : variations) {
						Set<InterMineObject> diseaseTerms = (Set<InterMineObject>) var.getFieldValue("diseaseTerms");
						for (InterMineObject dt : diseaseTerms) {
							String diseaseTitle = (String) dt.getFieldValue("title");
							if (ignoredNames.contains(diseaseTitle)) {
								continue;
							}
							if (diseaseSnpMap.get(diseaseTitle) == null) {
								diseaseSnpMap.put(diseaseTitle, new HashSet<String>());
							}
							diseaseSnpMap.get(diseaseTitle).add(snpId);
						}
					}
				}
				
				Set<InterMineObject> frequencies = (Set<InterMineObject>) snp.getFieldValue("frequencies");
				Map<String, String> freqMap = new HashMap<String, String>();
				for (InterMineObject freqItem : frequencies) {
					String allele = (String) freqItem.getFieldValue("allele");
					Float frequency = (Float) freqItem.getFieldValue("frequency");
					String dataSetCode = (String) ((InterMineObject) freqItem.getFieldValue("dataSet")).getFieldValue("code");
					String popCode = (String) ((InterMineObject) freqItem.getFieldValue("population")).getFieldValue("code");
					String key = String.format("%s, %s", popCode, dataSetCode);
					String freqString = String.format("%s: %.2f", allele, frequency);
					freqMap.put(key, freqString);
				}
				String maf = "-";
				if (freqMap.size() > 0) {
					if (freqMap.get("JPN, HGVD") != null) {
						maf = String.format("%s (JPN, HGVD)", freqMap.get("JPN, HGVD"));
					} else if (freqMap.get("JPN, 1KJPN") != null) {
						maf = String.format("%s (JPN, 1KJPN)", freqMap.get("JPN, 1KJPN"));
					} else if (freqMap.get("JPT, 1KGP") != null) {
						maf = String.format("%s (JPT, 1KGP)", freqMap.get("JPT, 1KGP"));
					} else {
						String pop = freqMap.keySet().iterator().next();
						maf = String.format("%s (%s)", freqMap.get(pop), pop);
					}
				}
				
				snpInfoMap.put(snpId, Arrays.asList(fc, maf, StringUtils.join(csSet, "; "), snp.getId().toString()));
			}
			
			Set<String> diseases = diseaseSnpMap.keySet();
			for (String d : diseases) {
				Set<String> snpIds = diseaseSnpMap.get(d);
				for (String snpId : snpIds) {
					if (snpInfoMap.get(snpId) != null) { // should not be null?
						List<String> info = snpInfoMap.get(snpId);
						List<String> pvalues = new ArrayList<String>();
						if (snpGwasMap.get(snpId) != null) {
							Set<InterMineObject> gwasSet = snpGwasMap.get(snpId);
							for (InterMineObject gwasItem : gwasSet) {
								Double pvalue = (Double) gwasItem.getFieldValue("pvalue");
								pvalues.add(String.format("<a href=\"report.do?id=%s\">%s</a>", gwasItem.getId().toString(), pvalue.toString()));
//								Set<InterMineObject> efoTerms = (Set<InterMineObject>) gwasItem
//										.getFieldValue("efoTerms");
//								for (InterMineObject efot : efoTerms) {
//									String diseaseTitle = (String) efot.getFieldValue("name");
//								}
							}
						}
						ret.add(Arrays.asList(d, String.format("<a href=\"report.do?id=%s\">%s</a>", info.get(3), snpId), 
								info.get(0), info.get(1), info.get(2), StringUtils.join(pvalues, "<br/>")));
					}
				}
			}
			
		} catch (IllegalAccessException e) {
//			e.printStackTrace();
			LOG.error(e.getMessage());
		}
		
		request.setAttribute("geneticDiseaseTable", ret);
	}
	
	

}
