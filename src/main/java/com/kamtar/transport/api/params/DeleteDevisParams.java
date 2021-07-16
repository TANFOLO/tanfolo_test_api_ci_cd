package com.kamtar.transport.api.params;

import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;


public class DeleteDevisParams extends ParentParams {

	@ApiModelProperty(notes = "Identifiant du devis", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.devis.id}")
	@UUIDObligatoireConstraint(message = "{err.devis.id_invalid}")
	private String id;

	public DeleteDevisParams() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}



}
