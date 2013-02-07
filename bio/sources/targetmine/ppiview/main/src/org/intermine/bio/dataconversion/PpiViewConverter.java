package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;
import org.intermine.xml.full.ItemHelper;

public class PpiViewConverter extends BioFileConverter {
	
	public static final String DATASET_TITLE = "PPI view";
	private static final String DATA_SOURCE_NAME = "H-InvDB";

	private static Logger m_oLogger = Logger.getLogger(PpiViewConverter.class);
	
	private ItemWriter m_oWriter;
	
	// <PrimaryAcc, Protein>
	private Map<String, Item> m_oProteinMap = new HashMap<String, Item>();
	
	// <Sprot human accession number>
	private Set<String> m_oSprotHumanSet = new TreeSet<String>();
	
	// H-invitation FCDNA file
	private File m_oHip2UniProtFile;
	
	// Swiss-Prot human dat file (uniprot_sprot_human.dat)
	private File m_oSprotHumanFile;
	
	// <HIP_ID, HIX_ID>
	private Map<Integer, Integer> m_oHipHixMap = new HashMap<Integer, Integer>();
	
	// <HIP_ID, TreeSet<Uniprot>>
	private Map<Integer, TreeSet<String>> m_oHipUniprotMap = new HashMap<Integer, TreeSet<String>>();
	
	// <HIP_ID, representative uniprot accession number>
	// To hold representative uniprot entry for each hip.
	private Map<Integer, String> m_oHipRepresentMap = new HashMap<Integer, String>();
	
	// Key is "dbname_id". ex) MINT_MINT-2837333
	// Value is Item ProteinInteractionSource
	private TreeMap<String, Item> m_oPISourceMap = new TreeMap<String, Item>();
	
	public PpiViewConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
		m_oWriter = writer;
	}
	
	@Override
	/**
	 * Each line of a input file should be in the following format(tab-delimited)
	 * <protein_name>	<tool_name>	<start_pos>	<end_pos>	<comment>	<score>
	 * And file MUST be sorted by <protein_name>
	 */
	public void process(Reader reader) throws Exception {
		
		if (null == m_oHip2UniProtFile) {
			throw new NullPointerException("hip2UniProtFile property not set");
		}
		
		try {
			readSprotHuman(new FileReader(m_oSprotHumanFile));
		} catch (IOException oErr) {
			throw new RuntimeException("error reading sprotHumanFile", oErr);
		}
		
		try {
			readHip2UniProt(new FileReader(m_oHip2UniProtFile));
		} catch (IOException oErr) {
			throw new RuntimeException("error reading cdnaFile", oErr);
		}
		
		selectRepresentative();
		
		m_oLogger.debug("m_oHipHixMap size:" + m_oHipHixMap.size());
		m_oLogger.debug("m_oHipUniprotMap size:" + m_oHipUniprotMap.size());
		
		BufferedReader oBr = new BufferedReader(reader);
		boolean bIsHeader = true;
		
		while(oBr.ready()) {
			
			String strLine = oBr.readLine();
			
			// Skip a header line.
			if( bIsHeader ) {
				bIsHeader = false;
				continue;
			}
			
			if(null == strLine || "".equals(strLine)) {
				continue;
			}
			
			String[] a_strFields = strLine.split("\t", -1);
			if(a_strFields.length < 9) {
				continue;
			}
			String strIntId = a_strFields[0];
			
			Integer iHipA = Integer.parseInt( a_strFields[1].replaceAll("HIP", "") );
			Integer iHipB = Integer.parseInt( a_strFields[2].replaceAll("HIP", "") );
			
			ArrayList<Item> a_oPISourceList = new ArrayList<Item>();
			a_oPISourceList.addAll( getProteinInteractionSource("BIND", a_strFields[3]) );
			a_oPISourceList.addAll( getProteinInteractionSource("DIP", a_strFields[4]) );
			a_oPISourceList.addAll( getProteinInteractionSource("MINT", a_strFields[5]) );
			a_oPISourceList.addAll( getProteinInteractionSource("HPRD", a_strFields[6]) );
			a_oPISourceList.addAll( getProteinInteractionSource("IntAct", a_strFields[7]) );
			a_oPISourceList.addAll( getProteinInteractionSource("GNP_Y2H", a_strFields[8]) );
			
			List<Item> oUniProtListA = getProteinList(m_oHipUniprotMap.get( iHipA ));
			List<Item> oUniProtListB = getProteinList(m_oHipUniprotMap.get( iHipB ));
			
			// Nothing to do
			if( oUniProtListA.size() == 0 || oUniProtListB.size() == 0 ) {
				continue;
			}
			
			Item oRepA = getProtein(m_oHipRepresentMap.get(iHipA));
			Item oRepB = getProtein(m_oHipRepresentMap.get(iHipB));
						
			for( Item oUniProtA : oUniProtListA ) {
				registerInteraction(strIntId, oUniProtA, oRepB, oUniProtListB, a_oPISourceList);
			}
			
			for( Item oUniProtB : oUniProtListB ) {
				registerInteraction(strIntId, oUniProtB, oRepA, oUniProtListA, a_oPISourceList);
			}
		}
	}
	
	/**
	 * Register ProteinInteraction
	 * @param strIntId
	 * @param oProtein
	 * @param oRepresentPartner
	 * @param oPartners
	 * @param oPISourceList
	 * @throws ObjectStoreException 
	 */
	private void registerInteraction(
			String strIntId,
			Item oProtein,
			Item oRepresentPartner,
			List<Item> oPartners,
			ArrayList<Item> oPISourceList
		) throws ObjectStoreException {
		
		Item oPI = createItem("ProteinInteraction");
		
		oPI.setAttribute("intId", strIntId);
		oPI.setReference("protein", oProtein);
		oPI.setReference("representativePartner", oRepresentPartner);
		
		for (Item oPartner: oPartners) {
			oPI.addToCollection("allPartners", oPartner);
		}
		
		for (Item oPiSource : oPISourceList) {
			oPI.addToCollection("piSources", oPiSource);
		}
		
		store(oPI);
	}
	
	/**
	 * Get ProteinInteractionSource item
	 * @param strDbName
	 * @param strIds
	 * @return List of ProteinInteractionSources' identifier
	 */
	private List<Item> getProteinInteractionSource(String strDbName, String strIds) throws ObjectStoreException {
		
		if(null == strDbName || "".equals(strDbName) || null == strIds || "".equals(strIds)) {
			return new ArrayList<Item>();
		}
		
		ArrayList<Item> a_oPISourceList = new ArrayList<Item>();
		
		for(String strId : strIds.split(",")) {
			
			String strDbNameId = strDbName + "_" + strIds;
			if(!m_oPISourceMap.containsKey(strDbNameId)){
				
				Item oPISource = createItem("ProteinInteractionSource");
				oPISource.setAttribute("dbName", strDbName);
				oPISource.setAttribute("identifier", strId);
				store(oPISource);
				m_oPISourceMap.put(strDbNameId, oPISource);
				
			}
			a_oPISourceList.add( m_oPISourceMap.get(strDbNameId) );
		}
		
		return a_oPISourceList;
	}
	
	private void readHip2UniProt(Reader oReader) throws IOException {
		
		BufferedReader oBr = new BufferedReader(oReader);
		
		while( oBr.ready() ) {
			
			String strLine = oBr.readLine();
			
			if(null == strLine || "".equals(strLine)) {
				continue;
			}
			
			String[] strFields = strLine.split("\t");
			
			Integer iHip = Integer.parseInt( strFields[0].replaceAll("HIP", "") );
			
			if(strFields.length == 1) {
				continue;
			}
			
			String[] strUniProts = strFields[1].split(",");
			
			for(String strUniProt : strUniProts) {
			
				putHipUniprot( iHip, strUniProt );
				
			}
		}
	}
	
	/**
	 * Parse H-inv DB's FCDNA file to get relation between HIP-ID and
	 *  UniProt primary accession number 
	 * @param oReader Reader for FCDNA file 
	 */
	/*
	private void readCdnas(Reader oReader) throws IOException {
		
		BufferedReader oBr = new BufferedReader(oReader);
		
		Integer iHixId = null;
			
		while( oBr.ready() ) {
			
			String strLine = oBr.readLine();
			if(null == strLine || "".equals(strLine)) {
				continue;
			}
			if("//".equals(strLine)) {
				
				iHixId = null;
				
			}
			String[] a_strKeyValue = strLine.split(": ");
			if(2 != a_strKeyValue.length) {
				continue;
			}
			if( "CDNA_CLUSTER-ID".equals(a_strKeyValue[0]) ) {
				
				iHixId = Integer.parseInt( a_strKeyValue[1].replaceAll("HIX", "") );
				
			} else if( "CDNA_H-INVITATIONAL-PROTEIN-ID".equals(a_strKeyValue[0]) ) {
				
				if(iHixId == null) {
					continue;
				}
				Integer iHipId = Integer.parseInt( a_strKeyValue[1].replaceAll("HIP", "") );
				m_oHipHixMap.put(iHipId, iHixId);
				
			} else if( "CDNA_DB-REFERENCE_UNIPROT-PROTEIN-ID".equals(a_strKeyValue[0]) ) {
				
				String strUniProt = a_strKeyValue[1];
				putHixUniprot( iHixId, strUniProt );
				
			}
		}
	}*/
	
	private void readSprotHuman(Reader oReader) throws IOException {
		
		BufferedReader oBr = new BufferedReader(oReader);
		while( oBr.ready() ) {
			
			String strLine = oBr.readLine();
			if(null == strLine || "".equals(strLine) || !strLine.startsWith("AC   ")) {
				continue;
			}
			strLine = strLine.replaceAll("AC   ", "");
			strLine = strLine.replaceAll(";$", "");			
			String[] fields = strLine.split("; ");
			
			for(String strAccNo : fields) {
				m_oSprotHumanSet.add(strAccNo);
			}
		}
	}
	
	/**
	 * Put HipxID - UniprotAccession relation to m_oHipUniprotMap
	 * @param iHipId
	 * @param strUniProt
	 */
	private void putHipUniprot(Integer iHipId, String strUniProt) {
		
		if(m_oHipUniprotMap.containsKey(iHipId)) {
			
			if(!m_oHipUniprotMap.get(iHipId).contains(strUniProt)) {
				
				m_oHipUniprotMap.get(iHipId).add(strUniProt);
				
			}
			
		} else {
			
			TreeSet<String> oSet = new TreeSet<String>();
			oSet.add(strUniProt);
			m_oHipUniprotMap.put(iHipId, oSet);
			
		}
		
	}
	
	/**
	 * Select one representative protein among each HIP.
	 * Rule of selection
	 *  1. If at least one Swiss-Prot entry exists, select first one in a list
	 *  2. Otherwise, select first TrEMBL entry in a list
	 */
	private void selectRepresentative() {
		
		for(Entry<Integer, TreeSet<String>> oEntry: m_oHipUniprotMap.entrySet() ) {
			
			String strSwissProt = null;
			String strTrEmbl = null;
			
			for(String strUniProt : oEntry.getValue()) {
				if(m_oSprotHumanSet.contains(strUniProt)) {
					
					if(null == strSwissProt) {
						strSwissProt = strUniProt;
					}
					
				} else {
					
					if(null == strTrEmbl) {
						strTrEmbl = strUniProt;
					}
					
				}
			}
			
			m_oHipRepresentMap.put(oEntry.getKey(), null != strSwissProt ? strSwissProt : strTrEmbl);
		}
	}
	
	public void setHip2UniProtFile(File oHip2UniProtFile) {
		this.m_oHip2UniProtFile = oHip2UniProtFile;
	}
	
	public void setSprotHumanFile(File oSprotHumanFile) {
		this.m_oSprotHumanFile = oSprotHumanFile;
	}
	
	/**
	 * Get protein item
	 * @param strPrimaryAcc primary accession number for protein
	 * @return protein item
	 */
	private Item getProtein(String strPrimaryAcc) {
		
		if(m_oProteinMap.containsKey(strPrimaryAcc)) {
			return m_oProteinMap.get(strPrimaryAcc);
		} else {
			Item oProtein = createItem("Protein");
	        oProtein.setAttribute("primaryAccession", strPrimaryAcc);
	        //oProtein.setAttribute("hipId", strHipId);
	        //oProtein.setReference("organism", getOrganismHuman());
	        try {
	        	m_oWriter.store(ItemHelper.convert(oProtein));
	        } catch(ObjectStoreException e) {
	        	e.printStackTrace();
	        }
	        m_oProteinMap.put(strPrimaryAcc, oProtein);
	        return oProtein;
		}
    }
	
	/**
	 * Get protein item list
	 * @return
	 */
	private List<Item> getProteinList(Set<String> oUniProtSet) {
		
		ArrayList<Item> oItemList = new ArrayList<Item>();
		
		if(null == oUniProtSet) {
			return oItemList;
		}
		
		for(String strAccNo : oUniProtSet) {
			oItemList.add(getProtein(strAccNo));
		}
		
		return oItemList;
		
	}

}
