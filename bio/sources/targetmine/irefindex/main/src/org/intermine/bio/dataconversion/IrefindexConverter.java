package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.model.bio.Protein;
import org.intermine.model.bio.Synonym;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.query.ConstraintOp;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryCollectionReference;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.QueryValue;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.objectstore.query.SimpleConstraint;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * Parser for iRefIndex (http://irefindex.uio.no/wiki/iRefIndex)
 * 
 * @author chenyian
 */
public class IrefindexConverter extends BioFileConverter {
	//
	private static final String DATASET_TITLE = "iRefIndex";
	private static final String DATA_SOURCE_NAME = "iRefIndex interaction data set";
	private static final String TYPE_FILE = "interactiontype.txt";

	private static final Logger LOG = Logger.getLogger(IrefindexConverter.class);

	private Map<String, String> interactionTypeMap;

	private Map<String, String> geneMap = new HashMap<String, String>();
	private Map<String, String> pubMap = new HashMap<String, String>();
	private Map<String, String> miMap = new HashMap<String, String>();
	private Map<MultiKey, String> expMap = new HashMap<MultiKey, String>();

	// to prevent duplications
	private List<String> interactions = new ArrayList<String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public IrefindexConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		if (interactionTypeMap == null) {
			readInteractionType();
		}

		if (proteinIdMap == null) {
			getProteinIdMap();
		}

		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(reader));

		// skip header
		iterator.next();

		while (iterator.hasNext()) {
			String[] cols = iterator.next();

			if (cols[7].equals("OPHID Predicted Protein Interaction")
					|| cols[7].equals("HPRD Text Mining Confirmation")
					|| cols[7].equals("MINT Text Mining Confirmation")) {
				continue;
			}

			// some data from OPHID are not tagged in the author column
			String sourceDb = getMiDesc(cols[12]);
			if (sourceDb.equals("ophid")) {
				continue;
			}
			String[] ids = cols[13].split("\\|");

			// TODO add ProteinInteraction
			String proteinIdA = getProteinId(cols[0]);
			String proteinIdB = getProteinId(cols[1]);
			if (proteinIdA != null && proteinIdB != null) {
				String piSourceRef = getPiSource(sourceDb, ids[0]);
				createProteinInteraction(proteinIdA, cols[9], proteinIdB, cols[10], piSourceRef);
				createProteinInteraction(proteinIdB, cols[10], proteinIdA, cols[9], piSourceRef);
			}

			String geneA = processAltIdentifier(cols[2]);
			if (geneA == null) {
				continue;
			}
			String geneB = processAltIdentifier(cols[3]);
			if (geneB == null) {
				continue;
			}

			String expRefId = getExperiment(cols[8].substring(7), cols[7], cols[6], cols[28],
					sourceDb, ids[0]);

			String intKey = String.format("%s_%s_%s", geneA, geneB, expRefId);

			if (interactions.contains(intKey)) {
				continue;
			}

			String conf = cols[14].split("\\|PSICQUIC")[0].trim();

			createInteraction(getMiDesc(cols[18]), geneA, cols[9], geneB, cols[10], cols[11],
					expRefId, getMiDesc(cols[16]), getMiDesc(cols[20]), conf);

			if (!geneA.equals(geneB)) {
				createInteraction(getMiDesc(cols[19]), geneB, cols[10], geneA, cols[9], cols[11],
						expRefId, getMiDesc(cols[17]), getMiDesc(cols[21]), conf);
			}
			interactions.add(intKey);
		}

	}

	private void createProteinInteraction(String proteinIdA, String taxonIdA, String proteinIdB,
			String taxonIdB, String piSourceRef) throws ObjectStoreException {
		String name = String.format("iRef:%s_%s", proteinIdA, proteinIdB);
		Item pi = createItem("ProteinInteraction");
		pi.setAttribute("shortName", name);
		pi.setReference("protein", getProtein(proteinIdA, taxonIdA));
		pi.setReference("representativePartner", getProtein(proteinIdB, taxonIdB));
		pi.addToCollection("piSources", piSourceRef);
		store(pi);
	}

	private String getMiDesc(String s) {
		return s.substring(8, s.length() - 1);
	}

	private void createInteraction(String role, String geneId, String geneTaxon,
			String interactingGeneId, String interactingGeneTaxon, String miType, String expRefId,
			String bioRole, String interactor, String confidence) throws ObjectStoreException {
		Item interaction = createItem("Interaction");
		interaction.setAttribute("role", role);
		interaction.setReference("gene", getGene(geneId, geneTaxon));
		interaction.addToCollection("interactingGenes",
				getGene(interactingGeneId, interactingGeneTaxon));
		if (!miType.equals("-")) {
			miType = miType.substring(0, 7);
			interaction.setReference("type", getInteractionTerm(miType));
			String intType = interactionTypeMap.get(miType);
			if (intType != null) {
				interaction.setAttribute("interactionType", intType);
			} else {
				LOG.error(String.format("Cannot resolve interaction type: %s", miType));
			}
		}
		interaction.setReference("experiment", expRefId);
		String interactionName = "iRef:" + geneId + "_" + interactingGeneId;
		interaction.setAttribute("name", interactionName);
		interaction.setAttribute("shortName", interactionName);

		if (!confidence.isEmpty()) {
			interaction.setAttribute("confidenceText", confidence);
		}

		// 2 new attributes
		interaction.setAttribute("biologicalRole", bioRole);
		interaction.setAttribute("interactorType", interactor);

		store(interaction);
	}

	private void readInteractionType() throws IOException {
		interactionTypeMap = new HashMap<String, String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(getClass()
				.getClassLoader().getResourceAsStream(TYPE_FILE)));
		String line;
		String type = "";
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}
			if (line.startsWith("//")) {
				continue;
			}
			if (line.startsWith("#")) {
				type = line.substring(1);
			} else {
				interactionTypeMap.put(line, type);
			}
		}

	}

	private String getPublication(String pubMedId) throws ObjectStoreException {
		String itemId = pubMap.get(pubMedId);
		if (itemId == null) {
			Item pub = createItem("Publication");
			pub.setAttribute("pubMedId", pubMedId);
			itemId = pub.getIdentifier();
			pubMap.put(pubMedId, itemId);
			store(pub);
		}
		return itemId;
	}

	private String getGene(String geneId, String taxonField) throws ObjectStoreException {
		String itemId = geneMap.get(geneId);

		String taxonId = taxonField;
		if (itemId == null) {
			Item gene = createItem("Gene");
			gene.setAttribute("ncbiGeneNumber", geneId);
			if (!taxonField.equals("-")) {
				taxonId = taxonField.substring(6, taxonField.indexOf("("));
				gene.setReference("organism", getOrganism(taxonId));
			}
			itemId = gene.getIdentifier();
			geneMap.put(geneId, itemId);
			store(gene);
		}
		return itemId;
	}

	private String getInteractionTerm(String miId) throws ObjectStoreException {
		String itemId = miMap.get(miId);
		if (itemId == null) {
			Item term = createItem("InteractionTerm");
			term.setAttribute("identifier", miId);
			itemId = term.getIdentifier();
			miMap.put(miId, itemId);
			store(term);
		}
		return itemId;
	}

	private String getExperiment(String pubMedId, String author, String detectioniMethod,
			String host, String sourceDb, String sourceId) throws ObjectStoreException {
		MultiKey key = new MultiKey(pubMedId, detectioniMethod, sourceId);
		String ret = expMap.get(key);
		if (ret == null) {
			Item exp = createItem("InteractionExperiment");
			if (!author.equals("-")) {
				exp.setAttribute("name", author);
			}
			exp.setReference("publication", getPublication(pubMedId));
			if (!detectioniMethod.equals("-")) {
				exp.addToCollection("interactionDetectionMethods",
						getInteractionTerm(detectioniMethod.substring(0, 7)));
			}

			// new attributes
			if (host.startsWith("taxid:")) {
				String desc = host.substring(host.indexOf("(") + 1, host.length() - 1);
				if (desc.equals("-")) {
					desc = host.substring(0, host.indexOf("("));
				}
				exp.setAttribute("hostOrganism", desc);
			}
			exp.setAttribute("sourceDb", sourceDb);
			exp.setAttribute("sourceId", sourceId);

			ret = exp.getIdentifier();
			expMap.put(key, ret);
			store(exp);
		}
		return ret;
	}

	private String processAltIdentifier(String altIdentifier) {
		String[] ids = altIdentifier.split("\\|");
		for (String id : ids) {
			if (id.startsWith("entrezgene/locuslink:")) {
				return id.substring(id.indexOf(":") + 1);
			}
		}
		return null;
	}

	private String getProteinId(String string) {
		if (string.startsWith("uniprotkb:")) {
			return string.substring(string.indexOf(":") + 1);
		} else if (string.startsWith("refseq:")) {
			Set<String> ids = proteinIdMap.get(string.substring(string.indexOf(":") + 1));
			if (ids != null) {
				if (ids.size() == 1) {
					return ids.iterator().next();
				}
				LOG.info("Multiple mapping: " + string + "; " + ids);
			} else {
				LOG.info("Unablel to map the ID: " + string);
			}
		}
		return null;
	}

	private Map<String, Set<String>> proteinIdMap;
	private String osAlias = null;

	/**
	 * Set the ObjectStore alias.
	 * 
	 * @param osAlias
	 *            The ObjectStore alias
	 */
	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

	@SuppressWarnings("unchecked")
	private void getProteinIdMap() throws Exception {
		proteinIdMap = new HashMap<String, Set<String>>();

		Query q = new Query();
		QueryClass qcSynonym = new QueryClass(Synonym.class);
		QueryClass qcProtein = new QueryClass(Protein.class);
		QueryField qfValue = new QueryField(qcSynonym, "value");
		QueryField qfPrimaryId = new QueryField(qcProtein, "primaryAccession");
		q.addFrom(qcSynonym);
		q.addFrom(qcProtein);
		q.addToSelect(qfValue);
		q.addToSelect(qfPrimaryId);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		QueryCollectionReference synRef = new QueryCollectionReference(qcProtein, "synonyms");
		cs.addConstraint(new ContainsConstraint(synRef, ConstraintOp.CONTAINS, qcSynonym));

		cs.addConstraint(new SimpleConstraint(qfValue, ConstraintOp.MATCHES, new QueryValue("NP_%")));

		q.setConstraint(cs);

		ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);

		Results results = os.execute(q);
		Iterator<Object> iterator = results.iterator();
		while (iterator.hasNext()) {
			ResultsRow<String> rr = (ResultsRow<String>) iterator.next();
			String refseqId = rr.get(0);
			if (refseqId.contains(".")){
				refseqId = refseqId.substring(0, refseqId.indexOf("."));
			}
			if (proteinIdMap.get(refseqId) == null) {
				proteinIdMap.put(refseqId, new HashSet<String>());
			}
			proteinIdMap.get(refseqId).add(rr.get(1));
		}
	}

	private Map<String, String> proteinMap = new HashMap<String, String>();

	private String getProtein(String uniprotId, String taxonField) throws ObjectStoreException {
		String ret = proteinMap.get(uniprotId);

		if (ret == null) {
			Item protein = createItem("Protein");
			protein.setAttribute("primaryAccession", uniprotId);
			if (!taxonField.equals("-")) {
				String taxonId = taxonField.substring(6, taxonField.indexOf("("));
				protein.setReference("organism", getOrganism(taxonId));
			}
			ret = protein.getIdentifier();
			proteinMap.put(uniprotId, ret);
			store(protein);
		}
		return ret;
	}

	private Map<String, String> piSourceMap = new HashMap<String, String>();

	private String getPiSource(String sourceDb, String sourceId) throws ObjectStoreException {
		String ret = piSourceMap.get(sourceDb + "-" + sourceId);

		if (ret == null) {
			Item piSource = createItem("ProteinInteractionSource");
			piSource.setAttribute("dbName", sourceDb);
			piSource.setAttribute("identifier", sourceId);

			ret = piSource.getIdentifier();
			piSourceMap.put(sourceDb + "-" + sourceId, ret);
			store(piSource);
		}
		return ret;
	}

}
