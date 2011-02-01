package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.intermine.dataconversion.FileConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;
import org.intermine.xml.full.ItemHelper;

public class Integr8Converter extends FileConverter {
	
	private static Logger m_oLogger = Logger.getLogger(Integr8Converter.class);
	
	private ItemWriter m_oWriter;
	
	// <PrimaryAccession, Protein item>
	private Map<String, Item> m_oProteinMap = new HashMap<String, Item>();
	
	// <PrimaryIdentifier, reference to ProteinDomain item>
	private Map<String, String> m_oProteinDomainMap = new HashMap<String, String>();	
	
	public Integr8Converter(ItemWriter writer, Model model) {
		super(writer, model);
		m_oWriter = writer;
	}
	
	@Override
	public void process(Reader reader) throws Exception {
		
		//TODO Should I close reader ?
		BufferedReader oBr = new BufferedReader(reader);
		boolean bIsHeader = true;
		
//		int iTotalStored = 0;
//		int iIsoformStored = 0;
		
		while(oBr.ready()) {
			
			String strLine = oBr.readLine();
			
			// Read through a header line.
			if( bIsHeader ) {
				bIsHeader = false;
			}
			
			if(null == strLine || "".equals(strLine) || strLine.startsWith("#")) {
				continue;
			}
			
			String[] a_strFields = strLine.split("\t");
			if(a_strFields.length < 5) {
				continue;
			}
			String strDbId = a_strFields[0];
			String strMemberDbId = a_strFields[1];
			String strInterProId = a_strFields[2];
			String strFromPos = a_strFields[3];
			String strToPos = a_strFields[4];
			
			// discard all isoform entries
			// discard if strDbId is secondary or later isoform (ex:A12345-2)
//			int iIsoformNum = getIsoformNum(strDbId);
//			if(1 <= iIsoformNum ) {
//				continue;
//			}
			
			String strProteinDomainRef = getProteinDomain(strInterProId);
			
			Item oPdr = createItem("ProteinDomainRegion");
			
			oPdr.setAttribute("start", strFromPos);
			oPdr.setAttribute("end", strToPos);
			oPdr.setAttribute("originalId", strMemberDbId);
			oPdr.setAttribute("originalDb", getDatabaseName(strMemberDbId));
			oPdr.setAttribute("uniprotId", strDbId);
			oPdr.setReference("proteinDomain", strProteinDomainRef);
			String[] id = strDbId.split("\\-");
			oPdr.setReference("protein", getProtein(id[0]));
			// setRegionToProtein(strDbId, oPdr);
			
			store(oPdr);
			
//			iTotalStored++;
//			if(iIsoformNum == 1) {
//				iIsoformStored++;
//			}
		}
		
//		m_oLogger.debug(iTotalStored + " IPI entries are stored. Of which, in"
//				+ iIsoformStored + " cases, isoform suffix were removed.");
	}
	
	/**
	 * Get isoform number of argument strUniprotAcc.
	 * If strUniprotAcc is "A12345-1", return 1.
	 * If strUniprotAcc is not in isoform format, return 0.
	 * @param strUniprotAcc
	 * @return
	 */
//	private static int getIsoformNum(String strUniprotAcc) {
//		
//		int iPosition = strUniprotAcc.indexOf("-");
//		if(iPosition == -1) {
//			return 0;
//		} else {
//			return Integer.parseInt(strUniprotAcc.substring(iPosition + 1));
//		}
//		
//	}
	
	/**
	 * Get Protein reference
	 * @param strPrimaryAccession primary identifier
	 * @return Protein item
	 * @throws ObjectStoreException 
	 */
	private Item getProtein(String strPrimaryAccession) throws ObjectStoreException {
		
		if(!m_oProteinMap.containsKey(strPrimaryAccession)) {
			Item oProtein = createItem("Protein");
			oProtein.setAttribute("primaryAccession", strPrimaryAccession);
			store(oProtein);
	        m_oProteinMap.put(strPrimaryAccession, oProtein);
		}
		
		return m_oProteinMap.get(strPrimaryAccession);
    }
	
	/**
	 * Get ProteinDomain reference
	 * @param strPrimaryIdentifier primary identifier
	 * @return ProteinDomain reference
	 */
	private String getProteinDomain(String strPrimaryIdentifier) {
		
		if(!m_oProteinDomainMap.containsKey(strPrimaryIdentifier)) {
			Item oProteinDomain = createItem("ProteinDomain");
			oProteinDomain.setAttribute("primaryIdentifier", strPrimaryIdentifier);
	        try {
	        	m_oWriter.store(ItemHelper.convert(oProteinDomain));
	        } catch(ObjectStoreException e) {
	        	e.printStackTrace();
	        }
	        m_oProteinDomainMap.put(strPrimaryIdentifier, oProteinDomain.getIdentifier());	        
		}
		
		return m_oProteinDomainMap.get(strPrimaryIdentifier);
    }
	
	/**
	 * Set Protein its domain region
	 * @param strPrimaryAcc UniProt primary accession
	 * @param oPdr ProteinDomainRegion item
	 * @throws ObjectStoreException 
	 */
//	private void setRegionToProtein(String strPrimaryAcc, Item oPdr) throws ObjectStoreException {
//		Item oProtein = getProtein(strPrimaryAcc);
//		oPdr.setReference("protein", oProtein);
//	}

	private String getDatabaseName(String dbId) {
		String dbName = null;
		if (dbId.startsWith("PF")) {
			dbName = "Pfam";
		} else if (dbId.startsWith("SM")) {
			dbName = "SMART";
		} else if (dbId.startsWith("SSF")) {
			dbName = "SUPERFAMILY";
		} else if (dbId.startsWith("PS")) {
			dbName = "PROSITE";
		} else if (dbId.startsWith("PR")) {
			dbName = "PRINTS";
		} else if (dbId.startsWith("PTHR")) {
			dbName = "PANTHER";
		} else if (dbId.startsWith("G3DSA")) {
			dbName = "Gene3D";
		} else if (dbId.startsWith("TIGR")) {
			dbName = "TIGRFAMs";
		} else if (dbId.startsWith("PD")) {
			dbName = "ProDom";
		} else if (dbId.startsWith("PIRSF")) {
			dbName = "PIRSF";
		} else if (dbId.startsWith("MF_")) {
			dbName = "HAMAP";
		} else {
			throw new RuntimeException("Unknown DB found. ID: " + dbId);
		}

		return dbName;
	}
}
