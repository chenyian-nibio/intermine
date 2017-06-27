package org.intermine.bio.dataconversion;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.sql.Database;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class ChemblDbConverter extends BioDBConverter {
	private static final Logger LOG = Logger.getLogger(ChemblDbConverter.class);
	//
	private static final String DATASET_TITLE = "ChEMBL";
	private static final String DATA_SOURCE_NAME = "ChEMBL";

	private Map<String, Set<String>> drugMap = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> synonymMap = new HashMap<String, Set<String>>();

	private Map<String, String> proteinMap = new HashMap<String, String>();
	private Map<String, String> compoundMap = new HashMap<String, String>();
	private Map<String, String> publicationMap = new HashMap<String, String>();
	private Map<String, String> compoundGroupMap = new HashMap<String, String>();
	private Map<String, String> drugTypeMap = new HashMap<String, String>();
	private Map<String, String> interactionMap = new HashMap<String, String>();

//	private Map<String, String> drugTypeTranslateMap = new HashMap<String, String>();

	/**
	 * Construct a new ChemblDbConverter.
	 * 
	 * @param database
	 *            the database to read from
	 * @param model
	 *            the Model used by the object store we will write to with the ItemWriter
	 * @param writer
	 *            an ItemWriter used to handle Items created
	 */
	public ChemblDbConverter(Database database, Model model, ItemWriter writer) {
		super(database, model, writer, DATA_SOURCE_NAME, DATASET_TITLE);
//		drugTypeTranslateMap.put("Protein", "biotech");
//		drugTypeTranslateMap.put("Small molecule", "small molecule");
	}

	/**
	 * {@inheritDoc}
	 */
	public void process() throws Exception {

		Connection connection = getDatabase().getConnection();

		Statement stmt = connection.createStatement();
		String queryName = "select distinct cr.molregno, compound_name "
				+ " from compound_records as cr " + " where compound_name is not null ";
		ResultSet resName = stmt.executeQuery(queryName);
		while (resName.next()) {
			String molId = String.valueOf(resName.getInt("molregno"));
			String name = resName.getString("compound_name");
			// also save these names as synonyms
			if (synonymMap.get(molId) == null) {
				synonymMap.put(molId, new HashSet<String>());
			}
			synonymMap.get(molId).add(name);
		}

		String queryDrug = "select distinct molregno, trade_name " + " from formulations as fo "
				+ " join products on fo.product_id=products.product_id "
				+ " where approval_date is not null";
		ResultSet resDrug = stmt.executeQuery(queryDrug);
		while (resDrug.next()) {
			String molId = String.valueOf(resDrug.getInt("molregno"));
			String tradeName = resDrug.getString("trade_name");
			if (drugMap.get(molId) == null) {
				drugMap.put(molId, new HashSet<String>());
			}
			drugMap.get(molId).add(tradeName);
		}
		String querySynonym = "select distinct molregno, synonyms "
				+ " from molecule_synonyms where syn_type != 'RESEARCH_CODE'";
		ResultSet resSynonym = stmt.executeQuery(querySynonym);
		while (resSynonym.next()) {
			String molId = String.valueOf(resSynonym.getInt("molregno"));
			String synonym = resSynonym.getString("synonyms");
			if (synonymMap.get(molId) == null) {
				synonymMap.put(molId, new HashSet<String>());
			}
			synonymMap.get(molId).add(synonym);
		}
		
		// create chembl compound entries for all compound
		String queryMolecule = " select md.molregno, md.pref_name, md.chembl_id, md.molecule_type, cs.standard_inchi_key, cs.standard_inchi, canonical_smiles "
				+ " from molecule_dictionary as md "
				+ " left join compound_structures as cs on cs.molregno=md.molregno ";
		ResultSet resMolecule = stmt.executeQuery(queryMolecule);
		int c = 0;
		while (resMolecule.next()) {
			String molId = String.valueOf(resMolecule.getInt("molregno"));
			String chemblId = resMolecule.getString("chembl_id");
			String inchiKey = String.valueOf(resMolecule.getString("standard_inchi_key"));
			String inchi = String.valueOf(resMolecule.getString("standard_inchi"));
			String smiles = String.valueOf(resMolecule.getString("canonical_smiles"));
			String moleculeType = resMolecule.getString("molecule_type");
			
			Map<String,String> structureMap = new HashMap<String, String>();
			if (inchi != null && !"".equals(inchi)) {
				structureMap.put("InChI", inchi);
			}
			if (smiles != null && !"".equals(smiles)) {
				structureMap.put("SMILES", smiles);
			}

			String name = String.valueOf(resMolecule.getString("pref_name"));
			if (name == null || name.equals("null")) {
				name = chemblId;
			}
			
			String compoundRef = compoundMap.get(chemblId);
			if (compoundRef == null) {
				Item compound = createItem("ChemblCompound");
				compound.setAttribute("originalId", chemblId);
				compound.setAttribute("identifier", String.format("ChEMBL:%s", chemblId));

				// if the length of the name is greater than 40 characters,
				// use id instead and save the long name as the synonym
				if (name.length() > 40) {
					name = chemblId;
					if (synonymMap.get(molId) == null) {
						synonymMap.put(molId, new HashSet<String>());
					}
//					synonymMap.get(molId).add(name);
				}
				compound.setAttribute("name", name);

//				String drugType = drugTypeTranslateMap.get(moleculeType);
//				if (!StringUtils.isEmpty(drugType)) {
//					compound.addToCollection("drugTypes", getDrugType(drugType));
//				}
				if (!StringUtils.isEmpty(moleculeType) && !moleculeType.equals("Unclassified") && !moleculeType.equals("Unknown")) {
					compound.addToCollection("drugTypes", getDrugType(moleculeType.toLowerCase()));
				}
				
				if (!inchiKey.equals("null")) {
					compound.setAttribute("inchiKey", inchiKey);
					String compoundGroupId = inchiKey.substring(0, inchiKey.indexOf("-"));
					if (compoundGroupId.length() == 14) {
						compound.setReference("compoundGroup",
								getCompoundGroup(compoundGroupId, name));
					} else {
						LOG.error(String.format("Bad InChIKey value: %s, %s .", chemblId, inchiKey));
					}
				}
				
				Set<String> synonyms = synonymMap.get(molId);
				if (synonyms != null) {
					for (String s : synonyms) {
						setSynonyms(compound, s);
					}
				}

				Set<String> tradeNames = drugMap.get(molId);
				if (tradeNames != null) {
					compound.addToCollection("drugTypes", getDrugType("approved"));
					for (String tn : tradeNames) {
						if (!synonyms.contains(tn)) {
							setSynonyms(compound, tn);
						}
					}
				}

				store(compound);
				
				for (String key: structureMap.keySet()) {
					Item structure = createItem("CompoundStructure");
					structure.setAttribute("type", key);
					structure.setAttribute("value", structureMap.get(key));
					structure.setReference("compound", compound);
					store(structure);
				}
				
				compoundRef = compound.getIdentifier();
				compoundMap.put(chemblId, compoundRef);
				c++;
			}
		}
		LOG.info(c + "ChEMBL compounds were stored.");

		// query for interactions
		String queryInteraction = " select distinct md.chembl_id, "
				+ " act.standard_type, act.standard_value, cseq.accession, cseq.tax_id, docs.pubmed_id, "
				+ " ass.chembl_id as assay_id, ass.description " + " from activities as act "
				+ " join molecule_dictionary as md on md.molregno=act.molregno "
				+ " join assays as ass on ass.assay_id=act.assay_id "
				+ " join target_dictionary as td on td.tid=ass.tid "
				+ " join target_components as tc on tc.tid=ass.tid "
				+ " join component_sequences as cseq on cseq.component_id=tc.component_id "
				+ " join docs on docs.doc_id=ass.doc_id "
				+ " where ass.confidence_score >= 4 " + " and ass.assay_type = 'B' "
				+ " and td.target_type = 'SINGLE PROTEIN' "
				+ " and act.standard_type in ('IC50','Kd','Ki','EC50','AC50') "
				+ " and act.standard_value <= 10000 " + " and act.standard_relation = '=' "
				+ " and act.standard_units = 'nM' ";
		ResultSet resInteraction = stmt.executeQuery(queryInteraction);
		int i = 0;
		while (resInteraction.next()) {
			String chemblId = resInteraction.getString("chembl_id");
			String uniprotId = resInteraction.getString("accession");
			String pubmedId = String.valueOf(resInteraction.getInt("pubmed_id"));
			String standardType = resInteraction.getString("standard_type");
			float conc = resInteraction.getFloat("standard_value");
			String assayId = resInteraction.getString("assay_id");
			String assayDesc = resInteraction.getString("description");

			String intId = uniprotId + "-" + chemblId;
			String interactionRef = interactionMap.get(intId);
			if (interactionRef == null) {
				String compoundRef = compoundMap.get(chemblId);
				if (compoundRef == null) {
					LOG.info("Unfound ChEMBL compound: " + chemblId);
					continue;
				}

				Item interactionItem = createItem("ChemblInteraction");
				interactionItem.setReference("protein", getProtein(uniprotId));
				interactionItem.setReference("compound", compoundRef);

				store(interactionItem);
				interactionRef = interactionItem.getIdentifier();
				interactionMap.put(intId, interactionRef);
				i++;
			}
			String assayRef = getCompoundProteinInteractionAssay(assayId, assayDesc, pubmedId);

			Item activity = createItem("Activity");
			activity.setAttribute("type", standardType);
			activity.setAttribute("conc", String.valueOf(conc));
			activity.setReference("assay", assayRef);
			activity.setReference("interaction", interactionRef);
			store(activity);
		}
		// System.out.println(i + "ChEMBL interaction were integrated.");
		LOG.info(i + "ChEMBL interaction were integrated.");
		
		stmt.close();
		connection.close();
	}

	Map<String, String> assayMap = new HashMap<String, String>();

	private String getCompoundProteinInteractionAssay(String identifier, String name, String pubmedId)
			throws ObjectStoreException {
		String ret = assayMap.get(identifier);
		if (ret == null) {
			Item item = createItem("CompoundProteinInteractionAssay");
			item.setAttribute("identifier", identifier.toLowerCase());
			item.setAttribute("originalId", identifier);
			item.setAttribute("name", name);
			item.setAttribute("source", "ChEMBL");
			item.addToCollection("publications", getPublication(pubmedId));
			store(item);
			ret = item.getIdentifier();
			assayMap.put(identifier, ret);
		}
		return ret;
	}

	private String getProtein(String uniprotId) throws ObjectStoreException {
		String ret = proteinMap.get(uniprotId);
		if (ret == null) {
			Item item = createItem("Protein");
			item.setAttribute("primaryAccession", uniprotId);
			store(item);
			ret = item.getIdentifier();
			proteinMap.put(uniprotId, ret);
		}
		return ret;
	}

	private String getPublication(String pubMedId) throws ObjectStoreException {
		String ret = publicationMap.get(pubMedId);
		if (ret == null) {
			Item item = createItem("Publication");
			item.setAttribute("pubMedId", pubMedId);
			store(item);
			ret = item.getIdentifier();
			publicationMap.put(pubMedId, ret);
		}
		return ret;
	}

	private String getDrugType(String name) throws ObjectStoreException {
		String ret = drugTypeMap.get(name);
		if (ret == null) {
			Item item = createItem("DrugType");
			item.setAttribute("name", name);
			store(item);
			ret = item.getIdentifier();
			drugTypeMap.put(name, ret);
		}
		return ret;
	}

	private String getCompoundGroup(String inchiKey, String name) throws ObjectStoreException {
		String ret = compoundGroupMap.get(inchiKey);
		if (ret == null) {
			Item item = createItem("CompoundGroup");
			item.setAttribute("identifier", inchiKey);
			item.setAttribute("name", name);
			store(item);
			ret = item.getIdentifier();
			compoundGroupMap.put(inchiKey, ret);
		}
		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDataSetTitle(int taxonId) {
		return DATASET_TITLE;
	}

	private void setSynonyms(Item subject, String value) throws ObjectStoreException {
		Item syn = createItem("CompoundSynonym");
		syn.setAttribute("value", value);
		syn.setReference("subject", subject);
		store(syn);
	}
}
