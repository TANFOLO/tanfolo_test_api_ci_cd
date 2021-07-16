package com.kamtar.transport.api.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.enums.*;
import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.repository.UtilisateurProprietaireRepository;
import com.kamtar.transport.api.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
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
import com.kamtar.transport.api.criteria.OperationSpecificationsBuilder;
import com.kamtar.transport.api.criteria.ParentSpecificationsBuilder;
import com.kamtar.transport.api.criteria.PredicateUtils;
import com.kamtar.transport.api.params.CreateEditOperationAppelOffreParams;
import com.kamtar.transport.api.params.PropositionOperationAppelOffreParams;
import com.kamtar.transport.api.params.RefuserOperationAppelOffreParams;
import com.kamtar.transport.api.repository.UtilisateurDriverRepository;
import com.kamtar.transport.api.security.SecurityUtils;
import com.kamtar.transport.api.swagger.ListOperationAppelOffre;
import com.kamtar.transport.api.utils.JWTProvider;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@Api(value="Gestion des appels d'offres des opérations auprès des drivers", description="Gestion des appels d'offres des opérations auprès des drivers")
@RestController
@EnableWebMvc
public class OperationAppelOffreController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(OperationAppelOffreController.class);  

	@Autowired
	CountryService countryService;

	@Autowired
	MapperJSONService mapperJSONService;

	@Autowired
	OperationService operationService;

	@Autowired
	VehiculeService vehiculeService;

	@Autowired
	UtilisateurProprietaireRepository proprietaireRepository;

	@Autowired
	OperationAppelOffreService operationAppelOffreService;

	@Autowired
	ClientService clientService;

	@Autowired
	NotificationService notificationService;

	@Autowired
	UtilisateurDriverService utilisateurDriverService;

	@Autowired
	UtilisateurProprietaireService utilisateurProprietaireService;

	@Autowired
	UtilisateurOperateurKamtarService utilisateurOperateurKamtarService;

	@Autowired
	UtilisateurClientService clientExpediteurService;

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	ActionAuditService actionAuditService;

	@Autowired
	UtilisateurDriverService utilisateurTransporteurService;

	@Autowired
	OperationChangementStatutService operationChangementStatutService;



	/**
	 * Création/modification d'un appel d'offre
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Création/modification d'un appel d'offre")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "Impossible de trouver l'opération"),
			@ApiResponse(code = 404, message = "Impossible de trouver le véhicule"),
			@ApiResponse(code = 404, message = "Impossible de trouver le driver principal du véhicule"),
			@ApiResponse(code = 404, message = "Impossible de trouver le propriétaire du véhicule"),
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'opération"),
			@ApiResponse(code = 200, message = "Appel d'offre de l'opération mise à jour", response = OperationAppelOffre.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/operation/appel_offre", 
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity edit(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody CreateEditOperationAppelOffreParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que 
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}


		UtilisateurOperateurKamtar operateur = utilisateurOperateurKamtarService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), jwtProvider.getCodePays(token));

		List<OperationAppelOffre> operations = operationAppelOffreService.edit(postBody, operateur, token);
		if (operations != null) {
			for (OperationAppelOffre operation : operations) {
				actionAuditService.creerOperationAppelOffre(operation, token);
			}
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operations), HttpStatus.CREATED);

		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible d'enregistrer l'appel d'offre.");

	}

	/**
	 * Refuser une proposition pour une opération par le driver ou le propriétaire
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Refuser une proposition pour une opération par le driver ou le propriétaire")
	@ApiResponses(value = {	
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (transporteur ou propriétaire ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'appel d'offre"),
			@ApiResponse(code = 404, message = "Impossible de trouver le véhicule"),
			@ApiResponse(code = 403, message = "Vous ne pouvez plus proposer de prix sur une opération en cours"),
			@ApiResponse(code = 403, message = "Vous ne pouvez plus proposer de prix sur une opération où le véhicule a été affecté"),
			@ApiResponse(code = 403, message = "Vous n'avez pas le droit d'accéder à ce véhicule"),
			@ApiResponse(code = 200, message = "Proposition envoyée")
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/operation/appel_offre/refuser", 
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity refuser(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody RefuserOperationAppelOffreParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que 
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une operation
		if (!SecurityUtils.transporteur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// chargement de l'opération
		OperationAppelOffre operation_appel_offre = operationAppelOffreService.getByUUID(postBody.getId_operation_appel_offre().toString(), jwtProvider.getCodePays(token));
		if (operation_appel_offre == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'appel d'offre.");
		}

		Operation operation = operation_appel_offre.getOperation();
		if (operation == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le véhicule.");
		} else if (!operation.getStatut().equals(OperationStatut.ENREGISTRE.toString()) &&!operation.getStatut().equals(OperationStatut.EN_COURS_DE_TRAITEMENT.toString())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez plus proposer de prix sur une opération en cours");
		} else if (operation.getVehicule() != null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez plus proposer de prix sur une opération où le véhicule a été affecté");
		}

		Vehicule vehicule = operation_appel_offre.getVehicule();

		// si c'est un driver, vérifie qu'il est bien driver principal du véhicule
		UtilisateurDriver driver = null;
		if (SecurityUtils.transporteur(jwtProvider, token)) {
			UUID uuid = jwtProvider.getUUIDFromJWT(token);
			driver = utilisateurDriverService.getByUUID(uuid.toString(), jwtProvider.getCodePays(token));

			if (!vehicule.getDriverPrincipal().equals(driver)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'accéder à ce véhicule");
			}
		}

		// si c'est un propriétaire, vérifie qu'il est bien propriétaire du véhicule
		UtilisateurProprietaire proprietaire = null;
		if (SecurityUtils.proprietaire(jwtProvider, token)) {
			UUID uuid = jwtProvider.getUUIDFromJWT(token);
			proprietaire = utilisateurProprietaireService.getByUUID(uuid.toString(), jwtProvider.getCodePays(token));

			if (!vehicule.getProprietaire().equals(proprietaire)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'accéder à ce véhicule");
			}
		}		



		operation_appel_offre.setStatut("3");
		operationAppelOffreService.save(operation_appel_offre);

		actionAuditService.getOperationsAppelOffreOperation(token, operation);

		// envoi de la notification au backoffice
		Notification notification = new Notification(NotificationType.WEB_BACKOFFICE.toString(), null, "Demande refusée par un driver", operation.getUuid().toString(), null, operation.getCodePays());
		notificationService.create(notification, jwtProvider.getCodePays(token));


		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operation_appel_offre), HttpStatus.OK);

	}

	/**
	 * Envoi d'une proposition pour une opération par le driver ou le propriétaire
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Envoi d'une proposition pour une opération par le driver ou le propriétaire")
	@ApiResponses(value = {	
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'appel d'offre."),
			@ApiResponse(code = 404, message = "Impossible de trouver le véhicule."),
			@ApiResponse(code = 403, message = "Vous ne pouvez plus proposer de prix sur une opération en cours"),
			@ApiResponse(code = 403, message = "Vous ne pouvez plus proposer de prix sur une opération où le véhicule a été affecté"),
			@ApiResponse(code = 403, message = "Vous n'avez pas le droit d'accéder à ce véhicule"),
			@ApiResponse(code = 200, message = "Proposition envoyée")
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/operation/appel_offre/proposition", 
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity proposer(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody PropositionOperationAppelOffreParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que 
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une operation
		if (!SecurityUtils.transporteur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// chargement de l'opération
		OperationAppelOffre operation_appel_offre = operationAppelOffreService.getByUUID(postBody.getId_operation_appel_offre().toString(), jwtProvider.getCodePays(token));
		if (operation_appel_offre == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'appel d'offre.");
		}

		Operation operation = operation_appel_offre.getOperation();
		if (operation == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le véhicule.");
		} else if (!operation.getStatut().equals(OperationStatut.ENREGISTRE.toString()) && !operation.getStatut().equals(OperationStatut.EN_COURS_DE_TRAITEMENT.toString())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez plus proposer de prix sur une opération en cours");
		} else if (operation.getVehicule() != null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez plus proposer de prix sur une opération où le véhicule a été affecté");
		}
		Vehicule vehicule = operation_appel_offre.getVehicule();


		// si c'est un driver, vérifie qu'il est bien driver principal du véhicule
		UtilisateurDriver driver = null;
		if (SecurityUtils.transporteur(jwtProvider, token)) {
			UUID uuid = jwtProvider.getUUIDFromJWT(token);
			driver = utilisateurDriverService.getByUUID(uuid.toString(), jwtProvider.getCodePays(token));

			if (!vehicule.getDriverPrincipal().equals(driver)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'accéder à ce véhicule");
			}
		}

		// si c'est un propriétaire, vérifie qu'il est bien propriétaire du véhicule
		UtilisateurProprietaire proprietaire = null;
		if (SecurityUtils.proprietaire(jwtProvider, token)) {
			UUID uuid = jwtProvider.getUUIDFromJWT(token);
			proprietaire = utilisateurProprietaireService.getByUUID(uuid.toString(), jwtProvider.getCodePays(token));

			if (!vehicule.getProprietaire().equals(proprietaire)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'accéder à ce véhicule");
			}
		}		



		operation_appel_offre.setMontantPropose(postBody.getMontant());
		operation_appel_offre.setMontantProposeDevise(postBody.getMontant_devise());
		operation_appel_offre.setStatut("2");
		operationAppelOffreService.save(operation_appel_offre);

		actionAuditService.getOperationsAppelOffreOperation(token, operation);

		// envoi de la notification au backoffice
		Notification notification = new Notification(NotificationType.WEB_BACKOFFICE.toString(), null, "Demande négociée par un driver", operation.getUuid().toString(), null, operation.getCodePays());
		notificationService.create(notification, jwtProvider.getCodePays(token));


		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operation_appel_offre), HttpStatus.OK);

	}



	/**
	 * Récupère les informations des appels d'offre d'une opération
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère les informations des appels d'offre d'une opération")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin ou opérateurs ayant droits)"),
			@ApiResponse(code = 404, message = "Impossible de trouver les appels d'offre d'une operation"),
			@ApiResponse(code = 200, message = "Liste des appels d'offre d'une operation", response = ListOperationAppelOffre.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operation/appels_offre", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity getOperationsTransporteurs(
			@ApiParam(value = "UUID operation", required = true) @RequestParam("operation") String uuid,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}


		Operation operation = operationService.getByUUID(uuid, jwtProvider.getCodePays(token));

		// filtrage
		List<String> colonnesFiltrageActive = Arrays.asList();
		ParentSpecificationsBuilder builder = new OperationSpecificationsBuilder();
		builder.with("operation", PredicateUtils.OPERATEUR_EGAL, operation, false);
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
		List<OperationAppelOffre> operations = operationAppelOffreService.getOperationsAppelsOffre("createdOn", Sort.Direction.DESC.toString(), 0, 999999, spec).getContent();
		actionAuditService.getOperationsAppelOffreOperation(token, operation);
		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operations), HttpStatus.OK);
	}

	/**
	 * Récupère les appels d'offres sur un statut, et éventuellement à un véhicule
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère les appels d'offres sur un statut, et éventuellement à un véhicule")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (reservés aux transporteurs, propriétaire, admin ou opérateurs ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver les appels d'offres"),
			@ApiResponse(code = 404, message = "Impossible de charger le véhicule."),
			@ApiResponse(code = 200, message = "Liste de appels d'offres", response = ListOperationAppelOffre.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/appels_offre", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity getListeAppelOffres(
			@ApiParam(value = "1 = afficher les appels d'offres déjà repondus (par défaut), 0 = masquer les appels d'offres déjà repondus", required = false) @Param("deja_repondus") Integer deja_repondus,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS) && !SecurityUtils.transporteur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// filtrage
		List<String> colonnesFiltrageActive = Arrays.asList();
		ParentSpecificationsBuilder builder = new OperationSpecificationsBuilder();

		// filtrage par véhicules
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


		builder.with("vehicule", PredicateUtils.OPERATEUR_IN, vehicules, false);

		// filtrage sur statut
		builder.with("statut", PredicateUtils.OPERATEUR_IS_NULL, null, false);


		Specification spec = builder.build();

		// chargement
		List<OperationAppelOffre> operations_appel_offres = new ArrayList<OperationAppelOffre> ();
		if (!vehicules.isEmpty()) {
			operations_appel_offres = operationAppelOffreService.getOperationsAppelsOffre(vehicules, jwtProvider.getCodePays(token));
		}

		// filtre sur les opérations où le véhicule n'a pas encore été affecté et où l'opératiob est en "enregistré"
		List<OperationAppelOffre> operations_filtrees = new ArrayList<OperationAppelOffre>();
		for (OperationAppelOffre operation_appel_offre : operations_appel_offres) {
			if ((operation_appel_offre.getOperation().getStatut().equals(OperationStatut.ENREGISTRE.toString()) || operation_appel_offre.getOperation().getStatut().equals(OperationStatut.EN_COURS_DE_TRAITEMENT.toString())) && operation_appel_offre.getOperation().getVehicule() == null) {
				operations_filtrees.add(operation_appel_offre);
			}
		}


		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operations_filtrees), HttpStatus.OK);
	}
	
	
	/**
	 * Compte le nombre d'appels d'offres à répondre, liés à un véhicule
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Compte le nombre d'appels d'offres à répondre, liés à un véhicule")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (reservés aux transporteurs ou propriétaires)"),
			@ApiResponse(code = 404, message = "Impossible de trouver les appels d'offres"),
			@ApiResponse(code = 404, message = "Impossible de charger le véhicule."),
			@ApiResponse(code = 200, message = "Liste de appels d'offres", response = ListOperationAppelOffre.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/appels_offre/vehicule/compter", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity getAppelOffresCompterVehicules(
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

		// compter
		Long nb = new Long(0);
		if (!vehicules.isEmpty()) {
			nb = operationAppelOffreService.compterAppelOffresNonRepondus(vehicules, jwtProvider.getCodePays(token));
		}


		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(nb), HttpStatus.OK);
	}


}
