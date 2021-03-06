package com.kamtar.transport.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.criteria.OperationSpecificationsBuilder;
import com.kamtar.transport.api.criteria.ParentSpecificationsBuilder;
import com.kamtar.transport.api.criteria.PredicateUtils;
import com.kamtar.transport.api.enums.*;
import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.*;
import com.kamtar.transport.api.repository.GeolocRepository;
import com.kamtar.transport.api.repository.UtilisateurClientPersonnelRepository;
import com.kamtar.transport.api.repository.UtilisateurDriverRepository;
import com.kamtar.transport.api.repository.UtilisateurProprietaireRepository;
import com.kamtar.transport.api.security.SecurityUtils;
import com.kamtar.transport.api.service.*;
import com.kamtar.transport.api.swagger.*;
import com.kamtar.transport.api.utils.DatatableUtils;
import com.kamtar.transport.api.utils.DateUtils;
import com.kamtar.transport.api.utils.EnumUtils;
import com.kamtar.transport.api.utils.JWTProvider;
import com.wbc.core.utils.FileUtils;
import com.wbc.core.utils.StringUtils;
import io.swagger.annotations.*;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
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


@Api(value="Gestion des r??clamations", description="API Rest qui g??re l'ensemble des r??clamations")
@RestController
@EnableWebMvc
public class ReclamationController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(ReclamationController.class);


	@Autowired
	private UtilisateurClientPersonnelRepository utilisateurClientPersonnelRepository;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	MapperJSONService mapperJSONService;

	@Autowired
	ExportExcelService exportExcelService;

	@Autowired
	UtilisateurClientService utilisateurClientService;

	@Autowired
	FactureProprietaireService factureProprietaireService;

	@Autowired
	CountryService countryService;

	@Autowired
	OperationService operationService;

	@Autowired
	ReclamationService reclamationService;

	@Autowired
	ReclamationEchangeService reclamationEchangeService;

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
	 * Cr??ation d'une r??clamation par un client
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Cr??ation d'une r??clamation par un client")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (client)"),
			@ApiResponse(code = 400, message = "Impossible d'enregistrer la r??clamation."),
			@ApiResponse(code = 404, message = "Impossible de charger l'op??ration."),
			@ApiResponse(code = 401, message = "Vous n'avez pas le droit d'enregistrer une r??clamation pour cette op??ration."),
			@ApiResponse(code = 201, message = "R??clamation cr????e", response = Reclamation.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/reclamation",
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity create(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody CreateReclamationParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que 
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// v??rifie que les droits lui permettent de g??rer une operation
		if (!SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnel(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// chargement de l'op??ration
		Operation operation = operationService.getByUUID(postBody.getOperation(), code_pays);
		if (operation == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger l'op??ration");
		}

		// enregistrement
		Reclamation reclamation = reclamationService.create(postBody, operation, token);
		if (reclamation != null) {
			actionAuditService.creerReclamation(reclamation, operation, token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(reclamation), HttpStatus.CREATED);
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible d'enregistrer la r??clamation.");

	}


	/**
	 * Ajouter un ??change ?? une r??clamation
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Ajouter un ??change ?? une r??clamation")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (op??rateur)"),
			@ApiResponse(code = 400, message = "Impossible d'enregistrer l'??change de la r??clamation."),
			@ApiResponse(code = 404, message = "Impossible de charger la r??clamation associ??e."),
			@ApiResponse(code = 201, message = "Echange de r??clamation cr????e", response = ReclamationEchange.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/reclamation/echange",
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity createEchange(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody CreateReclamationEchangeParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// v??rifie que les droits lui permettent de g??rer une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_RECLAMATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// chargement de l'op??ration
		Reclamation reclamation = reclamationService.getByUUID(postBody.getReclamation(), code_pays);
		if (reclamation == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger la r??clamation");
		}

		// enregistrement
		ReclamationEchange reclamation_echange = reclamationEchangeService.create(postBody, token);
		if (reclamation_echange != null) {
			actionAuditService.creerReclamationEchange(reclamation_echange, token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(reclamation_echange), HttpStatus.CREATED);
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible d'enregistrer la r??clamation.");

	}


	/**
	 * Modifie le statut d'une r??clamation
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Modifie le statut d'une r??clamation")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (op??rateur)"),
			@ApiResponse(code = 400, message = "Impossible d'enregistrer le nouveau statut de la r??clamation."),
			@ApiResponse(code = 404, message = "Impossible de charger la r??clamation."),
			@ApiResponse(code = 201, message = "R??clamation modifi??", response = Reclamation.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/reclamation",
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity changerEchange(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody EditReclamationParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// v??rifie que les droits lui permettent de g??rer une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_RECLAMATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// chargement de la r??clamation
		Reclamation reclamation = reclamationService.getByUUID(postBody.getReclamation(), code_pays);
		if (reclamation == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger la r??clamation");
		}

		// enregistrement
		reclamation = reclamationService.changer_statut(postBody, reclamation, token);

		actionAuditService.changerStatutReclamation(reclamation, token);
		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(reclamation), HttpStatus.CREATED);


	}

	/**
	 * R??cup??re les informations d'une r??clamation
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "R??cup??re les informations d'une r??clamation")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Jeton invalide, veuillez vous reconnecter"),
			@ApiResponse(code = 403, message = "Vous n'avez pas le droit d'effectuer cette op??ration"),
			@ApiResponse(code = 404, message = "Impossible de trouver la r??clamation"),
			@ApiResponse(code = 200, message = "R??clamation demand??e", response = Reclamation.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/reclamation",
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity get(
			@ApiParam(value = "UUID de la r??clamation", required = true) @RequestParam("uuid") String uuid,
			@ApiParam(value = "Est ce qu'il faut charger les ??changes ?", required = false) @RequestParam("echanges") Boolean echanges,
			@ApiParam(value = "Jeton JWT pour authentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// v??rifie que les droits lui permettent d'afficher une r??clamation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_RECLAMATIONS) && (!SecurityUtils.client(jwtProvider, token))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// chargement de la r??clamation
		Reclamation reclamation = reclamationService.getByUUID(uuid, code_pays);
		if (SecurityUtils.client(jwtProvider, token)) {

			// v??rifications
			UtilisateurClient utilisateur_client = clientExpediteurService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
			UtilisateurClientPersonnel utilisateur_client_personnel = utilisateurClientPersonnelService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
			Client client = null;
			if (utilisateur_client != null) {
				String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
				client = clientService.getByUUID(uuid_client, code_pays);

				if (!utilisateur_client.equals(reclamation.getClient_utilisateur())) {
					throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vous n'avez pas le droit d'acc??der ?? cette r??clamation.");
				}

			} else if (utilisateur_client_personnel != null) {
				String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
				client = clientService.getByUUID(uuid_client, code_pays);

				if (!utilisateur_client_personnel.equals(reclamation.getClient_personnel())) {
					throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vous n'avez pas le droit d'acc??der ?? cette r??clamation.");
				}
			}

		}

		if (reclamation != null) {

			// chargement des ??changes si demand??
			if (echanges) {
				List<ReclamationEchange> reclamations_echanges = reclamationEchangeService.get(reclamation, code_pays);
				reclamation.setEchanges(reclamations_echanges);
			}

			actionAuditService.getReclamation(reclamation, token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(reclamation), HttpStatus.OK);
		}

		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le client.");
	}




	/**
	 * Liste de toutes les r??clamations
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste de toutes les r??clamations")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Le statut est invalide."),
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (admin, ou op??rateur ayant le droit)"),
			@ApiResponse(code = 200, message = "Liste des r??clamations (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/reclamations",
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_reclamations(
			@ApiParam(value = "Crit??res de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de consulter les op??rateurs katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// v??rifie que les droits lui permettent de traiter les recommandations
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_RECLAMATIONS) && !SecurityUtils.client_personnel(jwtProvider, token) && !SecurityUtils.client(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		// param??tres du datatable
		String draw 		= postBody.getFirst("draw").toString();
		Integer length 		= Integer.valueOf(postBody.getFirst("length").toString());

		// tri, sens et num??ro de page
		String colonneTriDefaut = "createdOn";
		List<String> colonnesTriAutorise = Arrays.asList("code", "createdOn");

		String order_column_bdd = DatatableUtils.getOrderColonne(colonneTriDefaut, colonnesTriAutorise, postBody);

		String sort_bdd = DatatableUtils.getSort(postBody);
		Integer numero_page = DatatableUtils.getNumeroPage(postBody, length);

		// filtrage
		List<String> colonnesFiltrageActive = Arrays.asList("code", "statut", "motif");
		ParentSpecificationsBuilder builder = new OperationSpecificationsBuilder();
		Specification spec_general = DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, "CUSTOM_NON_VALIDE");
		if (spec_general == null) {
			spec_general = Specification.where(DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, "CUSTOM_NON_VALIDE"));
		}

		// filtrage par date de cr??ation
		spec_general = DatatableUtils.fitrageDate(spec_general, postBody, "createdOn", "createdOn");

		// chargement du user et client
		UtilisateurClient utilisateur_client = utilisateurClientService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), jwtProvider.getCodePays(token));
		UtilisateurClientPersonnel utilisateur_client_personnel = utilisateurClientPersonnelService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), jwtProvider.getCodePays(token));
		Client client = null;
		if (utilisateur_client != null) {
			String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
			client = clientService.getByUUID(uuid_client, jwtProvider.getCodePays(token));
		} else if (utilisateur_client_personnel != null) {
			String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
			client = clientService.getByUUID(uuid_client, jwtProvider.getCodePays(token));
		}

		// filtrae par client_utilisateur
		if (utilisateur_client != null) {
			Specification spec2 = new Specification<Operation>() {
				public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					List<Predicate> predicates = new ArrayList<Predicate>();
					predicates.add(builder.equal(root.get("client_utilisateur"), utilisateur_client));
					return builder.and(predicates.toArray(new Predicate[predicates.size()]));
				}
			};
			spec_general = spec_general.and(spec2);

		} else if (utilisateur_client_personnel != null) {
			// filtrage par client_utilisateur
			Specification spec2 = new Specification<Operation>() {
				public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					List<Predicate> predicates = new ArrayList<Predicate>();
					predicates.add(builder.equal(root.get("client_personnel"), utilisateur_client_personnel));
					return builder.and(predicates.toArray(new Predicate[predicates.size()]));
				}
			};
			spec_general = spec_general.and(spec2);
		}

		// filtre par client dans datatable
		Map<Integer, String> indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		Integer position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, "uuid_client");
		if (position_colonne != null) {
			String filtre_par_client = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			if (filtre_par_client != null && !"".equals(filtre_par_client.trim())) {
				Specification spec2 = new Specification<Operation>() {
					public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						Client expediteur = clientService.getByUUID(filtre_par_client, jwtProvider.getCodePays(token));
						predicates.add(builder.equal(root.get("client"), expediteur));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec_general = spec_general.and(spec2);
			}
		}

		// filtre par op??ration dans datatable
		position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, "operation");
		if (position_colonne != null) {
			String filtre_par_operation = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			if (filtre_par_operation != null && !"".equals(filtre_par_operation.trim())) {
				Specification spec3 = new Specification<Operation>() {
					public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						Operation operation = operationService.getByCode(Long.valueOf(filtre_par_operation), jwtProvider.getCodePays(token));
						if (operation != null) {
							predicates.add(builder.equal(root.get("operation"), operation));
						}
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec_general = spec_general.and(spec3);
			}
		}

		// filtrage par statut
		if (postBody.containsKey("statut")) {
			String statut = postBody.getFirst("statut").toString();
			if (!EnumUtils.reclamationStatutContains(statut)) {
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
		List<Reclamation> reclamations = reclamationService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec_general).getContent();
		Long total = reclamationService.countAll(spec_general);

		actionAuditService.getReclamations(token);

		// pr??pare les r??sultast
		JSONArray jsonArrayOffres = new JSONArray();
		if (reclamations != null) {
			jsonArrayOffres.addAll(reclamations);
		}
		Map<String, Object> jsonDataResults = new LinkedHashMap<String, Object>();
		jsonDataResults.put("draw", draw);	
		jsonDataResults.put("recordsTotal", total);		
		jsonDataResults.put("recordsFiltered", total);	
		jsonDataResults.put("data", jsonArrayOffres);		

		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(jsonDataResults), HttpStatus.OK);
	}




	/**
	 * Export des r??clamations
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Export des r??clamations")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv??s aux admins)"),
			@ApiResponse(code = 200, message = "CSV des administrateurs")
	})
	@RequestMapping(
			produces = "application/json",
			value = "/reclamations/export",
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
		Page<Reclamation> leads = reclamationService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);

		// convertion liste en excel
		byte[] bytesArray = exportExcelService.export(leads, null);

		// audit
		actionAuditService.exportReclamations(token);

		return ResponseEntity.ok()
				//.headers(headers) // add headers if any
				.contentLength(bytesArray.length)
				.contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
				.body(bytesArray);
	}

}
