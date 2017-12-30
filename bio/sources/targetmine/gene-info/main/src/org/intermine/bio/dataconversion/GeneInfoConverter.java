package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.metadata.StringUtil;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * many integrations have been moved to gene-esummary source (2017.4.24)
 *  
 * @author chenyian
 */
public class GeneInfoConverter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(GeneInfoConverter.class);
	private static final String PROP_FILE = "gene-info_config.properties";
	//
	private static final String DATASET_TITLE = "Gene";
	private static final String DATA_SOURCE_NAME = "NCBI";

	private Set<String> taxonIds;

	private Map<String, Set<String>> unigeneMap;
	private List<String> uniGeneSpecies;

	private Map<String, Set<String>> accessionMap;
	private Map<String, Set<String>> ucscMap;
	private Map<String, Set<String>> historyMap;

	private Map<String, Map<String, String>> idMap;

	private File gene2unigeneFile;
	private File gene2accessionFile;
	private File knownToLocusLinkFile;
	private File genehistoryFile;

	public void setGene2unigeneFile(File gene2unigeneFile) {
		this.gene2unigeneFile = gene2unigeneFile;
	}

	public void setGene2accessionFile(File gene2accessionFile) {
		this.gene2accessionFile = gene2accessionFile;
	}

	public void setKnownToLocusLinkFile(File knownToLocusLinkFile) {
		this.knownToLocusLinkFile = knownToLocusLinkFile;
	}

	public void setGenehistoryFile(File genehistoryFile) {
		this.genehistoryFile = genehistoryFile;
	}

	public void setGeneinfoOrganisms(String taxonIds) {
		this.taxonIds = new HashSet<String>(Arrays.asList(StringUtil.split(taxonIds, " ")));
		LOG.info("Setting list of organisms to " + this.taxonIds);
		System.out.println("Setting list of organisms to " + this.taxonIds);
	}

	public void setUnigeneOrganisms(String species) {
		this.uniGeneSpecies = Arrays.asList(StringUtil.split(species, " "));
		LOG.info("Only extract UniGene mapping for following species: " + this.uniGeneSpecies);
		System.out.println("Only extract UniGene mapping for following species: "
				+ this.uniGeneSpecies);
	}

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public GeneInfoConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	private void readConfigFile() {
		LOG.info("Reading configuration file......");

		Properties properties = new Properties();
		try {
			properties.load(getClass().getClassLoader().getResourceAsStream(PROP_FILE));
		} catch (IOException e) {
			throw new RuntimeException("Problem loading properties '" + PROP_FILE + "'", e);
		}
		initIdentifierMap();

		for (Entry<Object, Object> entry : properties.entrySet()) {

			String key = (String) entry.getKey();
			String value = (String) entry.getValue();

			String[] split = key.trim().split("\\.");
			if (idMap.get(split[0]) == null) {
				LOG.error("Unrecognized organism id: " + split[0]
						+ ", which is not specified in property: geneinfo.organisms. ");
				continue;
			}
			idMap.get(split[0]).put(split[1], value.trim());
		}

	}

	private void initIdentifierMap() {
		idMap = new HashMap<String, Map<String, String>>();
		for (String taxId : taxonIds) {
			idMap.put(taxId, new HashMap<String, String>());
		}
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {

		if (idMap == null) {
			readConfigFile();
		}
		if (unigeneMap == null) {
			processGene2unigene();
		}
		if (accessionMap == null) {
			processGene2accession();
		}
		if (ucscMap == null) {
			processKnownToLocusLink();
		}
		if (historyMap == null) {
			processGeneHistoryFile();
		}

		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(reader));

		// #Format: tax_id GeneID Symbol LocusTag Synonyms dbXrefs chromosome map_location
		// description
		// type_of_gene Symbol_from_nomenclature_authority Full_name_from_nomenclature_authority
		// Nomenclature_status Other_designations Modification_date
		// (tab is used as a separator, pound sign - start of a comment)

		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String taxId = cols[0].trim();
			String geneId = cols[1].trim();
			String type = cols[9].trim();
			String dbXrefs = cols[5].trim();

			if (!taxonIds.contains(taxId)) {
				continue;
			}

			Set<String> geneSynonyms = new HashSet<String>();
			Item gene = createItem("Gene");
			// 2013/8/1 set NCBI gene id as primaryIdentifier
			gene.setAttribute("primaryIdentifier", geneId);
			gene.setAttribute("ncbiGeneId", geneId);
			gene.setReference("organism", getOrganism(taxId));
			// check if the gene is micro RNA
			if (type.equals("miscRNA") && dbXrefs.contains("miRBase:")) {
				gene.setAttribute("type", "microRNA");
			} else {
				gene.setAttribute("type", type);
			}

			if (!dbXrefs.equals("-")) {
				Map<String, String> dbNameIdMap = processDbXrefs(dbXrefs);

				String dbId = dbNameIdMap.get(idMap.get(taxId).get("secondaryIdentifier"));

				if (dbId != null) {
					// e.g. MGI:MGI:88052 or HGNC:HGNC:603
					if (dbId.indexOf(":") != dbId.lastIndexOf(":")) {
						dbId = dbId.substring(dbId.indexOf(":") + 1);
					}
					gene.setAttribute("secondaryIdentifier", dbId);
					// also add secondaryIdentifier as synonym
					geneSynonyms.add(dbId);
				}

				// 2013/8/1
				// save Ensembl ID as synonym
				String ensemblId = dbNameIdMap.get("Ensembl");
				if (ensemblId != null) {
					// add synonym for identifier
					geneSynonyms.add(ensemblId);
				}

			}

			// 2012/7/17
			// Add accession identifiers
			// Due to the quantity of id, the identifiers would be stored without version number.
			if (accessionMap.get(geneId) != null) {
				for (String s : accessionMap.get(geneId)) {
					String acc = s.contains(".") ? s.substring(0, s.indexOf(".")) : s;
					geneSynonyms.add(acc);
					// Store an extra RefSeq id with version tags
					if (s.contains("_")) {
						geneSynonyms.add(s);
					}
				}
			}

			// 2013/1/11
			// Add ucsc id as synonyms
			// The identifiers would be stored without version number.
			if (ucscMap.get(geneId) != null) {
				for (String s : ucscMap.get(geneId)) {
					String ucscId = s.contains(".") ? s.substring(0, s.indexOf(".")) : s;
					geneSynonyms.add(ucscId);
				}
			}

			// 2011/6/20
			// Include UniGene id as synonyms
			if (unigeneMap.get(geneId) != null) {
				for (String string : unigeneMap.get(geneId)) {
					geneSynonyms.add(string);
				}
			}

			// 2014/4/3
			// Include deprecated gene id as synonyms
			if (historyMap.get(geneId) != null) {
				for (String string : historyMap.get(geneId)) {
					geneSynonyms.add(string);
				}
			}

			store(gene);
			
			for (String alias : geneSynonyms) {
				Item item = createItem("Synonym");
				item.setReference("subject", gene.getIdentifier());
				item.setAttribute("value", alias);
				store(item);
			}
		}
	}

	private void processGeneHistoryFile() {
		LOG.info("Parsing the file gene_history file......");
		System.out.println("Parsing the file gene_history file......");
		historyMap = new HashMap<String, Set<String>>();
		try {
			FileReader reader = new FileReader(genehistoryFile);
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				if (taxonIds.contains(cols[0])) {
					if (historyMap.get(cols[1]) == null) {
						historyMap.put(cols[1], new HashSet<String>());
					}
					historyMap.get(cols[1]).add(cols[2]);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void processKnownToLocusLink() {
		LOG.info("Parsing the file knownToLocusLink......");
		System.out.println("Parsing the file knownToLocusLink......");
		ucscMap = new HashMap<String, Set<String>>();

		try {
			BufferedReader in = new BufferedReader(new FileReader(knownToLocusLinkFile));
			String line;
			while ((line = in.readLine()) != null) {
				String[] cols = line.split("\\t");
				if (ucscMap.get(cols[1]) == null) {
					ucscMap.put(cols[1], new HashSet<String>());
				}
				ucscMap.get(cols[1]).add(cols[0]);
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("The file 'knownToLocusLink' not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	private void processGene2accession() {
		LOG.info("Parsing the file gene2accession......");
		System.out.println("Parsing the file gene2accession......");
		accessionMap = new HashMap<String, Set<String>>();

		try {
			FileReader reader = new FileReader(gene2accessionFile);
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				if (taxonIds.contains(cols[0].trim())) {
					// take the 4th column as the synonyms
					String geneId = cols[1];
					if (!cols[3].trim().equals("-")) {
						Set<String> identifierSet = accessionMap.get(geneId);
						if (identifierSet == null) {
							identifierSet = new HashSet<String>();
							accessionMap.put(geneId, identifierSet);
						}
						identifierSet.add(cols[3]);
					}
				}
			}
			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("The file 'gene2accession' not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	private Map<String, String> processDbXrefs(String allDbRefString) {
		Map<String, String> ret = new HashMap<String, String>();

		// Genes with more than one id in specific database would be skip in this step
		// e.g. 1 gene id -> 2 ensembl id
		String[] values = allDbRefString.split("\\|");
		for (String dbRef : values) {
			String[] dbValue = dbRef.split(":");
			if (dbValue[0].equals("Ensembl") || dbValue[0].equals("FLYBASE")) {
				ret.put(dbValue[0], dbValue[1]);
			} else {
				ret.put(dbValue[0], dbRef);
			}
		}
		return ret;
	}

	private void processGene2unigene() {
		System.out.println("Parsing the file gene2unigeneFile......");
		unigeneMap = new HashMap<String, Set<String>>();
		// #Format: GeneID UniGene_cluster (tab is used as a separator, pound sign - start of a
		// comment)
		try {
			Iterator<String[]> iterator = FormattedTextParser
					.parseTabDelimitedReader(new FileReader(gene2unigeneFile));
			// skip header
			iterator.next();
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				if (uniGeneSpecies.contains(cols[1].split("\\.")[0])) {
					if (unigeneMap.get(cols[0]) == null) {
						unigeneMap.put(cols[0], new HashSet<String>());
					}
					unigeneMap.get(cols[0]).add(cols[1]);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("The file 'gene2unigene' not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
