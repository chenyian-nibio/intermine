package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
import org.intermine.metadata.ConstraintOp;
import org.intermine.metadata.StringUtil;
import org.intermine.model.bio.Publication;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.query.ConstraintSet;
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

/**
 * 
 * @author chenyian
 *
 */
public class MissingPublicationXomRetriever {
	private static final Logger LOG = Logger.getLogger(MissingPublicationXomRetriever.class);
	// rettype=abstract or just leave it out
	private static final String EFETCH_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&rettype=abstract&retmode=xml&id=";

	// number of records to retrieve per request
	private static final int BATCH_SIZE = 400;
	private String osAlias = null;
	private String outputFile = null;

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

		LOG.info("Starting MissingPublicationXomRetriever...");

		Writer writer = null;

		int i = 0;
		try {
			writer = new FileWriter(outputFile);

			ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);

			Set<String> pubMedIds = getPubMedIds();

			System.out.println("There are " + pubMedIds.size()
					+ " publication(s) without proper information.");
			LOG.info("There are " + pubMedIds.size()
					+ " publication(s) without proper information.");

			ItemFactory itemFactory = new ItemFactory(os.getModel(), "-1_");
			writer.write(FullRenderer.getHeader() + "\n");

			Set<String> identifiers = new HashSet<String>();
			for (Iterator<String> id = pubMedIds.iterator(); id.hasNext();) {
				identifiers.add(id.next());
				if (identifiers.size() == BATCH_SIZE || !id.hasNext()) {
					LOG.info("Querying NCBI efetch for " + identifiers.size() + " publications.");
					System.out.println(
							"Querying NCBI efetch for " + identifiers.size() + " publications.");
					Reader reader = null;
					while (reader == null) {
						try {
							reader = getReader(identifiers, apiKey);
						} catch (Exception e) {
							LOG.info(e.getMessage());
							LOG.info("URL: " + EFETCH_URL + StringUtil.join(identifiers, ","));
							System.out.println(
									"Error occured when retrieving the data from NCBI. Waiting to retry.");
							Thread.sleep(5000);
							System.out.println("Try retrieving the data from NCBI again.");
						}
					}

					XMLReader xmlreader = XMLReaderFactory.createXMLReader();
					xmlreader.setFeature(
							"http://apache.org/xml/features/nonvalidating/load-external-dtd",
							false);
					Builder parser = new Builder(xmlreader);

					Document doc = parser.build(reader);
					Element entry = doc.getRootElement();

					Elements elements = entry.getChildElements("PubmedArticle");

					for (int k = 0; k < elements.size(); k++) {
						Element element = elements.get(k);
						String pubMedId = element.getFirstChildElement("MedlineCitation")
								.getFirstChildElement("PMID").getValue();

						if (!pubMedIds.contains(pubMedId)) {
							continue;
						}

						Item publication = itemFactory.makeItemForClass("Publication");
						publication.setAttribute("pubMedId", pubMedId);
						// System.out.println("pubMedId: " + pubMedId);
						// LOG.info("pubMedId: " + pubMedId);

						Element article = element.getFirstChildElement("MedlineCitation")
								.getFirstChildElement("Article");
						String title = article.getFirstChildElement("ArticleTitle").getValue();
						if (title == null || "".equals(title)) {
							// some rare cases, the title is empty...
							title = "not available";
						}
						publication.setAttribute("title", title);

						if (article.getFirstChildElement("AuthorList") != null) {
							Element firstAuthor = article.getFirstChildElement("AuthorList")
									.getFirstChildElement("Author");
							if (firstAuthor.getFirstChildElement("CollectiveName") != null) {
								publication.setAttribute("firstAuthor", firstAuthor
										.getFirstChildElement("CollectiveName").getValue());
							} else {
								// according to the DTD, this is a must have field, should not be
								// null
								String last = firstAuthor.getFirstChildElement("LastName")
										.getValue();
								if (firstAuthor.getFirstChildElement("Initials") != null) {
									publication.setAttribute("firstAuthor", last + " " + firstAuthor
											.getFirstChildElement("Initials").getValue());
								} else {
									publication.setAttribute("firstAuthor", last);
								}
							}
						}

						if (article.getFirstChildElement("Pagination") != null) {
							if (article.getFirstChildElement("Pagination")
									.getFirstChildElement("MedlinePgn") != null) {
								String pages = article.getFirstChildElement("Pagination")
										.getFirstChildElement("MedlinePgn").getValue();
								if (!StringUtils.isEmpty(pages)) {
									publication.setAttribute("pages", pages);
								}
							} else {
								// TODO could be StartPage ...
								// <!ELEMENT Pagination ((StartPage, EndPage?, MedlinePgn?) |
								// MedlinePgn) >
							}
						}
						Element journal = article.getFirstChildElement("Journal");
						Element pubDate = journal.getFirstChildElement("JournalIssue")
								.getFirstChildElement("PubDate");
						if (pubDate.getFirstChildElement("Year") != null) {
							publication.setAttribute("year",
									pubDate.getFirstChildElement("Year").getValue());
						} else if (pubDate.getFirstChildElement("MedlineDate") != null) {
							String[] medlineDate = pubDate.getFirstChildElement("MedlineDate")
									.getValue().split(" ");
							String year = medlineDate[0];
							// example: 'Fall 2016' (pmid: 28078901)
							if (year.matches("^\\D.+")) {
								year = medlineDate[1];
							}
							// some year strings are ranges, for example: '1998-1999'
							if (year.contains("-")) {
								year = year.substring(0, year.indexOf("-"));
							}
							try {
								Integer.parseInt(year);
								publication.setAttribute("year", year);
							} catch (NumberFormatException e) {
								LOG.info(String.format(
										"Cannot parse year from publication id: %s, value: %s .",
										pubMedId, year));
							}
						}

						Element volume = journal.getFirstChildElement("JournalIssue")
								.getFirstChildElement("Volume");
						if (volume != null) {
							publication.setAttribute("volume", volume.getValue());
						}
						Element issue = journal.getFirstChildElement("JournalIssue")
								.getFirstChildElement("Issue");
						if (issue != null) {
							publication.setAttribute("issue", issue.getValue());
						}
						// use ISOAbbreviation instead of Title
						Element journalAbbr = journal.getFirstChildElement("ISOAbbreviation");
						if (journalAbbr != null) {
							publication.setAttribute("journal", journalAbbr.getValue());
						}
						writer.write(FullRenderer.render(publication));
						i++;
					}

					reader.close();
					identifiers.clear();
				}
			}

			writer.write(FullRenderer.getFooter() + "\n");

		} catch (Exception e) {
			throw new BuildException("exception while retrieving publications", e);
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

		System.out.println(String.format("%d publication objects were created.", i));
		LOG.info(String.format("%d publication objects were created.", i));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Set<String> getPubMedIds() throws Exception {
		Query q = new Query();
		QueryClass qc = new QueryClass(Publication.class);
		q.addFrom(qc);
		q.addToSelect(qc);

		// only retrieve those all 3 following columns are missing
		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		SimpleConstraint scTitle = new SimpleConstraint(new QueryField(qc, "title"),
				ConstraintOp.IS_NULL);
		cs.addConstraint(scTitle);

		SimpleConstraint scYear = new SimpleConstraint(new QueryField(qc, "year"),
				ConstraintOp.IS_NULL);
		cs.addConstraint(scYear);

		SimpleConstraint scFirstAuthor = new SimpleConstraint(new QueryField(qc, "firstAuthor"),
				ConstraintOp.IS_NULL);
		cs.addConstraint(scFirstAuthor);

		q.setConstraint(cs);

		ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);

		List<Publication> publications = ((List) os.executeSingleton(q));
		Iterator<Publication> iterator = publications.iterator();
		Set<String> pubmedIds = new HashSet<String>();
		while (iterator.hasNext()) {
			Publication publication = iterator.next();
			pubmedIds.add(publication.getPubMedId());
		}

		return pubmedIds;
	}

	private Reader getReader(Set<String> ids, String apiKey) throws Exception {
		String urlString = EFETCH_URL + StringUtil.join(ids, ",");
		if (apiKey != null) {
			urlString = urlString + "&api_key=" + apiKey;
		}
		return new BufferedReader(
				new InputStreamReader(new URL(urlString).openStream(), StandardCharsets.UTF_8));
	}

}
