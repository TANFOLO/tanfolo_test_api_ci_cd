package com.kamtar.transport.api.params;

import io.swagger.annotations.ApiModelProperty;


public class TelephoneExistantParams extends ParentParams {

	/**
	 * Adresse e-mail
	 */
	@ApiModelProperty(notes = "Numéro de téléphone à vérifier", allowEmptyValue = true, required = false)
	protected String telephone;

	@ApiModelProperty(notes = "Code pays où opére Kamtar", allowEmptyValue = true, required = false)
	protected String pays;

	public String getPays() {
		return pays;
	}

	public void setPays(String pays) {
		this.pays = pays;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
}
