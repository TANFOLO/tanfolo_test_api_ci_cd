package com.kamtar.transport.api.params;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;

public class MotDePassePerduParams extends ParentParams {

	
	/**
	 * Soit numéro de téléphone, soit email
	 */
	@ApiModelProperty(notes = "E-mail ou numéro de téléphone correspondant au compte", allowEmptyValue = false, required = true, dataType = "Chaine de caractère")
	@NotNull(message = "{err.user.sigin.login}")
	@Size(min = 1, max = 100, message = "{err.user.sigin.login_longueur}") 
	private String login;

	/**
	 * Type de compte : driver, client ou admin_operateur
	 */
	@ApiModelProperty(notes = "Type de compte", allowEmptyValue = false, required = true)
	private String type_compte;

	@ApiModelProperty(notes = "Code pays où opére Kamtar", allowEmptyValue = true, required = false)
	protected String code_pays;

	public String getCode_pays() {
		return code_pays;
	}

	public void setCode_pays(String code_pays) {
		this.code_pays = code_pays;
	}

	public String getType_compte() {
		return type_compte;
	}

	public void setType_compte(String type_compte) {
		this.type_compte = type_compte;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	
	



}
