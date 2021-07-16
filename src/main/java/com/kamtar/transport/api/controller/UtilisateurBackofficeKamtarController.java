package com.kamtar.transport.api.controller;

import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.model.Utilisateur;
import com.kamtar.transport.api.model.UtilisateurAdminKamtar;
import com.kamtar.transport.api.model.UtilisateurOperateurKamtar;
import com.kamtar.transport.api.params.SigninParams;
import com.kamtar.transport.api.utils.JWTProvider;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.ArrayList;
import java.util.List;


@Api(value="Gestion de la connexion au backoffice", description="API Rest qui gère la connexion au backoffice (opérateur et administrateur)")
@RestController
@EnableWebMvc
public class UtilisateurBackofficeKamtarController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(UtilisateurBackofficeKamtarController.class);  

	@Autowired
	CountryService countryService;

	@Autowired
	MapperJSONService mapperJSONService;

	@Autowired
	UtilisateurAdminKamtarService utilisateurAdminKamtarService;

	@Autowired
	UtilisateurOperateurKamtarService utilisateurOperateurKamtarService;

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	ActionAuditService actionAuditService;


	/**
	 * Connexion d'une admin ou opérateur kamtar (au backoffice) 
	 * @param postBody
	 * @return
	 */
	/*@RequestLogger(name = Application.microservice)*/
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Connexion d'une admin ou opérateur kamtar (au backoffice)")
	@ApiResponses(value = {
		    @ApiResponse(code = 403, message = "Votre compte est désactivé"),
		    @ApiResponse(code = 200, message = "Token d'autentification", response = JSONObject.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/signin", 
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity login(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody SigninParams postBody) throws JsonProcessingException {

		Utilisateur utilisateur_admin = utilisateurAdminKamtarService.login(postBody.getLogin(), postBody.getMot_de_passe(), postBody.getPays());
		Utilisateur utilisateur_operateur = utilisateurOperateurKamtarService.login(postBody.getLogin(), postBody.getMot_de_passe(), postBody.getPays());
		if (utilisateur_admin == null && utilisateur_operateur == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Veuillez vérifier vos identifiants");
		} 
		
		// est ce que le compte est désactivé
		if ((utilisateur_admin != null && utilisateur_admin.isActivate() == false) || (utilisateur_operateur != null && utilisateur_operateur.isActivate() == false)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Votre compte est désactivé");
		}

		if (utilisateur_admin != null) {
			actionAuditService.loginAdminKamtar((UtilisateurAdminKamtar)utilisateur_admin, postBody.getPays());
		} else if (utilisateur_operateur != null) {
			actionAuditService.loginOperateurKamtar((UtilisateurOperateurKamtar)utilisateur_operateur, postBody.getPays());
		}
		
		
		String token = jwtProvider.createJWT(utilisateur_admin != null ? utilisateur_admin: utilisateur_operateur, null);

		JSONObject res = new JSONObject();
		res.put("token", token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(res), HttpStatus.OK);

	}




}
