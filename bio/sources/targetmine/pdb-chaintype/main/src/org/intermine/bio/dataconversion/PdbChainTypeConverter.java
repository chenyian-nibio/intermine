package org.intermine.bio.dataconversion;

import java.io.File;
import java.io.FileReader;
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
public class PdbChainTypeConverter extends BioFileConverter {
	//
	private static final String DATASET_TITLE = "PDBj";
	private static final String DATA_SOURCE_NAME = "Protein Data Bank Japan";

	private Map<String, String> proteinStructureMap = new HashMap<String, String>();
	private Map<String, String> proteinChainMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public PdbChainTypeConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	private File mapFile;

	public void setMapFile(File mapFile) {
		this.mapFile = mapFile;
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		Map<String, String> typeMap = new HashMap<String, String>();
		if (null != mapFile) {
			Iterator<String[]> mapItr = FormattedTextParser
					.parseCsvDelimitedReader(new FileReader(mapFile));
			while (mapItr.hasNext()){
				String[] map = mapItr.next();
				typeMap.put(map[0].trim(), map[1].trim());
			}
		} else {
			System.out.println("The type mapping file is not set, the original string will be used.");
		}

		Iterator<String[]> iterator = FormattedTextParser.parseCsvDelimitedReader(reader);
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String pdbId = cols[0].toLowerCase();
			String moleculeType = cols[2];
			String[] chians = cols[1].split(",");
			for (String cid : chians) {
				if (typeMap.get(moleculeType) != null) {
					moleculeType = typeMap.get(moleculeType);
				}
				getProteinChain(pdbId, cid, moleculeType);
			}
		}
	}

	private String getProteinChain(String pdbId, String chainId, String moleculeType)
			throws ObjectStoreException {
		String identifier = pdbId + chainId;
		String ret = proteinChainMap.get(identifier);
		if (ret == null) {
			Item item = createItem("ProteinChain");
			item.setAttribute("pdbId", pdbId);
			item.setAttribute("chain", chainId);
			item.setAttribute("identifier", identifier);
			item.setReference("structure", getProteinStructure(pdbId));

			item.setAttribute("moleculeType", moleculeType);

			store(item);
			ret = item.getIdentifier();
			proteinChainMap.put(identifier, ret);
		}
		return ret;
	}

	private String getProteinStructure(String pdbId) throws ObjectStoreException {
		String ret = proteinStructureMap.get(pdbId);
		if (ret == null) {
			Item item = createItem("ProteinStructure");
			item.setAttribute("pdbId", pdbId);
			store(item);
			ret = item.getIdentifier();
			proteinStructureMap.put(pdbId, ret);
		}
		return ret;
	}
}
