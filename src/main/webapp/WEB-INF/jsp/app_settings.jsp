<%	
  String appType = (String) request.getAttribute("app_type");
  boolean disableIt = "app_view.jsp".equalsIgnoreCase(request.getParameter("file_name"))?true:false;
%>

<div>		
  <b>Application Name</b><br />
  <input <%if (disableIt) {out.print("disabled");} %> class="text ui-widget-content ui-corner-all" style="width: 70%;" type="text" name="app_name" value="${app_name}"><br /> <br /> 
			
  <b>Client-id</b><br />
  ${client_id}<input id="client_id_value" type="hidden" name="client_id" value="${client_id}"><br /><br /> 
		
  <b>Application Type</b><br />
  <div>
	  <label for="app_type_radio_provider">Provider
	    <input <%if (disableIt) {out.print("disabled");} %> id="app_type_radio_provider" type="radio" name="app_type" <%if ("Provider".equals(appType)) {out.print("checked=\"checked\"");}%> value="Provider">
	  </label>
			 
	  <label for="app_type_radio_patient">Patient
	    <input <%if (disableIt) {out.print("disabled");} %> id="app_type_radio_patient" type="radio" name="app_type" <%if ("Patient".equals(appType)) {out.print("checked=\"checked\"");}%> value="Patient">
	  </label>
			
	  <label for="app_type_radio_system">System
	    <input <%if (disableIt) {out.print("disabled");} %> id="app_type_radio_system" type="radio" name="app_type"<%if ("System".equals(appType)) {out.print("checked=\"checked\"");}%>  value="System">
	  </label>
  </div>
  <br/><br/>
</div>