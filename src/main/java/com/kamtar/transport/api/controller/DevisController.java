package com.kamtar.transport.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.criteria.*;
import com.kamtar.transport.api.enums.ClientPersonnelListeDeDroits;
import com.kamtar.transport.api.enums.OperateurListeDeDroits;
import com.kamtar.transport.api.enums.OperationStatut;
import com.kamtar.transport.api.enums.UtilisateurTypeDeCompte;
import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.*;
import com.kamtar.transport.api.repository.GeolocRepository;
import com.kamtar.transport.api.security.SecurityUtils;
import com.kamtar.transport.api.service.*;
import com.kamtar.transport.api.swagger.*;
import com.kamtar.transport.api.utils.DatatableUtils;
import com.kamtar.transport.api.utils.JWTProvider;
import com.wbc.core.utils.FileUtils;
import io.swagger.annotations.*;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


@Api(value="Gestion des devis", description="API Rest qui g??re l'ensemble des devis")
@RestController
@EnableWebMvc
public class DevisController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(DevisController.class);


	@Value("${kamtar.env}")
	private String kamtar_env;

	@Autowired
	CountryService countryService;

	@Autowired
	ExportExcelService exportExcelService;

	@Autowired
	MapperJSONService mapperJSONService;

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
	DevisService devisService;

	@Autowired
	VehiculeCarrosserieService vehiculeCarrosserieService;
	
	

	/**
	 * R??cup??re les informations d'un devis
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "R??cup??re les informations d'un devis")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (admin, ou op??rateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver le devis"),
			@ApiResponse(code = 200, message = "Retourne le devis demand??", response = Devis.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/devis",
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity get(
			@ApiParam(value = "UUID du devis", required = true) @RequestParam("uuid") String uuid,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de cr??er un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// v??rifie que les droits lui permettent de g??rer un vehicule
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_DEVIS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		// chargement
		Devis devis = devisService.getByUUID(uuid, jwtProvider.getCodePays(token));
		if (devis != null) {
			actionAuditService.getDevis(devis, token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(devis), HttpStatus.OK);
		}


		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le devis.");
	}



	/**
	 * Liste de tous les devis
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste de tous les devis")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (admin ou op??rateur ayant le droit)"),
			@ApiResponse(code = 200, message = "Liste de tous les vehicule (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/devis_liste",
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_offres(

			@ApiParam(value = "Crit??res de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de consulter les op??rateurs katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// v??rifie que les droits lui permettent de afficher un vehicule
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_DEVIS) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_DEVIS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		// param??tres du datatable
		String draw 		= postBody.getFirst("draw").toString();
		Integer length 		= Integer.valueOf(postBody.getFirst("length").toString());

		// tri, sens et num??ro de page
		String colonneTriDefaut = "createdOn";
		List<String> colonnesTriAutorise = Arrays.asList("createdOn", "code", "departDateProgrammeeOperation");

		String order_column_bdd = DatatableUtils.getOrderColonne(colonneTriDefaut, colonnesTriAutorise, postBody);
		String sort_bdd = DatatableUtils.getSort(postBody);
		Integer numero_page = DatatableUtils.getNumeroPage(postBody, length);

		// filtrage
		List<String> colonnesFiltrageActive = Arrays.asList("statut", "code", "clientNom", "clientPrenom", "departAdresseComplete", "arriveeAdresseComplete", "categorieVehicule");
		ParentSpecificationsBuilder builder = new DevisSpecificationsBuilder();
		Specification spec_general = Specification.where(DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, null));

		// filtrage par date de cr??ation
		spec_general = DatatableUtils.fitrageDate(spec_general, postBody, "arriveeDateProgrammee", "arriveeDateProgrammee");
		spec_general = DatatableUtils.fitrageDate(spec_general, postBody, "createdOn", "createdOn");
		spec_general = DatatableUtils.fitrageDate(spec_general, postBody, "departDateProgrammeeOperation", "departDateProgrammeeOperation");


		// filtrage par pays kamtar
		Specification spec_pays = new Specification<Devis>() {
			public Predicate toPredicate(Root<Devis> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get("codePays"), jwtProvider.getCodePays(token)));
				return builder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
		spec_general = spec_general.and(spec_pays);


		// pr??paration les deux requ??tes (r??sultat et comptage)
		Page<Devis> leads = devisService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec_general);
		Long total = devisService.countAll(spec_general);

		List<Devis> liste_devis = leads.getContent();
		actionAuditService.getDevisListe(token);

		// pr??pare les r??sultast
		JSONArray jsonArrayOffres = new JSONArray();
		if (leads != null) {
			jsonArrayOffres.addAll(liste_devis);
		}
		Map<String, Object> jsonDataResults = new LinkedHashMap<String, Object>();
		jsonDataResults.put("draw", draw);	
		jsonDataResults.put("recordsTotal", total);
		jsonDataResults.put("recordsFiltered", total);
		jsonDataResults.put("data", jsonArrayOffres);		

		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(jsonDataResults), HttpStatus.OK);
	}


	/**
	 * Cr??ation d'un devis par un prospect
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Cr??ation d'un devis par un prospect")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Impossible d'enregistrer le devis."),
			@ApiResponse(code = 201, message = "Devis cr????", response = Devis.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/devis",
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity createOperationExpediteur(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody CreateDevisParams postBody,
			@ApiParam(value = "Code du pays", required = true)  @RequestHeader("Pays") String code_pays) throws JsonProcessingException {

		// enregistrement
		Operation operation = devisService.create(postBody, code_pays);
		if (operation != null) {
			return new ResponseEntity<>(mapperJSONService.get(null).writeValueAsString(operation), HttpStatus.CREATED);
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible d'enregistrer le devis.");

	}

	/**
	 * Passage ?? "r??ponse impossible"
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Passage ?? \"r??ponse impossible")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Impossible d'enregistrer le devis."),
			@ApiResponse(code = 404, message = "Impossible de trouver le devis."),
			@ApiResponse(code = 201, message = "Devis mis ?? jour", response = Devis.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/devis/impossible",
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity devisReponseImpossible(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody DevisParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de cr??er un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// v??rifie que les droits lui permettent de g??rer un vehicule
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_DEVIS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// enregistrement
		Devis devis = devisService.getByUUID(postBody.getDevis(), code_pays);
		if (devis != null) {
			devis = devisService.passerAReponseImpossible(devis, code_pays);
			actionAuditService.changerStatutDevis(devis, token);
			return new ResponseEntity<>(mapperJSONService.get(null).writeValueAsString(devis), HttpStatus.CREATED);
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible de charger le devis.");

	}


	/**
	 * Passage ?? "en cours de traitement"
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Passage ?? 'en cours de traitement'")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Impossible d'enregistrer le devis."),
			@ApiResponse(code = 404, message = "Impossible de trouver le devis."),
			@ApiResponse(code = 201, message = "Devis mis ?? jour", response = Devis.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/devis/traitement",
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity devisPasseEnCourDeTraitement(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody DevisParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de cr??er un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// v??rifie que les droits lui permettent de g??rer un vehicule
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_DEVIS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// enregistrement
		Devis devis = devisService.getByUUID(postBody.getDevis(), code_pays);
		if (devis != null) {
			devis = devisService.passerAEnCoursDeTraitement(devis, code_pays);
			actionAuditService.changerStatutDevis(devis, token);
			return new ResponseEntity<>(mapperJSONService.get(null).writeValueAsString(devis), HttpStatus.CREATED);
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible de charger le devis.");

	}


	/**
	 * Convertion d'un devis en op??ration
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Convertion d'un devis en op??ration")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Impossible de cr??er l'op??ration."),
			@ApiResponse(code = 404, message = "Impossible de trouver le devis."),
			@ApiResponse(code = 201, message = "Operation cr????e", response = Operation.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/devis/convertir",
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity devisConvertirEnOperation(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody DevisParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de cr??er un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// v??rifie que les droits lui permettent de g??rer un vehicule
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_DEVIS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// enregistrement
		Devis devis = devisService.getByUUID(postBody.getDevis(), code_pays);
		if (devis != null) {
			Operation operation = devisService.convertionEnOperation(devis, code_pays);
			actionAuditService.changerStatutDevis(devis, token);
			actionAuditService.convertionDevisEnOperation(devis, operation, token);
			return new ResponseEntity<>(mapperJSONService.get(null).writeValueAsString(operation), HttpStatus.CREATED);
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible de charger le devis.");

	}


	/**
	 * Export des devis
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Export des devis")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv??s aux admins et op??raters ayant droit)"),
			@ApiResponse(code = 200, message = "CSV des devis")
	})
	@RequestMapping(
			produces = "application/json",
			value = "/devis/export",
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
		Page<Devis> leads = devisService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);

		// convertion liste en excel
		byte[] bytesArray = exportExcelService.export(leads, null);

		// audit
		actionAuditService.exportDevis(token);

		return ResponseEntity.ok()
				//.headers(headers) // add headers if any
				.contentLength(bytesArray.length)
				.contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
				.body(bytesArray);
	}




	/**
	 * Suppression d'un devis
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Modification d'un devis")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (admin, ou op??rateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver le devis"),
			@ApiResponse(code = 200, message = "Devis supprim??", response = Boolean.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/devis",
			method = RequestMethod.DELETE)
	@CrossOrigin(origins="*")
	public ResponseEntity delete(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody DeleteDevisParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de modifier un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// v??rifie que les droits lui permettent de g??rer un v??hicule
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_DEVIS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		// chargement
		Devis devis = devisService.getByUUID(postBody.getId(), jwtProvider.getCodePays(token));
		if (devis == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le devis.");
		}
		devisService.delete(devis, jwtProvider.getCodePays(token));

		actionAuditService.supprimerDevis(devis, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(devis), HttpStatus.OK);

	}


}
