package com.kamtar.transport.api.params;

import io.swagger.annotations.ApiModelProperty;


public class TelephoneParams extends ParentParams {

	@ApiModelProperty(notes = "Téléphone", allowEmptyValue = false, required = true)
	private String telephone;

	@ApiModelProperty(notes = "Pays où kamtar opère", allowEmptyValue = false, required = true)
	private String pays;

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
