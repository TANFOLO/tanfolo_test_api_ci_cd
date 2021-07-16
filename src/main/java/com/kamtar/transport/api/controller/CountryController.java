package com.kamtar.transport.api.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kamtar.transport.api.mixin.ClientMixin_DriverProprietaire;
import com.kamtar.transport.api.model.Country;
import com.kamtar.transport.api.mixin.OperationMixin_Client;
import com.kamtar.transport.api.model.Language;
import com.kamtar.transport.api.service.MapperJSONService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.service.CountryService;
import com.kamtar.transport.api.swagger.ListPays;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Api(value="Gestion des pays", description="API Rest qui gère l'ensemble des pays")
@RestController
@EnableWebMvc
public class CountryController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(CountryController.class);  

	@Autowired
	CountryService countryService;

	@Autowired
	MapperJSONService mapperJSONService;
	
	/**
	 * Récupére tous les pays
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupére les pays")
	@ApiResponses(value = {
		    @ApiResponse(code = 200, message = "Liste des pays", response = ListPays.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/countries", 
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity<Object> getCountries(
			HttpServletRequest request) throws JsonProcessingException {
		List<Country> countries = countryService.getAll();
		return new ResponseEntity<Object>(mapperJSONService.get(null).writeValueAsString(countries), HttpStatus.OK);
	}

	/**
	 * Récupére tous les pays où kamtar opère
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupére tous les pays où kamtar opère")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Liste des pays", response = ListPays.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/countries_kamtar",
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity<Object> getCountries2(
			HttpServletRequest request) throws JsonProcessingException {
		return new ResponseEntity<Object>(mapperJSONService.get(null).writeValueAsString(countryService.getCountriesKamtarOpere()), HttpStatus.OK);
	}
	


}
