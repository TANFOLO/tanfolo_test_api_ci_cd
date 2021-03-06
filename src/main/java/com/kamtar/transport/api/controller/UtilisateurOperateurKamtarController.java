package com.kamtar.transport.api.controller;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.enums.OperateurListeDeDroits;
import com.kamtar.transport.api.model.UtilisateurAdminKamtar;
import com.kamtar.transport.api.service.*;
import com.kamtar.transport.api.swagger.ListOperateursKamtar;
import com.kamtar.transport.api.swagger.ListUtilisateurAdminKamtar;
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
import com.kamtar.transport.api.criteria.UtilisateurOperateurKamtarSpecificationsBuilder;
import com.kamtar.transport.api.enums.UtilisateurTypeDeCompte;
import com.kamtar.transport.api.model.ActionAudit;
import com.kamtar.transport.api.model.Utilisateur;
import com.kamtar.transport.api.model.UtilisateurOperateurKamtar;
import com.kamtar.transport.api.params.CreateOperateurKamtarParams;
import com.kamtar.transport.api.params.DeleteOperateurKamtarParams;
import com.kamtar.transport.api.params.EditOperateurKamtarParams;
import com.kamtar.transport.api.params.SigninParams;
import com.kamtar.transport.api.security.SecurityUtils;
import com.kamtar.transport.api.swagger.MapDatatable;
import com.kamtar.transport.api.utils.DatatableUtils;
import com.kamtar.transport.api.utils.JWTProvider;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@Api(value="Gestion des op??rateurs", description="API Rest qui g??re les op??rateurs")
@RestController
@EnableWebMvc
public class UtilisateurOperateurKamtarController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(UtilisateurOperateurKamtarController.class);  

	@Autowired
	CountryService countryService;

	@Autowired
	UtilisateurOperateurKamtarService utilisateurOperateurKamtarService;

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	MapperJSONService mapperJSONService;

	@Autowired
	ExportExcelService exportExcelService;

	@Autowired
	ActionAuditService actionAuditService;

	/**
	 * Cr??ation d'un op??rateur kamtar
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Cr??ation d'un op??rateur kamtar")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "L'adresse e-mail est d??j?? utilis??e."),
			@ApiResponse(code = 400, message = "Le num??ro de t??l??phone est d??j?? utilis??."),
			@ApiResponse(code = 400, message = "Impossible d'enregistrer l'op??rateur Kamtar."),
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv?? aux admins)"),
		    @ApiResponse(code = 201, message = "Op??rateur cr????", response = UtilisateurOperateurKamtar.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/operateur_kamtar", 
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity create(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody CreateOperateurKamtarParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de cr??er un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.admin(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");		
		}

		// enregistrement
		UtilisateurOperateurKamtar user = utilisateurOperateurKamtarService.createUser(postBody, jwtProvider.getCodePays(token));
		if (user != null) {
			actionAuditService.creerOperateurKamtar(user, token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.CREATED);
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible d'enregistrer l'op??rateur Kamtar.");

	}

	/**
	 * Modification d'un op??rateur kamtar
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Modification d'un op??rateur kamtar")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv?? aux admins)"),
		    @ApiResponse(code = 404, message = "Impossible de trouver l'op??rateur"),
		    @ApiResponse(code = 200, message = "Op??rateur modifi??", response = UtilisateurOperateurKamtar.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/operateur_kamtar", 
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity edit(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody EditOperateurKamtarParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de modifier un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.admin(jwtProvider, token) && (SecurityUtils.operateur(jwtProvider, token) && !jwtProvider.getUUIDFromJWT(token).toString().equals(postBody.getId()))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");		
		}

		// chargement
		UtilisateurOperateurKamtar user = utilisateurOperateurKamtarService.getByUUID(postBody.getId(), jwtProvider.getCodePays(token));
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'op??rateur Kamtar.");
		}

		if (SecurityUtils.operateur(jwtProvider, token)) {
			utilisateurOperateurKamtarService.updateUserCompte(postBody, user);
		} else {
			utilisateurOperateurKamtarService.updateUser(postBody, user);
		}
		
		actionAuditService.editerOperateurKamtar(user, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.OK);

	}

	/**
	 * Suppression d'un op??rateur kamtar
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Suppression d'un op??rateur kamtar")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv?? aux admins)"),
		    @ApiResponse(code = 404, message = "Impossible de trouver l'op??rateur"),
		    @ApiResponse(code = 200, message = "Op??rateur supprim??", response = Boolean.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/operateur_kamtar", 
			method = RequestMethod.DELETE)
	@CrossOrigin(origins="*")
	public ResponseEntity delete(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody DeleteOperateurKamtarParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de modifier un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.admin(jwtProvider, token)) {
			logger.warn("type de compte incorrect " + UtilisateurTypeDeCompte.ADMIN_KAMTAR + " " + jwtProvider.getClaims(token).get("type_de_compte"));
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");		
		}

		// chargement
		UtilisateurOperateurKamtar user = utilisateurOperateurKamtarService.getByUUID(postBody.getId(), jwtProvider.getCodePays(token));
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'op??rateur Kamtar.");
		}
		utilisateurOperateurKamtarService.delete(user, jwtProvider.getCodePays(token));

		
		actionAuditService.supprimerOperateurKamtar(user, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.OK);

	}
	
	/**
	 * R??cup??re les informations d'un op??rateur kamtar
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "R??cup??re les informations d'un op??rateur kamtar")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv?? aux admins ou op??rateur connect??)"),
		    @ApiResponse(code = 404, message = "Impossible de trouver l'op??rateur"),
		    @ApiResponse(code = 200, message = "Op??rateur demand??", response = UtilisateurOperateurKamtar.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/operateur_kamtar", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity get(
			@ApiParam(value = "UUID de l'op??rateur", required = true) @RequestParam("uuid") String uuid,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de cr??er un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.admin(jwtProvider, token) && (SecurityUtils.operateur(jwtProvider, token) && !jwtProvider.getUUIDFromJWT(token).toString().equals(uuid))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");		
		}

		// chargement
		UtilisateurOperateurKamtar user = utilisateurOperateurKamtarService.getByUUID(uuid, jwtProvider.getCodePays(token));
		if (user != null) {
			actionAuditService.getOperateurKamtar(user, token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.OK);
		}


		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'op??rateur Kamtar.");
	}


	/**
	 * Liste de tous les utilisateurs op??rateurs kamtar
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste de tous les utilisateurs op??rateurs kamtar")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv??s aux admins)"),
		    @ApiResponse(code = 200, message = "Liste des op??rateurs (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/operateurs_kamtar", 
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_offres(
			@ApiParam(value = "Crit??res de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de consulter les op??rateurs katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.admin(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");		
		}

		// param??tres du datatable
		String draw 		= postBody.getFirst("draw").toString();
		Integer length 		= Integer.valueOf(postBody.getFirst("length").toString());

		// tri, sens et num??ro de page
		String colonneTriDefaut = "createdOn";
		List<String> colonnesTriAutorise = Arrays.asList("createdOn", "prenom", "nom", "fonction", "numeroTelephone1", "email", "service");

		String order_column_bdd = DatatableUtils.getOrderColonne(colonneTriDefaut, colonnesTriAutorise, postBody);
		String sort_bdd = DatatableUtils.getSort(postBody);
		Integer numero_page = DatatableUtils.getNumeroPage(postBody, length);

		// filtrage
		List<String> colonnesFiltrageActive = Arrays.asList("prenom", "nom", "fonction", "numeroTelephone1", "email", "service");
		ParentSpecificationsBuilder builder = new UtilisateurOperateurKamtarSpecificationsBuilder();
		Specification spec = DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, null);
		if (spec == null) {
			spec = Specification.where(DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, null));
		}

		// filtrage par date de cr??ation
		spec = DatatableUtils.fitrageDate(spec, postBody, "createdOn", "createdOn");

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
		Page<UtilisateurOperateurKamtar> leads = utilisateurOperateurKamtarService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);
		Long total = utilisateurOperateurKamtarService.countAll(spec);
		
		
		actionAuditService.getOperateursKamtar(token);

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
	 * Export des utilisateurs op??rateurs kamtar
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Export des op??rateurs kamtar")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv??s aux admins)"),
			@ApiResponse(code = 200, message = "CSV des op??rateurs kamtar")
	})
	@RequestMapping(
			produces = "application/json",
			value = "/operateurs_kamtar/export",
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
		Page<UtilisateurOperateurKamtar> leads = utilisateurOperateurKamtarService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);

		// convertion liste en excel
		byte[] bytesArray = exportExcelService.export(leads, null);

		// audit
		actionAuditService.exportOperateursKamtar(token);

		return ResponseEntity.ok()
				//.headers(headers) // add headers if any
				.contentLength(bytesArray.length)
				.contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
				.body(bytesArray);
	}
	

	/**
	 * Liste des actions des op??rateurs kamtar
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste des actions des op??rateurs kamtar")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv?? aux admins et op??rateurs)"),
		    @ApiResponse(code = 200, message = "Liste des actions des op??rateurs (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/operateur_kamtar/actions", 
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_actions(
			@ApiParam(value = "Crit??res de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de consulter les admin katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.admin(jwtProvider, token) && (!SecurityUtils.operateur(jwtProvider, token))) {
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

		// filtrage par administrateur
		Specification spec = null;
		String uuid_operateur_kamtar_pas_final = null;
		if (postBody.containsKey("operateurKamtar")) {
			final String uuid_operateur_kamtar = postBody.getFirst("operateurKamtar").toString();
			uuid_operateur_kamtar_pas_final = uuid_operateur_kamtar;
			// https://stackoverflow.com/questions/35201604/how-to-create-specification-using-jpaspecificationexecutor-by-combining-tables
			spec = new Specification<ActionAudit>() {
				public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					List<Predicate> predicates = new ArrayList<Predicate>();
					predicates.add(builder.equal(root.get("uuidUtilisateur"), uuid_operateur_kamtar));
					return builder.and(predicates.toArray(new Predicate[predicates.size()]));
				}
			};
		}

		// filtrage par date de cr??ation
		spec = DatatableUtils.fitrageDate(spec, postBody, "createdOn", "createdOn");

		// si c'est un op??rateur, on filtre pa son compte
		if (SecurityUtils.operateur(jwtProvider, token)) {
			spec = new Specification<ActionAudit>() {
				public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					List<Predicate> predicates = new ArrayList<Predicate>();
					predicates.add(builder.equal(root.get("uuidUtilisateur"), jwtProvider.getUUIDFromJWT(token).toString()));
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

		
		actionAuditService.getActionAuditOperateurKamtar(token, uuid_operateur_kamtar_pas_final);

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
