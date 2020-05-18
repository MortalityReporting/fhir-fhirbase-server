<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Authentication</title>
</head>
<body>
<%
	boolean validate = (boolean) request.getAttribute("auth_request_validated");
	if (validate) {
%>
	<p>In real world, user authentication should happen here. We just
		authenticate all as we are simulation authentication. Please just
		click authenticate button</p>
		
		<form action="${base_url}/smart/after-auth" method="post">
			<input type="hidden" name="launch" value="${launch}">
			<input type="hidden" name="response_type" value="${response_type}">
			<input type="hidden" name="client_id" value="${client_id}">
			<input type="hidden" name="redirect_uri" value="${redirect_uri}">
			<input type="hidden" name="scope" value="${scope}">
			<input type="hidden" name="aud" value="${aud}">
			<input type="hidden" name="state" value="${state}">
			<input type="submit" value="Authenticate!!">
		</form>
		
<%  
	} else { 
%>
	<p>Unauthorized Access</p>
<%  } %>
</body>
</html>