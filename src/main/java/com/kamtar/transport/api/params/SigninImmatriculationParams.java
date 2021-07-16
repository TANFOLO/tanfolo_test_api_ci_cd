package com.kamtar.transport.api.params;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;


public class SigninImmatriculationParams extends SigninParams{
	
	/**
	 * Immatriculation (obligatoire si c'est un driver qui se connecte, facultatif si c'est un propriétaire)
	 */
	@ApiModelProperty(notes = "Immatriculation du véhicule", allowEmptyValue = false, required = true)
	private String immatriculation;

	/**
	 * Est ce que le token servira dnas une webview ?
	 */
	private String webview;


	public String getWebview() {
		return webview;
	}

	public void setWebview(String webview) {
		this.webview = webview;
	}

	public String getImmatriculation() {
		return immatriculation;
	}

	public void setImmatriculation(String immatriculation) {
		this.immatriculation = immatriculation;
	}

	public SigninImmatriculationParams() {
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
