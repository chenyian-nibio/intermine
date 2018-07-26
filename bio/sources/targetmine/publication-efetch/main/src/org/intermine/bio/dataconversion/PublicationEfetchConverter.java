package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.model.bio.Publication;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.xml.full.Item;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

/**
 * import publication information from pre-retrieved pubmed xml file. <br/>
 * https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?tool=flymine&db=pubmed&rettype=abstract&retmode=xml&id=
 * 
 * @author chenyian
 */
public class PublicationEfetchConverter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(PublicationEfetchConverter.class);
	//
	private static final String DATASET_TITLE = "PubMed";
	private static final String DATA_SOURCE_NAME = "PubMed";

	private String osAlias;

	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

	private Set<String> pubMedIds;

	private Set<String> processedIds = new HashSet<String>();

	/**
	 * Constructor
	 *
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public PublicationEfetchConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 *
	 *
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		if (pubMedIds == null || pubMedIds.isEmpty()) {
			pubMedIds = getPubMedIds();
		}

		String fileName = getCurrentFile().getName();
		LOG.info(String.format("Processing the file %s ...", fileName));

		int i = 0;
		int j = 0;
		BufferedReader in = null;
		try {
			in = new BufferedReader(reader);
			String line;
			List<String> stringList = new ArrayList<String>();
			while ((line = in.readLine()) != null) {
				if (line.equals("///")) {
					Builder parser = new Builder();
					String string = StringUtils.join(stringList.subList(2, stringList.size()),
							"\n");
					// replace the problematic kapa (\ud835\udf05) with k; not a good solution ...
					if (string.contains("\ud835\udf05")) {
						string = string.replaceAll("\ud835\udf05", "k");
					}
					Document doc = parser.build(new StringReader(string));
					Element entry = doc.getRootElement();

					Elements elements = entry.getChildElements("PubmedArticle");

					for (int k = 0; k < elements.size(); k++) {
						Element element = elements.get(k);
						String pubMedId = element.getFirstChildElement("MedlineCitation")
								.getFirstChildElement("PMID").getValue();

						if (!pubMedIds.contains(pubMedId) || processedIds.contains(pubMedId)) {
							continue;
						}

						Item publication = createItem("Publication");
						publication.setAttribute("pubMedId", pubMedId);

						Element article = element.getFirstChildElement("MedlineCitation")
								.getFirstChildElement("Article");
						String title = article.getFirstChildElement("ArticleTitle").getValue();
						if (title == null || title.equals("")) {
							title = "(not available)";
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
						store(publication);
						processedIds.add(pubMedId);
						i++;
					}

					Elements pubmedBookArticles = entry.getChildElements("PubmedBookArticle");
					try {

						for (int k = 0; k < pubmedBookArticles.size(); k++) {
							Element element = pubmedBookArticles.get(k);
							Element bookDoc = element.getFirstChildElement("BookDocument");
							String pubMedId = bookDoc.getFirstChildElement("PMID").getValue();

							if (!pubMedIds.contains(pubMedId) || processedIds.contains(pubMedId)) {
								continue;
							}

							Item publication = createItem("Publication");
							publication.setAttribute("pubMedId", pubMedId);

							Element book = bookDoc.getFirstChildElement("Book");

							String title = book.getFirstChildElement("BookTitle").getValue();
							if (title == null || title.equals("")) {
								title = "(not available)";
							}
							publication.setAttribute("title", title);

							if (book.getFirstChildElement("AuthorList") != null) {
								Element firstAuthor = book.getFirstChildElement("AuthorList")
										.getFirstChildElement("Author");
								if (firstAuthor.getFirstChildElement("CollectiveName") != null) {
									publication.setAttribute("firstAuthor", firstAuthor
											.getFirstChildElement("CollectiveName").getValue());
								} else {
									// according to the DTD, this is a must have field, should not
									// be null
									String last = firstAuthor.getFirstChildElement("LastName")
											.getValue();
									if (firstAuthor.getFirstChildElement("Initials") != null) {
										publication.setAttribute("firstAuthor",
												last + " "
														+ firstAuthor
																.getFirstChildElement("Initials")
																.getValue());
									} else {
										publication.setAttribute("firstAuthor", last);
									}
								}
							}

							Element pubDate = book.getFirstChildElement("PubDate");
							if (pubDate.getFirstChildElement("Year") != null) {
								publication.setAttribute("year",
										pubDate.getFirstChildElement("Year").getValue());
							}

							publication.setAttribute("journal", "Book");
							store(publication);
							processedIds.add(pubMedId);
							j++;
						}
					} catch (Exception e) {
						LOG.info(string);
						throw new RuntimeException(e);
					}

					stringList.clear();
				} else {
					stringList.add(line);
				}
			}
		} catch (IOException e) {
			LOG.error(e);
		} finally {
			if (in != null)
				in.close();
		}

		System.out.println(String.format("%d publication objects were created.", i + j));
		System.out.println(String.format("%d pubmed articles and %d book articles.", i, j));
		LOG.info(String.format("%d publication objects were created.", i + j));
		LOG.info(String.format("%d pubmed articles and %d book articles.", i, j));
	}

	/**
	 * Retrieve the publications to be updated
	 * 
	 * @return a List of PubMed IDs
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Set<String> getPubMedIds() throws Exception {
		Query q = new Query();
		QueryClass qc = new QueryClass(Publication.class);
		q.addFrom(qc);
		q.addToSelect(qc);

		ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);

		List<Publication> publications = (List<Publication>) ((List) os.executeSingleton(q));
		Iterator<Publication> iterator = publications.iterator();
		Set<String> pubmedIds = new HashSet<String>();
		while (iterator.hasNext()) {
			Publication publication = iterator.next();
			pubmedIds.add(publication.getPubMedId());
		}

		return pubmedIds;
	}

}
