package org.intermine.bio.web.widget;

import org.intermine.model.bio.ProteinDomain;
import org.intermine.model.bio.ProteinDomainRegion;

/**
 * For sorting ProteinDomainRegion objects
 * @author Motokazu Ishikawa
 */
public class ProteinDomainRegionWrapper implements Comparable<ProteinDomainRegionWrapper> {
	
	/**
	 * Wrapped ProteinDomainRegion
	 */
	private ProteinDomainRegion m_oPdr;
	
	/**
	 * Constructor
	 * @param oPdr
	 */
	public ProteinDomainRegionWrapper(ProteinDomainRegion oPdr) {
		m_oPdr = oPdr;
	}
	
	public ProteinDomainRegion getProteinDomainRegion() {
		return m_oPdr;
	}

	public int compareTo(ProteinDomainRegionWrapper oCompared) {
		ProteinDomainRegion oPdr1 = m_oPdr;
		ProteinDomainRegion oPdr2 = oCompared.getProteinDomainRegion();
		
		ProteinDomain oPd1 = oPdr1.getProteinDomain();
		ProteinDomain oPd2 = oPdr2.getProteinDomain();
		
		String strIpr1 = oPd1.getPrimaryIdentifier();
		String strIpr2 = oPd2.getPrimaryIdentifier();
		
		int iCompareIpr = strIpr1.compareTo(strIpr2);
		
		if(iCompareIpr != 0) {
			return iCompareIpr;
		} else {
			int iCompareStart = oPdr1.getStart() - oPdr2.getStart();
			
			if(iCompareStart != 0) {
				return iCompareStart;
			} else {
				return oPdr1.getId() - oPdr2.getId();
			}
		}
	}
	
}
