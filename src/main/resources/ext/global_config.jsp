<%@page import="com.dotmarketing.util.json.JSONArray"%>
<%@page import="com.dotmarketing.util.UtilMethods"%>
<%@page import="com.aquent.globalconfig.GlobalConfigUtil"%>
<%@page import="com.dotmarketing.util.json.JSONObject"%>
<%@ page import="com.dotmarketing.util.Config" %>

<%@ include file="/html/common/init.jsp" %>
<%@ taglib uri="/WEB-INF/tld/dotmarketing.tld" prefix="dot" %>
<portlet:defineObjects/>
<%@ include file="/html/common/messages_inc.jsp" %>

<div id="agcPropertyList">
	<table class="listingTable shadowBox">
		<tbody>
			<tr>
				<th style="white-space:nowrap;">Aquent Global Config Administration - JSP 1</th>	
			</tr>
			<tr>
				<td class="buttonColumn">

					<button dojoType="dijit.form.Button" onClick="flushCache()" iconClass="resetIcon">
						Flush Cache
					</button>
					
					<button dojoType="dijit.form.Button" onClick="showEditProperty('agc_new')" iconClass="addIcon">
						Add a New Property
					</button>

				</td>
			</tr>
			<tr>
				<th>Current Properties</th>
			</tr>
			<tr>
				<td>
					
					<table class="listingTable shadowBox">
						<tbody>
							<tr>
								<th style="white-space:nowrap;">Key</th>
								<th style="white-space:nowrap;">Value</th>
								<th style="white-space:nowrap;">Action</th>
							</tr>
					
				<% 
					JSONObject data = GlobalConfigUtil.getInstance().getDataFromFile();
					if(UtilMethods.isSet(data) && data.has("props")) {
						JSONArray keys = data.getJSONArray("props");
						for(int i = 0; i<keys.length(); i++) {
							JSONObject jo = keys.getJSONObject(i);
							String key = jo.getString("key");
							String val = jo.getString("value");
				%>			
							<tr id="agc_<%=i%>">
								<td>
									<p id="agc_key_<%=i%>"><%=key%></p>
								</td>
								<td>
									<p id="agc_value_<%=i%>"><%=val%></p>
								</td>
								<td>
									<button dojoType="dijit.form.Button" onClick="showEditProperty('agc_<%=i%>')" iconClass="editIcon">Edit Property</button>
									<button dojoType="dijit.form.Button" onClick="deleteProperty('<%=i%>')" iconClass="deleteIcon">Delete Property</button>
								</td>
							</tr>
					
				<% 		
						}
						
						if(keys.length() == 0) {
				%>		
							<tr>
								<td colspan="3">
									No Properties yet.
								</td>
							</tr>
							
				<% 
						}
					} else {
				%>		
							<tr>
								<td colspan="3">
									No Properties yet.
								</td>
							</tr>
							
				<% 
					} 
				%>
			
						</tbody>
					</table>
					
				</td>
			</tr>
		</tbody>
	</table>
</div>

<div dojoType="dijit.Dialog" id="propertyFormDialog" title="Property Form" style="display:none;height:320px;width:450px;vertical-align: middle;" draggable="true">
	<div style="overflow-y:auto;" dojoType="dijit.layout.ContentPane">
		<div style="padding:0 0 10px 0; border-bottom:1px solid #ccc;">
			<form id="propertyForm" dojoType="dijit.form.Form">
				<input type="hidden" id="agc_orig_key" name="agc_orig_key" dojoType="dijit.form.TextBox" />
				<dl>
					<dt>
						<label for="agc_key">Key:</label>
					</dt>
					<dd style="clear: none;">
						<input type="text" id="agc_key" name="agc_key" required="true" dojoType="dijit.form.ValidationTextBox" invalidMessage="Key is Required." />
					</dd>
					<dt>
						<label for="agc_value">Value:</label>
					</dt>
					<dd style="clear: none;">
						<textarea id="agc_value" name="agc_value" dojoType="dijit.form.Textarea" style="width:250px; min-height:40px;"></textarea>
					</dd>
				</dl>
			</form>
		</div>
		<div class="clear"></div>
		<div class="buttonRow">
			<button dojoType="dijit.form.Button" onclick="editProperty()" type="button" iconClass="saveIcon">Save</button>
			<button dojoType="dijit.form.Button" onclick="dijit.byId('propertyFormDialog').hide()" type="button" iconClass="cancelIcon">Cancel</button>
		</div>
	</div>
</div>

<%
String ref = UtilMethods.encodeURL("/c/portal/layout?${request.getQueryString()}");

String agc_alert       = "";
String agc_alert_class = "";
String agc_status      = "";
String agc_action      = "";
String agc_msg         = "";

if(UtilMethods.isSet(request.getParameter("status"))) {
	agc_status = request.getParameter("status");
}

if(UtilMethods.isSet(request.getParameter("a"))) {
	agc_action = request.getParameter("a");
}

if(UtilMethods.isSet(request.getParameter("msg"))) {
	agc_msg = request.getParameter("msg");
}

if(agc_status.equalsIgnoreCase("success")) {
	agc_alert_class = "resolveIcon";
	if(agc_action.equalsIgnoreCase("flush")) {
		agc_alert = "The cache has been flushed";
	} else if(agc_action.equalsIgnoreCase("edit")) {
		agc_alert = "Properties Updated";
	} else if(agc_action.equalsIgnoreCase("delete")) {
		agc_alert = "Property Deleted";
	}
} else if(agc_status.equalsIgnoreCase("fail")) {
	agc_alert_class = "deleteIcon";
	if(agc_msg.equalsIgnoreCase("no_init")
			|| agc_msg.equalsIgnoreCase("no_action") 
			|| agc_msg.equalsIgnoreCase("inv_action")
			|| agc_msg.equalsIgnoreCase("no_user")) {
		agc_alert = "Something is not quite right, try that again (msg="+agc_msg+")";
	} else if(agc_msg.equalsIgnoreCase("no_admin")) {
		agc_alert = "You must be a CMS Administrator to perform that action";
	} else {
		agc_alert = "Error performing that action";
	}
}
%>

<script>

<% if(UtilMethods.isSet(agc_alert)) { %>
	showDotCMSSystemMessage("<div class=\"messageIcon <%=agc_alert_class%>\"></div><%=agc_alert%>")
<% } %>

	function flushCache() {
		window.location = "/app/globalConfigServlet?a=flush&referer=<%=ref%>}";
	}
	
	function showEditProperty(p) {
		var i = p.replace('agc_', '');
		var ok = '';
		var k = '';
		var v = '';
		if(i !== 'new') {
			ok = dijit.byId('agc_key_'+i).get('value');
			k = dijit.byId('agc_key_'+i).get('value');
			v = dijit.byId('agc_value_'+i).get('value');
		}
		
		alert("Got: "+i+", "+ok+", "+k+", "+v);
		
		dijit.byId('agc_orig_key').set('value', ok);
		dijit.byId('agc_key').set('value', k);
		dijit.byId('agc_value').set('value', v);
		dijit.byId('propertyFormDialog').show()
	}
	
	function editPropery() {
		var ok = dijit.byId('agc_orig_key').get('value');
		var k = dijit.byId('agc_key').get('value');
		var v = dijit.byId('agc_value').get('value');
		
		alert("Got: "+ok+", "+k+", "+v);
		
		window.location = "/app/globalConfigServlet?a=edit&orig_key="+ok+"&key="+k+"&value="+v+"&referer=<%=ref%>";
	}
	
	function deleteProperty(k) {
		window.location = "/app/globalConfigServlet?a=delete&key="+k+"&referer=<%=ref%>";
	}
</script>

											
						