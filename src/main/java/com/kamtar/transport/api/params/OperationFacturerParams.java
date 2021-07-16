package com.kamtar.transport.api.params;

import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;


public class OperationFacturerParams extends ParentParams {


	
	@ApiModelProperty(notes = "Identifiant des opérations", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.operation.create.id}")
	private List<String> id_operations;

	public List<String> getId_operations() {
		return id_operations;
	}

	public void setId_operations(List<String> id_operations) {
		this.id_operations = id_operations;
	}
}
