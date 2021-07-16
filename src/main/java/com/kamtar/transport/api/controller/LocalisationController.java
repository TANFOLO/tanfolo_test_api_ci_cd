package com.kamtar.transport.api.controller;

import java.util.List;
import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.service.MapperJSONService;
import com.kamtar.transport.api.swagger.ListAdresse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.classes.Adresse;
import com.kamtar.transport.api.service.LocalisationService;
import com.kamtar.transport.api.utils.JWTProvider;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@Api(value="Gestion des localisations", description="API Rest qui gère l'ensemble des localisations")
@RestController
@EnableWebMvc
public class LocalisationController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(LocalisationController.class);  

	@Autowired
	LocalisationService localisationService;

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	MapperJSONService mapperJSONService;

	/**
	 * Autocompletion
	 * @param postBody
	 * @return
	 */
	/*@RequestLogger(name = Application.microservice)*/
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Autocompletion sur les adresses postales")
	@ApiResponses(value = {
		    @ApiResponse(code = 200, message = "Liste d'adresse", response = ListAdresse.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/localisation/autocompletion", 
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity autocompletion(
			@ApiParam(value = "Partie de l'adresse recherchée", required = true) @RequestParam("query") String query
			) throws JsonProcessingException {

		List<Adresse> adresses = localisationService.autocomplete(query);

		return new ResponseEntity<>(mapperJSONService.get(null).writeValueAsString(adresses), HttpStatus.OK);

	}




}
