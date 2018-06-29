package org.intermine.bio.web.displayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
		
		HashSet<String> ignoredDiseaseNames = new HashSet<String>(IGNORED_DISEASE_NAMES);
		
		Map<String, List<List<String>>> diseaseInfoMap = new HashMap<String, List<List<String>>>();
		Map<String, List<String>> snpInfoMap = new HashMap<String, List<String>>();
		
//		List<List<String>> ret = new ArrayList<List<String>>();
		
		List<InterMineObject> disgenet = new ArrayList<InterMineObject>();
		List<InterMineObject> others = new ArrayList<InterMineObject>(); // so far only OMIM?
		
		try {
			Set<InterMineObject> snps = (Set<InterMineObject>) gene.getFieldValue("snps");
			for (InterMineObject vaItem : snps) {
				String fc = (String) ((InterMineObject) vaItem.getFieldValue("function")).getFieldValue("name");
				InterMineObject snp = (InterMineObject) vaItem.getFieldValue("snp");
				String snpId = (String) snp.getFieldValue("identifier");
				Set<InterMineObject> genomeWideAssociations = (Set<InterMineObject>) snp.getFieldValue("genomeWideAssociations");
				
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
					if (freqMap.size() > 1) {
						maf = maf + " ...";
					}
				}
				
				snpInfoMap.put(snpId, Arrays.asList(String.format("<a href=\"report.do?id=%s\">%s</a>", snp.getId(), snpId), 
						fc, String.format("<a href=\"report.do?id=%s\">%s</a>", snp.getId(), maf)));
				
				Set<InterMineObject> alleles = (Set<InterMineObject>) snp.getFieldValue("alleles");
				Set<String> csSet = new HashSet<String>();
				for (InterMineObject allele : alleles) {
					String cs = (String) allele.getFieldValue("clinicalSignificance");
					csSet.add(String.format("<a href=\"report.do?id=%s\">%s</a>", allele.getId().toString(), cs));
					Set<InterMineObject> variations = (Set<InterMineObject>) allele.getFieldValue("variations");
					for (InterMineObject var : variations) {
						Set<InterMineObject> publications = (Set<InterMineObject>) var.getFieldValue("publications");
						String numPub = String.format("<a href=\"report.do?id=%s\">%d</a>", var.getId().toString(), publications.size());
						Set<InterMineObject> diseaseTerms = (Set<InterMineObject>) var.getFieldValue("diseaseTerms");
						for (InterMineObject dt : diseaseTerms) {
							String diseaseTitle = (String) dt.getFieldValue("name");
							if (ignoredDiseaseNames.contains(diseaseTitle)) {
								continue;
							}
							if (diseaseInfoMap.get(snpId) == null) {
								diseaseInfoMap.put(snpId, new ArrayList<List<String>>());
							}
							diseaseInfoMap.get(snpId).add(Arrays.asList(diseaseTitle, StringUtils.join(csSet, "; "), numPub));
//							ret.add(Arrays.asList(diseaseTitle, String.format("<a href=\"report.do?id=%s\">%s</a>", snp.getId(), snpId), 
//									fc, String.format("<a href=\"report.do?id=%s\">%s</a>", snp.getId(), maf), StringUtils.join(csSet, "; "), numPub));
						}
					}
				}
				Map<String, Set<String>> diseasePvalueMap = new HashMap<String, Set<String>>();
				for (InterMineObject gwasItem : genomeWideAssociations) {
					Double pvalue = (Double) gwasItem.getFieldValue("pvalue");
					String pvalueString = String.format("<a href=\"report.do?id=%s\">%s</a>", gwasItem.getId().toString(), pvalue.toString());
					Set<InterMineObject> efoTerms = (Set<InterMineObject>) gwasItem.getFieldValue("efoTerms");
					for (InterMineObject efot : efoTerms) {
						String name = (String) efot.getFieldValue("name");
						if (diseasePvalueMap.get(name) == null) {
							diseasePvalueMap.put(name, new HashSet<String>());
						}
						diseasePvalueMap.get(name).add(pvalueString);
					}
				}
				for (String diseaseTitle: diseasePvalueMap.keySet()) {
					if (diseaseInfoMap.get(snpId) == null) {
						diseaseInfoMap.put(snpId, new ArrayList<List<String>>());
					}
					diseaseInfoMap.get(snpId).add(Arrays.asList(diseaseTitle, 
							StringUtils.join(diseasePvalueMap.get(diseaseTitle), ", "),
							String.valueOf(diseasePvalueMap.get(diseaseTitle).size())));
//					ret.add(Arrays.asList(diseaseTitle,
//							String.format("<a href=\"report.do?id=%s\">%s</a>", snp.getId(), snpId),
//							fc, String.format("<a href=\"report.do?id=%s\">%s</a>", snp.getId(), maf), 
//							StringUtils.join(diseasePvalueMap.get(diseaseTitle), ", "),
//							String.valueOf(diseasePvalueMap.get(diseaseTitle).size())));
				}
				
			}
			// process diseases
			Set<InterMineObject> diseases = (Set<InterMineObject>) gene.getFieldValue("diseases");
			for (InterMineObject disease : diseases) {
				InterMineObject dataSet  = (InterMineObject) disease.getFieldValue("dataSet");
				String dataSetName = (String) dataSet.getFieldValue("name");
				if (dataSetName.equals("DisGeNET")) {
					disgenet.add(disease);
				} else {
					others.add(disease);
				}
			}
			
		} catch (IllegalAccessException e) {
//			e.printStackTrace();
			LOG.error(e.getMessage());
		}
		
//		request.setAttribute("geneticDiseaseTable", ret);
		request.setAttribute("disgenet", disgenet);
		request.setAttribute("others", others);
		
		List<String> snpList = new ArrayList<String>(snpInfoMap.keySet());
		Collections.sort(snpList, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return Integer.valueOf(o1.substring(2)).compareTo(Integer.valueOf(o2.substring(2)));
			}
			
		});
		
		request.setAttribute("snpList", snpList);
		request.setAttribute("diseaseInfoMap", diseaseInfoMap);
		request.setAttribute("snpInfoMap", snpInfoMap);
	}
	
	

}
