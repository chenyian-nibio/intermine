package org.intermine.bio.dataconversion;

import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class MirtarbaseConverter extends BioFileConverter {
	// private static Logger LOG = Logger.getLogger(MirtarbaseConverter.class);
	//
	private static final String DATASET_TITLE = "miRTarBase";
	private static final String DATA_SOURCE_NAME = "miRTarBase";

	private Map<String, String> geneMap = new HashMap<String, String>();
	private Map<String, String> miRNAMap = new HashMap<String, String>();
	private Map<String, String> publicationMap = new HashMap<String, String>();
	private Map<String, String> experimentMap = new HashMap<String, String>();
	private Map<String, String> interactionMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public MirtarbaseConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String sourceId = cols[0];
			String accession = cols[2];
			String ncbiGeneId = cols[3];
			String experiment = cols[4];
			String pubmedId = cols[5];

			String[] accs = accession.split(",");
			for (int i = 0; i < accs.length; i++) {
				Item item = createItem("MiRNAEvidence");

				item.setReference("interaction", getMiRNAInteraction(accs[i], ncbiGeneId, sourceId));
				item.setReference("publication", getPublication(pubmedId));

				Set<String> expSet = new HashSet<String>(Arrays.asList(experiment.split("//|;")));
				
				for (String exp : expSet) {
					item.addToCollection("experiments", getMiRNAExperiment(exp));
				}

				store(item);

			}

		}

	}

	private String getMiRNAInteraction(String miRNAAcc, String geneId, String sourceId)
			throws ObjectStoreException {
		String identifier = miRNAAcc + "-" + geneId;

		String ret = interactionMap.get(identifier);

		if (ret == null) {
			Item item = createItem("MiRNAInteraction");

			item.setAttribute("identifier", identifier);
			item.setAttribute("sourceId", sourceId);
			item.setReference("targetGene", getGene(geneId));
			item.setReference("miRNA", getMiRNA(miRNAAcc));

			store(item);
			ret = item.getIdentifier();
			interactionMap.put(identifier, ret);
		}

		return ret;

	}

	private String getGene(String ncbiGeneId) throws ObjectStoreException {
		String ret = geneMap.get(ncbiGeneId);
		if (ret == null) {
			Item item = createItem("Gene");
			item.setAttribute("ncbiGeneId", ncbiGeneId);
			store(item);
			ret = item.getIdentifier();
			geneMap.put(ncbiGeneId, ret);
		}
		return ret;
	}

	private String getMiRNA(String accession) throws ObjectStoreException {
		String ret = miRNAMap.get(accession);
		if (ret == null) {
			Item item = createItem("MiRNA");
			item.setAttribute("primaryIdentifier", accession);
			store(item);
			ret = item.getIdentifier();
			miRNAMap.put(accession, ret);
		}
		return ret;
	}

	private String getMiRNAExperiment(String name) throws ObjectStoreException {
		String ret = experimentMap.get(name);
		if (ret == null) {
			Item item = createItem("MiRNAExperiment");
			item.setAttribute("name", name);
			store(item);
			ret = item.getIdentifier();
			experimentMap.put(name, ret);
		}
		return ret;
	}

	private String getPublication(String pubmedId) throws ObjectStoreException {
		String ret = publicationMap.get(pubmedId);
		if (ret == null) {
			Item item = createItem("Publication");
			item.setAttribute("pubMedId", pubmedId);
			store(item);
			ret = item.getIdentifier();
			publicationMap.put(pubmedId, ret);
		}
		return ret;
	}

}
