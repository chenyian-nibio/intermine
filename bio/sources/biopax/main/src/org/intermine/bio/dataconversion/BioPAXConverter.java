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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.Traverser;
import org.biopax.paxtools.controller.Visitor;
import org.biopax.paxtools.io.jena.JenaIOHandler;
import org.biopax.paxtools.io.simpleIO.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.pathwayStep;
import org.biopax.paxtools.model.level2.process;
import org.intermine.bio.util.OrganismData;
import org.intermine.bio.util.OrganismRepository;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;
/**
 * Converts BioPAX files into InterMine objects.
 *
 * @author Julie Sullivan
 * 
 * @author chenyian
 * Note: This parser may be only suitable for Reactome data set, not a general biopax parser
 * 2011/05/13 
 * Some unexpect pathway-protein associations are integrated to database, there should be a bug!
 * 
 */
public class BioPAXConverter extends BioFileConverter implements Visitor
{
    private static final Logger LOG = Logger.getLogger(BioPAXConverter.class);
    private static final String DEFAULT_DB_NAME = "UniProt";
    private String dbName = DEFAULT_DB_NAME;
    private String identifierField = "primaryAccession";
    private String bioentityType = "Protein";
    protected IdResolverFactory resolverFactory;
    private Map<String, Item> bioentities = new HashMap<String, Item>();
    private Traverser traverser;
    private Set<BioPAXElement> visited = new HashSet<BioPAXElement>();
    private int depth = 0;
    private Item organism, dataset;
    private String pathwayRefId = null;
    private Set<String> taxonIds = new HashSet<String>();
    private OrganismRepository or;
    private String dataSourceRefId = null, dataSourceName = null;
    private String curated = "false";
    private Map<String, Config> configs = new HashMap<String, Config>();
    private static final String PROP_FILE = "biopax_config.properties";


    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param intermineModel the Model
     * @throws ObjectStoreException if something goes horribly wrong
     */
    public BioPAXConverter(ItemWriter writer, org.intermine.metadata.Model intermineModel)
        throws ObjectStoreException {
        super(writer, intermineModel);
        // only construct factory here so can be replaced by mock factory in tests
        resolverFactory = new FlyBaseIdResolverFactory("gene");
        traverser = new Traverser(new SimpleEditorMap(BioPAXLevel.L2), this);
        readConfig();
        or = OrganismRepository.getOrganismRepository();
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
            if (attributes.length != 2) {
                throw new RuntimeException("Problem loading properties '" + PROP_FILE + "' on line "
                        + key);
            }
            String taxonId = attributes[0];
            String identifier = attributes[1];

            Config config = configs.get(taxonId);
            if (config == null) {
                config = new Config();
                configs.put(taxonId, config);
            }

            if ("bioentity".equals(identifier)) {
                config.setBioentity(value);
            } else {
                config.setIdentifier(identifier);
                config.setDb(value);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(@SuppressWarnings("unused") Reader reader) throws Exception {
        String taxonId = getTaxonId();
        if (taxonId == null) {
            // this file isn't from an organism specified in the project file
            return;
        }
        setDataset();
        // chenyian: here is a little bit risky because sometimes there are other organisms
//        setOrganism(taxonId);
        setConfig(taxonId);

        // navigate through the owl file
        JenaIOHandler jenaIOHandler = new JenaIOHandler(null, BioPAXLevel.L2);
        Model model = jenaIOHandler.convertFromOWL(new FileInputStream(getCurrentFile()));
        Set<pathway> pathwaySet = model.getObjects(pathway.class);
        for (pathway pathwayObj : pathwaySet) {
            try {
                pathwayRefId = getPathway(pathwayObj);
            } catch (ObjectStoreException e) {
                pathwayRefId = null;
                continue;
            }
            visited = new HashSet<BioPAXElement>();
            traverser.traverse(pathwayObj, model);
        }
    }

    private void setConfig(String taxonId) {
        Config config = configs.get(taxonId);
        if (config != null) {
            dbName = config.getDb();
            identifierField = config.getIdentifier();
            bioentityType = config.getBioentity();
        }
    }

    /**
     * Sets the list of taxonIds that should be imported if using split input files.
     *
     * @param taxonIds a space-separated list of taxonIds
     */
    public void setBiopaxOrganisms(String taxonIds) {
        this.taxonIds = new HashSet<String>(Arrays.asList(StringUtils.split(taxonIds, " ")));
        LOG.info("Setting list of organisms to " + this.taxonIds);
    }

    /**
     * @param curated true or false
     */
    public void setBiopaxCurated(String curated) {
        this.curated = curated;
    }

    /**
     * @param name name of datasource
     * @throws ObjectStoreException if storing datasource fails
     */
    public void setBiopaxDatasourcename(String name)
        throws ObjectStoreException {
        this.dataSourceName = name;
        Item datasource = createItem("DataSource");
        datasource.setAttribute("name", name);
        try {
            store(datasource);
        } catch (ObjectStoreException e) {
            throw new ObjectStoreException(e);
        }
        dataSourceRefId = datasource.getIdentifier();
    }

    /**
     * @param title name of dataset
     * @throws ObjectStoreException if storing datasource fails
     */
    public void setBiopaxDatasetname(String title)
        throws ObjectStoreException {
        dataset = createItem("DataSet");
        dataset.setAttribute("name", title);
    }

    /**
     * Adds the BioPAX element into the model and traverses the element for its dependent elements.
     *
     * @param bpe    the BioPAX element to be added into the model
     * @param model  model into which the element will be added
     * @param editor editor that is going to be used for traversing functionallity
     * @see org.biopax.paxtools.controller.Traverser
     */
    public void visit(BioPAXElement bpe, Model model, PropertyEditor editor) {
        if (bpe != null) {
//        	LOG.info(String.format("bpe: %s; dep: %d; pid: %s.", 
//        			StringUtils.substringAfter(bpe.getRDFId(), "#"), depth, pathwayRefId));
            if (bpe instanceof org.biopax.paxtools.model.level2.protein) {
                org.biopax.paxtools.model.level2.protein entity
                    = (org.biopax.paxtools.model.level2.protein) bpe;
                String className = entity.getModelInterface().getSimpleName();
                if (className.equalsIgnoreCase("protein") && StringUtils.isNotEmpty(pathwayRefId)) {
                    processProteinEntry(entity);
                }
            }
            if (!visited.contains(bpe)) {
            	// chenyian: prevent to traverse the NEXT-STEP of a pathwayStep
                if (bpe instanceof pathwayStep) {
                	pathwayStep ps = (pathwayStep) bpe;
                		Set<process> step_interactions = ps.getSTEP_INTERACTIONS();
                		for (process process : step_interactions) {
                        	visited.add(bpe);
                        	depth++;
                        	traverser.traverse(process, model);
                        	depth--;
						}
                } else {
                	visited.add(bpe);
                	depth++;
                	traverser.traverse(bpe, model);
                	depth--;
                }
            }
        }
    }

    private void processProteinEntry(org.biopax.paxtools.model.level2.protein entity) {
        // chenyian: the format is different; re-implement
            Set<org.biopax.paxtools.model.level2.xref> xrefs = entity.getXREF();
            for (org.biopax.paxtools.model.level2.xref xref : xrefs) {
                if (xref.getDB().equals(DEFAULT_DB_NAME)) {
                	String taxonId = entity.getORGANISM().getTAXON_XREF().getID();
                    processBioentity(xref.getID(), taxonId, pathwayRefId);
                }
            }
    }

    // chenyian: format is different; re-implement
    private void processBioentity(String identifier, String taxonId, String pathway) {
        if (identifier.contains("-")) {
        	identifier = identifier.split("-")[0];
        }

        if (identifier == null || identifier.length() < 2) {
            LOG.warn(bioentityType + " not stored:" + identifier);
            return;
        }

        Item item = getBioentity(identifier, taxonId);
        item.addToCollection("pathways", pathway);
        return;
    }

    private String getPathway(org.biopax.paxtools.model.level2.pathway pathway)
        throws ObjectStoreException {
        Item item = createItem("Pathway");
        item.setAttribute("name", pathway.getNAME());
        if (StringUtils.isNotEmpty(pathway.getSHORT_NAME())) {
        	item.setAttribute("shortName", pathway.getSHORT_NAME());
        }
        item.setAttribute("curated", curated);
        item.addToCollection("dataSets", dataset);
        for (org.biopax.paxtools.model.level2.xref xref : pathway.getXREF()) {
        	// chenyian: the file got from Reactome has different format!
        	if (xref.getRDFId().contains("#unification")) {
        		String xrefId = xref.getID();
        		if (xref.getDB().equals(dataSourceName) && !xrefId.startsWith("REACT_")) {
        			item.setAttribute("identifier", xrefId);
        			try {
        				store(item);
        			} catch (ObjectStoreException e) {
        				throw new ObjectStoreException(e);
        			}
        			LOG.info(String.format("name: %s (id:%s)...", 
        					pathway.getNAME(),item.getIdentifier()));
        			return item.getIdentifier();
        		}
        	}
        }
        return null;
    }

    private Item getBioentity(String identifier, String taxonId) {
        Item item = bioentities.get(identifier);
        if (item == null) {
            item = createItem(bioentityType);
            item.setAttribute(identifierField, identifier);
            item.setReference("organism", getOrganism(taxonId));
            item.addToCollection("dataSets", dataset);
            bioentities.put(identifier, item);
        }
        return item;
    }

//    private void setOrganism(String taxonId)
//        throws ObjectStoreException {
//        organism = createItem("Organism");
//        organism.setAttribute("taxonId", taxonId);
//        try {
//            store(organism);
//        } catch (ObjectStoreException e) {
//            throw new ObjectStoreException(e);
//        }
//    }

    private void setDataset()
        throws ObjectStoreException {
        if (dataset.getReference("dataSource") == null) {
            dataset.setReference("dataSource", dataSourceRefId);
            try {
                store(dataset);
            } catch (ObjectStoreException e) {
                throw new ObjectStoreException(e);
            }
        }
    }


    /**
     * Use the file name currently being processed to divine the name of the organism.  Return null
     * if this taxonId is not in our list of taxonIds to be processed.
     * @return the taxonId of current organism
     */
    private String getTaxonId() {

        File file = getCurrentFile();
        String filename = file.getName();
        String[] bits = filename.split(" ");

        // bad filename eg `Human immunodeficiency virus 1.owl`,
        // expecting "Drosophila melanogaster.owl"
        if (bits.length != 2) {
            String msg = "Bad filename:  '" + filename + "'.  Expecting filename in the format "
                + "'Drosophila melanogaster.owl'";
            LOG.error(msg);
            return null;
        }

        String genus = bits[0];
        String species = bits[1].split("\\.")[0];
        String organismName = genus + " " + species;
        OrganismData od = or.getOrganismDataByGenusSpecies(genus, species);
        if (od == null) {
            LOG.error("No data for " + organismName + ".  Please add to repository.");
            return null;
        }

        int taxonId = od.getTaxonId();
        String taxonIdString = String.valueOf(taxonId);

        // only process the taxonids set in the project XML file - if any
        if (!taxonIds.isEmpty() && !taxonIds.contains(taxonIdString)) {
            return null;
        }
        return taxonIdString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close()
        throws ObjectStoreException {
        for (Item item : bioentities.values()) {
            store(item);
        }
    }

    /**
     * Class to hold the config info for each taxonId.
     */
    class Config
    {
        protected String bioentity;
        protected String identifier;
        protected String db;

        /**
         * Constructor.
         */
        Config() {
            // nothing to do
        }

        /**
         * @return the bioentity
         */
        public String getBioentity() {
            return bioentity;
        }

        /**
         * @param bioentity the bioentity to set
         */
        public void setBioentity(String bioentity) {
            this.bioentity = bioentity;
        }

        /**
         * @return the identifier
         */
        public String getIdentifier() {
            return identifier;
        }

        /**
         * @param identifier the identifier to set
         */
        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        /**
         * @return the db
         */
        public String getDb() {
            return db;
        }

        /**
         * @param db the db to set
         */
        public void setDb(String db) {
            this.db = db;
        }
    }
}
