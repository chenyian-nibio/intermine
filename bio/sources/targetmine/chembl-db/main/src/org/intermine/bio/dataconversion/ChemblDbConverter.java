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
				+ " from compound_records as cr "
				+ " join activities as act on act.molregno=cr.molregno "
				+ " join assays as ass on ass.assay_id=act.assay_id "
				+ " join assay2target as a2t on a2t.assay_id=ass.assay_id "
				+ " where compound_name is not null " + " and standard_type  = 'IC50' "
				+ " and standard_units = 'nM' " + " and curated_by = 'Expert' ";
		ResultSet resName = stmt.executeQuery(queryName);
		while (resName.next()) {
			String molId = String.valueOf(resName.getInt("cr.molregno"));
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
				+ " act.standard_value, td.protein_accession, docs.pubmed_id, "
				+ " cs.standard_inchi_key " + " from activities as act "
				+ " join molecule_dictionary as md on md.molregno=act.molregno "
				+ " join assays as ass on ass.assay_id=act.assay_id "
				+ " join assay2target as a2t on a2t.assay_id=ass.assay_id "
				+ " join target_dictionary as td on td.tid=a2t.tid "
				+ " join docs on docs.doc_id=ass.doc_id "
				+ " join compound_structures as cs on cs.molregno=md.molregno "
				+ " where standard_type = 'IC50' " + " and standard_units = 'nM' "
				+ " and protein_accession is not null " + " and curated_by = 'Expert' ";
		ResultSet resInteraction = stmt.executeQuery(queryInteraction);
		int i = 0;
		while (resInteraction.next()) {
			String molId = String.valueOf(resInteraction.getInt("md.molregno"));
			String chemblId = resInteraction.getString("md.chembl_id");
			String type = resInteraction.getString("md.molecule_type");
			String uniprotId = resInteraction.getString("td.protein_accession");
			String pubmedId = String.valueOf(resInteraction.getInt("docs.pubmed_id"));
			String inchiKey = String.valueOf(resInteraction.getString("cs.standard_inchi_key"));
			float ic50 = resInteraction.getFloat("act.standard_value");
			Item item = createItem("ChemblInteraction");
			item.setAttribute("ic50", String.valueOf(ic50));
			item.setReference("publication", getPublication(pubmedId));
			item.setReference("protein", getProtein(uniprotId));

			String compoundRef = compoundMap.get(chemblId);
			if (compoundRef == null) {
				Item compound = createItem("ChemblCompound");
				compound.setAttribute("chemblId", chemblId);
				compound.setAttribute("identifier", String.format("ChEMBL: %s", chemblId));
				compound.setAttribute("inchiKey", inchiKey);
				String name = nameMap.get(molId);
				if (name == null) {
					name = chemblId;
				}
				compound.setAttribute("name", name);
				compound.addToCollection("drugTypes", getDrugType(drugTypeTranslateMap.get(type)));
				String compoundGroupId = inchiKey.substring(0, inchiKey.indexOf("-"));
				if (compoundGroupId.length() == 14) {
					compound.setReference("compoundGroup", getCompoundGroup(compoundGroupId, name));
				} else {
					LOG.error(String.format("Bad InChIKey value: %s, %s .", chemblId, inchiKey));
				}
				Set<String> synonyms = synonymMap.get(molId);
				if (synonyms != null) {
					for (String s : synonyms) {
						Item bn = createItem("CompoundSynonym");
						bn.setAttribute("value", s);
						bn.setReference("compound", compound);
						store(bn);
					}
				}

				Set<String> tradeNames = drugMap.get(molId);
				if (tradeNames != null) {
					compound.addToCollection("drugTypes", getDrugType("approved"));
					for (String tn : tradeNames) {
						if (!synonyms.contains(tn)) {
							Item bn = createItem("CompoundSynonym");
							bn.setAttribute("value", tn);
							bn.setReference("compound", compound);
							store(bn);
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
			i++;
		}
		System.out.println(i + "ChEMBL interaction were integrated.");
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
}
