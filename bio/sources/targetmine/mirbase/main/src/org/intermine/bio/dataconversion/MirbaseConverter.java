package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class MirbaseConverter extends BioFileConverter {
	// private static Logger LOG = Logger.getLogger(MirbaseConverter.class);
	//
	private static final String DATASET_TITLE = "miRBase";
	private static final String DATA_SOURCE_NAME = "miRBase";

	private Map<String, String> taxonIdMap;
	private Map<String, String> geneMap = new HashMap<String, String>();
	private Map<String, String> publicationMap = new HashMap<String, String>();
	private Map<String, Item> matureMap = new HashMap<String, Item>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public MirbaseConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		if (taxonIdMap == null) {
			readTaxonIdMap();
		}

		String fileName = getCurrentFile().getName();
		if (fileName.equals("miRNA.dat")) {
			System.out.println("processing miRNA.dat file...");
			processMirnaDat(reader);
		} else if (fileName.equals("mature.fa")) {
			System.out.println("processing mature.fa file...");
			processMatureFa(reader);
		}

	}

	private File taxonIdFile;

	public void setTaxonIdFile(File taxonIdFile) {
		this.taxonIdFile = taxonIdFile;
	}

	private void readTaxonIdMap() throws Exception {
		taxonIdMap = new HashMap<String, String>();
		FileReader fileReader = new FileReader(taxonIdFile);
		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(fileReader);
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			taxonIdMap.put(cols[1], cols[0]);
		}
	}

	private void processMatureFa(Reader reader) throws Exception {
		BufferedReader in = null;
		in = new BufferedReader(reader);
		String line;
		while ((line = in.readLine()) != null) {
			if (line.startsWith(">")) {
				// >hsa-let-7a-5p MIMAT0000062 Homo sapiens let-7a-5p
				Pattern pattern = Pattern.compile(">(.+) (MIMAT\\d+) (.+)");
				Matcher matcher = pattern.matcher(line);
				if (matcher.matches()) {
					String id = matcher.group(1);
					String accession = matcher.group(2);
					String name = matcher.group(3);
					Item item = matureMap.get(accession);
					if (item == null) {
						item = createItem("MiRNA");
						item.setAttribute("primaryIdentifier", accession);
					}
					item.setAttribute("secondaryIdentifier", id);
					item.setAttribute("name", name);
					item.setAttribute("symbol", id.substring(id.indexOf("-")+1));
					String seq = in.readLine();
					item.setReference("sequence", createSequence(seq));
					item.setAttribute("length", String.valueOf(seq.length()));
					item.setReference("organism", getOrganism(getTaxonIdByIdentifier(id)));
					matureMap.put(accession, item);

				}
			}

		}

	}
	
	private String getTaxonIdByIdentifier(String identifier) {
		String taxonId = taxonIdMap.get(identifier.substring(0, identifier.indexOf("-")));
		if (taxonId != null) {
			return taxonId;
		} else {
			throw new RuntimeException("unexpected organism code: "
					+ identifier.substring(0, 3) + " from entry: " + identifier);
		}
	}

	private void processMirnaDat(Reader reader) throws Exception {
		BufferedReader in = null;
		in = new BufferedReader(reader);
		String line;
		MicroRNAEntry entry = new MicroRNAEntry();
		while ((line = in.readLine()) != null) {
			// process
			if (line.startsWith("ID")) {
				// ID hsa-let-7a-1 standard; RNA; HSA; 80 BP.
				Pattern pattern = Pattern.compile("ID\\s+(.+?)\\s+\\w+; \\w+; \\w+; \\d+ BP\\.");
				Matcher matcher = pattern.matcher(line);
				if (matcher.matches()) {
					entry.identifier = matcher.group(1);
				}
			} else if (line.startsWith("AC")) {
				entry.accession = line.substring(5, 14);
			} else if (line.startsWith("DE")) {
				entry.description = line.substring(5);
			} else if (line.startsWith("RX")) {
				entry.pubMedIds.add(line.substring(line.indexOf(";") + 2, line.indexOf(".")));
			} else if (line.startsWith("FT")) {
				String substring = line.substring(line.indexOf("/") + 1);
				if (substring.startsWith("accession")) {
					entry.matures.add(substring.substring(11, substring.length() - 1));
				}
			} else if (line.startsWith("DR")) {
				// DR ENTREZGENE; 406881; MIRLET7A1.
				// DR   ENTREZGENE; 100422902; MIR3116-1.
				Pattern pattern = Pattern.compile("DR\\s+ENTREZGENE; (\\d+); (.+)\\.");
				Matcher matcher = pattern.matcher(line);
				if (matcher.matches()) {
					entry.geneId = matcher.group(1);
				}
			} else if (line.startsWith("//")) {
				// create MicroRNA
				createMicroRNA(entry);
				entry = new MicroRNAEntry();
			} else if (line.startsWith("  ")) {
				entry.sequence += line;
			}
		}
		in.close();
	}

	private void createMicroRNA(MicroRNAEntry entry) throws ObjectStoreException {
		Item item = createItem("MiRNAPrimaryTranscript");
		item.setAttribute("primaryIdentifier", entry.accession);
		item.setAttribute("secondaryIdentifier", entry.identifier);
		item.setAttribute("name", entry.description);
		item.setAttribute("symbol", entry.identifier.substring(entry.identifier.indexOf("-")+1));
		item.setReference("sequence", createSequence(entry.getSequence()));
		item.setAttribute("length", String.valueOf(entry.getSequence().length()));
		if (entry.geneId != null) {
			item.setReference("gene", getGene(entry.geneId));
		}
		for (String pubmedId : entry.pubMedIds) {
			item.addToCollection("publications", getPublication(pubmedId));
		}
		for (String mature : entry.matures) {
			item.addToCollection("microRNAs", getMiRNA(mature, item));
		}
		item.setReference("organism", getOrganism(getTaxonIdByIdentifier(entry.identifier)));
		store(item);
	}

	private String createSequence(String sequence) throws ObjectStoreException {
        Item item = createItem("Sequence");
        item.setAttribute("residues", sequence);
        item.setAttribute("length", String.valueOf(sequence.length()));
        store(item);
		return item.getIdentifier();
	}

	private Item getMiRNA(String accession, Item microRNA) throws ObjectStoreException {
		Item ret = matureMap.get(accession);
		if (ret == null) {
			ret = createItem("MiRNA");
			ret.setAttribute("primaryIdentifier", accession);
			matureMap.put(accession, ret);
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

	private static class MicroRNAEntry {
		String geneId;
		String accession;
		String description;
		String sequence = "";
		Set<String> pubMedIds = new HashSet<String>();
		Set<String> matures = new HashSet<String>();
		String identifier;

		public MicroRNAEntry() {
		}

		@Override
		public String toString() {
			return StringUtils.join(
					Arrays.asList(identifier, accession, geneId, getSequence(),
							pubMedIds.toString(), matures.toString()), "\t");
		}

		public String getSequence() {
			return sequence.replaceAll("\\d+", "").replaceAll("\\s+", "");
		}
	}

	@Override
	public void close() throws Exception {
		store(matureMap.values());
	}
}
