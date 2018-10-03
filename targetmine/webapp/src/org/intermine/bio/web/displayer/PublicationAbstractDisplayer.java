package org.intermine.bio.web.displayer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.intermine.api.InterMineAPI;
import org.intermine.model.InterMineObject;
import org.intermine.web.displayer.ReportDisplayer;
import org.intermine.web.logic.config.ReportDisplayerConfig;
import org.intermine.web.logic.results.ReportObject;

public class PublicationAbstractDisplayer extends ReportDisplayer {

	private static final String EFETCH_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&rettype=abstract&retmode=text&id=";

	public PublicationAbstractDisplayer(ReportDisplayerConfig config, InterMineAPI im) {
		super(config, im);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void display(HttpServletRequest request, ReportObject reportObject) {
		InterMineObject imo = (InterMineObject) reportObject.getObject();
		BufferedReader reader = null;
		String pubmedId;
		try {
			pubmedId = (String) imo.getFieldValue("pubMedId");
			if (pubmedId == null) {
				request.setAttribute("abstractContents", "No PubMed identifier found.");
				return;
			}
			
			reader = new BufferedReader(
					new InputStreamReader(new URL(EFETCH_URL + pubmedId).openStream(), StandardCharsets.UTF_8));
			if (reader != null) {
				String line;
				boolean isAuthor = false;
				boolean isAbstract = false;
				StringBuffer sb = new StringBuffer();
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("Author information:")) {
						isAuthor = true;
						continue;
					}
					if (StringUtils.isEmpty(line.trim()) && isAuthor) {
						isAbstract = true;
						isAuthor = false;
						continue;
					}
					if (isAbstract) {
						sb.append(line);
						if (StringUtils.isEmpty(line.trim())) {
							isAbstract = false;
						}
					}
				}  
				String contents = sb.toString();
//				request.setAttribute("abstractContents", "pubmedId: " + pubmedId + 
//						"; url: '" + EFETCH_URL + pubmedId + "'" +
//						"; contents: "+ contents);
				if (StringUtils.isEmpty(contents.trim())) {
					request.setAttribute("abstractContents", "Contents not available.");
				} else {
					request.setAttribute("abstractContents", contents);
				}
			}
		} catch (Exception e) {
			request.setAttribute("abstractContents", "Cannot get the publication abstract." + e.getMessage());
		}
	}
	
}
