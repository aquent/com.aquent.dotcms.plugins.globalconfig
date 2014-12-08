package com.aquent.globalconfig;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
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
	
	private static final String STATUS_SUCCESS = "\"status\" : \"success\"";
	private static final String STATUS_FAIL    = "\"status\" : \"fail\"";
	
	private static final String MSG_NO_INIT    = "\"msg\" : \"no_init\"";
	private static final String MSG_NO_ACTION  = "\"msg\" : \"no_action\"";
	private static final String MSG_INV_ACTION = "\"msg\" : \"inv_action\"";
	private static final String MSG_NO_USER    = "\"msg\" : \"no_user\"";
	private static final String MSG_NO_ADMIN   = "\"msg\" : \"no_admin\"";
	private static final String MSG_NO_KEY     = "\"msg\" : \"no_key\"";
	private static final String MSG_NO_VALUE   = "\"msg\" : \"no_value\"";
	private static final String MSG_FAIL       = "\"msg\" : \"fail\"";
	
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
		
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json");
        ServletOutputStream out = resp.getOutputStream();
		
		// Get the action
		String action = UtilMethods.isSet(req.getParameter("action")) ? req.getParameter("action") : "";
		if(action == "") {
			Logger.error(this, "GlobalConfigServlet Called but no action was specified");
			out.println("{" + STATUS_FAIL +","+ MSG_NO_ACTION + "}");
			return;
		}
		
		// Check to make sure we are inited
		if(!inited) {
			Logger.error(this, "GlobalConfigServlet Called but is not inited");
			out.println("{" + STATUS_FAIL +","+ MSG_NO_INIT + "}");
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
					out.println("{" + STATUS_FAIL +","+ MSG_NO_ADMIN + "}");
					return;
				}
			} catch (Exception e) {
				Logger.error(this, "Exception checking user roles", e);
				out.println("{" + STATUS_FAIL +","+ MSG_NO_ADMIN + "}");
				return;
			}
		} else {
			out.println("{" + STATUS_FAIL +","+ MSG_NO_USER + "}");
			return;
		}
		
		// Do the Action
		String status = STATUS_FAIL +","+ MSG_INV_ACTION;
		if(action.equalsIgnoreCase("flush")) {
			status = flushCache();
		} else if(action.equalsIgnoreCase("edit")) {
			status = editProperty(req);
		} else if(action.equalsIgnoreCase("delete")) {
			status = deleteProperty(req);
		} else {
			Logger.error(this, "GlobalConfigServlet Called with invalid action="+action);
			out.println("{" + STATUS_FAIL +","+ MSG_INV_ACTION + "}");
			return;
		}
		
		Logger.info(this, "Performed action : "+action+", "+status);
		
		out.println("{" + status + "}");
		return;
	}


	private String flushCache() {
		GlobalConfigCacheHandler.INSTANCE.removeAll();
		return STATUS_SUCCESS;
	}

	private String editProperty(HttpServletRequest req) {
		String orig_key = "";
		String key = "";
		String value = "";
		
		if(UtilMethods.isSet(req.getParameter("orig_key"))) orig_key = req.getParameter("orig_key");
		if(UtilMethods.isSet(req.getParameter("key"))) key = req.getParameter("key");
		if(UtilMethods.isSet(req.getParameter("value"))) value = req.getParameter("value");
		
		if(!UtilMethods.isSet("key")) return STATUS_FAIL +", "+ MSG_NO_KEY;
		if(!UtilMethods.isSet("value")) return STATUS_FAIL +", "+ MSG_NO_VALUE;
		
		boolean result = GlobalConfigUtil.getInstance().updateProperty(orig_key, key, value);
		
		if(result) return STATUS_SUCCESS;
		else return STATUS_FAIL +", "+ MSG_FAIL;
	}
	
	private String deleteProperty(HttpServletRequest req) {
		String key = "";
		
		if(UtilMethods.isSet(req.getParameter("key"))) key = req.getParameter("key");
		
		if(!UtilMethods.isSet("value")) return STATUS_FAIL +", "+ MSG_NO_VALUE;
		
		boolean result = GlobalConfigUtil.getInstance().deleteProperty(key);
		
		if(result) return STATUS_SUCCESS;
		else return STATUS_FAIL +", "+ MSG_FAIL;
	}
	
}
