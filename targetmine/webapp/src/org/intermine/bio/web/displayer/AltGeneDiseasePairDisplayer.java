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

/**
 * An alternative GeneDiseasePair displayer
 * 
 * @author chenyian
 *
 */
public class AltGeneDiseasePairDisplayer extends ReportDisplayer {
	protected static final Logger LOG = Logger.getLogger(AltGeneDiseasePairDisplayer.class);

	public AltGeneDiseasePairDisplayer(ReportDisplayerConfig config, InterMineAPI im) {
		super(config, im);
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	@Override
	public void display(HttpServletRequest request, ReportObject reportObject) {
		InterMineObject gene = (InterMineObject) reportObject.getObject();
		
		Map<String, List<Map<String, String>>> ret = new HashMap<String, List<Map<String,String>>>();
		ret.put("gwas", new ArrayList<Map<String, String>>());
		ret.put("clinvar", new ArrayList<Map<String, String>>());
		ret.put("dbsnpMesh", new ArrayList<Map<String, String>>());
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

				Set<InterMineObject> publications = (Set<InterMineObject>) gdItem.getFieldValue("publications");
				if (publications != null) {
					entryInfo.put("pubCountColumn", String.valueOf(publications.size()));
				} else {
					entryInfo.put("pubCountColumn", "0");
				}
				
				Set<InterMineObject> snps = (Set<InterMineObject>) gdItem.getFieldValue("snps");
				entryInfo.put("snpCountColumn", String.valueOf(snps.size()));

				Set<InterMineObject> gwasItems = (Set<InterMineObject>) gdItem.getFieldValue("gwas");
				Set<InterMineObject> alleles = (Set<InterMineObject>) gdItem.getFieldValue("alleles");
				if (gwasItems != null && !gwasItems.isEmpty()) {
					List<String> list = new ArrayList<String>();
					for (InterMineObject gwas : gwasItems) {
						Double pvalue = (Double) gwas.getFieldValue("pvalue");
						list.add(String.format("<a href=\"report.do?id=%s\">%s</a>", gwas.getId().toString(), pvalue.toString()));
					}
					entryInfo.put("gwasColumn", StringUtils.join(list, ", "));
					ret.get("gwas").add(entryInfo);
				} else if (alleles != null && !alleles.isEmpty()) {
					Set<String> csSet = new HashSet<String>();
					for (InterMineObject allele : alleles) {
						csSet.add((String) allele.getFieldValue("clinicalSignificance"));
					}
					List<String> list = new ArrayList<String>(csSet);
					Collections.sort(list);
					entryInfo.put("clinvarColumn", StringUtils.join(list, ", "));
					ret.get("clinvar").add(entryInfo);
				} else {
					ret.get("dbsnpMesh").add(entryInfo);
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
			LOG.error(e.getMessage());
		}
		
		request.setAttribute("geneticDiseaseList", ret);
		request.setAttribute("numOfAssociations", Integer.valueOf(
				ret.get("gwas").size() + ret.get("clinvar").size() + ret.get("dbsnpMesh").size()));
		request.setAttribute("disgenet", disgenet);
		request.setAttribute("others", others);

	}

}
