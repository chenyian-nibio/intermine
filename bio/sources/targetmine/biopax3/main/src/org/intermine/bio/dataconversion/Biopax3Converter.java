package org.intermine.bio.dataconversion;

import java.io.FileInputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.controller.Traverser;
import org.biopax.paxtools.controller.Visitor;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.RelationshipXref;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.xml.full.Item;

/**
 * used for parsing reactome biopax3 format file
 *    
 * @author chenyian
 */
public class Biopax3Converter extends BioFileConverter {

	private static final Logger LOG = Logger.getLogger(Biopax3Converter.class);
	//
	private static final String DATASET_TITLE = "Reactome";
	private static final String DATA_SOURCE_NAME = "Reactome";

	private Map<String, PathwayEntry> pathwayEntryMap = new HashMap<String, PathwayEntry>();

	private Set<BioPAXElement> visited;

	private Map<String, Item> pathwayMap = new HashMap<String, Item>();
	private Map<String, Item> proteinMap = new HashMap<String, Item>();

	private Item currentPathway;

	private Traverser traverser;

	private String taxonId = null;

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public Biopax3Converter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void process(Reader reader) throws Exception {
		String fn = getCurrentFile().getName();
		if (fn.startsWith("Homo")){
			taxonId = "9606";
		} else if(fn.startsWith("Mus")){
			taxonId = "10090";
		} else if (fn.startsWith("Rattus")){
			taxonId = "10116";
		} else {
			throw new RuntimeException("Unknown species: " + fn);
		}

		SimpleIOHandler handler = new SimpleIOHandler();
		// JenaIOHandler handler = new JenaIOHandler(new Level3FactoryImpl(), BioPAXLevel.L3);
		org.biopax.paxtools.model.Model owlModel = handler.convertFromOWL(new FileInputStream(
				getCurrentFile()));
		Set<Pathway> pathways = owlModel.getObjects(Pathway.class);

		traverser = new Traverser(SimpleEditorMap.L3, new Visitor() {

			@SuppressWarnings("rawtypes")
			@Override
			public void visit(BioPAXElement biopaxelement, Object range,
					org.biopax.paxtools.model.Model model, PropertyEditor propertyeditor) {
				// skip the 'nextStep' pathway
				if (propertyeditor.getProperty().equals("nextStep")) {
					return;
				}
				if (range != null && range instanceof BioPAXElement && !visited.contains(range)) {
					BioPAXElement bpe = (BioPAXElement) range;

					if (bpe instanceof Protein) {
						Protein p = (Protein) bpe;

						EntityReference er = p.getEntityReference();
						if (er != null) {
							if (er instanceof ProteinReference) {
								String taxonId = ((ProteinReference) er).getOrganism().getXref()
										.iterator().next().getId();
								for (Xref x : er.getXref()) {
									if (x instanceof UnificationXref
											|| x instanceof RelationshipXref) {
										// LOG.info(String.format("Protein: %s", x.getId()));
										String identifier = x.getId();
										if (identifier.contains("-")) {
											identifier = identifier.split("-")[0];
										}
										if (StringUtils.isEmpty(identifier)) {
											continue;
										}
										// discard the ensembl proteins 
										// unless we want to translate to uniport id (maybe not worth)
										if (identifier.startsWith("ENS")) {
											continue;
										}
										Item item = getProtein(identifier, taxonId);
										item.addToCollection("pathways", currentPathway);
									}
								}
							}
						} else {
//							LOG.error("Null EntityReference! " + p.getRDFId());
						}

					}
					visited.add(bpe);

					// go deeper
					traverser.traverse(bpe, model);
				}
			}
		});

		for (Pathway pathway : pathways) {
			// LOG.info(pathway.getDisplayName());

			String pathwayId = getReactomeId(pathway);
			if (pathwayId == null) {
				LOG.error("Cannot find Reactome ID: " + pathway.getRDFId());
				continue;
			}
			PathwayEntry parentPe = getPathwayEntry(pathwayId);
			Set<org.biopax.paxtools.model.level3.Process> processes = pathway.getPathwayComponent();
			for (org.biopax.paxtools.model.level3.Process process : processes) {
				if (process instanceof Pathway) {
					getPathwayEntry(getReactomeId((Pathway) process)).setParentPathway(parentPe);
				}
			}

			currentPathway = getPathway(pathwayId);
			String pathwayName = pathway.getDisplayName();
			Set<String> comments = pathway.getComment();
			Iterator<String> iterator = comments.iterator();
			String comment = null;
			while (iterator.hasNext()) {
				String c = iterator.next();
				if (c.matches("^\\w+ed:\\s.+")) {
					continue;
				} else {
					comment = c;
					break;
				}
			}
			if (comment != null) {
				if (comment.startsWith("This event has been computationally inferred")) {
//					pathwayName += " (computationally inferred)";
					currentPathway.setAttribute("label2", "computationally inferred");
				}
				
				currentPathway.setAttribute("description", comment.replaceAll("<\\/?.+?>", " "));
			}
			
//			LOG.info(pathwayId + ": " + pathwayName);
			currentPathway.setAttribute("name", pathwayName);
			

			visited = new HashSet<BioPAXElement>();
			traverser.traverse(pathway, owlModel);
		}

	}

	private String getReactomeId(Pathway pathway) {
		String ret = null;
		for (Xref xref : pathway.getXref()) {
			if (xref instanceof UnificationXref && xref.getDb().equals("Reactome")) {
				ret = xref.getId();
			}
		}
		return ret;
	}

	private Item getPathway(String pathwayId) {
		Item ret = pathwayMap.get(pathwayId);
		if (ret == null) {
			ret = createItem("Pathway");
			ret.setAttribute("identifier", pathwayId);
			ret.setReference("organism", getOrganism(taxonId));
			pathwayMap.put(pathwayId, ret);
		}
		return ret;
	}

	private Item getProtein(String uniprotAcc, String taxonId) {
		Item ret = proteinMap.get(uniprotAcc);
		if (ret == null) {
			ret = createItem("Protein");
			ret.setAttribute("primaryAccession", uniprotAcc);
			ret.setReference("organism", getOrganism(taxonId));
			proteinMap.put(uniprotAcc, ret);
		}
		return ret;
	}

	@Override
	public void close() throws Exception {
		// add the level information
		for (String pid : pathwayMap.keySet()) {
			Item item = pathwayMap.get(pid);
			item.setAttribute("label1", "lv" + String.valueOf(pathwayEntryMap.get(pid).getLevel()));
			for (String parentId : pathwayEntryMap.get(pid).getParentIds()) {
				item.addToCollection("parents", pathwayMap.get(parentId));
			}
			store(item);
		}
		store(proteinMap.values());
	}

	private PathwayEntry getPathwayEntry(String identifier) {
		PathwayEntry ret = pathwayEntryMap.get(identifier);
		if (ret == null) {
			ret = new PathwayEntry(identifier);
			pathwayEntryMap.put(identifier, ret);
		}
		return ret;
	}

	private static class PathwayEntry {
		private String identifier;
		private PathwayEntry parentPathway;

		public PathwayEntry(String identifier) {
			this.identifier = identifier;
		}

		public String getIdentifier() {
			return identifier;
		}

		public PathwayEntry getParentPathway() {
			return parentPathway;
		}

		public void setParentPathway(PathwayEntry parentPathway) {
			this.parentPathway = parentPathway;
		}

		public int getLevel() {
			PathwayEntry parentPathway = getParentPathway();
			if (parentPathway != null) {
				return parentPathway.getLevel() + 1;
			}
			return 1;
		}
		
		public Set<String> getParentIds() {
			HashSet<String> ret = new HashSet<String>();
			if (parentPathway != null) {
				ret.addAll(parentPathway.getParentIds());
				ret.add(parentPathway.getIdentifier());
			}
			return ret;
		}
	}

}
