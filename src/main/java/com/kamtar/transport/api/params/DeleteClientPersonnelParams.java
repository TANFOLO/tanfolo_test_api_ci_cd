package com.kamtar.transport.api.params;

import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;


public class DeleteClientPersonnelParams extends ParentParams {

	@ApiModelProperty(notes = "Identifiant de l'expéditeur", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.client.id}")
	@UUIDObligatoireConstraint(message = "{err.client.id_invalid}")
	private String id;


	public DeleteClientPersonnelParams() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}




}
