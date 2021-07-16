package com.kamtar.transport.api.controller;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.kamtar.transport.api.service.MarchandiseTypeService;
import com.kamtar.transport.api.swagger.ListMarchandiseType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Api(value="Gestion des types de marchandise", description="API Rest qui gère les types de marchandise")
@RestController
@EnableWebMvc
public class MarchandiseTypeController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(MarchandiseTypeController.class);  

	@Autowired
	MarchandiseTypeService marchandiseTypeService;

	@Autowired
	MapperJSONService mapperJSONService;
	
	/**
	 * Récupére les types de marchandises
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupére tous les types de marchandises")
	@ApiResponses(value = {
		    @ApiResponse(code = 200, message = "Liste des types de marchandises", response = ListMarchandiseType.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/marchandises/types", 
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity<Object> getMarchandisesTypes(
			HttpServletRequest request) throws JsonProcessingException {
		return new ResponseEntity<Object>(mapperJSONService.get(null).writeValueAsString(marchandiseTypeService.getAll()), HttpStatus.OK);
	}



}
