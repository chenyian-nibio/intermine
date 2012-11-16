package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.intermine.dataconversion.FileConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;

/**
 * 
 * @author ishikawa; chenyian
 * <br/>
 * @deprecated
 * 2012/11/15 with the modification of the region model, 
 * this parser (source) is replaced by sifts. 
 */
@Deprecated
public class MsdConverter extends FileConverter {
	
//	private static Logger m_oLogger = Logger.getLogger(MsdConverter.class);
	
	// <UniProtAccession, Protein item>
	private Map<String, Item> m_oProteinMap = new TreeMap<String, Item>();
	
	// <PDB ID, ProteinStructure item>
	private Map<String, Item> m_oProteinStructureMap = new TreeMap<String, Item>();
	
	// <PDBID_chain, ProteinChain Item>
	private Map<String, Item> m_oProteinChainMap = new TreeMap<String, Item>();
	
	private Pattern m_oInsertionCodePattern = Pattern.compile("[A-Za-z]");
	
	public MsdConverter(ItemWriter writer, Model model) {
		super(writer, model);
	}
	
	@Override
	public void process(Reader reader) throws Exception {
		
		BufferedReader oBr = new BufferedReader(reader);
		boolean bIsHeader = true;
		
		while(oBr.ready()) {
			
			String strLine = oBr.readLine();
			
			// Read through a header line.
			if( bIsHeader ) {
				bIsHeader = false;
				continue;
			}
			
			if(null == strLine || "".equals(strLine) || strLine.startsWith("#")) {
				continue;
			}
			
			String[] a_strFields = strLine.split("\t");
			if(a_strFields.length < 9) {
				continue;
			}
			
			Item oProteinStructure = getProteinStructure(a_strFields[0]);
			Item oProtein = getProtein(a_strFields[2]);
			Item oPDBRegion = createPDBRegion(a_strFields[4], a_strFields[5],a_strFields[6], a_strFields[7]);
			Item oProteinStructureRegion = createProteinStructureRegion(a_strFields[8], a_strFields[9]);
			Item oProteinChain = getProteinChain( a_strFields[0], a_strFields[1] );
			
			oProteinStructureRegion.setReference("protein", oProtein);
			oProteinStructureRegion.setReference("pdbRegion", oPDBRegion);
			oPDBRegion.setReference("chain", oProteinChain);
			oPDBRegion.setReference("proteinRegion", oProteinStructureRegion);
			oProteinChain.setReference("structure", oProteinStructure);
			
			store(oProteinStructureRegion);
			store(oPDBRegion);
		}
		
		// store ProteinChain
		store(m_oProteinChainMap.values());
	}
	
	private Item createPDBRegion(String resStart, String resEnd,String strStart, String strEnd) throws ObjectStoreException {
		
		Item oPDBRegion = createItem("PDBRegion");
		
		// If residue is with insertion code, split them.
		Matcher oStartMatcher = m_oInsertionCodePattern.matcher(strStart);
		if(oStartMatcher.find()) {
			oPDBRegion.setAttribute("start", strStart.substring(0, oStartMatcher.start()));
			oPDBRegion.setAttribute("startInsertionCode", strStart.substring(oStartMatcher.start()));
		} else {
			oPDBRegion.setAttribute("start", strStart);
		}
		
		Matcher oEndMatcher = m_oInsertionCodePattern.matcher(strEnd);
		if(oEndMatcher.find()) {
			oPDBRegion.setAttribute("end", strEnd.substring(0, oEndMatcher.start()));
			oPDBRegion.setAttribute("endInsertionCode", strEnd.substring(oEndMatcher.start()));
		} else {
			oPDBRegion.setAttribute("end", strEnd);
		}
		
		// chenyian: add 2 new attributes, residue start and end
		oPDBRegion.setAttribute("resStart", resStart);
		oPDBRegion.setAttribute("resEnd", resEnd);
		
		return oPDBRegion;
		
	}
	
	private Item createProteinStructureRegion(String strStart, String strEnd) throws ObjectStoreException {
		
		Item oProteinRegion = createItem("ProteinStructureRegion");
		oProteinRegion.setAttribute("start", strStart);
		oProteinRegion.setAttribute("end", strEnd);
		return oProteinRegion;
		
	}
	
	private Item getProtein(String strPrimaryAcc) throws ObjectStoreException {
		
		if(!m_oProteinMap.containsKey(strPrimaryAcc)) {
			
			Item oProtein = createItem("Protein");
			oProtein.setAttribute("primaryAccession", strPrimaryAcc);
			oProtein.setAttribute("uniprotAccession", strPrimaryAcc);
			store(oProtein);
			m_oProteinMap.put(strPrimaryAcc, oProtein);
			
		}
		
		return m_oProteinMap.get(strPrimaryAcc);
		
	}
	
	private Item getProteinChain( String strPdbId, String strChain ) throws ObjectStoreException {
		
		String strPdbIdChain = strPdbId + strChain;
		
		if ( !m_oProteinChainMap.containsKey( strPdbIdChain ) ) {
			
			Item oProteinChain = createItem( "ProteinChain" );
			oProteinChain.setAttribute( "pdbId", strPdbId );
			oProteinChain.setAttribute( "chain", strChain );
			m_oProteinChainMap.put( strPdbIdChain, oProteinChain );
			
		}
		
		return m_oProteinChainMap.get( strPdbIdChain );
	}
	
	private Item getProteinStructure(String strPdbId) throws ObjectStoreException {
		
		if(!m_oProteinStructureMap.containsKey(strPdbId)) {
			
			Item oProteinStructure = createItem("ProteinStructure");
			oProteinStructure.setAttribute("pdbId", strPdbId);
			store(oProteinStructure);
			m_oProteinStructureMap.put(strPdbId, oProteinStructure);
			
		}
		
		return m_oProteinStructureMap.get(strPdbId);
		
	}
	
	
}
