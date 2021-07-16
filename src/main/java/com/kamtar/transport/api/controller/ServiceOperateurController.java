package com.kamtar.transport.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.service.CountryService;
import com.kamtar.transport.api.service.MapperJSONService;
import com.kamtar.transport.api.service.ServiceOperateurService;
import com.kamtar.transport.api.swagger.ListPays;
import com.kamtar.transport.api.swagger.ListServiceOperateur;
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

@Api(value="Gestion des services es opérateurs", description="API Rest qui gère les services es opérateurs")
@RestController
@EnableWebMvc
public class ServiceOperateurController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(ServiceOperateurController.class);

	@Autowired
	ServiceOperateurService serviceOperateurService;

	@Autowired
	MapperJSONService mapperJSONService;
	
	/**
	 * Récupére les services des opérateurs
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupére les services des opérateurs")
	@ApiResponses(value = {
		    @ApiResponse(code = 200, message = "Liste des services des opérateurs", response = ListServiceOperateur.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/operateurs/services",
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity<Object> getServicesOperateurs(
			HttpServletRequest request) throws JsonProcessingException {
		return new ResponseEntity<Object>(mapperJSONService.get(null).writeValueAsString(serviceOperateurService.getAll()), HttpStatus.OK);
	}
	


}
