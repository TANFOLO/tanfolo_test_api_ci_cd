package com.kamtar.transport.api.controller;

import java.io.*;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kamtar.transport.api.enums.*;
import com.kamtar.transport.api.mixin.OperationMixin_Client;
import com.kamtar.transport.api.mixin.UtilisateurDriverMixin_Client;
import com.kamtar.transport.api.mixin.UtilisateurProprietaireMixin_Client;
import com.kamtar.transport.api.mixin.VehiculeMixin_Client;
import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.*;
import com.kamtar.transport.api.repository.GeolocRepository;
import com.kamtar.transport.api.repository.UtilisateurClientPersonnelRepository;
import com.kamtar.transport.api.service.*;
import com.kamtar.transport.api.swagger.*;
import com.kamtar.transport.api.utils.*;
import com.wbc.core.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.criteria.OperationSpecificationsBuilder;
import com.kamtar.transport.api.criteria.ParentSpecificationsBuilder;
import com.kamtar.transport.api.criteria.PredicateUtils;
import com.kamtar.transport.api.repository.UtilisateurDriverRepository;
import com.kamtar.transport.api.repository.UtilisateurProprietaireRepository;
import com.kamtar.transport.api.security.SecurityUtils;
import com.wbc.core.utils.FileUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@Api(value="Gestion des operations", description="API Rest qui gère l'ensemble des operations")
@RestController
@EnableWebMvc
public class OperationController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(OperationController.class);


	@Autowired
	private UtilisateurClientPersonnelRepository utilisateurClientPersonnelRepository;

	@Autowired
	private GeolocRepository geolocRepository;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	MapperJSONService mapperJSONService;

	@Autowired
	EmailToSendService emailToSendService;

	@Autowired
	FactureClientService factureClientService;

	@Autowired
	ExportExcelService exportExcelService;

	@Autowired
	UtilisateurClientService utilisateurClientService;

	@Autowired
	FactureProprietaireService factureProprietaireService;

	@Autowired
	CountryService countryService;

	@Autowired
	UtilisateurProprietaireService utilisateurProprietaireService;

	@Autowired
	VehiculeService vehiculeService;

	@Autowired
	OperationDocumentService operationDocumentService;

	@Autowired
	OperationAppelOffreService operationAppelOffreService;

	@Autowired
	OperationService operationService;

	@Autowired
	ClientService clientService;

	@Autowired
	UtilisateurDriverRepository transporteurRepository;

	@Autowired
	UtilisateurProprietaireRepository proprietaireRepository;

	@Autowired
	UtilisateurOperateurKamtarService utilisateurOperateurKamtarService;

	@Autowired
	NotificationService notificationService;

	@Autowired
	UtilisateurClientService clientExpediteurService;

	@Autowired
	UtilisateurClientPersonnelService utilisateurClientPersonnelService;

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	ActionAuditService actionAuditService;

	@Autowired
	UtilisateurDriverService utilisateurTransporteurService;

	@Autowired
	OperationChangementStatutService operationChangementStatutService;

	/**
	 * Récupére les types d'opération (pour TVA)
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupére les types d'opération (pour TVA)")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Liste des types d'opération")
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operation/type_tva",
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity<Object> getCountries(
			HttpServletRequest request) throws JsonProcessingException {

		List<String> enumNames = Stream.of(OperationTypeTVA.values())
				.map(Enum::name)
				.collect(Collectors.toList());

		return new ResponseEntity<Object>(mapperJSONService.get(null).writeValueAsString(enumNames), HttpStatus.OK);
	}

	/**
	 * Retourne les langues dispo pour le disponibles de l'opération (pour sélectionne le template de SMS dans la bonne langue)
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Retourne les langues disponibles pour le destinataire de l'opération (pour sélectionne le template de SMS dans la bonne langue)")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Liste des langues disponibles", response = ListLangue.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operation/destinataire/langues", 
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity<Object> getLanguesDestinataires(
			HttpServletRequest request) throws JsonProcessingException {
		List<String> languesSMS = Stream.of(TemplateSMSLangue.values()).map(Enum::name).collect(Collectors.toList());
		return new ResponseEntity<Object>(mapperJSONService.get(null).writeValueAsString(languesSMS), HttpStatus.OK);
	}

	/**
	 * Création d'une operation par un opérateur
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Création d'une operation par un opérateur")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Vous devez indiquer un transporteur pour valider l'opération"),
			@ApiResponse(code = 400, message = "Vous devez indiquer le client pour valider l'opération"),
			@ApiResponse(code = 400, message = "Vous devez indiquer le véhicule pour valider l'opération"),
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 400, message = "La date d'arrivée prévue doit être postérieure à la date de départ"),
			@ApiResponse(code = 400, message = "Impossible d'enregistrer l'opération."),
			@ApiResponse(code = 201, message = "Operation créée", response = Operation.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/operation", 
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity create(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody CreateOperationParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que 
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		Date departDateProgrammeeOperation_test = (Date) postBody.getDepartDateProgrammeeOperation().clone();
		departDateProgrammeeOperation_test.setHours(0);
		departDateProgrammeeOperation_test.setMinutes(0);
		departDateProgrammeeOperation_test.setSeconds(0);
		if (postBody.getArriveeDateProgrammeeOperation() != null && departDateProgrammeeOperation_test.after(postBody.getArriveeDateProgrammeeOperation())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date d'arrivée prévue doit être postérieure à la date de départ");
		}


		UtilisateurOperateurKamtar operateur = utilisateurOperateurKamtarService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), jwtProvider.getCodePays(token));

		// enregistrement
		Operation operation = operationService.create(postBody, operateur, token);
		if (operation != null) {
			actionAuditService.creerOperation(operation, token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operation), HttpStatus.CREATED);
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible d'enregistrer l'opération.");

	}

	/**
	 * Création d'une operation par un client
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Création d'une operation par un client")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (réservé aux clients)"),
			@ApiResponse(code = 404, message = "Impossible de charger le client à partir de l'utilisateur"),
			@ApiResponse(code = 400, message = "Impossible d'enregistrer l'opération."),
			@ApiResponse(code = 201, message = "Operation créée", response = Operation.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/operation_client", 
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity createOperationExpediteur(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody CreateOperationParClientParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que 
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une operation
		if (!SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.COMMANDER)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// vérifications
		UtilisateurClient utilisateur_client = clientExpediteurService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), jwtProvider.getCodePays(token));
		UtilisateurClientPersonnel utilisateur_client_personnel = utilisateurClientPersonnelService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), jwtProvider.getCodePays(token));
		Client client = null;
		if (utilisateur_client != null) {
			String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
			client = clientService.getByUUID(uuid_client, jwtProvider.getCodePays(token));
		} else if (utilisateur_client_personnel != null) {
			String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
			client = clientService.getByUUID(uuid_client, jwtProvider.getCodePays(token));
		}
		if (client == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger le client à partir de l'utilisateur");
		}

		// enregistrement
		List<Operation> operations = operationService.create(postBody, utilisateur_client, utilisateur_client_personnel, client, token);
		if (operations != null) {
			for (Operation operation : operations) {
				actionAuditService.creerOperation(operation, token);
			}
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operations), HttpStatus.CREATED);
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible d'enregistrer l'opération.");

	}


	/**
	 * Déclarer un incident par un transporteur ou un propriétaire
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Déclarer un incident par un transporteur ou un propriétaire")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (réservé aux transporteurs et propriétaires)"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'opération"),
			@ApiResponse(code = 400, message = "Vous ne pouvez pas déclarer un incident sur une opération om l véhicule n'a pas encore été affecté."),
			@ApiResponse(code = 403, message = "Vous n'avez pas accès à cette operation."),
			@ApiResponse(code = 201, message = "Incident enregistré", response = Operation.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/operation/incident/declarer",
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity declarerIncidentOperation(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody DeclarerIncidentOperationParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une operation
		if (!SecurityUtils.transporteur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// vérifications
		Operation operation = operationService.getByUUID(postBody.getOperation_id(), jwtProvider.getCodePays(token));
		if (operation == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'opération.");
		}

		if (operation.getVehicule() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas déclarer un incident sur une opération om l véhicule n'a pas encore été affecté.");
		}

		// sécurité
		UtilisateurDriver transporteur = null;
		UtilisateurProprietaire proprietaire = null;
		if (SecurityUtils.transporteur(jwtProvider, token)) {
			UUID uuid_transporteur = jwtProvider.getUUIDFromJWT(token);
			if (!operation.getTransporteur().getUuid().equals(uuid_transporteur)) {
				logger.warn("ce transporteur n'a pas accès à cette operation");
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas accès à cette operation.");
			}
		} else if (SecurityUtils.proprietaire(jwtProvider, token)) {
			String uuid_client = jwtProvider.getClaimsValue("uuid", token);
			if (!operation.getVehicule().getProprietaire().getUuid().toString().equals(uuid_client)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas accès à cette operation.");
			}
		}

		operation.setObservationsParTransporteur(postBody.getIncident());
		operationService.save(operation);
		actionAuditService.declarerIncidentOperation(postBody.getIncident(), operation, token);

		// si incident remplit, on envoit ue notification au backoffice
		if (postBody.getIncident() != null && !"".equals(postBody.getIncident())) {
			// envoi de la notification au backoffice
			Notification notification = new Notification(NotificationType.WEB_BACKOFFICE.toString(), null, "Incident déclaré par un driver sur l'opération " + operation.getCode(), operation.getUuid().toString(), null, operation.getCodePays());
			notificationService.create(notification, jwtProvider.getCodePays(token));
		}

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operation), HttpStatus.OK);


	}



	/**
	 * Annuler une opération
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Annuler une opération")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "L'opération a déjà été annulée"),
			@ApiResponse(code = 400, message = "Une opération terminée ne peut pas être annulée."),
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (réservé aux administrateurs)"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'opération"),
			@ApiResponse(code = 201, message = "Opération annulée", response = Operation.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/operation/annuler",
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity annulerOperation(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody AnnulerOperationParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une operation
		if (!SecurityUtils.admin(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// vérifications
		Operation operation = operationService.getByUUID(postBody.getOperation_id(), jwtProvider.getCodePays(token));
		if (operation == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'opération.");
		}
		if (operation.getAnnulationDate() != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'opération a déjà été annulé.");
		}
		if (operation.getStatut().equals(OperationStatut.DECHARGEMENT_TERMINE)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une opération terminée ne peut pas être annulée.");
		}

		String ancien_statut = operation.getStatut();

		// enregistre l'annulation
		operation.annuler(postBody);
		operationService.save(operation);
		actionAuditService.annulerOperation(operation, token);

		// enregistre le changement de statut
		OperationChangementStatut operation_changement_statut = new OperationChangementStatut(ancien_statut, operation.getStatut(), operation, null, null, null, operation.getCodePays());
		operationChangementStatutService.save(operation_changement_statut);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operation), HttpStatus.OK);


	}


	/**
	 * Affectation d'un véhicule & driver à une operation
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Affectation d'un véhicule & driver à une operation")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (réservé aux admins et opérateurs ayant droits)"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'opération"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'appel d'offre."),
			@ApiResponse(code = 400, message = "Vous ne pouvez pas affecter un véhicule et driver à une opération validée."),
			@ApiResponse(code = 400, message = "Vous ne pouvez pas affecter un appel d'offre refusé par un driver à une opération"),
			@ApiResponse(code = 400, message = "Vous ne pouvez pas affecter un appel d'offre non validée à une opération"),
			@ApiResponse(code = 201, message = "Operation affectée", response = Operation.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/operation/affecter", 
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity affecterOperation(

			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody AffecterOperationParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que 
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// vérifications
		Operation operation = operationService.getByUUID(postBody.getOperation_id(), jwtProvider.getCodePays(token));
		if (operation == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'opération.");
		} else if (!operation.getStatut().equals(OperationStatut.ENREGISTRE.toString()) && !operation.getStatut().equals(OperationStatut.EN_COURS_DE_TRAITEMENT.toString())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas affecter un véhicule et driver à une opération validée.");
		}

		OperationAppelOffre ao = operationAppelOffreService.getByUUID(postBody.getAppel_offre_id(), jwtProvider.getCodePays(token));
		if (ao == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'appel d'offre.");
		} else if (ao.getStatut().equals("3")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas affecter un appel d'offre refusé par un driver à une opération");
		} else if (!ao.getStatut().equals("1") && !ao.getStatut().equals("2")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas affecter un appel d'offre non validée à une opération");
		}

		operation.setVehicule(ao.getVehicule());
		operation.setTransporteur(ao.getTransporteur());
		operation.setPrixDemandeParDriver(ao.getMontantPropose());
		operation.setPrixDemandeParDriverDevise(ao.getMontantProposeDevise());

		operationService.save(operation);
		actionAuditService.affecterOperation(operation, ao, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operation), HttpStatus.OK);


	}


	/**
	 * Modification d'une operation
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Modification d'une operation")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Vous ne pouvez pas modifier une opération qui a été annulée."),
			@ApiResponse(code = 400, message = "Vous devez indiquer un transporteur pour valider l'opération"),
			@ApiResponse(code = 400, message = "Vous devez indiquer le client pour valider l'opération"),
			@ApiResponse(code = 400, message = "Vous devez indiquer le véhicule pour valider l'opération"),
			@ApiResponse(code = 400, message = "Vous ne pouvez pas modifier le statut d'une operation en dehors des status VALIDE et ENREGISTRE"),
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'opération"),
			@ApiResponse(code = 200, message = "Operation mise à jour", response = Operation.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/operation", 
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity edit(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody EditOperationParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de modifier un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// chargement
		Operation operation = operationService.getByUUID(postBody.getId(), jwtProvider.getCodePays(token));
		if (operation == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'opération.");
		}
		if (operation.getAnnulationDate() != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas modifier une opération qui a été annulée.");
		}
		operationService.update(postBody, operation, jwtProvider.getCodePays(token), token);


		actionAuditService.editerOperation(operation, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operation), HttpStatus.OK);

	}


	/**
	 * Changement de statut d'une operation
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Changement de statut d'une operation d'une operation")
	@ApiResponses(value = {	
			@ApiResponse(code = 400, message = "Vous ne pouvez pas modifier le statut d'une operation en dehors des status VALIDE et ENREGISTRE"),
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, transporteur, propriétaire ou opérateur ayant le droit)"),
			@ApiResponse(code = 403, message = "Vous n'avez pas accès à cette operation."),
			@ApiResponse(code = 403, message = "Vous n'avez pas le droit de passer cette opération à ce statut."),
			@ApiResponse(code = 404, message = "Impossible de trouver l'opération"),
			@ApiResponse(code = 200, message = "Operation mise à jour", response = Operation.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/operation/traiter", 
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity changer_statut_operation(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody ChangerStatutOperationParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de modifier un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS) && !SecurityUtils.transporteur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// chargement
		Operation operation = operationService.getByUUID(postBody.getId(), jwtProvider.getCodePays(token));
		if (operation == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'opération.");
		}

		// sécurité
		UtilisateurDriver transporteur = null;
		UtilisateurProprietaire proprietaire = null;
		if (SecurityUtils.transporteur(jwtProvider, token)) {
			UUID uuid_transporteur = jwtProvider.getUUIDFromJWT(token);
			if (!operation.getTransporteur().getUuid().equals(uuid_transporteur)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas accès à cette operation.");
			}
		} else if (SecurityUtils.proprietaire(jwtProvider, token)) {
			String uuid_client = jwtProvider.getClaimsValue("uuid", token);
			if (!operation.getVehicule().getProprietaire().getUuid().toString().equals(uuid_client)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas accès à cette operation.");
			}
		}

		// passage au statut
		String ancien_statut = operation.getStatut();
		String nouveau_statut = null;

		if (postBody.getNouveau_statut().equals(OperationStatut.EN_ROUTE_VERS_CLIENT.toString())) {
			if (!operation.getStatut().equals(OperationStatut.VALIDE.toString())) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit de passer cette opération à ce statut.");
			}

			nouveau_statut = OperationStatut.EN_ROUTE_VERS_CLIENT.toString();
			operation.setDepartDateOperation(new Date());
		} else if (postBody.getNouveau_statut().equals(OperationStatut.ARRIVEE_CHEZ_CLIENT.toString())) {
			if (!operation.getStatut().equals(OperationStatut.EN_ROUTE_VERS_CLIENT.toString())) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit de passer cette opération à ce statut.");
			}

			nouveau_statut = OperationStatut.ARRIVEE_CHEZ_CLIENT.toString();
			operation.setDateHeureArriveeChezClientPourCharger(new Date());
		} else if (postBody.getNouveau_statut().equals(OperationStatut.CHARGEMENT_EN_COURS.toString())) {
			if (!operation.getStatut().equals(OperationStatut.ARRIVEE_CHEZ_CLIENT.toString())) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit de passer cette opération à ce statut.");
			}
			nouveau_statut = OperationStatut.CHARGEMENT_EN_COURS.toString();
			operation.setDateHeureChargementCommence(new Date());

		} else if (postBody.getNouveau_statut().equals(OperationStatut.CHARGEMENT_TERMINE.toString())) {
			if (!operation.getStatut().equals(OperationStatut.CHARGEMENT_EN_COURS.toString())) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit de passer cette opération à ce statut.");
			} else {
				// vérifie que le transporteur a uploadé au moins un document
				if (operationDocumentService.countOperationDocuments(operation) == 0) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous devez envoyer un document sur l'app pour valider la fin du déchargement.");

				}
			}
			nouveau_statut = OperationStatut.CHARGEMENT_TERMINE.toString();
			operation.setDateHeureChargementTermine(new Date());

		} else if (postBody.getNouveau_statut().equals(OperationStatut.EN_DIRECTION_DESTINATION.toString())) {
			if (!operation.getStatut().equals(OperationStatut.CHARGEMENT_TERMINE.toString())) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit de passer cette opération à ce statut.");
			} else {
				// vérifie que le transporteur a uploadé au moins un document
				if (operationDocumentService.countOperationDocuments(operation) == 0) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous devez envoyer un document sur l'app pour valider la fin du déchargement.");

				}
			}
			nouveau_statut = OperationStatut.EN_DIRECTION_DESTINATION.toString();
			operation.setDateHeureEnRouteVersDestination(new Date());

		} else if (postBody.getNouveau_statut().equals(OperationStatut.ARRIVE_DESTINATION.toString())) {
			if (!operation.getStatut().equals(OperationStatut.EN_DIRECTION_DESTINATION.toString())) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit de passer cette opération à ce statut.");
			} else {
				// vérifie que le transporteur a uploadé au moins un document
				if (operationDocumentService.countOperationDocuments(operation) == 0) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous devez envoyer un document sur l'app pour valider la fin du déchargement.");

				}
			}
			nouveau_statut = OperationStatut.ARRIVE_DESTINATION.toString();
			operation.setDateHeureArriveADestination(new Date());

		} else if (postBody.getNouveau_statut().equals(OperationStatut.DECHARGEMENT_EN_COURS.toString())) {
			if (!operation.getStatut().equals(OperationStatut.ARRIVE_DESTINATION.toString())) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit de passer cette opération à ce statut.");
			} else {
				// vérifie que le transporteur a uploadé au moins un document
				if (operationDocumentService.countOperationDocuments(operation) == 0) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous devez envoyer un document sur l'app pour valider la fin du déchargement.");

				}
			}
			nouveau_statut = OperationStatut.DECHARGEMENT_EN_COURS.toString();
			operation.setDateHeureDechargementCommence(new Date());

		} else if (postBody.getNouveau_statut().equals(OperationStatut.DECHARGEMENT_TERMINE.toString())) {
			if (!operation.getStatut().equals(OperationStatut.DECHARGEMENT_EN_COURS.toString())) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit de passer cette opération à ce statut.");
			} else {
				// vérifie que le transporteur a uploadé au moins un document
				if (operationDocumentService.countOperationDocuments(operation) == 0) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous devez envoyer un document sur l'app pour valider la fin du déchargement.");

				}
			}
			nouveau_statut = OperationStatut.DECHARGEMENT_TERMINE.toString();
			operation.setDateHeureDechargementTermine(new Date());

			// envoi d'un email au client
			emailToSendService.envoyerFinOperation(operation, jwtProvider.getCodePays(token));

			// envoi d'une notification au backoffice
			Notification notification = new Notification(NotificationType.WEB_BACKOFFICE.toString(), null, "Opération " + operation.getCode() + " terminée", operation.getUuid().toString(), null, operation.getCodePays());
			notificationService.create(notification, jwtProvider.getCodePays(token));

		}

		if (nouveau_statut != null) {

			OperationChangementStatut operation_changement_statut = new OperationChangementStatut(ancien_statut, nouveau_statut, operation, null, transporteur, proprietaire, operation.getCodePays());
			operationChangementStatutService.save(operation_changement_statut);

			operation.setStatut(nouveau_statut);

			if (OperationStatut.DECHARGEMENT_TERMINE.toString().equals(nouveau_statut)) {
				// récupère et enregistre la dernière position connue du gps
				Geoloc geoloc = null;
				if (operation.getVehicule() != null && operation.getVehicule().getDriverPrincipal() != null) {
					geolocRepository.findByImmatriculationAndDriver(operation.getVehicule().getImmatriculation(), operation.getVehicule().getDriverPrincipal().getUuid().toString(), operation.getCodePays());
				}
				if (geoloc != null) {
					operation.setDechargementTermineLatitude(geoloc.getLatitude());
					operation.setDechargementTermineLongitude(geoloc.getLongitude());

				}
			}


			operationService.save(operation);

			actionAuditService.changerStatutOperation(operation, token, ancien_statut, nouveau_statut);

		}



		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operation), HttpStatus.OK);

	}




	/**
	 * Enregistrement d'une satisfaction par un client ou un driver
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Enregistrement d'une satisfaction par un client ou un driver")
	@ApiResponses(value = {

			@ApiResponse(code = 400, message = "Vous ne pouvez pas noter une opération qui a été annulé."),
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'opération"),
			@ApiResponse(code = 400, message = "La note déposée est invalide."),
			@ApiResponse(code = 403, message = "Vous n'avez pas accès à cette operation."),
			@ApiResponse(code = 200, message = "Satisfaction enregistrée", response = Operation.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/operation/satisfaction",
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity enregistrer_satisfaction_operation(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody OperationSatisfactionParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de modifier un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une operation
		if (!SecurityUtils.transporteur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token) && !SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_OPERATIONS) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_TOUTES_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// vérification
		if (postBody.getSatisfaction() < 1 || postBody.getSatisfaction() > 3) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La note déposée est invalide.");
		}

		// chargement
		Operation operation = operationService.getByUUID(postBody.getOperation(), code_pays);
		if (operation == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'opération.");
		} else if (operation.getAnnulationDate() != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas noter une opération qui a été annulé.");
		}

		// sécurité
		UtilisateurDriver transporteur = null;
		UtilisateurProprietaire proprietaire = null;
		if (SecurityUtils.transporteur(jwtProvider, token)) {
			UUID uuid_transporteur = jwtProvider.getUUIDFromJWT(token);
			if (!operation.getTransporteur().getUuid().equals(uuid_transporteur)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas accès à cette operation.");
			}
			operation.setSatisfactionDriver(postBody.getSatisfaction());
		} else if (SecurityUtils.proprietaire(jwtProvider, token)) {
			String uuid_client = jwtProvider.getClaimsValue("uuid", token);
			if (!operation.getVehicule().getProprietaire().getUuid().toString().equals(uuid_client)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas accès à cette operation.");
			}
			operation.setSatisfactionDriver(postBody.getSatisfaction());
		} else if (SecurityUtils.client(jwtProvider, token)) {
			String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
			if (!operation.getClient().getUuid().toString().equals(uuid_client)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas accès à cette operation.");
			}
			operation.setSatisfactionClient(postBody.getSatisfaction());
		} else if (SecurityUtils.client_personnel(jwtProvider, token)) {
			String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
			if (SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_TOUTES_OPERATIONS)) {
				if (!operation.getClient().getUuid().toString().equals(uuid_client)) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas accès à cette operation.");
				}
			} else {
				UtilisateurClientPersonnel client_personnel = utilisateurClientPersonnelService.getByUUID(uuid_client, code_pays);
				if (!client_personnel.equals(operation.getClient_personnel())) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas accès à cette operation.");
				}
				operation.setSatisfactionClient(postBody.getSatisfaction());
			}


		}

		operationService.save(operation);

		actionAuditService.enregistrementSatisfactionOperation(operation, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operation), HttpStatus.OK);

	}


	/**
	 * Récupère les informations d'une operation
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère les informations d'une operation")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, transporteur, propriétaire, client ou opérateur ayant le droit)"),
			@ApiResponse(code = 403, message = "Vous n'avez pas accès à cette operation."),
			@ApiResponse(code = 404, message = "Impossible de trouver l'opération"),
			@ApiResponse(code = 200, message = "La operation demandée", response = Operation.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operation", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity get(
			@Param("uuid") String uuid,
			@Param("code") String code,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_OPERATIONS) && !SecurityUtils.transporteur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token) && !SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_OPERATIONS) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_TOUTES_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// chargement
		Operation operation = null;
		if (uuid != null && !"".equals(uuid)) {
			operation = operationService.getByUUID(uuid, jwtProvider.getCodePays(token));
		} else if (code != null && !"".equals(code)) {
			operation = operationService.getByCode(new Long(code), jwtProvider.getCodePays(token));
		}
		if (operation != null) {

			if (SecurityUtils.transporteur(jwtProvider, token)) {
				// si c'est un transporteur, vérifie que l'opération lui est bien associé
				UUID uuid_transporteur = jwtProvider.getUUIDFromJWT(token);
				if (!operation.getTransporteur().getUuid().equals(uuid_transporteur)) {
					logger.warn("ce transporteur n'a pas accès à cette operation");
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas accès à cette operation.");
				}
			} else if (SecurityUtils.client(jwtProvider, token) || SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_TOUTES_OPERATIONS)) {
				String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
				if (!operation.getClient().getUuid().toString().equals(uuid_client)) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas accès à cette operation.");
				}
			} else if (SecurityUtils.client_personnel(jwtProvider, token)) {
				String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
				String uuid_client_personnel = jwtProvider.getUUIDFromJWT(token).toString();
				if (!operation.getClient().getUuid().toString().equals(uuid_client)) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas accès à cette operation.");
				}
				if (operation.getClient_personnel() != null && !operation.getClient_personnel().getUuid().toString().equals(uuid_client_personnel)) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas accès à cette operation.");
				}

			} else if (SecurityUtils.proprietaire(jwtProvider, token)) {
				UUID uuid_proprietaire = jwtProvider.getUUIDFromJWT(token);
				if (!operation.getVehicule().getProprietaire().getUuid().toString().equals(uuid_proprietaire.toString())) {
					logger.warn("ce propriétaire n'a pas accès à cette operation");
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas accès à cette operation.");
				}
			}

			actionAuditService.getOperation(operation, token);

			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operation), HttpStatus.OK);

		}


		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'opération.");
	}




	/**
	 * Récupère les operations attribuées à un transporteur
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère les operations attribuées à un transporteur")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (reservés aux transporteurs ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver les operations"),
			@ApiResponse(code = 200, message = "Liste de operations", response = ListOperation.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operations/transporteur", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity getOperationsTransporteurs(
			@ApiParam(value = "UUID transporteur", required = true) @RequestParam("transporteur") String uuid,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher une operation
		if (!SecurityUtils.transporteur(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}


		UtilisateurDriver transporteur1 = transporteurRepository.findByUUID(UUID.fromString(uuid), jwtProvider.getCodePays(token));

		// filtrage
		List<String> colonnesFiltrageActive = Arrays.asList();
		ParentSpecificationsBuilder builder = new OperationSpecificationsBuilder();
		builder.with("transporteur", PredicateUtils.OPERATEUR_EGAL, transporteur1, false);
		Specification spec = builder.build();

		// filtrage par pays kamtar
		Specification spec_pays = new Specification<ActionAudit>() {
			public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get("codePays"), jwtProvider.getCodePays(token)));
				return builder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
		spec = spec.and(spec_pays);



		// chargement
		List<Operation> operations = operationService.getOperationsTransporteur("departDateProgrammeeOperation", Sort.Direction.DESC.toString(), 0, 999999, spec, transporteur1).getContent();


		actionAuditService.getOperationsTransporteur(token, transporteur1);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operations), HttpStatus.OK);

	}



	/**
	 * Récupère les operations attribuées à un transporteur groupées pour une journée
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère les operations attribuées à un transporteur groupées pour une journée")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, transporteur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver le véhicule"),
			@ApiResponse(code = 200, message = "Liste des operations", response = ListOperation.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operations/transporteur/jour", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity getOperationsVehiculesParJour(
			@ApiParam(value = "Journée au format dd_MM_yyyy", required = true) @RequestParam("journee") String journee,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher une operation
		if (!SecurityUtils.transporteur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// filtrage par véhicule
		List<Vehicule> vehicules = new ArrayList<Vehicule>();
		if (SecurityUtils.transporteur(jwtProvider, token)) {
			String uuid_vehicule = jwtProvider.getClaimsValue("uuid_vehicule", token);
			Vehicule vehicule = vehiculeService.getByUUID(uuid_vehicule, jwtProvider.getCodePays(token));
			if (vehicule == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger le véhicule.");
			}
			vehicules.add(vehicule);
		} else if (SecurityUtils.proprietaire(jwtProvider, token)) {
			UUID uuid_proprietaire = jwtProvider.getUUIDFromJWT(token);
			UtilisateurProprietaire proprietaire = proprietaireRepository.findByUUID(uuid_proprietaire, jwtProvider.getCodePays(token));
			vehicules.addAll(vehiculeService.getByProprietaire(proprietaire, jwtProvider.getCodePays(token)));
		}

		UtilisateurDriver transporteur = null;
		UtilisateurProprietaire proprietaire = null;
		if (SecurityUtils.transporteur(jwtProvider, token)) {
			UUID uuid_transporteur = jwtProvider.getUUIDFromJWT(token);
			transporteur = transporteurRepository.findByUUID(uuid_transporteur, jwtProvider.getCodePays(token));
		} else if (SecurityUtils.proprietaire(jwtProvider, token)) {
			UUID uuid_proprietaire = jwtProvider.getUUIDFromJWT(token);
			proprietaire = proprietaireRepository.findByUUID(uuid_proprietaire, jwtProvider.getCodePays(token));

		}

		// filtrage
		ParentSpecificationsBuilder builder = new OperationSpecificationsBuilder();
		Specification spec = builder.build();

		// chargement
		List<Operation> operations = new ArrayList<Operation>();
		if (!vehicules.isEmpty()) {
			operations = operationService.getOperationsTransporteurJour(Sort.Direction.DESC.toString(), 0, 999999, spec, vehicules, journee, jwtProvider.getCodePays(token));
		}

		if (SecurityUtils.transporteur(jwtProvider, token)) {
			actionAuditService.getOperationsTransporteur(token, transporteur);
		} else if (SecurityUtils.proprietaire(jwtProvider, token)) {
			actionAuditService.getOperationsProprietaire(token, proprietaire);
		}

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operations), HttpStatus.OK);

	}

	/**
	 * Facturer des opérations (génère une facture client)
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Facturer des opérations (génère une facture client)")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Toutes les opérations doivent être adressées au même client pour pouvoir lancer la facturation."),
			@ApiResponse(code = 400, message = "L'opération XX a déjà été facturée."),
			@ApiResponse(code = 400, message = "Le type de l'opération XX (pour la TVA) n'a pas été renseigné."),
			@ApiResponse(code = 400, message = "Le 'prix à payer par le client' de l'opération XX n'est pas renseignée."),
			@ApiResponse(code = 400, message = "Impossible de créer la facture"),
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin ou pérateu ayant le droit)"),
			@ApiResponse(code = 403, message = "Vous devez sélectionner au moins une opération pour lancer la facturation."),
			@ApiResponse(code = 200, message = "Détails de la facture", response = FactureClient.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operations/facturer/client",
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public FactureClient facturerOperations(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody OperationFacturerParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token
			) throws IOException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.DECLENCHER_FACTURATION_CLIENT)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// vérifie qu'il y a au moins une opération
		if (postBody.getId_operations().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous devez sélectionner au moins une opération pour lancer la facturation.");
		}

		FactureClient facture_client = factureClientService.genererFacture(postBody, jwtProvider.getCodePays(token));


		return facture_client;

	}

	/**
	 * Facturer des opérations (génère une facture propriétaire)
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Facturer des opérations (génère une facture propriétaire)")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Toutes les opérations doivent avoir été réalisée avec des véhicules appartenant au même propriétaire."),
			@ApiResponse(code = 400, message = "L'opération XX a déjà été facturée."),
			@ApiResponse(code = 400, message = "Le 'prix demandé par le driver' de l'opération XX n'est pas renseignée."),
			@ApiResponse(code = 400, message = "Impossible de créer la facture"),
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin ou transporteur ayant le droit)"),
			@ApiResponse(code = 403, message = "Vous devez sélectionner au moins une opération pour lancer la facturation."),
			@ApiResponse(code = 200, message = "Détails de la facture", response = FactureProprietaire.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operations/facturer/proprietaire",
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public FactureProprietaire facturerOperations2(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody OperationFacturerParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token
	) throws IOException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.DECLENCHER_FACTURATION_PROPRIETAIRE)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// vérifie qu'il y a au moins une opération
		if (postBody.getId_operations().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous devez sélectionner au moins une opération pour lancer la facturation.");
		}

		FactureProprietaire facture_proprietaire = factureProprietaireService.genererFacture(postBody, jwtProvider.getCodePays(token));


		return facture_proprietaire;

	}

	/**
	 * Récupère les operations  filtrés et attribuées à un transporteur
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère les operations  filtrés et attribuées à un transporteur")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, transporteur, propriétaire ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver le véhicule"),
			@ApiResponse(code = 200, message = "Map de List de operation, avec en clé les jours", response = MapOperationParJour.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operations/vehicule", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity getOperationsTransporteurs2(
			@ApiParam(value = "Type d'opérations à filtrer (valeurs possibles = a_venir, en_cours, terminees)", required = true) @RequestParam("query") String query,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher une operation
		if (!SecurityUtils.transporteur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// filtrage par véhicule
		List<Vehicule> vehicules = new ArrayList<Vehicule>();

		UtilisateurDriver transporteur = null;
		UtilisateurProprietaire proprietaire = null;
		if (SecurityUtils.transporteur(jwtProvider, token)) {
			UUID uuid_transporteur = jwtProvider.getUUIDFromJWT(token);
			transporteur = transporteurRepository.findByUUID(uuid_transporteur, jwtProvider.getCodePays(token));

			// filtre sur les véhicules utilisés pour se connecter
			String uuid_vehicule = jwtProvider.getClaimsValue("uuid_vehicule", token);
			Vehicule vehicule = vehiculeService.getByUUID(uuid_vehicule, jwtProvider.getCodePays(token));
			if (vehicule == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger le véhicule.");
			}
			vehicules.add(vehicule);


		} else if (SecurityUtils.proprietaire(jwtProvider, token)) {
			UUID uuid_proprietaire = jwtProvider.getUUIDFromJWT(token);
			proprietaire = proprietaireRepository.findByUUID(uuid_proprietaire, jwtProvider.getCodePays(token));

			// filtre sur toute la flotte de ses véhicules
			vehicules.addAll(vehiculeService.getByProprietaire(proprietaire, jwtProvider.getCodePays(token)));
		}

		// query
		boolean uniquement_a_venir = false;
		boolean uniquement_en_cours = false;
		boolean uniquement_terminees = false;
		if ("a_venir".equals(query)) {
			uniquement_a_venir = true;
		} else if ("en_cours".equals(query)) {
			uniquement_en_cours = true;
		} else if ("terminees".equals(query)) {
			uniquement_terminees = true;
		}


		// chargement
		List<Operation> operations = new ArrayList<>();
		if (!vehicules.isEmpty()) {
			operations = operationService.getOperationsTransporteurEnCours(Sort.Direction.DESC.toString(), 0, 999999, transporteur, proprietaire, vehicules, uniquement_a_venir, uniquement_en_cours, uniquement_terminees, jwtProvider.getCodePays(token));
		}

		if (SecurityUtils.transporteur(jwtProvider, token)) {
			actionAuditService.getOperationsTransporteur(token, transporteur);
		} else if (SecurityUtils.proprietaire(jwtProvider, token)) {
			actionAuditService.getOperationsProprietaire(token, proprietaire);
		}
		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operations), HttpStatus.OK);


	}



	/**
	 * Export des operations
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Export des operations")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (reservés aux admins et opérateurs ayant droits)"),
			@ApiResponse(code = 200, message = "CSV des drivers")
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operations/export",
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

		// filtrage par date de début et fin
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
		Page<Operation> leads = operationService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);

		// convertion liste en excel
		byte[] bytesArray = exportExcelService.export(leads, null);

		// audit
		actionAuditService.exportOperations(token);

		return ResponseEntity.ok()
				//.headers(headers) // add headers if any
				.contentLength(bytesArray.length)
				.contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
				.body(bytesArray);
	}


	/**
	 * Liste de tous les operations
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste de tous les operations")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Le statut est invalide."),
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'opération"),
			@ApiResponse(code = 200, message = "Liste des operations (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operations", 
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_offres(
			@ApiParam(value = "Critères de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de consulter les opérateurs katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_OPERATIONS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_TRANSPORTEURS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_CLIENTS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_VEHICULE)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// filtrage par client
		Client expediteur = null;
		if (jwtProvider.getClaims(token).containsKey("uuid_client")) {
			UUID expediteur_uuid = UUID.fromString(jwtProvider.getClaims(token).get("uuid_client").toString());
			expediteur = clientService.getByUUID(expediteur_uuid.toString(), jwtProvider.getCodePays(token));
			if (expediteur == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger l'expéditeur");
			}
		}

		// paramètres du datatable
		String draw 		= postBody.getFirst("draw").toString();
		Integer length 		= Integer.valueOf(postBody.getFirst("length").toString());

		// tri, sens et numéro de page
		String colonneTriDefaut = "createdOn";
		List<String> colonnesTriAutorise = Arrays.asList("code", "createdOn", "prixAPayerParClient", "prixDemandeParDriver", "periodeOperation", "departDateProgrammeeOperation", "dateHeureChargementTermine" );

		String order_column_bdd = DatatableUtils.getOrderColonne(colonneTriDefaut, colonnesTriAutorise, postBody);

		if ("periodeOperation".equals(order_column_bdd)) {
			order_column_bdd = "departDateProgrammeeOperation";
		}
		String sort_bdd = DatatableUtils.getSort(postBody);
		Integer numero_page = DatatableUtils.getNumeroPage(postBody, length);

		// filtrage
		List<String> colonnesFiltrageActive = Arrays.asList("code", "statut", "valideParOperateur", "paiementRecu", "arriveeAdresseComplete", "prixDemandeParDriver", "prixAPayerParClient", "valideParOperateur", "arriveeAdresseVille", "departAdresseVille");
		ParentSpecificationsBuilder builder = new OperationSpecificationsBuilder();
		Specification spec_general = DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, "CUSTOM_NON_VALIDE");
		if (spec_general == null) {
			spec_general = Specification.where(DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, "CUSTOM_NON_VALIDE"));
		}

		// filtrage par date de création
		spec_general = DatatableUtils.fitrageDate(spec_general, postBody, "departDateProgrammeeOperation", "departDateProgrammeeOperation");
		spec_general = DatatableUtils.fitrageDate(spec_general, postBody, "dateHeureChargementTermine", "dateHeureChargementTermine");

		// filtrage par date de création
		spec_general = DatatableUtils.fitrageDate(spec_general, postBody, "dateFacture", "dateFacture");

		// filtre par proprietaire dans datatable
		Map<Integer, String> indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		Integer position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, "proprietaire");
		if (position_colonne != null) {
			String filtre_par_proprietaire = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			if (filtre_par_proprietaire != null && !"".equals(filtre_par_proprietaire.trim())) {
				Specification spec2 = new Specification<Operation>() {
					public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(filtre_par_proprietaire, jwtProvider.getCodePays(token));
						predicates.add(builder.equal(root.get("vehicule").get("proprietaire"), proprietaire));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec_general = spec_general.and(spec2);
			}
		}

		// filtre par périodes
		indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, "periodeOperation");
		if (position_colonne != null) {
			String filtre_par_periode = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			if (filtre_par_periode != null && !"".equals(filtre_par_periode.trim())) {
				Specification spec2 = new Specification<Operation>() {
					public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
						LocalDate firstDayOfMonth = null;
						LocalDate lastDay = null;
						LocalDate todaydate = LocalDate.now();
						// 1 = mois en cours, 2 = mois précédent
						if ("1".equals(filtre_par_periode)) {
							firstDayOfMonth = todaydate.withDayOfMonth(1);
							lastDay = todaydate.with(TemporalAdjusters.lastDayOfMonth());
						} else if ("2".equals(filtre_par_periode)) {
							firstDayOfMonth = todaydate.minusMonths(1).withDayOfMonth(1);;
							lastDay = todaydate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
						}
						if(firstDayOfMonth != null && lastDay != null) {
							try {
								logger.info("first : " + formatter.format(firstDayOfMonth));
								logger.info("lastDay : " + formatter.format(lastDay));
								Date date1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(formatter.format(firstDayOfMonth) + " 00:00:00");
								predicates.add(builder.greaterThanOrEqualTo(root.get("departDateProgrammeeOperation"), date1));
								Date date2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(formatter.format(lastDay) + " 23:59:59");
								predicates.add(builder.lessThanOrEqualTo(root.get("departDateProgrammeeOperation"), date2));
							} catch (ParseException e) {
								// erreur silencieuses
							} catch (ArrayIndexOutOfBoundsException e) {
								// erreur silencieuse
							}
						}

						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec_general = spec_general.and(spec2);
			}
		}

		// filtre par notes clients et drivers dans datatable
		indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, "notes");
		if (position_colonne != null) {
			String filtre_par_notes = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			if (filtre_par_notes != null && !"".equals(filtre_par_notes.trim())) {
				Specification spec2 = new Specification<Operation>() {
					public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						if ("1".equals(filtre_par_notes)) {
							// Meilleures notes clients
							predicates.add(builder.equal(root.get("satisfactionClient"), 1));
						} else if ("2".equals(filtre_par_notes)) {
							// Meilleures notes chauffeurs
							predicates.add(builder.equal(root.get("satisfactionDriver"), 1));
						} else if ("3".equals(filtre_par_notes)) {
							// Pires notes clients
							predicates.add(builder.equal(root.get("satisfactionClient"), 3));
						} else if ("4".equals(filtre_par_notes)) {
							// Pires notes chaffeurs
							predicates.add(builder.equal(root.get("satisfactionDriver"), 3));
						}
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec_general = spec_general.and(spec2);
			}
		}

		// filtrage sur statut custom "CUSTOM_NON_VALIDE"
		indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, "statut");
		if (position_colonne != null) {
			String filtre_par_statut = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			if (filtre_par_statut != null && !"".equals(filtre_par_statut.trim()) && "CUSTOM_NON_VALIDE".equals(filtre_par_statut)) {
				Specification spec2 = new Specification<Operation>() {
					public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						predicates.add(builder.equal(root.get("statut"), OperationStatut.ENREGISTRE.toString()));
						predicates.add(builder.equal(root.get("creePar"), "C"));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec_general = spec_general.and(spec2);
			} else if (filtre_par_statut != null && !"".equals(filtre_par_statut.trim()) && "ENREGISTRE".equals(filtre_par_statut)) {
				Specification spec2 = new Specification<Operation>() {
					public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						predicates.add(builder.equal(root.get("statut"), OperationStatut.ENREGISTRE.toString()));
						predicates.add(builder.equal(root.get("creePar"), "O"));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec_general = spec_general.and(spec2);
			}
		}

		// filtre par statut annulé
		indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, "annule");
		if (position_colonne != null) {
			String filtre_par_statut_annule = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			if (filtre_par_statut_annule != null && !"".equals(filtre_par_statut_annule.trim())) {
				Specification spec2 = new Specification<Operation>() {
					public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						if (filtre_par_statut_annule.equals("true")) {
							predicates.add(builder.isNotNull(root.get("annulationDate")));
						} else if (filtre_par_statut_annule.equals("false")) {
							predicates.add(builder.isNull(root.get("annulationDate")));
						}
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec_general = spec_general.and(spec2);
			}
		}


		// filtre par driver dans datatable
		indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, "driver");
		if (position_colonne != null) {
			String filtre_par_driver = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			if (filtre_par_driver != null && !"".equals(filtre_par_driver.trim())) {
				Specification spec2 = new Specification<Operation>() {
					public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						UtilisateurDriver driver = utilisateurTransporteurService.getByUUID(filtre_par_driver, jwtProvider.getCodePays(token));
						predicates.add(builder.equal(root.get("transporteur"), driver));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec_general = spec_general.and(spec2);
			}
		}

		// filtre par expéditeur dans datatable
		indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, "expediteur");
		if (position_colonne != null) {
			String filtre_par_expediteur = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			if (filtre_par_expediteur != null && !"".equals(filtre_par_expediteur.trim())) {
				Specification spec2 = new Specification<Operation>() {
					public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						Client expediteur = clientService.getByUUID(filtre_par_expediteur, jwtProvider.getCodePays(token));
						predicates.add(builder.equal(root.get("client"), expediteur));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				}; 
				spec_general = spec_general.and(spec2); 
			}
		}

		// filtre sur la date d'exédition
		indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, "departDateProgrammeeOperation");
		if (position_colonne != null) {
			String filtre_par_expediteur = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			if (filtre_par_expediteur != null && !"".equals(filtre_par_expediteur.trim())) {
				Specification spec2 = new Specification<Operation>() {
					public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();

						Date date1 = null;
						try {
							date1 = new SimpleDateFormat("dd/MM/yyyy").parse(filtre_par_expediteur);
							date1.setHours(0);
							date1.setMinutes(0);
							date1.setSeconds(0);
							Date date2 = new SimpleDateFormat("dd/MM/yyyy").parse(filtre_par_expediteur);
							date2.setHours(23);
							date2.setMinutes(59);
							date2.setSeconds(59);

							predicates.add(builder.greaterThanOrEqualTo(root.get("departDateProgrammeeOperation"), date1));
							predicates.add(builder.lessThanOrEqualTo(root.get("departDateProgrammeeOperation"), date2));
						} catch (ParseException e) {
							// si date pas conforme on ne fait rien
						}

						return builder.and(predicates.toArray(new Predicate[predicates.size()]));

					}
				};
				spec_general = spec_general.and(spec2);
			}
		}

		// filtre par type de véhicule
		indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, "vehiculeType");
		if (position_colonne != null) {
			String filtre_par_vehicule = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			if (filtre_par_vehicule != null && !"".equals(filtre_par_vehicule.trim())) {
				Specification spec2 = new Specification<Operation>() {
					public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						predicates.add(builder.equal(root.get("vehicule").get("typeVehicule"), filtre_par_vehicule));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec_general = spec_general.and(spec2);
			}
		}

		// filtre par carrosserie de véhicule
		indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, "vehiculeCarrosserie");
		if (position_colonne != null) {
			String filtre_par_vehicule = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			if (filtre_par_vehicule != null && !"".equals(filtre_par_vehicule.trim())) {
				Specification spec2 = new Specification<Operation>() {
					public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						predicates.add(builder.equal(root.get("vehicule").get("carrosserie"), filtre_par_vehicule));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec_general = spec_general.and(spec2);
			}
		}


		// filtre par cliet dans datatable
		indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, "client");
		if (position_colonne != null) {
			String filtre_par_expediteur = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			if (filtre_par_expediteur != null && !"".equals(filtre_par_expediteur.trim())) {
				Specification spec2 = new Specification<Operation>() {
					public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						Client expediteur = clientService.getByUUID(filtre_par_expediteur, jwtProvider.getCodePays(token));
						predicates.add(builder.equal(root.get("client"), expediteur));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec_general = spec_general.and(spec2);
			}
		}

		// filtrage par numero_facture client
		if (postBody.containsKey("numero_facture_client")) {
			Specification spec2 = new Specification<Operation>() {
				public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					List<Predicate> predicates = new ArrayList<Predicate>();
					FactureClient facture_client = factureClientService.getByNumero(postBody.getFirst("numero_facture_client").toString(), jwtProvider.getCodePays(token));
					if (facture_client != null) {
						predicates.add(builder.equal(root.get("facture"), facture_client.getNumeroFacture()));
					}
					return builder.and(predicates.toArray(new Predicate[predicates.size()]));
				}
			};
			spec_general = spec_general.and(spec2);

		}

		// filtrage par numero_facture proprietaire
		if (postBody.containsKey("numero_facture_proprietaire")) {
			Specification spec2 = new Specification<Operation>() {
				public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					List<Predicate> predicates = new ArrayList<Predicate>();
					FactureProprietaire facture_proprietaire = factureProprietaireService.getByNumero(postBody.getFirst("numero_facture_proprietaire").toString(), jwtProvider.getCodePays(token));
					if (facture_proprietaire != null) {
						predicates.add(builder.equal(root.get("factureProprietaire"), facture_proprietaire.getNumeroFacture()));
					}
					return builder.and(predicates.toArray(new Predicate[predicates.size()]));
				}
			};
			spec_general = spec_general.and(spec2);

		}



		// filtrage pa client
		if (postBody.containsKey("uuid_client")) {
			Specification spec2 = new Specification<Operation>() {
				public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					List<Predicate> predicates = new ArrayList<Predicate>();
					Client expediteur = clientService.getByUUID(postBody.getFirst("uuid_client").toString(), jwtProvider.getCodePays(token));
					predicates.add(builder.equal(root.get("client"), expediteur));
					return builder.and(predicates.toArray(new Predicate[predicates.size()]));
				}
			}; 
			spec_general = spec_general.and(spec2); 
		
		}

		// filtrage par proprietaire des véhicules
		if (postBody.containsKey("uuid_proprietaire")) {
			Specification spec2 = new Specification<Operation>() {
				public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					List<Predicate> predicates = new ArrayList<Predicate>();
					UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(postBody.getFirst("uuid_proprietaire").toString(), jwtProvider.getCodePays(token));
					predicates.add(builder.equal(root.get("vehicule").get("proprietaire"), proprietaire));
					return builder.and(predicates.toArray(new Predicate[predicates.size()]));
				}
			};
			spec_general = spec_general.and(spec2);

		}


		// fultrage par véhicule
		if (postBody.containsKey("uuid_vehicule")) {
			Specification spec2 = new Specification<Operation>() {
				public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					List<Predicate> predicates = new ArrayList<Predicate>();
					Vehicule vehicule = vehiculeService.getByUUID(postBody.getFirst("uuid_vehicule").toString(), jwtProvider.getCodePays(token));
					predicates.add(builder.equal(root.get("vehicule"), vehicule));
					return builder.and(predicates.toArray(new Predicate[predicates.size()]));
				}
			};
			spec_general = spec_general.and(spec2);

		}

		// fultrage par véhicule
		if (postBody.containsKey("uuid_chauffeur")) {
			Specification spec2 = new Specification<Operation>() {
				public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					List<Predicate> predicates = new ArrayList<Predicate>();
					UtilisateurDriver transporteur = utilisateurTransporteurService.getByUUID(postBody.getFirst("uuid_chauffeur").toString(), jwtProvider.getCodePays(token));
					predicates.add(builder.equal(root.get("transporteur"), transporteur));
					return builder.and(predicates.toArray(new Predicate[predicates.size()]));
				}
			};
			spec_general = spec_general.and(spec2);

		}




		// filtrage par statut
		if (postBody.containsKey("statut")) {
			String statut = postBody.getFirst("statut").toString();
			if (!EnumUtils.operationStatutContains(statut)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le statut est invalide");
			} 

			Specification spec = new Specification<Operation>() {
				public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					List<Predicate> predicates = new ArrayList<Predicate>();
					predicates.add(builder.equal(root.get("statut"), statut));
					return builder.and(predicates.toArray(new Predicate[predicates.size()]));
				}
			}; 

			spec_general = spec_general.and(spec);
		}

		// filtrage par statuts
		if (postBody.containsKey("statuts")) {
			String statuts_concatenes = postBody.getFirst("statuts").toString();
			if (statuts_concatenes != null && !"".equals(statuts_concatenes)) {
				List<String> statuts = Arrays.asList(statuts_concatenes.split("@"));
				if (statuts != null && !statuts.isEmpty()) {
					Specification spec = new Specification<Operation>() {
						public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
							List<Predicate> predicates = new ArrayList<Predicate>();
							for (int i = 0; i < statuts.size(); i++) {
								String statut = statuts.get(i).toString();
								predicates.add(builder.equal(root.get("statut"), statut));
							}
							return builder.or(predicates.toArray(new Predicate[predicates.size()]));
						}
					};
					spec_general = spec_general.and(spec);
				}
			}

		}

		// filtrage par pays kamtar
		Specification spec_pays = new Specification<ActionAudit>() {
			public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get("codePays"), jwtProvider.getCodePays(token)));
				return builder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
		spec_general = spec_general.and(spec_pays);




		// préparation les deux requêtes (résultat et comptage)
		List<Operation> operations = operationService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec_general).getContent();
		Long total = operationService.countAll(spec_general);

		// fultrage par véhicule
		if (postBody.containsKey("compter_nb_documents")) {
			// ajoute le nombre de PJ par opération
			for (Operation operation : operations) {
				long nbDocuments = operationDocumentService.countOperationDocuments(operation);
				operation.setNbDocuments(nbDocuments);
			}
		}


		actionAuditService.getOperations(token);

		// prépare les résultast
		JSONArray jsonArrayOffres = new JSONArray();
		if (operations != null) {
			jsonArrayOffres.addAll(operations);
		}
		Map<String, Object> jsonDataResults = new LinkedHashMap<String, Object>();
		jsonDataResults.put("draw", draw);	
		jsonDataResults.put("recordsTotal", total);		
		jsonDataResults.put("recordsFiltered", total);	
		jsonDataResults.put("data", jsonArrayOffres);		

		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(jsonDataResults), HttpStatus.OK);
	}


	/**
	 * Liste de tous les operations
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
			@ApiResponse(code = 200, message = "Liste des operations ", response = ListOperation.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operations/listes",
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_offres2(
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de consulter les opérateurs katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_OPERATIONS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_TRANSPORTEURS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_CLIENTS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_VEHICULE)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// tri, sens et numéro de page
		String order_column_bdd = "createdOn";
		String sort_bdd = "asc";
		ParentSpecificationsBuilder builder = new OperationSpecificationsBuilder();
		Specification spec_general = DatatableUtils.buildFiltres(null, null, builder, null);
		if (spec_general == null) {
			spec_general = Specification.where(DatatableUtils.buildFiltres(null, null, builder, null));
		}


		// filtrage par pays kamtar
		Specification spec_pays = new Specification<ActionAudit>() {
			public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get("codePays"), jwtProvider.getCodePays(token)));
				return builder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
		spec_general = spec_general.and(spec_pays);

		// préparation les deux requêtes (résultat et comptage)
		Page<Operation> leads = operationService.getAllPagined(order_column_bdd, sort_bdd, 0, 999999, spec_general);

		// audit
		actionAuditService.getOperations(token);

		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(leads.getContent()), HttpStatus.OK);
	}



	/**
	 * Autocompletion sur les operations
	 * @param postBody
	 * @return
	 */
	/*@RequestLogger(name = Application.microservice)*/
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Autocomplétion sur les operations")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin ou opérateur)"),
			@ApiResponse(code = 200, message = "Operation ", response = Operation.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operations/autocompletion", 
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity autocompletion(
			@ApiParam(value = "Partie de l'adresse", required = true) @RequestParam("query") String query, 
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestParam("token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher une operation
		if (!SecurityUtils.admin(jwtProvider, token) && !SecurityUtils.operateur(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		Operation operation = operationService.autocomplete(query, jwtProvider.getCodePays(token));

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operation), HttpStatus.OK);

	}



	/**
	 * Récupère les changements de statuts les plus récents des operations filtrés via le paramètre
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère les changements de statuts les plus récents des operations filtrés via le paramètre")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (client)"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'expéditeur"),
			@ApiResponse(code = 200, message = "Liste des changements de statut")
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operation/changements_statuts/expediteur", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity getChangementsStatutsOperationExpediteur(
			@ApiParam(value = "Type d'opérations à filtrer (valeurs possibles : programmees, en_cours)", required = true) @RequestParam("query") String query,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une opération
		if ( !SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_OPERATIONS) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_TOUTES_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		UUID expediteur_uuid = UUID.fromString(jwtProvider.getClaims(token).get("uuid_client").toString());
		logger.info("expediteur_uuid = " + expediteur_uuid);
		Client expediteur = clientService.getByUUID(expediteur_uuid.toString(), code_pays);
		if (expediteur == null) {
			logger.info("expediteur == null");
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger l'expéditeur");
		}

		String order_column_bdd = "createdOn";
		String sort_bdd = "desc";
		Integer numero_page = 0;

		// https://stackoverflow.com/questions/35201604/how-to-create-specification-using-jpaspecificationexecutor-by-combining-tables
		Specification spec = new Specification<OperationChangementStatut>() {
			public Predicate toPredicate(Root<OperationChangementStatut> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();

				if (jwtProvider.getTypeDeCompte(token).equals(UtilisateurTypeDeCompte.EXPEDITEUR.toString()) || SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_TOUTES_OPERATIONS)) {
					predicates.add(builder.equal(root.get("operation").get("client"), expediteur));
				} else if (jwtProvider.getTypeDeCompte(token).equals(UtilisateurTypeDeCompte.EXPEDITEUR_PERSONNEL.toString())) {
					String uuid_client_personnel = jwtProvider.getUUIDFromJWT(token).toString();
					UtilisateurClientPersonnel client_personnel = utilisateurClientPersonnelService.getByUUID(uuid_client_personnel, code_pays);
					predicates.add(builder.equal(root.get("operation").get("client_personnel"), client_personnel));
				}

				predicates.add(builder.notEqual(root.get("nouveauStatut"), OperationStatut.ENREGISTRE.toString()));
				predicates.add(builder.notEqual(root.get("nouveauStatut"), OperationStatut.EN_COURS_DE_TRAITEMENT.toString()));
				return builder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};

		// query
		if ("programmees".equals(query)) {
			Specification spec2 = new Specification<Operation>() {
				public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					List<Predicate> predicates = new ArrayList<Predicate>();
					predicates.add(builder.equal(root.get("operation").get("statut"), OperationStatut.VALIDE.toString()));
					return builder.and(predicates.toArray(new Predicate[predicates.size()]));
				}
			};
			spec = spec.and(spec2);

		} else if ("en_cours".equals(query)) {
			Specification spec2 = new Specification<Operation>() {
				public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					List<Predicate> predicates = new ArrayList<Predicate>();
					predicates.add(builder.notEqual(root.get("operation").get("statut"), OperationStatut.VALIDE.toString()));
					predicates.add(builder.notEqual(root.get("operation").get("statut"), OperationStatut.ENREGISTRE.toString()));
					predicates.add(builder.notEqual(root.get("operation").get("statut"), OperationStatut.EN_COURS_DE_TRAITEMENT.toString()));
					predicates.add(builder.notEqual(root.get("operation").get("statut"), OperationStatut.DECHARGEMENT_TERMINE.toString()));
					predicates.add(builder.isNull(root.get("annulationDate")));
					return builder.and(predicates.toArray(new Predicate[predicates.size()]));
				}
			};
			spec = spec.and(spec2);

		}

		// préparation les deux requêtes (résultat et comptage)
		Page<OperationChangementStatut> operations = operationChangementStatutService.getAllPagined(order_column_bdd, sort_bdd, numero_page, 30, spec);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operations.getContent()), HttpStatus.OK);

	}


	/**
	 * Récupère le trajet géolocalisé du véhicule durant l'opération
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère le trajet géolocalisé du véhicule durant l'opération")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (opération ayant droit ou admin)"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'opération"),
			@ApiResponse(code = 200, message = "Liste des géolicalisations")
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operation/trace",
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity getGeolocalisations(
			@ApiParam(value = "UUID de l'opération", required = true) @RequestParam("uuid") String uuid,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une opération
		if ( !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		Operation operation = operationService.getByUUID(uuid, code_pays);
		if (operation == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger l'opération");
		}

		List<Geoloc> geolocs = operationService.findGeolocsOperation(operation.getVehicule().getImmatriculation(), code_pays, operation);

		actionAuditService.geolocOperation(operation, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(geolocs), HttpStatus.OK);

	}

	/**
	 * Suppression d'une operation
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Suppression d'une operation")
	@ApiResponses(value = {	
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'opération"),
			@ApiResponse(code = 200, message = "Operation supprimée", response = Boolean.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/operation", 
			method = RequestMethod.DELETE)
	@CrossOrigin(origins="*")
	public ResponseEntity supprimer(

			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody DeleteOperationParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de modifier un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS) || !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.SUPPRESSION_OPERATION)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// chargement
		Operation operation = operationService.getByUUID(postBody.getId(), jwtProvider.getCodePays(token));
		if (operation == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'opération.");
		}
		operationService.delete(operation);


		actionAuditService.supprimerOperation(operation, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operation), HttpStatus.OK);

	}


	/**
	 * Duplication d'une operation
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Duplication d'une operation")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 403, message = "Vous n'avez pas le droit de dupliquer une opération qui n'est pas en statut ENREGISTRE."),
			@ApiResponse(code = 404, message = "Impossible de trouver l'opération"),
			@ApiResponse(code = 200, message = "Operation dupliquée", response = Operation.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/operation/dupliquer",
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity dupliquer(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody DupliquerOperationParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de modifier un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// chargement
		Operation operation = operationService.getByUUID(postBody.getId(), jwtProvider.getCodePays(token));
		if (operation == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'opération.");
		}

		// uniquement si l'opération est au statut enregistré
		if (!operation.getStatut().equals(OperationStatut.ENREGISTRE.toString())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit de dupliquer une opération qui n'est pas en statut ENREGISTRE.");

		}

		operation = operationService.dupliquer(operation);


		actionAuditService.dupliquerOperation(operation, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operation), HttpStatus.OK);

	}


	/**
	 * Liste de toutes les opérations d'un expéditeur
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste de toutes les opérations d'un expéditeur")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (expéditeur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver le client"),
			@ApiResponse(code = 200, message = "Liste des opérations (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operations/expediteur",
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_operations_expediteurs(
			@ApiParam(value = "Critères de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de consulter les opérateurs katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher une commande
		if (!SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_OPERATIONS) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_TOUTES_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// chargement du client
		UUID uuid_client = UUID.fromString(jwtProvider.getClaims(token).get("uuid_client").toString());
		Client client = clientService.getByUUID(uuid_client.toString(), jwtProvider.getCodePays(token));
		if (client == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger le client");
		}

		// filtres
		final boolean filtre_en_attente = postBody.containsKey("enAttente") && "true".equals(postBody.getFirst("enAttente").toString()) ? true: false;
		final boolean filtre_en_route = postBody.containsKey("enRoute") && "true".equals(postBody.getFirst("enRoute").toString()) ? true: false;
		final boolean filtre_recu = postBody.containsKey("recu") && "true".equals(postBody.getFirst("recu").toString()) ? true: false;
		final boolean filtre_programme = postBody.containsKey("programme") && "true".equals(postBody.getFirst("programme").toString()) ? true: false;

		// paramètres du datatable/operations/expediteur
		String draw 		= postBody.getFirst("draw").toString();
		Integer length 		= Integer.valueOf(postBody.getFirst("length").toString());

		// tri, sens et numéro de page
		String order_column_bdd = "departDateProgrammeeOperation";
		String sort_bdd = "desc";
		Integer numero_page = DatatableUtils.getNumeroPage(postBody, length);

		// filtrage
		Specification spec = new Specification<Operation>() {
			public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();

				// filtrage par client
				if (jwtProvider.getTypeDeCompte(token).equals(UtilisateurTypeDeCompte.EXPEDITEUR.toString()) || SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_TOUTES_OPERATIONS)) {
					predicates.add(builder.equal(root.get("client"), client));
				} else if (jwtProvider.getTypeDeCompte(token).equals(UtilisateurTypeDeCompte.EXPEDITEUR_PERSONNEL.toString())) {
					UtilisateurClientPersonnel client_personnel = utilisateurClientPersonnelService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
					predicates.add(builder.equal(root.get("client_personnel"), client_personnel));
				}

				// recherche par numéro de commande
				if (postBody.getFirst("recherche") != null && !"".equals(postBody.getFirst("recherche").toString())) {
					String recherche = postBody.getFirst("recherche").toString();
					try {
						Long code_long = new Long(recherche);
						predicates.add(builder.equal(root.get("code"), code_long));
					} catch (java.lang.NumberFormatException e) {
						// contrainte impossible pour faire échoue la recherche
						predicates.add(builder.isNull(root.get("code")));
					}

				}

				// filtre sur les statuts des commandes
				if (filtre_en_attente == true || filtre_en_route == true || filtre_recu == true || filtre_programme == true) {

					List<String> statuts_filtres = new ArrayList<String>();

					if (filtre_en_attente) {
						statuts_filtres.add(OperationStatut.ENREGISTRE.toString());
						statuts_filtres.add(OperationStatut.EN_COURS_DE_TRAITEMENT.toString());
					}
					if (filtre_en_route) {
						statuts_filtres.add(OperationStatut.EN_ROUTE_VERS_CLIENT.toString());
						statuts_filtres.add(OperationStatut.ARRIVEE_CHEZ_CLIENT.toString());
						statuts_filtres.add(OperationStatut.CHARGEMENT_EN_COURS.toString());
						statuts_filtres.add(OperationStatut.CHARGEMENT_TERMINE.toString());
						statuts_filtres.add(OperationStatut.EN_DIRECTION_DESTINATION.toString());
						statuts_filtres.add(OperationStatut.ARRIVE_DESTINATION.toString());
						statuts_filtres.add(OperationStatut.DECHARGEMENT_EN_COURS.toString());
					}
					if (filtre_recu) {
						statuts_filtres.add(OperationStatut.DECHARGEMENT_TERMINE.toString());
					}
					if (filtre_programme) {
						statuts_filtres.add(OperationStatut.VALIDE.toString());
					}

					Expression<String> inExpression = root.get("statut");
					Predicate inPredicate = inExpression.in(statuts_filtres);
					predicates.add(inPredicate);
				}

				// enlève les opérations annulées
				predicates.add(builder.isNull(root.get("annulationDate")));


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
		Page<Operation> liste_operations = operationService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);
		Long total = operationService.countAll(spec);

		// audit asynchrone
		actionAuditService.getOperationsClient(token, client);

		// prépare les résultast
		JSONArray jsonArrayOffres = new JSONArray();
		jsonArrayOffres.addAll(liste_operations.getContent());
		Map<String, Object> jsonDataResults = new LinkedHashMap<String, Object>();
		jsonDataResults.put("draw", draw);
		jsonDataResults.put("recordsTotal", total);
		jsonDataResults.put("recordsFiltered", total);
		jsonDataResults.put("data", jsonArrayOffres);

		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(jsonDataResults), HttpStatus.OK);
	}



	/**
	 * Liste de toutes les opérations d'un propriétaire
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste de toutes les opérations d'un propriétaire")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (expéditeur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver le propriétaire"),
			@ApiResponse(code = 200, message = "Liste des opérations (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operations/proprietaire",
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_operations_proprietaire(
			@ApiParam(value = "Critères de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de consulter les opérateurs katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher une commande
		if (!SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// chargement du proprietaire
		UUID uuid_proprietaire = UUID.fromString(jwtProvider.getUUIDFromJWT(token).toString());
		UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(uuid_proprietaire.toString(), jwtProvider.getCodePays(token));
		if (proprietaire == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger le proprietaire");
		}

		// filtres
		final boolean filtre_en_attente = postBody.containsKey("enAttente") && "true".equals(postBody.getFirst("enAttente").toString()) ? true: false;
		final boolean filtre_en_route = postBody.containsKey("enRoute") && "true".equals(postBody.getFirst("enRoute").toString()) ? true: false;
		final boolean filtre_recu = postBody.containsKey("recu") && "true".equals(postBody.getFirst("recu").toString()) ? true: false;
		final boolean filtre_programme = postBody.containsKey("programme") && "true".equals(postBody.getFirst("programme").toString()) ? true: false;

		// paramètres du datatable/operations/expediteur
		String draw 		= postBody.getFirst("draw").toString();
		Integer length 		= Integer.valueOf(postBody.getFirst("length").toString());

		// tri, sens et numéro de page
		String order_column_bdd = "updatedOn";
		String sort_bdd = "desc";
		Integer numero_page = DatatableUtils.getNumeroPage(postBody, length);

		// filtrage
		Specification spec = new Specification<Operation>() {
			public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();

				// filtrage par proprietaire
				predicates.add(builder.equal(root.get("vehicule").get("proprietaire"), proprietaire));

				// recherche par numéro de commande
				if (postBody.getFirst("recherche") != null && !"".equals(postBody.getFirst("recherche").toString())) {
					String recherche = postBody.getFirst("recherche").toString();
					try {
						Long code_long = new Long(recherche);
						predicates.add(builder.equal(root.get("code"), code_long));
					} catch (java.lang.NumberFormatException e) {
						// contrainte impossible pour faire échoue la recherche
						predicates.add(builder.isNull(root.get("code")));
					}

				}

				// filtre sur les statuts des commandes
				if (filtre_en_attente == true || filtre_en_route == true || filtre_recu == true || filtre_programme == true) {

					List<String> statuts_filtres = new ArrayList<String>();

					if (filtre_en_attente) {
						statuts_filtres.add(OperationStatut.ENREGISTRE.toString());
						statuts_filtres.add(OperationStatut.EN_COURS_DE_TRAITEMENT.toString());
					}
					if (filtre_en_route) {
						statuts_filtres.add(OperationStatut.EN_ROUTE_VERS_CLIENT.toString());
						statuts_filtres.add(OperationStatut.ARRIVEE_CHEZ_CLIENT.toString());
						statuts_filtres.add(OperationStatut.CHARGEMENT_EN_COURS.toString());
						statuts_filtres.add(OperationStatut.CHARGEMENT_TERMINE.toString());
						statuts_filtres.add(OperationStatut.EN_DIRECTION_DESTINATION.toString());
						statuts_filtres.add(OperationStatut.ARRIVE_DESTINATION.toString());
						statuts_filtres.add(OperationStatut.DECHARGEMENT_EN_COURS.toString());
					}
					if (filtre_recu) {
						statuts_filtres.add(OperationStatut.DECHARGEMENT_TERMINE.toString());
					}
					if (filtre_programme) {
						statuts_filtres.add(OperationStatut.VALIDE.toString());
					}

					Expression<String> inExpression = root.get("statut");
					Predicate inPredicate = inExpression.in(statuts_filtres);
					predicates.add(inPredicate);
				}

				// enlève les opérations annulées
				predicates.add(builder.isNull(root.get("annulationDate")));


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
		Page<Operation> liste_operations = operationService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);
		Long total = operationService.countAll(spec);

		// audit asynchrone
		actionAuditService.getOperationsProprietaire(token, proprietaire);

		// prépare les résultast
		JSONArray jsonArrayOffres = new JSONArray();
		jsonArrayOffres.addAll(liste_operations.getContent());
		Map<String, Object> jsonDataResults = new LinkedHashMap<String, Object>();
		jsonDataResults.put("draw", draw);
		jsonDataResults.put("recordsTotal", total);
		jsonDataResults.put("recordsFiltered", total);
		jsonDataResults.put("data", jsonArrayOffres);

		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(jsonDataResults), HttpStatus.OK);
	}



	/**
	 * Récupère le nombre d'opération programmées (validées par kamtar mais pas encore débutées)
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère le nombre d'opération programmées (validées par kamtar mais pas encore débutées)")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (réservés aux clients)"),
			@ApiResponse(code = 200, message = "Nombre d'opération", response = Long.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/client/operations/programmees/compter", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity compterOperationsProgrammees(
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une opération
		if (!SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_OPERATIONS) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_TOUTES_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
		Client client = clientService.getByUUID(uuid_client, code_pays);

		long nb_operations = 0;
		if (jwtProvider.getTypeDeCompte(token).equals(UtilisateurTypeDeCompte.EXPEDITEUR.toString()) || SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_TOUTES_OPERATIONS)) {
			nb_operations = operationService.countNbOperationsProgrammees(client, code_pays);
		} else if (jwtProvider.getTypeDeCompte(token).equals(UtilisateurTypeDeCompte.EXPEDITEUR_PERSONNEL.toString())) {
			String uuid_client_personnel = jwtProvider.getUUIDFromJWT(token).toString();
			UtilisateurClientPersonnel client_personnel = utilisateurClientPersonnelService.getByUUID(uuid_client_personnel, code_pays);
			nb_operations = operationService.countNbOperationsProgrammees(client_personnel, code_pays);
		}


		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(nb_operations), HttpStatus.OK);

	}

	/**
	 * Récupère le nombre d'opération en cours (statuts autre que ENREGISTRE et DECHARGEMENT_TERMINE)
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère le nombre d'opération en cours (statuts autre que ENREGISTRE et DECHARGEMENT_TERMINE")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (réservés aux clients ayant le droit)"),
			@ApiResponse(code = 200, message = "Nombre d'opérations", response = Long.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/client/operations/en_cours/compter", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity getOperationsEnCours(
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une opération
		if (!SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_OPERATIONS) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_TOUTES_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
		Client client = clientService.getByUUID(uuid_client, code_pays);

		// compte le nombre d'opération
		long nb_operations = 0;

		if (jwtProvider.getTypeDeCompte(token).equals(UtilisateurTypeDeCompte.EXPEDITEUR.toString()) || SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_TOUTES_OPERATIONS)) {
			nb_operations = operationService.countNbOperationsEnCours(client, code_pays);
		} else if (jwtProvider.getTypeDeCompte(token).equals(UtilisateurTypeDeCompte.EXPEDITEUR_PERSONNEL.toString())) {
			String uuid_client_personnel = jwtProvider.getUUIDFromJWT(token).toString();
			UtilisateurClientPersonnel client_personnel = utilisateurClientPersonnelService.getByUUID(uuid_client_personnel, code_pays);
			nb_operations = operationService.countNbOperationsEnCours(client_personnel, code_pays);
		}

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(nb_operations), HttpStatus.OK);

	}


	/**
	 * Récupère le nombre d'opération terminées (statuts DECHARGEMENT_TERMINE)
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère le nombre d'opération terminées (statut = DECHARGEMENT_TERMINE")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (réservés aux clients ayant le droit)"),
			@ApiResponse(code = 200, message = "Nombre d'opérations", response = Long.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/client/operations/arrivees/compter",
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity getOperationsTerminees(
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une opération
		if (!SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_OPERATIONS) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_TOUTES_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
		Client client = clientService.getByUUID(uuid_client, code_pays);

		// compte le nombre d'opération'
		long nb_operations = 0;

		if (jwtProvider.getTypeDeCompte(token).equals(UtilisateurTypeDeCompte.EXPEDITEUR.toString()) || SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_TOUTES_OPERATIONS)) {
			nb_operations = operationService.countNbOperationsTerminees(client, code_pays);
		} else if (jwtProvider.getTypeDeCompte(token).equals(UtilisateurTypeDeCompte.EXPEDITEUR_PERSONNEL.toString())) {
			String uuid_client_personnel = jwtProvider.getUUIDFromJWT(token).toString();
			UtilisateurClientPersonnel client_personnel = utilisateurClientPersonnelService.getByUUID(uuid_client_personnel, code_pays);
			nb_operations = operationService.countNbOperationsTerminees(client_personnel, code_pays);
		}

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(nb_operations), HttpStatus.OK);

	}


	/**
	 * Récupère les informations des documents de l'opération
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère les informations des documents de l'opération")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, transporteur, propriétaire ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'opération"),
			@ApiResponse(code = 200, message = "Retourne les informations des documents de l'opération", response = ListOperationDocuments.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operation/documents", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity getPhotos(
			@ApiParam(value = "UUID de l'opération", required = true) @RequestParam("uuid") String uuid,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer un vehicule
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS) && !SecurityUtils.transporteur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token) && !SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// chargement de l'offre si renseignée (=modification de l'offre)
		Operation operation = operationService.getByUUID(uuid, code_pays);
		if (operation != null) {

			if (SecurityUtils.transporteur(jwtProvider, token)) {
				UUID uuid_transporteur = jwtProvider.getUUIDFromJWT(token);
				UtilisateurDriver transporteur = transporteurRepository.findByUUID(uuid_transporteur, code_pays);
				if (!operation.getTransporteur().equals(transporteur)) {
					return null;
				}
			} else if (SecurityUtils.client(jwtProvider, token) || SecurityUtils.client_personnel(jwtProvider, token)) {
				String uuid_client = jwtProvider.getClaimsValue("uuid_client", token).toString();
				Client client = clientService.getByUUID(uuid_client, code_pays);
				if (!operation.getClient().equals(client)) {
					return null;
				}
			}

			// chargement
			List<OperationDocument> photos = operationDocumentService.getOperationDocuments(operation);


			actionAuditService.getOperationDocuments(operation, token);

			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(photos), HttpStatus.OK);
		}


		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'opération.");
	}

	/**
	 * Supprime un des documents de l'opération
	 * @param postBody
	 * @return
	 */
	/*@RequestLogger(name = Application.microservice)*/
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Supprime un des documents de l'opération")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin ou opérateur ayant le droit)"),
			@ApiResponse(code = 200, message = "Document supprimé", response = Boolean.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/operation/document",  
			method = RequestMethod.DELETE)
	@CrossOrigin
	public ResponseEntity<Object> deleteDocument(@Valid @RequestBody OperationDocumentDeleteParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer un vehicule
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS) && !SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnel(jwtProvider, token) && !SecurityUtils.transporteur(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// supprime le fichier dans le dossier si il existe (cas d'une création d'offre). Si c'est une modification, il n'y a pas le dossier donc on ne fait rien
		String tempDir = System.getProperty("java.io.tmpdir");
		File folderTempUUID = new File(tempDir + "/" + postBody.getFolderUuid());
		if (folderTempUUID.exists() && folderTempUUID.isDirectory() && FileUtils.isFilenameValid(postBody.getFilename())) {
			File photo = new File(folderTempUUID + "/" + postBody.getFilename());
			if (photo.exists() && photo.isFile() && photo.length() > 0) {
				boolean photo_deleted = photo.delete();
				if (!photo_deleted) {	
					return new ResponseEntity<Object>(null, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		}

		// supprime en bdd si elle existe (seulement dans le cas où il s'agit d'une modifciation de compte)
		if (postBody.getOperation() != null) {
			Operation operation = operationService.getByUUID(postBody.getOperation(), jwtProvider.getCodePays(token));
			if (operation != null) {

				Client client = null;
				if (SecurityUtils.client(jwtProvider, token)) {
					String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
					client = clientService.getByUUID(uuid_client, code_pays);
				} else if (SecurityUtils.client_personnel(jwtProvider, token)) {
					String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
					client = clientService.getByUUID(uuid_client, code_pays);
				}
				if (client != null && !client.equals(operation.getClient())) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit de supprimer ce document.");
				}

				// extrait l'ordre à partir du nom du fichier
				String filename = FileUtils.removeExtension(postBody.getFilename());
				Optional<OperationDocument> document = operationDocumentService.get(operation, filename);
				if (document.isPresent()) {
					operationDocumentService.delete(document.get());
				}
			}

		}



		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(postBody), HttpStatus.OK);
	}



	/**
	 * Stocke les documents de l'opération
	 * @param postBody
	 * @return
	 */
	/*@RequestLogger(name = Application.microservice)*/
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Stocke les documents de l'opération")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'opération"),
			@ApiResponse(code = 200, message = "Documents stockés", response = Boolean.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "multipart/form-data",
			value = "/operation/documents", 
			method = RequestMethod.POST)  
	@CrossOrigin(origins="*")
	public ResponseEntity<Object> addPhotos(MultipartHttpServletRequest request, 
			HttpServletResponse response, 
			@RequestParam("folderUuid") String folder_uuid,
			@RequestParam(value = "operation", required=false) String operationUuid,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer un vehicule
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS) && !SecurityUtils.transporteur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token) && !SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);
		String type_compte = jwtProvider.getTypeDeCompte(token);

		// chargement de l'offre si renseignée (=modification de l'offre)
		Operation operation = null;
		if (operationUuid != null && !"".equals(operationUuid)) {
			operation = operationService.getByUUID(operationUuid, code_pays);
		}

		if (SecurityUtils.transporteur(jwtProvider, token)) {
			UUID uuid_transporteur = jwtProvider.getUUIDFromJWT(token);
			UtilisateurDriver transporteur = transporteurRepository.findByUUID(uuid_transporteur, code_pays);
			if (!operation.getTransporteur().equals(transporteur)) {
				return null;
			}
		} else if (SecurityUtils.client(jwtProvider, token) || SecurityUtils.client_personnel(jwtProvider, token)) {
			String uuid_client = jwtProvider.getClaimsValue("uuid_client", token).toString();
			Client client = clientService.getByUUID(uuid_client, code_pays);
			if (operation != null && !operation.getClient().equals(client)) {
				return null;
			}
		}

		// création du dossier si il n'existe pas encore pour stocker toutes les photos du carroussel pour l'offre
		String tempDir = System.getProperty("java.io.tmpdir");
		File folderTempUUID = new File(tempDir + "/" + folder_uuid);
		FileUtils.createFolderIfNotExistWithRetry(folderTempUUID, 500);

		// écrit les photos dans le dossier
		Map<String, MultipartFile> fileMap = request.getFileMap();
		for (MultipartFile multipartFile : fileMap.values()) {
			String ordre_fichier = FileUtils.removeExtension(multipartFile.getOriginalFilename() );

			// vérifie si la photo n'existe pas déjà (l'ordre = le nom du fichier)
			// 1 - dans le dossier, cas de l'ajout d'une offre
			File[] directoryListing = folderTempUUID.listFiles();
			boolean trouve = false;
			if (directoryListing != null) {
				for (File child : directoryListing) {
					String ordre = FileUtils.removeExtension(child.getName());
					if (ordre.equals(FileUtils.removeExtension(multipartFile.getOriginalFilename()))) {
						trouve = true;
					}
				}
			}
			if (trouve) {
				return new ResponseEntity<Object>(null, HttpStatus.CONFLICT);
			}

					// 2 - en bdd, cas de la modification d'une offre
			if (operation != null) {
				Optional<OperationDocument> photo_carroussel = operationDocumentService.get(operation, ordre_fichier);
				if (photo_carroussel.isPresent()) {
					return new ResponseEntity<Object>(null, HttpStatus.CONFLICT);
				}
			}


			String outputFileName = folderTempUUID + "/" + multipartFile.getOriginalFilename();

			try {
				FileCopyUtils.copy(multipartFile.getBytes(), new FileOutputStream(outputFileName));
			} catch (FileNotFoundException e) {
				logger.error("FileNotFoundException lors de l'écriture du fichier " + multipartFile.getOriginalFilename(), e);
				return new ResponseEntity<Object>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			} catch (IOException e) {
				logger.error("IOException lors de l'écriture du fichier " + multipartFile.getOriginalFilename(), e);
				return new ResponseEntity<Object>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}

			// attache le document à l'opération si l'opération n'est pas null
			if (operation != null) {
				operationDocumentService.saveDocument(operation, new File(outputFileName), 10, type_compte);
			}

		}

		folderTempUUID.delete();
		return new ResponseEntity<Object>(true, HttpStatus.OK);
	}


	/**
	 * Stocke un document de l'opération
	 * @param postBody
	 * @return
	 */
	/*@RequestLogger(name = Application.microservice)*/
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Stocke un document de l'opération")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, transporteur ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'opération"),
			@ApiResponse(code = 200, message = "Document stocké", response = Boolean.class)
	})
	@RequestMapping(
			//produces = "application/json",
			//consumes = "multipart/form-data",
			value = "/operation/document",
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity<Object> addPhoto(MultipartFile file,
											@RequestParam(value = "operation", required=false) String operationUuid,
											@ApiParam(value = "Jeton JWT pour autentification", required = true) @RequestHeader("Token") String token) {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer un vehicule
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS) && !SecurityUtils.transporteur(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// chargement de l'offre si renseignée (=modification de l'offre)
		Operation operation = null;
		if (operationUuid != null && !"".equals(operationUuid)) {
			operation = operationService.getByUUID(operationUuid, jwtProvider.getCodePays(token));
		}

		if (SecurityUtils.transporteur(jwtProvider, token)) {
			UUID uuid_transporteur = jwtProvider.getUUIDFromJWT(token);
			UtilisateurDriver transporteur = transporteurRepository.findByUUID(uuid_transporteur, jwtProvider.getCodePays(token));
			if (!operation.getTransporteur().equals(transporteur)) {
				return null;
			}
		}

		// création du dossier si il n'existe pas encore pour stocker toutes les photos du carroussel pour l'offre
		String tempDir = System.getProperty("java.io.tmpdir");
		UUID uuid = UUID.randomUUID();
		String folder_uuid = uuid.toString();
		File folderTempUUID = new File(tempDir + "/" + folder_uuid);
		FileUtils.createFolderIfNotExistWithRetry(folderTempUUID, 500);

		// ecrit le fichier dans le répertoire
		try {
			file.transferTo(folderTempUUID);
		} catch (IOException e) {
			e.printStackTrace();
		}


		return new ResponseEntity<Object>(true, HttpStatus.OK);
	}


	/**
	 * Récupère le fichier image d'un des documents de l'opération
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère le fichier image d'un des documents de l'opération")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, transporteur ou opérateur )"),
			@ApiResponse(code = 200, message = "Fichier image", response = byte[].class)
	})
	@RequestMapping(
			produces = MediaType.APPLICATION_PDF_VALUE,
			value = "/operation/document", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity<Resource> getPhoto(
			@ApiParam(value = "UUID de la photo du document", required = true) @RequestParam("uuid") String uuid, 
			@ApiParam(value = "Jeton JWT pour autentification", required = true) @RequestParam("token") String token) throws IOException {

		// vérifie que le jeton est valide
		if (!jwtProvider.isValidJWT(token)) { 
			return null;
		}

		// vérifie que les droits lui permettent de afficher une commande
		if (!SecurityUtils.admin(jwtProvider, token) && !SecurityUtils.operateur(jwtProvider, token) && !SecurityUtils.transporteur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token) && !SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_OPERATIONS)) {
			return null;
		}

		String code_pays = jwtProvider.getCodePays(token);

		// chargement du document
		Optional<OperationDocument> document = operationDocumentService.load(uuid);
		if (!document.isPresent()) {
			return null;
		}

		if (SecurityUtils.transporteur(jwtProvider, token)) {
			UUID uuid_transporteur = jwtProvider.getUUIDFromJWT(token);
			UtilisateurDriver transporteur = transporteurRepository.findByUUID(uuid_transporteur, code_pays);
			if (!document.get().getOperation().getTransporteur().equals(transporteur)) {
				return null;
			}
		} else if (SecurityUtils.proprietaire(jwtProvider, token)) {
			UUID uuid_proprietaire = jwtProvider.getUUIDFromJWT(token);
			UtilisateurProprietaire proprietaire = proprietaireRepository.findByUUID(uuid_proprietaire, code_pays);
			if (document.get().getOperation().getVehicule() != null && !document.get().getOperation().getVehicule().getProprietaire().equals(proprietaire)) {
				return null;
			}
		} else if (SecurityUtils.client(jwtProvider, token) || SecurityUtils.client_personnel(jwtProvider, token)) {
			String uuid_client = jwtProvider.getClaimsValue("uuid_client", token).toString();
			Client client = clientService.getByUUID(uuid_client, code_pays);
			if (!document.get().getOperation().getClient().equals(client)) {
				return null;
			}
		}

		byte[] photo = operationDocumentService.get(uuid);
		if (photo != null && photo.length > 0) {

			InputStream resource = new ByteArrayInputStream(photo);
			String mimeType = URLConnection.guessContentTypeFromStream(resource);

			HttpHeaders headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
			headers.add("Pragma", "no-cache");
			headers.add("Expires", "0");
			headers.add("Content-Disposition", "attachment; filename=\"" + document.get().getFilename() + "." + document.get().getExtension() + "\"");
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			return ResponseEntity.ok()
					.headers(headers)
					.contentLength(photo.length)
					.body(new InputStreamResource(resource));

			//return photo;

		}
		return null;

	}


	/**
	 * Top X des destinations
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Top X des destinations")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin ou opérateurs ayant le droit)"),
			@ApiResponse(code = 200, message = "Séries (en ordonnées c'est le nombre total et la durée moyenne des courses) + tickes (noms des communes)")
	})
	@RequestMapping(
			produces = "application/json",
			value = "/statistiques/destinations/top",
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity getTopDestinations(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody StatistiqueTopDestinationParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.CONSULTER_STATISTIQUES)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		JSONObject res = new JSONObject();
		JSONArray arr_nb_totals_courses = new JSONArray();
		JSONArray arr_nb_durees_courses = new JSONArray();

		JSONArray arr_series = new JSONArray();
		JSONArray arr_ticks = new JSONArray();

		String codePays = jwtProvider.getCodePays(token);

		// filtrage par client
		List<String> criteres = new ArrayList<String>();
		if (postBody.getClient() != null && !postBody.getClient().equals("") && !postBody.getClient().equals("null")) {
			criteres.add("client = '" + postBody.getClient() + "'");
		}

		// filtrage par date
		if (postBody.getPeriode() != null && !postBody.getPeriode().equals("")) {
			String[] arr = postBody.getPeriode().split("-");
			String periode_debut = arr[0].trim();
			String periode_fin = arr[1].trim();
			criteres.add("departDateProgrammeeOperation BETWEEN '" + DateUtils.reverseDateFrancaiseToUS(periode_debut) + "' AND '" + DateUtils.reverseDateFrancaiseToUS(periode_fin) + "'");
		}

		// filtrage par types d'opérations
		boolean duree = false;
		String sql_duree = "";
		if (postBody.getOperations() != null && !postBody.getOperations().equals("")) {
			if ("0".equals(postBody.getOperations())) {
				// non réalisés
				criteres.add("statut IN ('" + OperationStatut.ENREGISTRE.toString() + "', '" + OperationStatut.VALIDE.toString() + "', '" + OperationStatut.EN_COURS_DE_TRAITEMENT.toString() + "')");
			} else if ("1".equals(postBody.getOperations())) {
				// réalisées
				criteres.add("statut IN ('" + OperationStatut.DECHARGEMENT_TERMINE.toString() + "')");
				duree = true;
				sql_duree = ", AVG(ABS((TIME_TO_SEC(dateHeureDechargementTermine) - TIME_TO_SEC(departDateOperation))/60))";
			}
		}

		// segmntation par pays
		criteres.add(" codePays = '" + codePays + "' ");


		String whereClause = StringUtils.join(criteres, " AND ");


		String sql = "SELECT o.arriveeAdresseVille, COUNT(uuid) " + sql_duree + " FROM Operation o WHERE " + whereClause + " GROUP BY arriveeAdresseVille ORDER BY COUNT(uuid) DESC";
		TypedQuery<Object[]> query = (TypedQuery<Object[]>) entityManager.createQuery(sql);
		query.setMaxResults(postBody.getNb());
		List<Object[]> operations = query.getResultList();
		for (int i=0; i<operations.size(); i++) {

			String commune = operations.get(i)[0].toString();
			Long total_courses = Long.valueOf(operations.get(i)[1].toString());

			Number duree_moyenne = null;
			try {
				if (operations.get(i) != null && operations.get(i).length > 2 && operations.get(i)[2] != null) {
					duree_moyenne = NumberFormat.getNumberInstance(Locale.US).parse(operations.get(i)[2].toString());
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}

			arr_nb_totals_courses.add(total_courses);
			arr_nb_durees_courses.add(duree_moyenne);
			arr_ticks.add(commune);
		}


		arr_series.add(arr_nb_totals_courses);
		if (duree) {
			arr_series.add(arr_nb_durees_courses);
		}
		res.put("series", arr_series);
		res.put("ticks", arr_ticks);

		actionAuditService.statTopsDestinations(token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(res), HttpStatus.OK);
	}



	/**
	 * Compte le nombre de commande en cours liés à un véhicule
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Compte le nombre de commande en cours liés à un véhicule")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (reservés aux transporteurs ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de charger le véhicule."),
			@ApiResponse(code = 200, message = "Nombre de commande en cours liés à un véhicule", response = Long.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operation/vehicule/a_venir/compter", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity getCompterOperationsAVenir(
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher une operation
		if (!SecurityUtils.transporteur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		List<Vehicule> vehicules = new ArrayList<Vehicule>();
		UtilisateurDriver transporteur = null;
		Long nb = new Long(0);
		if (SecurityUtils.transporteur(jwtProvider, token)) {
			String uuid_vehicule = jwtProvider.getClaimsValue("uuid_vehicule", token);
			transporteur = transporteurRepository.findByUUID(jwtProvider.getUUIDFromJWT(token), jwtProvider.getCodePays(token));

			Vehicule vehicule = vehiculeService.getByUUID(uuid_vehicule, jwtProvider.getCodePays(token));
			if (vehicule == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger le véhicule.");
			}
			vehicules.add(vehicule);

			if (!vehicules.isEmpty()) {
				nb = operationService.countNbOperationsProgrammees(vehicules, jwtProvider.getCodePays(token), transporteur);
			}
		} else if (SecurityUtils.proprietaire(jwtProvider, token)) {
			UUID uuid_proprietaire = jwtProvider.getUUIDFromJWT(token);
			UtilisateurProprietaire proprietaire = proprietaireRepository.findByUUID(uuid_proprietaire, jwtProvider.getCodePays(token));
			vehicules.addAll(vehiculeService.getByProprietaire(proprietaire, jwtProvider.getCodePays(token)));

			if (!vehicules.isEmpty()) {
				nb = operationService.countNbOperationsProgrammees(vehicules, jwtProvider.getCodePays(token));
			}
		}




		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(nb), HttpStatus.OK);
	}



	/**
	 * Compte le nombre de commande en cours liés à un véhicule
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Compte le nombre de commande en cours liés à un véhicule")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (reservés aux transporteurs ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de charger le véhicule."),
			@ApiResponse(code = 200, message = "Nombre de commande en cours liés à un véhicule", response = Long.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operation/vehicule/en_cours/compter", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity getCompterOperationsEnCours(
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher une operation
		if (!SecurityUtils.transporteur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}
		Long nb = new Long(0);

		// filtrage par véhicules
		UtilisateurDriver transporteur = null;
		List<Vehicule> vehicules = new ArrayList<Vehicule>();
		if (SecurityUtils.transporteur(jwtProvider, token)) {
			String uuid_vehicule = jwtProvider.getClaimsValue("uuid_vehicule", token);
			transporteur = transporteurRepository.findByUUID(jwtProvider.getUUIDFromJWT(token), jwtProvider.getCodePays(token));
			Vehicule vehicule = vehiculeService.getByUUID(uuid_vehicule, jwtProvider.getCodePays(token));
			if (vehicule == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger le véhicule.");
			}
			vehicules.add(vehicule);

			if (!vehicules.isEmpty()) {
				nb = operationService.countNbOperationsEnCours(vehicules, jwtProvider.getCodePays(token), transporteur);
			}

		} else if (SecurityUtils.proprietaire(jwtProvider, token)) {

			UUID uuid_proprietaire = jwtProvider.getUUIDFromJWT(token);
			UtilisateurProprietaire proprietaire = proprietaireRepository.findByUUID(uuid_proprietaire, jwtProvider.getCodePays(token));
			vehicules.addAll(vehiculeService.getByProprietaire(proprietaire, jwtProvider.getCodePays(token)));

			if (!vehicules.isEmpty()) {
				nb = operationService.countNbOperationsEnCours(vehicules, jwtProvider.getCodePays(token));
			}
		}




		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(nb), HttpStatus.OK);
	}




}
