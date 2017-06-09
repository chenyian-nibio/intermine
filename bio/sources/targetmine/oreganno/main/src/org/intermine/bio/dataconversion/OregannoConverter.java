package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class OregannoConverter extends BioFileConverter {
	// private static final Logger LOG = Logger.getLogger(OregannoConverter.class);

	//
	private static final String DATASET_TITLE = "ORegAnno";
	private static final String DATA_SOURCE_NAME = "ORegAnno";

	// so far only human
	private static final String TAXON_ID = "9606";

	private Map<String, String> geneMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public OregannoConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(reader));

		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			// source target sequence start end chromosome strand genomeBuild stableId
			String sourceId = cols[0].trim();
			String targetId = cols[1].trim();
			if (sourceId == null || sourceId.equals("")) {
				continue;
			}
			
			Item bindingStie = createItem("TFBindingSite");
			bindingStie.setAttribute("primaryIdentifier", cols[8]);
			bindingStie.setAttribute("name", String.format("TF %s target %s", sourceId, targetId));
			bindingStie.setAttribute("length", String.valueOf(cols[2].length()));
			bindingStie.setReference("sequence", createSequence(cols[2]));
			bindingStie.setReference("organism", getOrganism(TAXON_ID));
			
			if (!cols[3].equals("N/A") && !cols[4].equals("N/A") && !cols[5].equals("N/A")
					&& !cols[6].equals("N/A")) {
				String chromosomeRefId = getChromosome(cols[5], TAXON_ID);

				Item location = createItem("Location");
				location.setAttribute("start", cols[3]);
				location.setAttribute("end", cols[4]);
				location.setAttribute("strand", cols[6]);
				// TODO this part is not clear, comment out temporary...
//				location.setReference("feature", getGene(targetId));
				location.setReference("locatedOn", chromosomeRefId);
				store(location);
				
				bindingStie.setReference("chromosome", chromosomeRefId);
				bindingStie.setReference("chromosomeLocation", location);
			}
			store(bindingStie);

			getInteraction(sourceId, targetId).addToCollection("bindingSites", bindingStie);

		}
		reader.close();
	}

	Map<String, Item> interactionMap = new HashMap<String, Item>();

	private Item getInteraction(String tfGeneId, String targetGeneId) throws ObjectStoreException {
		String key = tfGeneId + "->" + targetGeneId;
		Item ret = interactionMap.get(key);
		if (ret == null) {
			ret = createItem("TranscriptionalRegulation");
			ret.setAttribute("name", key);
			ret.setReference("transcriptionFactor", getGene(tfGeneId));
			ret.setReference("targetGene", getGene(targetGeneId));

			interactionMap.put(key, ret);
		}
		return ret;
	}

	private String getGene(String primaryIdentifier) throws ObjectStoreException {
		String ret = geneMap.get(primaryIdentifier);
		if (ret == null) {
			Item item = createItem("Gene");
			item.setAttribute("primaryIdentifier", primaryIdentifier);
			item.setAttribute("ncbiGeneId", primaryIdentifier);
			store(item);
			ret = item.getIdentifier();
			geneMap.put(primaryIdentifier, ret);
		}
		return ret;
	}

	private Map<String, String> chromosomeMap = new HashMap<String, String>();

	private String getChromosome(String chr, String taxonId) throws ObjectStoreException {
		String key = chr + ":" + taxonId;
		String ret = chromosomeMap.get(key);
		if (ret == null) {
			Item item = createItem("Chromosome");
			String chrId = chr;
			if (chr.toLowerCase().startsWith("chr")) {
				chrId = chr.substring(3);
			}
			item.setAttribute("symbol", chrId);
			item.setReference("organism", getOrganism(taxonId));
			store(item);
			ret = item.getIdentifier();
			chromosomeMap.put(key, ret);
		}
		return ret;
	}

	private String createSequence(String sequence) throws ObjectStoreException {
		Item item = createItem("Sequence");
		item.setAttribute("residues", sequence);
		item.setAttribute("length", String.valueOf(sequence.length()));
		store(item);
		return item.getIdentifier();
	}

	@Override
	public void close() throws Exception {
		store(interactionMap.values());
	}

}
