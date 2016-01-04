package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.metadata.StringUtil;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;
import org.intermine.xml.full.ReferenceList;

/**
 * NOTE: modified from InterMine go-annotation source <br/>
 * DataConverter to parse a go annotation file into Items. *
 * 
 * @author Andrew Varley
 * @author Peter Mclaren - some additions to record the parents of a go term.
 * @author Julie Sullivan - updated to handle GAF 2.0
 * @author Xavier Watkins - refactored model
 * @author chenyian - add GoSlim
 */
public class GoGoslimConverter extends BioFileConverter {
	// protected static final String PROP_FILE = "go-annotation_config.properties";

	// configuration maps
	// private Map<String, Config> configs = new HashMap<String, Config>();
	private static final Map<String, String> WITH_TYPES = new LinkedHashMap<String, String>();

	// maps retained across all files
	protected Map<String, String> goTerms = new LinkedHashMap<String, String>();
	private Map<String, String> evidenceCodes = new LinkedHashMap<String, String>();
	private Map<String, String> publications = new LinkedHashMap<String, String>();
	// private Map<String, Item> organisms = new LinkedHashMap<String, Item>();
	protected Map<String, String> bioentityMap = new LinkedHashMap<String, String>();

	// maps renewed for each file
	private Map<GoTermToGene, Set<Evidence>> goTermGeneToEvidence = new LinkedHashMap<GoTermToGene, Set<Evidence>>();
	private Map<Integer, List<String>> productCollectionsMap;
	private Map<String, Integer> storedProductIds;

	// These should be altered for different ontologies:
	protected String termClassName = "GOTerm";
	protected String termCollectionName = "goAnnotation";
	protected String annotationClassName = "GOAnnotation";
	private static final String ANNOTATION_TYPE = "Protein";
	private static final String IDENTIFIER_FIELD = "primaryAccession";

	private static final Logger LOG = Logger.getLogger(GoGoslimConverter.class);

	// chenyian
	private static final String DATASET_TITLE = "UniProt-GOA";
	private static final String DATA_SOURCE_NAME = "UniProt";
	private boolean excludeComputational = false;

	private Map<String, Set<String>> goGoslimMap = new HashMap<String, Set<String>>();
	private Map<String, String> goSlimTerms = new HashMap<String, String>();
	private Map<String, String> ontologies = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 * @throws Exception
	 *             if an error occurs in storing or finding Model
	 */
	public GoGoslimConverter(ItemWriter writer, Model model) throws Exception {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
		productCollectionsMap = new LinkedHashMap<Integer, List<String>>();
		storedProductIds = new HashMap<String, Integer>();
	}

	// TODO chenyian: testing...
	static {
		WITH_TYPES.put("UniProtKB", "Protein");
		WITH_TYPES.put("InterPro", "ProteinDomain");
		WITH_TYPES.put("EC", "Enzyme");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(Reader reader) throws ObjectStoreException, IOException {
		if (goGoslimMap.isEmpty()) {
			readGoGoslimMap();
		}

		initialiseMapsForFile();

		BufferedReader br = new BufferedReader(reader);
		String line = null;

		// loop through entire file
		while ((line = br.readLine()) != null) {
			if (line.startsWith("!")) {
				continue;
			}
			String[] array = line.split("\t", -1); // keep trailing empty Strings

			if (!array[0].trim().equals("UniProtKB")) {
				continue;
			}

			if (array.length < 13) {
				throw new IllegalArgumentException("Not enough elements (should be > 13 not "
						+ array.length + ") in line: " + line);
			}

			// String taxonId = parseTaxonId(array[12]);

			String productId = array[1];

			String goId = array[4];
			String qualifier = array[3];
			String strEvidence = array[6];
			String withText = array[7];
			String annotationExtension = null;
			if (array.length >= 16) {
				annotationExtension = array[15];
			}
			if (StringUtils.isNotEmpty(strEvidence)) {
				// chenyian: skip IEA annotation
				if (excludeComputational && strEvidence.equals("IEA")) {
					continue;
				}
				storeEvidenceCode(strEvidence);
			} else {
				throw new IllegalArgumentException("Evidence is a required column but not "
						+ "found for goterm " + goId + " and productId " + productId);
			}

			String type = ANNOTATION_TYPE;

			// create unique key for go annotation
			GoTermToGene key = new GoTermToGene(productId, goId, qualifier);

			String dataSourceCode = array[14]; // e.g. GDB, where uniprot collect the data from
			String dataSource = array[0]; // e.g. UniProtKB, where the goa file comes from
			String productIdentifier = getBioEntity(productId, type, IDENTIFIER_FIELD);

			// null if resolver could not resolve an identifier
			if (productIdentifier != null) {

				// null if no pub found
				String pubRefId = newPublication(array[5]);

				// get evidence codes for this goterm|gene pair
				Set<Evidence> allEvidenceForAnnotation = goTermGeneToEvidence.get(key);

				// new evidence
				if (allEvidenceForAnnotation == null || !StringUtils.isEmpty(withText)) {
					Evidence evidence = new Evidence(strEvidence, pubRefId, withText, dataSource,
							dataSourceCode);
					allEvidenceForAnnotation = new LinkedHashSet<Evidence>();
					allEvidenceForAnnotation.add(evidence);
					goTermGeneToEvidence.put(key, allEvidenceForAnnotation);
					// chenyian
					Integer storedAnnotationId = createGoAnnotation(productIdentifier, type, goId,
							qualifier, dataSource, dataSourceCode, annotationExtension);
					evidence.setStoredAnnotationId(storedAnnotationId);
				} else {
					boolean seenEvidenceCode = false;
					Integer storedAnnotationId = null;

					for (Evidence evidence : allEvidenceForAnnotation) {
						String evidenceCode = evidence.getEvidenceCode();
						storedAnnotationId = evidence.storedAnnotationId;
						// already have evidence code, just add pub
						if (evidenceCode.equals(strEvidence)) {
							evidence.addPublicationRefId(pubRefId);
							seenEvidenceCode = true;
						}
					}
					if (!seenEvidenceCode) {
						Evidence evidence = new Evidence(strEvidence, pubRefId, withText,
								dataSource, dataSourceCode);
						evidence.storedAnnotationId = storedAnnotationId;
						allEvidenceForAnnotation.add(evidence);
					}
				}
			}
		}
		storeProductCollections();
		storeEvidence();
	}

	// chenyian: read go-goslim mapping
	private File goslimMapFile;

	public void setGoslimMap(File goslimMap) {
		this.goslimMapFile = goslimMap;
	}

	private void readGoGoslimMap() {

		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(goslimMapFile));
			String line;
			while ((line = in.readLine()) != null) {
				String[] cols = line.split("=>");
				String goId = cols[0].trim();
				String mapping = cols[1].trim();
				if (mapping.equals("//")) {
					continue;
				}
				String[] part = mapping.split(" // ");
				HashSet<String> goslimIds = new HashSet<String>();
				// use only pertinent GOSlim mapping?
				goslimIds.addAll(Arrays.asList(part[0].trim().split(" ")));
				goGoslimMap.put(goId, goslimIds);
			}
			in.close();
		} catch (FileNotFoundException e) {
			LOG.error(e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	private String getGoSlimTerm(String identifier) throws ObjectStoreException {
		String ret = goSlimTerms.get(identifier);
		if (ret == null) {
			Item item = createItem("GOSlimTerm");
			item.setAttribute("identifier", identifier);
			item.setReference("ontology", getOntology("GOSlim"));
			store(item);

			ret = item.getIdentifier();
			goSlimTerms.put(identifier, ret);
		}
		return ret;
	}

	private String getOntology(String name) throws ObjectStoreException {
		String ret = ontologies.get(name);
		if (ret == null) {
			Item item = createItem("Ontology");
			item.setAttribute("name", name);
			store(item);

			ret = item.getIdentifier();
			ontologies.put(name, ret);
		}
		return ret;
	}

	/**
	 * Reset maps that don't need to retain their contents between files.
	 */
	protected void initialiseMapsForFile() {
		goTermGeneToEvidence = new LinkedHashMap<GoTermToGene, Set<Evidence>>();
	}

	private void storeProductCollections() throws ObjectStoreException {
		System.out.println("goTermGeneToEvidence.size = " + goTermGeneToEvidence.size());
		System.out.println("productCollectionsMap.size = " + productCollectionsMap.size());
		System.out.println("storedProductIds.size = " + storedProductIds.size());
		for (Map.Entry<Integer, List<String>> entry : productCollectionsMap.entrySet()) {
			Integer storedProductId = entry.getKey();
			List<String> annotationIds = entry.getValue();
			ReferenceList goAnnotation = new ReferenceList(termCollectionName, annotationIds);
			store(goAnnotation, storedProductId);
		}
	}

	private void storeEvidence() throws ObjectStoreException {
		for (Set<Evidence> annotationEvidence : goTermGeneToEvidence.values()) {
			List<String> evidenceRefIds = new ArrayList<String>();
			Integer goAnnotationRefId = null;
			for (Evidence evidence : annotationEvidence) {
				Item goevidence = createItem("GOEvidence");
				goevidence.setReference("code", evidenceCodes.get(evidence.getEvidenceCode()));
				List<String> publicationEvidence = evidence.getPublications();
				if (!publicationEvidence.isEmpty()) {
					goevidence.setCollection("publications", publicationEvidence);
				}

				// with objects
				if (!StringUtils.isEmpty(evidence.withText)) {
					goevidence.setAttribute("withText", evidence.withText);
					List<String> with = createWithObjects(evidence.withText, evidence.dataSource,
							evidence.dataSourceCode);
					if (!with.isEmpty()) {
						goevidence.addCollection(new ReferenceList("with", with));
					}
				}

				store(goevidence);
				evidenceRefIds.add(goevidence.getIdentifier());
				goAnnotationRefId = evidence.getStoredAnnotationId();
			}

			ReferenceList refIds = new ReferenceList("evidence", new ArrayList<String>(
					evidenceRefIds));
			store(refIds, goAnnotationRefId);
		}
	}

	private Integer createGoAnnotation(String productIdentifier, String productType, String goId,
			String qualifier, String dataSource, String dataSourceCode, String annotationExtension)
			throws ObjectStoreException {
		Item goAnnotation = createItem(annotationClassName);
		goAnnotation.setReference("subject", productIdentifier);
		goAnnotation.setReference("ontologyTerm", getGoTerm(goId));

		if (!StringUtils.isEmpty(qualifier)) {
			goAnnotation.setAttribute("qualifier", qualifier);
		}
		if (!StringUtils.isEmpty(annotationExtension)) {
			goAnnotation.setAttribute("annotationExtension", annotationExtension);
		}
		Set<String> goslimIds = goGoslimMap.get(goId);
		if (goslimIds != null) {
			for (String goslimId : goslimIds) {
				goAnnotation.addToCollection("goSlimTerms", getGoSlimTerm(goslimId));
			}
		}

		addProductCollection(productIdentifier, goAnnotation.getIdentifier());

		Integer storedAnnotationId = store(goAnnotation);
		return storedAnnotationId;
	}

	private void addProductCollection(String productRefId, String goAnnotationRefId) {
		Integer storedProductId = storedProductIds.get(productRefId);
		List<String> annotationIds = productCollectionsMap.get(storedProductId);
		if (annotationIds == null) {
			annotationIds = new ArrayList<String>();
			productCollectionsMap.put(storedProductId, annotationIds);
		}
		annotationIds.add(goAnnotationRefId);
	}

	/**
	 * Given the 'with' text from a gene_association entry parse for recognised identifier types and
	 * create Gene or Protein items accordingly.
	 * 
	 * @param withText
	 *            string from the gene_association entry
	 * @param organism
	 *            organism to reference
	 * @param dataSource
	 *            the name of goa file source
	 * @param dataSourceCode
	 *            short code to describe data source
	 * @throws ObjectStoreException
	 *             if problem when storing
	 * @return a list of Items
	 */
	protected List<String> createWithObjects(String withText, String dataSource,
			String dataSourceCode) throws ObjectStoreException {

		List<String> withProductList = new ArrayList<String>();
		try {
			String[] elements = withText.split("[; |,]");
			for (int i = 0; i < elements.length; i++) {
				String entry = elements[i].trim();
				// rely on the format being type:identifier
				if (entry.indexOf(':') > 0) {
					String prefix = entry.substring(0, entry.indexOf(':'));
					String value = entry.substring(entry.indexOf(':') + 1);

					if (WITH_TYPES.containsKey(prefix) && StringUtils.isNotEmpty(value)) {
						String className = WITH_TYPES.get(prefix);
						String productIdentifier;
						if ("Protein".equals(className)) {
							if (value.contains("-")) {
								value = value.substring(0, value.indexOf("-"));
							}
							if (value.contains(":")) {
								value = value.substring(0, value.indexOf(":"));
							}
							productIdentifier = getBioEntity(value, className, "primaryAccession");
						} else {
							// Enzyme and ProteinDomain don't associate with organisms
							productIdentifier = getBioEntity(value, className, "primaryIdentifier");
						}
						if (productIdentifier != null) {
							withProductList.add(productIdentifier);
						}
					} else {
						LOG.debug("createWithObjects skipping a withType prefix:" + prefix);
					}
				}
			}
		} catch (RuntimeException e) {
			LOG.error("createWithObjects broke with: " + withText);
			throw e;
		}
		return withProductList;
	}

	private String getBioEntity(String identifier, String type, String field)
			throws ObjectStoreException {
		// chenyian: so far there are only 3 types:
		// Protein, ProteinDomain and Enzyme which have distinct identifier even cross-species
		String ret = bioentityMap.get(identifier);

		if (ret == null) {
			Item item = createItem(type);
			item.setAttribute(field, identifier);

			Integer storedProductId = store(item);
			ret = item.getIdentifier();
			storedProductIds.put(ret, storedProductId);
			bioentityMap.put(identifier, ret);
		}
		return ret;
	}

	private String getGoTerm(String identifier) throws ObjectStoreException {
		String ret = goTerms.get(identifier);
		if (ret == null) {
			Item item = createItem(termClassName);
			item.setAttribute("identifier", identifier);
			item.setReference("ontology", getOntology("GO"));
			store(item);

			ret = item.getIdentifier();
			goTerms.put(identifier, ret);
		}
		return ret;
	}

	private void storeEvidenceCode(String code) throws ObjectStoreException {
		if (evidenceCodes.get(code) == null) {
			Item item = createItem("GOEvidenceCode");
			item.setAttribute("code", code);
			evidenceCodes.put(code, item.getIdentifier());
			store(item);
		}
	}

	private String newPublication(String codes) throws ObjectStoreException {
		String pubRefId = null;
		Item item = null;
		// possible types: DOI, GO_REF, PMID, Reactome (2014/10/22)
		if (codes.startsWith("PMID:")) {
			String pubMedId = codes.substring(5);
			if (StringUtil.allDigits(pubMedId)) {
				pubRefId = publications.get(pubMedId);
				if (pubRefId == null) {
					item = createItem("Publication");
					item.setAttribute("pubMedId", pubMedId);
					store(item);
					pubRefId = item.getIdentifier();
					publications.put(pubMedId, pubRefId);
				}
			}
		}
		return pubRefId;
	}

	// private Item newOrganism(String taxonId) throws ObjectStoreException {
	// Item item = organisms.get(taxonId);
	// if (item == null) {
	// item = createItem("Organism");
	// item.setAttribute("taxonId", taxonId);
	// organisms.put(taxonId, item);
	// store(item);
	// }
	// return item;
	// }

	// private String parseTaxonId(String input) {
	// if ("taxon:".equals(input)) {
	// throw new IllegalArgumentException("Invalid taxon id read: " + input);
	// }
	// String taxonId = input.split(":")[1];
	// if (taxonId.contains("|")) {
	// taxonId = taxonId.split("\\|")[0];
	// }
	// return taxonId;
	// }

	private class Evidence {
		private List<String> publicationRefIds = new ArrayList<String>();
		private String evidenceCode = null;
		private Integer storedAnnotationId = null;
		private String withText = null;
		private String dataSourceCode = null;
		private String dataSource = null;

		// dataSource, dataSourceCode

		protected Evidence(String evidenceCode, String publicationRefId, String withText,
				String dataset, String datasource) {
			this.evidenceCode = evidenceCode;
			this.withText = withText;
			this.dataSourceCode = dataset;
			this.dataSource = datasource;
			addPublicationRefId(publicationRefId);
		}

		protected void addPublicationRefId(String publicationRefId) {
			if (publicationRefId != null) {
				publicationRefIds.add(publicationRefId);
			}
		}

		protected List<String> getPublications() {
			return publicationRefIds;
		}

		protected String getEvidenceCode() {
			return evidenceCode;
		}

		@SuppressWarnings("unused")
		protected String getWithText() {
			return withText;
		}

		@SuppressWarnings("unused")
		protected String getDataset() {
			return dataSourceCode;
		}

		@SuppressWarnings("unused")
		protected String getDatasource() {
			return dataSource;
		}

		/**
		 * @return the storedAnnotationId
		 */
		protected Integer getStoredAnnotationId() {
			return storedAnnotationId;
		}

		/**
		 * @param storedAnnotationId
		 *            the storedAnnotationId to set
		 */
		protected void setStoredAnnotationId(Integer storedAnnotationId) {
			this.storedAnnotationId = storedAnnotationId;
		}
	}

	/**
	 * Identify a GoTerm/geneProduct pair with qualifier used to also use evidence code
	 */
	private class GoTermToGene {
		private String productId;
		private String goId;
		private String qualifier;

		/**
		 * Constructor
		 * 
		 * @param productId
		 *            gene/protein identifier
		 * @param goId
		 *            GO term id
		 * @param qualifier
		 *            qualifier
		 */
		GoTermToGene(String productId, String goId, String qualifier) {
			this.productId = productId;
			this.goId = goId;
			this.qualifier = qualifier;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object o) {
			if (o instanceof GoTermToGene) {
				GoTermToGene go = (GoTermToGene) o;
				return productId.equals(go.productId) && goId.equals(go.goId)
						&& qualifier.equals(go.qualifier);
			}
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return ((3 * productId.hashCode()) + (5 * goId.hashCode()) + (7 * qualifier.hashCode()));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			StringBuffer toStringBuff = new StringBuffer();

			toStringBuff.append("GoTermToGene - productId:");
			toStringBuff.append(productId);
			toStringBuff.append(" goId:");
			toStringBuff.append(goId);
			toStringBuff.append(" qualifier:");
			toStringBuff.append(qualifier);

			return toStringBuff.toString();
		}
	}

}
