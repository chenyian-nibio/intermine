package org.intermine.web;

/*
 * Copyright (C) 2002-2005 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreSummary;
import org.intermine.objectstore.query.BagConstraint;
import org.intermine.objectstore.query.ClassConstraint;
import org.intermine.util.TypeUtil;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.actions.TilesAction;

/**
 * Controller for the main constraint editing tile
 * @author Thomas Riley
 */
public class MainConstraintController extends TilesAction
{
    /**
     * @see TilesAction#execute
     */
    public ActionForward execute(ComponentContext context,
                                 ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();
        Profile profile = (Profile) session.getAttribute(Constants.PROFILE);
        ServletContext servletContext = session.getServletContext();
        ObjectStore os = (ObjectStore) servletContext.getAttribute(Constants.OBJECTSTORE);
        Model model = (Model) os.getModel();
        PathQuery query = (PathQuery) session.getAttribute(Constants.QUERY);
        ObjectStoreSummary oss = (ObjectStoreSummary) servletContext.
                                               getAttribute(Constants.OBJECT_STORE_SUMMARY);
        Map classKeys = (Map) servletContext.getAttribute(Constants.CLASS_KEYS);
        
        //set up the node on which we are editing constraints
        if (session.getAttribute("editingNode") != null) {

            MainHelper.moveToRequest("editingNode", request);
            PathNode node = (PathNode) request.getAttribute("editingNode");
            MainHelper.moveToRequest("editingConstraintIndex", request);
            MainHelper.moveToRequest("editingTemplateConstraint", request);
            MainHelper.moveToRequest("editingConstraintValue", request);
            MainHelper.moveToRequest("editingConstraintOperand", request);
            
            if (node.getPath().indexOf(".") != -1 && node.isAttribute()) {
                request.setAttribute("displayConstraint", new DisplayConstraint(node, model, oss));
            } else {
                // loop query arguments
                ArrayList paths = new ArrayList();
                Iterator iter = query.getNodes().values().iterator();
                while (iter.hasNext()) {
                    PathNode anode = (PathNode) iter.next();
                    if (anode != node && anode.getType().equals(node.getType())) {
                        paths.add(anode.getPath());
                    }
                }

                Map attributeOps = MainHelper.mapOps(ClassConstraint.VALID_OPS);
                request.setAttribute ("loopQueryOps", attributeOps);
                request.setAttribute ("loopQueryPaths", paths);
            }
            
            // work out the parent class of node if it is a key field or the class
            // of object/reference/collection
            String nodeType;
            boolean useBags;
            if (node.isAttribute() && (node.getPath().indexOf('.')) >= 0) {
                nodeType = ((PathNode) query.getNodes().get(
                        node.getPath().substring(0,
                        node.getPath().lastIndexOf(".")))).getType();
                useBags = ClassKeyHelper.isKeyField(classKeys, nodeType, node
                        .getFieldName());
            } else {
            	if ((node.getPath().indexOf('.')) >= 0) {
            		nodeType = TypeUtil.unqualifiedName(MainHelper.getTypeForPath(
            					node.getPath(), query));
            	}  else {
            		nodeType = node.getType();
            	}
            	useBags = ClassKeyHelper.hasKeyFields(classKeys, nodeType);
            }
            
            if (useBags) {
            	Map bags = profile.getBagsOfType(nodeType, os.getModel());
            	if (!bags.isEmpty()) {
            		request.setAttribute("bagOps", MainHelper.mapOps(BagConstraint.VALID_OPS));
            		request.setAttribute("bags", bags);
            	}
            }
        }
        return null;
    }
}
