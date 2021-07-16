package com.kamtar.transport.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.service.MapperJSONService;
import com.kamtar.transport.api.service.VehiculeTonnageService;
import com.kamtar.transport.api.service.VehiculeTypeService;
import com.kamtar.transport.api.swagger.ListVehiculeTonnage;
import com.kamtar.transport.api.swagger.ListVehiculeType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;

@Api(value="Gestion des tonnages de véhicules", description="API Rest qui gère les tonnages de véhicules")
@RestController
@EnableWebMvc
public class VehiculeTonnageController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(VehiculeTonnageController.class);

	@Autowired
	VehiculeTonnageService vehiculeTonnageService;

	@Autowired
	MapperJSONService mapperJSONService;
	
	/**
	 * Récupére les tonnages de véhicules
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupére tous les tonnages de véhicules")
	@ApiResponses(value = {
		    @ApiResponse(code = 200, message = "Liste des tonnages de véhicules", response = ListVehiculeTonnage.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/vehicules/tonnages",
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity<Object> getVehiculesTonnages(
			HttpServletRequest request) throws JsonProcessingException {
		return new ResponseEntity<Object>(mapperJSONService.get(null).writeValueAsString(vehiculeTonnageService.getAll()), HttpStatus.OK);
	}
	


}
