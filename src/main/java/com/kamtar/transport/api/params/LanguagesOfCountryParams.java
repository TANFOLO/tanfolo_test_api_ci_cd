package com.kamtar.transport.api.params;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.kamtar.transport.api.validation.CountryCode3AlreadyExistConstraint;
import com.kamtar.transport.api.validation.CountryCodeAlreadyExistConstraint;

import io.swagger.annotations.ApiModelProperty;


public class LanguagesOfCountryParams extends ParentParams {
	
	@ApiModelProperty(notes = "Code du pays", allowEmptyValue = false, required = true, dataType = "Chaine de caractère")
	@NotNull(message = "{err.country.code}")
	@Size(min = 1, max = 10, message = "{err.country.code_longueur}") 
	private String code;
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public LanguagesOfCountryParams() {
		super();
	}


}
