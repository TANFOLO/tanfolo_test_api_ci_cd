package com.kamtar.transport.api.params;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;

import io.swagger.annotations.ApiModelProperty;


public class DeleteAdminKamtarParams extends ParentParams {
	
	@ApiModelProperty(notes = "Identifiant de l'administrateur", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.user.id}")
	@UUIDObligatoireConstraint(message = "{err.utilisateur.id_invalid}")
	private String id;
	
	
	
	public DeleteAdminKamtarParams() {
		super();
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}



}
