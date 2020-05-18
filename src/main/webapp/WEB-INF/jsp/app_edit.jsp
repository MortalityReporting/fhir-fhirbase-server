<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Application Setting - Edit</title>
<%@ include file="head-scripts.jsp"%>
</head>
<body class='ui-widget'>
	<%
	String clientId = (String) request.getAttribute("client_id");
	String scope = (String) request.getAttribute("scope");
	if (clientId != null && !clientId.isEmpty()) {
	%>
	<jsp:include page="header.jsp">
		<jsp:param name="file_name" value="app_edit.jsp" />
	</jsp:include>

	<div style="padding: 10px;">
	<form id="app-update" action="${base_url}/smart/app-update">
		<jsp:include page="app_settings.jsp">
			<jsp:param name="file_name" value="app_edit.jsp" />
		</jsp:include>
		<jsp:include page="flow_settings.jsp">
			<jsp:param name="file_name" value="app_edit.jsp" />
		</jsp:include>
		<jsp:include page="scope_settings.jsp">
			<jsp:param name="file_name" value="app_edit.jsp" />
		</jsp:include>
	</form>
	</div>
	<%
		} else {
	%>
	Unauthorized Access
	<%
		}
	%>
</body>
</html>