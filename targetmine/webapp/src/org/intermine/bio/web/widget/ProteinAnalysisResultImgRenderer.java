package org.intermine.bio.web.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.intermine.api.InterMineAPI;
import org.intermine.metadata.ConstraintOp;
import org.intermine.model.bio.Protein;
import org.intermine.model.bio.ProteinDomain;
import org.intermine.model.bio.ProteinDomainRegion;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.query.BagConstraint;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryCollectionReference;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.SingletonResults;
import org.intermine.web.logic.session.SessionMethods;
import org.intermine.web.struts.InterMineAction;

public class ProteinAnalysisResultImgRenderer extends InterMineAction {

	private static Logger m_oLogger = Logger.getLogger(ProteinAnalysisResultImgRenderer.class);

	// left margin
	private static int X_MARGIN = 10;

	// pixels per one amino acid on protein scale
	private static int WIDTH_PER_AA = 1;

	// Height of protein scale area
	private static int SCALE_HEIGHT = 20;

	// Height of protein scale mark height
	private static int SCALE_MARK_HEIGHT = 5;

	// Width of area for tool's name
	private static int TOOL_NAME_WIDTH = 500;

	// X offset of tool name
	private static int TOOL_NAME_X_OFFSET = 20;

	// Y offset of tool name
	private static int TOOL_NAME_Y_OFFSET = 20;

	// Y margin of hit region
	private static int HIT_REGION_Y_MARGIN = 5;

	// Height of area for one tool's name
	private static int ONE_TOOL_NAME_HEIGHT = 25;

	// Color for back ground
	private static Color m_oColorOfBG = Color.WHITE;

	// Color for scale of protein
	private static Color m_oColorOfSP = Color.BLACK;

	// Color for gauge
	private static Color m_oColorOfG = Color.LIGHT_GRAY;

	// Stroke for scale of protein length
	private static Stroke m_oStrokeOfSP = new BasicStroke(1);

	// Font for scale of protein length
	private static Font m_oFontOfSP = new Font("Times New Roman", Font.PLAIN, 10);

	// Color for tool name
	private static Color m_oColorOfTN = Color.BLACK;

	// Font for tool name
	private static Font m_oFontOfTool = new Font("Times New Roman", Font.PLAIN, 12);

	public ProteinAnalysisResultImgRenderer() {

	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		m_oLogger.info("analysis result renderer start");

        final InterMineAPI im = SessionMethods.getInterMineAPI(request.getSession());

        String strId = request.getParameter("object");

		// chenyian: seems intermine 0.93 changes the way to get ObjectStore,
        // the constant OBJECTSTORE was removed. 
        ObjectStore oOs = im.getObjectStore();
		Protein oProtein = (Protein) oOs.getObjectById(Integer.valueOf(strId));

		Collection<ProteinDomainRegion> oRes = getProteinDomainRegions(oOs, oProtein);

		BufferedImage oImg = drawGraph(oProtein, oRes);
		ServletOutputStream oSos = response.getOutputStream();
		ImageIO.write(oImg, "png", oSos);
		oSos.close();
		return null;
	}

	private Collection<ProteinDomainRegion> getProteinDomainRegions(ObjectStore oOs,
			Protein oProtein) {

		// create all needed query classes
		QueryClass oQcProtein = new QueryClass(Protein.class);
		QueryClass oQcPdr = new QueryClass(ProteinDomainRegion.class);

		// create needed references between the clases
		QueryCollectionReference oQcrPdr = new QueryCollectionReference(oQcProtein,
				"proteinDomainRegions");
		//QueryObjectReference oQoePd
		//= new QueryObjectReference(oQcPdr, "proteinDomain");

		// build up constraint
		ConstraintSet oCs = new ConstraintSet(ConstraintOp.AND);
		List<String> oAccs = new ArrayList<String>();
		oAccs.add(oProtein.getPrimaryAccession());
		QueryField oQf = new QueryField(oQcProtein, "primaryAccession");
		BagConstraint oBc = new BagConstraint(oQf, ConstraintOp.IN, oAccs);
		ContainsConstraint oCcPdr = new ContainsConstraint(oQcrPdr, ConstraintOp.CONTAINS, oQcPdr);
		//ContainsConstraint oCcPr = new ContainsConstraint(oQoePd, ConstraintOp.CONTAINS, oQcPr);

		// build up query
		Query oQ = new Query();
		oCs.addConstraint(oBc);
		oCs.addConstraint(oCcPdr);
		//oCs.addConstraint(oCcPr);

		oQ.setConstraint(oCs);
		oQ.addToSelect(oQcPdr);
		oQ.addFrom(oQcProtein);
		oQ.addFrom(oQcPdr);
		//oQ.addFrom(oQcPr);

		// chenyian: modified for upgrading to v0.95
		SingletonResults srs = oOs.executeSingleton(oQ);
		List<ProteinDomainRegion> ret = new ArrayList<ProteinDomainRegion>();
		for (Object sr : srs) {
			ret.add((ProteinDomainRegion) sr);
		}
		
		return ret;
	}

	private BufferedImage drawGraph(Protein oProtein, Collection<ProteinDomainRegion> oRes) {

		int iWidth = X_MARGIN + oProtein.getLength() * WIDTH_PER_AA + TOOL_NAME_WIDTH;

		int iHeight = SCALE_HEIGHT + ONE_TOOL_NAME_HEIGHT * oRes.size();

		BufferedImage oImg = new BufferedImage(iWidth, iHeight, BufferedImage.TYPE_INT_RGB);

		Graphics2D oG2 = oImg.createGraphics();
		oG2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// fill background
		oG2.setPaint(m_oColorOfBG);
		oG2.fillRect(0, 0, iWidth, iHeight);

		// draw scale for protein length
		drawScale(oG2, oProtein, iHeight);

		// Sort ProteinDomainRegion by identifier and start 
		TreeSet<ProteinDomainRegionWrapper> oPdrSet = new TreeSet<ProteinDomainRegionWrapper>();

		for (ProteinDomainRegion oPdr : oRes) {
			oPdrSet.add(new ProteinDomainRegionWrapper(oPdr));
		}

		// Draw each ProteinDomainRegion
		int iPdrCount = 0;
		int iIprCount = 0;
		String strCurrentIpr = "";

		for (ProteinDomainRegionWrapper oWrapper : oPdrSet) {

			ProteinDomainRegion oPdr = oWrapper.getProteinDomainRegion();
			ProteinDomain oPd = oPdr.getProteinDomain();

			if (!strCurrentIpr.equals(oPd.getPrimaryIdentifier())) {
				iIprCount++;
				strCurrentIpr = oPd.getPrimaryIdentifier();
			}

			// draw tool name
			oG2.setPaint(m_oColorOfTN);
			oG2.setFont(m_oFontOfTool);
			oG2.drawString(oPd.getPrimaryIdentifier() + " " + oPdr.getOriginalId() + " "
					+ oPd.getName(), X_MARGIN + oProtein.getLength() * WIDTH_PER_AA
					+ TOOL_NAME_X_OFFSET, SCALE_HEIGHT + ONE_TOOL_NAME_HEIGHT * iPdrCount
					+ TOOL_NAME_Y_OFFSET);

			// draw domain region
			oG2.setPaint(getColor4IPR(iIprCount));
			oG2.fillRect(X_MARGIN + oPdr.getStart() * WIDTH_PER_AA, SCALE_HEIGHT
					+ ONE_TOOL_NAME_HEIGHT * iPdrCount + HIT_REGION_Y_MARGIN, (oPdr.getEnd() - oPdr
					.getStart())
					* WIDTH_PER_AA, ONE_TOOL_NAME_HEIGHT - 2 * HIT_REGION_Y_MARGIN);

			iPdrCount++;

		}

		return oImg;

	}

	/**
	 * Draw scale for protein length
	 * 
	 * @param oG2
	 *            Graphics2D
	 * @param oProtein
	 *            scale will drawn for
	 * @param iHeight
	 *            graph height
	 */
	private void drawScale(Graphics2D oG2, Protein oProtein, int iHeight) {
		oG2.setPaint(m_oColorOfSP);
		oG2.setStroke(m_oStrokeOfSP);
		oG2.drawLine(X_MARGIN, SCALE_HEIGHT, X_MARGIN + oProtein.getLength() * WIDTH_PER_AA,
				SCALE_HEIGHT);

		for (int iM = 0; iM * 100 < oProtein.getLength(); iM++) {
			int iPosition = iM * 100;

			oG2.setPaint(m_oColorOfSP);
			oG2.drawLine(X_MARGIN + WIDTH_PER_AA * iPosition, SCALE_HEIGHT - SCALE_MARK_HEIGHT,
					X_MARGIN + WIDTH_PER_AA * iPosition, SCALE_HEIGHT);

			oG2.setFont(m_oFontOfSP);
			oG2.drawString(String.valueOf(iPosition), X_MARGIN + WIDTH_PER_AA * iPosition,
					SCALE_HEIGHT - SCALE_MARK_HEIGHT);

			oG2.setPaint(m_oColorOfG);
			oG2.drawLine(X_MARGIN + WIDTH_PER_AA * iPosition, SCALE_HEIGHT, X_MARGIN + WIDTH_PER_AA
					* iPosition, iHeight);
		}

		oG2.setPaint(m_oColorOfSP);
		oG2.drawLine(X_MARGIN + WIDTH_PER_AA * oProtein.getLength(), SCALE_HEIGHT
				- SCALE_MARK_HEIGHT, X_MARGIN + WIDTH_PER_AA * oProtein.getLength(), SCALE_HEIGHT);

	}

	private Color getColor4IPR(int iIprSerial) {

		switch (iIprSerial % 6) {
		case 0:
			return Color.RED;
		case 1:
			return new Color(204, 102, 0);
		case 2:
			return Color.YELLOW;
		case 3:
			return Color.GREEN;
		case 4:
			return Color.BLUE;
		case 5:
			return new Color(204, 153, 204);
		default:
			return Color.BLACK;
		}

	}


}
