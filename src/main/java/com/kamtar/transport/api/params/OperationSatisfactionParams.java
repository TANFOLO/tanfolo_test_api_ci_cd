package com.kamtar.transport.api.params;

import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


public class OperationSatisfactionParams extends ParentParams {

	@ApiModelProperty(notes = "Identifiant de l'opération", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.operation.create.id}")
	@UUIDObligatoireConstraint(message = "{err.operation.create.id_invalid}")
	private String operation;
	
	@ApiModelProperty(notes = "Identifiant de l'appel d'offre", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.operation_appel_offre.create.id}")
	private Integer satisfaction;


	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public Integer getSatisfaction() {
		return satisfaction;
	}

	public void setSatisfaction(Integer satisfaction) {
		this.satisfaction = satisfaction;
	}
}
