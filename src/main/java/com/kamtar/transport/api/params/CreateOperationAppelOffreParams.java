package com.kamtar.transport.api.params;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kamtar.transport.api.validation.UUIDFacultatifConstraint;
import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;

import io.swagger.annotations.ApiModelProperty;


public class CreateOperationAppelOffreParams extends ParentParams {


	@ApiModelProperty(notes = "Identifiant de l'opération", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.operation.create.id}")
	@UUIDObligatoireConstraint(message = "{err.operation.create.id_invalid}")
	private String id_operation;
	
	@ApiModelProperty(notes = "Identifiant du véhicule", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.vehicule.id}")
	@UUIDObligatoireConstraint(message = "{err.vehicule.id_invalid}")
	private String id_vehicule;
	
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


	public String getId_vehicule() {
		return id_vehicule;
	}

	public void setId_vehicule(String id_vehicule) {
		this.id_vehicule = id_vehicule;
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
