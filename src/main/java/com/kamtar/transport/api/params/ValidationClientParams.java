package com.kamtar.transport.api.params;

import com.kamtar.transport.api.validation.NumeroDeTelephoneLibreValidConstraint;
import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


public class ValidationClientParams extends ParentParams {

	@ApiModelProperty(notes = "Code de validation", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.client.validation}")
	private String validation;

	@ApiModelProperty(notes = "Numéro de téléphone principale de l'expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.client.validation.telephone}")
	@Size(min = 1, max = 200, message = "{err.client.validation.telephone_longueur}")
	protected String telephone;

	@ApiModelProperty(notes = "Code du pays dans lequel kamtar opère", allowEmptyValue = false, required = true)
	private String pays;

	public String getPays() {
		return pays;
	}

	public void setPays(String pays) {
		this.pays = pays;
	}

	public String getValidation() {
		return validation;
	}

	public void setValidation(String validation) {
		this.validation = validation;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
}
