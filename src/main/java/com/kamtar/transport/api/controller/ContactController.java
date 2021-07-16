package com.kamtar.transport.api.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.kamtar.transport.api.enums.UtilisateurTypeDeCompte;
import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.repository.UtilisateurDriverRepository;
import com.kamtar.transport.api.repository.UtilisateurProprietaireRepository;
import com.kamtar.transport.api.service.*;
import com.kamtar.transport.api.swagger.ListPays;
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
import com.kamtar.transport.api.params.ContactParams;
import com.kamtar.transport.api.utils.JWTProvider;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.UUID;


@Api(value="Gestion de l'envoi de message de contact par un utilisateur à Kamtar", description="API Rest qui gère l'envoi de messages")
@RestController
@EnableWebMvc
public class ContactController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(ContactController.class);  

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	ActionAuditService actionAuditService;

	@Autowired
	MapperJSONService mapperJSONService;

	@Autowired
	ContactService contactService;

	@Autowired
	ClientService clientService;

	@Autowired
	SMSService smsService;

	@Autowired
	UtilisateurDriverRepository transporteurRepository;

	@Autowired
	UtilisateurProprietaireRepository utilisateurProprietaireRepository;



	/**
	 * Envoi d'un message à Kamtar
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Envoi un message par email à Kamtar")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Impossible d'enregistrer votre message."),
		    @ApiResponse(code = 201, message = "true", response = Boolean.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/contact", 
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity contact(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody ContactParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = false, allowEmptyValue = true)  @RequestHeader("Token") String token) {

		Utilisateur utilisateur = null;
		if (jwtProvider.isValidJWT(token)) {
			String type_compte = jwtProvider.getTypeDeCompte(token);
			if (UtilisateurTypeDeCompte.DRIVER.equals(type_compte)) {
				UUID uuid_transporteur = jwtProvider.getUUIDFromJWT(token);
				utilisateur = transporteurRepository.findByUUID(uuid_transporteur, jwtProvider.getCodePays(token));
			} else if (UtilisateurTypeDeCompte.EXPEDITEUR.equals(type_compte)) {
				Client client = clientService.getByUUID(jwtProvider.getClaimsValue("uuid_client", token), jwtProvider.getCodePays(token));
				utilisateur = client.getUtilisateur();
			} else if (UtilisateurTypeDeCompte.PROPRIETAIRE.equals(type_compte)) {
				UUID uuid_proprietaire = jwtProvider.getUUIDFromJWT(token);
				utilisateur = utilisateurProprietaireRepository.findByUUID(uuid_proprietaire, jwtProvider.getCodePays(token));

			}

		}

		Contact contact = contactService.create(postBody, utilisateur);
		
		if (contact != null) {
			actionAuditService.contact(contact, utilisateur, null);
			return new ResponseEntity<>(true, HttpStatus.CREATED);
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible d'enregistrer votre message.");

	}




}
