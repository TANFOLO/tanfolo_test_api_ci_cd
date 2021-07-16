package com.kamtar.transport.api.params;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


public class EmailExistantParams extends ParentParams {

	/**
	 * Adresse e-mail
	 */
	@ApiModelProperty(notes = "Adresse e-mail à vérifier", allowEmptyValue = true, required = false)
	protected String email;

	@ApiModelProperty(notes = "Code pays où opére Kamtar", allowEmptyValue = true, required = false)
	protected String pays;

	public String getPays() {
		return pays;
	}

	public void setPays(String pays) {
		this.pays = pays;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
