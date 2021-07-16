package com.kamtar.transport.api.controller;

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.enums.UtilisateurTypeDeCompte;
import com.kamtar.transport.api.model.*;
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
import com.kamtar.transport.api.params.MotDePassePerduChangerParams;
import com.kamtar.transport.api.params.MotDePassePerduParams;
import com.kamtar.transport.api.utils.JWTProvider;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@Api(value="Gestion des mots de passe perdu", description="API Rest qui gère l'ensemble des mots de passe perdu")
@RestController
@EnableWebMvc
public class MotDePassePerduController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(MotDePassePerduController.class);  

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	MapperJSONService mapperJSONService;

	@Autowired
	ActionAuditService actionAuditService;

	@Autowired
	MotDePassePerduService motDePassePerduService;

	@Autowired
	UtilisateurDriverService utilisateurTransporteurService;

	@Autowired
	UtilisateurProprietaireService utilisateurProprietaireService;

	@Autowired
	UtilisateurClientPersonnelService utilisateurClientPersonnelService;

	@Autowired
	ClientService clientService;
	
	@Autowired
	UtilisateurClientService utilisateurClientService;

	@Autowired
	UtilisateurAdminKamtarService utilisateurAdminKamtarService;

	@Autowired
	UtilisateurOperateurKamtarService utilisateurOperateurKamtarService;

	/**
	 * Création d'une demande de mot de passe perdu par un utilisateur a oublié son mot de passe
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Création d'une demande de mot de passe perdu par un utilisateur a oublié son mot de passe")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Impossible de gérer la demande de mot de passe perdu."),
			@ApiResponse(code = 404, message = "Impossible de trouver l'utilisateur"),
		    @ApiResponse(code = 201, message = "Demande de mot de passe perdu créé", response = MotDePassePerdu.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/*/motdepasse/perdu", 
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity contact(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody MotDePassePerduParams postBody) throws JsonProcessingException {

		UtilisateurDriver transporteur = null;
		UtilisateurProprietaire proprietaire = null;
		UtilisateurOperateurKamtar operateur_kamtar = null;
		UtilisateurAdminKamtar admin = null;
		UtilisateurClient client = null;
		UtilisateurClientPersonnel client_personnel = null;

		if (postBody.getType_compte().equals("driver")) {
			transporteur = utilisateurTransporteurService.login(postBody.getLogin(), postBody.getCode_pays());
		} else if (postBody.getType_compte().equals("proprietaire")) {
			proprietaire = utilisateurProprietaireService.login(postBody.getLogin(), postBody.getCode_pays());
		} else if (postBody.getType_compte().equals("admin_operateur")) {
			operateur_kamtar = utilisateurOperateurKamtarService.login(postBody.getLogin(), postBody.getCode_pays());
			admin = utilisateurAdminKamtarService.login(postBody.getLogin(), postBody.getCode_pays());
		} else if (postBody.getType_compte().equals("client")) {
			client = utilisateurClientService.login(postBody.getLogin(), postBody.getCode_pays());
			client_personnel = utilisateurClientPersonnelService.login(postBody.getLogin(), postBody.getCode_pays());
		}

		if (transporteur == null && operateur_kamtar == null && client == null && admin == null && client_personnel == null && proprietaire == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'utilisateur.");
		}

		MotDePassePerdu mot_de_passe_perdu = motDePassePerduService.create(operateur_kamtar, transporteur, client, admin, client_personnel, proprietaire, postBody.getCode_pays());

		if (mot_de_passe_perdu != null) {
			
			String uuid_utilisateur = null;
			if (transporteur != null) {
				uuid_utilisateur = transporteur.getUuid().toString();
			} else if (operateur_kamtar != null) {
				uuid_utilisateur = operateur_kamtar.getUuid().toString();
			} else if (client != null) {
				uuid_utilisateur = client.getUuid().toString();
			} else if (client_personnel != null) {
				uuid_utilisateur = client_personnel.getUuid().toString();
			}

			
			actionAuditService.motDePassePerdu(uuid_utilisateur, mot_de_passe_perdu, postBody.getLogin(), postBody.getCode_pays());
			
			mot_de_passe_perdu.setEnvoi("email");

			return new ResponseEntity<>(mapperJSONService.get(null).writeValueAsString(mot_de_passe_perdu), HttpStatus.CREATED);

		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible de gérer la demande de mot de passe perdu.");

	}
	
	
	/**
	 * Un utilisateur veut modifier son mot de passe suite à l'oubli du mot de passe
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Un utilisateur veut modifier son mot de passe suite à l'oubli du mot de passe")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "La demande de modification de mot de passe est introuvable."),
			@ApiResponse(code = 403, message = "La demande de modification de mot de passe a déjà été réalisée."),
			@ApiResponse(code = 400, message = "La demande de modification de mot de passe a déjà été réalisée."),
			@ApiResponse(code = 400, message = "Impossible de modifier votre mot de passe."),
			@ApiResponse(code = 400, message = "Les deux mots de passe ne sont pas identitiques"),
		    @ApiResponse(code = 404, message = "La demande de mot de passe est introuvable"),
		    @ApiResponse(code = 200, message = "Token pour s'identifier", response = JSONObject.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/*/motdepasse/modifier", 
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity modifier(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody MotDePassePerduChangerParams postBody) throws JsonProcessingException {
		
		Boolean mot_de_passe_change = motDePassePerduService.changerMotDePasser(postBody.getToken(), postBody.getNouveau_mot_de_passe(), postBody.getCode_pays());

		if (mot_de_passe_change) {
			
			String token = null;
			
			// modifie le mot de passe de l'utilisateur
			MotDePassePerdu mdp = motDePassePerduService.charger(postBody.getToken(), postBody.getCode_pays());
			if (mdp.getOperateurKamtar() != null) {
				if (!mdp.getOperateurKamtar().isActivate()) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Votre compte est désactivé");
				}
				token = jwtProvider.createJWT(mdp.getOperateurKamtar(), null);
				actionAuditService.loginOperateurKamtar((UtilisateurOperateurKamtar)mdp.getOperateurKamtar(), postBody.getCode_pays());
			} else if (mdp.getTransporteur() != null) {
				// si c'est un transporteur on ne le conecte pas -car il devra choisir son véhicule) => donc pas de génération de token
				if (!mdp.getTransporteur().isActivate()) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Votre compte est désactivé");
				}
				//token = jwtProvider.createJWT(mdp.getTransporteur(), null);
				actionAuditService.loginTransporteur((UtilisateurDriver)mdp.getTransporteur(), postBody.getCode_pays());
			} else if (mdp.getProprietaire() != null) {
				if (!mdp.getProprietaire().isActivate()) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Votre compte est désactivé");
				}
				//token = jwtProvider.createJWT(mdp.getProprietaire(), null);
				actionAuditService.loginProprietaire((UtilisateurProprietaire) mdp.getProprietaire(), postBody.getCode_pays());
			} else if (mdp.getClient() != null) {
				if (!mdp.getClient().isActivate()) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Votre compte est désactivé");
				}
				
				Client client = clientService.getByUtilisateur(mdp.getClient(), postBody.getCode_pays());
				if ((client == null)) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "L'utilisateur n'est attaché à aucun expéditeur");
				}

				List<String> additional_informations = Arrays.asList(new String[]{mdp.getClient().getNom(), client.getUuid().toString(), client.getTypeCompte(), client.getNom()});
				token = jwtProvider.createJWT(mdp.getClient(), additional_informations);
				actionAuditService.loginClient((UtilisateurClient)mdp.getClient(), postBody.getCode_pays());
			} else if (mdp.getClientPersonnel() != null) {
				if (!mdp.getClientPersonnel().isActivate()) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Votre compte est désactivé");
				}

				UtilisateurClientPersonnel client_personnel = mdp.getClientPersonnel();
				if ((client_personnel == null)) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Impossible de charger le compte");
				}

				List<String> additional_informations = Arrays.asList(new String[]{client_personnel.getNom(), client_personnel.getClient().getUuid().toString(), UtilisateurTypeDeCompte.EXPEDITEUR_PERSONNEL.toString(), client_personnel.getNom(), client_personnel.getListe_droits()});
				token = jwtProvider.createJWT(client_personnel, additional_informations);
				actionAuditService.loginClientPersonnel(client_personnel, postBody.getCode_pays());
			}else if (mdp.getAdminKamtar() != null) {
				if (!mdp.getAdminKamtar().isActivate()) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Votre compte est désactivé");
				}
				token = jwtProvider.createJWT(mdp.getAdminKamtar(), null);
				actionAuditService.loginAdminKamtar((UtilisateurAdminKamtar)mdp.getAdminKamtar(), postBody.getCode_pays());
			}
			
			JSONObject res = new JSONObject();
			res.put("token", token);

			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(res), HttpStatus.OK);
			
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible de modifier votre mot de passe.");

	}


}
