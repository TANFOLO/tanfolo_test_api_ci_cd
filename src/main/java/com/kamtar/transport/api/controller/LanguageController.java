package com.kamtar.transport.api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.service.MapperJSONService;
import com.kamtar.transport.api.swagger.ListLanguage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.model.Country;
import com.kamtar.transport.api.params.CreateAdminKamtarParams;
import com.kamtar.transport.api.params.LanguagesOfCountryParams;
import com.kamtar.transport.api.service.CountryService;
import com.kamtar.transport.api.service.LanguageService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Api(value="Gestion des langues", description="API Rest qui gère l'ensemble des langues")
@RestController
@EnableWebMvc
public class LanguageController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(LanguageController.class);  

	@Autowired
	LanguageService languageService;
	
	@Autowired
	CountryService countryService;

	@Autowired
	MapperJSONService mapperJSONService;
	
	/**
	 * Récupére tutes les langues
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupére toutes les langues")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Récupére toutes les langues", response = ListLanguage.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/languages", 
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity<Object> getLanguages(
			HttpServletRequest request) throws JsonProcessingException {
		return new ResponseEntity<Object>(mapperJSONService.get(null).writeValueAsString(languageService.getAll()), HttpStatus.OK);
	}
	
	/**
	 * Récupére les langues d'un pays
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupére les langues d'un pays")
	@ApiResponses(value = {
		    @ApiResponse(code = 200, message = "Liste des langues du pays demandé", response = ListLanguage.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/languages", 
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> getLanguagesOfCountry(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody LanguagesOfCountryParams postBody) throws JsonProcessingException {
		
		Optional<Country> country = countryService.getByCode(postBody.getCode());
		if (country.isPresent()) {
			return new ResponseEntity<Object>(mapperJSONService.get(null).writeValueAsString(country.get().getLanguages()), HttpStatus.OK);
		}
		
		return new ResponseEntity<Object>(null, HttpStatus.NOT_FOUND);
	}
	

}
