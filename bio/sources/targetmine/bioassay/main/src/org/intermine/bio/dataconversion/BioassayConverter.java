package org.intermine.bio.dataconversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
public class BioassayConverter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(BioassayConverter.class);
	//
	private static final String DATASET_TITLE = "BioAssay";
	private static final String DATA_SOURCE_NAME = "PubChem";

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public BioassayConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		readGiMapping();
		readPubmedMapping();
		readCidMapping();
		readNameMap();
		readInchikeyMap();
		
		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		while (iterator.hasNext()) {
			// aid, name, source, target(gi), target name, symbol 
			String[] cols = iterator.next();
			String aid = cols[0];
			String name = cols[1];
			String source = cols[2];
			String gi = cols[3];
			// So far, we ignore those assay with multiple targets (difficult to interpret) 
			if (gi.equals("MultipleTargets")) {
				LOG.info(String.format("Skip AID:%s because multiple targets.", aid));
				continue;
			}
			if (giMapping.get(gi) == null) {
				LOG.info(String.format("Skip AID:%s because unidentified gi number.", aid));
				continue;
			}
		
			Item assay = createItem("CompoundProteinInteractionAssay");
			assay.setAttribute("identifier", "pcassay" + aid);
			assay.setAttribute("originalId", aid);
			assay.setAttribute("name", name);
			assay.setAttribute("source", source);
			if (pubmedMapping.get(aid)!=null) {
				for (String pubMedId: pubmedMapping.get(aid)) {
					assay.addToCollection("publications", getPublication(pubMedId));
				}
			}
			store(assay);

			for (String acc: giMapping.get(gi)) {
				for (String cid: cidMapping.get(aid)) {
					Item activity = createItem("Activity");
					activity.setReference("assay", assay);
					activity.setReference("interaction", getInteraction(acc, cid));
					store(activity);
				}
			}

		}
		
	}
	
	private Map<String, String> interactionMap = new HashMap<String, String>();
	private String getInteraction(String acc, String cid) throws ObjectStoreException {
		String intId = acc + "-" + cid;
		String ret = interactionMap.get(intId);
		if (ret == null) {
			Item interaction = createItem("BioAssayInteraction");
			interaction.setReference("protein", getProtein(acc));
			interaction.setReference("compound", getPubChemCompound(cid));
			store(interaction);
			ret = interaction.getIdentifier();
		}
		return ret;
	}
	
	private Map<String, String> publicationMap = new HashMap<String, String>();
	
	private String getPublication(String pubMedId) throws ObjectStoreException {
		String ret = publicationMap.get(pubMedId);
		if (ret == null) {
			Item item = createItem("Publication");
			item.setAttribute("pubMedId", pubMedId);
			store(item);
			ret = item.getIdentifier();
			publicationMap.put(pubMedId, ret);
		}
		return ret;
	}
	
	private Map<String, String> proteinMap = new HashMap<String, String>();
	
	private String getProtein(String uniprotAcc) throws ObjectStoreException {
		String ret = proteinMap.get(uniprotAcc);
		if (ret == null) {
			Item item = createItem("Protein");
			item.setAttribute("primaryAccession", uniprotAcc);
			store(item);
			ret = item.getIdentifier();
			proteinMap.put(uniprotAcc, ret);
		}
		return ret;
	}

	private Map<String, String> pubChemCompoundMap = new HashMap<String, String>();

	private String getPubChemCompound(String cid) throws ObjectStoreException {
		String ret = pubChemCompoundMap.get(cid);
		if (ret == null) {
			Item item = createItem("PubChemCompound");
			item.setAttribute("originalId", cid);
			item.setAttribute("identifier", String.format("PubChem:%s", cid));
			String name = nameMap.get(cid);
			if (name == null) {
				name = String.format("CID %s", cid);
			}
			// if the length of the name is greater than 40 characters,
			// use id instead and save the long name as the synonym
			if (name.length() > 40) {
				setSynonyms(item, name);
				name = String.format("CID %s", cid);
			}
			item.setAttribute("name", name);

			String inchiKey = inchikeyMap.get(cid);
			if (inchiKey != null) {
				item.setAttribute("inchiKey", inchiKey);
				// chenyian: no longer needed
//				setSynonyms(item, inchiKey);
				item.setReference("compoundGroup",
						getCompoundGroup(inchiKey.substring(0, inchiKey.indexOf("-")), name));
			}

			store(item);
			ret = item.getIdentifier();
			pubChemCompoundMap.put(cid, ret);
		}
		return ret;
	}


	private File giUniprotaccFile;
	private File aidPubmedidFile;
	private File aidActivecidFile;
	private File nameFile;
	private File inchikeyFile;

	public void setGiUniprotaccFile(File giUniprotaccFile) {
		this.giUniprotaccFile = giUniprotaccFile;
	}

	public void setAidPubmedidFile(File aidPubmedidFile) {
		this.aidPubmedidFile = aidPubmedidFile;
	}

	public void setAidActivecidFile(File aidActivecidFile) {
		this.aidActivecidFile = aidActivecidFile;
	}

	public void setNameFile(File nameFile) {
		this.nameFile = nameFile;
	}

	public void setInchikeyFile(File inchikeyFile) {
		this.inchikeyFile = inchikeyFile;
	}

	private Map<String, Set<String>> giMapping = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> pubmedMapping = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> cidMapping = new HashMap<String, Set<String>>();
	private Map<String, String> nameMap = new HashMap<String, String>();
	private Map<String, String> inchikeyMap = new HashMap<String, String>();

	private void readGiMapping() {
		try {
			Iterator<String[]> iterator = FormattedTextParser
					.parseTabDelimitedReader(new FileReader(giUniprotaccFile));
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				if (!cols[1].startsWith("?")) {
					giMapping.put(cols[0], new HashSet<String>(Arrays.asList(cols[1].split(","))));
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void readInchikeyMap() {
		try {
			Iterator<String[]> iterator = FormattedTextParser
					.parseTabDelimitedReader(new FileReader(inchikeyFile));
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				inchikeyMap.put(cols[0], cols[1]);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void readNameMap() {
		try {
			Iterator<String[]> iterator = FormattedTextParser
					.parseTabDelimitedReader(new FileReader(nameFile));
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				nameMap.put(cols[0], cols[1]);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void readCidMapping() {
		try {
			Iterator<String[]> iterator = FormattedTextParser
					.parseTabDelimitedReader(new FileReader(aidActivecidFile));
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				cidMapping.put(cols[0], new HashSet<String>(Arrays.asList(cols[1].split(","))));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void readPubmedMapping() {
		try {
			Iterator<String[]> iterator = FormattedTextParser
					.parseTabDelimitedReader(new FileReader(aidPubmedidFile));
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				if (StringUtils.isNotEmpty(cols[1])) {
					pubmedMapping.put(cols[0],
							new HashSet<String>(Arrays.asList(cols[1].split(","))));
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setSynonyms(Item subject, String value) throws ObjectStoreException {
		Item syn = createItem("CompoundSynonym");
		syn.setAttribute("value", value);
		syn.setReference("subject", subject);
		store(syn);
	}

	private Map<String, Item> compoundGroupMap = new HashMap<String, Item>();
	private Map<String, String> cgNameMap = new HashMap<String, String>();

	private Item getCompoundGroup(String inchiKey, String name) throws ObjectStoreException {
		Item ret = compoundGroupMap.get(inchiKey);
		if (ret == null) {
			ret = createItem("CompoundGroup");
			ret.setAttribute("identifier", inchiKey);
			compoundGroupMap.put(inchiKey, ret);
		}
		// randomly pick one name
		if (cgNameMap.get(inchiKey) == null || !name.startsWith("CID")) {
			cgNameMap.put(inchiKey, name);
			ret.setAttribute("name", name);
		}
		return ret;
	}

	@Override
	public void close() throws Exception {
		store(compoundGroupMap.values());
	}

}
