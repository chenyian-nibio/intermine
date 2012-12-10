package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class GwasConverter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(GwasConverter.class);
	//
	private static final String DATASET_TITLE = "Genome-Wide Association Studies";
	private static final String DATA_SOURCE_NAME = "National Human Genome Research Institute";

	private Map<String, String> diseaseMap = new HashMap<String, String>();

	private Map<String, String> geneMap = new HashMap<String, String>();
	private Map<String, String> gwaMap = new HashMap<String, String>();
	private Map<String, String> pubMap = new HashMap<String, String>();
	private Map<String, String> doMap = new HashMap<String, String>();
	private Map<String, Item> snpMap = new HashMap<String, Item>();

	private File diseaseMapFile;

	public void setDiseaseMapFile(File diseaseMapFile) {
		this.diseaseMapFile = diseaseMapFile;
	}

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public GwasConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		if (diseaseMap.isEmpty()) {
			LOG.info("Read diseaseMap file......");
			readDiseaseMap();
		}
		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(reader));
		// sikp header
		iterator.next();
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			if (cols.length < 2) {
				continue;
			}
			String snpGeneId = cols[17];
			// skip those intergenic snp or snp mapping to multiple genes
			if (verifySnp(snpGeneId)) {
				String gwaRefId = getGenomeWideAssociation(cols[7], cols[1]);
				String geneRefId = getGene(snpGeneId);
				Item snp = getSnp(cols[21], cols[24]);
				snp.setReference("gene", geneRefId);
				snp.addToCollection("genomeWideAssociations", gwaRefId);
			}
		}

		store(snpMap.values());
	}

	private boolean verifySnp(String snpGeneId) {
		return !(StringUtils.isEmpty(snpGeneId) || snpGeneId.contains(","));
	}

	private Item getSnp(String dbSnpId, String context) {
		Item ret = snpMap.get(dbSnpId);
		if (ret == null) {
			ret = createItem("SNP");
			ret.setAttribute("identifier", dbSnpId);
			if (!StringUtils.isEmpty(context)) {
				ret.setAttribute("context", context);
			}
			snpMap.put(dbSnpId, ret);
		}
		return ret;
	}

	private String getGene(String geneId) throws ObjectStoreException {
		String ret = geneMap.get(geneId);
		if (ret == null) {
			Item item = createItem("Gene");
			item.setAttribute("ncbiGeneId", geneId);
			store(item);
			ret = item.getIdentifier();
			geneMap.put(geneId, ret);
		}
		return ret;
	}

	private String getPublication(String pubMedId) throws ObjectStoreException {
		String ret = pubMap.get(pubMedId);
		if (ret == null) {
			Item item = createItem("Publication");
			item.setAttribute("pubMedId", pubMedId);
			store(item);
			ret = item.getIdentifier();
			pubMap.put(pubMedId, ret);
		}
		return ret;
	}

	private String getGenomeWideAssociation(String trait, String pubMedId)
			throws ObjectStoreException {
		String ret = gwaMap.get(trait + pubMedId);
		if (ret == null) {
			Item item = createItem("GenomeWideAssociation");
			item.setAttribute("trait", trait);
			item.setReference("publication", getPublication(pubMedId));
			String doTerms = diseaseMap.get(trait);
			if (doTerms != null) {
				String[] terms = doTerms.split(",");
				for (String term : terms) {
					item.addToCollection("doTerms", getDoTerm(term));
				}
			} else {
//				LOG.info("DOTerm for '" + trait + "' not found. ");
			}
			store(item);
			ret = item.getIdentifier();
			gwaMap.put(trait + pubMedId, ret);
		}
		return ret;
	}

	private String getDoTerm(String doId) throws ObjectStoreException {
		String ret = doMap.get(doId);
		if (ret == null) {
			Item item = createItem("DOTerm");
			item.setAttribute("identifier", doId);
			store(item);
			ret = item.getIdentifier();
			doMap.put(doId, ret);
		}
		return ret;
	}

	private void readDiseaseMap() {
		try {
			Iterator<String[]> iterator = FormattedTextParser
					.parseTabDelimitedReader(new BufferedReader(new FileReader(diseaseMapFile)));
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				if (cols.length < 2) {
					continue;
				}
				diseaseMap.put(cols[0], cols[1]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
