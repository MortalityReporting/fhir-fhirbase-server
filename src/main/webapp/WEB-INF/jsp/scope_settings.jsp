
<%
String clientId = (String) request.getAttribute("client_id");
String appType = (String) request.getAttribute("app_type");
String scope = (String) request.getAttribute("scope");
String fileName = (String) request.getAttribute("file_name");
boolean disableIt = "app_view.jsp".equalsIgnoreCase(request.getParameter("file_name"))?true:false;
%>

<script>
var userReadCheckBoxIds = ["#user_condition_r", "#user_documentreference_r", "#user_encounter_r", "#user_medicationstatement_r", "#user_medicationrequest_r", "#user_observation_r", "#user_patient_r", "#user_procedure_r"];
var userWriteCheckBoxIds = ["#user_condition_w", "#user_documentreference_w", "#user_encounter_w", "#user_medicationstatement_w", "#user_medicationrequest_w", "#user_observation_w", "#user_patient_w", "#user_procedure_w"];
var patientReadCheckBoxIds = ["#patient_condition_r", "#patient_documentreference_r", "#patient_encounter_r", "#patient_medicationstatement_r", "#patient_medicationrequest_r", "#patient_observation_r", "#patient_patient_r", "#patient_procedure_r"];
var patientWriteCheckBoxIds = ["#patient_condition_w", "#patient_documentreference_w", "#patient_encounter_w", "#patient_medicationstatement_w", "#patient_medicationrequest_w", "#patient_observation_w", "#patient_patient_w", "#patient_procedure_w"];
var resources = ["Condition", "DocumentReference", "Encounter", "MedicationStatement", "MedicationRequest", "Observation", "Patient", "Procedure"];


function remove_user_all_from_selected () {
	var scopes = $("#selected_scopes").val();
	
	scopes = scopes.replace(/system\/([a-zA-Z]+|\*)\.(read|write|\*)/g, "");		
	scopes = scopes.replace(/user\/([a-zA-Z]+|\*)\.(read|write|\*)/g, "");
	$("#selected_scopes").val(scopes.replace(/\s+/g,' ').trim());
}

function update_user_r () {
	var typeName = "user";	
	
	if ($("#app_type_radio_system").is(":checked")) {
		typeName = "system";
	}
	
	var scopes = "";
	
	if ($("#all_user_r").is(":checked")) {
		if ($("#all_user_w").is(":checked")) {
			scopes = scopes.replace(typeName+"/*.write", "");
			scopes = scopes.replace(typeName+"/*.read", "");
			scopes = scopes.replace(typeName+"/*.*", "");
			scopes += " "+typeName+"/*.*";
		} else {
			scopes += " "+typeName+"/*.read";
		}
		return scopes;
	}
	
	if ($("#user_condition_r").is(":checked")) {
		$("#user_condition_r").prop("checked", true);
		scopes += " "+typeName+"/Condition.read";
	} else {
		$("#user_condition_r").prop("checked", false); 
	}
	if ($("#user_documentreference_r").is(":checked")) {
		$("#user_documentreference_r").prop("checked", true); 
		scopes += " "+typeName+"/DocumentReference.read";
	} else {
		$("#user_documentreference_r").prop("checked", false); 
	}
	if ($("#user_encounter_r").is(":checked")) {
		$("#user_encounter_r").prop("checked", true); 
		scopes += " "+typeName+"/Encounter.read";
	} else {
		$("#user_encounter_r").prop("checked", false); 
	}
	if ($("#user_medicationstatement_r").is(":checked")) {
		$("#user_medicationstatement_r").prop("checked", true); 
		scopes += " "+typeName+"/MedicationStatement.read";
	} else {
		$("#user_medicationstatement_r").prop("checked", false); 
	}
	if ($("#user_medicationrequest_r").is(":checked")) {
		$("#user_medicationrequest_r").prop("checked", true); 
		scopes += " "+typeName+"/MedicationRequest.read";
	} else {
		$("#user_medicationrequest_r").prop("checked", false); 
	}
	if ($("#user_observation_r").is(":checked")) {
		$("#user_observation_r").prop("checked", true); 
		scopes += " "+typeName+"/Observation.read";
	} else {
		$("#user_observation_r").prop("checked", false); 
	}
	if ($("#user_patient_r").is(":checked")) {
		$("#user_patient_r").prop("checked", true); 
		scopes += " "+typeName+"/Patient.read";
	} else {
		$("#user_patient_r").prop("checked", false); 
	}
	if ($("#user_procedure_r").is(":checked")) {
		$("#user_procedure_r").prop("checked", true); 
		scopes += " "+typeName+"/Procedure.read";
	} else {
		$("#user_procedure_r").prop("checked", false); 
	}
	
	return scopes;
}

function update_user_w (scopes) {
	var typeName = "user";
	if ($("#app_type_radio_system").is(":checked")) {
		typeName = "system";
	}

	if ($("#all_user_w").is(":checked")) {
		if ($("#all_user_r").is(":checked")) {
			scopes = scopes.replace(typeName+"/*.read", "");
			scopes = scopes.replace(typeName+"/*.write", "");
			scopes = scopes.replace(typeName+"/*.*", "");
			scopes += " "+typeName+"/*.*";
		} else {
			scopes += " "+typeName+"/*.write";
		}
		return scopes;
	}

	if ($("#user_condition_w").is(":checked")) {
		$("#user_condition_w").prop("checked", true);
		scopes += " "+typeName+"/Condition.write";
	} else {
		$("#user_condition_w").prop("checked", false); 
	}
	if ($("#user_documentreference_w").is(":checked")) {
		$("#user_documentreference_w").prop("checked", true); 
		scopes += " "+typeName+"/DocumentReference.write";
	} else {
		$("#user_documentreference_w").prop("checked", false); 
	}
	if ($("#user_encounter_w").is(":checked")) {
		$("#user_encounter_w").prop("checked", true); 
		scopes += " "+typeName+"/Encounter.write";
	} else {
		$("#user_encounter_w").prop("checked", false); 
	}
	if ($("#user_medicationstatement_w").is(":checked")) {
		$("#user_medicationstatement_w").prop("checked", true); 
		scopes += " "+typeName+"/MedicationStatement.write";
	} else {
		$("#user_medicationstatement_w").prop("checked", false); 
	}
	if ($("#user_medicationrequest_w").is(":checked")) {
		$("#user_medicationrequest_w").prop("checked", true); 
		scopes += " "+typeName+"/MedicationRequest.write";
	} else {
		$("#user_medicationrequest_w").prop("checked", false); 
	}
	if ($("#user_observation_w").is(":checked")) {
		$("#user_observation_w").prop("checked", true); 
		scopes += " "+typeName+"/Observation.write";
	} else {
		$("#user_observation_w").prop("checked", false); 
	}
	if ($("#user_patient_w").is(":checked")) {
		$("#user_patient_w").prop("checked", true); 
		scopes += " "+typeName+"/Patient.write";
	} else {
		$("#user_patient_w").prop("checked", false); 
	}
	if ($("#user_procedure_w").is(":checked")) {
		$("#user_procedure_w").prop("checked", true); 
		scopes += " "+typeName+"/Procedure.write";
	} else {
		$("#user_procedure_w").prop("checked", false); 
	}
	
	return scopes;
}

// for patient scope
function remove_patient_all_from_selected () {
	var scopes = $("#selected_scopes").val();
	scopes = scopes.replace(/patient\/([a-zA-Z]+|\*)\.(read|write|\*)/g, "");
	$("#selected_scopes").val(scopes.replace(/\s+/g,' ').trim());
}

function update_patient_r (scopes) {
	if ($("#all_patient_r").is(":checked")) {
		scopes += " patient/*.read";
		return scopes;
	}
	
	if ($("#patient_condition_r").is(":checked")) {
		$("#patient_condition_r").prop("checked", true);
		scopes += " patient/Condition.read";
	} else {
		$("#patient_condition_r").prop("checked", false); 
	}
	if ($("#patient_documentreference_r").is(":checked")) {
		$("#patient_documentreference_r").prop("checked", true); 
		scopes += " patient/DocumentReference.read";
	} else {
		$("#patient_documentreference_r").prop("checked", false); 
	}
	if ($("#patient_encounter_r").is(":checked")) {
		$("#patient_encounter_r").prop("checked", true); 
		scopes += " patient/Encounter.read";
	} else {
		$("#patient_encounter_r").prop("checked", false); 
	}
	if ($("#patient_medicationstatement_r").is(":checked")) {
		$("#patient_medicationstatement_r").prop("checked", true); 
		scopes += " patient/MedicationStatement.read";
	} else {
		$("#patient_medicationstatement_r").prop("checked", false); 
	}
	if ($("#patient_medicationrequest_r").is(":checked")) {
		$("#patient_medicationrequest_r").prop("checked", true); 
		scopes += " patient/MedicationRequest.read";
	} else {
		$("#patient_medicationrequest_r").prop("checked", false); 
	}
	if ($("#patient_observation_r").is(":checked")) {
		$("#patient_observation_r").prop("checked", true); 
		scopes += " patient/Observation.read";
	} else {
		$("#patient_observation_r").prop("checked", false); 
	}
	if ($("#patient_patient_r").is(":checked")) {
		$("#patient_patient_r").prop("checked", true); 
		scopes += " patient/Patient.read";
	} else {
		$("#patient_patient_r").prop("checked", false); 
	}
	if ($("#patient_procedure_r").is(":checked")) {
		$("#patient_procedure_r").prop("checked", true); 
		scopes += " patient/Procedure.read";
	} else {
		$("#patient_procedure_r").prop("checked", false); 
	}
	
	return scopes;
}

function update_patient_w (scopes) {
	if ($("#all_patient_w").is(":checked")) {
		scopes += " patient/*.write";
		return scopes;
	}

	if ($("#patient_condition_w").is(":checked")) {
		$("#patient_condition_w").prop("checked", true);
		scopes += " patient/Condition.write";
	} else {
		$("#patient_condition_w").prop("checked", false); 
	}
	if ($("#patient_documentreference_w").is(":checked")) {
		$("#patient_documentreference_w").prop("checked", true); 
		scopes += " patient/DocumentReference.write";
	} else {
		$("#patient_documentreference_w").prop("checked", false); 
	}
	if ($("#patient_encounter_w").is(":checked")) {
		$("#patient_encounter_w").prop("checked", true); 
		scopes += " patient/Encounter.write";
	} else {
		$("#patient_encounter_w").prop("checked", false); 
	}
	if ($("#patient_medicationstatement_w").is(":checked")) {
		$("#patient_medicationstatement_w").prop("checked", true); 
		scopes += " patient/MedicationStatement.write";
	} else {
		$("#patient_medicationstatement_w").prop("checked", false); 
	}
	if ($("#patient_medicationrequest_w").is(":checked")) {
		$("#patient_medicationrequest_w").prop("checked", true); 
		scopes += " patient/MedicationRequest.write";
	} else {
		$("#patient_medicationrequest_w").prop("checked", false); 
	}
	if ($("#patient_observation_w").is(":checked")) {
		$("#patient_observation_w").prop("checked", true); 
		scopes += " patient/Observation.write";
	} else {
		$("#patient_observation_w").prop("checked", false); 
	}
	if ($("#patient_patient_w").is(":checked")) {
		$("#patient_patient_w").prop("checked", true); 
		scopes += " patient/Patient.write";
	} else {
		$("#patient_patient_w").prop("checked", false); 
	}
	if ($("#patient_procedure_w").is(":checked")) {
		$("#patient_procedure_w").prop("checked", true); 
		scopes += " patient/Procedure.write";
	} else {
		$("#patient_procedure_w").prop("checked", false); 
	}
	
	return scopes;
}

function reset_scopes() {
	var scopes = "";
	$("#selected_scopes").val(scopes);
	
	scopes = update_user_r(scopes);
	scopes = update_user_w(scopes);
	scopes = update_patient_r(scopes);
	scopes = update_patient_w(scopes);
	
	$("#selected_scopes").val(scopes.replace(/\s+/g,' ').trim());	
}

function select_all_user_r_table() {
	$("#all_user_r").prop("checked", true);	
	
	for (id of userReadCheckBoxIds) {
		$(id).prop("checked", true);		
	}
}

function deselect_all_user_r_table() {
	$("#all_user_r").prop("checked", false);	

	for (id of userReadCheckBoxIds) {
		$(id).prop("checked", false);		
	}
}

function select_all_user_w_table() {
	$("#all_user_w").prop("checked", true);

	for (id of userWriteCheckBoxIds) {
		$(id).prop("checked", true);		
	}
}

function deselect_all_user_w_table() {
	$("#all_user_w").prop("checked", false);

	for (id of userWriteCheckBoxIds) {
		$(id).prop("checked", false);		
	}
}

function select_all_patient_r_table() {
	$("#all_patient_r").prop("checked", true);	
	
	for (id of patientReadCheckBoxIds) {
		$(id).prop("checked", true);		
	}
}

function deselect_all_patient_r_table() {
	$("#all_patient_r").prop("checked", false);	
	
	for (id of patientReadCheckBoxIds) {
		$(id).prop("checked", false);		
	}
}

function select_all_patient_w_table() {
	$("#all_patient_w").prop("checked", true);

	for (id of patientWriteCheckBoxIds) {
		$(id).prop("checked", true);		
	}
}

function deselect_all_patient_w_table() {
	$("#all_patient_w").prop("checked", false);
	
	for (id of patientWriteCheckBoxIds) {
		$(id).prop("checked", false);		
	}
}

function update_scope_table_from_scopes(scopes) {
	var user_r_done = false;
	var user_w_done = false;
	var patient_r_done = false;
	var patient_w_done = false;
	
	var typeName = "user";
	
	if ($("#app_type_radio_system").is(":checked")) {
		typeName = "system";
	}

	if (scopes.includes(typeName+"/*.*")) {
		select_all_user_r_table();
		select_all_user_w_table();
		user_r_done = true;
		user_w_done = true;
	}

	if (scopes.includes("patient/*.*")) {
		select_all_patient_r_table();
		select_all_patient_w_table();
		patient_r_done = true;
		patient_w_done = true;
	}

	if (user_r_done && user_w_done && patient_r_done && patient_w_done) {
		// all are selected. return
		return;
	}

	if (scopes.includes(typeName+"/*.read") && user_r_done == false) {
		select_all_user_r_table();
		user_r_done = true;
	}

	if (scopes.includes(typeName+"/*.write") && user_w_done == false) {
		select_all_user_w_table();
		user_w_done = true;
	}


	if (scopes.includes("patient/*.read") && patient_r_done == false) {
		select_all_patient_r_table();
		patient_r_done = true;
	}

	if (scopes.includes("patient/*.write") && patient_w_done == false) {
		select_all_patient_w_table();
		patient_w_done = true;
	}

	
	for (i = 0; i < resources.length; i++) {
		if (user_r_done == false) {
			if (scopes.includes(typeName+"/"+resources[i]+".read")
					|| scopes.includes(typeName+"/"+resources[i]+".*")) {
				$(userReadCheckBoxIds[i]).prop("checked", true);
			}
		}
		
		if (user_w_done == false) {
			if (scopes.includes(typeName+"/"+resources[i]+".write")
					|| scopes.includes(typeName+"/"+resources[i]+".*")) {
				$(userWriteCheckBoxIds[i]).prop("checked", true);
			}
		}
		
		if (patient_r_done == false) {
			if (scopes.includes("patient/"+resources[i]+".read")
					|| scopes.includes("patienbt/"+resources[i]+".*")) {
				$(patientReadCheckBoxIds[i]).prop("checked", true);
			}
		}
		
		if (patient_w_done == false) {
			if (scopes.includes("patient/"+resources[i]+".write")
					|| scopes.includes("patienbt/"+resources[i]+".*")) {
				$(patientWriteCheckBoxIds[i]).prop("checked", true);
			}
		}
	}
}

function update_selected_scopes() {
	// update selected_scopes from table.

	var typeName = "user";
	
	if ($("#app_type_radio_system").is(":checked")) {
		typeName = "system";
	}

	var user_r_scopes_done = false;
	var user_w_scopes_done = false;
	var patient_r_scopes_done = false;
	var patient_w_scopes_done = false;

	// update selected_scopes from table.
	var scopes = "";
	if ($("#all_user_r").is(":checked") && $("#all_user_w").is(":checked")) {
		scopes = typeName + "/*.* ";
		user_r_scopes_done = true;
		user_w_scopes_done = true;
	} else {
		if ($("#all_user_r").is(":checked")) {
			scopes = typeName + "/*.read ";
			user_r_scopes_done = true;
		} else if ($("#all_user_w").is(":checked")) {
			scopes = typeName + "/*.write ";
			user_w_scopes_done = true;
		}
	}
	
	if ($("#all_patient_r").is(":checked") && $("#all_patient_w").is(":checked")) {
		scopes += "patient/*.* ";
		patient_r_scopes_done = true;
		patient_w_scopes_done = true;
	} else {
		if ($("#all_patient_r").is(":checked")) {
			scopes += "patient/*.read ";
			patient_r_scopes_done = true;
		} else if ($("#all_patient_w").is(":checked")) {
			scopes += "patient/*.write ";
			patient_w_scopes_done = true;
		}
	}
	
	if (!user_r_scopes_done || !user_w_scopes_done || !patient_r_scopes_done || !patient_w_scopes_done) {
		for (i = 0; i < userReadCheckBoxIds.length; i++) {
			if (!user_r_scopes_done) {
				if ($(userReadCheckBoxIds[i]).is(":checked")) {
					scopes += typeName+"/"+resources[i]+".read "
				}
			}
			
			if (!user_w_scopes_done) {
				if ($(userWriteCheckBoxIds[i]).is(":checked")) {
					scopes += typeName+"/"+resources[i]+".write "
				}				
			}
			
			if (!patient_r_scopes_done) {
				if ($(patientReadCheckBoxIds[i]).is(":checked")) {
					scopes += "patient/"+resources[i]+".read "
				}				
			}
			
			if (!patient_w_scopes_done) {
				if ($(patientWriteCheckBoxIds[i]).is(":checked")) {
					scopes += "patient/"+resources[i]+".write "
				}				
			}
		}		
	}
	
	if ("system" == typeName) {
		// make sure we do not have patient and user changed to system.
		scopes = scopes.replace(/patient\/([a-zA-Z]+|\*)\.(read|write|\*)/g, "");
		scopes = scopes.replace("user", "system");
	}
	
	$("#selected_scopes").val(scopes.replace(/\s+/g,' ').trim());

}

function set_all_if_all_selected(ids, allId) {
	for (id of ids) {
		if ($(id).is(":checked") == false) {
			return;
		}
	}
	
	$(allId).prop("checked", true);
}

$(document).ready(function() {
	$( function() {
	    $( "input[type='checkbox']" ).checkboxradio({
	      icon: false
	    });
	});
	
	$( function() {
	    $( "input[type='radio']" ).checkboxradio({
	      icon: false
	    });
	});

	// update scope tables with scope parameter passed from storage
	update_scope_table_from_scopes("<%=scope%>");
	
	// selected_scopes are always updated from tables.
	update_selected_scopes();
	
	// follows are for app type changes.
	$("input[id^='app_type_radio_']").change(function() {
		update_selected_scopes();
		
		if ($("#app_type_radio_system").is(":checked")) {
			var scopes = $("#selected_scopes").val();
			scopes = scopes.replace(/patient\/([a-zA-Z]+|\*)\.(read|write|\*)/g, "");
			scopes = scopes.replace("user", "system");
			$("#selected_scopes").val(scopes.replace(/\s+/g,' ').trim());
		} else {
			var scopes = $("#selected_scopes").val();
			scopes = scopes.replace(/system\/([a-zA-Z]+|\*)\.(read|write|\*)/g, "");
			$("#selected_scopes").val(scopes.replace(/\s+/g,' ').trim());
		}
	});
	
	$("#all_user_r").change(function() {
		var typeName = "user";
		if ($("#app_type_radio_system").is(":checked")) {
			typeName = "system";
		}

		if ($("#all_user_r").is(":checked")) {
			select_all_user_r_table();
		} else {
			deselect_all_user_r_table()
		}
		
		update_selected_scopes();
		
		$("input[type='checkbox']").checkboxradio("refresh");
	});

	$("#all_user_w").change(function() {
		var typeName = "user";
		if ($("#app_type_radio_system").is(":checked")) {
			typeName = "system";
		}

		if ($("#all_user_w").is(":checked")) {
			select_all_user_w_table();
		} else {
			deselect_all_user_w_table();
		}
		
		update_selected_scopes();

		$("input[type='checkbox']").checkboxradio("refresh");
	});
	
	$("input[id^='user_']").change(function() {
		myId = $(this).attr('id');
		if ($(this).is(":checked") == true) {
			if (myId.endsWith("_r")) {
				set_all_if_all_selected(userReadCheckBoxIds, "#all_user_r");
			} else {
				set_all_if_all_selected(userWriteCheckBoxIds, "#all_user_w");
			}
		} else {
			if (myId.endsWith("_r")) {
				$("#all_user_r").prop("checked", false);
			} else {
				$("#all_user_w").prop("checked", false);
			}
		}

		update_selected_scopes();

		$("input[type='checkbox']").checkboxradio("refresh");
	});
	
	// for patient scopes
	$("#all_patient_r").change(function() {
		if ($("#all_patient_r").is(":checked")) {
			select_all_patient_r_table();
		} else {
			deselect_all_patient_r_table();
		}
		
		update_selected_scopes();
		
		$("input[type='checkbox']").checkboxradio("refresh");
	});

	$("#all_patient_w").change(function() {
		if ($("#all_patient_w").is(":checked")) {
			select_all_patient_w_table()
		} else {
			deselect_all_patient_w_table();
		}
		
		update_selected_scopes()
		
		$("input[type='checkbox']").checkboxradio("refresh");
	});
	
	$("input[id^='patient_']").change(function() {
		myId = $(this).attr('id');
		if ($(this).is(":checked") == true) {
			if (myId.endsWith("_r")) {
				set_all_if_all_selected(patientReadCheckBoxIds, "#all_patient_r");
			} else {
				set_all_if_all_selected(patientWriteCheckBoxIds, "#all_patient_w");
			}
		} else {
			if (myId.endsWith("_r")) {
				$("#all_patient_r").prop("checked", false);
			} else {
				$("#all_patient_w").prop("checked", false);
			}
		}

		update_selected_scopes();

		$("input[type='checkbox']").checkboxradio("refresh");
	});
	
});
</script>

<b>Selected Scopes</b><br/>
<textarea id="selected_scopes" name="selected_scopes" class="text ui-widget-content ui-corner-all" style="width: 70%;" rows="7" readonly></textarea>

<table style="border:0; width: 70%;">
	<tr>
		<th id="user_or_system_scope_head">
			Scope
		</th>
		<th id="patient_scope_head">Patient Scopes</th>
	</tr>
	<tr>
		<td valign="top">
			<table style="width: 100%; border: 1px solid grey; border-collapse: collapse;">
				<tr>
					<td style="border: 1px solid grey; padding: 5px;"><b><i>All Resources</i></b></td>
					<td style="border: 1px solid grey; padding: 5px;">
						<label for="all_user_r">read
							<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="all_user_r" name="all_user_r" > 
						</label>
						<label for="all_user_w">write
							<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="all_user_w" name="all_user_w" >
						</label>
					</td>
				</tr>
				<tr>
					<td style="border: 1px solid grey; padding: 5px;">Condition</td>
					<td style="border: 1px solid grey; padding: 5px;">
						<label for="user_condition_r">read
							<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="user_condition_r" name="user_condition_r" > 
						</label>
						<label for="user_condition_w">write
							<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="user_condition_w" name="user_condition_w" >
						</label>
					</td>
				</tr>
				<tr>
					<td style="border: 1px solid grey; padding: 5px;">DocumentReference</td>
					<td style="border: 1px solid grey; padding: 5px;">
						<label for="user_documentreference_r">read
							<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="user_documentreference_r" name="user_documentreference_r" >
						</label>
						<label for="user_documentreference_w">write
 							<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="user_documentreference_w" name="user_documentreference_w" >
 						</label>
					</td>
				</tr>
				<tr>
					<td style="border: 1px solid grey; padding: 5px;">Encounter</td>
					<td style="border: 1px solid grey; padding: 5px;">
						<label for="user_encounter_r">read
							<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="user_encounter_r" name="user_encounter_r" >  
						</label>
 						<label for="user_encounter_w">write	
 							<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="user_encounter_w" name="user_encounter_w" >
 						</label>
					</td>
				</tr>
				<tr>
					<td style="border: 1px solid grey; padding: 5px;">MedicationStatement</td>
					<td style="border: 1px solid grey; padding: 5px;">
						<label for="user_medicationstatement_r">read
							<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="user_medicationstatement_r" name="user_medicationstatement_r" >
						</label>
 						<label for="user_medicationstatement_w"> write
 							<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="user_medicationstatement_w" name="user_medicationstatement_w" >
 						</label>
					</td>
				</tr>
				<tr>
					<td style="border: 1px solid grey; padding: 5px;">MedicationRequest</td>
					<td style="border: 1px solid grey; padding: 5px;">
						<label for="user_medicationrequest_r">read
							<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="user_medicationrequest_r" name="user_medicationrequest_r" > 
						</label>
 						<label for="user_medicationrequest_w"> write
 							<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="user_medicationrequest_w" name="user_medicationrequest_w" >
 						</label>
					</td>
				</tr>
				<tr>
					<td style="border: 1px solid grey; padding: 5px;">Observation</td>
					<td style="border: 1px solid grey; padding: 5px;">
						<label for="user_observation_r">read
							<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="user_observation_r" name="user_observation_r" >
						</label> 
 						<label for="user_observation_w">write
 							<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="user_observation_w" name="user_observation_w" > 
 						</label>
					</td>
				</tr>
				<tr>
					<td style="border: 1px solid grey; padding: 5px;">Patient</td>
					<td style="border: 1px solid grey; padding: 5px;">
						<label for="user_patient_r">read
							<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="user_patient_r" name="user_patient_r" >
						</label>
 						<label for="user_patient_w">write
 							<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="user_patient_w" name="user_patient_w" >
 						</label>
					</td>
				</tr>
				<tr>
					<td style="border: 1px solid grey; padding: 5px;">Procedure</td>
					<td style="border: 1px solid grey; padding: 5px;">
						<label for="user_procedure_r">read
							<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="user_procedure_r" name="user_procedure_r" > 
						</label>
 						<label for="user_procedure_w">write
 							<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="user_procedure_w" name="user_procedure_w" >
 						</label>
					</td>
				</tr>
			</table>
	</td>
	<td valign="top" id="patient_scope_content">
		<table style="width: 100%; padding: 2px; border: 1px solid grey; border-collapse: collapse;">
			<tr>
				<td style="border: 1px solid grey; padding: 5px;"><b><i>All Resource</i></b></td>
				<td style="border: 1px solid grey; padding: 5px;">
					<label for="all_patient_r">read
						<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="all_patient_r" name="all_patient_r" > 
					</label>
					<label for="all_patient_w">write
						<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="all_patient_w" name="all_patient_w" >
					</label>
				</td>
			</tr>
			<tr>
				<td style="border: 1px solid grey; padding: 5px;">Condition</td>
				<td style="border: 1px solid grey; padding: 5px;">
					<label for="patient_condition_r">read 
						<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="patient_condition_r" name="patient_condition_r" >
					</label>
 					<label for="patient_condition_w">write
 						<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="patient_condition_w" name="patient_condition_w" >
 					</label>
				</td>
			</tr>
			<tr>
				<td style="border: 1px solid grey; padding: 5px;">DocumentReference</td>
				<td style="border: 1px solid grey; padding: 5px;">
					<label for="patient_documentreference_r">read
						<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="patient_documentreference_r" name="patient_documentreference_r" > 
					</label>
					<label for="patient_documentreference_w">write
 						<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="patient_documentreference_w" name="patient_documentreference_w" >
 					</label>
				</td>
			</tr>
			<tr>
				<td style="border: 1px solid grey; padding: 5px;">Encounter</td>
				<td style="border: 1px solid grey; padding: 5px;">
					<label for="patient_encounter_r">read
						<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="patient_encounter_r" name="patient_encounter_r" >
					</label>
 					<label for="patient_encounter_w">write
 						<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="patient_encounter_w" name="patient_encounter_w" >
 					</label>
				</td>
			</tr>
			<tr>
				<td style="border: 1px solid grey; padding: 5px;">MedicationStatement</td>
				<td style="border: 1px solid grey; padding: 5px;">
					<label for="patient_medicationstatement_r">read
						<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="patient_medicationstatement_r" name="patient_medicationstatement_r" > 
					</label>
 					<label for="patient_medicationstatement_w">write
 						<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="patient_medicationstatement_w" name="patient_medicationstatement_w" >
 					</label>
				</td>
			</tr>
			<tr>
				<td style="border: 1px solid grey; padding: 5px;">MedicationRequest</td>
				<td style="border: 1px solid grey; padding: 5px;">
					<label for="patient_medicationrequest_r">read
						<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="patient_medicationrequest_r" name="patient_medicationrequest_r" >
					</label>
 					<label for="patient_medicationrequest_w">write
 						<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="patient_medicationrequest_w" name="patient_medicationrequest_w" >
 					</label>
				</td>
			</tr>
			<tr>
				<td style="border: 1px solid grey; padding: 5px;">Observation</td>
				<td style="border: 1px solid grey; padding: 5px;">
					<label for="patient_observation_r">read
						<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="patient_observation_r" name="patient_observation_r" >
					</label>
 					<label for="patient_observation_w">write
 						<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="patient_observation_w" name="patient_observation_w" >
 					</label>
				</td>
			</tr>
			<tr>
				<td style="border: 1px solid grey; padding: 5px;">Patient</td>
				<td style="border: 1px solid grey; padding: 5px;">
					<label for="patient_patient_r">read
						<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="patient_patient_r" name="patient_patient_r" >
					</label>
 					<label for="patient_patient_w">write
 						<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="patient_patient_w" name="patient_patient_w" >
 					</label>
				</td>
			</tr>
			<tr>
				<td style="border: 1px solid grey; padding: 5px;">Procedure</td>
				<td style="border: 1px solid grey; padding: 5px;">
					<label for="patient_procedure_r">read
						<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="patient_procedure_r" name="patient_procedure_r" >
					</label>
 					<label for="patient_procedure_w">write
 						<input <%if (disableIt) {out.print("disabled");} %> type="checkbox" id="patient_procedure_w" name="patient_procedure_w" >
 					</label>
				</td>
			</tr>
		</table>
	</td>
	</tr>
</table>
