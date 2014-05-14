package org.intermine.bio.dataconversion;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

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
	private static final String DATA_SOURCE_NAME = "EMBL-EBI";

	private Map<String, String> nameMap = new HashMap<String, String>();
	private Map<String, Set<String>> drugMap = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> synonymMap = new HashMap<String, Set<String>>();

	private Map<String, String> proteinMap = new HashMap<String, String>();
	private Map<String, String> compoundMap = new HashMap<String, String>();
	private Map<String, String> publicationMap = new HashMap<String, String>();
	private Map<String, String> compoundGroupMap = new HashMap<String, String>();
	private Map<String, String> drugTypeMap = new HashMap<String, String>();
	private Map<String, String> interactionMap = new HashMap<String, String>();

	private Map<String, String> drugTypeTranslateMap = new HashMap<String, String>();

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
		drugTypeTranslateMap.put("Protein", "biotech");
		drugTypeTranslateMap.put("Small molecule", "small molecule");
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
			String value = nameMap.get(molId);
			// take the shortest name (no reason; arbitrary)
			if (value == null || value.length() > name.length()) {
				nameMap.put(molId, name);
			}
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

		String queryInteraction = " select distinct md.molregno, md.chembl_id, md.molecule_type, "
				+ " act.standard_type, act.standard_value, cseq.accession, cseq.tax_id, docs.pubmed_id, "
				+ " cs.standard_inchi_key, ass.chembl_id as assay_id, ass.description " + " from activities as act "
				+ " join molecule_dictionary as md on md.molregno=act.molregno "
				+ " join assays as ass on ass.assay_id=act.assay_id "
				+ " join target_dictionary as td on td.tid=ass.tid "
				+ " join target_components as tc on tc.tid=ass.tid "
				+ " join component_sequences as cseq on cseq.component_id=tc.component_id "
				+ " join docs on docs.doc_id=ass.doc_id "
				+ " join compound_structures as cs on cs.molregno=md.molregno "
				+ " where ass.confidence_score >= 4 " + " and ass.assay_type = 'B' "
				+ " and td.target_type = 'SINGLE PROTEIN' "
				+ " and act.standard_type in ('IC50','Kd','Ki') "
				+ " and act.standard_value < 10000 " + " and act.standard_relation = '=' "
				+ " and act.standard_units = 'nM' ";
		ResultSet resInteraction = stmt.executeQuery(queryInteraction);
		int i = 0;
		while (resInteraction.next()) {
			String molId = String.valueOf(resInteraction.getInt("molregno"));
			String chemblId = resInteraction.getString("chembl_id");
			String moleculeType = resInteraction.getString("molecule_type");
			String uniprotId = resInteraction.getString("accession");
			String pubmedId = String.valueOf(resInteraction.getInt("pubmed_id"));
			String inchiKey = String.valueOf(resInteraction.getString("standard_inchi_key"));
			String standardType = resInteraction.getString("standard_type");
			float conc = resInteraction.getFloat("standard_value");
			String assayId = resInteraction.getString("assay_id");
			String assayDesc = resInteraction.getString("description");

			String intId = uniprotId + "-" + chemblId;
			String interactionRef = interactionMap.get(intId);
			if (interactionRef == null) {

				Item item = createItem("ChemblInteraction");
				item.setReference("protein", getProtein(uniprotId));

				String compoundRef = compoundMap.get(chemblId);
				if (compoundRef == null) {
					Item compound = createItem("ChemblCompound");
					compound.setAttribute("chemblId", chemblId);
					compound.setAttribute("primaryIdentifier", String.format("ChEMBL:%s", chemblId));
					compound.setAttribute("secondaryIdentifier", chemblId);
					compound.setAttribute("inchiKey", inchiKey);

					// assign inchikey as synonym
					setSynonyms(compound, inchiKey);

					String name = nameMap.get(molId);
					if (name == null) {
						name = chemblId;
					}
					// if the length of the name is greater than 40 characters,
					// use id instead and save the long name as the synonym
					if (name.length() > 40) {
						setSynonyms(compound, name);
						name = chemblId;
					}
					compound.setAttribute("name", name);

					String drugType = drugTypeTranslateMap.get(moleculeType);
					if (!StringUtils.isEmpty(drugType)) {
						compound.addToCollection("drugTypes", getDrugType(drugType));
					}
					String compoundGroupId = inchiKey.substring(0, inchiKey.indexOf("-"));
					if (compoundGroupId.length() == 14) {
						compound.setReference("compoundGroup",
								getCompoundGroup(compoundGroupId, name));
					} else {
						LOG.error(String.format("Bad InChIKey value: %s, %s .", chemblId, inchiKey));
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
					compoundRef = compound.getIdentifier();
					compoundMap.put(chemblId, compoundRef);
					// LOG.info(chemblId +"; "+inchiKey+"; "+name+"; "+type);
				}
				item.setReference("compound", compoundRef);

				store(item);
				interactionRef = item.getIdentifier();
				interactionMap.put(intId, interactionRef);
				i++;
			}
			Item assay = getCompoundProteinInteractionAssay(assayId, assayDesc, pubmedId);
			assay.addToCollection("interactions", interactionRef);

			Item activity = createItem("Activity");
			activity.setAttribute("type", standardType);
			activity.setAttribute("conc", String.valueOf(conc));
			activity.setReference("assay", assay);
			store(activity);
		}
		// System.out.println(i + "ChEMBL interaction were integrated.");
		LOG.info(i + "ChEMBL interaction were integrated.");
	}

	Map<String, Item> assayMap = new HashMap<String, Item>();

	private Item getCompoundProteinInteractionAssay(String identifier, String name, String pubmedId)
			throws ObjectStoreException {
		Item ret = assayMap.get(identifier);
		if (ret == null) {
			ret = createItem("CompoundProteinInteractionAssay");
			ret.setAttribute("identifier", identifier.toLowerCase());
			ret.setAttribute("originalId", identifier);
			ret.setAttribute("name", name);
			ret.setAttribute("source", "ChEMBL");
			ret.addToCollection("publications", getPublication(pubmedId));
			assayMap.put(identifier, ret);
		}
		return ret;
	}

	@Override
	public void close() throws Exception {
		store(assayMap.values());
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
		Item syn = createItem("Synonym");
		syn.setAttribute("value", value);
		syn.setReference("subject", subject);
		store(syn);
	}
}
