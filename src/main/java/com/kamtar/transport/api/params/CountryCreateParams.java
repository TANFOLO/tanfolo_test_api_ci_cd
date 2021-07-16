package com.kamtar.transport.api.params;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.kamtar.transport.api.validation.CountryCode3AlreadyExistConstraint;
import com.kamtar.transport.api.validation.CountryCodeAlreadyExistConstraint;

import io.swagger.annotations.ApiModelProperty;


public class CountryCreateParams extends ParentParams {

	@ApiModelProperty(notes = "Code du pays sur 2 caractères", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.country.code}")
	@Size(min = 1, max = 10, message = "{err.country.code_longueur}") 
	@CountryCodeAlreadyExistConstraint(message = "{err.country.code_deja_utilise}")
	private String code;

	@ApiModelProperty(notes = "Nom du pays", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.country.name}")
	@Size(min = 1, max = 250, message = "{err.country.name_longueur}")    
	private String name;

	@ApiModelProperty(notes = "Code du pays du 3 caractères (ISO3)", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.country.code3}")
	@Size(min = 3, max = 3, message = "{err.country.code3_longueur}")    
	@CountryCode3AlreadyExistConstraint(message = "{err.country.code3_deja_utilise}")
	private String code3;

	@ApiModelProperty(notes = "Code du continent", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.country.continent}")
	@Size(min = 1, max = 10, message = "{err.country.continent_longueur}")    
	private String continent;

	@ApiModelProperty(notes = "Liste des langues parlées dans le pays", allowEmptyValue = true, required = false, dataType = "Liste de code de langues")
	List<String> languages;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CountryCreateParams() {
		super();
	}

	public String getCode3() {
		return code3;
	}

	public void setCode3(String code3) {
		this.code3 = code3;
	}

	public List<String> getLanguages() {
		return languages;
	}

	public void setLanguages(List<String> languages) {
		this.languages = languages;
	}

	public String getContinent() {
		return continent;
	}

	public void setContinent(String continent) {
		this.continent = continent;
	}

}
