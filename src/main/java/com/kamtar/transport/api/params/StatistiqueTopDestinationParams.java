package com.kamtar.transport.api.params;

import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


public class StatistiqueTopDestinationParams extends ParentParams {

	@ApiModelProperty(notes = "Identifiant du client", allowEmptyValue = true, required = false, dataType = "UUID")
	private String client;

	@ApiModelProperty(notes = "Période", allowEmptyValue = true, required = false, dataType = "String")
	private String periode;

	@ApiModelProperty(notes = "Opérations", allowEmptyValue = true, required = false, dataType = "Integer")
	private String operations;

	@ApiModelProperty(notes = "Nombre de destinations", allowEmptyValue = true, required = false, dataType = "Integer")
	private Integer nb;


	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getPeriode() {
		return periode;
	}

	public void setPeriode(String periode) {
		this.periode = periode;
	}

	public String getOperations() {
		return operations;
	}

	public void setOperations(String operations) {
		this.operations = operations;
	}

	public Integer getNb() {
		return nb;
	}

	public void setNb(Integer nb) {
		this.nb = nb;
	}
}
