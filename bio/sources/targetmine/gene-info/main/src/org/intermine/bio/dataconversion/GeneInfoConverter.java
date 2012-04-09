package org.intermine.bio.dataconversion;

/*
 * Copyright (C) 2002-2009 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.util.StringUtil;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class GeneInfoConverter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(GeneInfoConverter.class);
	private static final String PROP_FILE = "gene-info_config.properties";
	//
	private static final String DATASET_TITLE = "Entrez Gene";
	private static final String DATA_SOURCE_NAME = "NCBI";

	private Set<String> taxonIds;

	private Map<String, String> chromosomeMap = new HashMap<String, String>();
	private Map<String, Set<String>> transcriptMap;
	private Map<String, Set<String>> translationMap;

	private Map<String, Set<String>> unigeneMap;
	private List<String> uniGeneSpecies;

	private Map<String, Map<String, String>> idMap;
	private Map<String, String> synonyms = new HashMap<String, String>();

	private Set<String> primaryIds = new HashSet<String>();

	private File gene2ensemblFile;
	private File gene2unigeneFile;

	public void setGeneinfoOrganisms(String taxonIds) {
		this.taxonIds = new HashSet<String>(Arrays.asList(StringUtil.split(taxonIds, " ")));
		LOG.info("Setting list of organisms to " + this.taxonIds);
	}

	public void setUnigeneOrganisms(String species) {
		this.uniGeneSpecies = Arrays.asList(StringUtil.split(species, " "));
		LOG.info("Only extract UniGene mapping for following species: " + this.uniGeneSpecies);
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
		if (transcriptMap == null) {
			readGene2ensembl();
		}
		if (unigeneMap == null) {
			processGene2unigene();
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
			String name = cols[8].trim();
			String desc = cols[13].trim();
			String symbol = cols[2].trim();
			String type = cols[9].trim();
			String chromosome = cols[6].trim();
			String dbXrefs = cols[5].trim();
			// String mapLocation = cols[7].trim();

			if (!taxonIds.contains(taxId)) {
				continue;
			}

			Item gene = createItem("Gene");
			gene.setAttribute("ncbiGeneNumber", geneId);
			gene.setAttribute("name", name);
			gene.setAttribute("description", desc);
			gene.setReference("organism", getOrganism(taxId));
			// check if the gene is micro RNA
			if (type.equals("miscRNA") && dbXrefs.contains("miRBase:")){
				gene.setAttribute("type", "microRNA");
			} else {
				gene.setAttribute("type", type);
			}

			String geneRefId = gene.getIdentifier();

			List<String> allSynonyms = new ArrayList<String>();

			if (!cols[5].trim().equals("-")) {
				Map<String, String> dbNameIdMap = processDbXrefs(cols[5]);

				for (String idType : idMap.get(taxId).keySet()) {
					String dbId = dbNameIdMap.get(idMap.get(taxId).get(idType));

					if (dbId != null) {
						if (idType.equals("primaryIdentifier")) {
							if (primaryIds.contains(dbId)) {
								continue;
							}
							primaryIds.add(dbId);
						}
						gene.setAttribute(idType, dbId);
						// add synonym for identifier
						String synId = getSynonym(geneRefId, dbId, "");
						if (synId != null) {
							allSynonyms.add(synId);
						}
					}
				}

			}
			if (!StringUtils.isEmpty(symbol) && !symbol.equals("-")) {
				gene.setAttribute("symbol", symbol);
				String synId = getSynonym(geneRefId, symbol, "");
				if (synId != null) {
					allSynonyms.add(synId);
				}
			}
			// add other synonyms
			if (!cols[4].trim().equals("-")) {
				for (String s : cols[4].trim().split("\\|")) {
					String synId = getSynonym(geneRefId, s, "");
					if (synId != null) {
						allSynonyms.add(synId);
					}
				}
			}

			gene.setReference("chromosome", getChromosome(taxId, chromosome));

			// 2011/6/20
			// Transcript and Translation classes are not really useful ...
			// These identifiers (Ensembl, RefSeq) are set as synonyms
			if (transcriptMap.get(geneId) != null) {
				for (String pairs : transcriptMap.get(geneId)) {
					for (String s : pairs.split("\\|")) {
						String synId = getSynonym(geneRefId, s, "");
						if (synId != null) {
							allSynonyms.add(synId);
						}
					}
				}
			}
			if (translationMap.get(geneId) != null) {
				for (String pairs : translationMap.get(geneId)) {
					for (String s : pairs.split("\\|")) {
						String synId = getSynonym(geneRefId, s, "");
						if (synId != null) {
							allSynonyms.add(synId);
						}
					}
				}
			}

			// 2011/6/20
			// Include UniGene id as synonyms
			if (unigeneMap.get(geneId) != null) {
				for (String string : unigeneMap.get(geneId)) {
					String synId = getSynonym(geneRefId, string, "");
					allSynonyms.add(synId);
				}
			}

			if (!allSynonyms.isEmpty()) {
				gene.setCollection("synonyms", allSynonyms);
			}

			store(gene);

		}
	}

	private String getChromosome(String taxId, String no) throws ObjectStoreException {
		String ret = chromosomeMap.get(taxId + "-" + no);
		if (ret == null) {
			Item chromosome = createItem("Chromosome");
			chromosome.setReference("organism", getOrganism(taxId));
			chromosome.setAttribute("primaryIdentifier", no);
			store(chromosome);
			ret = chromosome.getIdentifier();
			chromosomeMap.put(taxId + "-" + no, ret);
		}
		return ret;
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

	// type and isPrimary are deprecated from v0.94
	private String getSynonym(String subjectId, String value, String datasetRefId)
			throws ObjectStoreException {
		String key = subjectId + value;
		if (StringUtils.isEmpty(value)) {
			return null;
		}
		String refId = synonyms.get(key);
		if (refId == null) {
			Item item = createItem("Synonym");
			item.setReference("subject", subjectId);
			item.setAttribute("value", value);
			refId = item.getIdentifier();
			store(item);
			synonyms.put(key, refId);
		}
		return refId;
	}

	private void readGene2ensembl() {
		LOG.info("Parsing the file gene2ensembl......");

		transcriptMap = new HashMap<String, Set<String>>();
		translationMap = new HashMap<String, Set<String>>();

		// #Format: tax_id GeneID Ensembl_gene_identifier RNA_nucleotide_accession.version
		// Ensembl_rna_identifier protein_accession.version Ensembl_protein_identifier (tab is used
		// as a separator, pound sign - start of a comment)
		try {

			Reader reader = new BufferedReader(new FileReader(gene2ensemblFile));
			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
			// generate gene -> transcrip/translation map
			while (iterator.hasNext()) {
				String[] cols = iterator.next();

				if (taxonIds.contains(cols[0])) {
					if (!cols[3].equals("-")) {
						Set<String> transcriptSet = transcriptMap.get(cols[1]);
						if (transcriptSet == null) {
							transcriptSet = new HashSet<String>();
							transcriptMap.put(cols[1], transcriptSet);
						}
						transcriptSet.add(cols[3] + "|" + cols[4]);
					}
					if (!cols[5].equals("-")) {
						Set<String> translationSet = translationMap.get(cols[1]);
						if (translationSet == null) {
							translationSet = new HashSet<String>();
							translationMap.put(cols[1], translationSet);
						}
						translationSet.add(cols[5] + "|" + cols[6]);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("The file 'gene2ensembl' not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	public void setGene2ensemblFile(File gene2ensemblFile) {
		this.gene2ensemblFile = gene2ensemblFile;
	}

	public void setGene2unigeneFile(File gene2unigeneFile) {
		this.gene2unigeneFile = gene2unigeneFile;
	}

	private void processGene2unigene() {
		unigeneMap = new HashMap<String, Set<String>>();
		// #Format: GeneID UniGene_cluster (tab is used as a separator, pound sign - start of a
		// comment)
		try {
			Iterator<String[]> iterator = FormattedTextParser
					.parseTabDelimitedReader(new BufferedReader(new FileReader(gene2unigeneFile)));
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
