package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.intermine.metadata.StringUtil;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.util.PropertiesUtil;
import org.intermine.xml.full.FullRenderer;
import org.intermine.xml.full.Item;
import org.intermine.xml.full.ItemFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * 
 * @author chenyian
 *
 */
public class OrganismXomRetriever {
	protected static final Logger LOG = Logger.getLogger(OrganismXomRetriever.class);
	private static final String ESUMMARY_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=taxonomy&retmode=xml&version=2.0&id=";
    
	// number of records to retrieve per request
	private static final int BATCH_SIZE = 500;
	private String osAlias = null;
	private String outputFile = null;
	private List<String> hasShortName;

	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public void setHasShortName(String taxonIds) {
		this.hasShortName = Arrays.asList(taxonIds.split(" "));
		LOG.info("Only the following organisms contain 'shortName': "
				+ StringUtils.join(hasShortName, ","));
	}

	public void execute() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

		if (osAlias == null) {
			throw new BuildException("osAlias attribute is not set");
		}

		Properties properties = PropertiesUtil.getPropertiesStartingWith("ncbi");
		String apiKey = properties.getProperty("ncbi.apikey");

		LOG.info("Starting OrganismXomRetriever...");

		Writer writer = null;

		int i = 0;
		try {
			writer = new FileWriter(outputFile);

			ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);

			Set<String> taxonIds = getTaxonIds();
			
			System.out.println(String.format("%d %s object(s) to be processed.", taxonIds.size(), "Organism"));
			LOG.info(String.format("%d %s object(s) to be processed.", taxonIds.size(), "Organism"));

			ItemFactory itemFactory = new ItemFactory(os.getModel(), "-1_");
			writer.write(FullRenderer.getHeader() + "\n");

			Set<String> identifiers = new HashSet<String>();
			for (Iterator<String> id = taxonIds.iterator(); id.hasNext();) {
				identifiers.add(id.next());
				if (identifiers.size() == BATCH_SIZE || !id.hasNext()) {
					LOG.info("Querying NCBI efetch for " + identifiers.size() + " organisms.");
					Reader reader = null;
					while (reader == null) {
						try {
							reader = getReader(identifiers, apiKey);
						} catch (Exception e) {
							LOG.info(e.getMessage());
							LOG.info("URL: " + ESUMMARY_URL + StringUtil.join(identifiers, ","));
							System.out.println("Error occured when retrieving the data from NCBI. Waiting to retry.");
							Thread.sleep(5000);
							System.out.println("Try retrieving the data from NCBI again.");
						}
					}

					XMLReader xmlreader = XMLReaderFactory.createXMLReader();
					xmlreader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
					Builder parser = new Builder(xmlreader);

					Document doc = parser.build(reader);
					Element entry = doc.getRootElement();

					Elements elements = entry.getFirstChildElement("DocumentSummarySet").getChildElements("DocumentSummary");
		        	

					for (int k = 0; k < elements.size(); k++) {
						Element element = elements.get(k);
						String uid = element.getAttribute("uid").getValue();
						if (element.getChildElements("error").size() > 0) {
							LOG.error("Unable to retrieve organism: " + uid);
						} else {
							Item item = itemFactory.makeItemForClass("Organism");
							item.setAttribute("taxonId", uid);
							if (element.getFirstChildElement("ScientificName") != null) {
								String sciName = element.getFirstChildElement("ScientificName").getValue();
								if (!StringUtils.isEmpty(sciName)) {
									item.setAttribute("name", sciName);
								}
							}
							String genusName = null;
							if (element.getFirstChildElement("Genus") != null) {
								genusName = element.getFirstChildElement("Genus").getValue();
								if (!StringUtils.isEmpty(genusName)) {
									item.setAttribute("genus", genusName);
								}
							}
							String speciesName = null;
							if (element.getFirstChildElement("Species") != null) {
								speciesName = element.getFirstChildElement("Species").getValue();
								if (!StringUtils.isEmpty(speciesName)) {
									item.setAttribute("species", speciesName);
								}
							}
							if (hasShortName.contains(uid)) {
								if (!StringUtils.isEmpty(genusName) && !StringUtils.isEmpty(speciesName)) {
									item.setAttribute("shortName", genusName.charAt(0) + ". "
											+ speciesName);
								}
							}
							Item taxonomy = itemFactory.makeItemForClass("Taxonomy");
							taxonomy.setAttribute("taxonId", uid);
							writer.write(FullRenderer.render(taxonomy));
							
							item.setReference("taxonomy", taxonomy);
							writer.write(FullRenderer.render(item));
							i++;
						}
					}
					
					reader.close();
					identifiers.clear();
				}
			}
			
			// add a generic organism 
			Item item = itemFactory.makeItemForClass("Organism");
			item.setAttribute("taxonId", "0");
			item.setAttribute("name", "not specified");
			writer.write(FullRenderer.render(item));

			writer.write(FullRenderer.getFooter() + "\n");

		} catch (Exception e) {
			throw new BuildException("exception while retrieving organisms", e);
		} finally {
			Thread.currentThread().setContextClassLoader(cl);
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}
		
		System.out.println(String.format("%d organism objects were created.",i));
		LOG.info(String.format("%d organism objects were created.",i));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Set<String> getTaxonIds() throws Exception {
		Query q = new Query();
		QueryClass c = new QueryClass(Class.forName("org.intermine.model.bio.Organism"));
		QueryField f1 = new QueryField(c, "taxonId");
		q.addFrom(c);
		q.addToSelect(f1);
		q.setDistinct(true);

		ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);

		Set<String> ret = new HashSet<String>();
		Iterator iterator = os.execute(q).iterator();
		while (iterator.hasNext()) {
			ResultsRow<Integer> rr = (ResultsRow<Integer>) iterator.next();
			ret.add(rr.get(0).toString());
		}
		return ret;
	}

	private Reader getReader(Set<String> ids, String apiKey) throws Exception {
		String urlString = ESUMMARY_URL + StringUtil.join(ids, ",");
		if (apiKey != null) {
			urlString = urlString + "&api_key=" + apiKey;
		}
		return new BufferedReader(new InputStreamReader(new URL(urlString).openStream(), StandardCharsets.UTF_8));
    }

}
