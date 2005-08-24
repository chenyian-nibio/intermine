package org.intermine.web.history;

/*
 * Copyright (C) 2002-2005 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.intermine.web.Constants;
import org.intermine.web.ForwardParameters;
import org.intermine.web.InterMineDispatchAction;
import org.intermine.web.Profile;
import org.intermine.web.SaveQueryHelper;
import org.intermine.web.SavedQuery;
import org.intermine.web.SessionMethods;

import org.apache.log4j.Logger;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Implementation of <strong>Action</strong> that modifies a saved query or bag.
 *
 * @author Mark Woodbridge
 */
public class ModifyQueryChangeAction extends InterMineDispatchAction
{
    private static final Logger LOG = Logger.getLogger(ModifyQueryChangeAction.class);
    
    /**
     * Load a query or bag.
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     * @exception Exception if the application business logic throws
     *  an exception
     */
    public ActionForward load(ActionMapping mapping,
                              ActionForm form,
                              HttpServletRequest request,
                              HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();
        ServletContext servletContext = session.getServletContext();
        Profile profile = (Profile) session.getAttribute(Constants.PROFILE);
        String queryName = request.getParameter("name");
        SavedQuery sq;
        
        if (request.getParameter("type").equals("history")) {
            sq = (SavedQuery) profile.getHistory().get(queryName);
        } else if (request.getParameter("type").equals("bag")) {
            return new ForwardParameters(mapping.findForward("bagDetails"))
                .addParameter("bagName", queryName).forward();
        } else {
            sq = (SavedQuery) profile.getSavedQueries().get(queryName);
        }
        
        SessionMethods.loadQuery(sq.getPathQuery(), session);
        return mapping.findForward("query");
    }
    
    /**
     * Save a query from the history.
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     * @exception Exception if the application business logic throws
     *  an exception
     */
    public ActionForward save(ActionMapping mapping,
                              ActionForm form,
                              HttpServletRequest request,
                              HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();
        ServletContext servletContext = session.getServletContext();
        Profile profile = (Profile) session.getAttribute(Constants.PROFILE);
        String queryName = request.getParameter("name");
        SavedQuery sq = (SavedQuery) profile.getHistory().get(queryName);
        sq = SessionMethods.saveQuery(session,
                SaveQueryHelper.findNewQueryName(profile.getSavedQueries(), queryName),
                sq.getPathQuery(), sq.getDateCreated());
        return new ForwardParameters(mapping.findForward("history")).addParameter("action", "rename")
            .addParameter("type", "saved").addParameter("name", sq.getName()).forward();
    }
}