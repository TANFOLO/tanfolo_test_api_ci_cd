package com.kamtar.transport.api.params;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;

public class MotDePassePerduChangerParams extends ParentParams {

	/**
	 * Mot de passe
	 */
	@ApiModelProperty(notes = "Nouveau mot de passe", allowEmptyValue = false, required = true, dataType = "Chaine de caractère")
	@NotNull(message = "{err.user.mot_de_passe_perdu.mot_de_passe}")
	@Size(min = 1, max = 100, message = "{err.user.mot_de_passe_perdu.mot_de_passe_longueur}") 
	protected String nouveau_mot_de_passe;

	/**
	 * Token
	 */
	@ApiModelProperty(notes = "Token de changement de mot de passe", allowEmptyValue = false, required = true, dataType = "Chaine de caractère")
	@NotNull(message = "{err.user.mot_de_passe_perdu.token}")
	@Size(min = 24, max = 24, message = "{err.user.mot_de_passe_perdu.token_longueur}") 
	protected String token;

	@ApiModelProperty(notes = "Code du pays où opère kamtar", allowEmptyValue = false, required = true)
	protected String code_pays;

	public String getCode_pays() {
		return code_pays;
	}

	public void setCode_pays(String code_pays) {
		this.code_pays = code_pays;
	}

	public String getNouveau_mot_de_passe() {
		return nouveau_mot_de_passe;
	}

	public void setNouveau_mot_de_passe(String nouveau_mot_de_passe) {
		this.nouveau_mot_de_passe = nouveau_mot_de_passe;
	}

	public MotDePassePerduChangerParams() {
		super();
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}


	
	



}
