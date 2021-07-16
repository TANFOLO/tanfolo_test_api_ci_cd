package com.kamtar.transport.api.params;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


public class DeleteFactureClientParams extends ParentParams {

	@ApiModelProperty(notes = "Identifiant de la facture,", allowEmptyValue = true, required = false, dataType = "UUID")
	@NotNull(message = "{err.facture.id}")
	@Size(min = 1, max = 255, message = "{err.facture.id_longueur}")
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	

}
