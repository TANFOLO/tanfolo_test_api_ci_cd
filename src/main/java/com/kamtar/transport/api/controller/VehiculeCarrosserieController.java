package com.kamtar.transport.api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.model.VehiculeCarrosserie;
import com.kamtar.transport.api.service.MapperJSONService;
import io.swagger.annotations.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.*;

import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.service.CountryService;
import com.kamtar.transport.api.service.VehiculeCarrosserieService;
import com.kamtar.transport.api.service.VehiculeTypeService;
import com.kamtar.transport.api.swagger.ListPays;
import com.kamtar.transport.api.swagger.ListVehiculeType;

import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Api(value="Gestion des carrosseries des véhicules", description="API Rest qui gère les carrosseries des véhicules")
@RestController
@EnableWebMvc
public class VehiculeCarrosserieController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(VehiculeCarrosserieController.class);  

	@Autowired
	VehiculeCarrosserieService vehiculeCarrosserieService;

	@Autowired
	MapperJSONService mapperJSONService;
	
	/**
	 * Récupére les carrosseries des véhicules en fonction du pays
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt, 
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "écupére les carrosseries des véhicules en fonction du pays")
	@ApiResponses(value = {
		    @ApiResponse(code = 200, message = "écupére les carrosseries des véhicules en fonction du pays", response = ListVehiculeType.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/vehicules/carrosseries", 
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity<Object> getVehiculesCarrosseries(
			@ApiParam(value = "Code pays pour la segmentation des carrosseries", required = false) @RequestParam("codePays") String codePays,
			HttpServletRequest request) throws JsonProcessingException {

		List<VehiculeCarrosserie> carrosseries = vehiculeCarrosserieService.getAll(codePays);
		return new ResponseEntity<Object>(mapperJSONService.get(null).writeValueAsString(carrosseries), HttpStatus.OK);
	}
	


}
