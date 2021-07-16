package com.kamtar.transport.api.params;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;

import io.swagger.annotations.ApiModelProperty;


public class DeleteDriverParams extends ParentParams {
	
	@ApiModelProperty(notes = "Identifiant du driver", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.user.id}")
	@UUIDObligatoireConstraint(message = "{err.utilisateur.id_invalid}")
	private String id;
	
	

	public DeleteDriverParams() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}



}
