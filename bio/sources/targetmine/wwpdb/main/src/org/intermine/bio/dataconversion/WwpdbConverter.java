package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class WwpdbConverter extends BioFileConverter {
	protected static final Logger LOG = Logger.getLogger(WwpdbConverter.class);
	//
	private static final String DATASET_TITLE = "wwPDB";
	private static final String DATA_SOURCE_NAME = "World Wide Protein Data Bank";

	// column index
	private static final int PDB_ID = 0;
	private static final int HEADER = 1;
	private static final int COMPOUND = 3;
	private static final int SOURCE = 4;
	private static final int RESOLUTION = 6;
	private static final int EXPERIMENT_TYPE = 7;

	// For prevent duplicated data.
	private Set<String> pdbIds = new HashSet<String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public WwpdbConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		if (structureFactors.isEmpty()) {
			readStructureFactorsFile();
		}
		if (refinementDetailsMap.isEmpty()) {
			readRefinementDetailsFile();
		}

		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);

		// skip first 2 lines (header)
		iterator.next();
		iterator.next();

		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String pdbid = cols[PDB_ID].toLowerCase();
			if (pdbIds.contains(pdbid)) {
				LOG.error("Duplicated pdbId found: '" + pdbid + "', this line will be skipped!");
				continue;
			}
			Item proteinStructure = createItem("ProteinStructure");
			proteinStructure.setAttribute("pdbId", pdbid);
			
			String resolution = "";
			if (!cols[EXPERIMENT_TYPE].contains(", ") && cols[RESOLUTION].contains(", ")) {
				resolution = cols[RESOLUTION].split(", ")[0];
			} else {
				for (String resValue: cols[RESOLUTION].split(", ")) {
					if (resValue.equals("NOT")) {
						continue;
					}
					resolution = mergeRefine(resolution, resValue);
				}
			}
			
			if (!StringUtils.isEmpty(resolution)) {
				proteinStructure.setAttribute("resolution", resolution);
			}
			proteinStructure.setAttribute("experimentType", cols[EXPERIMENT_TYPE]);

			proteinStructure.setAttribute("name", cols[COMPOUND]);
			if (cols[HEADER].length() != 0) {
				proteinStructure.setAttribute("classification", cols[HEADER]);
			}
			if (cols[SOURCE].length() != 0) {
				proteinStructure.setAttribute("source", cols[SOURCE]);
			}

			// contains structure factors
			Boolean containsStructureFactors = Boolean.FALSE;
			if (structureFactors.contains(pdbid)) {
				containsStructureFactors = Boolean.TRUE;
			}
			proteinStructure.setAttribute("containsStructureFactors", containsStructureFactors.toString());
			
			// Refinement details
			String refinementDetails = refinementDetailsMap.get(pdbid);
			if (refinementDetails != null) {
				String rObserved = "";
				String rAll = "";
				String rWork = "";
				String rFree = "";
				String averageBFactor = "";
				if (refinementDetails.contains("##")) {
					for (String line: refinementDetails.split("##")) {
						String[] details = (line + ",dummy").split(",");
						// # pdbid,entry_id,ls_R_factor_obs,ls_R_factor_all,ls_R_factor_R_work,ls_R_factor_R_free,B_iso_mean,ls_d_res_high
						rObserved = mergeRefine(rObserved, details[2]);
						rAll = mergeRefine(rAll, details[3]);
						rWork = mergeRefine(rWork, details[4]);
						rFree = mergeRefine(rFree, details[5]);
						averageBFactor = mergeRefine(averageBFactor, details[6]);
					}
				} else {
					// the ",dummy" is to prevent removing of trailing empty strings;
					// e.g. "1e07,1E07,,,,,,"
					String[] details = (refinementDetails + ",dummy").split(",");
					// # pdbid,entry_id,ls_R_factor_obs,ls_R_factor_all,ls_R_factor_R_work,ls_R_factor_R_free,B_iso_mean,ls_d_res_high
					rObserved = details[2];
					rAll = details[3];
					rWork = details[4];
					rFree = details[5];
					averageBFactor = details[6];
				}

				if (!StringUtils.isEmpty(rObserved)) {
					proteinStructure.setAttribute("rObserved", rObserved);
				}
				if (!StringUtils.isEmpty(rAll)) {
					proteinStructure.setAttribute("rAll", rAll);
				}
				if (!StringUtils.isEmpty(rWork)) {
					proteinStructure.setAttribute("rWork", rWork);
				}
				if (!StringUtils.isEmpty(rFree)) {
					proteinStructure.setAttribute("rFree", rFree);
				}
				if (!StringUtils.isEmpty(averageBFactor)) {
					proteinStructure.setAttribute("averageBFactor", averageBFactor);
				}
			}

			store(proteinStructure);
			pdbIds.add(pdbid);
		}

	}
	
	private String mergeRefine(String original, String newAdd) {
		if (!StringUtils.isEmpty(newAdd)) {
			if (StringUtils.isEmpty(original)) {
				original = newAdd;
			} else {
				original = original + ", " + newAdd;
			}
		}
		return original;
	}
	
	private Set<String> structureFactors = new HashSet<String>();
	private Map<String, String> refinementDetailsMap = new HashMap<String, String>();

	private File structureFactorsFile;
	private File refinementDetailsFile;

	public void setStructureFactorsFile(File structureFactorsFile) {
		this.structureFactorsFile = structureFactorsFile;
	}
	public void setRefinementDetailsFile(File refinementDetailsFile) {
		this.refinementDetailsFile = refinementDetailsFile;
	}

	private void readStructureFactorsFile() throws Exception {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(structureFactorsFile));
			String line;
			while ((line = in.readLine()) != null) {
				structureFactors.add(line.trim());
			}
		} catch (FileNotFoundException e) {
			LOG.error(e);
		} catch (IOException e) {
			LOG.error(e);
		} finally {
			if (in != null)
				in.close();
		}
	}
	private void readRefinementDetailsFile() throws Exception {
		BufferedReader in = null;
		try {
			// # pdbid,entry_id,ls_R_factor_obs,ls_R_factor_all,ls_R_factor_R_work,ls_R_factor_R_free,B_iso_mean,ls_d_res_high
			in = new BufferedReader(new FileReader(refinementDetailsFile));
			String line;
			while ((line = in.readLine()) != null) {
				if (line.startsWith("#")) {
					continue;
				}
				String[] cols = line.split(",");
				if (cols.length < 3) {
					// e.g. "5jpr,5JPR,,,,,,"
					continue;
				}
				if (refinementDetailsMap.get(cols[0]) != null) {
					refinementDetailsMap.put(cols[0], refinementDetailsMap.get(cols[0]) + "##" + line);
				} else {
					refinementDetailsMap.put(cols[0], line);
				}
			}
		} catch (FileNotFoundException e) {
			LOG.error(e);
		} catch (IOException e) {
			LOG.error(e);
		} finally {
			if (in != null)
				in.close();
		}
	}

}
