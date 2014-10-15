package org.intermine.bio.dataconversion;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.model.bio.Protein;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.query.ConstraintOp;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.SimpleConstraint;
import org.intermine.util.SAXParser;
import org.intermine.xml.full.Item;
import org.intermine.xml.full.ReferenceList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * modified form the uniprot parser, only suitable for human, mouse and rat
 * because some parameters are hard-coded
 *  
 * @author chenyian
 */
public class FillProteinsXmlConverter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(FillProteinsXmlConverter.class);
	//
	private static final String DATA_SOURCE_NAME = "UniProt";
    private static final int POSTGRES_INDEX_SIZE = 2712;
    private static final String FEATURE_TYPES = "initiator methionine, signal peptide, transit peptide, propeptide, chain, peptide, topological domain, transmembrane region, intramembrane region, domain, repeat, calcium-binding region, zinc finger region, DNA-binding region, nucleotide phosphate-binding region, region of interest, coiled-coil region, short sequence motif, compositionally biased region, active site, metal ion-binding site, binding site, site, non-standard amino acid, modified residue, lipid moiety-binding region, glycosylation site, disulfide bond, cross-link";

    private String dataSource;

	private Map<String, String> keywords = new HashMap<String, String>();
	private Map<String, String> ontologies = new HashMap<String, String>();
	private Map<String, String> genes = new HashMap<String, String>();
    private Map<String, String> publications = new HashMap<String, String>();
    private Map<String, Map<String, String>> sequences = new HashMap<String, Map<String, String>>();
    private Map<String, String> allSequences = new HashMap<String, String>();

    private Set<String> identifiers = new HashSet<String>();
    
    private Set<Item> synonymsAndXrefs = new HashSet<Item>();

	private String osAlias = null;
	private Set<String> proteinAcc = new HashSet<String>();
	
	// for testing
	private int numOfNewEntries = 0;
	private int numOfProcessEntries = 0;
	
	@Override
	public void close() throws Exception {
		super.close();
		String info2 = numOfProcessEntries + " missing-info entries have been processed.";
		System.out.println(info2);
		LOG.info(info2);

		String info = numOfNewEntries + " missing-info entries have been created.";
		System.out.println(info);
		LOG.info(info);
	}

	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public FillProteinsXmlConverter(ItemWriter writer, Model model) {
		super(writer, model);
		dataSource = getDataSource(DATA_SOURCE_NAME);
		try {
			setOntology("UniProtKeyword");
		} catch (SAXException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		if (proteinAcc.isEmpty()) {
			getProteinAcc();
		}
		
		UniprotHandler handler = new UniprotHandler();
        try {
            SAXParser.parse(new InputSource(reader), handler);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

	}

	private void getProteinAcc() throws Exception {
		ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);

		List<Protein> proteins = getProteins(os);

		LOG.info("There are " + proteins.size() + " protein(s) without primaryIdentifier.");
		System.out.println("There are " + proteins.size() + " protein(s) without primaryIdentifier.");
		System.out.println("Start to fill missing information from uniprot xml files.");

		for (Iterator<Protein> i = proteins.iterator(); i.hasNext();) {
			Protein protein = (Protein) i.next();
			proteinAcc.add(protein.getPrimaryAccession());
		}
	}

	/* converts the XML into UniProt entry objects. run once per file */
	private class UniprotHandler extends DefaultHandler {
		private SimpleUniprotEntry entry;
		private Stack<String> stack = new Stack<String>();
		private String attName = null;
		private StringBuffer attValue = null;
		private int entryCount = 0;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attrs)
				throws SAXException {

			String previousQName = null;
			if (!stack.isEmpty()) {
				previousQName = stack.peek();
			}
			attName = null;
			if ("entry".equals(qName)) {
				entry = new SimpleUniprotEntry();
				String dataSetTitle = getAttrValue(attrs, "dataset") + " data set";
				entry.setDatasetRefId(getDataSet(dataSetTitle, dataSource));
			} else if ("fullName".equals(qName)
					&& stack.search("protein") == 2
					&& ("recommendedName".equals(previousQName) || "submittedName"
							.equals(previousQName))) {
				attName = "proteinName";
			} else if (("fullName".equals(qName) || "shortName".equals(qName))
					&& stack.search("protein") == 2
					&& ("alternativeName".equals(previousQName)
							|| "recommendedName".equals(previousQName) || "submittedName"
								.equals(previousQName))) {
				attName = "synonym";
			} else if ("fullName".equals(qName) && "recommendedName".equals(previousQName)
					&& stack.search("component") == 2) {
				attName = "component";
			} else if ("name".equals(qName) && "entry".equals(previousQName)) {
				attName = "primaryIdentifier";
			} else if ("ecNumber".equals(qName)) {
				attName = "ecNumber";
			} else if ("accession".equals(qName)) {
				attName = "value";
			} else if ("dbReference".equals(qName) && "organism".equals(previousQName)) {
				entry.setTaxonId(getAttrValue(attrs, "id"));
			} else if ("id".equals(qName) && "isoform".equals(previousQName)) {
				attName = "isoform";
			} else if ("sequence".equals(qName) && "isoform".equals(previousQName)) {
				String sequenceType = getAttrValue(attrs, "type");
				// ignore "external" types
				if ("displayed".equals(sequenceType)) {
					entry.addCanonicalIsoform(entry.getAttribute());
				} else if ("described".equals(sequenceType)) {
					entry.addIsoform(entry.getAttribute());
				}
			} else if ("sequence".equals(qName)) {
				String strLength = getAttrValue(attrs, "length");
				String strMass = getAttrValue(attrs, "mass");
				if (strLength != null) {
					entry.setLength(strLength);
					attName = "residues";
				}
				if (strMass != null) {
					entry.setMolecularWeight(strMass);
				}
				boolean isFragment = false;
				if (getAttrValue(attrs, "fragment") != null) {
					isFragment = true;
				}
				entry.setFragment(isFragment);
			} else if ("feature".equals(qName) && getAttrValue(attrs, "type") != null) {
				Item feature = getFeature(getAttrValue(attrs, "type"),
						getAttrValue(attrs, "description"), getAttrValue(attrs, "status"));
				entry.addFeature(feature);
			} else if (("begin".equals(qName) || "end".equals(qName)) && entry.processingFeature()
					&& getAttrValue(attrs, "position") != null) {
				entry.addFeatureLocation(qName, getAttrValue(attrs, "position"));
			} else if ("position".equals(qName) && entry.processingFeature()
					&& getAttrValue(attrs, "position") != null) {
				entry.addFeatureLocation("begin", getAttrValue(attrs, "position"));
				entry.addFeatureLocation("end", getAttrValue(attrs, "position"));
				// chenyian: retrieve RefSeq ids
			} else if ("dbReference".equals(qName) && "RefSeq".equals(getAttrValue(attrs, "type"))) {
				entry.addRefSeqProteinId(getAttrValue(attrs, "id"));
				// chenyian: retrieve Ensembl protein ids
			} else if ("property".equals(qName) && "dbReference".equals(previousQName)
					&& "protein sequence ID".equals(getAttrValue(attrs, "type"))) {
				entry.addEnsemblProteinId(getAttrValue(attrs, "value"));
			} else if ("dbReference".equals(qName) && "citation".equals(previousQName)
					&& "PubMed".equals(getAttrValue(attrs, "type"))) {
				entry.addPub(getPublication(getAttrValue(attrs, "id")));
			} else if ("comment".equals(qName)
					&& StringUtils.isNotEmpty(getAttrValue(attrs, "type"))) {
				entry.setCommentType(getAttrValue(attrs, "type"));
			} else if ("text".equals(qName) && "comment".equals(previousQName)) {
				attName = "text";
				String commentEvidence = getAttrValue(attrs, "evidence");
				if (StringUtils.isNotEmpty(commentEvidence)) {
					entry.setCommentEvidence(commentEvidence);
				}
			} else if ("keyword".equals(qName)) {
				attName = "keyword";
			} else if ("dbReference".equals(qName) && "entry".equals(previousQName)) {
				entry.addDbref(getAttrValue(attrs, "type"), getAttrValue(attrs, "id"));
			} else if ("property".equals(qName) && "dbReference".equals(previousQName)) {
				String type = getAttrValue(attrs, "type");
				if (type.equals("gene designation")) {
					entry.addGeneDesignation(getAttrValue(attrs, "value"));
				}
			} else if ("name".equals(qName) && "gene".equals(previousQName)) {
				attName = getAttrValue(attrs, "type");
			} else if ("evidence".equals(qName) && "entry".equals(previousQName)) {
				String evidenceCode = getAttrValue(attrs, "key");
				String pubmedString = getAttrValue(attrs, "attribute");
				if (StringUtils.isNotEmpty(evidenceCode) && StringUtils.isNotEmpty(pubmedString)) {
					String pubRefId = getEvidence(pubmedString);
					entry.addPubEvidence(evidenceCode, pubRefId);
				}
			} else if ("dbreference".equals(qName) || "comment".equals(qName)
					|| "isoform".equals(qName) || "gene".equals(qName)) {
				// set temporary holder variables to null
				entry.reset();
			}
			super.startElement(uri, localName, qName, attrs);
			stack.push(qName);
			attValue = new StringBuffer();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			super.endElement(uri, localName, qName);
			stack.pop();
			if (attName == null && attValue.toString() == null) {
				return;
			}

			String previousQName = null;
			if (!stack.isEmpty()) {
				previousQName = stack.peek();
			}

			if ("sequence".equals(qName)) {
				entry.setSequence(attValue.toString().replaceAll("\n", ""));
			} else if (StringUtils.isNotEmpty(attName) && "proteinName".equals(attName)) {
				entry.setName(attValue.toString());
			} else if (StringUtils.isNotEmpty(attName) && "synonym".equals(attName)) {
				entry.addProteinName(attValue.toString());
			} else if (StringUtils.isNotEmpty(attName) && "ecNumber".equals(attName)) {
				entry.addECNumber(attValue.toString());
			} else if ("text".equals(qName) && "comment".equals(previousQName)) {
				String commentText = attValue.toString();
				if (StringUtils.isNotEmpty(commentText)) {
					Item item = createItem("Comment");
					item.setAttribute("type", entry.getCommentType());
					if (commentText.length() > POSTGRES_INDEX_SIZE) {
						// comment text is a string
						String ellipses = "...";
						String choppedComment = commentText.substring(0, POSTGRES_INDEX_SIZE
								- ellipses.length());
						item.setAttribute("description", choppedComment + ellipses);
					} else {
						item.setAttribute("description", commentText);
					}
					String refId = item.getIdentifier();
					try {
						Integer objectId = store(item);
						entry.addCommentRefId(refId, objectId);
					} catch (ObjectStoreException e) {
						throw new SAXException(e);
					}
				}
			} else if ("name".equals(qName) && "gene".equals(previousQName)) {
				entry.addGeneName(attName, attValue.toString());
			} else if ("keyword".equals(qName)) {
				entry.addKeyword(getKeyword(attValue.toString()));
			} else if (StringUtils.isNotEmpty(attName) && "primaryIdentifier".equals(attName)) {
				entry.setPrimaryIdentifier(attValue.toString());
			} else if ("accession".equals(qName)) {
				String accession = attValue.toString();
				entry.addAccession(accession);
				if (accession.equals(entry.getPrimaryAccession())) {
					checkUniqueIdentifier(entry, accession);
				}
			} else if (StringUtils.isNotEmpty(attName) && "component".equals(attName)
					&& "fullName".equals(qName) && "recommendedName".equals(previousQName)
					&& stack.search("component") == 2) {
				entry.addComponent(attValue.toString());
			} else if ("id".equals(qName) && "isoform".equals(previousQName)) {
				String accession = attValue.toString();

				// 119 isoforms have commas in their IDs
				if (accession.contains(",")) {
					String[] accessions = accession.split("[, ]+");
					accession = accessions[0];
					for (int i = 1; i < accessions.length; i++) {
						entry.addIsoformSynonym(accessions[i]);
					}
				}

				// attribute should be empty, unless isoform has two <id>s
				if (entry.getAttribute() == null) {
					entry.addAttribute(accession);
				} else {
					// second <id> value is ignored and added as a synonym
					entry.addIsoformSynonym(accession);
				}
			} else if ("entry".equals(qName)) {
				if (proteinAcc.contains(entry.getPrimaryAccession())) {
					try {
						processCommentEvidence(entry);
						processEntry(entry);
						numOfProcessEntries++;
					} catch (ObjectStoreException e) {
						throw new SAXException(e);
					}
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void characters(char[] ch, int start, int length) {
			int st = start;
			int l = length;
			if (attName != null) {

				// DefaultHandler may call this method more than once for a single
				// attribute content -> hold text & create attribute in endElement
				while (l > 0) {
					boolean whitespace = false;
					switch (ch[st]) {
					case ' ':
					case '\r':
					case '\n':
					case '\t':
						whitespace = true;
						break;
					default:
						break;
					}
					if (!whitespace) {
						break;
					}
					++st;
					--l;
				}

				if (l > 0) {
					StringBuffer s = new StringBuffer();
					s.append(ch, st, l);
					attValue.append(s);
				}
			}
		}

		private void processEntry(SimpleUniprotEntry uniprotEntry) throws SAXException,
				ObjectStoreException {
			entryCount++;
			if (entryCount % 10000 == 0) {
				LOG.info("Processed " + entryCount + " entries.");
			}

			if (uniprotEntry.hasDatasetRefId() && uniprotEntry.hasPrimaryAccession()
					&& !uniprotEntry.isDuplicate()) {

				Item protein = createItem("Protein");
				
				protein.addToCollection("dataSets", uniprotEntry.getDatasetRefId());

				/* primaryAccession, primaryIdentifier, name, etc */
				processIdentifiers(protein, uniprotEntry);

				// processECNumbers(protein, uniprotEntry);

				String isCanonical = (uniprotEntry.isIsoform() ? "false" : "true");
				protein.setAttribute("isUniprotCanonical", isCanonical);

				/* sequence */
				if (!uniprotEntry.isIsoform()) {
					processSequence(protein, uniprotEntry);
				}

				protein.setReference("organism", getOrganism(uniprotEntry.getTaxonId()));

				/* publications */
				if (uniprotEntry.getPubs() != null) {
					protein.setCollection("publications", uniprotEntry.getPubs());
				}

				/* comments */
				if (uniprotEntry.hasComments()) {
					protein.setCollection("comments", uniprotEntry.getComments());
					processCommentEvidence(uniprotEntry);
				}

				/* keywords */
				if (uniprotEntry.getKeywords() != null) {
					protein.setCollection("keywords", uniprotEntry.getKeywords());
				}

				/* features */
				processFeatures(protein, uniprotEntry);

				/* components */
				if (uniprotEntry.getComponents() != null && !uniprotEntry.getComponents().isEmpty()) {
					processComponents(protein, uniprotEntry);
				}

				// record that we have seen this sequence for this organism
				addSeenSequence(uniprotEntry.getTaxonId(), uniprotEntry.getMd5checksum(),
						protein.getIdentifier());

				try {
					/* dbrefs (go terms, refseq) */
					processDbrefs(protein, uniprotEntry);

					/* genes */
					processGene(protein, uniprotEntry);

					store(protein);
					
					numOfNewEntries++;

					// create synonyms for accessions and store xrefs and synonyms we've collected
					processSynonyms(protein.getIdentifier(), uniprotEntry);

				} catch (ObjectStoreException e) {
					throw new SAXException(e);
				}
				synonymsAndXrefs = new HashSet<Item>();
			}
		}

		private void processCommentEvidence(SimpleUniprotEntry uniprotEntry) throws ObjectStoreException {
			Map<Integer, List<String>> commentEvidence = uniprotEntry.getCommentEvidence();
			for (Map.Entry<Integer, List<String>> e : commentEvidence.entrySet()) {
				Integer intermineObjectId = e.getKey();
				List<String> evidenceCodes = e.getValue();
				List<String> pubRefIds = new ArrayList<String>();
				for (String code : evidenceCodes) {
					String pubRefId = uniprotEntry.getPubRefId(code);
					if (pubRefId != null) {
						pubRefIds.add(pubRefId);
					} else {
						LOG.error("bad evidence code:" + code + " for "
								+ uniprotEntry.getPrimaryAccession());
					}
				}
				if (!pubRefIds.isEmpty()) {
					ReferenceList publications = new ReferenceList("publications",
							new ArrayList<String>(pubRefIds));
					store(publications, intermineObjectId);
				}
			}
		}

		private void processSequence(Item protein, SimpleUniprotEntry uniprotEntry) {
			String seqIdentifier = getSequenceIdentfier(uniprotEntry.getMd5checksum(),
					uniprotEntry.getSequence(), uniprotEntry.getLength());
			protein.setAttribute("length", uniprotEntry.getLength());
			protein.setReference("sequence", seqIdentifier);
			protein.setAttribute("molecularWeight", uniprotEntry.getMolecularWeight());
			protein.setAttribute("md5checksum", uniprotEntry.getMd5checksum());
		}

		private String getSequenceIdentfier(String md5Checksum, String residues, String length) {
			if (!allSequences.containsKey(md5Checksum)) {
				Item item = createItem("Sequence");
				item.setAttribute("residues", residues);
				item.setAttribute("length", length);
				item.setAttribute("md5checksum", md5Checksum);
				try {
					store(item);
				} catch (ObjectStoreException e) {
					throw new RuntimeException(e);
				}
				allSequences.put(md5Checksum, item.getIdentifier());
			}
			return allSequences.get(md5Checksum);
		}

		private void processIdentifiers(Item protein, SimpleUniprotEntry uniprotEntry) {
			protein.setAttribute("name", uniprotEntry.getName());
			protein.setAttribute("isFragment", uniprotEntry.isFragment());
			protein.setAttribute("uniprotAccession", uniprotEntry.getUniprotAccession());
			String primaryAccession = uniprotEntry.getPrimaryAccession();
			protein.setAttribute("primaryAccession", primaryAccession);

			String primaryIdentifier = uniprotEntry.getPrimaryIdentifier();
			protein.setAttribute("uniprotName", primaryIdentifier);

			// primaryIdentifier must be unique, so append isoform suffix, eg -1
			if (uniprotEntry.isIsoform()) {
				primaryIdentifier = getIsoformIdentifier(primaryAccession, primaryIdentifier);
			}
			protein.setAttribute("primaryIdentifier", primaryIdentifier);
		}

		private String getIsoformIdentifier(String primaryAccession, String primaryIdentifier) {
			String isoformIdentifier = primaryIdentifier;
			String[] bits = primaryAccession.split("\\-");
			if (bits.length == 2) {
				isoformIdentifier += "-" + bits[1];
			}
			return isoformIdentifier;
		}

		private void processComponents(Item protein, SimpleUniprotEntry uniprotEntry) throws SAXException {
			for (String componentName : uniprotEntry.getComponents()) {
				Item component = createItem("Component");
				component.setAttribute("name", componentName);
				component.setReference("protein", protein);
				try {
					store(component);
				} catch (ObjectStoreException e) {
					throw new SAXException(e);
				}
			}
		}

		private void processFeatures(Item protein, SimpleUniprotEntry uniprotEntry) throws SAXException {
			List<String> featureTypes = Arrays.asList(FEATURE_TYPES.split(",\\s*"));
			for (Item feature : uniprotEntry.getFeatures()) {
				// only store the features of interest
				if (featureTypes.isEmpty()
						|| featureTypes.contains(feature.getAttribute("type").getValue())) {
					feature.setReference("protein", protein);
					try {
						store(feature);
					} catch (ObjectStoreException e) {
						throw new SAXException(e);
					}
				}
			}
		}

		private void processSynonyms(String proteinRefId, SimpleUniprotEntry uniprotEntry)
				throws ObjectStoreException {

			// accessions
			for (String accession : uniprotEntry.getAccessions()) {
				createSynonym(proteinRefId, accession, true);
			}

			// primaryIdentifier if isoform
			if (uniprotEntry.isIsoform()) {
				String isoformIdentifier = getIsoformIdentifier(uniprotEntry.getPrimaryAccession(),
						uniprotEntry.getPrimaryIdentifier());
				createSynonym(proteinRefId, isoformIdentifier, true);
			}

			// name <recommendedName> or <alternateName>
			for (String name : uniprotEntry.getProteinNames()) {
				createSynonym(proteinRefId, name, true);
			}

			// isoforms with extra identifiers
			List<String> isoformSynonyms = uniprotEntry.getIsoformSynonyms();
			if (isoformSynonyms != null && !isoformSynonyms.isEmpty()) {
				for (String identifier : isoformSynonyms) {
					createSynonym(proteinRefId, identifier, true);
				}
			}

			// chenyian: RefSeq identifiers
			for (String refSeqId : entry.getRefSeqProteinIds()) {
				createSynonym(proteinRefId, refSeqId, true);
			}
			// chenyian: Ensembl protein identifiers
			for (String ensemblProteinId : entry.getEnsemblProteinIds()) {
				createSynonym(proteinRefId, ensemblProteinId, true);
			}

			// store xrefs and other synonyms we've created elsewhere
			for (Item item : synonymsAndXrefs) {
				if (item == null) {
					continue;
				}
				store(item);
			}
		}

		private void processDbrefs(Item protein, SimpleUniprotEntry uniprotEntry) throws SAXException,
				ObjectStoreException {
			Map<String, Set<String>> dbrefs = uniprotEntry.getDbrefs();
			for (Map.Entry<String, Set<String>> dbref : dbrefs.entrySet()) {
				String key = dbref.getKey();
				Set<String> values = dbref.getValue();
				for (String identifier : values) {
					setCrossReference(protein.getIdentifier(), identifier, key, false);
				}
			}
		}

		// if cross references not listed in CONFIG, load all
		private void setCrossReference(String subjectId, String value, String dataSource,
				boolean store) throws ObjectStoreException {
			List<String> xrefs = Arrays.asList("RefSeq,UniGene".split(","));
			if (xrefs.contains(dataSource)) {
				Item item = createCrossReference(subjectId, value, dataSource, store);
				if (item != null) {
					synonymsAndXrefs.add(item);
				}
			}
		}

		// gets the unique identifier and list of identifiers to set
		// loops through each gene entry, assigns refId to protein
		private void processGene(Item protein, SimpleUniprotEntry uniprotEntry)
				throws ObjectStoreException {
			String taxId = uniprotEntry.getTaxonId();
			String uniqueIdentifierField = "primaryIdentifier";
			Set<String> geneIdentifiers = getGeneIdentifiers(uniprotEntry, uniqueIdentifierField);
			if (geneIdentifiers == null) {
				LOG.error("no valid gene identifiers found for "
						+ uniprotEntry.getPrimaryAccession());
				return;
			}
			Item gene = null;
			for (String identifier : geneIdentifiers) {
				if (StringUtils.isEmpty(identifier)) {
					continue;
				}
				gene = getGene(protein, uniprotEntry, identifier, taxId, uniqueIdentifierField);
				if (gene != null) {
					store(gene);
				}
			}

		}

		private Item getGene(Item protein, SimpleUniprotEntry uniprotEntry, String geneIdentifier,
				String taxId, String uniqueIdentifierField) {
			String identifier = geneIdentifier;
			if (identifier == null) {
				return null;
			}

			String geneRefId = genes.get(identifier);
			if (geneRefId == null) {
				Item gene = createItem("Gene");
				gene.setAttribute(uniqueIdentifierField, identifier);
				gene.setReference("organism", getOrganism(taxId));
				geneRefId = gene.getIdentifier();
				genes.put(identifier, geneRefId);
				protein.addToCollection("genes", geneRefId);
				return gene;
			}
			protein.addToCollection("genes", geneRefId);
			return null;
		}

		private Set<String> getGeneIdentifiers(SimpleUniprotEntry uniprotEntry, String identifierField) {
			return getByDbref(uniprotEntry, "GeneID");
		}

		private Set<String> getByDbref(SimpleUniprotEntry uniprotEntry, String value) {
			Set<String> geneIdentifiers = new HashSet<String>();
			if ("Ensembl".equals(value)) {
				// See #2122
				String geneDesignation = uniprotEntry.getGeneDesignation(value);
				geneIdentifiers.add(geneDesignation);
			} else {
				Map<String, Set<String>> dbrefs = uniprotEntry.getDbrefs();
				final String msg = "no " + value
						+ " identifier found for gene attached to protein: "
						+ uniprotEntry.getPrimaryAccession();
				if (dbrefs == null || dbrefs.isEmpty()) {
					LOG.error(msg);
					return null;
				}
				Set<String> dbrefValues = dbrefs.get(value);
				if (dbrefValues == null || dbrefValues.isEmpty()) {
					LOG.error(msg);
					return null;
				}
				geneIdentifiers = dbrefs.get(value);
			}
			return geneIdentifiers;
		}

		private String getAttrValue(Attributes attrs, String name) {
			if (attrs.getValue(name) != null) {
				return attrs.getValue(name).trim();
			}
			return null;
		}

	}
	
    private String setOntology(String title)
            throws SAXException {
            String refId = ontologies.get(title);
            if (refId == null) {
                Item ontology = createItem("Ontology");
                ontology.setAttribute("name", title);
                ontologies.put(title, ontology.getIdentifier());
                try {
                    store(ontology);
                } catch (ObjectStoreException e) {
                    throw new SAXException(e);
                }
            }
            return refId;
        }

	private Item getFeature(String type, String description, String status) throws SAXException {
		Item feature = createItem("UniProtFeature");
		feature.setAttribute("type", type);
		String keywordRefId = getKeyword(type);
		feature.setReference("feature", keywordRefId);
		String featureDescription = description;
		if (status != null) {
			featureDescription = (description == null ? status : description + " (" + status + ")");
		}
		if (!StringUtils.isEmpty(featureDescription)) {
			feature.setAttribute("description", featureDescription);
		}
		return feature;
	}

	private String getKeyword(String title) throws SAXException {
		String refId = keywords.get(title);
		if (refId == null) {
			Item item = createItem("OntologyTerm");
			item.setAttribute("name", title);
			item.setReference("ontology", ontologies.get("UniProtKeyword"));
			refId = item.getIdentifier();
			keywords.put(title, refId);
			try {
				store(item);
			} catch (ObjectStoreException e) {
				throw new SAXException(e);
			}
		}
		return refId;
	}

	private String getPublication(String pubMedId) throws SAXException {
		String refId = publications.get(pubMedId);

		if (refId == null) {
			Item item = createItem("Publication");
			item.setAttribute("pubMedId", pubMedId);
			publications.put(pubMedId, item.getIdentifier());
			try {
				store(item);
			} catch (ObjectStoreException e) {
				throw new SAXException(e);
			}
			refId = item.getIdentifier();
		}

		return refId;
	}

    private String getEvidence(String attribute) throws SAXException {
        if (attribute.contains("=")) {
            String[] bits = attribute.split("=");
            if (bits.length == 2) {
                String pubMedId = bits[1];
                if (StringUtils.isNotEmpty(pubMedId)) {
                    return getPublication(pubMedId);
                }
            }
        }
        return null;
    }

    private void checkUniqueIdentifier(SimpleUniprotEntry entry, String identifier) {
        if (StringUtils.isNotEmpty(identifier)) {
            if (!isUniqueIdentifier(identifier)) {
                entry.setDuplicate(true);
            }
        }
    }

    private boolean isUniqueIdentifier(String identifier) {
        if (identifiers.contains(identifier)) {
            LOG.error("not assigning duplicate identifier:  " + identifier);
            return false;
        }
        identifiers.add(identifier);
        return true;
    }
    
    private void addSeenSequence(String taxonId, String md5checksum, String proteinIdentifier) {
        Map<String, String> orgSequences = sequences.get(taxonId);
        if (orgSequences == null) {
            orgSequences = new HashMap<String, String>();
            sequences.put(taxonId, orgSequences);
        }
        if (!orgSequences.containsKey(md5checksum)) {
            orgSequences.put(md5checksum, proteinIdentifier);
        }
    }

    private boolean seenSequence(String taxonId, String md5checksum) {
        Map<String, String> orgSequences = sequences.get(taxonId);
        if (orgSequences == null) {
            orgSequences = new HashMap<String, String>();
            sequences.put(taxonId, orgSequences);
        }
        return orgSequences.containsKey(md5checksum);
    }

    
	/**
	 * Retrieve the proteins to be updated
	 * 
	 * @param os
	 *            the ObjectStore to read from
	 * @return a List of Protein object
	 */
	protected List<Protein> getProteins(ObjectStore os) {
		Query q = new Query();
		QueryClass qc = new QueryClass(Protein.class);
		q.addFrom(qc);
		q.addToSelect(qc);

		SimpleConstraint sc = new SimpleConstraint(new QueryField(qc, "primaryIdentifier"),
				ConstraintOp.IS_NULL);

		q.setConstraint(sc);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Protein> ret = (List<Protein>) ((List) os.executeSingleton(q));

		return ret;
	}

}
