package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.intermine.metadata.ConstraintOp;
import org.intermine.metadata.StringUtil;
import org.intermine.model.bio.Gene;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.SimpleConstraint;
import org.intermine.util.PropertiesUtil;
import org.intermine.xml.full.FullRenderer;
import org.intermine.xml.full.Item;
import org.intermine.xml.full.ItemFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

/**
 * 
 * @author chenyian
 *
 */
public class MissingGeneXomRetriever {
	private static final Logger LOG = Logger.getLogger(MissingGeneXomRetriever.class);
	
    private static final String ESUMMARY_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=gene&id=";

	private static final int BATCH_SIZE = 200;
	
	private String osAlias = null;
	private String outputFile = null;

	private Map<String, Item> organismMap = new HashMap<String, Item>();
	
	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public void execute() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

		if (osAlias == null) {
			throw new BuildException("osAlias attribute is not set");
		}
		
		Properties properties = PropertiesUtil.getPropertiesStartingWith("ncbi");
		String apiKey = properties.getProperty("ncbi.apikey");

		LOG.info("Starting MissingGeneRetriever");

		Writer writer = null;

		try {
			writer = new FileWriter(outputFile);

			ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);

			List<Gene> genes = getGenes(os);

			System.out.println("There are " + genes.size() + " gene(s) without proper information.");
			LOG.info("There are " + genes.size() + " gene(s) without proper information.");

			Set<String> geneIds = new HashSet<String>();

			ItemFactory itemFactory = new ItemFactory(os.getModel(), "-1_");
			writer.write(FullRenderer.getHeader() + "\n");

			for (Iterator<Gene> i = genes.iterator(); i.hasNext();) {
				Gene gene = (Gene) i.next();
				geneIds.add(gene.getPrimaryIdentifier());
				if (geneIds.size() == BATCH_SIZE || !i.hasNext()) {
					LOG.info("Querying NCBI esummary for " + geneIds.size() + " genes.");
					System.out.println("Querying NCBI esummary for " + geneIds.size() + " genes.");
					Reader reader = null;
					while (reader == null) {
						try {
							reader = getReader(geneIds, apiKey);
						} catch (Exception e) {
							LOG.info(e.getMessage());
							LOG.info("URL: " + ESUMMARY_URL + StringUtil.join(geneIds, ","));
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

					Elements elements = entry.getChildElements("DocumentSummarySet").get(0).getChildElements("DocumentSummary");

					for (int k = 0; k < elements.size(); k++) {
						Element element = elements.get(k);
						String uid = element.getAttribute("uid").getValue();
						if (element.getChildElements("error").size() > 0) {
							LOG.error("Unable to retrieve gene: " + uid);
						} else {
							Set<String> geneSynonyms = new HashSet<String>();
							Item geneItem = itemFactory.makeItemForClass("Gene");
							geneItem.setAttribute("primaryIdentifier", uid);
							
							String symbol = element.getChildElements("NomenclatureSymbol").get(0).getValue();
							if (StringUtils.isEmpty(symbol)) {
								symbol = element.getChildElements("Name").get(0).getValue();
							}
							geneItem.setAttribute("symbol", symbol);
							geneSynonyms.add(symbol);
							
							String taxonId = element.getChildElements("Organism").get(0).getChildElements("TaxID").get(0).getValue();
							geneItem.setReference("organism", getOrganism(taxonId, itemFactory));

//							String status = element.getChildElements("Status").get(0).getValue();
							String currentId = element.getChildElements("CurrentID").get(0).getValue();
							if (!currentId.equals("0")) {
								geneItem.setAttribute("briefDescription", String.format("This record was replaced with Gene ID: %s", currentId));
							}
							
							String name = element.getChildElements("NomenclatureName").get(0).getValue();
							if (StringUtils.isEmpty(name)) {
								// the 'description' attribute is more like a name?
								name = element.getChildElements("Description").get(0).getValue();
								if (StringUtils.isEmpty(name)) {
									name = "unavailable";
								}
							}
							geneItem.setAttribute("name", name);
							
							String otherDesignations = element.getChildElements("OtherDesignations").get(0).getValue();
							if (!StringUtils.isEmpty(otherDesignations)) {
								geneSynonyms.addAll(Arrays.asList(otherDesignations.split("\\|")));
							}
							String otherAliases = element.getChildElements("OtherAliases").get(0).getValue();
							if (!StringUtils.isEmpty(otherAliases)) {
								geneSynonyms.addAll(Arrays.asList(otherAliases.split(", ")));
							}

							String summary = element.getChildElements("Summary").get(0).getValue();
							if (!StringUtils.isEmpty(summary)) {
								// store 'summary' attribute in the 'description' field
								geneItem.setAttribute("description", summary);
							}

							writer.write(FullRenderer.render(geneItem));
							
							for (String alias : geneSynonyms) {
								Item item = itemFactory.makeItemForClass("Synonym");
								item.setReference("subject", geneItem.getIdentifier());
								item.setAttribute("value", alias);
								writer.write(FullRenderer.render(item));
							}

						}
					}
					
					reader.close();
					
					geneIds.clear();
				}
			}
			
			// save the Organism object
			for (Iterator<Item> iter = organismMap.values().iterator(); iter.hasNext();) {
				Item item = iter.next();
				writer.write(FullRenderer.render(item));
			}

			writer.write(FullRenderer.getFooter() + "\n");

		} catch (Exception e) {
			throw new BuildException("exception while retrieving genes", e);
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
	}

	protected List<Gene> getGenes(ObjectStore os) {
		Query q = new Query();
		QueryClass qc = new QueryClass(Gene.class);
		q.addFrom(qc);
		q.addToSelect(qc);

		SimpleConstraint sc = new SimpleConstraint(new QueryField(qc, "name"),
				ConstraintOp.IS_NULL);

		q.setConstraint(sc);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Gene> ret = (List<Gene>) ((List) os.executeSingleton(q));

		return ret;
	}

	protected Reader getReader(Set<String> ids, String apiKey) throws Exception {
		String urlString = ESUMMARY_URL + StringUtil.join(ids, ",");
		if (apiKey != null) {
			urlString = urlString + "&api_key=" + apiKey;
		}
		URL url = new URL(urlString);
		return new BufferedReader(new InputStreamReader(url.openStream()));
	}

	private Item getOrganism(String taxonId, ItemFactory itemFactory) {
		Item ret = organismMap.get(taxonId);
		if (ret == null) {
			ret = itemFactory.makeItemForClass("Organism");
			ret.setAttribute("taxonId", taxonId);
			organismMap.put(taxonId, ret);
		}
		return ret;
	}

}
