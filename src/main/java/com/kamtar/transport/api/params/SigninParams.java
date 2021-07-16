package com.kamtar.transport.api.params;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;


public class SigninParams extends ParentParams {
	
	/**
	 * Soit numéro de téléphone, soit email
	 */
	@ApiModelProperty(notes = "Adresse e-mail ou numéro de téléphone principal", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.sigin.login}")
	@Size(min = 1, max = 100, message = "{err.user.sigin.login_longueur}") 
	protected String login;
	
	@ApiModelProperty(notes = "Mot de passe", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.sigin.password}")
	@Size(min = 1, max = 250, message = "{err.user.sigin.password_longueur}")    
	protected String mot_de_passe;

	@ApiModelProperty(notes = "Code pays où opére Kamtar", allowEmptyValue = true, required = false)
	protected String pays;

	public String getPays() {
		return pays;
	}

	public void setPays(String pays) {
		this.pays = pays;
	}

	public SigninParams() {
		super();
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getMot_de_passe() {
		return mot_de_passe;
	}

	public void setMot_de_passe(String mot_de_passe) {
		this.mot_de_passe = mot_de_passe;
	}



}
