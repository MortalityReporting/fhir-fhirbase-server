<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Application Setting - View</title>
<%@ include file="head-scripts.jsp"%>
</head>
<body class='ui-widget'>
	<%
	String clientId = (String) request.getAttribute("client_id");
	String appName = (String) request.getAttribute("app_name");
	String appType = (String) request.getAttribute("app_type");
	String scope = (String) request.getAttribute("scope");
	String launchUri = (String) request.getAttribute("launch_uri");
	
	if (clientId != null && !clientId.isEmpty() && appName != null && !appName.isEmpty() && appType != null
			&& !appType.isEmpty() && scope != null && !scope.isEmpty()) {
	%>

	<jsp:include page="header.jsp">
		<jsp:param name="file_name" value="app_view.jsp" />
	</jsp:include>
	
	<%		
	if (launchUri != null && !launchUri.isEmpty()) {
	%>
	<form action="${base_url}/smart/app-launch">
		<button class="ui-button" type="submit">Launch Application</button> 
		patient id: <input type="text" name="patient_id"> <input type="hidden" name="client_id" value="${client_id}">
	</form>
	<br />
	<%
	}
	%>
	
	<jsp:include page="app_settings.jsp">
		<jsp:param name="file_name" value="app_view.jsp" />
	</jsp:include>
	<jsp:include page="flow_settings.jsp">
		<jsp:param name="file_name" value="app_view.jsp" />
	</jsp:include>
	<jsp:include page="scope_settings.jsp">
		<jsp:param name="file_name" value="app_view.jsp" />
	</jsp:include>

	<%
		} else {
	%>
	Unauthorized Access
	<%
		}
	%>
</body>
</html>