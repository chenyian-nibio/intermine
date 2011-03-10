package org.intermine.bio.postprocess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.intermine.model.bio.DataSet;
import org.intermine.model.bio.DataSource;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.Interaction;
import org.intermine.model.bio.InteractionExperiment;
import org.intermine.model.bio.Protein;
import org.intermine.model.bio.ProteinInteraction;
import org.intermine.model.bio.ProteinInteractionSource;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.intermine.ObjectStoreWriterInterMineImpl;
import org.intermine.objectstore.query.ConstraintOp;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryCollectionReference;
import org.intermine.objectstore.query.QueryObjectReference;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.util.DynamicUtil;

public class PPI2GGITask {
	
	protected ObjectStoreWriterInterMineImpl osw;
	
	protected ObjectStore os;
	
	private DataSet oPpiViewDs = null;
	
	private Map<String, Gene> oGeneMap = new HashMap<String, Gene>();
	
	private Map<String, DataSource> oDsMap = new HashMap<String, DataSource>();
	
	private Map<String, InteractionExperiment> oIsMap = new HashMap<String, InteractionExperiment>();
	
	public PPI2GGITask(ObjectStoreWriter osw) {
        if (osw instanceof ObjectStoreWriterInterMineImpl) {
            this.osw = (ObjectStoreWriterInterMineImpl) osw;
            this.os = osw.getObjectStore();
        } else {
            throw new RuntimeException("the ObjectStoreWriter is not an "
                                       + "ObjectStoreWriterInterMineImpl");
        }
	}
	
	public void ppi2ggi() throws Exception {
		
		this.oPpiViewDs = (DataSet)DynamicUtil.createObject( Collections.singleton( DataSet.class ) );
		this.oPpiViewDs.setName("PPI View");
		osw.store(this.oPpiViewDs);
		
		//HashMap<String, Gene> oMap = new HashMap<String, Gene>();
		HashMap<String, String> oMap = new HashMap<String, String>();
		List<PISet> oPiSetList = new ArrayList<PISet>();
		// protein-gene relation
		Query pGQuery = new Query();
		QueryClass pQc = new QueryClass(Protein.class);
		QueryClass gQc = new QueryClass(Gene.class);
		
		pGQuery.addFrom(pQc);
		pGQuery.addFrom(gQc);
		pGQuery.addToSelect(pQc);
		pGQuery.addToSelect(gQc);
		
		ConstraintSet pGCs = new ConstraintSet(ConstraintOp.AND);
		QueryCollectionReference p2GRef = new QueryCollectionReference( pQc, "genes" );
		ContainsConstraint p2GConstraint =
			new ContainsConstraint( p2GRef, ConstraintOp.CONTAINS, gQc );
		pGCs.addConstraint(p2GConstraint);
		
		pGQuery.setConstraint(pGCs);
		
		Results pGResults = osw.getObjectStore().execute(pGQuery);
		Iterator pGIter = pGResults.iterator();
		
		while(pGIter.hasNext()){
			
			ResultsRow row = (ResultsRow)pGIter.next();
			Protein oProtein = (Protein)row.get(0);
			Gene oGene = (Gene)row.get(1);
//			System.out.println("pid:" + oProtein.getPrimaryIdentifier()+", geneid:" +oGene.getNcbiGeneNumber());
			oMap.put( oProtein.getPrimaryIdentifier(), oGene.getNcbiGeneNumber() );
			
		}
		
		// fetch all ProteinInteraction objects and related Protein and it's corresponding Gene 
		// and ProteinInteractionSource
		QueryClass rpQc = new QueryClass(Protein.class); //Representative partner
		
		Query pISQuery = new Query();
		QueryClass pIQc = new QueryClass(ProteinInteraction.class);
		QueryClass pISQc = new QueryClass(ProteinInteractionSource.class);
		
		pISQuery.addFrom(pQc);
		pISQuery.addFrom(pIQc);
		pISQuery.addFrom(pISQc);
		pISQuery.addFrom(rpQc); //representative partner
		
		pISQuery.addToSelect(pQc);
		pISQuery.addToSelect(pIQc);
		pISQuery.addToSelect(pISQc);
		pISQuery.addToSelect(rpQc); //representative partner
		
		ConstraintSet pISCs = new ConstraintSet(ConstraintOp.AND);
		
		QueryCollectionReference p2PIRef = new QueryCollectionReference( pQc, "proteinInteractions" );
		ContainsConstraint p2PIConstraint =
			new ContainsConstraint( p2PIRef, ConstraintOp.CONTAINS, pIQc );
		pISCs.addConstraint( p2PIConstraint );
		
		QueryCollectionReference pI2PISRef = new QueryCollectionReference( pIQc, "piSources" );
		ContainsConstraint pI2PISConstraint =
			new ContainsConstraint( pI2PISRef, ConstraintOp.CONTAINS, pISQc );
		pISCs.addConstraint( pI2PISConstraint );
		
		QueryObjectReference pI2RpRef = new QueryObjectReference( pIQc, "representativePartner" );
		ContainsConstraint pI2RpConstraint =
			new ContainsConstraint( pI2RpRef, ConstraintOp.CONTAINS, rpQc );
		pISCs.addConstraint( pI2RpConstraint );
		
		pISQuery.setConstraint(pISCs);
		
		Results pISResults = osw.getObjectStore().execute(pISQuery);
		Iterator pISIter = pISResults.iterator();
		
		while(pISIter.hasNext()){
			
			ResultsRow row = (ResultsRow)pISIter.next();
			
			Protein oProtein = (Protein)row.get(0);
			ProteinInteraction oPI = (ProteinInteraction)row.get(1);
			ProteinInteractionSource oPIS = (ProteinInteractionSource)row.get(2);
			Protein oRepPartner = (Protein)row.get(3);
			
			PISet oPiSet = new PISet(
					oProtein.getPrimaryIdentifier(),
					oRepPartner.getPrimaryIdentifier(),
					oPIS.getIdentifier(),
					oPIS.getDbName(),
					oPI.getIntId());
//			System.out.println(String.format("%s, %s, %s, %s, %s",
//					oProtein.getPrimaryIdentifier(),
//					oRepPartner.getPrimaryIdentifier(),
//					oPIS.getIdentifier(),
//					oPIS.getDbName(),
//					oPI.getIntId() ));
			oPiSetList.add(oPiSet);
		}
		Iterator<PISet> oItr = oPiSetList.iterator();
		while(oItr.hasNext()){
			PISet oPiSet = oItr.next();
//			System.out.println("oPiSet.getIntId():" + oPiSet.getIntId() + ", geneid:" + oMap.get( oPiSet.getProteinPrimaryIdentifier() ));
			//Gene oGene = (Gene)DynamicUtil.createObject( Collections.singleton( Gene.class ) );
			//oGene.setNcbiGeneNumber(oMap.get( oPiSet.getProteinPrimaryIdentifier() ));
			// ignore if gene has no ncbigenenumber
			if(!oMap.containsKey( oPiSet.getProteinPrimaryIdentifier()) || null == oMap.get( oPiSet.getProteinPrimaryIdentifier() )){
				continue;
			}
			Gene oGene = getGene(oMap.get( oPiSet.getProteinPrimaryIdentifier() ));
			
			//Gene oPartnerGene = (Gene)DynamicUtil.createObject( Collections.singleton( Gene.class ) );
			//oPartnerGene.setNcbiGeneNumber(oMap.get( oPiSet.getRepresentativePartnerPrimaryIdentifier() ));
			if(!oMap.containsKey( oPiSet.getRepresentativePartnerPrimaryIdentifier()) || null == oMap.get( oPiSet.getRepresentativePartnerPrimaryIdentifier() )){
				continue;
			}
			Gene oPartnerGene = getGene(oMap.get( oPiSet.getRepresentativePartnerPrimaryIdentifier() ));
			
			Interaction oInteraction =
                (Interaction) DynamicUtil.createObject( Collections.singleton( Interaction.class ) );
			//oGene.addInteractions(oInteraction);
			oInteraction.addInteractingGenes(oPartnerGene);
//			String intName = "PPIView:" + oPiSet.getIntId();
			// chenyian: follow the naming rule of biogrid dataset 
			// short name use gene id and name use uniprot id for keeping original info
			String intName;
			String sName;
			if (oGene.getNcbiGeneNumber().equals(oPartnerGene.getNcbiGeneNumber())) {
				intName = "PPIView:" + oPiSet.getProteinPrimaryIdentifier();
				sName = "PPIView:" + oGene.getNcbiGeneNumber();
			} else {
				intName = "PPIView:" + oPiSet.getProteinPrimaryIdentifier() + "_" + oPiSet.getRepresentativePartnerPrimaryIdentifier();
				sName = "PPIView:" + oGene.getNcbiGeneNumber() + "_" + oPartnerGene.getNcbiGeneNumber();
			}
			oInteraction.setShortName( sName );
			oInteraction.setName( intName );
			// chenyian: interactionType should be 'physical'
//			oInteraction.setInteractionType("ppi");
			oInteraction.setInteractionType("physical");
			oInteraction.setExperiment( getIE(oPiSet.getInteractionSourceId(), oPiSet.getInteractionSourceDb()) );
			oInteraction.setGene(oGene);
			//osw.store(oGene);
			osw.store(oInteraction);
			//oGene.addInteractions(oInteraction);
			osw.addToCollection(oInteraction.getId(),Interaction.class,"dataSets",this.oPpiViewDs.getId());
		}
		
	}
	
	public InteractionExperiment getIE(String id, String db)
	 throws ObjectStoreException{
		
		if(!oIsMap.containsKey(id + db)){
			
			InteractionExperiment oInteractionExperiment = (InteractionExperiment) DynamicUtil.createObject( Collections.singleton( InteractionExperiment.class ) );
			oInteractionExperiment.setName(id);
			oInteractionExperiment.setDescription(db + " " + id);
			osw.store(oInteractionExperiment);
			oIsMap.put(id+db, oInteractionExperiment);
			
		}
		return oIsMap.get(id+db);
		
	}
	
	public Gene getGene(String ncbiGeneNumber){
		
		Gene oGene = (Gene)DynamicUtil.createObject( Collections.singleton( Gene.class ) );
		oGene.setNcbiGeneNumber(ncbiGeneNumber);
		try {
            oGene = (Gene) os.getObjectByExample(oGene,
                    Collections.singleton("ncbiGeneNumber"));
        } catch (ObjectStoreException e) {
            throw new RuntimeException(
                    "unable to fetch FlyMine DataSource object", e);
        }
        return oGene;
	}
	
	private class PISet{
		public String proteinPrimaryIdentifier;
		
		public String representativePartnerPrimaryIdentifier;

		public String interactionSourceId;
		
		public String interactionSourceDb;
		
		public Integer intId;
		
		public PISet(String ppi, String rppi, String isID, String isDb, Integer intId){
			//System.out.println(ppi + " "+ rppi + " " + isID + " " + isDb + " " + intId);
			this.proteinPrimaryIdentifier = ppi;
			this.representativePartnerPrimaryIdentifier = rppi;
			this.interactionSourceId = isID;
			this.interactionSourceDb = isDb;
			this.intId = intId;
		}
		
		public String getProteinPrimaryIdentifier() {
			return proteinPrimaryIdentifier;
		}

		public void setProteinPrimaryIdentifier(String proteinPrimaryIdentifier) {
			this.proteinPrimaryIdentifier = proteinPrimaryIdentifier;
		}

		public String getRepresentativePartnerPrimaryIdentifier() {
			return representativePartnerPrimaryIdentifier;
		}

		public void setRepresentativePartnerPrimaryIdentifier(
				String representativePartnerPrimaryIdentifier) {
			this.representativePartnerPrimaryIdentifier = representativePartnerPrimaryIdentifier;
		}

		public String getInteractionSourceId() {
			return interactionSourceId;
		}

		public void setInteractionSourceId(String interactionSourceId) {
			this.interactionSourceId = interactionSourceId;
		}

		public String getInteractionSourceDb() {
			return interactionSourceDb;
		}
		
		public void setInteractionSourceDb(String interactionSourceDb) {
			this.interactionSourceDb = interactionSourceDb;
		}

		public Integer getIntId() {
			return intId;
		}

		public void setIntId(Integer intId) {
			this.intId = intId;
		}
		
	}
	
}

