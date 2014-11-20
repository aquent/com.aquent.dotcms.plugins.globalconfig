package com.aquent.globalconfig;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.RoleAPI;
import com.dotmarketing.business.web.WebAPILocator;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.liferay.portal.model.User;

public class GlobalConfigServlet extends HttpServlet {

	private static final long serialVersionUID = -3872799699991603950L;
	
	private boolean inited = false;
	
	private RoleAPI roleAPI = APILocator.getRoleAPI();
	
	private static final String STATUS_SUCCESS = "s=success";
	private static final String STATUS_FAIL    = "s=fail";
	
	private static final String MSG_NO_INIT    = "&m=no_init";
	private static final String MSG_NO_ACTION  = "&m=no_action";
	private static final String MSG_INV_ACTION = "&m=inv_action";
	private static final String MSG_NO_USER    = "&m=no_user";
	private static final String MSG_NO_ADMIN   = "&m=no_admin";
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		inited = true;
	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Logger.debug(this, "GlobalConfigServlet doPost");
		
		// Get the referer
		String referer = req.getParameter("referer");
		if(referer.indexOf('?') > 0) referer = referer+"&";
		else referer = referer+"?";
		
		// Get the action
		String action = UtilMethods.isSet(req.getParameter("a")) ? req.getParameter("a") : "";
		if(action == "") {
			Logger.error(this, "GlobalConfigServlet Called but no action was specified");
			resp.sendRedirect(referer+STATUS_FAIL+MSG_NO_ACTION);
			return;
		}
		
		// Check to make sure we are inited
		if(!inited) {
			Logger.error(this, "GlobalConfigServlet Called but is not inited");
			resp.sendRedirect(referer+STATUS_FAIL+MSG_NO_INIT+"&a="+action);
			return;
		}
		
		 // Get the back-end user
		User user = null;
		try {
			user = WebAPILocator.getUserWebAPI().getLoggedInUser(req);
		} catch (Exception e) {
			Logger.error(this, "Unable to get back-end user", e);
		}
		
		if(user != null) {
			// Check the user's Role
			try {
				if(!roleAPI.doesUserHaveRole(user, roleAPI.loadCMSAdminRole())) {
					resp.sendRedirect(referer+STATUS_FAIL+MSG_NO_ADMIN+"&a="+action);
					return;
				}
			} catch (Exception e) {
				Logger.error(this, "Exception checking user roles", e);
				resp.sendRedirect(referer+STATUS_FAIL+MSG_NO_ADMIN+"&a="+action);
				return;
			}
		} else {
			resp.sendRedirect(referer+STATUS_FAIL+MSG_NO_USER+"&a="+action);
			return;
		}
		
		// Do the Action
		String status = STATUS_FAIL+MSG_INV_ACTION+"&a="+action;
		if(action.equalsIgnoreCase("flush")) {
			status = flushCache();
		} else if(action.equalsIgnoreCase("edit")) {
			status = editProperty(req);
		} else if(action.equalsIgnoreCase("delete")) {
			status = deleteProperty(req);
		} else {
			Logger.error(this, "GlobalConfigServlet Called with invalid action="+action);
			resp.sendRedirect(referer+STATUS_FAIL+MSG_INV_ACTION+"&a="+action);
			return;
		}
		
		resp.sendRedirect(referer+status+"&a="+action);
		return;
	}


	private String flushCache() {
		GlobalConfigCacheHandler.INSTANCE.removeAll();
		return STATUS_SUCCESS;
	}

	private String editProperty(HttpServletRequest req) {
		// TODO Auto-generated method stub
		return STATUS_SUCCESS;
	}
	
	private String deleteProperty(HttpServletRequest req) {
		// TODO Auto-generated method stub
		return STATUS_SUCCESS;
	}
	
}
