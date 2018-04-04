package org.intermine.bio.dataconversion;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/*
 * Copyright (C) 2002-2016 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.sql.Database;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class DbsnpConverter extends BioDBConverter
{
	private static final Logger LOG = Logger.getLogger(DbsnpConverter.class);
	// 
    private static final String DATASET_TITLE = "dbSNP";
    private static final String DATA_SOURCE_NAME = "NCBI";

	private static final int HUMAN_TAXON_ID = 9606;
	private static final int BATCH_SIZE = 10000;

	private static final String PRIMARY_ASSEMBLY = "Primary_Assembly";
	private static final int CDS_REFERENCE = 8;
	private static final String REF = "ref";

    /**
     * Construct a new DbsnpConverter.
     * @param database the database to read from
     * @param model the Model used by the object store we will write to with the ItemWriter
     * @param writer an ItemWriter used to handle Items created
     */
    public DbsnpConverter(Database database, Model model, ItemWriter writer) {
        super(database, model, writer, DATA_SOURCE_NAME, DATASET_TITLE);
    }


    /**
     * {@inheritDoc}
     */
    public void process() throws Exception {
		// a database has been initialised from properties starting with db.dbsnp

		Connection connection = getDatabase().getConnection();

		// process data with direct SQL queries on the source database, for example:
		Statement stmt = connection.createStatement();
		String querySnpFunc = "SELECT fxn_class, abbrev, descrip, SO_id FROM SnpFunctionCode;";
		ResultSet snpFuncRes = stmt.executeQuery(querySnpFunc);
		Map<Integer, String> functionMap = new HashMap<Integer, String>();
		while (snpFuncRes.next()) {
			int classId = snpFuncRes.getInt("fxn_class");
			Item item = createItem("SNPFunction");
			item.setAttribute("name", snpFuncRes.getString("abbrev"));
			item.setAttribute("description", snpFuncRes.getString("descrip"));
			store(item);
			functionMap.put(Integer.valueOf(classId), item.getIdentifier());
		}
		
		long maxSnpId = 0;
		String queryMaxSnpId = "SELECT max(snp_id) FROM SNP;";
		ResultSet maxSnpIdRes = stmt.executeQuery(queryMaxSnpId);
		if (maxSnpIdRes.next()) {
			maxSnpId = maxSnpIdRes.getLong(1);
		} else {
			throw new RuntimeException("CANNOT get the maximal snp id.");
		}
		
		for (int i = 0; i < maxSnpId; i = i + BATCH_SIZE) {
			
			// get allele
			String querySnpAllele = String.format(" SELECT snp_id, var_str " + 
					" FROM SNP JOIN UniVariation ON SNP.univar_id = UniVariation.univar_id " + 
					" WHERE SNP.snp_id > %d " + " AND SNP.snp_id <= %d ", i, i + BATCH_SIZE);
			ResultSet snpAlleleRes = stmt.executeQuery(querySnpAllele);
			Map<String, String> alleleMap = new HashMap<String, String>(); 
			while (snpAlleleRes.next()) {
				String snpId = snpAlleleRes.getString("snp_id");
				String allele = snpAlleleRes.getString("var_str");
				alleleMap.put(snpId, allele);
			}
			if (alleleMap.size() == 0) {
				System.out.println(String.format("%d - %d skipped!", i, i + BATCH_SIZE));  // to be deleted
				LOG.info(String.format("%d - %d skipped!", i, i + BATCH_SIZE));
				continue;
			}
			System.out.println(String.format("do some process for %d - %d", i, i + BATCH_SIZE));  // to be deleted
			LOG.info(String.format("do some process for %d - %d", i, i + BATCH_SIZE));

			// get pubmed
			String queryPubmed = String.format(" SELECT snp_id, pubmed_id " + 
					" FROM SNPPubmed " + " WHERE snp_id > %d AND snp_id <= %d ", i, i + BATCH_SIZE);
			ResultSet pubmedRes = stmt.executeQuery(queryPubmed);
			Map<String, Set<String>> pubmedMap = new HashMap<String, Set<String>>(); 
			while (pubmedRes.next()) {
				String snpId = pubmedRes.getString("snp_id");
				String pubmedId = pubmedRes.getString("pubmed_id");
				if (pubmedMap.get(snpId) == null) {
					pubmedMap.put(snpId, new HashSet<String>());
				}
				pubmedMap.get(snpId).add(pubmedId);
			}

			// get snp position
			String queryPosition = String.format(" SELECT snp_id, contig_chr, orientation, phys_pos_from, asn_from, asn_to, group_term" + 
					" FROM SNPContigLoc JOIN ContigInfo ON SNPContigLoc.ctg_id = ContigInfo.ctg_id " + 
					" WHERE snp_id > %d AND snp_id <= %d ", i, i + BATCH_SIZE);
			ResultSet positionRes = stmt.executeQuery(queryPosition);
			Map<String, Map<String, Set<LocationHolder>>> snpInfoMap = new HashMap<String, Map<String, Set<LocationHolder>>>();
			while (positionRes.next()) {
				String snpId = positionRes.getString("snp_id");
				if (snpInfoMap.get(snpId) == null) {
					snpInfoMap.put(snpId, new HashMap<String, Set<LocationHolder>>());
				}
				String groupTerm = positionRes.getString("group_term");
				if (snpInfoMap.get(snpId).get(groupTerm) == null) {
					snpInfoMap.get(snpId).put(groupTerm, new HashSet<LocationHolder>());
				}
				
				String chr = positionRes.getString("contig_chr");
				String orient = positionRes.getInt("orientation") == 0? "Fwd": "Rev";
				int pos = positionRes.getInt("phys_pos_from");
				int asnFrom = positionRes.getInt("asn_from");
				int asnTo = positionRes.getInt("asn_to");
				
				snpInfoMap.get(snpId).get(groupTerm).add(new LocationHolder(chr, orient, pos, asnFrom, asnTo));
			}
			
			Map<String, String> snpItemMap = new HashMap<String, String>();
			for (String snpId: snpInfoMap.keySet()) {
				Item item = createItem("SNP");
				item.setAttribute("identifier", "rs" + snpId);
				
				Set<String> chrSet = new HashSet<String>();
				if (snpInfoMap.get(snpId).get(PRIMARY_ASSEMBLY) != null) {
					for (LocationHolder lh: snpInfoMap.get(snpId).get(PRIMARY_ASSEMBLY)) {
						item.setAttribute("orientation", lh.orient);
						if (!StringUtils.isEmpty(lh.chr)) {
							String chromosome = getChromosome(lh.chr);
							chrSet.add(lh.chr);
							if (lh.pos > 0) {
								Item location = createItem("Location");
								
								if (lh.asnTo - lh.asnFrom > 0) {
									location.setAttribute("start", String.valueOf(lh.pos + 1));
									location.setAttribute("end", String.valueOf(lh.pos + lh.asnTo - lh.asnFrom + 1));
								} else {
									String posString = String.valueOf(lh.pos + 1);
									location.setAttribute("start", posString);
									location.setAttribute("end", posString);
								}
								location.setReference("locatedOn", chromosome);
								store(location);
								item.addToCollection("locations", location);
							}
						}
					}
				} else {
					LocationHolder lh = snpInfoMap.get(snpId).values().iterator().next().iterator().next();
					item.setAttribute("orientation", lh.orient);
					if (!StringUtils.isEmpty(lh.chr)) {
						String chromosome = getChromosome(lh.chr);
						chrSet.add(lh.chr);
						if (lh.pos > 0) {
							Item location = createItem("Location");
							
							if (lh.asnTo - lh.asnFrom > 0) {
								location.setAttribute("start", String.valueOf(lh.pos + 1));
								location.setAttribute("end", String.valueOf(lh.pos + lh.asnTo - lh.asnFrom + 1));
							} else {
								String posString = String.valueOf(lh.pos + 1);
								location.setAttribute("start", posString);
								location.setAttribute("end", posString);
							}
							location.setReference("locatedOn", chromosome);
							store(location);
							item.addToCollection("locations", location);
						}
					}
				}

				if (chrSet.size() > 0) {
					String chrString = StringUtils.join(chrSet, "/");
					item.setAttribute("chromosome", chrString);
				}
				
				String allele = alleleMap.get(snpId);
				if (allele != null) {
					item.setAttribute("refseqAllele", allele);
				}
				// add publications
				if (pubmedMap.get(snpId) != null) {
					for (String pubmedId : pubmedMap.get(snpId)) {
						item.addToCollection("publications", getPublication(pubmedId));
					}
				}
				
				store(item);
				snpItemMap.put(snpId, item.getIdentifier());
			}
			
			// get transcript
			String queryTranscript = String
					.format(" SELECT DISTINCT SCLI.snp_id, gene_id, fxn_class, SCLI.mrna_acc, SCLI.mrna_ver, "
							+ " SCLI.mrna_start, SCLI.mrna_stop, SCLI.mrna_orien, SCL.orientation, SCLI.allele, "
							+ " SCLI.codon, SCLI.protein_acc, SCLI.protein_ver, SCLI.aa_position, SCLI.residue "
							+ " FROM SNPContigLocusId AS SCLI JOIN SNPContigLoc AS SCL ON SCLI.snp_id = SCL.snp_id "
							+ " AND SCLI.ctg_id = SCL.ctg_id AND SCLI.asn_from = SCL.asn_from "
							+ " WHERE mrna_acc NOT LIKE 'X%%' AND SCLI.snp_id > %d AND SCLI.snp_id <= %d ", i, i + BATCH_SIZE);
			ResultSet transcriptRes = stmt.executeQuery(queryTranscript);
			Map<String, Map<String, Map<String, ReferenceHolder>>> referenceDataMap = new HashMap<String, Map<String, Map<String, ReferenceHolder>>>();
			Map<String, Integer> fxnMap = new HashMap<String, Integer>();
			Set<String> missingSnps = new HashSet<String>();  // TODO to be remove
			while (transcriptRes.next()) {
				String snpId = transcriptRes.getString("snp_id");
				String geneId = transcriptRes.getString("gene_id");
				int fxn = transcriptRes.getInt("fxn_class");
				String mrnaAcc = transcriptRes.getString("mrna_acc") + "." + transcriptRes.getString("mrna_ver");
				
				String refKey = snpId + "-" + geneId;
				if (referenceDataMap.get(refKey) == null) {
					referenceDataMap.put(refKey, new HashMap<String, Map<String, ReferenceHolder>>());
				}
				if (referenceDataMap.get(refKey).get(mrnaAcc) == null) {
					referenceDataMap.get(refKey).put(mrnaAcc, new HashMap<String, ReferenceHolder>());
				}
				
				int mrnaStart = transcriptRes.getInt("mrna_start");
				int mrnaStop = transcriptRes.getInt("mrna_stop");
				String mrnaPos = null;
				if (mrnaStart > 0) {
					if (mrnaStop > mrnaStart) {
						mrnaPos = String.format("%d..%d", mrnaStart + 1, mrnaStop + 1);
					}
					mrnaPos = String.valueOf(mrnaStart + 1);
				}
				
				String orientation = null;
				if (transcriptRes.getString("mrna_orien") != null) {
					orientation = transcriptRes.getInt("mrna_orien") == transcriptRes.getInt("orientation")? "Fwd": "Rev";
				}
				String allele = transcriptRes.getString("allele");
				String codon = transcriptRes.getString("codon");
				String proteinAcc = null;
				if (!StringUtils.isEmpty(transcriptRes.getString("protein_acc"))) {
					proteinAcc = transcriptRes.getString("protein_acc") + "."
							+ transcriptRes.getString("protein_ver");
				}
				int aaPos = transcriptRes.getInt("aa_position");
				String residue = transcriptRes.getString("residue");
				
				if (fxn == CDS_REFERENCE) {
					referenceDataMap.get(refKey).get(mrnaAcc).put(REF, new ReferenceHolder(
							mrnaPos, orientation, allele, codon, proteinAcc, aaPos, residue));
				} else {
					int size = referenceDataMap.get(refKey).get(mrnaAcc).size();
					referenceDataMap.get(refKey).get(mrnaAcc).put(String.format("%d_%d", fxn, size),
							new ReferenceHolder(mrnaPos, orientation, allele, codon, proteinAcc,
									aaPos, residue));
					
					fxnMap.put(refKey, Integer.valueOf(fxn));
				}
				 // TODO to be remove
				// collect missing snps 
				if (snpItemMap.get(snpId) == null) {
					missingSnps.add(snpId);
				}
			}
			
			if (missingSnps.size() != 0) {
				throw new RuntimeException("null SNP found. snpIds: " + StringUtils.join(missingSnps, ",")); // TODO to be remove
			}
			
			for (String snpGeneId : referenceDataMap.keySet()) {
				String[] split = snpGeneId.split("-");
				String snpId = split[0];
				String geneId = split[1];
				Item vaItem = createItem("VariationAnnotation");
				vaItem.setReference("gene", getGene(geneId));
				String refId = snpItemMap.get(snpId);
				if (refId == null) {
					throw new RuntimeException("null SNP, snpId: " + snpId); // TODO to be remove
				}
				vaItem.setReference("snp", refId);
				String funcRef = functionMap.get(fxnMap.get(snpGeneId));
				if (funcRef != null) {
					vaItem.setReference("function", funcRef);
				}
				store(vaItem);
				
				for (String mrnaAcc: referenceDataMap.get(snpGeneId).keySet()) {
					Map<String, ReferenceHolder> snpRefMap = referenceDataMap.get(snpGeneId).get(mrnaAcc);
					Set<String> keys = snpRefMap.keySet();
					for (String key : keys) {
						if (key.equals(REF)) {
							// some strange case, e.g. rs3800961
							if (keys.size() == 1) {
								ReferenceHolder rh = snpRefMap.get(key);
								createSNPReference(mrnaAcc, rh.mrnaPos, rh.orientation, rh.allele,
										rh.codon, rh.proteinAcc, rh.aaPos, rh.residue,
										functionMap.get(Integer.valueOf(CDS_REFERENCE)),
										vaItem.getIdentifier());
							}
						} else {
							if (keys.contains(REF)) {
								ReferenceHolder rh = snpRefMap.get(key);
								ReferenceHolder rhRef = snpRefMap.get(REF);
								createSNPReference(mrnaAcc, rh.mrnaPos, rh.orientation,
										String.format("%s -> %s", rhRef.allele, rh.allele),
										String.format("%s -> %s", rhRef.codon, rh.codon),
										rh.proteinAcc, rh.aaPos,
										String.format("%s -> %s", rhRef.residue, rh.residue),
										functionMap.get(Integer.valueOf(key.split("_")[0])),
										vaItem.getIdentifier());
							} else {
								ReferenceHolder rh = snpRefMap.get(key);
								createSNPReference(mrnaAcc, rh.mrnaPos, rh.orientation, rh.allele,
										rh.codon, rh.proteinAcc, rh.aaPos, rh.residue,
										functionMap.get(Integer.valueOf(key.split("_")[0])),
										vaItem.getIdentifier());
							}
						}
					}
				}
			}

		}

		stmt.close();
		connection.close();
	   }

	/**
     * {@inheritDoc}
     */
    @Override
    public String getDataSetTitle(int taxonId) {
        return DATASET_TITLE;
    }
    
	private Map<String, String> geneMap = new HashMap<String, String>();
	private Map<String, String> chromosomeMap = new HashMap<String, String>();
	private Map<String, String> publicationMap = new HashMap<String, String>();
	private String getGene(String primaryIdentifier) throws ObjectStoreException {
		String ret = geneMap.get(primaryIdentifier);
		if (ret == null) {
			Item item = createItem("Gene");
			item.setAttribute("primaryIdentifier", primaryIdentifier);
			store(item);
			ret = item.getIdentifier();
			geneMap.put(primaryIdentifier, ret);
		}
		return ret;
	}
	private String getChromosome(String chr) throws ObjectStoreException {
		String ret = chromosomeMap.get(chr);
		if (ret == null) {
			Item item = createItem("Chromosome");
			String chrId = chr;
			if (chr.toLowerCase().startsWith("chr")) {
				chrId = chr.substring(3);
			}
			item.setAttribute("symbol", chrId);
			item.setReference("organism", getOrganismItem(HUMAN_TAXON_ID));
			store(item);
			ret = item.getIdentifier();
			chromosomeMap.put(chr, ret);
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
	
	private String createSNPReference(String mrnaAcc, String mrnaPos, String orientation,
			String allele, String codon, String proteinAcc, int aaPos, String residue,
			String funcRef, String vaItemRef) throws ObjectStoreException {
		Item item = createItem("SNPReference");
		item.setAttribute("mrnaAccession", mrnaAcc);
		if (!StringUtils.isEmpty(mrnaPos)) {
			item.setAttribute("mrnaPosition", mrnaPos);
		}
		if (!StringUtils.isEmpty(orientation)) {
			item.setAttribute("orientation", orientation);
		}
		if (!StringUtils.isEmpty(allele)) {
			item.setAttribute("mrnaAllele", allele);
		}
		if (!StringUtils.isEmpty(codon)) {
			item.setAttribute("mrnaCodon", codon);
		}
		if (!StringUtils.isEmpty(proteinAcc)) {
			item.setAttribute("proteinAccession", proteinAcc);
		}
		if (aaPos > 0) {
			item.setAttribute("proteinPosition", String.valueOf(aaPos));
		}
		if (!StringUtils.isEmpty(residue)) {
			item.setAttribute("residue", residue);
		}
		item.setReference("function", funcRef);
		item.setReference("annotation", vaItemRef);
		store(item);

		return item.getIdentifier();
	}	

	static class LocationHolder {
		String chr;
		String orient;
		int pos;
		int asnFrom;
		int asnTo;
		public LocationHolder(String chr, String orient, int pos, int asnFrom, int asnTo) {
			this.chr = chr;
			this.orient = orient;
			this.pos = pos;
			this.asnFrom = asnFrom;
			this.asnTo = asnTo;
		}
	}
	
	static class ReferenceHolder {
		String mrnaPos;
		String orientation;
		String allele;
		String codon;
		String proteinAcc;
		int aaPos;
		String residue;
		
		public ReferenceHolder(String mrnaPos, String orientation, String allele, String codon,
				String proteinAcc, int aaPos, String residue) {
			this.mrnaPos = mrnaPos;
			this.orientation = orientation;
			this.allele = allele;
			this.codon = codon;
			this.proteinAcc = proteinAcc;
			this.aaPos = aaPos;
			this.residue = residue;
		}
		
	}
}
