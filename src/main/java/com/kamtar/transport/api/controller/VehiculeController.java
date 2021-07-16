package com.kamtar.transport.api.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.criteria.OperationSpecificationsBuilder;
import com.kamtar.transport.api.enums.ClientPersonnelListeDeDroits;
import com.kamtar.transport.api.enums.OperationStatut;
import com.kamtar.transport.api.enums.UtilisateurTypeDeCompte;
import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.*;
import com.kamtar.transport.api.repository.GeolocRepository;
import com.kamtar.transport.api.service.*;
import com.kamtar.transport.api.swagger.*;
import com.kamtar.transport.api.utils.ExportUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
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
import com.kamtar.transport.api.criteria.ParentSpecificationsBuilder;
import com.kamtar.transport.api.criteria.PredicateUtils;
import com.kamtar.transport.api.criteria.VehiculeSpecificationsBuilder;
import com.kamtar.transport.api.enums.OperateurListeDeDroits;
import com.kamtar.transport.api.security.SecurityUtils;
import com.kamtar.transport.api.utils.DatatableUtils;
import com.kamtar.transport.api.utils.JWTProvider;
import com.wbc.core.utils.FileUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@Api(value="Gestion des véhicules", description="API Rest qui gère l'ensemble des véhicules")
@RestController
@EnableWebMvc
public class VehiculeController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(VehiculeController.class);


	@Value("${kamtar.env}")
	private String kamtar_env;

	@Autowired
	CountryService countryService;

	@Autowired
	ExportExcelService exportExcelService;

	@Autowired
	GeolocRepository geolocRepository;

	@Autowired
	GeolocService geolocService;

	@Autowired
	ClientService clientService;

	@Autowired
	MapperJSONService mapperJSONService;

	@Autowired
	OperationAppelOffreService operationAppelOffreService;
	
	@Autowired
	VehiculeService vehiculeService;

	@Autowired
	UtilisateurClientService clientExpediteurService;

	@Autowired
	UtilisateurClientPersonnelService utilisateurClientPersonnelService;

	@Autowired
	UtilisateurProprietaireService utilisateurProprietaireService;

	@Autowired
	UtilisateurDriverService utilisateurDriverService;

	@Autowired
	UtilisateurOperateurKamtarService utilisateurOperateurKamtarService;

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	ActionAuditService actionAuditService;

	@Autowired
	OperationService operationService;

	@Autowired
	VehiculePhotoService vehiculePhotoService;

	@Autowired
	VehiculeTypeService vehiculeTypeService;

	@Autowired
	VehiculeCarrosserieService vehiculeCarrosserieService;
	
	
	/**
	 * Création d'un véhicule depuis public
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Enregistrement d'un vehicule")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Impossible d'enregistrer le vehicule."),
			@ApiResponse(code = 201, message = "Retourne le véhicule créé", response = Vehicule.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/vehicule/public", 
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity create(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody CreateComptePublicParams postBody1) throws JsonProcessingException {

		// enregistrement
		Vehicule vehicule = vehiculeService.create(postBody1, postBody1.getPays());
		if (vehicule != null) {
			return new ResponseEntity<>(mapperJSONService.get(null).writeValueAsString(vehicule), HttpStatus.CREATED);
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible d'enregistrer le vehicule.");

	}

	

	/**
	 * Création d'un véhicule par le backoffice
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Enregistrement d'un vehicule")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Impossible d'enregistrer le vehicule."),
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (uniquement les admin et opérateurs ayant le droit)"),
			@ApiResponse(code = 201, message = "Retourne le véhicule créé", response = Vehicule.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/vehicule", 
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity create(

			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody CreateVehiculeParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que 
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer un vehicule
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_VEHICULE) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		UtilisateurOperateurKamtar operateur = utilisateurOperateurKamtarService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), jwtProvider.getCodePays(token));

		// enregistrement
		Vehicule vehicule = vehiculeService.create(postBody, operateur, token);
		if (vehicule != null) {
			actionAuditService.creerVehicule(vehicule, token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(vehicule), HttpStatus.CREATED);
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible d'enregistrer le vehicule.");

	}

	/**
	 * Vérification avant création d'un véhicule par le public
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Vérification des informations avant création d'un vehicule")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Le numéro d'immatriculation existe déjà."),
			@ApiResponse(code = 400, message = "Le code du pays d'immatriculation n'existe pas."),
			@ApiResponse(code = 400, message = "Erreur de vérification du véhicule"),
			@ApiResponse(code = 200, message = "Vérifications OK")
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/vehicule/verifications", 
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity verification(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody CreateVehiculePublicParams postBody) {

		// vérification sur l'immatriculation
		if (vehiculeService.immatriculationExist(postBody.getImmatriculation(), postBody.getPays())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le numéro d'immatriculation existe déjà.");
		}

		// vérification sur le pays d'immat
		if (!countryService.codeExist(postBody.getImmatriculationPays())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le code du pays d'immatriculation n'existe pas.");
		}


		return new ResponseEntity<>(true, HttpStatus.OK);


	}

	/**
	 * Modification d'un vehicule
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Modification d'un vehicule")
	@ApiResponses(value = {

			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver le vehicule"),
			@ApiResponse(code = 200, message = "Retourne le vehicule modifié", response = Vehicule.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/vehicule", 
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity edit(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody EditVehiculeParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de modifier un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer un vehicule
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_VEHICULE) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// chargement
		Vehicule vehicule = vehiculeService.getByUUID(postBody.getId(), jwtProvider.getCodePays(token));
		if (vehicule == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le vehicule.");
		}

		if (SecurityUtils.proprietaire(jwtProvider, token)) {
			UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
			if (!proprietaire.equals(vehicule.getProprietaire())) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit de modifier ce véhicule.");
			}

		}

		vehiculeService.update(postBody, vehicule, jwtProvider.getCodePays(token));


		actionAuditService.editerVehicule(vehicule, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(vehicule), HttpStatus.OK);

	}


	/**
	 * Modification de la disponibilité d'un vehicule
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Modification de la disponibilité d'un vehicule")
	@ApiResponses(value = {

			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver le vehicule"),
			@ApiResponse(code = 200, message = "Retourne le vehicule modifié", response = Vehicule.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/vehicule/disponibilite",
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity changer_disponibilite(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody DisponibiliteVehiculeParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de modifier un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer un vehicule
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_VEHICULE) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// chargement
		Vehicule vehicule = vehiculeService.getByUUID(postBody.getId(), jwtProvider.getCodePays(token));
		if (vehicule == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le vehicule.");
		}

		if (SecurityUtils.proprietaire(jwtProvider, token)) {
			UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
			if (!proprietaire.equals(vehicule.getProprietaire())) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit de modifier ce véhicule.");
			}

		}

		vehiculeService.update_disponibilite(postBody, vehicule, jwtProvider.getCodePays(token));


		actionAuditService.changerDisponibiliteVehicule(vehicule, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(vehicule), HttpStatus.OK);

	}

	/**
	 * Récupère les informations d'un vehicule
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère les informations d'un vehicule")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver le vehicule"),
			@ApiResponse(code = 200, message = "Retourne le vehicule demandé", response = Vehicule.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/vehicule", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity get(
			@ApiParam(value = "UUID du vehicule", required = true) @RequestParam("uuid") String uuid,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer un vehicule
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_VEHICULE) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);



		// chargement
		Vehicule vehicule = vehiculeService.getByUUID(uuid, jwtProvider.getCodePays(token));
		if (vehicule != null) {

			if (SecurityUtils.proprietaire(jwtProvider, token)) {
				UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
				List<Vehicule> vehicules = vehiculeService.getByProprietaire(proprietaire, code_pays);
				if (!vehicules.contains(vehicule)) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'accéder à ce véhicule.");
				}
			}

			actionAuditService.getVehicule(vehicule, token);

			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(vehicule), HttpStatus.OK);
		}


		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le vehicule.");
	}


	/**
	 * Récupère la position d'un vehicule avec driver
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère la position d'un vehicule avec driver")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Impossible de trouver la localisation du véhicule"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'expéditeur"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'opération"),
			@ApiResponse(code = 401, message = "Vous n'avez pas le droit d'accéder à cette opération."),
			@ApiResponse(code = 200, message = "Retourne la position du véhicule", response = Geoloc.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/geoloc",
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity getGeoloc(
			@ApiParam(value = "UUID de l'opération", required = true) @RequestParam("uuid") String uuid_operation,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer un vehicule
		if (!SecurityUtils.client(jwtProvider, token) && !SecurityUtils.admin(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		UtilisateurClient utilisateur_client = clientExpediteurService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), jwtProvider.getCodePays(token));
		if (SecurityUtils.client(jwtProvider, token) && utilisateur_client == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'expéditeur.");
		}


		// chargement
		Operation operation = operationService.getByUUID(uuid_operation, jwtProvider.getCodePays(token));
		if (operation != null) {

			Client client = clientService.getByUtilisateur(utilisateur_client, jwtProvider.getCodePays(token));
			if (!operation.getClient().equals(client) && !SecurityUtils.admin(jwtProvider, token)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'accéder à cette opération.");
			}
			if (operation.getStatut().equals(OperationStatut.ENREGISTRE.toString()) || operation.getStatut().equals(OperationStatut.VALIDE.toString())  || operation.getStatut().equals(OperationStatut.EN_COURS_DE_TRAITEMENT.toString())/*|| operation.getStatut().equals(OperationStatut.DECHARGEMENT_TERMINE.toString()) || operation.getAnnulationDate() != null */) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible de trouver la localisation du véhicule");
			}
			Geoloc geoloc = geolocRepository.findByImmatriculationAndDriver(operation.getVehicule().getImmatriculation(), operation.getTransporteur().getUuid().toString(), jwtProvider.getCodePays(token));
			if (geoloc != null) {
				actionAuditService.getGeolocVehicule(operation.getVehicule(), token);
				return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(geoloc), HttpStatus.OK);
			} else {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver la localisation du véhicule.");
			}


		}


		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'opération.");
	}



	/**
	 * Récupère la position de tous les véhicules des opérations en cours pour un client
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère la position de tous les véhicules des opérations en cours pour un client")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver l'expéditeur"),
			@ApiResponse(code = 404, message = "Impossible de trouver les opérations"),
			@ApiResponse(code = 200, message = "Retourne les positions des véhicules", response = ListGeoloc.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/geolocs/operations",
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity getGeolocsOperations(
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer un vehicule
		if (!SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_OPERATIONS) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_TOUTES_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		/*UtilisateurClient utilisateur_client = clientExpediteurService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), jwtProvider.getCodePays(token));
		if (utilisateur_client == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'expéditeur.");
		}*/


		// chargement
		Client client = clientService.getByUUID(jwtProvider.getClaimsValue("uuid_client", token), code_pays);

		ParentSpecificationsBuilder builder = new OperationSpecificationsBuilder();

		if (jwtProvider.getTypeDeCompte(token).equals(UtilisateurTypeDeCompte.EXPEDITEUR.toString()) || SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_TOUTES_OPERATIONS)) {
			builder.with("client", PredicateUtils.OPERATEUR_EGAL, client, false);
		} else if (jwtProvider.getTypeDeCompte(token).equals(UtilisateurTypeDeCompte.EXPEDITEUR_PERSONNEL.toString())) {
			String uuid_client_personnel = jwtProvider.getUUIDFromJWT(token).toString();
			UtilisateurClientPersonnel client_personnel = utilisateurClientPersonnelService.getByUUID(uuid_client_personnel, code_pays);
			builder.with("client_personnel", PredicateUtils.OPERATEUR_EGAL, client_personnel, false);
		}

		Specification spec = builder.build();

		// filtrage par pays kamtar
		Specification spec_pays = new Specification<ActionAudit>() {
			public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get("codePays"), code_pays));
				return builder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
		spec = spec.and(spec_pays);

		List<Operation> operations = (List<Operation>) operationService.getOperationsClient("createdOn", "asc", 0, 999999, spec, client).getContent();
		if (operations != null) {

			// liste des véhicules
			Set<String> vehicules = new HashSet<String>();
			for (Operation operation : operations) {
				List<String> statuts = new ArrayList<String>();
				statuts.add(OperationStatut.ENREGISTRE.toString());
				statuts.add(OperationStatut.EN_COURS_DE_TRAITEMENT.toString());
				statuts.add(OperationStatut.VALIDE.toString());
				statuts.add(OperationStatut.DECHARGEMENT_TERMINE.toString());
				if (!statuts.contains(operation.getStatut()) && operation.getVehicule() != null) {
					vehicules.add(operation.getVehicule().getImmatriculation());
				}
			}
			List<String> vehicules2 = new ArrayList<String>();
			vehicules2.addAll(vehicules);
			List<Geoloc> geolocs = new ArrayList<Geoloc>();
			if (!vehicules2.isEmpty()) {
				geolocs = geolocRepository.findByImmatriculations(vehicules2, jwtProvider.getCodePays(token));
			}
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(geolocs), HttpStatus.OK);

		}


		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver les opérations.");
	}


	/**
	 * Récupère la position de tous les véhicules
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère la position de tous les véhicules")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Impossible de trouver la localisation des véhicules."),
			@ApiResponse(code = 404, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 200, message = "Retourne la position de tous les véhicules", response = ListGeoloc.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/geolocs",
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity getGeolocs(
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer un vehicule
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_VEHICULE) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_VEHICULE)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		Calendar dateMin = new GregorianCalendar();
		dateMin.set(Calendar.HOUR_OF_DAY, 0);
		dateMin.set(Calendar.MINUTE, 0);
		dateMin.set(Calendar.SECOND, 0);

		Calendar dateMax = new GregorianCalendar();
		dateMax.set(Calendar.HOUR_OF_DAY, 23);
		dateMax.set(Calendar.MINUTE, 59);
		dateMax.set(Calendar.SECOND, 59);

		List<Geoloc> geolocs = (List<Geoloc>) geolocRepository.findAllGeoloc(jwtProvider.getCodePays(token), dateMin.getTime(), dateMax.getTime());
		if (geolocs != null) {

			// evol 43+19+44
			//if ("PROD".equals(kamtar_env)) {
				Map<String, Geoloc> immatriculation_geoloc = new HashMap<String, Geoloc>();
				for (Geoloc geoloc : geolocs) {
					if (!immatriculation_geoloc.containsKey(geoloc.getImmatriculation())) {
						immatriculation_geoloc.put(geoloc.getImmatriculation(), geoloc);
					}
				}
				List<Geoloc> geolocs_vehicules = new ArrayList<Geoloc>();
				Iterator<Map.Entry<String, Geoloc>> iter = immatriculation_geoloc.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, Geoloc> next = iter.next();
					geolocs_vehicules.add(next.getValue());
				}

				actionAuditService.getGeolocsVehicule(token);
				return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(geolocs_vehicules), HttpStatus.OK);
			//} else {
			//	return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(geolocs), HttpStatus.OK);
			//}

		} else {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver la localisation des véhicules.");
		}


	}



	/**
	 * Récupère la dernière position des véhicules d'un propriétaire
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère la dernière position des véhicules d'un propriétaire")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Impossible de trouver la localisation des véhicules."),
			@ApiResponse(code = 404, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 200, message = "Retourne la position de tous les véhicules", response = ListGeoloc.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/geolocs/proprietaire",
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity getGeolocsProprietaires(
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer un vehicule
		if (!SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);
		UtilisateurProprietaire user = utilisateurProprietaireService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);

		List<Vehicule> vehicules = vehiculeService.getByProprietaire(user, code_pays);
		List<String> immatriculations = new ArrayList<String>();
		for (Vehicule vehicule : vehicules) {
			immatriculations.add(vehicule.getImmatriculation());
		}
		List<Geoloc> geolocs = (List<Geoloc>) geolocRepository.findByImmatriculations(immatriculations, code_pays);
		if (geolocs != null) {


			Map<String, Geoloc> immatriculation_geoloc = new HashMap<String, Geoloc>();
			for (Geoloc geoloc : geolocs) {
				if (!immatriculation_geoloc.containsKey(geoloc.getImmatriculation())) {
					immatriculation_geoloc.put(geoloc.getImmatriculation(), geoloc);
				}
			}
			List<Geoloc> geolocs_vehicules = new ArrayList<Geoloc>();
			Iterator<Map.Entry<String, Geoloc>> iter = immatriculation_geoloc.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, Geoloc> next = iter.next();
				geolocs_vehicules.add(next.getValue());
			}

			actionAuditService.getGeolocsVehicule(token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(geolocs_vehicules), HttpStatus.OK);


		} else {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver la localisation des véhicules.");
		}


	}





	/**
	 * Récupère les informations des photos du vehicule
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère les informations des photos du vehicule")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver le vehicule"),
			@ApiResponse(code = 200, message = "Retourne le vehicule demandé", response = ListVehiculePhoto.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/vehicule/photos", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity getPhotos(
			@ApiParam(value = "UUID du vehicule", required = true) @RequestParam("uuid") String uuid,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer un vehicule
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_VEHICULE) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// chargement
		Vehicule vehicule = vehiculeService.getByUUID(uuid, jwtProvider.getCodePays(token));
		if (vehicule != null) {

			if (SecurityUtils.proprietaire(jwtProvider, token)) {
				UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
				List<Vehicule> vehicules = vehiculeService.getByProprietaire(proprietaire, code_pays);
				if (!vehicules.contains(vehicule)) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'accéder à ce véhicule.");
				}
			}

			List<VehiculePhoto> photos = vehiculePhotoService.getPhotosVehicule(vehicule, jwtProvider.getCodePays(token));
			actionAuditService.getPhotosVehicule(vehicule, token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(photos), HttpStatus.OK);
		}

		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le vehicule.");
	}




	/**
	 * Liste de tous les vehicules
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste de tous les vehicules")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin ou opérateur ayant le droit)"),
			@ApiResponse(code = 200, message = "Liste de tous les vehicule", response = ListVehicule.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/vehicules/liste",
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_offres2(
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de consulter les opérateurs katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher un vehicule
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_VEHICULE) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_VEHICULE) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_TRANSPORTEURS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}


		String order_column_bdd = "createdOn";
		String sort_bdd = "asc";

		ParentSpecificationsBuilder builder = new VehiculeSpecificationsBuilder();
		Specification spec_general = Specification.where(DatatableUtils.buildFiltres(null, null, builder, null));


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
		Page<Vehicule> leads = vehiculeService.getAllPagined(order_column_bdd, sort_bdd, 0, 999999, spec_general);

		// audit
		actionAuditService.getVehicules(token);


		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(leads.getContent()), HttpStatus.OK);
	}




	/**
	 * Liste de tous les vehicules
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste de tous les vehicules")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin ou opérateur ayant le droit)"),
			@ApiResponse(code = 200, message = "Liste de tous les vehicule (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/vehicules", 
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_offres(

			@ApiParam(value = "Critères de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de consulter les opérateurs katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher un vehicule
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_VEHICULE) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_VEHICULE) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_TRANSPORTEURS) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// paramètres du datatable
		String draw 		= postBody.getFirst("draw").toString();
		Integer length 		= Integer.valueOf(postBody.getFirst("length").toString());

		// tri, sens et numéro de page
		String colonneTriDefaut = "createdOn";
		List<String> colonnesTriAutorise = Arrays.asList("createdOn", "nom", "chargeUtileTonne", "volumeVehiculeM3", "immatriculation");

		String order_column_bdd = DatatableUtils.getOrderColonne(colonneTriDefaut, colonnesTriAutorise, postBody);
		String sort_bdd = DatatableUtils.getSort(postBody);
		Integer numero_page = DatatableUtils.getNumeroPage(postBody, length);

		// filtrage
		List<String> colonnesFiltrageActive = Arrays.asList("immatriculation", "carrosserie", "marque", "modeleSerie", "localisationHabituelleVehicule", "disponible");
		ParentSpecificationsBuilder builder = new VehiculeSpecificationsBuilder();
		Specification spec_general = Specification.where(DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, null));

		// filtrage par date de création
		spec_general = DatatableUtils.fitrageDate(spec_general, postBody, "createdOn", "createdOn");

		// filtrage sur charge utiles et volume
		spec_general = DatatableUtils.fitrageEntier(spec_general, postBody, "chargeUtileTonne", "chargeUtileTonne");
		spec_general = DatatableUtils.fitrageEntier(spec_general, postBody, "volumeVehiculeM3", "volumeVehiculeM3");

		// filtrage par propriétaire
		String uuid_proprietaire = null;
		if (postBody.containsKey("proprietaire")) {
			uuid_proprietaire = postBody.getFirst("proprietaire").toString();
			UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(uuid_proprietaire, jwtProvider.getCodePays(token));
			if (proprietaire != null) {
				builder.with("proprietaire", PredicateUtils.OPERATEUR_EGAL, proprietaire, false);
				spec_general = spec_general.and(builder.build());
			}
		}

		// filtrage imposé sur le propriétaire si c'est un propriétaire qui demande
		if (SecurityUtils.proprietaire(jwtProvider, token)) {
			UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
			builder.with("proprietaire", PredicateUtils.OPERATEUR_EGAL, proprietaire, false);
			spec_general = spec_general.and(builder.build());
		}


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
						predicates.add(builder.equal(root.get("proprietaire"), proprietaire));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec_general = spec_general.and(spec2);
			}
		}


		// ajouter au résultat les demandes d'appel d'offre de l'opération
		String uuid_operation = null;
		Operation operation = null;
		boolean load_appel_offre_operation = false;
		if (postBody.containsKey("operation")) {
			uuid_operation = postBody.getFirst("operation").toString();
			operation = operationService.getByUUID(uuid_operation, jwtProvider.getCodePays(token));
			if (operation != null) {
				load_appel_offre_operation = true;
			}

		}

		// filtrage par chauffeur principale
		String uuid_chauffeur = null;
		if (postBody.containsKey("chauffeur")) {
			uuid_chauffeur = postBody.getFirst("chauffeur").toString();
			UtilisateurDriver chauffeur = utilisateurDriverService.getByUUID(uuid_chauffeur, jwtProvider.getCodePays(token));
			if (chauffeur != null) {
				builder.with("driverPrincipal", PredicateUtils.OPERATEUR_EGAL, chauffeur, false);
				spec_general = spec_general.and(builder.build());
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

		// si on demande à croiser la liste des véhicules ave cles envois d'apel d'offre, on charge les appels d'offres de ces véhicules
		if (load_appel_offre_operation) {

			// filtrage sur statut de l'appel d'offre
			String filter_activee = postBody.getFirst("columns[9][search][value]").toString();
			boolean filtre_accepte = false;
			boolean filtre_refuse = false;
			if ("acceptes".equals(filter_activee)) {
				filtre_accepte = true;
			} else if ("refuses".equals(filter_activee)) {
				filtre_refuse = true;
			}

			if (filtre_accepte || filtre_refuse) {

				// filtrage sur statut de l'appel d'offre
				String filter_deja_envoye = postBody.getFirst("columns[11][search][value]").toString();

				List<OperationAppelOffre> liste_operation_appel_offres = operationAppelOffreService.findByOperation(operation, filtre_accepte, filtre_refuse, jwtProvider.getCodePays(token));
				logger.info("liste_operation_appel_offres = " + liste_operation_appel_offres.size());
				if (!liste_operation_appel_offres.isEmpty()) {
					List<UUID> vehicules_appel_offre = new ArrayList<UUID>();
					for (OperationAppelOffre ao : liste_operation_appel_offres) {
						vehicules_appel_offre.add(ao.getVehicule().getUuid());
					}

					// filtrage par pays kamtar
					Specification spec_pays2 = new Specification<ActionAudit>() {
						public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
							List<Predicate> predicates = new ArrayList<Predicate>();
							predicates.add(root.get("uuid").in(vehicules_appel_offre));
							return builder.and(predicates.toArray(new Predicate[predicates.size()]));
						}
					};
					spec_general = spec_general.and(spec_pays2);
				} else {
					Specification spec_pays2 = new Specification<ActionAudit>() {
						public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
							List<Predicate> predicates = new ArrayList<Predicate>();
							predicates.add(root.get("uuid").isNull()); // condition pour casser la requete
							return builder.and(predicates.toArray(new Predicate[predicates.size()]));
						}
					};
					spec_general = spec_general.and(spec_pays2);
				}
			}


			// filtrage sur statut de l'appel d'offre
			String filter_deja_envoye = postBody.getFirst("columns[11][search][value]").toString();

			if ("deja_envoye".equals(filter_deja_envoye)) {
				List<OperationAppelOffre> liste_operation_appel_offres = operationAppelOffreService.findByOperation(operation, jwtProvider.getCodePays(token));
				if (!liste_operation_appel_offres.isEmpty()) {
					List<UUID> vehicules_appel_offre = new ArrayList<UUID>();
					for (OperationAppelOffre ao : liste_operation_appel_offres) {
						vehicules_appel_offre.add(ao.getVehicule().getUuid());
					}

					// filtrage par pays kamtar
					Specification spec_pays2 = new Specification<ActionAudit>() {
						public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
							List<Predicate> predicates = new ArrayList<Predicate>();
							predicates.add(root.get("uuid").in(vehicules_appel_offre));
							return builder.and(predicates.toArray(new Predicate[predicates.size()]));
						}
					};
					spec_general = spec_general.and(spec_pays2);
				}
			} else if ("pas_encore_envoye".equals(filter_deja_envoye)) {
				List<OperationAppelOffre> liste_operation_appel_offres = operationAppelOffreService.findByOperation(operation, jwtProvider.getCodePays(token));
				if (!liste_operation_appel_offres.isEmpty()) {
					List<UUID> vehicules_appel_offre = new ArrayList<UUID>();
					for (OperationAppelOffre ao : liste_operation_appel_offres) {
						vehicules_appel_offre.add(ao.getVehicule().getUuid());
					}

					// filtrage par pays kamtar
					Specification spec_pays2 = new Specification<ActionAudit>() {
						public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
							return builder.not(root.get("uuid").in(vehicules_appel_offre));
							//return builder.and(predicates.toArray(new Predicate[predicates.size()]));
						}
					};
					spec_general = spec_general.and(spec_pays2);
				}
			}

		}




		// préparation les deux requêtes (résultat et comptage)
		Page<Vehicule> leads = vehiculeService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec_general);
		Long total = vehiculeService.countAll(spec_general);

		List<Vehicule> liste_vehicules = leads.getContent();
		actionAuditService.getVehicules(token);
		List<Vehicule> vehicules3 = new ArrayList<Vehicule>();


		// si on demande à croiser la liste des véhicules ave cles envois d'apel d'offre, on charge les appels d'offres de ces véhicules
		if (load_appel_offre_operation) {


			// filtrage sur statut de l'appel d'offre
			String filter_activee = postBody.getFirst("columns[9][search][value]").toString();
			boolean filtre_accepte = false;
			boolean filtre_refuse = false;
			if ("acceptes".equals(filter_activee)) {
				filtre_accepte = true;
			} else if ("refuses".equals(filter_activee)) {
				filtre_refuse = true;
			}

			List<OperationAppelOffre> liste_operation_appel_offres = operationAppelOffreService.findByVehiculeAndOperation(operation, liste_vehicules, filtre_accepte, filtre_refuse, jwtProvider.getCodePays(token)) ;



			if (!liste_operation_appel_offres.isEmpty()) {

				if (filtre_accepte || filtre_refuse) {
					List<Vehicule> liste_vehicules2 = new LinkedList<Vehicule>();
					for (OperationAppelOffre a : liste_operation_appel_offres) {
						if (a.getVehicule() != null) {
							liste_vehicules2.add(a.getVehicule());
						}
					}
					liste_vehicules = new ArrayList<Vehicule>();
					liste_vehicules.addAll(liste_vehicules2);
				}


				for (int i = 0; i < liste_vehicules.size(); i++) {

					boolean trouve = false;
					int cpt = 0;
					while (!trouve && cpt < liste_operation_appel_offres.size()) {

						if (liste_operation_appel_offres.get(cpt) != null && liste_operation_appel_offres.get(cpt).getVehicule() != null && liste_operation_appel_offres.get(cpt).getVehicule().equals(liste_vehicules.get(i))) {
							OperationAppelOffre oao = liste_operation_appel_offres.get(cpt);
							// pour éviter la recursion
							oao.setOperation(null);
							oao.setVehicule(null);
							liste_vehicules.get(i).setAppelOffre(liste_operation_appel_offres.get(cpt));
							trouve = true;
						}
						cpt++;
					}

				}
				vehicules3.addAll(liste_vehicules);


			} else {
				vehicules3.addAll(liste_vehicules);
			}

		} else {
			vehicules3.addAll(liste_vehicules);
		}


		// prépare les résultast
		JSONArray jsonArrayOffres = new JSONArray();
		if (leads != null) {
			jsonArrayOffres.addAll(vehicules3);
		}
		Map<String, Object> jsonDataResults = new LinkedHashMap<String, Object>();
		jsonDataResults.put("draw", draw);	
		jsonDataResults.put("recordsTotal", total);
		jsonDataResults.put("recordsFiltered", total);
		jsonDataResults.put("data", jsonArrayOffres);		

		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(jsonDataResults), HttpStatus.OK);
	}



	/**
	 * Retourne la liste des points geolocalisés d'un véhicule sur une période passée en paramètre
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Retourne la liste des points geolocalisés d'un véhicule sur une période passée en paramètre")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (reservés aux admins et opératers ayant droit)"),
			@ApiResponse(code = 200, message = "Liste des points")
	})
	@RequestMapping(
			produces = "application/json",
			value = "/vehicule/geolocs",
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity<Object> geolocs_vehicule(
			@ApiParam(value = "Date de l'historique", required = true) @RequestParam("date_historique") String date_historique,
			@ApiParam(value = "Marge de l'historique", required = true) @RequestParam("marge_historique") Integer marge_historique,
			@ApiParam(value = "UUID du véhicule", required = true) @RequestParam("vehicule") String uuid_vehicule,
			@ApiParam(value = "UUID du proprietaire", required = true) @RequestParam("proprietaire") String uuid_proprietaire,
			@ApiParam(value = "UUID du client", required = true) @RequestParam("client") String uuid_client,
			@ApiParam(value = "UUID du driver", required = true) @RequestParam("driver") String uuid_driver,
			@ApiParam(value = "Code de la carrosseries", required = true) @RequestParam("carrosserie") String carrosserie,
			@ApiParam(value = "Code de l'opération", required = true) @RequestParam("operation") String code_operation,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws Exception {

		// vérifie que le jeton est valide et que les droits lui permettent de consulter les admin katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_VEHICULE) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_VEHICULE)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);



		Date dateHistorique = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(date_historique);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateHistorique);
		calendar.add(Calendar.HOUR_OF_DAY, marge_historique);
		Date dateFin = calendar.getTime();
		calendar.setTime(dateHistorique);
		calendar.add(Calendar.HOUR_OF_DAY, marge_historique * -1);
		Date dateDebut = calendar.getTime();


		String sort_bdd = "asc";
		Integer numero_page = 0;
		Integer length = 999999;

		// filtrage par opération
		if (code_operation != null && !"".equals(code_operation)) {
			try {
				Long code_long = Long.valueOf(code_operation);
				Operation operation = operationService.getByCode(code_long, code_pays);
				if (operation != null) {
					if (dateDebut.before(operation.getDepartDateOperation())) {
						dateDebut = operation.getDepartDateOperation();
					}
					if (dateFin.after(operation.getDerniereDateConnue())) {
						dateFin = operation.getDerniereDateConnue();
					}

				}
			} catch (NumberFormatException e) {
				// erreur silencieuse
			}

		}


		// filtrage par date
		Date finalDateDebut = dateDebut;
		Date finalDateFin = dateFin;
		Specification spec = new Specification<Geoloc>() {
			public Predicate toPredicate(Root<Geoloc> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.greaterThanOrEqualTo(root.get("createdOn"), finalDateDebut));
				predicates.add(builder.lessThanOrEqualTo(root.get("createdOn"), finalDateFin));
				return builder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};

		// filtrage par client
		if (uuid_client != null && !"".equals(uuid_client)) {
			try {
				Client client = clientService.getByUUID(uuid_client, code_pays);
				if (client != null) {
					List<Operation> operations = operationService.getOperationsClient(client, code_pays);
					Specification spec_client = null;
					for (Operation operation : operations) {
						if (operation.getVehicule() != null) {
							spec_client = new Specification<Geoloc>() {
								public Predicate toPredicate(Root<Geoloc> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
									List<Predicate> predicates = new ArrayList<Predicate>();
									predicates.add(builder.greaterThanOrEqualTo(root.get("createdOn"), operation.getDateHeureChargementCommence()));
									predicates.add(builder.lessThanOrEqualTo(root.get("createdOn"), operation.getDerniereDateConnue()));
									predicates.add(builder.equal(root.get("immatriculation"), operation.getVehicule().getImmatriculation()));
									return builder.or(predicates.toArray(new Predicate[predicates.size()]));
								}
							};
						}


					}
					if (spec_client != null) {
						spec = spec.and(spec_client);
					}
				}

			} catch (NumberFormatException e) {
				// erreur silencieuse
			}

		}




		// filtrage par véhicule
		if (uuid_vehicule != null && !"".equals(uuid_vehicule)) {
			Vehicule vehicule = vehiculeService.getByUUID(uuid_vehicule, code_pays);
			if (vehicule != null) {
				Specification spec_vehicule = new Specification<Geoloc>() {
					public Predicate toPredicate(Root<Geoloc> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						predicates.add(builder.equal(root.get("immatriculation"), vehicule.getImmatriculation()));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec = spec.and(spec_vehicule);
			}
		}

		// filtrage par proprietaire
		if (uuid_proprietaire != null && !"".equals(uuid_proprietaire)) {
			UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(uuid_proprietaire, code_pays);
			if (proprietaire != null) {
				List<Vehicule> vehicules = vehiculeService.getByProprietaire(proprietaire, code_pays);
				List<String> immatriculations_vehicules = new ArrayList<String>();
				for (Vehicule vehicule: vehicules) {
					immatriculations_vehicules.add(vehicule.getImmatriculation());
				}
				if (!immatriculations_vehicules.isEmpty()) {
					Specification spec_proprietaire = new Specification<Geoloc>() {
						public Predicate toPredicate(Root<Geoloc> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
							List<Predicate> predicates = new ArrayList<Predicate>();
							predicates.add(root.get("immatriculation").in(immatriculations_vehicules));
							return builder.and(predicates.toArray(new Predicate[predicates.size()]));
						}
					};
					spec = spec.and(spec_proprietaire);
				}
			}
		}

		// filtrage par carrosseries
		logger.info("carrosserie = " + carrosserie);
		if (carrosserie != null && !"".equals(carrosserie)) {
			List<Vehicule> vehcules = vehiculeService.getByCarrosseries(carrosserie, code_pays);
			logger.info("filtrage par carrosserie");
			List<String> immatriculations_vehicules = new ArrayList<String>();
			for (Vehicule vehicule: vehcules) {
				immatriculations_vehicules.add(vehicule.getImmatriculation());
			}
			if (!immatriculations_vehicules.isEmpty()) {
				Specification spec_proprietaire = new Specification<Geoloc>() {
					public Predicate toPredicate(Root<Geoloc> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						predicates.add(root.get("immatriculation").in(immatriculations_vehicules));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec = spec.and(spec_proprietaire);
			}

		}

		// filtrage par driver
		logger.info("uuid_driver = " + uuid_driver);
		if (uuid_driver != null && !"".equals(uuid_driver)) {
			UtilisateurDriver driver = utilisateurDriverService.getByUUID(uuid_driver, code_pays);
			if (driver != null) {
				logger.info("filtrage par driver");
				Specification spec_driver = new Specification<Geoloc>() {
					public Predicate toPredicate(Root<Geoloc> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						predicates.add(builder.equal(root.get("driver"), driver.getUuid().toString()));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec = spec.and(spec_driver);
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
		spec = spec.and(spec_pays);

		// préparation les deux requêtes (résultat et comptage)
		List<Geoloc> geolocs = geolocService.getAllPagined("createdOn", sort_bdd, numero_page, length, spec).getContent();
		Map<String, List<Geoloc>> geolocs_vehicules = new HashMap<String, List<Geoloc>>();
		for (Geoloc geoloc : geolocs) {
			if (!geolocs_vehicules.containsKey(geoloc.getImmatriculation())) {
				geolocs_vehicules.put(geoloc.getImmatriculation(), new ArrayList<Geoloc>());
			}
			List<Geoloc> geolocs2 = geolocs_vehicules.get(geoloc.getImmatriculation());
			geolocs2.add(geoloc);
			geolocs_vehicules.put(geoloc.getImmatriculation(), geolocs2);
		}

		// audit
		actionAuditService.getGeolocsVehicule(token);

		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(geolocs_vehicules), HttpStatus.OK);
	}




	/**
	 * Retourne la liste des points geolocalisés
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Retourne la liste des points geolocalisés")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (reservés aux admins et opératers ayant droit)"),
			@ApiResponse(code = 200, message = "Liste des points")
	})
	@RequestMapping(
			produces = "application/json",
			value = "/vehicule/geoloc/realtime",
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity<Object> geolocs_vehicule_realtime(
			@ApiParam(value = "Type de la période (valeurs possible : today)", required = true) @RequestParam("periode") String periode,
			@ApiParam(value = "UUID du véhicule", required = true) @RequestParam("vehicule") String uuid_vehicule,
			@ApiParam(value = "UUID du proprietaire", required = true) @RequestParam("proprietaire") String uuid_proprietaire,
			@ApiParam(value = "UUID du client", required = true) @RequestParam("client") String uuid_client,
			@ApiParam(value = "UUID du driver", required = true) @RequestParam("driver") String uuid_driver,
			@ApiParam(value = "Code de la carrosseries", required = true) @RequestParam("carrosserie") String carrosserie,
			@ApiParam(value = "Code de l'opération", required = true) @RequestParam("operation") String code_operation,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws Exception {

		// vérifie que le jeton est valide et que les droits lui permettent de consulter les admin katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_VEHICULE) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_VEHICULE)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		Date dateFin = calendar.getTime();
		calendar.add(Calendar.HOUR_OF_DAY, 30 * 24 * -1);
		Date dateDebut = calendar.getTime();

		String sort_bdd = "desc";
		Integer numero_page = 0;
		Integer length = 999999;

		Operation operation = null;

		// filtrage par date
		if (code_operation != null && !"".equals(code_operation)) {
			try {
				Long code_long = Long.valueOf(code_operation);
				operation = operationService.getByCode(code_long, code_pays);
				if (operation != null) {
					if (dateDebut.before(operation.getDepartDateOperation())) {
						dateDebut = operation.getDepartDateOperation();
					}
					if (dateFin.after(operation.getDerniereDateConnue())) {
						dateFin = operation.getDerniereDateConnue();
					}				}
			} catch (NumberFormatException e) {
				// erreur silencieuse
			}

		}

		if (periode != null && "today".equals(periode)) {

			Calendar calendar1 = Calendar.getInstance();
			calendar1.setTime(new Date());
			calendar1.set(Calendar.HOUR_OF_DAY, 0);
			calendar1.set(Calendar.MINUTE, 0);
			calendar1.set(Calendar.SECOND, 0);

			Calendar calendar2 = Calendar.getInstance();
			calendar2.setTime(calendar1.getTime());
			calendar2.add(Calendar.DAY_OF_MONTH, 1);

			dateDebut = calendar1.getTime();
			dateFin = calendar2.getTime();

			logger.info("dateDebut = " + dateDebut);
			logger.info("dateFin = " + dateFin);

		}

		// filtrage par date
		Date finalDateDebut = dateDebut;
		Date finalDateFin = dateFin;
		Specification spec = new Specification<Geoloc>() {
			public Predicate toPredicate(Root<Geoloc> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.greaterThanOrEqualTo(root.get("createdOn"), finalDateDebut));
				predicates.add(builder.lessThanOrEqualTo(root.get("createdOn"), finalDateFin));
				return builder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};

		final Operation operation_final = operation;
		if (operation != null) {
			if (operation.getVehicule() != null) {
				Specification spec_operation = new Specification<Geoloc>() {
					public Predicate toPredicate(Root<Geoloc> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						predicates.add(builder.equal(root.get("immatriculation"), operation_final.getVehicule().getImmatriculation()));
						return builder.or(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec = spec.and(spec_operation);
			}
		}


		// filtrage par client
		if (uuid_client != null && !"".equals(uuid_client)) {
			try {
				Client client = clientService.getByUUID(uuid_client, code_pays);
				if (client != null) {
					List<Operation> operations = operationService.getOperationsClient(client, code_pays);
					Specification spec_client = null;
					for (Operation operation2 : operations) {
						if (operation2.getVehicule() != null) {
							spec_client = new Specification<Geoloc>() {
								public Predicate toPredicate(Root<Geoloc> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
									List<Predicate> predicates = new ArrayList<Predicate>();
									predicates.add(builder.greaterThanOrEqualTo(root.get("createdOn"), operation2.getDateHeureChargementCommence()));
									predicates.add(builder.lessThanOrEqualTo(root.get("createdOn"), operation2.getDerniereDateConnue()));
									predicates.add(builder.equal(root.get("immatriculation"), operation2.getVehicule().getImmatriculation()));
									return builder.or(predicates.toArray(new Predicate[predicates.size()]));
								}
							};
						}


					}
					if (spec_client != null) {
						spec = spec.and(spec_client);
					}
				}

			} catch (NumberFormatException e) {
				// erreur silencieuse
			}

		}




		// filtrage par véhicule
		if (uuid_vehicule != null && !"".equals(uuid_vehicule)) {
			Vehicule vehicule = vehiculeService.getByUUID(uuid_vehicule, code_pays);
			if (vehicule != null) {
				Specification spec_vehicule = new Specification<Geoloc>() {
					public Predicate toPredicate(Root<Geoloc> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						predicates.add(builder.equal(root.get("immatriculation"), vehicule.getImmatriculation()));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec = spec.and(spec_vehicule);
			}
		}

		// filtrage par proprietaire
		if (uuid_proprietaire != null && !"".equals(uuid_proprietaire)) {
			UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(uuid_proprietaire, code_pays);
			if (proprietaire != null) {
				List<Vehicule> vehicules = vehiculeService.getByProprietaire(proprietaire, code_pays);
				List<String> immatriculations_vehicules = new ArrayList<String>();
				for (Vehicule vehicule: vehicules) {
					immatriculations_vehicules.add(vehicule.getImmatriculation());
				}
				if (!immatriculations_vehicules.isEmpty()) {
					Specification spec_proprietaire = new Specification<Geoloc>() {
						public Predicate toPredicate(Root<Geoloc> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
							List<Predicate> predicates = new ArrayList<Predicate>();
							predicates.add(root.get("immatriculation").in(immatriculations_vehicules));
							return builder.and(predicates.toArray(new Predicate[predicates.size()]));
						}
					};
					spec = spec.and(spec_proprietaire);
				}
			}
		}

		// filtrage par carrosseries
		logger.info("carrosserie = " + carrosserie);
		if (carrosserie != null && !"".equals(carrosserie)) {
			List<Vehicule> vehcules = vehiculeService.getByCarrosseries(carrosserie, code_pays);
			logger.info("filtrage par carrosserie");
			List<String> immatriculations_vehicules = new ArrayList<String>();
			for (Vehicule vehicule: vehcules) {
				immatriculations_vehicules.add(vehicule.getImmatriculation());
			}
			if (!immatriculations_vehicules.isEmpty()) {
				Specification spec_proprietaire = new Specification<Geoloc>() {
					public Predicate toPredicate(Root<Geoloc> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						predicates.add(root.get("immatriculation").in(immatriculations_vehicules));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec = spec.and(spec_proprietaire);
			}

		}

		// filtrage par driver
		logger.info("uuid_driver = " + uuid_driver);
		if (uuid_driver != null && !"".equals(uuid_driver)) {
			UtilisateurDriver driver = utilisateurDriverService.getByUUID(uuid_driver, code_pays);
			if (driver != null) {
				logger.info("filtrage par driver");
				Specification spec_driver = new Specification<Geoloc>() {
					public Predicate toPredicate(Root<Geoloc> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						predicates.add(builder.equal(root.get("driver"), driver.getUuid().toString()));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec = spec.and(spec_driver);
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
		spec = spec.and(spec_pays);

		// préparation les deux requêtes (résultat et comptage)
		List<Geoloc> geolocs = geolocService.getAllPagined("createdOn", sort_bdd, numero_page, length, spec).getContent();
		Map<String, List<Geoloc>> geolocs_vehicules = new HashMap<String, List<Geoloc>>();
		for (Geoloc geoloc : geolocs) {
			if (!geolocs_vehicules.containsKey(geoloc.getImmatriculation())) {
				geolocs_vehicules.put(geoloc.getImmatriculation(), new ArrayList<Geoloc>());

				List<Geoloc> geolocs2 = new ArrayList<Geoloc>();
				geolocs2.add(geoloc);
				geolocs_vehicules.put(geoloc.getImmatriculation(), geolocs2);

			}
		}

		// audit
		actionAuditService.getGeolocsVehicule(token);

		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(geolocs_vehicules), HttpStatus.OK);
	}



	/**
	 * Export des véhicules
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Export des véhicules")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (reservés aux admins et opératers ayant droit)"),
			@ApiResponse(code = 200, message = "CSV des véhicules")
	})
	@RequestMapping(
			produces = "application/json",
			value = "/vehicules/export",
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
		Page<Vehicule> leads = vehiculeService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);

		// convertion liste en excel
		byte[] bytesArray = exportExcelService.export(leads, null);

		// audit
		actionAuditService.exportVehicules(token);

		return ResponseEntity.ok()
				//.headers(headers) // add headers if any
				.contentLength(bytesArray.length)
				.contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
				.body(bytesArray);
	}


	/**
	 * Supprime une des photos du véhicule
	 * @param postBody
	 * @return
	 */
	/*@RequestLogger(name = Application.microservice)*/
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Supprime une des photos du véhicule")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin ou opérateur ayant le droit)"),
			@ApiResponse(code = 200, message = "Photo supprimée")
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/vehicule/photo", 
			method = RequestMethod.DELETE)
	@CrossOrigin
	public ResponseEntity<Object>  deletePhoto(@Valid @RequestBody VehiculePhotoDeleteParams postBody,
		@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer un vehicule
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_VEHICULE) && !SecurityUtils.proprietaire(jwtProvider, token)) {
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
			folderTempUUID.delete();
		}

		// supprime en bdd si elle existe (seulement dans le cas où il s'agit d'une modifciation de compte)
		if (postBody.getVehicule() != null) {
			Vehicule vehicule = vehiculeService.getByUUID(postBody.getVehicule(), jwtProvider.getCodePays(token));
			if (vehicule != null) {

				if (SecurityUtils.proprietaire(jwtProvider, token)) {
					UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
					List<Vehicule> vehicules = vehiculeService.getByProprietaire(proprietaire, code_pays);
					if (!vehicules.contains(vehicule)) {
						throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'accéder à ce véhicule.");
					}
				}


				// extrait l'ordre à partir du nom du fichier
				String filename = FileUtils.removeExtension(postBody.getFilename());
				Optional<VehiculePhoto> photo = vehiculePhotoService.get(vehicule, filename, jwtProvider.getCodePays(token));
				if (photo.isPresent()) {
					vehiculePhotoService.delete(photo.get());
				}
			}

		}



		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(postBody), HttpStatus.OK);
	}



	/**
	 * Suppression d'un vehicule
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Modification d'un vehicule")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Vous ne pouvez pas supprimer ce véhicule car il est utilisé dans au moins une opération"),
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver le vehicule"),
			@ApiResponse(code = 200, message = "Vehicule supprimé", response = Boolean.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/vehicule", 
			method = RequestMethod.DELETE)
	@CrossOrigin(origins="*")
	public ResponseEntity delete(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody DeleteVehiculeParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de modifier un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer un véhicule
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_VEHICULE) || !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.SUPPRESSION_VEHICULE)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Veuillez contacter Kamtar pour cette action");
		}

		// chargement
		Vehicule vehicule = vehiculeService.getByUUID(postBody.getId(), jwtProvider.getCodePays(token));
		if (vehicule == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le vehicule.");
		}
		vehiculeService.delete(vehicule, jwtProvider.getCodePays(token));


		actionAuditService.supprimerVehicule(vehicule, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(vehicule), HttpStatus.OK);

	}


	/**
	 * Stocke les photos du véhicule
	 * @param postBody
	 * @return
	 */
	/*@RequestLogger(name = Application.microservice)*/
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Stocke les photos du véhicule")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver le vehicule"),
			@ApiResponse(code = 200, message = "Vehicule supprimé", response = Boolean.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "multipart/form-data",
			value = "/vehicule/photos", 
			method = RequestMethod.POST)  
	@CrossOrigin(origins="*")
	public ResponseEntity<Object> addPhotos(MultipartHttpServletRequest request,
		HttpServletResponse response,
		@RequestParam("folderUuid") String folder_uuid,
		@RequestParam(value = "vehicule", required=false) String vehiculeId,
		@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) {

		String code_pays = jwtProvider.getCodePays(token);

		// création du dossier si il n'existe pas encore pour stocker toutes les photos du carroussel pour l'offre
		String tempDir = System.getProperty("java.io.tmpdir");
		File folderTempUUID = new File(tempDir + "/" + folder_uuid);
		FileUtils.createFolderIfNotExistWithRetry(folderTempUUID, 500);

		// chargement de l'offre si renseignée (=modification de l'offre)
		Vehicule vehicule = null;
		if (vehiculeId != null && !"".equals(vehiculeId)) {
			vehicule = vehiculeService.getByUUID(vehiculeId, jwtProvider.getCodePays(token));

			if (SecurityUtils.proprietaire(jwtProvider, token)) {
				UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
				List<Vehicule> vehicules = vehiculeService.getByProprietaire(proprietaire, code_pays);
				if (!vehicules.contains(vehicule)) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'accéder à ce véhicule.");
				}
			}
		}



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
			if (vehicule != null) {
				Optional<VehiculePhoto> photo_carroussel = vehiculePhotoService.get(vehicule, ordre_fichier, jwtProvider.getCodePays(token));
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

		}

		folderTempUUID.delete();

		return new ResponseEntity<Object>(true, HttpStatus.OK);
	}

	/**
	 * Récupère le fichier image de la photo principale du véhicule
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère le fichier image de la photo principale du véhicule")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur)"),
			@ApiResponse(code = 200, message = "Fichier image", response = byte[].class)
	})
	@RequestMapping(
			produces = MediaType.IMAGE_PNG_VALUE,
			value = "/vehicule/photo/principale", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public byte[]getPhotoPrincipale(
			@ApiParam(value = "UUID du véhicule", required = true) @RequestParam("uuid") String uuid, 
			@ApiParam(value = "Jeton JWT pour autentification", required = true) @RequestParam("token") String token) {

		// vérifie que le jeton est valide
		if (!jwtProvider.isValidJWT(token)) {
			return null;
		}

		// vérifie que les droits lui permettent de afficher une commande
		if (!SecurityUtils.admin(jwtProvider, token) && !SecurityUtils.operateur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			return null;
		}

		String code_pays = jwtProvider.getCodePays(token);

		Vehicule vehicule = vehiculeService.getByUUID(uuid, jwtProvider.getCodePays(token));
		if (vehicule != null) {

			// chargement
			if (SecurityUtils.proprietaire(jwtProvider, token)) {
				UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
				List<Vehicule> vehicules = vehiculeService.getByProprietaire(proprietaire, code_pays);
				if (!vehicules.contains(vehicule)) {
					logger.error("Vous n'avez pas le droit de consulter à l'assurance de ce véhicule");
					return null;
				}
			}

			if (vehicule.getPhotoPrincipale() != null && !"".equals(vehicule.getPhotoPrincipale())) {

				byte[] image = vehiculePhotoService.get(vehicule.getPhotoPrincipale());
				return image;

			}
		}
		return null;

	}

	/**
	 * Récupère le fichier image de l'assurance du véhicule
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère le fichier image de l'assurance du véhicule")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur)"),
			@ApiResponse(code = 200, message = "Fichier image", response = byte[].class)
	})
	@RequestMapping(
			produces = MediaType.IMAGE_PNG_VALUE,
			value = "/vehicule/assurance", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public byte[]getSignaturedeExpediteur(
			@ApiParam(value = "UUID du véhicule", required = true) @RequestParam("uuid") String uuid, 
			@ApiParam(value = "Jeton JWT pour autentification", required = true) @RequestParam("token") String token) {

		// vérifie que le jeton est valide
		if (!jwtProvider.isValidJWT(token)) {
			return null;
		}

		// vérifie que les droits lui permettent de afficher une commande
		if (!SecurityUtils.admin(jwtProvider, token) && !SecurityUtils.operateur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			return null;
		}

		String code_pays = jwtProvider.getCodePays(token);

		Vehicule vehicule = vehiculeService.getByUUID(uuid, jwtProvider.getCodePays(token));
		if (vehicule != null) {

			// chargement
			if (SecurityUtils.proprietaire(jwtProvider, token)) {
				UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
				List<Vehicule> vehicules = vehiculeService.getByProprietaire(proprietaire, code_pays);
				if (!vehicules.contains(vehicule)) {
					logger.error("Vous n'avez pas le droit de consulter à l'assurance de ce véhicule");
					return null;
				}
			}

			if (vehicule.getDocumentAssurance() != null && !"".equals(vehicule.getDocumentAssurance())) {

				byte[] image = vehiculePhotoService.get(vehicule.getDocumentAssurance());
				return image;

			}
		}
		return null;

	}

	/**
	 * Récupère le fichier image de la carte grise du véhicule
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère le fichier image de la carte grise du véhicule")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur)"),
			@ApiResponse(code = 200, message = "Fichier image", response = byte[].class)
	})
	@RequestMapping(
			produces = MediaType.IMAGE_PNG_VALUE,
			value = "/vehicule/carte_grise", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public byte[]getCartegrise(
			@ApiParam(value = "UUID du véhicule", required = true) @RequestParam("uuid") String uuid, 
			@ApiParam(value = "Jeton JWT pour autentification", required = true) @RequestParam("token") String token) {

		// vérifie que le jeton est valide
		if (!jwtProvider.isValidJWT(token)) {
			return null;
		}

		// vérifie que les droits lui permettent de afficher une commande
		if (!SecurityUtils.admin(jwtProvider, token) && !SecurityUtils.operateur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			return null;
		}

		String code_pays = jwtProvider.getCodePays(token);

		Vehicule vehicule = vehiculeService.getByUUID(uuid, jwtProvider.getCodePays(token));
		if (vehicule != null) {

			// chargement
			if (SecurityUtils.proprietaire(jwtProvider, token)) {
				UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
				List<Vehicule> vehicules = vehiculeService.getByProprietaire(proprietaire, code_pays);
				if (!vehicules.contains(vehicule)) {
					logger.error("Vous n'avez pas le droit de consulter la photo de la carte grise");
					return null;
				}
			}

			if (vehicule.getDocumentCarteGrise() != null && !"".equals(vehicule.getDocumentCarteGrise())) {

				byte[] image = vehiculePhotoService.get(vehicule.getDocumentCarteGrise());
				return image;

			}
		}
		return null;

	}


	/**
	 * Récupère le fichier image d'une des photos du véhicule
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère le fichier image d'une des photos du véhicule")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur )"),
			@ApiResponse(code = 200, message = "Fichier image", response = byte[].class)
	})
	@RequestMapping(
			produces = MediaType.IMAGE_PNG_VALUE,
			value = "/vehicule/photo", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public byte[]getPhoto(
			@ApiParam(value = "UUID de la photo du véhicule", required = true) @RequestParam("uuid") String uuid, 
			@ApiParam(value = "Jeton JWT pour autentification", required = true) @RequestParam("token") String token) {

		// vérifie que le jeton est valide
		if (!jwtProvider.isValidJWT(token)) {
			return null;
		}

		// vérifie que les droits lui permettent de afficher une commande
		if (!SecurityUtils.admin(jwtProvider, token) && !SecurityUtils.operateur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			return null;
		}

		String code_pays = jwtProvider.getCodePays(token);


		byte[] photo = vehiculePhotoService.get(uuid);
		if (photo != null && photo.length > 0) {
			return photo;
		}

		return null;

	}

	/**
	 * Autocompletion des drivers
	 * @param postBody
	 * @return
	 */
	/*@RequestLogger(name = Application.microservice)*/
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Autocompletion des véhicules")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 200, message = "Liste des véhicules", response = ListVehicule.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/vehicules/autocompletion", 
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity autocompletion(
			@ApiParam(value = "Partie de l'immatriculation à rechercher", required = true) @RequestParam("query") String query, 
			@ApiParam(value = "Jeton JWT pour autentification", required = true) @RequestParam("token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de lire les expéditeurs
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_VEHICULE) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		List<Vehicule> vehicules = vehiculeService.autocomplete(query, jwtProvider.getCodePays(token));

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(vehicules), HttpStatus.OK);

	}

}
