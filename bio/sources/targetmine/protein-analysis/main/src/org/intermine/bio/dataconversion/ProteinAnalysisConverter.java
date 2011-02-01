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

public class ProteinAnalysisConverter extends FileConverter {

	private static Logger m_oLogger = Logger.getLogger(ProteinAnalysisConverter.class);
	
//	private ItemWriter m_oWriter;
	// <ToolName, ComputationalAnalysis>
	private Map<String, Item> m_oToolMap = new HashMap<String, Item>();
	// <PrimaryAcc, Protein>
	private Map<String, Item> m_oProteinMap = new HashMap<String, Item>();
	
	public ProteinAnalysisConverter(ItemWriter writer, Model model) {
		super(writer, model);
		// chenyian: unused?
//		m_oWriter = writer;
	}
	
	@Override
	/**
	 * Each line of a input file should be in the following format(tab-delimited)
	 * <protein_name> <tool_name>	<start_pos>	<end_pos>	<comment>	<score>
	 * And file MUST be sorted by <protein_name>
	 */
	public void process(Reader reader) throws Exception {
		
		BufferedReader oReader = new BufferedReader(reader);
		
		// <ToolName, ProteinComputationalResult>
		Map<String, Item> oResultMap = new HashMap<String, Item>();
		// Current protein id
		String strCurProtId = "";
		
		while(oReader.ready()) {
			String strLine = oReader.readLine();
			if(null == strLine || "".equals(strLine)) {
				continue;
			}
			String[] a_strFields = strLine.split("\t");
			
			//if (a_strFields.length < 6) {
				// At least, identifier, taxid, proteinname, toolname, startpos and endpos are necessary.
				//continue;
			//}
			String strPrimaryAcc = a_strFields[0];
			String strToolName = a_strFields[1];
			String strStartPos = a_strFields[2];
			String strEndPos = a_strFields[3];
			String strComment = (5 <= a_strFields.length ? a_strFields[4] : "");
			String strScore = (6 <= a_strFields.length ? a_strFields[5] : "NaN");
			
			m_oLogger.info("strPrimaryAcc = " + strPrimaryAcc + ", strToolName = " + strToolName);
			
			if(null != strPrimaryAcc && !strCurProtId.equals(strPrimaryAcc)) {
				// Here comes a new protein
				oResultMap.clear();
				strCurProtId = strPrimaryAcc;
			}
			
			if(!oResultMap.containsKey(strToolName)) {
				// Item of ProteinComputationalResult
				Item oPcr = createItem("ProteinComputationalResult");
				
				oPcr.setReference("protein", getProtein(strPrimaryAcc));
				
				oPcr.setReference("computationalAnalysis", getTool(strToolName));
				oResultMap.put(strToolName, oPcr);
				store(oPcr);
			}
			
			// Item of ProteinRegion
			Item oPr = createItem("ProteinHitRegion");
			oPr.setReference("proteinComputationalResult", oResultMap.get(strToolName));
			oPr.setAttribute("description", strComment);
			oPr.setAttribute("score", strScore);
			if( strStartPos.matches("\\d+") ) {
				oPr.setAttribute("start", strStartPos);
			}
			if( strEndPos.matches("\\d+") ) {
				oPr.setAttribute("end", strEndPos);
			}
			
			store(oPr);
		}
	}
		
	private Item getProtein(String strPrimaryAcc) throws ObjectStoreException {
		
		if(!m_oProteinMap.containsKey(strPrimaryAcc)) {
			Item oProtein = createItem("Protein");
			oProtein.setAttribute("primaryAccession", strPrimaryAcc);
			store(oProtein);
	        m_oProteinMap.put(strPrimaryAcc, oProtein);
		}
		
		return m_oProteinMap.get(strPrimaryAcc);
		
    }
	
	private Item getTool(String strToolName) throws ObjectStoreException {
		
		if(!m_oToolMap.containsKey(strToolName)) {
			Item oCompAnal = createItem("ComputationalAnalysis");
			oCompAnal.setAttribute("algorithm", strToolName);
			store(oCompAnal);
			m_oToolMap.put(strToolName, oCompAnal);
		}
		
		return m_oToolMap.get(strToolName);
		
	}

}
