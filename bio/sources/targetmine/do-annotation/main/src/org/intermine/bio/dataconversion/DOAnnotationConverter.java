package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.intermine.dataconversion.FileConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;

/**
 * 
 * @author ishikawa 
 * @author chenyian - refactoring
 * 
 */
public class DOAnnotationConverter extends FileConverter {

//	private static Logger m_oLogger = Logger.getLogger(DOAnnotationConverter.class);

	private Map<String, Item> m_oDOTermMap = new TreeMap<String, Item>();

	private Map<String, Item> m_oGeneMap = new TreeMap<String, Item>();

	// chenyian: reference to Publication class instead of using Integer
	private Map<String, String> publicationiMap = new HashMap<String, String>();

	// chenyian: one doa may contains several GeneRIF
	private Map<String, Item> doAnnotationMap = new HashMap<String, Item>();
	
	public DOAnnotationConverter(ItemWriter writer, Model model) {
		super(writer, model);
	}

	@Override
	public void process(Reader reader) throws Exception {

		BufferedReader oBr = new BufferedReader(reader);

		while (oBr.ready()) {

			String strLine = oBr.readLine();

			if (null == strLine || "".equals(strLine)) {
				continue;
			}

			String[] array = strLine.split("\t", -1);
			Item oDOTerm = getDOTerm(array[4]);
			// chenyian: suppose that each DO term only annotated to one gene once 
			Item doAnnotation = getDOAnnotation(array[0] + array[4]);
			
			Item geneRif = getGeneRIF(array[1], array[5], array[6], array[2]);
			doAnnotation.addToCollection("geneRifs", geneRif);
			
			Item oGene = getGene(array[0]);

			doAnnotation.setReference("subject", oGene);
			doAnnotation.setReference("ontologyTerm", oDOTerm);
			
			oGene.addToCollection("doAnnotations", doAnnotation);
		}
		
		store(doAnnotationMap.values());
		store(m_oGeneMap.values());
	}

	private Item getDOTerm(String strIdentifier) throws ObjectStoreException {

		if (!m_oDOTermMap.containsKey(strIdentifier)) {

			Item oDOTerm = createItem("DOTerm");
			oDOTerm.setAttribute("identifier", strIdentifier);
//			oDOTerm.setAttribute("cui", strCui);
			store(oDOTerm);
			m_oDOTermMap.put(strIdentifier, oDOTerm);

		}

		return m_oDOTermMap.get(strIdentifier);

	}

	private Item getDOAnnotation(String geneDoString) {
		Item ret = doAnnotationMap.get(geneDoString);
		if (ret == null ) {
			ret = createItem("DOAnnotation");
			doAnnotationMap.put(geneDoString, ret);
		}
		return ret;
	}

	private Item getGeneRIF(String strSentence, String strPhrase, String strScore, String pubMedIds)
			throws ObjectStoreException {

		Item ret = createItem("GeneRIF");
		ret.setAttribute("sentence", strSentence);
		ret.setAttribute("phrase", strPhrase);
		ret.setAttribute("score", strScore);

		for (String pubmedId : pubMedIds.split(",")) {
			ret.addToCollection("publications", getPublication(pubmedId));
		}
		
		store(ret);

		return ret;
	}

	private Item getGene(String ncbiGeneId) throws ObjectStoreException {

		if (!m_oGeneMap.containsKey(ncbiGeneId)) {

			Item oGene = createItem("Gene");
			oGene.setAttribute("ncbiGeneId", ncbiGeneId);
			m_oGeneMap.put(ncbiGeneId, oGene);

		}

		return m_oGeneMap.get(ncbiGeneId);

	}

	// chenyian: reference to Publication class instead of using Integer
	private String getPublication(String pubmedId) throws ObjectStoreException {
		String ret = publicationiMap.get(pubmedId);
		if (ret == null) {
			Item publication = createItem("Publication");
			publication.setAttribute("pubMedId", pubmedId);
			store(publication);
			ret = publication.getIdentifier();
			publicationiMap.put(pubmedId, ret);
		}
		return ret;
	}
}
