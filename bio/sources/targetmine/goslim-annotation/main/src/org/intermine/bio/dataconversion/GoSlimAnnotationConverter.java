package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
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
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.StringUtil;
import org.intermine.xml.full.Item;
import org.intermine.xml.full.ReferenceList;

/**
 * This parser is modified and simplified from original GOA parser, 
 * thus probably is only suitable for TargetMine;
 * Human, mouse, rat GOA file format are same so far
 * BioEntity references in GOEvidence were ignored
 * 
 * @author chenyian
 */
public class GoSlimAnnotationConverter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(GoSlimAnnotationConverter.class);
	//
	private static final String DATASET_TITLE = "UniProt-GOA";
	private static final String DATA_SOURCE_NAME = "EMBL-EBI";

	protected Map<String, String> goSlimTerms = new LinkedHashMap<String, String>();
	private Map<String, String> evidenceCodes = new LinkedHashMap<String, String>();
	private Map<String, String> publications = new LinkedHashMap<String, String>();
	protected Map<String, String> productMap = new LinkedHashMap<String, String>();
	private Set<String> dbRefs = new HashSet<String>();

	private Map<GoSlimTermToGene, Set<Evidence>> goSlimTermGeneToEvidence = new LinkedHashMap<GoSlimTermToGene, Set<Evidence>>();
	private Map<Integer, List<String>> productCollectionsMap;
	private Map<String, Integer> storedProductIds;

	private Map<String, String> proteinMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public GoSlimAnnotationConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {

		initialiseMapsForFile();

		BufferedReader br = new BufferedReader(reader);
		String line = null;

		// loop through entire file
		// http://www.geneontology.org/GO.format.gaf-2_0.shtml
		while ((line = br.readLine()) != null) {
			if (line.startsWith("!")) {
				continue;
			}
			String[] array = line.split("\t", -1); // keep trailing empty Strings
			if (array.length < 13) {
				throw new IllegalArgumentException("Not enough elements (should be > 13 not "
						+ array.length + ") in line: " + line);
			}

			String taxonId = parseTaxonId(array[12]);

			String uniprotId = array[1];

			String goId = array[4];
			String qualifier = array[3];
			String strEvidence = array[6];
			String withText = array[7];
			String annotationExtension = null;
			if (array.length >= 16) {
				annotationExtension = array[15];
			}
			if (StringUtils.isNotEmpty(strEvidence)) {
				storeEvidenceCode(strEvidence);
			} else {
				throw new IllegalArgumentException("Evidence is a required column but not "
						+ "found for goterm " + goId + " and productId " + uniprotId);
			}

			String type = array[11];

			// create unique key for go annotation
			GoSlimTermToGene key = new GoSlimTermToGene(uniprotId, goId, qualifier);

			String dataSourceCode = array[14]; // e.g. GDB, where uniprot collect the data from
			String dataSource = array[0]; // e.g. UniProtKB, where the goa file comes from
			String organism = getOrganism(taxonId);
			String productIdentifier = getProtein(uniprotId, taxonId);

			// null if resolver could not resolve an identifier
			// TODO to be removed, should not be null
			if (productIdentifier != null) {

				// null if no pub found
				String pubRefId = newPublication(array[5]);

				// get evidence codes for this goterm|gene pair
				Set<Evidence> allEvidenceForAnnotation = goSlimTermGeneToEvidence.get(key);

				// new evidence
				if (allEvidenceForAnnotation == null || !StringUtils.isEmpty(withText)) {
					String goTermIdentifier = getGoSlimTerm(goId);
					Evidence evidence = new Evidence(strEvidence, pubRefId, withText, organism,
							dataSource, dataSourceCode);
					allEvidenceForAnnotation = new LinkedHashSet<Evidence>();
					allEvidenceForAnnotation.add(evidence);
					goSlimTermGeneToEvidence.put(key, allEvidenceForAnnotation);
					Integer storedAnnotationId = createGoAnnotation(productIdentifier, type,
							goTermIdentifier, qualifier, annotationExtension);
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
						Evidence evidence = new Evidence(strEvidence, pubRefId, withText, organism,
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

	/**
	 * Reset maps that don't need to retain their contents between files.
	 */
	protected void initialiseMapsForFile() {
		goSlimTermGeneToEvidence = new LinkedHashMap<GoSlimTermToGene, Set<Evidence>>();
		productCollectionsMap = new LinkedHashMap<Integer, List<String>>();
		storedProductIds = new HashMap<String, Integer>();
	}

	private void storeProductCollections() throws ObjectStoreException {
		for (Map.Entry<Integer, List<String>> entry : productCollectionsMap.entrySet()) {
			Integer storedProductId = entry.getKey();
			List<String> annotationIds = entry.getValue();
			ReferenceList goAnnotation = new ReferenceList("goSlimAnnotation", annotationIds);
			store(goAnnotation, storedProductId);
		}
	}

	private void storeEvidence() throws ObjectStoreException {
		for (Set<Evidence> annotationEvidence : goSlimTermGeneToEvidence.values()) {
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

	private Integer createGoAnnotation(String productIdentifier, String productType,
			String termIdentifier, String qualifier, String annotationExtension)
			throws ObjectStoreException {
		Item goSlimAnnotation = createItem("GOSlimAnnotation");
		goSlimAnnotation.setReference("subject", productIdentifier);
		goSlimAnnotation.setReference("ontologyTerm", termIdentifier);

		if (!StringUtils.isEmpty(qualifier)) {
			goSlimAnnotation.setAttribute("qualifier", qualifier);
		}
		if (!StringUtils.isEmpty(annotationExtension)) {
			goSlimAnnotation.setAttribute("annotationExtension", annotationExtension);
		}

		// chenyian: add goAnnotation reference for protein also
		addProductCollection(productIdentifier, goSlimAnnotation.getIdentifier());

		Integer storedAnnotationId = store(goSlimAnnotation);
		return storedAnnotationId;
	}

	private void addProductCollection(String productIdentifier, String goAnnotationIdentifier) {
		Integer storedProductId = storedProductIds.get(productIdentifier);
		List<String> annotationIds = productCollectionsMap.get(storedProductId);
		if (annotationIds == null) {
			annotationIds = new ArrayList<String>();
			productCollectionsMap.put(storedProductId, annotationIds);
		}
		annotationIds.add(goAnnotationIdentifier);
	}

	private String getProtein(String uniprotAcc, String taxonId) throws ObjectStoreException {
		String ret = proteinMap.get(uniprotAcc);
		if (ret == null) {
			Item item = createItem("Protein");
			item.setAttribute("primaryAccession", uniprotAcc);
			item.setReference("organism", getOrganism(taxonId));
			ret = item.getIdentifier();
			proteinMap.put(uniprotAcc, ret);
			Integer storedProductId = store(item);
			storedProductIds.put(ret, storedProductId);
		}
		return ret;
	}

	private String getGoSlimTerm(String identifier) throws ObjectStoreException {
		if (identifier == null) {
			return null;
		}

		String goTermIdentifier = goSlimTerms.get(identifier);
		if (goTermIdentifier == null) {
			Item item = createItem("GOSlimTerm");
			item.setAttribute("identifier", identifier);
			store(item);

			goTermIdentifier = item.getIdentifier();
			goSlimTerms.put(identifier, goTermIdentifier);
		}
		return goTermIdentifier;
	}

	private void storeEvidenceCode(String code) throws ObjectStoreException {
		if (evidenceCodes.get(code) == null) {
			Item item = createItem("GOEvidenceCode");
			item.setAttribute("code", code);
			evidenceCodes.put(code, item.getIdentifier());
			store(item);
		}
	}

	private String getDataSourceCodeName(String sourceCode) {
		String title = sourceCode;

		// re-write some codes to better data source names
		if ("UniProtKB".equalsIgnoreCase(sourceCode)) {
			title = "UniProt";
		} else if ("FB".equalsIgnoreCase(sourceCode)) {
			title = "FlyBase";
		} else if ("WB".equalsIgnoreCase(sourceCode)) {
			title = "WormBase";
		} else if ("SP".equalsIgnoreCase(sourceCode)) {
			title = "UniProt";
		} else if (sourceCode.startsWith("GeneDB")) {
			title = "GeneDB";
		} else if ("SANGER".equalsIgnoreCase(sourceCode)) {
			title = "GeneDB";
		} else if ("GOA".equalsIgnoreCase(sourceCode)) {
			title = "Gene Ontology";
		} else if ("PINC".equalsIgnoreCase(sourceCode)) {
			title = "Proteome Inc.";
		} else if ("Pfam".equalsIgnoreCase(sourceCode)) {
			title = "PFAM"; // to merge with interpro
		}
		return title;
	}

	private String newPublication(String codes) throws ObjectStoreException {
		String pubRefId = null;
		String[] array = codes.split("[|]");
		Set<String> xrefs = new HashSet<String>();
		Item item = null;
		for (int i = 0; i < array.length; i++) {
			if (array[i].startsWith("PMID:")) {
				String pubMedId = array[i].substring(5);
				if (StringUtil.allDigits(pubMedId)) {
					pubRefId = publications.get(pubMedId);
					if (pubRefId == null) {
						item = createItem("Publication");
						item.setAttribute("pubMedId", pubMedId);
						pubRefId = item.getIdentifier();
						publications.put(pubMedId, pubRefId);

					}
				}
			} else {
				xrefs.add(array[i]);
			}
		}
		ReferenceList refIds = new ReferenceList("crossReferences");

		// PMID may be first or last so we can't process xrefs until we've looked at all IDs
		if (StringUtils.isNotEmpty(pubRefId)) {
			for (String xref : xrefs) {
				refIds.addRefId(createDbReference(xref));
			}
		}
		if (item != null) {
			item.addCollection(refIds);
			store(item);
		}
		return pubRefId;
	}

	private String createDbReference(String value) throws ObjectStoreException {
		if (StringUtils.isEmpty(value)) {
			return null;
		}
		String dataSource = null;
		if (!dbRefs.contains(value)) {
			Item item = createItem("DatabaseReference");
			// FB:FBrf0055969
			if (value.contains(":")) {
				String[] bits = value.split(":");
				if (bits.length == 2) {
					String db = bits[0];
					dataSource = getDataSourceCodeName(db);
					value = bits[1];
				}
			}
			item.setAttribute("identifier", value);
			if (StringUtils.isNotEmpty(dataSource)) {
				item.setReference("source", getDataSource(dataSource));
			}
			dbRefs.add(value);
			store(item);
			return item.getIdentifier();
		}
		return null;
	}

	private String parseTaxonId(String input) {
		if ("taxon:".equals(input)) {
			throw new IllegalArgumentException("Invalid taxon id read: " + input);
		}
		String taxonId = input.split(":")[1];
		if (taxonId.contains("|")) {
			taxonId = taxonId.split("\\|")[0];
		}
		return taxonId;
	}

	private class Evidence {
		private List<String> publicationRefIds = new ArrayList<String>();
		private String evidenceCode = null;
		private Integer storedAnnotationId = null;
		private String withText = null;
		private String organismRefId = null;
		private String dataSourceCode = null;
		private String dataSource = null;

		// dataSource, dataSourceCode

		protected Evidence(String evidenceCode, String publicationRefId, String withText,
				String organismRefId, String dataset, String datasource) {
			this.evidenceCode = evidenceCode;
			this.withText = withText;
			this.organismRefId = organismRefId;
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

		@SuppressWarnings("unused")
		protected String getOrganismRefId() {
			return organismRefId;
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
	private class GoSlimTermToGene {
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
		GoSlimTermToGene(String productId, String goId, String qualifier) {
			this.productId = productId;
			this.goId = goId;
			this.qualifier = qualifier;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object o) {
			if (o instanceof GoSlimTermToGene) {
				GoSlimTermToGene go = (GoSlimTermToGene) o;
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
