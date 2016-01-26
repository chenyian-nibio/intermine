package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.intermine.util.SAXParser;
import org.intermine.xml.full.FullRenderer;
import org.intermine.xml.full.Item;
import org.intermine.xml.full.ItemFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class to fill in genes information from ncbi-esummary
 * 
 * @author chenyian
 */
public class MissingGeneRetriever {
	protected static final Logger LOG = Logger.getLogger(MissingGeneRetriever.class);
	protected static final String ESUMMARY_URL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?tool=flymine&version=1.0&db=gene&id=";
	// number of summaries to retrieve per request
	protected static final int BATCH_SIZE = 200;

	// number of times to try the same bacth from the server
	// private static final int MAX_TRIES = 5;

//	private Set<String> createdGeneIds = new HashSet<String>();

	private String osAlias = null;
	private String outputFile = null;

	private Map<String, Item> organismMap = new HashMap<String, Item>();

//	private static final int RETRY = 10;

	/**
	 * Set the ObjectStore alias.
	 * 
	 * @param osAlias
	 *            The ObjectStore alias
	 */
	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

	/**
	 * Set the output file name
	 * 
	 * @param outputFile
	 *            The output file name
	 */
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	/**
	 * 
	 * @throws BuildException
	 *             if an error occurs
	 */
	public void execute() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

		if (osAlias == null) {
			throw new BuildException("osAlias attribute is not set");
		}
		if (outputFile == null) {
			throw new BuildException("outputFile attribute is not set");
		}

		LOG.info("Starting MissingGeneRetriever");

		Writer writer = null;

		try {
			writer = new FileWriter(outputFile);

			ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);

			List<Gene> genes = getGenes(os);

			System.out.println("There are " + genes.size() + " gene(s) without proper information.");
			LOG.info("There are " + genes.size() + " gene(s) without proper information.");
			System.out.println("There are " + genes.size() + " gene(s) without proper information.");

			Set<String> geneIds = new HashSet<String>();
			Set<Item> toStore = new HashSet<Item>();

			ItemFactory itemFactory = new ItemFactory(os.getModel(), "-1_");
			writer.write(FullRenderer.getHeader() + "\n");
			for (Iterator<Gene> i = genes.iterator(); i.hasNext();) {
				Gene gene = (Gene) i.next();
				geneIds.add(gene.getPrimaryIdentifier());
				if (geneIds.size() == BATCH_SIZE || !i.hasNext()) {
					LOG.info("Querying NCBI esummary for " + geneIds.size() + "genes.");
					SAXParser.parse(new InputSource(getReader(geneIds)), new Handler(toStore,
							itemFactory), false);
					for (Iterator<Item> j = toStore.iterator(); j.hasNext();) {
						Item item = j.next();
						writer.write(FullRenderer.render(item));
					}
					geneIds.clear();
					toStore.clear();
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

	/**
	 * Retrieve the genes to be updated
	 * 
	 * @param os
	 *            the ObjectStore to read from
	 * @return a List of Protein object
	 */
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

	/**
	 * Obtain the esummary information for the genes
	 * 
	 * @param ids
	 *            the gene ids of the genes
	 * @return a Reader for the information
	 * @throws Exception
	 *             if an error occurs
	 */
	protected Reader getReader(Set<String> ids) throws Exception {
		URL url = new URL(ESUMMARY_URL + StringUtil.join(ids, ","));
		return new BufferedReader(new InputStreamReader(url.openStream()));
	}

	/**
	 * Extension of DefaultHandler to handle an esummary for an Gene
	 */
	class Handler extends DefaultHandler {
		Set<Item> toStore;
		Item gene;
		String name;
		StringBuffer characters;
		ItemFactory itemFactory;
		boolean isMerged = false;

		/**
		 * Constructor
		 * 
		 * @param toStore
		 *            a set in which the new Organism items are stored
		 * @param itemFactory
		 *            the factory
		 */
		public Handler(Set<Item> toStore, ItemFactory itemFactory) {
			this.toStore = toStore;
			this.itemFactory = itemFactory;
		}

		/**
		 * {@inheritDoc}
		 */
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if ("ERROR".equals(qName)) {
				name = qName;
			} else if ("Id".equals(qName)) {
				name = "Id";
			} else {
				name = attrs.getValue("Name");
			}
			characters = new StringBuffer();
		}

		/**
		 * {@inheritDoc}
		 */
		public void characters(char[] ch, int start, int length) {
			characters.append(new String(ch, start, length));
		}

		/**
		 * {@inheritDoc}
		 */
		public void endElement(String uri, String localName, String qName) {
			if ("ERROR".equals(name)) {
				LOG.error("Unable to retrieve gene: " + characters);
			} else if ("Id".equals(name)) {
				gene = itemFactory.makeItemForClass("Gene");
				toStore.add(gene);
				gene.setAttribute("primaryIdentifier", characters.toString());
			} else if ("TaxID".equals(name)) {
				if (!StringUtils.isEmpty(characters.toString())) {
//					LOG.info("TaxId: " + characters.toString());
					gene.setReference("organism", getOrganism(characters.toString(), itemFactory));
				}
			} else if ("Name".equals(name)) {
				gene.setAttribute("symbol", characters.toString());
			} else if ("CurrentID".equals(name)) {
				String currentId = characters.toString();
				if (!currentId.equals("0")) {
					gene.setAttribute("briefDescription",
							String.format("This record was replaced with Gene ID: %s", currentId));
				}
			} else if ("OtherDesignations".equals(name)) {
				String desc = characters.toString();
				if (desc != null && !desc.equals("")) {
					gene.setAttribute("description", desc);
				} else {
					gene.setAttribute("description", "-");
				}
			} else if ("Description".equals(name)) {
				String value = characters.toString();
				if (StringUtils.isEmpty(value)) {
					value = "unavailable";
					LOG.info("The name of the gene " + gene.getAttribute("primaryIdentifier").getValue() + " is unavailable.");
				}
				gene.setAttribute("name", value);
			}
			name = null;
		}
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
