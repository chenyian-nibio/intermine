package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;


/**
 * parse pre-retrieved xml data sources from NCBI esummary
 * 
 * @author chenyian
 */
public class GeneEsummaryConverter extends BioFileConverter
{
	private static final Logger LOG = Logger.getLogger(GeneEsummaryConverter.class);
	//
    private static final String DATASET_TITLE = "Gene";
    private static final String DATA_SOURCE_NAME = "NCBI";

	private Map<String, String> chromosomeMap = new HashMap<String, String>();
	
    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public GeneEsummaryConverter(ItemWriter writer, Model model) {
        super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
    }

    /**
     * 
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
    	  
		BufferedReader in = null;  
		try {  
			in = new BufferedReader(reader);  
			String line;
//			StringBuffer sb = new StringBuffer();
			List<String> stringList = new ArrayList<String>();
			while ((line = in.readLine()) != null) {  
				if (line.equals("///")) {
					Builder parser = new Builder();
//					Document doc = parser.build(new StringReader(sb.toString()));
					String string = StringUtils.join(stringList.subList(2, stringList.size()), "\n");
					Document doc = parser.build(new StringReader(string));
					Element entry = doc.getRootElement();

					Elements elements = entry.getChildElements("DocumentSummarySet").get(0).getChildElements("DocumentSummary");

					for (int k = 0; k < elements.size(); k++) {
						Element element = elements.get(k);
						String uid = element.getAttribute("uid").getValue();
						if (element.getChildElements("error").size() > 0) {
							LOG.error("Unable to retrieve gene: " + uid);
						} else {
							Set<String> geneSynonyms = new HashSet<String>();
							Item geneItem = createItem("Gene");
							geneItem.setAttribute("primaryIdentifier", uid);
//							System.out.println("identifier: " + uid);
							//TODO to be deprecated
							geneItem.setAttribute("ncbiGeneId", uid);
							
							String symbol = element.getChildElements("NomenclatureSymbol").get(0).getValue();
							if (StringUtils.isEmpty(symbol)) {
								symbol = element.getChildElements("Name").get(0).getValue();
							}
							geneItem.setAttribute("symbol", symbol);
							geneSynonyms.add(symbol);
							
							String taxonId = element.getChildElements("Organism").get(0).getChildElements("TaxID").get(0).getValue();
							geneItem.setReference("organism", getOrganism(taxonId));

							// TODO (unchecked) should be unnecessary, should not happened
//							String status = element.getChildElements("Status").get(0).getValue();
//							String currentId = element.getChildElements("CurrentID").get(0).getValue();
//							if (!currentId.equals("0")) {
//								geneItem.setAttribute("briefDescription", String.format("This record was replaced with Gene ID: %s", currentId));
//							}
							
							String name = element.getChildElements("NomenclatureName").get(0).getValue();
							if (StringUtils.isEmpty(name)) {
								// the 'description' attribute is more like a name?
								name = element.getChildElements("Description").get(0).getValue();
								if (StringUtils.isEmpty(name)) {
									name = "unavailable";
								}
							}
							geneItem.setAttribute("name", name);
							
							String otherDesignations = element.getChildElements("OtherDesignations").get(0).getValue();
							if (!StringUtils.isEmpty(otherDesignations)) {
								geneSynonyms.addAll(Arrays.asList(otherDesignations.split("\\|")));
							}
							String otherAliases = element.getChildElements("OtherAliases").get(0).getValue();
							if (!StringUtils.isEmpty(otherAliases)) {
								geneSynonyms.addAll(Arrays.asList(otherAliases.split(", ")));
							}

							String summary = element.getChildElements("Summary").get(0).getValue();
							if (!StringUtils.isEmpty(summary)) {
								// store 'summary' attribute in the 'description' field
								geneItem.setAttribute("description", summary);
							}
							
							String chromosome = element.getChildElements("Chromosome").get(0).getValue();
							if (element.getChildElements("GenomicInfo").get(0).getChildElements("GenomicInfoType").size() > 0) {
								Element genomicInfo = element.getChildElements("GenomicInfo").get(0).getChildElements("GenomicInfoType").get(0);
								String chrRef = getChromosome(taxonId, genomicInfo.getChildElements("ChrAccVer").get(0).getValue(), chromosome);
								geneItem.setReference("chromosome", chrRef);
								
								Item location = createItem("Location");
								Integer startValue = Integer.valueOf(genomicInfo.getChildElements("ChrStart").get(0).getValue()) + 1;
								location.setAttribute("start", String.valueOf(startValue));
								Integer endValue = Integer.valueOf(genomicInfo.getChildElements("ChrStop").get(0).getValue()) + 1;
								location.setAttribute("end", String.valueOf(endValue));
								String strand = endValue.intValue() > startValue.intValue() ? "+" : "-";
								location.setAttribute("strand", String.valueOf(strand));
								if (chrRef != null) {
									location.setReference("locatedOn", chrRef);
								}
								location.setReference("feature", geneItem);
								store(location);
								geneItem.setReference("chromosomeLocation", location);
							}

							store(geneItem);
							
							for (String alias : geneSynonyms) {
								Item item = createItem("Synonym");
								item.setReference("subject", geneItem.getIdentifier());
								item.setAttribute("value", alias);
								store(item);
							}

						}
					}
					
//					System.out.println("Processed " + elements.size() + " entries.");
					
//					sb = new StringBuffer();
		        	stringList.clear();
				} else {
//					sb.append(line + "\n");
					stringList.add(line);
				}
			}  
		}  
		catch (IOException e) {  
			LOG.error(e) ;  
		} finally {  
			if(in != null) in.close();  
		}  

    }

	private String getChromosome(String taxonId, String identifier, String symbol) throws ObjectStoreException {
		String key = taxonId + "-" + symbol;
		String ret = chromosomeMap.get(key);
		if (ret == null) {
			Item chromosome = createItem("Chromosome");
			chromosome.setReference("organism", getOrganism(taxonId));
			chromosome.setAttribute("primaryIdentifier", identifier);
			chromosome.setAttribute("symbol", symbol);
			store(chromosome);
			ret = chromosome.getIdentifier();
			chromosomeMap.put(key, ret);
		}
		return ret;
	}

}
