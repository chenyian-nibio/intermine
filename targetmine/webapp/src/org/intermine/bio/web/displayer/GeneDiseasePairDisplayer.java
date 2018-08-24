package org.intermine.bio.web.displayer;

import java.util.ArrayList;
import java.util.Collections;
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

public class GeneDiseasePairDisplayer extends ReportDisplayer {
	protected static final Logger LOG = Logger.getLogger(GeneDiseasePairDisplayer.class);

	public GeneDiseasePairDisplayer(ReportDisplayerConfig config, InterMineAPI im) {
		super(config, im);
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	@Override
	public void display(HttpServletRequest request, ReportObject reportObject) {
		InterMineObject gene = (InterMineObject) reportObject.getObject();
		
		List<Map<String, String>> ret = new ArrayList<Map<String,String>>();
		List<InterMineObject> disgenet = new ArrayList<InterMineObject>();
		List<InterMineObject> others = new ArrayList<InterMineObject>(); // so far only OMIM?
		
		try {
			String geneSymbol = (String) gene.getFieldValue("symbol");
			
			Set<InterMineObject> geneDiseasePairs = (Set<InterMineObject>) gene.getFieldValue("geneDiseasePairs");
			for (InterMineObject gdItem : geneDiseasePairs) {
				Map<String,String> entryInfo = new HashMap<String, String>();
				InterMineObject diseaseTerm = (InterMineObject) gdItem.getFieldValue("diseaseTerm");
				String name = (String) diseaseTerm.getFieldValue("name");
				entryInfo.put("diseaseColumn", String.format("<a href=\"report.do?id=%s\">%s - %s</a>", gdItem.getId().toString(), name, geneSymbol));
				
				Set<InterMineObject> gwasItems = (Set<InterMineObject>) gdItem.getFieldValue("gwas");
				if (gwasItems != null) {
					List<String> list = new ArrayList<String>();
					for (InterMineObject gwas : gwasItems) {
						Double pvalue = (Double) gwas.getFieldValue("pvalue");
						list.add(String.format("<a href=\"report.do?id=%s\">%s</a>", gwas.getId().toString(), pvalue.toString()));
					}
					entryInfo.put("gwasColumn", StringUtils.join(list, ", "));
				}
				Set<InterMineObject> alleles = (Set<InterMineObject>) gdItem.getFieldValue("alleles");
				if (alleles != null) {
//					List<String> list = new ArrayList<String>();
//					for (InterMineObject allele : alleles) {
//						String cs = (String) allele.getFieldValue("clinicalSignificance");
//						list.add(String.format("<a href=\"report.do?id=%s\">%s</a>", allele.getId().toString(), cs));
//						if (list.size() > 9) {
//							list.add("...");
//							break;
//						}
//					}
					Set<String> csSet = new HashSet<String>();
					for (InterMineObject allele : alleles) {
						csSet.add((String) allele.getFieldValue("clinicalSignificance"));
					}
					List<String> list = new ArrayList<String>(csSet);
					Collections.sort(list);
					entryInfo.put("clinvarColumn", StringUtils.join(list, ", "));
				}
				
				if (StringUtils.isEmpty(entryInfo.get("gwasColumn"))
						&& StringUtils.isEmpty(entryInfo.get("clinvarColumn"))) {
					entryInfo.put("diseaseMeshColumn", "O");
				} else {
					entryInfo.put("diseaseMeshColumn", "-");
				}
				
				Set<InterMineObject> publications = (Set<InterMineObject>) gdItem.getFieldValue("publications");
				if (publications != null) {
					entryInfo.put("pubCountColumn", String.valueOf(publications.size()));
				} else {
					entryInfo.put("pubCountColumn", "0");
				}
				
				Set<InterMineObject> snps = (Set<InterMineObject>) gdItem.getFieldValue("snps");
//				Set<String> pubmedIdSet = new HashSet<String>();
//				for (InterMineObject snp : snps) {
//					Set<InterMineObject> snpPubs = (Set<InterMineObject>) snp.getFieldValue("publications");
//					for (InterMineObject snpPub : snpPubs) {
//						String pubMedId = (String) snpPub.getFieldValue("pubMedId");
//						pubmedIdSet.add(pubMedId);
//					}
//				}
//				entryInfo.put("snpPubCountColumn", String.valueOf(pubmedIdSet.size()));
				entryInfo.put("snpCountColumn", String.valueOf(snps.size()));
				

				ret.add(entryInfo);
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
			LOG.error(e.getMessage());
		}
		
		request.setAttribute("geneticDiseaseList", ret);
		request.setAttribute("disgenet", disgenet);
		request.setAttribute("others", others);

	}

}
