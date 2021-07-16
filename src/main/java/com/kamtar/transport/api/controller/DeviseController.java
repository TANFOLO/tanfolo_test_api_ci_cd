package com.kamtar.transport.api.controller;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.sockets.classes.Message;
import com.kamtar.transport.api.swagger.ListDevise;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Api(value="Gestion des devises", description="API Rest qui gère l'ensemble des devises")
@RestController
@EnableWebMvc
public class DeviseController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(DeviseController.class);  

	@Autowired
	DeviseService deviseService;

	@Autowired
	WeatherAPIService weatherAPIService;

	@Autowired
	DirectionAPIService directionAPIService;

	@Autowired
	MapperJSONService mapperJSONService;

	@Autowired
	OperationService operationService;
	
	/**
	 * Récupére les devises
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupére les devises")
	@ApiResponses(value = {
		    @ApiResponse(code = 200, message = "Liste des devises")
		})
	@RequestMapping(
			produces = "application/json",
			value = "/devises", 
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity<Object> getDevises(
			HttpServletRequest request) throws JsonProcessingException {


		//operationService.refreshPredictifEveryDay();
		//operationService.refreshPredictifEveryHour();

		return new ResponseEntity<Object>(mapperJSONService.get(null).writeValueAsString(deviseService.getAll()), HttpStatus.OK);
	}
	
	

}
