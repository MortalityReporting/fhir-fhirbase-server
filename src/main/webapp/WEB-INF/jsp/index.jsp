<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<html>
<head>
<meta charset="UTF-8">
<title>SMART on FHIR Lite Platform</title>
<%@ include file = "head-scripts.jsp" %>
</head>
<body class='ui-widget'>
	<jsp:include page="header.jsp">
		<jsp:param name="fileName" value="index.jsp" />
	</jsp:include>
	<p>
		<b>This page simulates SMART on FHIR Authorization Flows for the following Application Types</b>
	</p>
	<p>
		<ul>
			<li>Provide: EHR Initiated Launching.</li>
			<li>Patient: Stand-along Launching</li>
			<li>System: Back-end Service</li>
		</ul>
	</p>
	<br />
	<p>Registered Applications:</p>
<% int i = 0; int mod = 0;%>
	<table style="border:0">
	<c:forEach items="${appList.appEntries}" var="appEntry">
		<% 
			mod = i%4;
			if (mod == 0) {
		%>
			<tr>
		<%
			}
		%>
		<td><a style="text-decoration:none" href="${base_url}/smart/app-view/?client_id=${appEntry.appId}"><div style="width:200px; height:200px; background-color:#f0f0f0; line-height: 40px; text-align: center; padding: 5px; margin: 5px;">${appEntry.appName}<br/><br/>Application Type: ${appEntry.appType}</div></a></td>
		<% 
			if (4-mod == 1) {
		%>
			</tr>
		<%
			}
		%>
		<% 
			i++; 
		%>
	</c:forEach>
	
	<%
		mod = i%4;
		if (mod > 0) {
			for (int j=0; j<(4-mod); j++) {
	%>
			<td></td>
	<%
			}
		}
	%>
	</table>
</body>
</html>