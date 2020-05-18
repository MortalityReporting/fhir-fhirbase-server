<%
	boolean disableIt = "app_view.jsp".equalsIgnoreCase(request.getParameter("file_name"))?true:false;
%>
<div id="non_system_data">
	<b>SMART Launch URI</b><br /> 
	&nbsp;&nbsp;<input <%if (disableIt) {out.print("disabled");} %> class="text ui-widget-content ui-corner-all" style="width: 70%;" type="text" name="launch_uri" value="${launch_uri}"><br /> <br /> 
	<b>Redirect URI</b><br /> 
	&nbsp;&nbsp;<input <%if (disableIt) {out.print("disabled");} %> class="text ui-widget-content ui-corner-all" style="width: 70%;" type="text" name="redirect_uri" value="${redirect_uri}"><br /> <br /> 
	<b>Standard Scopes</b> (user and patient scope only)<br /> 
	&nbsp;&nbsp;launch profile openid online_access launch/patient(only for patient type)<br /> <br />
</div>
<div id="system_data">
	<b>Public Key</b><br /> 
	&nbsp;&nbsp;<textarea <%if (disableIt) {out.print("disabled");} %> class="text ui-widget-content ui-corner-all" rows="7" cols="100" id="public_key" name="public_key">${public_key}</textarea><br /> <br /> 
	<b>Key ID (JWK's kid)</b><br /> 
	&nbsp;&nbsp;<input <%if (disableIt) {out.print("disabled");} %> class="text ui-widget-content ui-corner-all" id="kid" name="kid" value="${kid}"><br /> <br /> 
	<b>Key Type (JWK's kty)</b><br /> 
	&nbsp;&nbsp;<input <%if (disableIt) {out.print("disabled");} %> class="text ui-widget-content ui-corner-all" id="kty" name="kty" value="${kty}"><br /> <br /> 
	<b>Expiration (JWK's exp)</b><br /> 
	&nbsp;&nbsp;<input <%if (disableIt) {out.print("disabled");} %> class="text ui-widget-content ui-corner-all" id="exp" name="exp" value="${exp}"><br /> <br /> 
	<b>JWK Raw Data (if available)</b><br /> 
	&nbsp;&nbsp;<textarea <%if (disableIt) {out.print("disabled");} %> class="text ui-widget-content ui-corner-all" rows="7" cols="100" id="jwk_raw" name="jwk_raw">${jwk_raw}</textarea><br /> <br /> 
</div>