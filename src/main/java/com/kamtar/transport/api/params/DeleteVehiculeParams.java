package com.kamtar.transport.api.params;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;

import io.swagger.annotations.ApiModelProperty;


public class DeleteVehiculeParams extends ParentParams {
	
	@ApiModelProperty(notes = "Identifiant du véhicule", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.vehicule.id}")
	@UUIDObligatoireConstraint(message = "{err.vehicule.id_invalid}")
	private String id;

	public DeleteVehiculeParams() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}



}
