package com.kamtar.transport.api.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.opendevl.JFlat;
import com.google.gson.Gson;
import com.kamtar.transport.api.model.Client;
import com.kamtar.transport.api.service.*;
import com.kamtar.transport.api.utils.ExportUtils;
import org.apache.commons.io.FileUtils;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.kamtar.transport.api.criteria.PredicateUtils;
import com.kamtar.transport.api.params.ExportDateRangeParams;
import com.kamtar.transport.api.swagger.ListUtilisateurAdminKamtar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
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
import com.kamtar.transport.api.criteria.ActionAuditSpecificationsBuilder;
import com.kamtar.transport.api.criteria.ParentSpecificationsBuilder;
import com.kamtar.transport.api.criteria.UtilisateurAdminKamtarSpecificationsBuilder;
import com.kamtar.transport.api.enums.UtilisateurTypeDeCompte;
import com.kamtar.transport.api.model.ActionAudit;
import com.kamtar.transport.api.model.UtilisateurAdminKamtar;
import com.kamtar.transport.api.params.CreateAdminKamtarParams;
import com.kamtar.transport.api.params.DeleteAdminKamtarParams;
import com.kamtar.transport.api.params.EditAdminKamtarParams;
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


@Api(value="Gestion des administrateurs", description="API Rest qui g??re les administrateurs")
@RestController
@EnableWebMvc
public class UtilisateurAdminKamtarController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(UtilisateurAdminKamtarController.class);  

	@Autowired
	CountryService countryService;

	@Autowired
	MapperJSONService mapperJSONService;

	@Autowired
	ExportExcelService exportExcelService;

	@Autowired
	UtilisateurAdminKamtarService utilisateurAdminKamtarService;

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	ActionAuditService actionAuditService;

	/**
	 * Cr??ation d'un admin kamtar
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Cr??ation d'un admin kamtar")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "L'adresse e-mail est d??j?? utilis??e."),
			@ApiResponse(code = 400, message = "Le num??ro de t??l??phone est d??j?? utilis??."),
			@ApiResponse(code = 400, message = "Impossible d'enregistrer l'admin Kamtar."),
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv?? aux admin)"),
		    @ApiResponse(code = 201, message = "Administrateur cr????", response = UtilisateurAdminKamtar.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/admin_kamtar", 
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity create(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody CreateAdminKamtarParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de cr??er un admin kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.admin(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		// enregistrement
		UtilisateurAdminKamtar user = utilisateurAdminKamtarService.createUser(postBody, jwtProvider.getCodePays(token));
		if (user != null) {
			actionAuditService.creerAdminKamtar(user, token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.CREATED);
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible d'enregistrer l'admin Kamtar.");

	}

	/**
	 * Modification d'un admin kamtar
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Modification d'un admin kamtar")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv?? aux admins)"),
		    @ApiResponse(code = 404, message = "Impossible de trouver l'admin"),
		    @ApiResponse(code = 200, message = "Administrateur mis ?? jour", response = UtilisateurAdminKamtar.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/admin_kamtar", 
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity edit(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody EditAdminKamtarParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de cr??er un admin kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.admin(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");		
		}

		// chargement
		UtilisateurAdminKamtar user = utilisateurAdminKamtarService.getByUUID(postBody.getId(), jwtProvider.getCodePays(token));
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'admin Kamtar.");
		}
		utilisateurAdminKamtarService.updateUser(postBody, user);

		
		actionAuditService.editerAdminKamtar(user, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.OK);

	}
	
	/**
	 * Suppression d'un admin kamtar
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Suppression d'un admin kamtar")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv?? aux admins)"),
		    @ApiResponse(code = 404, message = "Impossible de trouver l'admin"),
		    @ApiResponse(code = 200, message = "Administrateur supprim??", response = Boolean.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/admin_kamtar", 
			method = RequestMethod.DELETE)
	@CrossOrigin(origins="*")
	public ResponseEntity delete(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody DeleteAdminKamtarParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de cr??er un admin kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.admin(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");		
		}

		// chargement
		UtilisateurAdminKamtar user = utilisateurAdminKamtarService.getByUUID(postBody.getId(), jwtProvider.getCodePays(token));
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'admin Kamtar.");
		}
		utilisateurAdminKamtarService.deleteUser(user);

		
		actionAuditService.supprimerAdminKamtar(user, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.OK);

	}

	/**
	 * R??cup??re les informations d'un admin kamtar
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "R??cup??re les informations d'un admin kamtar")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv??s aux admins)"),
		    @ApiResponse(code = 404, message = "Impossible de trouver l'admin"),
		    @ApiResponse(code = 200, message = "Administrateur demand??", response = UtilisateurAdminKamtar.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/admin_kamtar", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity get(
			@ApiParam(value = "UUID de l'admin", required = true) @RequestParam("uuid") String uuid,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de cr??er un admin kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.admin(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");		
		}

		// chargement
		UtilisateurAdminKamtar user = utilisateurAdminKamtarService.getByUUID(uuid, jwtProvider.getCodePays(token));
		if (user != null) {
			actionAuditService.getAdminKamtar(user, token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.OK);
		}

		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'admin Kamtar.");
	}


	/**
	 * Liste de tous les utilisateurs admin kamtar
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste de tous les utilisateurs admin kamtar")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv??s aux admins)"),
		    @ApiResponse(code = 200, message = "Liste des administrateurs", response = ListUtilisateurAdminKamtar.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/admins_kamtar", 
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_offres(
			@ApiParam(value = "Crit??res de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de consulter les admin katmars
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
		List<String> colonnesTriAutorise = Arrays.asList("createdOn", "prenom", "nom", "email", "numeroTelephone1");

		String order_column_bdd = DatatableUtils.getOrderColonne(colonneTriDefaut, colonnesTriAutorise, postBody);
		String sort_bdd = DatatableUtils.getSort(postBody);
		Integer numero_page = DatatableUtils.getNumeroPage(postBody, length);

		// filtrage
		List<String> colonnesFiltrageActive = Arrays.asList("nom", "prenom", "email", "numeroTelephone1");
		ParentSpecificationsBuilder builder = new UtilisateurAdminKamtarSpecificationsBuilder();
		Specification spec = DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, null);
		if (spec == null) {
			logger.info("spec == null");
			spec = Specification.where(DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, null));
		} else {
			logger.info("spec != null");
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
		Page<UtilisateurAdminKamtar> leads = utilisateurAdminKamtarService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);
		Long total = utilisateurAdminKamtarService.countAll(spec);

		
		actionAuditService.getAdminsKamtar(token);

		// pr??pare les r??sultast
		org.json.simple.JSONArray jsonArrayOffres = new org.json.simple.JSONArray();
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
	 * Export des utilisateurs admin kamtar
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Export des utilisateurs admin kamtar")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv??s aux admins)"),
			@ApiResponse(code = 200, message = "CSV des administrateurs")
	})
	@RequestMapping(
			produces = "application/json",
			value = "/admins_kamtar/export",
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
		Page<UtilisateurAdminKamtar> leads = utilisateurAdminKamtarService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);

		// convertion liste en excel
		byte[] bytesArray = exportExcelService.export(leads, null);

		// audit
		actionAuditService.exportAdminsKamtar(token);

		return ResponseEntity.ok()
				//.headers(headers) // add headers if any
				.contentLength(bytesArray.length)
				.contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
				.body(bytesArray);
	}



	/**
	 * Liste des actions des admins kamtar
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste des actions des admins kamtar")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv??s aux admins)"),
		    @ApiResponse(code = 200, message = "Liste des actions des administrateurs (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/admin_kamtar/actions", 
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_actions(
			@ApiParam(value = "Crit??res de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de consulter les admin katmars
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
		List<String> colonnesTriAutorise = Arrays.asList("createdOn");

		String order_column_bdd = DatatableUtils.getOrderColonne(colonneTriDefaut, colonnesTriAutorise, postBody);
		String sort_bdd = DatatableUtils.getSort(postBody);
		Integer numero_page = DatatableUtils.getNumeroPage(postBody, length);


		// filtrage par administrateur
		String uuid_admin_kamtar_pas_final = null;
		Specification spec = null;
		if (postBody.containsKey("adminKamtar")) {
			final String uuid_admin_kamtar = postBody.getFirst("adminKamtar").toString();
			uuid_admin_kamtar_pas_final = uuid_admin_kamtar;

			// https://stackoverflow.com/questions/35201604/how-to-create-specification-using-jpaspecificationexecutor-by-combining-tables
			spec = new Specification<ActionAudit>() {
				public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					List<Predicate> predicates = new ArrayList<Predicate>();
					predicates.add(builder.equal(root.get("uuidUtilisateur"), uuid_admin_kamtar));
					return builder.and(predicates.toArray(new Predicate[predicates.size()]));
				}
			};
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
		Page<ActionAudit> leads = actionAuditService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);
		Long total = actionAuditService.countAll(spec);

		
		actionAuditService.getActionAuditAdministrateurKamtar(token, uuid_admin_kamtar_pas_final);

		// pr??pare les r??sultast
		org.json.simple.JSONArray jsonArrayOffres = new org.json.simple.JSONArray();
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
