package org.intermine.web.results;

/*
 * Copyright (C) 2002-2004 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.intermine.web.Constants;
import org.intermine.web.InterMineBag;
import org.intermine.web.BagHelper;

/**
 * Implementation of <strong>LookupDispatchAction</strong>. Changes the
 * size of the results displayed.
 *
 * @author Andrew Varley
 */
public class ChangeResultsSizeAction extends Action
{
    /**
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     * @exception Exception if the application business logic throws
     *  an exception
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws Exception {
        ActionForward forward = null;

        ChangeResultsForm crsForm = (ChangeResultsForm) form;
        String button = crsForm.getButton();

        if ("changePageSize".equals(button)) {
            return changePageSize(mapping, form, request, response);
        } else if ("saveNewBag".equals(button)) {
            return saveNewBag(mapping, form, request, response);
        } else if ("addToExistingBag".equals(button)) {
            return addToExistingBag(mapping, form, request, response);
        }

        return null;
    }

    /**
     * Change the page size of the PagedTable
     *
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     *
     * @exception ServletException if a servlet error occurs
     */
    public ActionForward changePageSize(ActionMapping mapping, ActionForm form,
                                        HttpServletRequest request, HttpServletResponse response)
        throws ServletException {
        HttpSession session = request.getSession();

        PagedTable pt = (PagedTable) session.getAttribute(Constants.RESULTS_TABLE);
        ChangeResultsForm changeResultsForm = (ChangeResultsForm) form;

        pt.setPageSize(Integer.parseInt(changeResultsForm.getPageSize()));

        // Need to set the start so that we are on the page containing the current start item
        pt.setStart((pt.getStart() / pt.getPageSize()) * pt.getPageSize());

        return mapping.findForward("results");
    }

    /**
     * Save a new bag of objects
     *
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     *
     * @exception ServletException if a servlet error occurs
     */
    public ActionForward saveNewBag(ActionMapping mapping, ActionForm form,
                                    HttpServletRequest request, HttpServletResponse response)
        throws ServletException {
        saveBag(((ChangeResultsForm) form).getNewBagName(), form, request);
        return mapping.findForward("results");
    }

    /**
     * Add to existing bag of objects
     *
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     *
     * @exception ServletException if a servlet error occurs
     */
    public ActionForward addToExistingBag(ActionMapping mapping, ActionForm form,
                                          HttpServletRequest request, HttpServletResponse response)
        throws ServletException {
        saveBag(((ChangeResultsForm) form).getBagName(), form, request);
        return mapping.findForward("results");
    }

    /**
     * Save the selected objects to a bag on the session
     *
     * @param bagName the bag to save to
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     *
     * @exception ServletException if a servlet error occurs
     */
    public void saveBag(String bagName, ActionForm form, HttpServletRequest request)
        throws ServletException {
        ChangeResultsForm changeResultsForm = (ChangeResultsForm) form;

        HttpSession session = request.getSession();

        PagedTable pt = (PagedTable) session.getAttribute(Constants.RESULTS_TABLE);
        String[] selectedObjects = changeResultsForm.getSelectedObjects();

        InterMineBag bag = new InterMineBag();

        // Go through the selected items and add to the set
        for (Iterator itemIterator = Arrays.asList(selectedObjects).iterator();
             itemIterator.hasNext();) {
            String selectedObject = (String) itemIterator.next();
            // selectedObject is of the form "column,row" or "column"
            int commaIndex = selectedObject.indexOf(",");
            if (commaIndex == -1) {
                int column = Integer.parseInt(selectedObject);

                for (Iterator rowIterator = pt.getList().iterator();
                     rowIterator.hasNext();) {
                    List thisRow = (List) rowIterator.next();
                    bag.add(thisRow.get(column));
                }
            } else {
                // use the column,row to pick out the object from PagedTable
                int column = Integer.parseInt(selectedObject.substring(0, commaIndex));
                int row = Integer.parseInt(selectedObject.substring(commaIndex + 1));
                bag.add(((List) pt.getList().get(row)).get(column));
            }
        }

        BagHelper.saveBag(request, bagName, bag);
    }

    /**
     * Distributes the actions to the necessary methods, by providing a Map from action to
     * the name of a method.
     *
     * @return a Map
     */
    protected Map getKeyMethodMap() {
        Map map = new HashMap();
        map.put("button.change", "changePageSize");
        map.put("bag.new", "saveNewBag");
        map.put("bag.existing", "addToExistingBag");
        return map;
    }
}
