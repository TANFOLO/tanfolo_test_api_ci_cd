package com.kamtar.transport.api.params;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


public class DupliquerOperationParams extends ParentParams {

	@ApiModelProperty(notes = "Identifiant de l'opération,", allowEmptyValue = true, required = false, dataType = "UUID")
	@NotNull(message = "{err.commande.id}")
	@Size(min = 1, max = 255, message = "{err.operation.id_longueur}")
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	

}
