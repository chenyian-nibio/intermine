package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.intermine.dataconversion.FileConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;

public class FugueConverter extends FileConverter {

//	private static Logger m_oLogger = Logger.getLogger(FugueConverter.class);
	
	// <PrimaryAcc, Protein>
	private Map<String, Item> m_oProteinMap = new HashMap<String, Item>();
	// <PrimaryAcc, ProteinComputationalResult>
	private Map<String, Item> m_oResultMap = new HashMap<String, Item>();
	// Item of ComputationalAnalysis for FUGUE
	private Item m_oFugue = null;
	
	public FugueConverter(ItemWriter writer, Model model) {
		super(writer, model);
	}
	@Override
	/**
	 * Each line of a input file should be in the following format(tab-delimited)
	 * <protein_name>	<tool_name>	<start_pos>	<end_pos>	<comment>	<score>
	 * And file MUST be sorted by <protein_name>
	 */
	public void process(Reader reader) throws Exception {
		
		//Todo Should I close reader ?
		BufferedReader oReader = new BufferedReader(reader);
				
		boolean bIsAlignmentLine = false; // After reading properties(id, etc.), turn this to false 
		
		String strPrimaryAcc = null;
		String strProfileId = null;
		//String strHomstradFam = null;
		String strZScore = null;
		String strPid = null;
		String strStartPos = null;
		String strEndPos = null;
		StringBuffer oAlignment = new StringBuffer();
		
		while(oReader.ready()) {
			String strLine = oReader.readLine();
			if(null == strLine || "".equals(strLine) || strLine.startsWith("#")) {
				continue;
			}
			
			if(bIsAlignmentLine) {
				
				oAlignment.append(strLine + "\n");
				
			} else {
				
				String[] a_strFields = strLine.split("\t");
				try {
					strPrimaryAcc = a_strFields[0];
					strProfileId = a_strFields[1];
					//strHomstradFam = a_strFields[2];
					strZScore = a_strFields[3];
					strPid = round(a_strFields[4], 2);					
					strStartPos = a_strFields[5];
					strEndPos = a_strFields[6];
				} catch(ArrayIndexOutOfBoundsException e) {
					throw new Exception("Invalid fugue result file: " + strLine, e);
				}
				bIsAlignmentLine = true;
			}
		}
		
		if(!m_oResultMap.containsKey(strPrimaryAcc)) {
			
			Item oPcr = createItem("ProteinComputationalResult");
			oPcr.setReference("protein", getProtein( strPrimaryAcc));
			oPcr.setReference("computationalAnalysis", getFugue());
			m_oResultMap.put(strPrimaryAcc, oPcr);
			store(oPcr);
			
		}
		
		// Item of HitProfile
		Item oFhp = createItem("FugueHitProfile");
		oFhp.setReference("proteinComputationalResult", m_oResultMap.get(strPrimaryAcc));
		if( ! isNullOrEmpty(strProfileId) ){
			oFhp.setAttribute("description", strProfileId);
		}
		if( ! isNullOrEmpty(strZScore) ){
			oFhp.setAttribute("score", strZScore);
			oFhp.setAttribute("z_score", strZScore);
		}
		if( ! isNullOrEmpty(strStartPos) ){
			oFhp.setAttribute("start", strStartPos);
		}
		if( ! isNullOrEmpty(strEndPos) ){
			oFhp.setAttribute("end", strEndPos);
		}
		if( ! isNullOrEmpty(strProfileId) ){
			oFhp.setAttribute("profile_id", strProfileId);
		}
		//oFhp.setAttribute("homstrad_family", strHomstradFam);
		if( ! isNullOrEmpty(strPid) ){
			oFhp.setAttribute("pid", strPid);
		}
		if( oAlignment.length() != 0 ){
			oFhp.setAttribute("alignment", oAlignment.toString());
		}
		store(oFhp);
	}
	
	private String round(String original, int iScale) {
		BigDecimal oBd = new BigDecimal(original);
		return String.valueOf(oBd.setScale(iScale, BigDecimal.ROUND_HALF_UP).doubleValue());		
	}
	
	/**
	 * get Item for "ComputationalAnalysis " of Fugue
	 * @return Item for "ComputationalAnalysis"
	 * @throws ObjectStoreException 
	 */
	private Item getFugue() throws ObjectStoreException {
		
		if( null == m_oFugue ){
			Item oFugue = createItem("ComputationalAnalysis");
			oFugue.setAttribute("algorithm", "Fugue");
			store(oFugue);
			m_oFugue = oFugue;
		}
		
		return m_oFugue;

	}

	/**
	 * get an Item for "Protein"
	 * @param strTaxId Tax ID
	 * @param strPrimaryAcc Primary accession
	 * @return Item for "Protein"
	 * @throws ObjectStoreException 
	 */
	private Item getProtein(String strPrimaryAcc) throws ObjectStoreException {
		
		if( ! m_oProteinMap.containsKey(strPrimaryAcc)) {
			
			Item oProtein = createItem("Protein");
	        oProtein.setAttribute("primaryAccession", strPrimaryAcc);
	        store(oProtein);
	        m_oProteinMap.put(strPrimaryAcc, oProtein);

		}
		
		return m_oProteinMap.get( strPrimaryAcc );
        
    }
	
	private boolean isNullOrEmpty( String strSubject ){
		
		return (null == strSubject || strSubject.length() == 0);
		
	}
}
