package com.kamtar.transport.api.controller;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.*;
import com.kamtar.transport.api.service.*;
import com.kamtar.transport.api.swagger.ListOperateursKamtar;
import com.kamtar.transport.api.utils.ExportUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.criteria.ClientSpecificationsBuilder;
import com.kamtar.transport.api.criteria.ParentSpecificationsBuilder;
import com.kamtar.transport.api.enums.OperateurListeDeDroits;
import com.kamtar.transport.api.security.SecurityUtils;
import com.kamtar.transport.api.swagger.ListClient;
import com.kamtar.transport.api.swagger.MapDatatable;
import com.kamtar.transport.api.utils.DatatableUtils;
import com.kamtar.transport.api.utils.JWTProvider;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@Api(value="Gestion des clients", description="API Rest qui gère l'ensemble des clients")
@RestController
@EnableWebMvc
public class ClientController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(ClientController.class);

	@Autowired
	OperationService operationService;

	@Autowired
    ClientService clientService;

	@Autowired
	ExportExcelService exportExcelService;

	@Autowired
	EmailToSendService emailToSendService;

	@Autowired
	MapperJSONService mapperJSONService;

	@Autowired
	UtilisateurOperateurKamtarService utilisateurOperateurKamtarService;

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	ActionAuditService actionAuditService;

	@Autowired
	UtilisateurClientService utilisateurClientService;

	/**
	 * Création d'un client
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Création d'un client")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "L'adresse e-mail est déjà utilisée."),
			@ApiResponse(code = 400, message = "Le numéro de téléphone est déjà utilisé."),
			@ApiResponse(code = 400, message = "Impossible d'enregistrer le client."),
		    @ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
		    @ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
		    @ApiResponse(code = 201, message = "Client créé", response = Client.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/client", 
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity create(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody CreateClientParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que 
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}
		
		// vérifie que les droits lui permettent de lire les expéditeurs
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_CLIENTS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}
		
		// enregistrement
		Client client = clientService.create(postBody, token);
		if (client != null) {
			actionAuditService.creerClient(client, token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(client), HttpStatus.CREATED);
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible d'enregistrer le client.");

	}
	
	/**
	 * Création d'un client par un visiteur anonyme
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Création d'un client par un visiteur anonyme")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Vous devez renseigner le nom de l'entreprise."),
			@ApiResponse(code = 400, message = "L'adresse e-mail est déjà utilisée."),
			@ApiResponse(code = 400, message = "Le numéro de téléphone est déjà utilisé."),
			@ApiResponse(code = 400, message = "Impossible d'enregistrer le client."),
		    @ApiResponse(code = 201, message = "Client créé", response = Client.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/client2", 
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity create2(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody CreateClientAnonymeParams postBody) throws JsonProcessingException {
		
		// enregistrement
		Client client = clientService.create(postBody);
		if (client != null) {
			actionAuditService.creerClient(client, postBody.getCode_pays());
			return new ResponseEntity<>(mapperJSONService.get(null).writeValueAsString(client), HttpStatus.CREATED);
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible d'enregistrer le client.");

	}


	/**
	 * Validation du client
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Validation du client")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Impossible de valider le compte client."),
			@ApiResponse(code = 201, message = "Client validé", response = Client.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/client/validation",
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity validation(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody ValidationClientParams postBody) throws JsonProcessingException {

		// enregistrement
		UtilisateurClient client = clientService.getByValidationCode(postBody.getValidation(), postBody.getTelephone(), postBody.getPays());
		if (client == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible de trouver le compte client.");
		}
		if (client.isActivate()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Votre compte est déjà activé.");
		}
		client.setActivate(true);

		// envoi d'un email de confirmation de création du compte
		emailToSendService.envoyerConfirmationCreationCompte(client, client.getCodePays());

		actionAuditService.validerClient(client, postBody.getPays());
		return new ResponseEntity<>(mapperJSONService.get(null).writeValueAsString(client), HttpStatus.OK);


	}

	/**
	 * Autocompletion des clients
	 * @param postBody
	 * @return
	 */
	/*@RequestLogger(name = Application.microservice)*/
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Autocompletion des clients")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
		    @ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
		    @ApiResponse(code = 200, message = "Liste des clients", response = ListClient.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/clients/autocompletion", 
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity autocompletion(
			@ApiParam(value = "Partie du nom de l'expéditeur à rechercher", required = true) @RequestParam("query") String query, 
			@ApiParam(value = "Jeton JWT pour autentification", required = true) @RequestParam("token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de lire les expéditeurs
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_CLIENTS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		List<Client> clients = clientService.autocomplete(query, jwtProvider.getCodePays(token));

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(clients), HttpStatus.OK);

	}


	/**
	 * Si c'est un client pro, vérifie que les infos obligatoires sont renseignés
	 * @param postBody
	 * @return
	 */
	/*@RequestLogger(name = Application.microservice)*/
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Si c'est un client pro, vérifie que les infos obligatoires sont renseignés")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 200, message = "false si c'est un client pro et que ses informations sont renseignés, true sinon", response = Boolean.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/client/verification",
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity verifierInfosClientsPro(
			@ApiParam(value = "UUID du client", required = true) @RequestParam("uuid") String uuid,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) {

		// vérifie que le jeton est valide
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de lire les expéditeurs
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_CLIENTS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		Client client = clientService.getByUUID(uuid, jwtProvider.getCodePays(token));
		if (client.getTypeCompte().equals("B")) {
			if (client.getCompteContribuable() == null || client.getCompteContribuable().equals("")) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attention! Le compte contribuable du client n'est pas renseigné !");
			} else if (client.getNumeroRCCM() == null || client.getNumeroRCCM().equals("")) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attention! Le numéro RCCM du client n'est pas renseigné !");
			} else if (client.getDelais() == null || client.getDelais().equals("")) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attention! Le délais de paiement n'est pas renseigné !");
			}
		}

		return new ResponseEntity<>(true,  HttpStatus.OK);

	}


	/**
	 * Modification d'un client
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Modification d'un client")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "L'adresse e-mail est déjà utilisée."),
			@ApiResponse(code = 400, message = "Le numéro de téléphone est déjà utilisé."),
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
		    @ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
		    @ApiResponse(code = 404, message = "Impossible de trouver le client"),
		    @ApiResponse(code = 200, message = "Client mis à jour", response = Client.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/client", 
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity edit(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody EditClientParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de modifier un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer un expéditeur
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_CLIENTS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// chargement
		Client client = clientService.getByUUID(postBody.getId(), jwtProvider.getCodePays(token));
		if (client == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le client.");
		}
		clientService.update(postBody, client, jwtProvider.getCodePays(token));

		
		actionAuditService.editerClient(client, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(client), HttpStatus.OK);

	}


	/**
	 * Modification de son compte par un client
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Modification de son compte par un client")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "L'adresse e-mail est déjà utilisée."),
			@ApiResponse(code = 400, message = "Le numéro de téléphone est déjà utilisé."),
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver le client"),
			@ApiResponse(code = 200, message = "Client mis à jour", response = Client.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/client2",
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity edit2(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody EditClientPublicParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de modifier un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer un expéditeur
		if (!SecurityUtils.client(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);

		// chargement
		Client client = clientService.getByUUID(uuid_client.toString(), jwtProvider.getCodePays(token));
		if (client == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le client.");
		}
		clientService.update(postBody, client, jwtProvider.getCodePays(token));


		actionAuditService.editerClient(client, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(client), HttpStatus.OK);

	}
	
	/**
	 * Suppression d'un client
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Suppression d'un client")
	@ApiResponses(value = {
		    @ApiResponse(code = 400, message = "Il reste au moins une opération qui référence un client"),
		    @ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
		    @ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
		    @ApiResponse(code = 404, message = "Impossible de trouver le client"),
		    @ApiResponse(code = 200, message = "Client supprimé", response = Boolean.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/client", 
			method = RequestMethod.DELETE)
	@CrossOrigin(origins="*")
	public ResponseEntity delete(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody DeleteClientParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de modifier un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer un expéditeur
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_CLIENTS) || !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.SUPPRESSION_CLIENT)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// chargement
		Client client = clientService.getByUUID(postBody.getId(), jwtProvider.getCodePays(token));
		if (client == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le client.");
		}

		// est ce qu'il y a des opérations attachées à ce client ?
		long nb = operationService.countOperationsClient(client, jwtProvider.getCodePays(token));
		if (nb > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas supprimer ce client car il est attaché à au moins une opération.");

		}

		clientService.delete(client, jwtProvider.getCodePays(token));
		actionAuditService.deleteClient(client, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(client), HttpStatus.OK);

	}

	/**
	 * Récupère les informations d'un client
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère les informations d'un client")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
		    @ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, expéditeur ayant le droit ou opérateur ayant le droit)"),
		    @ApiResponse(code = 404, message = "Impossible de trouver le client"),
		    @ApiResponse(code = 200, message = "Client demandé", response = Client.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/client", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity get(
			@ApiParam(value = "UUID de l'expéditeur", required = true) @RequestParam("uuid") String uuid,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher un point expéditeur
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_CLIENTS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_CLIENTS)  && (!SecurityUtils.client(jwtProvider, token))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// chargement d'un expéditeur si c'ets un admin ou de soit même si l'utilisateur connecté est un epxéditeur
		Client client = null;
		if (SecurityUtils.admin(jwtProvider, token) || SecurityUtils.operateur(jwtProvider, token)) {
			client = clientService.getByUUID(uuid, jwtProvider.getCodePays(token));
		} else if (SecurityUtils.client(jwtProvider, token)) {
			client = clientService.getByUUID(jwtProvider.getClaimsValue("uuid_client", token), jwtProvider.getCodePays(token));
		}
		
		if (client != null) {
			actionAuditService.getClient(client, token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(client), HttpStatus.OK);
		}

		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le client.");
	}


	/**
	 * Liste de tous les clients
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste de tous les clients")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
		    @ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
		    @ApiResponse(code = 200, message = "Liste de tous les clients (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/clients", 
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_offres(
			@ApiParam(value = "Critères de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de consulter les opérateurs katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher un expéditeur
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_CLIENTS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_CLIENTS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// paramètres du datatable
		String draw 		= postBody.getFirst("draw").toString();
		Integer length 		= Integer.valueOf(postBody.getFirst("length").toString());

		// tri, sens et numéro de page
		String colonneTriDefaut = "createdOn";
		List<String> colonnesTriAutorise = Arrays.asList("createdOn", "prenom", "nom", "contactEmail", "contactNumeroDeTelephone1", "typeCompte");

		String order_column_bdd = DatatableUtils.getOrderColonne(colonneTriDefaut, colonnesTriAutorise, postBody);
		String sort_bdd = DatatableUtils.getSort(postBody);
		Integer numero_page = DatatableUtils.getNumeroPage(postBody, length);

		// filtrage
		List<String> colonnesFiltrageActive = Arrays.asList("nom", "typeCompte");
		ParentSpecificationsBuilder builder = new ClientSpecificationsBuilder();
		Specification spec = DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, null);
		if (spec == null) {
			spec = Specification.where(DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, null));
		}

		// filtrage par date de création
		spec = DatatableUtils.fitrageDate(spec, postBody, "createdOn", "createdOn");

		// filtre sur le numro de téléphone
		Map<Integer, String> indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		Integer position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, "utilisateur.numeroTelephone1");
		if (position_colonne != null) {
			String filtre_par_expediteur = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			if (filtre_par_expediteur != null && !"".equals(filtre_par_expediteur.trim())) {
				Specification spec2 = new Specification<Client>() {
					public Predicate toPredicate(Root<Client> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						predicates.add(builder.like(root.get("utilisateur").get("numeroTelephone1"), "%" + filtre_par_expediteur + "%"));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec = spec.and(spec2);
			}
		}

		// filtre sur l'email
		indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, "utilisateur.email");
		if (position_colonne != null) {
			String filtre_par_expediteur = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			if (filtre_par_expediteur != null && !"".equals(filtre_par_expediteur.trim())) {
				Specification spec2 = new Specification<Client>() {
					public Predicate toPredicate(Root<Client> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						predicates.add(builder.like(root.get("utilisateur").get("email"), "%" + filtre_par_expediteur + "%"));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec = spec.and(spec2);
			}
		}


		// filtre sur l'activation
		indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, "utilisateur.activate");
		if (position_colonne != null) {
			String filtre_par_expediteur = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			if (filtre_par_expediteur != null && !"".equals(filtre_par_expediteur.trim())) {
				Specification spec2 = new Specification<Client>() {
					public Predicate toPredicate(Root<Client> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						predicates.add(builder.equal(root.get("utilisateur").get("activate"), "1".equals(filtre_par_expediteur)));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec = spec.and(spec2);
			}
		}

		// filtrage par pays kamtar
		Specification spec_pays = new Specification<Client>() {
			public Predicate toPredicate(Root<Client> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get("codePays"), jwtProvider.getCodePays(token)));
				return builder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
		spec = spec.and(spec_pays);

		// préparation les deux requêtes (résultat et comptage)
		Page<Client> leads = clientService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);
		Long total = clientService.countAll(spec);

		
		actionAuditService.getClients(token);

		// prépare les résultast
		JSONArray jsonArrayOffres = new JSONArray();
		if (leads != null) {
			jsonArrayOffres.addAll(leads.getContent());
		}
		Map<String, Object> jsonDataResults = new LinkedHashMap<String, Object>();
		jsonDataResults.put("draw", draw);	
		jsonDataResults.put("recordsTotal", total);		
		jsonDataResults.put("recordsFiltered", total);	
		jsonDataResults.put("data", jsonArrayOffres);		

		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(jsonDataResults), HttpStatus.OK);
	}



	/**
	 * Liste de tous les clients
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste de tous les clients")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 200, message = "Liste de tous les clients", response = ListClient.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/clients/liste",
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_offres(
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de consulter les opérateurs katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher un expéditeur
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_CLIENTS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_CLIENTS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}


		String order_column_bdd = "createdOn";
		String sort_bdd = "asc";

		ParentSpecificationsBuilder builder = new ClientSpecificationsBuilder();
		Specification spec = DatatableUtils.buildFiltres(null, null, builder, null);
		if (spec == null) {
			spec = Specification.where(DatatableUtils.buildFiltres(null, null, builder, null));
		}


		// filtrage par pays kamtar
		Specification spec_pays = new Specification<Client>() {
			public Predicate toPredicate(Root<Client> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get("codePays"), jwtProvider.getCodePays(token)));
				return builder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
		spec = spec.and(spec_pays);

		// préparation les deux requêtes (résultat et comptage)
		Page<Client> leads = clientService.getAllPagined(order_column_bdd, sort_bdd, 0, 999999, spec);

		// audit
		actionAuditService.getClients(token);

		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(leads.getContent()), HttpStatus.OK);
	}




	/**
	 * Export des clients kamtar
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Export des clients kamtar")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (reservés aux admins ou opérateurs ayant droit)"),
			@ApiResponse(code = 200, message = "CSV des clients", response = ListClient.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/clients/export",
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity<byte[]> export(
			@ApiParam(value = "Date de début", required = true) @RequestParam("date_debut") String date_debut,
			@ApiParam(value = "Date de fin", required = true) @RequestParam("date_fin") String date_fin,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestParam("Token") String token) throws Exception {

		// vérifie que le jeton est valide et que les droits lui permettent de consulter les admin katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.admin(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		Date dateDebut = new SimpleDateFormat("yyyy-MM-dd").parse(date_debut);
		Date dateFin = new SimpleDateFormat("yyyy-MM-dd").parse(date_fin);

		Calendar dateMin = new GregorianCalendar();
		dateMin.setTime(dateDebut);
		dateMin.set(Calendar.HOUR_OF_DAY, 0);
		dateMin.set(Calendar.MINUTE, 0);
		dateMin.set(Calendar.SECOND, 0);

		Calendar dateMax = new GregorianCalendar();
		dateMax.setTime(dateFin);
		dateMax.set(Calendar.HOUR_OF_DAY, 23);
		dateMax.set(Calendar.MINUTE, 59);
		dateMax.set(Calendar.SECOND, 59);


		String order_column_bdd = "createdOn";
		String sort_bdd = "asc";
		Integer numero_page = 0;
		Integer length = 999999;

		// filtrage par administrateur
		Specification spec = new Specification<ActionAudit>() {
			public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.greaterThanOrEqualTo(root.get(order_column_bdd), dateMin.getTime()));
				predicates.add(builder.lessThanOrEqualTo(root.get(order_column_bdd), dateMax.getTime()));
				return builder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};

		// filtrage par pays kamtar
		Specification spec_pays = new Specification<ActionAudit>() {
			public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get("codePays"), jwtProvider.getCodePays(token)));
				return builder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
		spec = spec.and(spec_pays);

		// préparation les deux requêtes (résultat et comptage)
		Page<Client> leads = clientService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);

		// convertion liste en excel
		byte[] bytesArray = exportExcelService.export(leads, null);

		// audit
		actionAuditService.exportClients(token);

		return ResponseEntity.ok()
				//.headers(headers) // add headers if any
				.contentLength(bytesArray.length)
				.contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
				.body(bytesArray);
	}


}
