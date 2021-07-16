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


public class AffecterOperationParams extends ParentParams {

	@ApiModelProperty(notes = "Identifiant de l'opération", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.operation.create.id}")
	@UUIDObligatoireConstraint(message = "{err.operation.create.id_invalid}")
	private String operation_id;
	
	@ApiModelProperty(notes = "Identifiant de l'appel d'offre", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.operation_appel_offre.create.id}")
	@Size(min = 1, max = 100, message = "{err.operation_appel_offre.create.id_longueur}") 
	private String appel_offre_id;

	public String getOperation_id() {
		return operation_id;
	}

	public void setOperation_id(String operation_id) {
		this.operation_id = operation_id;
	}

	public String getAppel_offre_id() {
		return appel_offre_id;
	}

	public void setAppel_offre_id(String appel_offre_id) {
		this.appel_offre_id = appel_offre_id;
	}

	

	



}
