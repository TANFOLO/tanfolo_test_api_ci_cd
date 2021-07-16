package com.kamtar.transport.api.params;

import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


public class EditReclamationParams extends ParentParams {

	@ApiModelProperty(notes = "UUID de la réclamation", allowEmptyValue = false, required = true)
	@UUIDObligatoireConstraint(message = "{err.reclamation.echange.create.reclamation_uuid}")
	protected String reclamation;

	@ApiModelProperty(notes = "Code du statut de la réclamation", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.reclamation.edit.statut}")
	@Size(min = 1, max = 100, message = "{err.reclamation.edit.statut_longueur}")
	protected String statut;

	public EditReclamationParams() {
		super();
	}

	public String getStatut() {
		return statut;
	}

	public void setStatut(String statut) {
		this.statut = statut;
	}

	public String getReclamation() {
		return reclamation;
	}

	public void setReclamation(String reclamation) {
		this.reclamation = reclamation;
	}
}
