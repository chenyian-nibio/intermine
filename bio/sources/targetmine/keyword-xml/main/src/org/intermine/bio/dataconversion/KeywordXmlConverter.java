package org.intermine.bio.dataconversion;

import java.io.Reader;

import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.xml.full.Item;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;


/**
 * 
 * @author chenyian
 */
public class KeywordXmlConverter extends BioFileConverter
{
//	private static Logger LOG = Logger.getLogger(KeywordXmlConverter.class);
    //
    private static final String DATASET_TITLE = "UniProt keywords data set";
    private static final String DATA_SOURCE_NAME = "UniProt";
    
    private String ontologyRefId = null;

    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public KeywordXmlConverter(ItemWriter writer, Model model) {
        super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
    }

    /**
     * 
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
    	if (ontologyRefId == null) {
            Item ontology = createItem("Ontology");
            ontology.setAttribute("name", "UniProtKeyword");
            store(ontology);
            ontologyRefId = ontology.getIdentifier();
    	}
    	
		Builder parser = new Builder();
		Document doc = parser.build(reader);

		Elements keywordElements = doc.getRootElement().getChildElements("keyword");
		
		for (int i = 0; i < keywordElements.size(); i++) {
			Element keyword = keywordElements.get(i);
			
			String id = keyword.getAttributeValue("id");
			Elements names = keyword.getChildElements("name");
			String name = names.get(0).getValue();

			String desc = keyword.getChildElements("description").get(0).getValue();
			
//			LOG.info(String.format("%s(%s): %s", id, name, desc.replaceAll("\\s+", " ")));
			
            Item item = createItem("OntologyTerm");
            item.setAttribute("identifier", id);
            item.setAttribute("name", name);
            item.setAttribute("description", desc.replaceAll("\\s+", " "));
            item.setReference("ontology", ontologyRefId);
			for (int k = 1; k < names.size(); k++) {
				Item synonym = createItem("OntologyTermSynonym");
				synonym.setAttribute("name", names.get(k).getValue());
				store(synonym);
				item.addToCollection("synonyms", synonym);
			}
			store(item);
		}

    }
    
}
