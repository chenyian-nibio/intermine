package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.intermine.dataconversion.FileConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;

public class KeggOrthologyConverter extends FileConverter {
	
	private static Logger m_oLogger = Logger.getLogger(KeggOrthologyConverter.class);
	
	public static String TYPE_OF_PARALOG = "paralog";
	
	public static String TYPE_OF_ORTHOLOG = "ortholog";
	
	// <kegg orthology number, list of String (Protein or Gene) >
	private Map<String, List<String>> m_oOrthologMap = new TreeMap<String, List<String>>();
	
	// <kegg orthology number, list of String (Protein or Gene)>
	private Map<String, List<String>> m_oParalogMap = new TreeMap<String, List<String>>();
	
	// <primary acc, Protein item>
	private Map<String, Item> m_oGeneMap = new TreeMap<String, Item>();
	
	// DataSource item for KEGG GENES
	private Item m_oKeggGeneSource = null;
	
	private Item m_oKeggGeneDataSet = null;
	
	public KeggOrthologyConverter(ItemWriter writer, Model model) {
		super(writer, model);
	}
	
	@Override
	public void process(Reader reader) throws Exception {
		
		m_oLogger.debug( "filenameis" + getCurrentFile().getName() + ", size=" + m_oOrthologMap.size() );
		
		BufferedReader oBr = new BufferedReader(reader);
		
		while(oBr.ready()) {
			
			String strLine = oBr.readLine();
			
			if(null == strLine || "".equals(strLine)) {
				continue;
			}
			
			String[] a_strFields = strLine.split("\t");
			if(a_strFields.length < 7) {
				continue;
			}
			
//			String strGene = a_strFields[0];
//			String strNcbiGi = a_strFields[1];
			String strGeneId = a_strFields[2].replace("ncbi-geneid:", "");
//			String strUniProts = a_strFields[3].replace("up:", "");
//			String strEcNum = a_strFields[4];
			String strKoNums = a_strFields[5];
//			String strPathway = a_strFields[6];
			
			// All line must have at least one gene id
			if("".equals(strGeneId)) {
				throw new RuntimeException("Kegg gene file is invalid(doesn't have geneid)" + strLine);
			}
			// chenyian: Nothing to do
			if("".equals(strKoNums)) {
				continue;
			}
			
			String[] strKoNumList = strKoNums.replace("ko:", "").split(" ");
						
			for(String strKoNum : strKoNumList) {
					
				addToParalog(strKoNum, strGeneId);
				
				// add orthologs
				if( !m_oOrthologMap.containsKey(strKoNum)) {
					continue;
				}
				for(String strOrthologAcc : m_oOrthologMap.get(strKoNum)) {
					m_oLogger.debug("strOthologAcc:" + strOrthologAcc);
					
					Item oGene = getGene(strGeneId);
					Item oOrtholog = getGene(strOrthologAcc);
					
					Item oGeneHomolog1 = createItem("GeneHomolog");
					
					oGeneHomolog1.setAttribute("koNumber", strKoNum);
					oGeneHomolog1.setAttribute("type", TYPE_OF_ORTHOLOG);
					oGeneHomolog1.setReference("homolog", oOrtholog);
					oGeneHomolog1.setReference("source", oGene);
					oGeneHomolog1.addToCollection("dataSets", getKeggGeneDataSet());
					
					store(oGeneHomolog1);
					
					Item oGeneHomolog2 = createItem("GeneHomolog");
					
					oGeneHomolog2.setAttribute("koNumber", strKoNum);
					oGeneHomolog2.setAttribute("type", TYPE_OF_ORTHOLOG);
					oGeneHomolog2.setReference("homolog", oGene);
					oGeneHomolog2.setReference("source", oOrtholog);
					oGeneHomolog2.addToCollection("dataSets", getKeggGeneDataSet());
					
					store(oGeneHomolog2);
					
				}
			}
		}
		
		// Store paralogous relationship
		storeParalogs();
		
		// postprocess
		moveParalogs2Orthologs();
	}
	
	private Item getKeggGeneDataSet() throws ObjectStoreException {
		
		if(null == m_oKeggGeneDataSet) {

			m_oKeggGeneSource = createItem("DataSource");
			m_oKeggGeneSource.setAttribute("name", "KEGG GENES");
			m_oKeggGeneSource.setAttribute("url", "http://www.genome.ad.jp/kegg/genes.html");
			m_oKeggGeneSource.setAttribute("description", "KEGG_GENES");
			
			m_oKeggGeneDataSet = createItem("DataSet");
			m_oKeggGeneDataSet.setAttribute("name", "KEGG GENES data set");
			m_oKeggGeneDataSet.setAttribute("url", "http://www.genome.jp/kegg/genes.html");
			m_oKeggGeneDataSet.setReference("dataSource", m_oKeggGeneSource);
			
			store(m_oKeggGeneSource);
			store(m_oKeggGeneDataSet);
		}
		
		return m_oKeggGeneDataSet;
		
	}
	
	private void addToParalog(String strKoNum, String strAcc) {
		
		if( !m_oParalogMap.containsKey(strKoNum) ) {
			m_oParalogMap.put(strKoNum, new ArrayList<String>());
		}
		
		m_oParalogMap.get(strKoNum).add(strAcc);
		
	}
	
	private void moveParalogs2Orthologs() {
		
		for(Map.Entry<String, List<String>> oEntry : m_oParalogMap.entrySet()) {
			if( !m_oOrthologMap.containsKey(oEntry.getKey()) ) {
				m_oOrthologMap.put(oEntry.getKey(), new ArrayList<String>());
			}
			
			m_oOrthologMap.get(oEntry.getKey()).addAll(oEntry.getValue());
		}
		
		m_oParalogMap = new TreeMap<String, List<String>>();
	}
	
	private void storeParalogs() throws ObjectStoreException {
		
		for( Map.Entry<String, List<String>> oEntry : m_oParalogMap.entrySet() ) {
			
			if( 1 == oEntry.getValue().size() ) {
				continue;
			}
			
			for( String strAcc : oEntry.getValue() ) {
				
				Item oGene = getGene(strAcc);
				
				for( String strParalogAcc : oEntry.getValue() ) {
					
					if( !strAcc.equals(strParalogAcc) ) {
						Item oParalog = getGene(strParalogAcc);
						Item oGeneHomolog = createItem("GeneHomolog");
						
						oGeneHomolog.setAttribute("koNumber", oEntry.getKey());
						oGeneHomolog.setAttribute("type", TYPE_OF_PARALOG);
						oGeneHomolog.setReference("homolog", oParalog);
						oGeneHomolog.setReference("source", oGene);
						oGeneHomolog.addToCollection("dataSets", getKeggGeneDataSet());
												
						store(oGeneHomolog);
					}
				}
			}
		}
	}
	
	/**
	 * Get Gene Item, having argument primary accession number.
	 * @param strGeneId
	 * @return Item(gene)
	 * @throws ObjectStoreException
	 */
	private Item getGene(String strGeneId) throws ObjectStoreException {
		
		if(!m_oGeneMap.containsKey(strGeneId)) {
			
			Item oGene = createItem("Gene");
			oGene.setAttribute("ncbiGeneId", strGeneId);
			m_oGeneMap.put(strGeneId, oGene);
			store(oGene);
			
		}
		
		return m_oGeneMap.get(strGeneId);
		
	}
	
}
