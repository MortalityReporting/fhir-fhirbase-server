<script>
	function app_type_visual() {
		var appTypeString = $("input[name='app_type']:checked").val();
		if (appTypeString == "System") {
			// hide patient_scope_head and patient_scope_content
			$("#patient_scope_head").hide();
			$("#patient_scope_content").hide();
			$("#system_data").show();
			$("#non_system_data").hide();
			$("#user_or_system_scope_head").html("System Scope");
		} else {
			$("#patient_scope_head").show();
			$("#patient_scope_content").show();
			$("#system_data").hide();
			$("#non_system_data").show();
			$("#user_or_system_scope_head").html("User Scope");
		}
	}
	
	$(document).ready(function() {
		$("#app-save-div").click(function(e) {
			e.preventDefault();
			$("#app-update").submit();
		});

		$("#app-create-div").click(function(e) {
			e.preventDefault();
			$("#app-create").submit();
		});

		$("#app-new-div").click(function(e) {
			e.preventDefault();
			window.location="${base_url}/smart/app-create/";
		});
		
		$("#app-edit-div").click(function(e) {
			e.preventDefault();
			window.location="${base_url}/smart/app-edit/?client_id=${client_id}";
		});
		
		$("#about-div").click(function(e) {
			e.preventDefault();
			$( "#dialog" ).dialog();
		});

		$("#app-delete-div").click(function(e) {
			e.preventDefault();
			window.location="${base_url}/smart/app-delete/?client_id=${client_id}";
		});

		$("input[name='app_type']").change(function() {
			app_type_visual();
		});
		
		$("#copy_client_id").click(function() {
 			$("#client_id_value").focus();
			$("#client_id_value").select();
			$(document).execCommand("copy");
			alert($("#client_id_value").val());
 		});


		app_type_visual();
	});
</script>

<ul id="menu">
	<li><div>
			<a href="${base_url}/smart/" style="text-decoration: none">Home</a>
		</div></li>
	<li><div>Application</div>
		<ul>
			<li><div id="app-new-div">New</div></li>
			<%
				if ("app_edit.jsp".equalsIgnoreCase(request.getParameter("file_name"))) {
			%>
			<li><div id="app-save-div">Save</div></li>
			<%
				} else {
					if ("app_create.jsp".equalsIgnoreCase(request.getParameter("file_name"))) {
			%>
			<li><div id="app-create-div">Save</div></li>
			<%
					} else {
			%>
			<li class="ui-state-disabled"><div>Save</div></li>
			<%
					}
				}
				if ("app_view.jsp".equalsIgnoreCase(request.getParameter("file_name"))) {
			%>
			<li><div id="app-edit-div">Edit</div></li>
			<%
				} else {
			%>
			<li class="ui-state-disabled"><div>Edit</div></li>
			<%
				}
			if ("app_view.jsp".equalsIgnoreCase(request.getParameter("file_name"))
					|| "app_edit.jsp".equalsIgnoreCase(request.getParameter("file_name"))) {
			%>
			<li><div id="app-delete-div">Delete</div></li>
			<%
				} else {
			%>
			<li class="ui-state-disabled"><div>Delete</div></li>
			<%
				}
			%>
		</ul></li>
	<li><div id="about-div">About</div></li>
</ul>

<div id="dialog" style="display: none;" title="About">
  <p>This page is to provide SMART on FHIR authorization flows. Currently, it supports
  provider (EHR-initiated), patient (stand-alone), and system (backend service).</p> 
  <p>Please use <a style="outline:none;" href="https://github.com/omoponfhir/omoponfhir-main/issues">this issue link</a> for any issues or needs.</p>
</div>

<br/>
<br/>