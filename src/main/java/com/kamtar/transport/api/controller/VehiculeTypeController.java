package com.kamtar.transport.api.controller;

import java.util.HashMap;
import java.util.Map;

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
import com.kamtar.transport.api.service.CountryService;
import com.kamtar.transport.api.service.VehiculeTypeService;
import com.kamtar.transport.api.swagger.ListPays;
import com.kamtar.transport.api.swagger.ListVehiculeType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Api(value="Gestion des types de véhicules", description="API Rest qui gère les types de véhicules")
@RestController
@EnableWebMvc
public class VehiculeTypeController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(VehiculeTypeController.class);  

	@Autowired
	VehiculeTypeService vehiculeTypeService;

	@Autowired
	MapperJSONService mapperJSONService;
	
	/**
	 * Récupére les types de véhicules
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupére tous les types de véhicules")
	@ApiResponses(value = {
		    @ApiResponse(code = 200, message = "Liste des types de véhicules", response = ListVehiculeType.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/vehicules/types", 
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity<Object> getVehiculesTypes(
			HttpServletRequest request) throws JsonProcessingException {
		return new ResponseEntity<Object>(mapperJSONService.get(null).writeValueAsString(vehiculeTypeService.getAll()), HttpStatus.OK);
	}
	


}
