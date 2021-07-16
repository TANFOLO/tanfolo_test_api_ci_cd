package com.kamtar.transport.api.controller;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.kamtar.transport.api.model.UtilisateurClient;
import com.kamtar.transport.api.params.TestOperationRecurrence;
import com.kamtar.transport.api.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.enums.TemplateSMS;
import com.kamtar.transport.api.enums.TemplateSMSLangue;
import com.kamtar.transport.api.model.SMS;
import com.kamtar.transport.api.params.TestEnvoyerSMSParams;
import com.kamtar.transport.api.params.TestErreur500;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@RestController
@EnableWebMvc
public class TestController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(TestController.class);  

	@Autowired
	SMSService smsService;

	@Autowired
	OperationService operationService;

	@Autowired
	EmailToSendService emailToSendService;

	@Autowired
	UtilisateurClientService utilisateurClientService;


	@Autowired
	MapperJSONService mapperJSONService;

	/**
	 * Test d'envoi d'un sms (appel au microservce wbc-sms)
	 * @param postBody
	 * @return
	 */
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "")
	@ApiResponses(value = {
		    @ApiResponse(code = 200, message = "", response = Boolean.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/test/email/envoyer",
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity testEnvoyerSMS(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody TestEnvoyerSMSParams postBody) {

		if (postBody.getCle() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clé vide");
		}

		if (!postBody.getCle().equals("2bqQSrTF6neT")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clé invalide");
		}


		UtilisateurClient client = utilisateurClientService.login("aurelien.caruel@gmail.com", "CI");
		emailToSendService.envoyerConfirmationCreationCompte(client, "CI");
		return new ResponseEntity<Object>(true, HttpStatus.OK);


	}


	/**
	 * Test de déclenchement d'une erreur 500
	 * @param postBody
	 * @return
	 */
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "")
	@ApiResponses(value = {
		    @ApiResponse(code = 200, message = "", response = Boolean.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/test/erreur/500", 
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity testEeereur500(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody TestErreur500 postBody) {

		if (postBody.getCle() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clé vide");
		}

		if (!postBody.getCle().equals("2bqQSrTF6neT")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clé invalide");
		}

		String a = null;
		String b = a.concat("b");
		
		return new ResponseEntity<Object>(true, HttpStatus.OK);


	}

	/**
	 * Test d"opration recurrentes
	 * @param postBody
	 * @return
	 */
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "", response = Boolean.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/test/operation/recurrente",
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity testOperationRecurrente(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody TestOperationRecurrence postBody) {

		if (postBody.getCle() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clé vide");
		}

		if (!postBody.getCle().equals("2bqQSrTF6neT")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clé invalide");
		}

		Calendar now = new GregorianCalendar();
		String d = postBody.getDate_appel();
		String[] splitted = d.split("/");
		Integer mois = Integer.valueOf(splitted[1])-1;
		now.set(Integer.valueOf(splitted[0]), mois, Integer.valueOf(splitted[2]));


		now.set(Calendar.YEAR, Integer.valueOf(splitted[0]));
		now.set(Calendar.MONTH, mois);
		now.set(Calendar.DAY_OF_MONTH, Integer.valueOf(splitted[2]));

		logger.info("now = " + now.getTime());

		// doit être fait avant le calcul du prédictif
		operationService.creerOperationsRecurrentes(now.getTime(), postBody.getNb_jours());

		return new ResponseEntity<Object>(true, HttpStatus.OK);


	}

	/**
	 * Test d"opration recurrentes
	 * @param postBody
	 * @return
	 */
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "", response = Boolean.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/test/operation/recurrente2",
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity testOperationRecurrente2(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody TestOperationRecurrence postBody) {

		if (postBody.getCle() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clé vide");
		}

		if (!postBody.getCle().equals("2bqQSrTF6neT")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clé invalide");
		}


		operationService.creerOperationsRecurrentes(new Date(), 1);

		return new ResponseEntity<Object>(true, HttpStatus.OK);


	}



	/**
	 * Test d'envoi du rapport journalier'
	 * @param postBody
	 * @return
	 */
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "", response = Boolean.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/test/operation/rapport/journalier",
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity testOperationRapportJournalier(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody TestOperationRecurrence postBody) {

		if (postBody.getCle() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clé vide");
		}

		if (!postBody.getCle().equals("2bqQSrTF6neT")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clé invalide");
		}

		Calendar now = new GregorianCalendar();
		String d = postBody.getDate_appel();
		String[] splitted = d.split("/");
		Integer mois = Integer.valueOf(splitted[1])-1;
		now.set(Integer.valueOf(splitted[0]), mois, Integer.valueOf(splitted[2]));


		now.set(Calendar.YEAR, Integer.valueOf(splitted[0]));
		now.set(Calendar.MONTH, mois);
		now.set(Calendar.DAY_OF_MONTH, Integer.valueOf(splitted[2]));

		logger.info("now = " + now.getTime());

		// doit être fait avant le calcul du prédictif
		operationService.listeOperationsRapportJournalier(now.getTime());

		return new ResponseEntity<Object>(true, HttpStatus.OK);


	}


	/**
	 * Test d'envoi du rapport mensuel
	 * @param postBody
	 * @return
	 */
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "", response = Boolean.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/test/operation/rapport/mensuel",
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity testOperationRapportMensuel(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody TestOperationRecurrence postBody) {

		if (postBody.getCle() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clé vide");
		}

		if (!postBody.getCle().equals("2bqQSrTF6neT")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clé invalide");
		}

		Calendar now = new GregorianCalendar();
		String d = postBody.getDate_appel();
		String[] splitted = d.split("/");
		Integer mois = Integer.valueOf(splitted[1])-1;
		now.set(Integer.valueOf(splitted[0]), mois, Integer.valueOf(splitted[2]));


		now.set(Calendar.YEAR, Integer.valueOf(splitted[0]));
		now.set(Calendar.MONTH, mois);
		now.set(Calendar.DAY_OF_MONTH, Integer.valueOf(splitted[2]));

		logger.info("now = " + now.getTime());

		// doit être fait avant le calcul du prédictif
		operationService.listeOperationsRapportMensuel(now.getTime());

		return new ResponseEntity<Object>(true, HttpStatus.OK);


	}


}
