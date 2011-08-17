package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;

public class DrugBankConverter extends BioFileConverter {
	
	private static Logger m_oLogger = Logger.getLogger(DrugBankConverter.class);

	private static final String DATASET_TITLE = "DrugBank";
	private static final String DATA_SOURCE_NAME = "DrugBank";
	
	private enum HEADER {
		AHFS_Codes,
		ATC_Codes,
		Absorption,
		Biotransformation,
		Brand_Mixtures,
		Brand_Names,
		CAS_Registry_Number,
		ChEBI_ID,
		Chemical_Formula,
		Chemical_IUPAC_Name,
		Chemical_Structure,
		Creation_Date,
		DPD_Drug_ID_Number,
		Description,
		Dosage_Forms,
		Drug_Category,
		Drug_Interactions,
		Drug_Reference,
		Drug_Type,
		Experimental_Caco2_Permeability,
		Experimental_LogP_Hydrophobicity,
		Experimental_Logs,
		Experimental_Water_Solubility,
		FDA_Label_Files,
		Food_Interactions,
		GenBank_ID,
		Generic_Name,
		HET_ID,
		Half_Life,
		InChI_Identifier,
		InChI_Key,
		Indication,
		KEGG_Compound_ID,
		KEGG_Drug_ID,
		LIMS_Drug_ID,
		MSDS_Files,
		Mass_Spec_File,
		Mechanism_Of_Action,
		Melting_Point,
		Molecular_Weight_Avg,
		Molecular_Weight_Mono,
		Organisms_Affected,
		PDB_Experimental_ID,
		PDB_Homology_ID,
		PDRhealth_Link,
		PharmGKB_ID,
		Pharmacology,
		Predicted_LogP_Hydrophobicity,
		Predicted_LogS,
		Predicted_Water_Solubility,
		Primary_Accession_No,
		Protein_Binding,
		PubChem_Compound_ID,
		PubChem_Substance_ID,
		RxList_Link,
		Secondary_Accession_No,
		Smiles_String_canonical,
		Smiles_String_isomeric,
		State,
		Structure,
		SwissProt_ID,
		SwissProt_Name,
		Synonyms,
		Synthesis_Reference,
		TargetDrug_References,
		TargetSwissProtId,
		Toxicity,
		Update_Date,
		Wikipedia_Link,
		contraindication_insert,
		interaction_insert,
		pKa_Isoelectric_Point,
		patient_information_insert,
		NONE
	}
	
	// key is SwissProt primary accession, value is corresponding Protein Item
	private Map<String, Item> m_oProteinMap = new TreeMap<String, Item>();
	
	// key is PubMed ID, value is curresponding Publication Item.
	private Map<String, Item> m_oPublicationMap = new TreeMap<String, Item>();
	
	// key is header string, value is HEADER enum
	private Map<String, HEADER> oMap = createHeaderMap();
	
	// parser (from String to java.util.Date)
//	private SimpleDateFormat m_oFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	// variables below are for retaining data of ONE drug cards.
	
	// Which column is currently processed.
	private HEADER m_eCurrent = HEADER.NONE;
	
	private ArrayList<String> m_oBrandNames;
	
	private String m_oCasRegNo;
	
	private StringBuffer m_oDescription;
	
	private String m_oFdaLabel;
	
	private String m_oGenericName;
	
	private String m_oHetId;
	
	private String m_oKeggDrugId;

	private String m_oPubChemCid;

	private String m_oChebiId;

	private String m_oInchiKey;
	
	private String m_oProteinId;
	
	private String m_oPrimaryAccNo;
	
	// Number of target (# Drug_Target_1_GenAtlas_ID: -> 1)
	private String m_strTargetNum;
	
	// Map for target information. key is target number, value is TargetContainer
	private Map<String, TargetContainer> m_oTargetMap;

	private List<String> m_oDrugType;

	private Map<String, String> drugTypeMap = new HashMap<String, String>();

	private Map<String, String> hetGroupMap = new HashMap<String, String>();

	private Map<String, String> chebiCompoundMap = new HashMap<String, String>();

	private Map<String, String> pubChemMap = new HashMap<String, String>();

	private Map<String, String> compoundGroupMap = new HashMap<String, String>();
	
	public DrugBankConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}
	
	@Override
	public void process(Reader reader) throws Exception {
		
		// Initialize member variables
		initialize();
		
		BufferedReader oBr = new BufferedReader(reader);
		
		while(oBr.ready()) {
			
			String strLine = oBr.readLine();
			
			if(null == strLine || "".equals(strLine)) {
				continue;
			}
			
			if(strLine.startsWith("#")) {
				processHeader(strLine);
			} else {
				processData(strLine);
			}
			
		}
	}
	
	private void initialize() {
		
		m_eCurrent = HEADER.NONE;
		m_oBrandNames = new ArrayList<String>();
		m_oCasRegNo = null;
		m_oDescription = new StringBuffer();
		m_oFdaLabel = null;
		m_oGenericName = null;
		m_oHetId = null;
		m_oKeggDrugId = null;
		m_oPubChemCid = null;
		m_oChebiId = null;
		m_oInchiKey = null;
		m_oProteinId = null;
		m_oPrimaryAccNo = null;
		m_strTargetNum = null;
		m_oTargetMap = new TreeMap<String, TargetContainer>();
		m_oDrugType = new ArrayList<String>();
	}
	
	private void processHeader(String strLine) throws ObjectStoreException {
		
		if(strLine.startsWith("# Drug_Target")) {
			
			String[] splits = strLine.split("_");
			m_strTargetNum = splits[2];
			
			// prepare TargetContainer object in map
			if( !m_oTargetMap.containsKey(m_strTargetNum) ) {
				m_oTargetMap.put(m_strTargetNum, new TargetContainer());
			}
			
			if( strLine.endsWith("_SwissProt_ID:") ) {
				
				m_eCurrent = HEADER.TargetSwissProtId;
				
			} else if( strLine.endsWith("_Drug_References:") ) {
				
				m_eCurrent = HEADER.TargetDrug_References;
				
			} else {
				
				m_eCurrent = HEADER.NONE;
				
			}
			
		} if(strLine.startsWith("#END")) {
			
			// register data
			Item oDrug = createItem("Drug");
			for( String strBrandName : m_oBrandNames ) {
				oDrug.addToCollection( "brandNames", getSynonym(strBrandName) );
			}
			oDrug.setAttribute( "drugBankId", m_oPrimaryAccNo );

			// chenyian: set the compound identifier
			oDrug.setAttribute("identifier", String.format("DrugBank: %s", m_oPrimaryAccNo));
			
			if( m_oFdaLabel != null){
				oDrug.setAttribute( "fdaLabelIssuedDate", m_oFdaLabel );
			}
			oDrug.setAttribute("description", m_oDescription.toString());
			oDrug.setAttribute( "genericName", m_oGenericName );
			// chenyian: Compound's attribute
			oDrug.setAttribute( "name", m_oGenericName );
			if (notEmpty(m_oKeggDrugId)){
				oDrug.setAttribute("keggDrugId", m_oKeggDrugId);
			}
			if (notEmpty(m_oCasRegNo)){
				oDrug.setAttribute( "casRegistryNumber", m_oCasRegNo );
			}
			if (notEmpty(m_oHetId)){
				oDrug.setReference("hetGroup", getHetGroup(m_oHetId));
			}

			// chenyian: 
			if (notEmpty(m_oChebiId)){
				oDrug.setReference("chebiCompound", getChebiCompound(m_oChebiId));
			}
			// chenyian: 
			if (notEmpty(m_oPubChemCid)){
				oDrug.setReference("pubChemCompound", getPubChemCompound(m_oPubChemCid));
			}
			// chenyian: 
			if (notEmpty(m_oInchiKey)){
				String inchiKey = m_oInchiKey.substring(m_oInchiKey.indexOf("=")+1, m_oInchiKey.indexOf("-"));
				if (inchiKey.length() != 14) {
					m_oLogger.info(String.format("Bad InChIKey value: %s, %s .", m_oPrimaryAccNo, m_oInchiKey));
				} else {
					oDrug.setReference("compoundGroup", getCompoundGroup(inchiKey, m_oGenericName));
				}
			}
			
			if (notEmpty(m_oProteinId)){
				oDrug.setReference("protein", getProtein(m_oProteinId));
			}
						
			for(Map.Entry<String, TargetContainer> oTarget : m_oTargetMap.entrySet()) {
				
				m_oLogger.debug( " drug-target: " + m_oPrimaryAccNo + " " + oTarget.getKey() + " primaryAcc=" + oTarget.getValue().getPrimaryAccNo() );
				
				// If SwissProtId of a target is "Not Available", it is ignored.
				// These target may be not protein target, such as DNA
				if( "Not Available".equals(oTarget.getValue().getPrimaryAccNo()) ) {
					continue;
				}
				
				Item oProtein = getProtein(oTarget.getValue().getPrimaryAccNo());
				
				Item oInteraction = createItem("DrugProteinInteraction");
				oInteraction.setReference("protein", oProtein);
				oInteraction.setReference("drug", oDrug);
				
				for(String strPubMedId : oTarget.getValue().getDrugReferences()) {
					oInteraction.addToCollection("publications", getPublication(strPubMedId));
				}
				
				store(oInteraction);
				
			}
			
			// chenyian: add drug_type
			for (String type : m_oDrugType) {
				String trimed = type.trim();
				oDrug.addToCollection("drugTypes", getDrugType(trimed));
			}
			
			store(oDrug);
			
			// initialize variables;
			initialize();
			
		} else {
			if(oMap.containsKey(strLine)) {
				m_eCurrent = oMap.get(strLine);
			}else {
				m_oLogger.debug(" **" + strLine + " doesn't exist in the map");
			}
		}
	}

	private boolean notEmpty(String string) {
		return string != null && !string.equals("Not Available");
	}
	
	private String getDrugType(String type) throws ObjectStoreException {
		String ret = drugTypeMap.get(type);
		if (ret == null) {
			Item item = createItem("DrugType");
			item.setAttribute("name", type);
			store(item);
			ret = item.getIdentifier();
			drugTypeMap.put(type, ret);
		}
		return ret;
	}

	private String getHetGroup(String hetId) throws ObjectStoreException {
		String ret = hetGroupMap.get(hetId);
		if (ret == null) {
			Item item = createItem("HetGroup");
			item.setAttribute("hetId", hetId);
			item.setAttribute("identifier", String.format("HetGroup: %s", hetId));
			store(item);
			ret = item.getIdentifier();
			hetGroupMap.put(hetId, ret);
		}
		return ret;
	}
	
	private String getChebiCompound(String chebiId) throws ObjectStoreException {
		String ret = chebiCompoundMap.get(chebiId);
		if (ret == null) {
			Item item = createItem("ChebiCompound");
			item.setAttribute("chebiId", chebiId);
			item.setAttribute("identifier", String.format("CHEBI: %s", chebiId));
			store(item);
			ret = item.getIdentifier();
			chebiCompoundMap.put(chebiId, ret);
		}
		return ret;
	}

	private String getPubChemCompound(String pubChemCid) throws ObjectStoreException {
		String ret = pubChemMap.get(pubChemCid);
		if (ret == null) {
			Item item = createItem("PubChemCompound");
			item.setAttribute("pubChemCid", pubChemCid);
			item.setAttribute("identifier", String.format("PubChem: %s", pubChemCid));
			store(item);
			ret = item.getIdentifier();
			pubChemMap.put(pubChemCid, ret);
		}
		return ret;
	}
	
	private String getCompoundGroup(String inchiKey, String name) throws ObjectStoreException {
		String ret = compoundGroupMap.get(inchiKey);
		if (ret == null) {
			Item item = createItem("CompoundGroup");
			item.setAttribute("identifier", inchiKey);
			// chenyian: randomly pick one name 
			item.setAttribute("name", name);
			store(item);
			ret = item.getIdentifier();
			compoundGroupMap.put(inchiKey, ret);
		}
		return ret;
	}

	private void processData(String strLine) throws ParseException {
		
		switch(m_eCurrent) {
			case Brand_Names:
				m_oBrandNames.add(strLine);
				break;
				
			case CAS_Registry_Number:
				m_oCasRegNo = strLine;
				break;
				
			case Description:
				m_oDescription.append(strLine);
				break;
				
			case FDA_Label_Files:
				if( ! "Not Available".equals(strLine) ){
					String[] columns = strLine.split("\t");
					m_oFdaLabel = columns[0];
				}
				break;
				
			case Generic_Name:
				m_oGenericName = strLine;
				break;
				
			case HET_ID:
				m_oHetId = strLine;
				break;
				
			case KEGG_Drug_ID:
				m_oKeggDrugId = strLine;
				break;

			// chenyian: add pubmed cid (for the integration of stitch data)
			case PubChem_Compound_ID:
				m_oPubChemCid = strLine;
				break;
			case ChEBI_ID:
				m_oChebiId = strLine;
				break;
			case InChI_Key:
				m_oInchiKey = strLine;
				break;
			case SwissProt_ID:
				m_oProteinId = strLine;
				break;
				
			case Primary_Accession_No:
				m_oPrimaryAccNo = strLine;
				break;
				
			case TargetSwissProtId:				
				m_oTargetMap.get(m_strTargetNum).setPrimaryAccNo(strLine);
				break;
			
			case TargetDrug_References:
				String[] a_strFields = strLine.split("\t");
				m_oTargetMap.get(m_strTargetNum).addDrugReference(a_strFields[0]);
				break;

			// chenyian: add drug_type
			case Drug_Type:				
				m_oDrugType.add(strLine);
				break;
		}
		
	}
	
	private Item getProtein(String strPrimaryAccession) throws ObjectStoreException {
		
		if(!m_oProteinMap.containsKey(strPrimaryAccession)) {
			
			Item oDrugTarget = createItem("Protein");
			oDrugTarget.setAttribute("primaryAccession", strPrimaryAccession);
			oDrugTarget.setAttribute("uniprotAccession", strPrimaryAccession);
			store(oDrugTarget);
			m_oProteinMap.put(strPrimaryAccession, oDrugTarget);
			
		}
		
		return m_oProteinMap.get(strPrimaryAccession);
		
	}
	
	private Item getPublication(String strPubMedId) throws ObjectStoreException {
		
		if(!m_oPublicationMap.containsKey(strPubMedId)) {
			
			Item oPublication = createItem("Publication");
			oPublication.setAttribute("pubMedId", strPubMedId);
			store(oPublication);
			m_oPublicationMap.put(strPubMedId, oPublication);
			
		}
		
		return m_oPublicationMap.get(strPubMedId);
		
	}
	
	private Item getSynonym(String strSynonym) throws ObjectStoreException {
		
		Item oSynonym = createItem("Synonym");
//		oSynonym.setAttribute("type", "drug brand name");
		oSynonym.setAttribute("value", strSynonym);
		store(oSynonym);
		
		return oSynonym;
						
	}
	
	private Map<String, HEADER> createHeaderMap() {
		
		Map<String, HEADER> oMap = new TreeMap<String, HEADER>();
		oMap.put("# AHFS_Codes:", HEADER.AHFS_Codes);
		oMap.put("# ATC_Codes:", HEADER.ATC_Codes);
		oMap.put("# Absorption:", HEADER.Absorption);
		oMap.put("# Biotransformation:", HEADER.Biotransformation);
		oMap.put("# Brand_Mixtures:", HEADER.Brand_Mixtures);
		oMap.put("# Brand_Names:", HEADER.Brand_Names);
		oMap.put("# CAS_Registry_Number:", HEADER.CAS_Registry_Number);
		oMap.put("# ChEBI_ID:", HEADER.ChEBI_ID);
		oMap.put("# Chemical_Formula:", HEADER.Chemical_Formula);
		oMap.put("# Chemical_IUPAC_Name:", HEADER.Chemical_IUPAC_Name);
		oMap.put("# Chemical_Structure:", HEADER.Chemical_Structure);
		oMap.put("# Creation_Date:", HEADER.Creation_Date);
		oMap.put("# DPD_Drug_ID_Number:", HEADER.DPD_Drug_ID_Number);
		oMap.put("# Description:", HEADER.Description);
		oMap.put("# Dosage_Forms:", HEADER.Dosage_Forms);
		oMap.put("# Drug_Category:", HEADER.Drug_Category);
		oMap.put("# Drug_Interactions:", HEADER.Drug_Interactions);
		oMap.put("# Drug_Reference:", HEADER.Drug_Reference);
		oMap.put("# Drug_Type:", HEADER.Drug_Type);
		oMap.put("# Experimental_Caco2_Permeability:", HEADER.Experimental_Caco2_Permeability);
		oMap.put("# Experimental_LogP_Hydrophobicity:", HEADER.Experimental_LogP_Hydrophobicity);
		oMap.put("# Experimental_Logs:", HEADER.Experimental_Logs);
		oMap.put("# Experimental_Water_Solubility:", HEADER.Experimental_Water_Solubility);
		oMap.put("# FDA_Label_Files:", HEADER.FDA_Label_Files);
		oMap.put("# Food_Interactions:", HEADER.Food_Interactions);
		oMap.put("# GenBank_ID:", HEADER.GenBank_ID);
		oMap.put("# Generic_Name:", HEADER.Generic_Name);
		oMap.put("# HET_ID:", HEADER.HET_ID);
		oMap.put("# Half_Life:", HEADER.Half_Life);
		oMap.put("# InChI_Identifier:", HEADER.InChI_Identifier);
		oMap.put("# InChI_Key:", HEADER.InChI_Key);
		oMap.put("# Indication:", HEADER.Indication);
		oMap.put("# KEGG_Compound_ID:", HEADER.KEGG_Compound_ID);
		oMap.put("# KEGG_Drug_ID:", HEADER.KEGG_Drug_ID);
		oMap.put("# LIMS_Drug_ID:", HEADER.LIMS_Drug_ID);
		oMap.put("# MSDS_Files:", HEADER.MSDS_Files);
		oMap.put("# Mass_Spec_File:", HEADER.Mass_Spec_File);
		oMap.put("# Mechanism_Of_Action:", HEADER.Mechanism_Of_Action);
		oMap.put("# Melting_Point:", HEADER.Melting_Point);
		oMap.put("# Molecular_Weight_Avg:", HEADER.Molecular_Weight_Avg);
		oMap.put("# Molecular_Weight_Mono:", HEADER.Molecular_Weight_Mono);
		oMap.put("# Organisms_Affected:", HEADER.Organisms_Affected);
		oMap.put("# PDB_Experimental_ID:", HEADER.PDB_Experimental_ID);
		oMap.put("# PDB_Homology_ID:", HEADER.PDB_Homology_ID);
		oMap.put("# PDRhealth_Link:", HEADER.PDRhealth_Link);
		oMap.put("# PharmGKB_ID:", HEADER.PharmGKB_ID);
		oMap.put("# Pharmacology:", HEADER.Pharmacology);
		oMap.put("# Predicted_LogP_Hydrophobicity:", HEADER.Predicted_LogP_Hydrophobicity);
		oMap.put("# Predicted_LogS:", HEADER.Predicted_LogS);
		oMap.put("# Predicted_Water_Solubility:", HEADER.Predicted_Water_Solubility);
		oMap.put("# Primary_Accession_No:", HEADER.Primary_Accession_No);
		oMap.put("# Protein_Binding:", HEADER.Protein_Binding);
		oMap.put("# PubChem_Compound_ID:", HEADER.PubChem_Compound_ID);
		oMap.put("# PubChem_Substance_ID:", HEADER.PubChem_Substance_ID);
		oMap.put("# RxList_Link:", HEADER.RxList_Link);
		oMap.put("# Secondary_Accession_No:", HEADER.Secondary_Accession_No);
		oMap.put("# Smiles_String_canonical:", HEADER.Smiles_String_canonical);
		oMap.put("# Smiles_String_isomeric:", HEADER.Smiles_String_isomeric);
		oMap.put("# State:", HEADER.State);
		oMap.put("# Structure:", HEADER.Structure);
		oMap.put("# SwissProt_ID:", HEADER.SwissProt_ID);
		oMap.put("# SwissProt_Name:", HEADER.SwissProt_Name);
		oMap.put("# Synonyms:", HEADER.Synonyms);
		oMap.put("# Synthesis_Reference:", HEADER.Synthesis_Reference);
		oMap.put("# Toxicity:", HEADER.Toxicity);
		oMap.put("# Update_Date:", HEADER.Update_Date);
		oMap.put("# Wikipedia_Link:", HEADER.Wikipedia_Link);
		oMap.put("# contraindication_insert:", HEADER.contraindication_insert);
		oMap.put("# interaction_insert:", HEADER.interaction_insert);
		oMap.put("# pKa_Isoelectric_Point:", HEADER.pKa_Isoelectric_Point);
		oMap.put("# patient_information_insert:", HEADER.patient_information_insert);
		
		return oMap;
	}
	
	/**
	 * Used to store various information about one DrugTarget temporarily.
	 */
	private class TargetContainer {
		
		/**
		 * Primary accession number of SwissProt
		 */
		private String m_strPrimaryAccNo;
		
		/**
		 * PubMed ID for drug - drug target references
		 */
		private List<String> m_oDrugReferences = new ArrayList<String>();

		public String getPrimaryAccNo() {
			return m_strPrimaryAccNo;
		}
		
		public void setPrimaryAccNo(String primaryAccNo) {
			m_strPrimaryAccNo = primaryAccNo;
		}
		
		public List<String> getDrugReferences() {
			return m_oDrugReferences;
		}
		
		public void addDrugReference(String strDrugReference) {
			m_oDrugReferences.add(strDrugReference);
		}
	}
}
