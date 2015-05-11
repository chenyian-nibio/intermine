package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
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
import org.intermine.metadata.StringUtil;
import org.intermine.model.bio.Protein;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.xml.full.Item;

/**
 * This converter parse enzyme info from 'ENZYME nomenclature database' <br/>
 * The file could be get at ftp of expasy (http://www.expasy.org/enzyme/)
 * 
 * <pre>
 * wget ftp://ftp.expasy.org/databases/enzyme/enzyme.dat
 * </pre>
 * 
 * The parser will try to associate all proteins found in the built database with the enzymes 
 * regardless of species. <br/> 
 * Better to use after all proteins are incorporated. 
 * 
 * @author chenyian
 */
public class EnzymeConverter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(EnzymeConverter.class);

	//
	private static final String DATASET_TITLE = "ENZYME";
	private static final String DATA_SOURCE_NAME = "ExPASy";

	private Set<String> organismNames;

	public void setEnzymeOrganisms(String organismNames) {
		this.organismNames = new HashSet<String>(
				Arrays.asList(StringUtil.split(organismNames, " ")));
		LOG.info("Setting list of organisms to " + this.organismNames);
	}

	private Map<String, String> proteinMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public EnzymeConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		queryAllProteinAccs();

		BufferedReader br = new BufferedReader(reader);
		String l;
		boolean isEntry = false;
		EnzymeEntry ee = new EnzymeEntry();
		while ((l = br.readLine()) != null) {
			if (l.trim().equals("//")) {
				// skip header part
				if (!isEntry) {
					isEntry = true;
					continue;
				}
				createNewEnzyme(ee);
				ee = new EnzymeEntry();
			}

			if (l.startsWith("DR")) {
				Set<String> accs = processPrimaryAcc(l.substring(5));
				if (!accs.isEmpty()) {
					ee.proteins.addAll(accs);
				}
			} else if (l.startsWith("ID")) {
				ee.ecNumber = l.substring(5);
			} else if (l.startsWith("DE")) {
				ee.description += l.substring(5);
			} else if (l.startsWith("CF")) {
				ee.cofactor += l.substring(5);
			} else if (l.startsWith("CA")) {
				ee.catalyticActivity += l.substring(5);
			} else if (l.startsWith("AN")) {
				ee.altName += l.substring(5);
			}
		}
		br.close();
	}

	private Set<String> processPrimaryAcc(String proteinString) {
		Set<String> ret = new HashSet<String>();
		String[] proteinEntries = proteinString.split("  ");
		for (String entry : proteinEntries) {
			if (entry.indexOf(",") > 0) {
				String acc = entry.substring(0, entry.indexOf(","));
				if (proteinAccs.contains(acc)) {
					ret.add(acc);
				}
			}
		}
		return ret;
	}

	private void createNewEnzyme(EnzymeEntry enzymeEntry) throws ObjectStoreException {
		Item enzyme = createItem("Enzyme");
		if (enzymeEntry.ecNumber.equals("")) {
			LOG.error("failed to parse the enzyme entry." + enzymeEntry.toString());
			return;
		}
		// for BioEntity
		enzyme.setAttribute("primaryIdentifier", enzymeEntry.ecNumber);
		enzyme.setAttribute("ecNumber", enzymeEntry.ecNumber);
		String desc = enzymeEntry.description;
		if (!desc.equals("")) {
			enzyme.setAttribute("description", desc.replaceAll("\\.$", ""));
		}
		String catalyticActivity = enzymeEntry.catalyticActivity;
		if (!catalyticActivity.equals("")) {
			enzyme.setAttribute("catalyticActivity", catalyticActivity.replaceAll("\\.$", ""));
		}
		String cofactor = enzymeEntry.cofactor;
		if (!cofactor.equals("")) {
			enzyme.setAttribute("cofactor", cofactor.replaceAll("\\.$", ""));
		}

		for (String primaryAccession : enzymeEntry.proteins) {
			String refId = getProtein(primaryAccession);
			if (refId != null) {
				enzyme.addToCollection("proteins", refId);
			}
		}

		for (String aliasName : enzymeEntry.getSynonyms()) {
			Item item = createItem("Synonym");
			item.setReference("subject", enzyme.getIdentifier());
			item.setAttribute("value", aliasName);
			store(item);
			enzyme.addToCollection("synonyms", item);
		}

		store(enzyme);
	}

	private String getProtein(String primaryAccession) throws ObjectStoreException {
		String ret = proteinMap.get(primaryAccession);
		if (ret == null) {
			Item protein = createItem("Protein");
			protein.setAttribute("primaryAccession", primaryAccession);
			ret = protein.getIdentifier();
			proteinMap.put(primaryAccession, ret);
			store(protein);
		}
		return ret;
	}

	private class EnzymeEntry {
		protected String ecNumber = "";
		protected String description = "";
		protected String catalyticActivity = "";
		protected String cofactor = "";

		protected String altName = "";
		protected Set<String> proteins = new HashSet<String>();

		protected Set<String> getSynonyms() {
			Set<String> synonyms = new HashSet<String>();
			for (String an : altName.split("\\.")) {
				if (!StringUtils.isEmpty(an.trim())) {
					synonyms.add(an.trim());
				}
			}
			return synonyms;
		}
	}

	private Set<String> proteinAccs = new HashSet<String>();

	private String osAlias = null;

	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

	@SuppressWarnings("unchecked")
	private void queryAllProteinAccs() throws Exception {

		Query q = new Query();
		QueryClass qcProtein = new QueryClass(Protein.class);

		QueryField qfPrimaryAcc = new QueryField(qcProtein, "primaryAccession");

		q.addFrom(qcProtein);
		q.addToSelect(qfPrimaryAcc);

		ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);

		Results results = os.execute(q);
		Iterator<Object> iterator = results.iterator();
		while (iterator.hasNext()) {
			ResultsRow<String> rr = (ResultsRow<String>) iterator.next();
			proteinAccs.add(rr.get(0));
		}
	}

}
