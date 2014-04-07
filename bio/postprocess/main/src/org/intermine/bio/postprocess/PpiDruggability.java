package org.intermine.bio.postprocess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.Interaction;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.query.ConstraintOp;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryObjectReference;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.util.DynamicUtil;
import org.intermine.util.FormattedTextParser;

public class PpiDruggability {
	private static final Logger LOG = Logger.getLogger(PpiDruggability.class);

	protected ObjectStoreWriter osw;

	private Model model;

	private File drpiasFile;

	private Map<String, DrpiasData> dataMap = new HashMap<String, DrpiasData>();

	private Map<String, InterMineObject> ppiDruggabilityMap = new HashMap<String, InterMineObject>();

	public void setDrpiasFile(File drpiasFile) {
		this.drpiasFile = drpiasFile;
	}

	public PpiDruggability(ObjectStoreWriter osw) {
		this.osw = osw;
		model = Model.getInstanceByName("genomic");
	}

	public void annotatePpiDruggabilities() {
		readDruggabilityMap();

		Results results = queryInteractions();

		System.out.println(results.size() + " interactions found.");

		Iterator<?> iterator = results.iterator();

		int count = 0;
		try {
			osw.beginTransaction();
			while (iterator.hasNext()) {
				ResultsRow<?> result = (ResultsRow<?>) iterator.next();
				Interaction interaction = (Interaction) result.get(0);
				Gene gene1 = (Gene) result.get(1);
				Gene gene2 = (Gene) result.get(2);
				
				InterMineObject ppiDruggability = getPpiDruggability(gene1.getPrimaryIdentifier(), gene2.getPrimaryIdentifier());
				
				if (ppiDruggability != null) {
					interaction.setFieldValue("ppiDruggability", ppiDruggability);
					osw.store(interaction);
					
					count++;
				}

			}
			osw.commitTransaction();

		} catch (ObjectStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(String.format("There were %d interactions annotated with ppi druggability.", count));

	}

	private InterMineObject getPpiDruggability(String gene1, String gene2)
			throws ObjectStoreException {
		InterMineObject ret = ppiDruggabilityMap.get(String.format("%s-%s", gene1, gene2));
		if (ret == null) {
			DrpiasData drpiasData = dataMap.get(String.format("%s-%s", gene1, gene2));
			if (drpiasData != null) {
				InterMineObject item = (InterMineObject) DynamicUtil.simpleCreateObject(model
						.getClassDescriptorByName("PpiDruggability").getType());
				item.setFieldValue("structuralScore", drpiasData.getStructuralScore());
				item.setFieldValue("drugChemicalScore", drpiasData.getDrugChemicalScore());
				item.setFieldValue("functionalScore", drpiasData.getFunctionalScore());
				item.setFieldValue("allScore", drpiasData.getAllScore());

				ppiDruggabilityMap.put(String.format("%s-%s", gene1, gene2), item);
				// for the convenience ...
				ppiDruggabilityMap.put(String.format("%s-%s", gene2, gene1), item);

				osw.store(item);
			}
		}
		return ret;
	}

	private void readDruggabilityMap() {
		try {
			int count = 0;

			FileReader reader = new FileReader(drpiasFile);
			Iterator<String[]> iterator = FormattedTextParser.parseCsvDelimitedReader(reader);
			while (iterator.hasNext()) {
				String[] cols = iterator.next();
				dataMap.put(
						String.format("%s-%s", cols[0], cols[1]),
						new DrpiasData(Float.valueOf(cols[2]), Float.valueOf(cols[3]), Float
								.valueOf(cols[4]), Float.valueOf(cols[5])));
				// for the convenience ...
				dataMap.put(
						String.format("%s-%s", cols[1], cols[0]),
						new DrpiasData(Float.valueOf(cols[2]), Float.valueOf(cols[3]), Float
								.valueOf(cols[4]), Float.valueOf(cols[5])));
				count++;
			}

			System.out.println(count + " PPI druggability entries were read.");

			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Results queryInteractions() {
		Query q = new Query();
		QueryClass qcGene1 = new QueryClass(Gene.class);
		QueryClass qcGene2 = new QueryClass(Gene.class);
		QueryClass qcInteraction = new QueryClass(Interaction.class);

		q.addFrom(qcInteraction);
		q.addFrom(qcGene1);
		q.addFrom(qcGene2);
		q.addToSelect(qcInteraction);
		q.addToSelect(qcGene1);
		q.addToSelect(qcGene2);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);
		QueryObjectReference qor3 = new QueryObjectReference(qcInteraction, "gene1");
		cs.addConstraint(new ContainsConstraint(qor3, ConstraintOp.CONTAINS, qcGene1));
		QueryObjectReference qor4 = new QueryObjectReference(qcInteraction, "gene2");
		cs.addConstraint(new ContainsConstraint(qor4, ConstraintOp.CONTAINS, qcGene2));
		q.setConstraint(cs);

		ObjectStore os = osw.getObjectStore();

		return os.execute(q);
	}

	private class DrpiasData {
		private Float structuralScore;
		private Float drugChemicalScore;
		private Float functionalScore;
		private Float allScore;

		public DrpiasData(Float structuralScore, Float drugChemicalScore, Float functionalScore,
				Float allScore) {
			this.structuralScore = structuralScore;
			this.drugChemicalScore = drugChemicalScore;
			this.functionalScore = functionalScore;
			this.allScore = allScore;
		}

		public Float getStructuralScore() {
			return structuralScore;
		}

		public Float getDrugChemicalScore() {
			return drugChemicalScore;
		}

		public Float getFunctionalScore() {
			return functionalScore;
		}

		public Float getAllScore() {
			return allScore;
		}
	}
}
