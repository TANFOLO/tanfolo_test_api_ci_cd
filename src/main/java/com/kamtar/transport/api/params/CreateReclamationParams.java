package com.kamtar.transport.api.params;

import com.kamtar.transport.api.validation.NumeroDeTelephoneValidConstraint;
import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


public class CreateReclamationParams extends ParentParams {

	@ApiModelProperty(notes = "Motif de la réclamation", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.reclamation.create.motif}")
	@Size(min = 1, max = 100, message = "{err.reclamation.create.motif_longueur}")
	protected String motif;

	@ApiModelProperty(notes = "Descriptif de la réclamation", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.reclamation.create.descriptif}")
	@Size(min = 1, max = 100, message = "{err.reclamation.create.descriptif_longueur}")
	protected String descriptif;

	@ApiModelProperty(notes = "Opération liée à la réclamation", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.reclamation.create.operation}")
	@UUIDObligatoireConstraint(message = "{err.reclamation.create.operation_uuid}")
	private String operation;

	@ApiModelProperty(notes = "Langue parlée par l'administrateur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.locale}")
	protected String locale;

	public CreateReclamationParams() {
		super();
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getMotif() {
		return motif;
	}

	public void setMotif(String motif) {
		this.motif = motif;
	}

	public String getDescriptif() {
		return descriptif;
	}

	public void setDescriptif(String descriptif) {
		this.descriptif = descriptif;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}
}
