package com.kamtar.transport.api.controller;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.criteria.OperationSpecificationsBuilder;
import com.kamtar.transport.api.enums.NotificationType;
import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.*;
import com.kamtar.transport.api.security.ListeDroitOperateursKamtar;
import com.kamtar.transport.api.service.*;
import com.kamtar.transport.api.swagger.ListOperation;
import com.kamtar.transport.api.utils.ExportUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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
import com.kamtar.transport.api.criteria.ParentSpecificationsBuilder;
import com.kamtar.transport.api.criteria.UtilisateurDriverSpecificationsBuilder;
import com.kamtar.transport.api.criteria.UtilisateurOperateurKamtarSpecificationsBuilder;
import com.kamtar.transport.api.enums.OperateurListeDeDroits;
import com.kamtar.transport.api.enums.UtilisateurTypeDeCompte;
import com.kamtar.transport.api.security.SecurityUtils;
import com.kamtar.transport.api.swagger.ListDriver;
import com.kamtar.transport.api.swagger.ListProprietaire;
import com.kamtar.transport.api.swagger.MapDatatable;
import com.kamtar.transport.api.utils.DatatableUtils;
import com.kamtar.transport.api.utils.JWTProvider;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@Api(value="Gestion des drivers des v??hicules", description="API Rest qui g??re les drivers des v??hicules")
@RestController
@EnableWebMvc
public class UtilisateurDriverController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(UtilisateurDriverController.class);  

	@Autowired
	CountryService countryService;

	@Autowired
	UtilisateurDriverService utilisateurDriverService;

	@Autowired
	MapperJSONService mapperJSONService;

	@Autowired
	ExportExcelService exportExcelService;

	@Autowired
	NotificationService notificationService;

	@Autowired
	UtilisateurProprietaireService utilisateurProprietaireService;

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	ActionAuditService actionAuditService;

	@Autowired
	VehiculeService vehiculeService;

	@Autowired
	DriverPhotoService driverPhotoService;

	/**
	 * Cr??ation d'un driver via le backoffice
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Cr??ation d'un driver")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "L'adresse e-mail est d??j?? utilis??e."),
			@ApiResponse(code = 400, message = "Le num??ro de t??l??phone est d??j?? utilis??."),
			@ApiResponse(code = 400, message = "Impossible d'enregistrer le driver."),
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv?? aux admins ou op??rateurs ayant droits)"),
		    @ApiResponse(code = 201, message = "Driver cr????", response = UtilisateurDriver.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/driver", 
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity create(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody CreateDriverParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de cr??er un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_TRANSPORTEURS) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");		
		}

		String code_pays = jwtProvider.getCodePays(token);

		UtilisateurProprietaire proprietaire = null;
		if (SecurityUtils.proprietaire(jwtProvider, token)) {
			proprietaire = utilisateurProprietaireService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
		}

		// enregistrement
		UtilisateurDriver user = utilisateurDriverService.createUser(postBody, jwtProvider.getCodePays(token), proprietaire);
		if (user != null) {
			actionAuditService.creerDriver(user, token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.CREATED);
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible d'enregistrer le driver.");

	}


	
	/**
	 * V??rification d'un driver avant cr??ation cot?? public
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "V??rification d'un driver avant cr??ation cot?? public")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Le driver poss??de d??j?? un compte, le mot de passe associ?? ?? ce compte est incorrect"),
			@ApiResponse(code = 400, message = "Erreur lors de la v??rification du driver."),
		    @ApiResponse(code = 201, message = "Driver valide")
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/driver/verifications", 
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity verification(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody CreateDriverPublicParams postBody) {

		// enregistrement
		if (postBody.getChauffeur_email() != null && !"".equals(postBody.getChauffeur_email()) && utilisateurDriverService.emailExist(postBody.getChauffeur_email(), postBody.getPays())) {

			// si l'email existe d??j??, on regarde si le mot de passe saisie est celui du driver
			UtilisateurDriver driver = utilisateurDriverService.login(postBody.getChauffeur_email(), postBody.getChauffeur_password(), postBody.getPays());
			if (driver == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le driver poss??de d??j?? un compte, le mot de passe associ?? ?? ce compte est incorrect");
			}

		}
		
		if (postBody.getChauffeur_numero_telephone_1() != null && !"".equals(postBody.getChauffeur_numero_telephone_1()) && utilisateurDriverService.numeroDeTelephoneExist(postBody.getChauffeur_numero_telephone_1(), postBody.getPays())) {

			// si l'email existe d??j??, on regarde si le mot de passe saisie est celui du driver
			UtilisateurDriver driver = utilisateurDriverService.login(postBody.getChauffeur_numero_telephone_1(), postBody.getChauffeur_password(), postBody.getPays());
			if (driver == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le driver poss??de d??j?? un compte, le mot de passe associ?? ?? ce compte est incorrect");
			}
		}
		

		return new ResponseEntity<>(true, HttpStatus.OK);

	}

	/**
	 * Modification d'un driver
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Modification d'un driver")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv?? aux admins et op??rateurs ayant droits)"),
		    @ApiResponse(code = 404, message = "Impossible de trouver le driver"),
		    @ApiResponse(code = 200, message = "Driver modifi??", response = UtilisateurDriver.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/driver", 
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity edit(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody EditDriverParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de modifier un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_TRANSPORTEURS) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");		
		}

		String code_pays = jwtProvider.getCodePays(token);

		// chargement
		UtilisateurProprietaire proprietaire = null;
		if (SecurityUtils.proprietaire(jwtProvider, token)) {
			proprietaire = utilisateurProprietaireService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
			List<Vehicule> vehicules = vehiculeService.getByProprietaire(proprietaire, code_pays);
			List<String> drivers = new ArrayList<String>();
			for (Vehicule vehicule : vehicules) {
				if (vehicule.getDriverPrincipal() != null && !drivers.contains(vehicule.getDriverPrincipal().getUuid().toString())) {
					drivers.add(vehicule.getDriverPrincipal().getUuid().toString());
				}
			}
			List<UtilisateurDriver> drivers_2 = utilisateurDriverService.getDriversOfProprietaire(proprietaire,code_pays);
			for (UtilisateurDriver driver_2 : drivers_2) {
				if (!drivers.contains(driver_2.getUuid().toString())) {
					drivers.add(driver_2.getUuid().toString());
				}
			}
			if (!drivers.contains(postBody.getId())) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vous n'avez pas le droit de supprimer ce driver.");
			}
		}


		// chargement
		UtilisateurDriver user = utilisateurDriverService.getByUUID(postBody.getId(), jwtProvider.getCodePays(token));
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le driver.");
		}
		utilisateurDriverService.updateUser(postBody, user, proprietaire);

		actionAuditService.editerDriver(user, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.OK);

	}



	/**
	 * Modification de son compte par le driver
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Modification de son compte par le driver")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv?? aux transporteurs)"),
			@ApiResponse(code = 404, message = "Impossible de trouver le driver"),
			@ApiResponse(code = 200, message = "Driver modifi??", response = UtilisateurDriver.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/driver2",
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity edit(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody EditTransporteurPublicParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de modifier un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.transporteur(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		UUID uuid_driver = jwtProvider.getUUIDFromJWT(token);

		// chargement
		UtilisateurDriver user = utilisateurDriverService.getByUUID(uuid_driver.toString(), jwtProvider.getCodePays(token));
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le driver.");
		}
		utilisateurDriverService.updateUser(postBody, user);


		actionAuditService.editerDriver(user, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.OK);

	}

	/**
	 * Suppression d'un driver
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Suppression d'un driver")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Le driver est attach?? ?? au moins une op??ration."),
			@ApiResponse(code = 400, message = "Le driver est attach?? ?? au moins une op??ration."),
			@ApiResponse(code = 400, message = "Le driver est attach?? ?? au moins un appel d'offre."),
			@ApiResponse(code = 400, message = "Le driver est attach?? ?? au moins un v??hicule."),
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv?? aux admins et op??rateurs ayant droits)"),
		    @ApiResponse(code = 404, message = "Impossible de trouver le driver"),
		    @ApiResponse(code = 200, message = "Driver supprim??", response = Boolean.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/driver", 
			method = RequestMethod.DELETE)
	@CrossOrigin(origins="*")
	public ResponseEntity delete(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody DeleteDriverParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de modifier un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_TRANSPORTEURS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.SUPPRESSION_DRIVER) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// chargement
		if (SecurityUtils.proprietaire(jwtProvider, token)) {
			UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
			List<Vehicule> vehicules = vehiculeService.getByProprietaire(proprietaire, code_pays);
			List<String> drivers = new ArrayList<String>();
			for (Vehicule vehicule : vehicules) {
				if (vehicule.getDriverPrincipal() != null && !drivers.contains(vehicule.getDriverPrincipal().getUuid().toString())) {
					drivers.add(vehicule.getDriverPrincipal().getUuid().toString());
				}
			}
			List<UtilisateurDriver> drivers_2 = utilisateurDriverService.getDriversOfProprietaire(proprietaire,code_pays);
			for (UtilisateurDriver driver_2 : drivers_2) {
				if (!drivers.contains(driver_2.getUuid().toString())) {
					drivers.add(driver_2.getUuid().toString());
				}
			}
			if (!drivers.contains(postBody.getId())) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vous n'avez pas le droit de supprimer ce driver.");
			}
		}

		// chargement
		UtilisateurDriver user = utilisateurDriverService.getByUUID(postBody.getId(), jwtProvider.getCodePays(token));


		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le driver.");
		}
		utilisateurDriverService.delete(user, jwtProvider.getCodePays(token));

		
		actionAuditService.supprimerDriver(user, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.OK);

	}
	
	/**
	 * R??cup??re les informations d'un driver
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "R??cup??re les informations d'un driver")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv?? aux admins, transporteurs et op??rateurs ayant droits)"),
		    @ApiResponse(code = 404, message = "Impossible de trouver le driver"),
		    @ApiResponse(code = 200, message = "Driver demand??", response = UtilisateurDriver.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/driver", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity get(
			@ApiParam(value = "UUID du driver", required = true) @RequestParam("uuid") String uuid,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de cr??er un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if ((!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_TRANSPORTEURS)) && !SecurityUtils.transporteur(jwtProvider, token) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_TRANSPORTEURS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_VEHICULE) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");		
		}

		String code_pays = jwtProvider.getCodePays(token);

		// chargement
		UtilisateurDriver user = null;
		if (SecurityUtils.proprietaire(jwtProvider, token)) {
			UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
			List<Vehicule> vehicules = vehiculeService.getByProprietaire(proprietaire, code_pays);
			List<String> drivers = new ArrayList<String>();
			for (Vehicule vehicule : vehicules) {
				if (vehicule.getDriverPrincipal() != null && !drivers.contains(vehicule.getDriverPrincipal().getUuid().toString())) {
					drivers.add(vehicule.getDriverPrincipal().getUuid().toString());
				}
			}
			List<UtilisateurDriver> drivers_2 = utilisateurDriverService.getDriversOfProprietaire(proprietaire,code_pays);
			for (UtilisateurDriver driver_2 : drivers_2) {
				if (!drivers.contains(driver_2.getUuid().toString())) {
					drivers.add(driver_2.getUuid().toString());
				}
			}
			if (!drivers.contains(uuid)) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vous n'avez pas le droit d'acc??der aux informations de ce driver.");
			}

		} else if (SecurityUtils.transporteur(jwtProvider, token)) {
			if (!jwtProvider.getUUIDFromJWT(token).toString().equals(uuid)) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vous n'avez pas le droit d'acc??der aux informations de ce driver.");
			}
		}

		user = utilisateurDriverService.getByUUID(uuid, jwtProvider.getCodePays(token));

		if (user != null) {
			actionAuditService.getDriver(user, token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.OK);
		}

		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le driver.");
	}


	/**
	 * Liste de tous les drivers
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste de tous les drivers")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv??s aux admins)"),
		    @ApiResponse(code = 200, message = "Liste des drivers (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/drivers", 
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_offres(
			@ApiParam(value = "Crit??res de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de consulter les op??rateurs katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_TRANSPORTEURS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_TRANSPORTEURS) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			 throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// param??tres du datatable
		String draw 		= postBody.getFirst("draw").toString();
		Integer length 		= Integer.valueOf(postBody.getFirst("length").toString());

		// tri, sens et num??ro de page
		String colonneTriDefaut = "createdOn";
		List<String> colonnesTriAutorise = Arrays.asList("createdOn", "prenom", "nom", "email");

		String order_column_bdd = DatatableUtils.getOrderColonne(colonneTriDefaut, colonnesTriAutorise, postBody);
		String sort_bdd = DatatableUtils.getSort(postBody);
		Integer numero_page = DatatableUtils.getNumeroPage(postBody, length);

		// filtrage
		List<String> colonnesFiltrageActive = Arrays.asList("nom", "prenom", "numeroTelephone1", "email", "numeroPermis");
		ParentSpecificationsBuilder builder = new UtilisateurDriverSpecificationsBuilder();
		Specification spec = DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, null);
		if (spec == null) {
			spec = Specification.where(DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, null));
		}

		// filtrage par date de cr??ation
		spec = DatatableUtils.fitrageDate(spec, postBody, "createdOn", "createdOn");

		// filtre sur l'activation
		Map<Integer, String> indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		Integer position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, "activate");
		if (position_colonne != null) {
			String filtre_par_expediteur = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			if (filtre_par_expediteur != null && !"".equals(filtre_par_expediteur.trim())) {
				Specification spec2 = new Specification<Client>() {
					public Predicate toPredicate(Root<Client> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						predicates.add(builder.equal(root.get("activate"), "1".equals(filtre_par_expediteur)));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec = spec.and(spec2);
			}
		}

		// filtrage sur le propri??taire
		if (SecurityUtils.proprietaire(jwtProvider, token)) {
			UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
			List<Vehicule> vehicules = vehiculeService.getByProprietaire(proprietaire, code_pays);
			List<UUID> drivers = new ArrayList<UUID>();
			for (Vehicule vehicule : vehicules) {
				if (vehicule.getDriverPrincipal() != null && !drivers.contains(vehicule.getDriverPrincipal().getUuid())) {
					drivers.add(vehicule.getDriverPrincipal().getUuid());
				}
			}
			List<UtilisateurDriver> drivers_2 = utilisateurDriverService.getDriversOfProprietaire(proprietaire,code_pays);
			for (UtilisateurDriver driver_2 : drivers_2) {
				if (!drivers.contains(driver_2.getUuid())) {
					drivers.add(driver_2.getUuid());
				}
			}
			Specification spec2 = new Specification<Client>() {
				public Predicate toPredicate(Root<Client> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					List<Predicate> predicates = new ArrayList<Predicate>();
					predicates.add(root.get("uuid").in(drivers));
					return builder.and(predicates.toArray(new Predicate[predicates.size()]));
				}
			};
			spec = spec.and(spec2);
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


		// pr??paration les deux requ??tes (r??sultat et comptage)
		Page<UtilisateurDriver> leads = utilisateurDriverService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);
		Long total = utilisateurDriverService.countAll(spec);
		
		
		actionAuditService.getDrivers(token);

		// pr??pare les r??sultast
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
	 * Liste de tous les drivers
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste de tous les drivers")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv??s aux admins)"),
			@ApiResponse(code = 200, message = "Liste des drivers)", response = ListDriver.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/drivers/liste",
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_offres(
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de consulter les op??rateurs katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_TRANSPORTEURS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_TRANSPORTEURS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		// param??tres du datatable
		Integer length 		= Integer.valueOf(999999999);

		// tri, sens et num??ro de page
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


		// pr??paration les deux requ??tes (r??sultat et comptage)
		Page<UtilisateurDriver> leads = utilisateurDriverService.getAllPagined(order_column_bdd, sort_bdd, 0, length, spec_general);

		actionAuditService.getDrivers(token);

		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(leads.getContent()), HttpStatus.OK);
	}



	/**
	 * Export des drivers
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Export des drivers")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv??s aux admins et op??rateurs ayant droits)"),
			@ApiResponse(code = 200, message = "CSV des drivers")
	})
	@RequestMapping(
			produces = "application/json",
			value = "/drivers/export",
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity<byte[]> export(
			@ApiParam(value = "Date de d??but", required = true) @RequestParam("date_debut") String date_debut,
			@ApiParam(value = "Date de fin", required = true) @RequestParam("date_fin") String date_fin,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestParam("Token") String token) throws Exception {

		// v??rifie que le jeton est valide et que les droits lui permettent de consulter les admin katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.admin(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
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


		// pr??paration les deux requ??tes (r??sultat et comptage)
		Page<UtilisateurDriver> leads = utilisateurDriverService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);

		// convertion liste en excel
		byte[] bytesArray = exportExcelService.export(leads, null);

		// audit
		actionAuditService.exportDrivers(token);

		return ResponseEntity.ok()
				//.headers(headers) // add headers if any
				.contentLength(bytesArray.length)
				.contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
				.body(bytesArray);
	}


	/**
	 * Autocompletion des drivers
	 * @return
	 */
	/*@RequestLogger(name = Application.microservice)*/
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Autocompletion des drivers")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (admin, ou op??rateur ayant le droit)"),
		    @ApiResponse(code = 200, message = "Liste des drivers", response = ListDriver.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/drivers/autocompletion", 
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity autocompletion(
			@ApiParam(value = "Partie du nom du driver ?? rechercher", required = true) @RequestParam("query") String query, 
			@ApiParam(value = "Jeton JWT pour autentification", required = true) @RequestParam("token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// v??rifie que les droits lui permettent de lire les exp??diteurs
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_TRANSPORTEURS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_OPERATIONS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_VEHICULE) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		List<UtilisateurDriver> drivers_autorises = null;
		if (SecurityUtils.proprietaire(jwtProvider, token)) {
			UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
			List<Vehicule> vehicules = vehiculeService.getByProprietaire(proprietaire, code_pays);
			drivers_autorises = new ArrayList<UtilisateurDriver>();
			for (Vehicule vehicule : vehicules) {
				if (vehicule.getDriverPrincipal() != null && !drivers_autorises.contains(vehicule.getDriverPrincipal().getUuid().toString())) {
					drivers_autorises.add(vehicule.getDriverPrincipal());
				}
			}
			List<UtilisateurDriver> drivers_2 = utilisateurDriverService.getDriversOfProprietaire(proprietaire,code_pays);
			for (UtilisateurDriver driver_2 : drivers_2) {
				if (!drivers_autorises.contains(driver_2)) {
					drivers_autorises.add(driver_2);
				}
			}
		}

		List<UtilisateurDriver> drivers = utilisateurDriverService.autocomplete(query, jwtProvider.getCodePays(token), drivers_autorises);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(drivers), HttpStatus.OK);

	}


	/**
	 * R??cup??re le fichier image de la photo de profil du driver
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "R??cup??re le fichier image de la photo de profil du driver")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (admin, ou op??rateur)"),
			@ApiResponse(code = 200, message = "Fichier image", response = byte[].class)
	})
	@RequestMapping(
			produces = MediaType.IMAGE_PNG_VALUE,
			value = "/driver/photo_profil", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public byte[]getSignaturedeExpediteur(
			@ApiParam(value = "UUID du driver", required = true) @RequestParam("uuid") String uuid, 
			@ApiParam(value = "Jeton JWT pour autentification", required = true) @RequestParam("token") String token) {

		// v??rifie que le jeton est valide
		if (!jwtProvider.isValidJWT(token)) {
			return null;
		}

		// v??rifie que les droits lui permettent de afficher une commande
		if (!SecurityUtils.admin(jwtProvider, token) && !SecurityUtils.operateur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			return null;
		}

		UtilisateurDriver driver = utilisateurDriverService.getByUUID(uuid, jwtProvider.getCodePays(token));
		if (driver != null) {
			if (SecurityUtils.proprietaire(jwtProvider, token)) {
				UtilisateurProprietaire proprietaire = utilisateurProprietaireService.get(token, jwtProvider.getCodePays(token));
				if (proprietaire != null && driver.getProprietaire() != null && !driver.getProprietaire().equals(proprietaire)) {
					return null;
				}
			}
			return driverPhotoService.get(driver.getPhoto());
		}
		return null;

	}
	
	/**
	 * R??cup??re le fichier image de la photo du permis du driver
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "R??cup??re le fichier image de la photo du permis du driver")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (admin, ou op??rateur)"),
			@ApiResponse(code = 200, message = "Fichier image", response = byte[].class)
	})
	@RequestMapping(
			produces = MediaType.IMAGE_PNG_VALUE,
			value = "/driver/photo_permis", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public byte[]getPhotoPermis(
			@ApiParam(value = "UUID du driver", required = true) @RequestParam("uuid") String uuid, 
			@ApiParam(value = "Jeton JWT pour autentification", required = true) @RequestParam("token") String token) {
		logger.info("uuid=" + uuid + " token=" + token);

		// v??rifie que le jeton est valide
		if (!jwtProvider.isValidJWT(token)) { 
			return null;
		}

		// v??rifie que les droits lui permettent de afficher une commande
		if (!SecurityUtils.admin(jwtProvider, token) && !SecurityUtils.operateur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			return null;
		}

		String code_pays = jwtProvider.getCodePays(token);

		// chargement
		if (SecurityUtils.proprietaire(jwtProvider, token)) {
			UtilisateurProprietaire proprietaire = utilisateurProprietaireService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
			List<Vehicule> vehicules = vehiculeService.getByProprietaire(proprietaire, code_pays);
			List<String> drivers = new ArrayList<String>();
			for (Vehicule vehicule : vehicules) {
				if (vehicule.getDriverPrincipal() != null && !drivers.contains(vehicule.getDriverPrincipal().getUuid().toString())) {
					drivers.add(vehicule.getDriverPrincipal().getUuid().toString());
				}
			}
			List<UtilisateurDriver> drivers_2 = utilisateurDriverService.getDriversOfProprietaire(proprietaire,code_pays);
			for (UtilisateurDriver driver_2 : drivers_2) {
				if (!drivers.contains(driver_2.getUuid().toString())) {
					drivers.add(driver_2.getUuid().toString());
				}
			}
			logger.info("drivers=" + drivers);
			logger.info("uuid=" + uuid);
			if (!drivers.contains(uuid)) {
				logger.info("aaaa");
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vous n'avez pas le droit de consulter la photo du permis de ce driver");
			}
		}

		UtilisateurDriver driver = utilisateurDriverService.getByUUID(uuid, jwtProvider.getCodePays(token));
		if (driver != null) {

			return driverPhotoService.get(driver.getPhotoPermis());
		}
		return null;

	}



	/**
	 * Est ce que le num??ro de t??l??phone est d??j?? utilis?? sur un compte driver ?
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Est ce que le num??ro de t??l??phone est d??j?? utilis?? sur un compte driver ?")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "true si le num??ro de t??l??phone est d??j?? utilis??, false sinon", response = Boolean.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/driver/telephone",
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity check_email2(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody TelephoneParams postBody) throws JsonProcessingException {

		boolean exist = false;
		if (postBody.getTelephone() != null && !"".equals(postBody.getTelephone().trim())) {
			exist = utilisateurDriverService.numeroDeTelephoneExist(postBody.getTelephone(), postBody.getPays());
		}
		return new ResponseEntity<>(mapperJSONService.get(null).writeValueAsString(exist), HttpStatus.OK);

	}

	/**
	 * Connexion d'un transporteur
	 * @param postBody
	 * @return
	 */
	/*@RequestLogger(name = Application.microservice)*/
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Connexion d'un transporteur")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Plaque d'immatriculation introuvable"),
			@ApiResponse(code = 400, message = "Le v??hicule est d??sactiv??"),
		    @ApiResponse(code = 403, message = "Le v??hicule est d??sactiv??"),
		    @ApiResponse(code = 403, message = "Vous devez indiquer une plaque d'immatriculation"),
			@ApiResponse(code = 401, message = "Votre compte est d??sactiv??"),
			@ApiResponse(code = 401, message = "Veuillez v??rifier vos identifiants"),
		    @ApiResponse(code = 200, message = "Token d'autentification", response = JSONObject.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/transporteur/signin", 
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity login(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody SigninImmatriculationParams postBody) throws JsonProcessingException {
		if (postBody.getPays() == null || "".equals(postBody.getPays())) {
			postBody.setPays("CI");
		}

		// quel est le type de compte (propri??taire ou transporteur ?)
		UtilisateurDriver transporteur = utilisateurDriverService.login(postBody.getLogin(), postBody.getMot_de_passe(), postBody.getPays());
		UtilisateurProprietaire proprietaire = utilisateurProprietaireService.login(postBody.getLogin(), postBody.getMot_de_passe(), postBody.getPays());
		
		JSONObject res = new JSONObject();
		List<String> additional_informations = new ArrayList<String>();
		additional_informations.add(0, null);
		additional_informations.add(1, null);
		additional_informations.add( 2, postBody.getWebview());
		additional_informations.add(3, null);

		if (transporteur != null && postBody.getImmatriculation() != null && !"".equals(postBody.getImmatriculation().trim())) {
			logger.info("c'est un transporteur");

			// si c'est un driver, il faut qu'il renseigne son mot de passe
			/*if (postBody.getImmatriculation() == null || "".equals(postBody.getImmatriculation().trim())) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous devez indiquer une plaque d'immatriculation");
			}*/

			// est ce que le compte est d??sactiv??
			if (transporteur.isActivate() == false) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Votre compte est d??sactiv??");
			}

			// v??rification authentification immatriculation/mot de passe
			Vehicule vehicule = vehiculeService.signin(postBody, postBody.getPays());
			if (vehicule == null) {
				return new ResponseEntity<>(true, HttpStatus.NOT_FOUND);
			} else if (!vehicule.isActivate()) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Le v??hicule est d??sactiv??");
			}

			// retourne les codes sur les informations obligatoires
			List<String> ret = new ArrayList<String>();
			if (transporteur.getPhotoPermis() == null || transporteur.getPhotoPermis().equals("")) {
				ret.add("1");
			}
			if (vehicule.getDocumentCarteGrise() == null || vehicule.getDocumentCarteGrise().equals("")) {
				ret.add("2");
			}
			if (vehicule.getPhotoPrincipale() == null || vehicule.getPhotoPrincipale().equals("")) {
				ret.add("3");
			}

			additional_informations.set(0,  vehicule.getUuid().toString());
			additional_informations.set( 1, vehicule.getImmatriculation());
			additional_informations.add( 3, String.join("-", ret));

			actionAuditService.loginTransporteur((UtilisateurDriver)transporteur, postBody.getPays());

			String token = jwtProvider.createJWT(transporteur, additional_informations);

			res.put("token", token);

			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(res), HttpStatus.OK);

		} else if (proprietaire != null  ) {
			logger.info("c'est un propri??taire");
			// est ce que le compte est d??sactiv??
			if ((proprietaire != null && proprietaire.isActivate() == false)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Votre compte est d??sactiv??");
			}

			if (postBody.getImmatriculation() != null && !"".equals(postBody.getImmatriculation())) {
				additional_informations.set(1, postBody.getImmatriculation());
			}

			actionAuditService.loginProprietaire((UtilisateurProprietaire)proprietaire, postBody.getPays());

			String token = jwtProvider.createJWT(proprietaire, additional_informations);
			res.put("token", token);

			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(res), HttpStatus.OK);
		} else {

			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Veuillez v??rifier vos identifiants");
		}



	}


	/**
	 * Liste des actions d'un driver
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste des actions d'un driver")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv?? aux admins et op??rateurs ayant droits)"),
			@ApiResponse(code = 200, message = "Liste des actions d'un exp??diteur (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/chauffeur/actions",
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_actions(
			@ApiParam(value = "Crit??res de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de consulter les admin katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_TRANSPORTEURS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		// param??tres du datatable
		String draw 		= postBody.getFirst("draw").toString();
		Integer length 		= Integer.valueOf(postBody.getFirst("length").toString());

		// tri, sens et num??ro de page
		String colonneTriDefaut = "createdOn";
		List<String> colonnesTriAutorise = Arrays.asList("createdOn");

		String order_column_bdd = DatatableUtils.getOrderColonne(colonneTriDefaut, colonnesTriAutorise, postBody);
		String sort_bdd = DatatableUtils.getSort(postBody);
		Integer numero_page = DatatableUtils.getNumeroPage(postBody, length);

		// filtrage par exp??diteur
		Specification spec = null;
		String uuid_driver = null;
		if (postBody.containsKey("chauffeur")) {
			uuid_driver = postBody.getFirst("chauffeur").toString();

			UUID chauffeur_uuid = UUID.fromString(uuid_driver);
			UtilisateurDriver driver = utilisateurDriverService.getByUUID(chauffeur_uuid.toString(), jwtProvider.getCodePays(token));
			uuid_driver = driver.getUuid().toString();

			final String uuid_driver2 = uuid_driver;

			// https://stackoverflow.com/questions/35201604/how-to-create-specification-using-jpaspecificationexecutor-by-combining-tables
			spec = new Specification<ActionAudit>() {
				public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					List<Predicate> predicates = new ArrayList<Predicate>();
					predicates.add(builder.equal(root.get("uuidUtilisateur"), uuid_driver2));
					return builder.and(predicates.toArray(new Predicate[predicates.size()]));
				}
			};

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

		// pr??paration les deux requ??tes (r??sultat et comptage)
		Page<ActionAudit> leads = actionAuditService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);
		Long total = actionAuditService.countAll(spec);


		actionAuditService.getActionAuditClient(token, uuid_driver);

		// pr??pare les r??sultast
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



}
