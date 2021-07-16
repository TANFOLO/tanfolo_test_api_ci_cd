package com.kamtar.transport.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.service.MapperJSONService;
import com.kamtar.transport.api.service.StatutReclamationService;
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

@Api(value="Gestion des statuts des réclamations", description="API Rest qui gère les statuts des réclamations")
@RestController
@EnableWebMvc
public class StatutReclamationController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(StatutReclamationController.class);

	@Autowired
	StatutReclamationService statutReclamationService;

	@Autowired
	MapperJSONService mapperJSONService;
	
	/**
	 * Récupére tous les statuts des reclamations
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupére tous les statuts des reclamations")
	@ApiResponses(value = {
		    @ApiResponse(code = 200, message = "Liste des statuts reclamations")
		})
	@RequestMapping(
			produces = "application/json",
			value = "/statutReclamations",
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity<Object> getStatutReclamations(
			HttpServletRequest request) throws JsonProcessingException {

		return new ResponseEntity<Object>(mapperJSONService.get(null).writeValueAsString(statutReclamationService.getAll()), HttpStatus.OK);
	}
	
	

}
