package com.kamtar.transport.api.params;

import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


public class CreateReclamationEchangeParams extends ParentParams {

	@ApiModelProperty(notes = "UUID de la réclamation", allowEmptyValue = false, required = true)
	@UUIDObligatoireConstraint(message = "{err.reclamation.echange.create.reclamation_uuid}")
	protected String reclamation;

	@ApiModelProperty(notes = "Descriptif de l'échange de la réclamation", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.reclamation.echange.create.descriptif}")
	@Size(min = 1, max = 100, message = "{err.reclamation.echange.create.descriptif_longueur}")
	protected String descriptif;

	@ApiModelProperty(notes = "Langue parlée par l'administrateur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.locale}")
	protected String locale;

	public CreateReclamationEchangeParams() {
		super();
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getDescriptif() {
		return descriptif;
	}

	public void setDescriptif(String descriptif) {
		this.descriptif = descriptif;
	}

	public String getReclamation() {
		return reclamation;
	}

	public void setReclamation(String reclamation) {
		this.reclamation = reclamation;
	}
}
