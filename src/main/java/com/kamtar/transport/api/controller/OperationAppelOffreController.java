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


@Api(value="Gestion des appels d'offres des op??rations aupr??s des drivers", description="Gestion des appels d'offres des op??rations aupr??s des drivers")
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
	 * Cr??ation/modification d'un appel d'offre
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Cr??ation/modification d'un appel d'offre")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "Impossible de trouver l'op??ration"),
			@ApiResponse(code = 404, message = "Impossible de trouver le v??hicule"),
			@ApiResponse(code = 404, message = "Impossible de trouver le driver principal du v??hicule"),
			@ApiResponse(code = 404, message = "Impossible de trouver le propri??taire du v??hicule"),
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (admin, ou op??rateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'op??ration"),
			@ApiResponse(code = 200, message = "Appel d'offre de l'op??ration mise ?? jour", response = OperationAppelOffre.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/operation/appel_offre", 
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity edit(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody CreateEditOperationAppelOffreParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que 
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// v??rifie que les droits lui permettent de g??rer une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
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
	 * Refuser une proposition pour une op??ration par le driver ou le propri??taire
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Refuser une proposition pour une op??ration par le driver ou le propri??taire")
	@ApiResponses(value = {	
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (transporteur ou propri??taire ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'appel d'offre"),
			@ApiResponse(code = 404, message = "Impossible de trouver le v??hicule"),
			@ApiResponse(code = 403, message = "Vous ne pouvez plus proposer de prix sur une op??ration en cours"),
			@ApiResponse(code = 403, message = "Vous ne pouvez plus proposer de prix sur une op??ration o?? le v??hicule a ??t?? affect??"),
			@ApiResponse(code = 403, message = "Vous n'avez pas le droit d'acc??der ?? ce v??hicule"),
			@ApiResponse(code = 200, message = "Proposition envoy??e")
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/operation/appel_offre/refuser", 
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity refuser(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody RefuserOperationAppelOffreParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que 
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// v??rifie que les droits lui permettent de g??rer une operation
		if (!SecurityUtils.transporteur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		// chargement de l'op??ration
		OperationAppelOffre operation_appel_offre = operationAppelOffreService.getByUUID(postBody.getId_operation_appel_offre().toString(), jwtProvider.getCodePays(token));
		if (operation_appel_offre == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'appel d'offre.");
		}

		Operation operation = operation_appel_offre.getOperation();
		if (operation == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le v??hicule.");
		} else if (!operation.getStatut().equals(OperationStatut.ENREGISTRE.toString()) &&!operation.getStatut().equals(OperationStatut.EN_COURS_DE_TRAITEMENT.toString())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez plus proposer de prix sur une op??ration en cours");
		} else if (operation.getVehicule() != null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez plus proposer de prix sur une op??ration o?? le v??hicule a ??t?? affect??");
		}

		Vehicule vehicule = operation_appel_offre.getVehicule();

		// si c'est un driver, v??rifie qu'il est bien driver principal du v??hicule
		UtilisateurDriver driver = null;
		if (SecurityUtils.transporteur(jwtProvider, token)) {
			UUID uuid = jwtProvider.getUUIDFromJWT(token);
			driver = utilisateurDriverService.getByUUID(uuid.toString(), jwtProvider.getCodePays(token));

			if (!vehicule.getDriverPrincipal().equals(driver)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'acc??der ?? ce v??hicule");
			}
		}

		// si c'est un propri??taire, v??rifie qu'il est bien propri??taire du v??hicule
		UtilisateurProprietaire proprietaire = null;
		if (SecurityUtils.proprietaire(jwtProvider, token)) {
			UUID uuid = jwtProvider.getUUIDFromJWT(token);
			proprietaire = utilisateurProprietaireService.getByUUID(uuid.toString(), jwtProvider.getCodePays(token));

			if (!vehicule.getProprietaire().equals(proprietaire)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'acc??der ?? ce v??hicule");
			}
		}		



		operation_appel_offre.setStatut("3");
		operationAppelOffreService.save(operation_appel_offre);

		actionAuditService.getOperationsAppelOffreOperation(token, operation);

		// envoi de la notification au backoffice
		Notification notification = new Notification(NotificationType.WEB_BACKOFFICE.toString(), null, "Demande refus??e par un driver", operation.getUuid().toString(), null, operation.getCodePays());
		notificationService.create(notification, jwtProvider.getCodePays(token));


		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operation_appel_offre), HttpStatus.OK);

	}

	/**
	 * Envoi d'une proposition pour une op??ration par le driver ou le propri??taire
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Envoi d'une proposition pour une op??ration par le driver ou le propri??taire")
	@ApiResponses(value = {	
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (admin, ou op??rateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'appel d'offre."),
			@ApiResponse(code = 404, message = "Impossible de trouver le v??hicule."),
			@ApiResponse(code = 403, message = "Vous ne pouvez plus proposer de prix sur une op??ration en cours"),
			@ApiResponse(code = 403, message = "Vous ne pouvez plus proposer de prix sur une op??ration o?? le v??hicule a ??t?? affect??"),
			@ApiResponse(code = 403, message = "Vous n'avez pas le droit d'acc??der ?? ce v??hicule"),
			@ApiResponse(code = 200, message = "Proposition envoy??e")
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/operation/appel_offre/proposition", 
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity proposer(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody PropositionOperationAppelOffreParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que 
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// v??rifie que les droits lui permettent de g??rer une operation
		if (!SecurityUtils.transporteur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		// chargement de l'op??ration
		OperationAppelOffre operation_appel_offre = operationAppelOffreService.getByUUID(postBody.getId_operation_appel_offre().toString(), jwtProvider.getCodePays(token));
		if (operation_appel_offre == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'appel d'offre.");
		}

		Operation operation = operation_appel_offre.getOperation();
		if (operation == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le v??hicule.");
		} else if (!operation.getStatut().equals(OperationStatut.ENREGISTRE.toString()) && !operation.getStatut().equals(OperationStatut.EN_COURS_DE_TRAITEMENT.toString())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez plus proposer de prix sur une op??ration en cours");
		} else if (operation.getVehicule() != null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez plus proposer de prix sur une op??ration o?? le v??hicule a ??t?? affect??");
		}
		Vehicule vehicule = operation_appel_offre.getVehicule();


		// si c'est un driver, v??rifie qu'il est bien driver principal du v??hicule
		UtilisateurDriver driver = null;
		if (SecurityUtils.transporteur(jwtProvider, token)) {
			UUID uuid = jwtProvider.getUUIDFromJWT(token);
			driver = utilisateurDriverService.getByUUID(uuid.toString(), jwtProvider.getCodePays(token));

			if (!vehicule.getDriverPrincipal().equals(driver)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'acc??der ?? ce v??hicule");
			}
		}

		// si c'est un propri??taire, v??rifie qu'il est bien propri??taire du v??hicule
		UtilisateurProprietaire proprietaire = null;
		if (SecurityUtils.proprietaire(jwtProvider, token)) {
			UUID uuid = jwtProvider.getUUIDFromJWT(token);
			proprietaire = utilisateurProprietaireService.getByUUID(uuid.toString(), jwtProvider.getCodePays(token));

			if (!vehicule.getProprietaire().equals(proprietaire)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'acc??der ?? ce v??hicule");
			}
		}		



		operation_appel_offre.setMontantPropose(postBody.getMontant());
		operation_appel_offre.setMontantProposeDevise(postBody.getMontant_devise());
		operation_appel_offre.setStatut("2");
		operationAppelOffreService.save(operation_appel_offre);

		actionAuditService.getOperationsAppelOffreOperation(token, operation);

		// envoi de la notification au backoffice
		Notification notification = new Notification(NotificationType.WEB_BACKOFFICE.toString(), null, "Demande n??goci??e par un driver", operation.getUuid().toString(), null, operation.getCodePays());
		notificationService.create(notification, jwtProvider.getCodePays(token));


		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operation_appel_offre), HttpStatus.OK);

	}



	/**
	 * R??cup??re les informations des appels d'offre d'une op??ration
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "R??cup??re les informations des appels d'offre d'une op??ration")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (admin ou op??rateurs ayant droits)"),
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

		// v??rifie que le jeton est valide et que les droits lui permettent de cr??er un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// v??rifie que les droits lui permettent de afficher une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
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
	 * R??cup??re les appels d'offres sur un statut, et ??ventuellement ?? un v??hicule
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "R??cup??re les appels d'offres sur un statut, et ??ventuellement ?? un v??hicule")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv??s aux transporteurs, propri??taire, admin ou op??rateurs ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver les appels d'offres"),
			@ApiResponse(code = 404, message = "Impossible de charger le v??hicule."),
			@ApiResponse(code = 200, message = "Liste de appels d'offres", response = ListOperationAppelOffre.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/appels_offre", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity getListeAppelOffres(
			@ApiParam(value = "1 = afficher les appels d'offres d??j?? repondus (par d??faut), 0 = masquer les appels d'offres d??j?? repondus", required = false) @Param("deja_repondus") Integer deja_repondus,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de cr??er un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// v??rifie que les droits lui permettent de afficher une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS) && !SecurityUtils.transporteur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		// filtrage
		List<String> colonnesFiltrageActive = Arrays.asList();
		ParentSpecificationsBuilder builder = new OperationSpecificationsBuilder();

		// filtrage par v??hicules
		List<Vehicule> vehicules = new ArrayList<Vehicule>();
		if (SecurityUtils.transporteur(jwtProvider, token)) {
			String uuid_vehicule = jwtProvider.getClaimsValue("uuid_vehicule", token);
			Vehicule vehicule = vehiculeService.getByUUID(uuid_vehicule, jwtProvider.getCodePays(token));
			if (vehicule == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger le v??hicule.");
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

		// filtre sur les op??rations o?? le v??hicule n'a pas encore ??t?? affect?? et o?? l'op??ratiob est en "enregistr??"
		List<OperationAppelOffre> operations_filtrees = new ArrayList<OperationAppelOffre>();
		for (OperationAppelOffre operation_appel_offre : operations_appel_offres) {
			if ((operation_appel_offre.getOperation().getStatut().equals(OperationStatut.ENREGISTRE.toString()) || operation_appel_offre.getOperation().getStatut().equals(OperationStatut.EN_COURS_DE_TRAITEMENT.toString())) && operation_appel_offre.getOperation().getVehicule() == null) {
				operations_filtrees.add(operation_appel_offre);
			}
		}


		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(operations_filtrees), HttpStatus.OK);
	}
	
	
	/**
	 * Compte le nombre d'appels d'offres ?? r??pondre, li??s ?? un v??hicule
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Compte le nombre d'appels d'offres ?? r??pondre, li??s ?? un v??hicule")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv??s aux transporteurs ou propri??taires)"),
			@ApiResponse(code = 404, message = "Impossible de trouver les appels d'offres"),
			@ApiResponse(code = 404, message = "Impossible de charger le v??hicule."),
			@ApiResponse(code = 200, message = "Liste de appels d'offres", response = ListOperationAppelOffre.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/appels_offre/vehicule/compter", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity getAppelOffresCompterVehicules(
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de cr??er un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// v??rifie que les droits lui permettent de afficher une operation
		if (!SecurityUtils.transporteur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		List<Vehicule> vehicules = new ArrayList<Vehicule>();
		if (SecurityUtils.transporteur(jwtProvider, token)) {
			String uuid_vehicule = jwtProvider.getClaimsValue("uuid_vehicule", token);
			Vehicule vehicule = vehiculeService.getByUUID(uuid_vehicule, jwtProvider.getCodePays(token));
			if (vehicule == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger le v??hicule.");
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
