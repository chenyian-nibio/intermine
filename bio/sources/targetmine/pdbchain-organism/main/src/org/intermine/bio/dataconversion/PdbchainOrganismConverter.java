package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.FileConverter;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class PdbchainOrganismConverter extends FileConverter {

	protected static final Logger LOG = Logger.getLogger(PdbchainOrganismConverter.class);
	//
	private Map<String, String> organismMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public PdbchainOrganismConverter(ItemWriter writer, Model model) {
		super(writer, model);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(reader));

		Set<String> pdbIdChainTax = new HashSet<String>();

		// collecting info first
		HashMap<String, Set<String>> chainOrganismMap = new HashMap<String, Set<String>>();
		HashMap<String, String> chainMolTypeMap = new HashMap<String, String>();
		// skip header
		iterator.next();
		while (iterator.hasNext()) {
			// there are more than one tab between columns, for example,
			// "101m\tA\t9755\t\tPROTEIN\t\t\tPhyseter catodon"
			String[] cols = iterator.next();
			String chain = cols[0] + "-" + cols[1];
			String taxId = cols[2];
			String molType = cols[4];

			if (pdbIdChainTax.contains(chain + taxId)) {
				LOG.error("Duplicated pdbId-chain-taxId found: '" + chain + "; " + taxId
						+ "', this line will be skipped!");
				continue;
			}

			if (chainOrganismMap.get(chain) == null) {
				chainOrganismMap.put(chain, new HashSet<String>());
			}
			chainOrganismMap.get(chain).add(taxId);

			chainMolTypeMap.put(chain, molType);

			pdbIdChainTax.add(chain + taxId);
		}

		// create ProteinChain objects
		for (String pdbIdChain : chainOrganismMap.keySet()) {

			String[] values = pdbIdChain.split("-");

			Item proteinChain = createItem("ProteinChain");
			proteinChain.setAttribute("pdbId", values[0]);
			proteinChain.setAttribute("chain", values[1]);
			proteinChain.setAttribute("identifier", values[0] + values[1]);
			proteinChain.setAttribute("moleculeType", chainMolTypeMap.get(pdbIdChain));

			for (String taxId : chainOrganismMap.get(pdbIdChain)) {
				if (taxId != null && taxId.length() > 0 && StringUtils.isNumeric(taxId)) {
					proteinChain.addToCollection("organism", getOrganism(taxId));
				}
			}

			store(proteinChain);

		}

	}

	private String getOrganism(String taxonId) throws ObjectStoreException {
		String refId = organismMap.get(taxonId);
		if (refId == null) {
			Item item = createItem("Organism");
			item.setAttribute("taxonId", taxonId);
			refId = item.getIdentifier();
			organismMap.put(taxonId, refId);
			store(item);
		}
		return refId;
	}

}
