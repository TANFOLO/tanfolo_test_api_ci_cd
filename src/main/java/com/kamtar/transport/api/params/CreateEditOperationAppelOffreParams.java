package com.kamtar.transport.api.params;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;

import io.swagger.annotations.ApiModelProperty;


public class CreateEditOperationAppelOffreParams extends ParentParams {


	@ApiModelProperty(notes = "Identifiant de l'opération", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.operation.create.id}")
	@UUIDObligatoireConstraint(message = "{err.operation.create.id_invalid}")
	private String id_operation;
	
	@ApiModelProperty(notes = "Identifiant du véhicule", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.véhicule.id}")
	private List<String> id_vehicules;
	
	@ApiModelProperty(notes = "Montant proposé par le driver pour cette opération", allowEmptyValue = true, required = false)
	protected Double montant;

	@ApiModelProperty(notes = "Devise du montant proposé par le driver pour cette opération", allowEmptyValue = true, required = false, dataType = "Devise parmi la liste retournée par le plus API /devises")
	protected String montant_devise;

	public String getId_operation() {
		return id_operation;
	}

	public void setId_operation(String id_operation) {
		this.id_operation = id_operation;
	}


	public List<String> getId_vehicules() {
		return id_vehicules;
	}

	public void setId_vehicules(List<String> id_vehicules) {
		this.id_vehicules = id_vehicules;
	}

	public Double getMontant() {
		return montant;
	}

	public void setMontant(Double montant) {
		this.montant = montant;
	}

	public String getMontant_devise() {
		return montant_devise;
	}

	public void setMontant_devise(String montant_devise) {
		this.montant_devise = montant_devise;
	}
	
	
	



}
