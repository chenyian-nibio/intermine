package org.intermine.bio.dataconversion;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;
import org.intermine.xml.full.ReferenceList;


/**
 * DataConverter to load Kegg Pathways and link them to Genes
 *
 * @author Xavier Watkins
 */
public class KeggPathwayConverter extends BioFileConverter
{
    protected static final Logger LOG = Logger.getLogger(KeggPathwayConverter.class);
    private static final String PROP_FILE = "kegg_config.properties";
    private Map<String, Item> geneItems = new HashMap<String, Item>();
    protected IdResolverFactory resolverFactory;
    private Map<String, String[]> config = new HashMap<String, String[]>();
    private Set<String> taxonIds = new HashSet<String>();

    protected Map<String, String> pathwayIdentifiers = new HashMap<String, String>();
    protected Map<String, Item> pathwaysNotStored = new HashMap<String, Item>();

    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public KeggPathwayConverter(ItemWriter writer, Model model) {
        super(writer, model, "GenomeNet", "KEGG pathways data set");
        readConfig();
        // only construct factory here so can be replaced by mock factory in tests
        resolverFactory = new FlyBaseIdResolverFactory("gene");

        // chenyian:
		readClass = false;
    }

    // chenyian: start
	private Map<String, String> mainClass = new HashMap<String, String>();
	private Map<String, String> subClass = new HashMap<String, String>();

	private File pathwayClassFile;
	private boolean readClass;

	public void setPathwayClassFile(File pathwayClassFile) {
		this.pathwayClassFile = pathwayClassFile;
	}

	@SuppressWarnings("unchecked")
	private void readPathwayClassification() {
		if (pathwayClassFile == null) {
			throw new NullPointerException("pathwayClassFile property not set");
		}
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(pathwayClassFile));

			Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);

			while (iterator.hasNext()) {
				String[] strings = iterator.next();
				mainClass.put(strings[1], strings[2]);
				subClass.put(strings[0], strings[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		readClass = true;
	}
	// chenyian: end
	
    /**
     * Sets the list of taxonIds that should be imported
     *
     * @param taxonIds a space-separated list of taxonIds
     */
    public void setKeggOrganisms(String taxonIds) {
        this.taxonIds = new HashSet<String>(Arrays.asList(StringUtils.split(taxonIds, " ")));
        LOG.info("Setting list of organisms to " + this.taxonIds);
    }

    private void readConfig() {
        Properties props = new Properties();
        try {
            props.load(getClass().getClassLoader().getResourceAsStream(PROP_FILE));
        } catch (IOException e) {
            throw new RuntimeException("Problem loading properties '" + PROP_FILE + "'", e);
        }

        for (Map.Entry<Object, Object> entry: props.entrySet()) {

            String key = (String) entry.getKey();
            String value = ((String) entry.getValue()).trim();

            String[] attributes = key.split("\\.");
            if (attributes.length == 0) {
                throw new RuntimeException("Problem loading properties '" + PROP_FILE + "' on line "
                                           + key);
            }
            String organism = attributes[0];

            if (config.get(organism) == null) {
                String[] configs = new String[2];
                configs[1] = "primaryIdentifier";
                config.put(organism, configs);
            }
            if ("taxonId".equals(attributes[1])) {
                config.get(organism)[0] = value;
            } else if ("identifier".equals(attributes[1])) {
                config.get(organism)[1] = value;
            } else {
                String msg = "Problem processing properties '" + PROP_FILE + "' on line " + key
                    + ".  This line has not been processed.";
                LOG.error(msg);
            }
        }
    }

    /**
     * Read each line from flat file, create genes and synonyms.
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
		if (readClass == false) {
			readPathwayClassification();
		}

        Iterator<?> lineIter = FormattedTextParser.parseTabDelimitedReader(reader);
        File currentFile = getCurrentFile();
        while (lineIter.hasNext()) {
            String[] line = (String[]) lineIter.next();
            Pattern filePattern = Pattern.compile("^(\\S+)_gene_map.*");
            Matcher matcher = filePattern.matcher(currentFile.getName());
            if (line.length <= 1 || line[0].startsWith("#")) {
                continue;
            }
            if (currentFile.getName().startsWith("map_title")) {
                processPathway(line);
            } else if (matcher.find()) {
                String organism = matcher.group(1);
                String taxonId = config.get(organism)[0];
                // only process organisms set in project.xml
                if (!taxonIds.isEmpty() && !taxonIds.contains(taxonId)) {
                    continue;
                }
                if (taxonId != null && taxonId.length() != 0) {
                    String geneName = line[0];

                    // There are some strange ids for D. melanogaster, the rest start with Dmel_,
                    // ignore any D. melanogaster ids without Dmel_ and strip this off the rest
                    if ("7227".equals(taxonId) && !geneName.startsWith("Dmel_")) {
                        continue;
                    }

                    // We don't want Dmel_ prefix on D. melanogaster genes
                    if (geneName.startsWith("Dmel_")) {
                        geneName = geneName.substring(5);
                    }

                    String mapIdentifiers = line[1];
                    ReferenceList referenceList = new ReferenceList("pathways");
                    String [] mapArray = mapIdentifiers.split(" ");
                    for (int i = 0; i < mapArray.length; i++) {
                        String identifier = mapArray[i];
                        String refId = pathwayIdentifiers.get(identifier);
                        if (refId == null) {
                            Item item = getPathway(identifier);
                            refId = item.getIdentifier();
                            pathwaysNotStored.put(identifier, item);
                        }
                        referenceList.addRefId(refId);
                    }
                    getGene(geneName, organism, referenceList);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws ObjectStoreException {
        for (Item pathway : pathwaysNotStored.values()) {
            store(pathway);
        }
        pathwaysNotStored.clear();
    }

    private Map<String, String> geneCollectionCheck = new HashMap<String, String>();

    private Item getGene(String geneCG, String organism, ReferenceList referenceList)
        throws ObjectStoreException {
        String identifier = null;
        IdResolver resolver = resolverFactory.getIdResolver(false);
        String taxonId = config.get(organism)[0];
        if ("7227".equals(taxonId) && resolver != null) {
            int resCount = resolver.countResolutions(taxonId, geneCG);
            if (resCount != 1) {
                LOG.info("RESOLVER: failed to resolve gene to one identifier, ignoring gene: "
                         + geneCG + " count: " + resCount + " FBgn: "
                         + resolver.resolveId(taxonId, geneCG));
                return null;
            }
            identifier = resolver.resolveId(taxonId, geneCG).iterator().next();
        } else {
            identifier = geneCG;
        }

        Item gene = geneItems.get(identifier);
        if (gene == null) {
            gene = createItem("Gene");
            gene.setAttribute(config.get(organism)[1], identifier);
            gene.setReference("organism", getOrganism(taxonId));
            gene.addCollection(referenceList);
            geneItems.put(identifier, gene);
            store(gene);
            geneCollectionCheck.put(identifier, referenceList.getRefIds().toString());
        } else {
            if (!referenceList.getRefIds().toString().equals(geneCollectionCheck.get(identifier))) {
                throw new IllegalArgumentException("Not storing Gene for a second time: " + geneCG
                        + ", " + identifier + ", but collections differ: "
                        + referenceList.getRefIds() + " versus "
                        + geneCollectionCheck.get(identifier));
            }
        }
        return gene;
    }
    private void processPathway(String[] line) throws ObjectStoreException {
        String identifier = line[0];
        String name = line[1];
        Item pathway = pathwaysNotStored.remove(identifier);
        if (pathway == null) {
            pathway = getPathway(identifier);
        }
        pathway.setAttribute("name", name);
		// chenyian: add classification information
		String subclass = subClass.get(identifier);
		if (subclass == null) {
			LOG.error("No subclass found for " + identifier);
		} else {
			pathway.setAttribute("subClass", subclass);
			pathway.setAttribute("mainClass", mainClass.get(subclass));
		}
        store(pathway);
    }

    private Item getPathway(String identifier) {
        Item item = createItem("Pathway");
        item.setAttribute("identifier", identifier);
        pathwayIdentifiers.put(identifier, item.getIdentifier());
        return item;
    }
}
