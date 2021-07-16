package com.kamtar.transport.api.params;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.kamtar.transport.api.validation.LanguageCodeAlreadyExistConstraint;

import io.swagger.annotations.ApiModelProperty;


public class LanguageCreateParams extends ParentParams {
	
	@ApiModelProperty(notes = "Code de la langue", allowEmptyValue = false, required = true, dataType = "Chaine de caractère")
	@NotNull(message = "{err.language.code}")
	@Size(min = 1, max = 10, message = "{err.language.code_longueur}") 
	@LanguageCodeAlreadyExistConstraint(message = "{err.language.code_deja_utilise}")
	private String code;
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public LanguageCreateParams() {
		super();
	}

}
