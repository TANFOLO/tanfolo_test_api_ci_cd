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
import com.kamtar.transport.api.criteria.OperationSpecificationsBuilder;
import com.kamtar.transport.api.params.*;
import com.kamtar.transport.api.service.*;
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
import com.kamtar.transport.api.criteria.UtilisateurProprietaireSpecificationsBuilder;
import com.kamtar.transport.api.enums.OperateurListeDeDroits;
import com.kamtar.transport.api.enums.UtilisateurTypeDeCompte;
import com.kamtar.transport.api.model.ActionAudit;
import com.kamtar.transport.api.model.Client;
import com.kamtar.transport.api.model.Utilisateur;
import com.kamtar.transport.api.model.UtilisateurOperateurKamtar;
import com.kamtar.transport.api.model.UtilisateurProprietaire;
import com.kamtar.transport.api.security.SecurityUtils;
import com.kamtar.transport.api.swagger.ListClient;
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


@Api(value="Gestion des propri??taires des v??hicules", description="API Rest qui g??re les propri??taires des v??hicules")
@RestController
@EnableWebMvc
public class UtilisateurProprietaireController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(UtilisateurProprietaireController.class);  

	@Autowired
	CountryService countryService;

	@Autowired
	MapperJSONService mapperJSONService;

	@Autowired
	UtilisateurProprietaireService utilisateurProprietaireService;

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	ExportExcelService exportExcelService;

	@Autowired
	ActionAuditService actionAuditService;
	
	@Autowired
	DocumentsProprietaireService documentsProprietaireService;

	/**
	 * Cr??ation d'un propri??taire depuis le backoffice
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Cr??ation d'un propri??taire")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "L'adresse e-mail est d??j?? utilis??e."),
			@ApiResponse(code = 400, message = "Le num??ro de t??l??phone est d??j?? utilis??."),
			@ApiResponse(code = 400, message = "Impossible d'enregistrer le propri??taire."),
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv?? aux admins ou op??rateurs ayant droits)"),
		    @ApiResponse(code = 201, message = "Propri??taire cr????", response = UtilisateurProprietaire.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/proprietaire", 
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity create(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody CreateProprietaireParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de cr??er un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_PROPRIETAIRE)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");		
		}

		String pays = jwtProvider.getCodePays(token);

		// enregistrement
		UtilisateurProprietaire user = utilisateurProprietaireService.createUser(postBody, pays);
		if (user != null) {
			actionAuditService.creerProprietaire(user, token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.CREATED);
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible d'enregistrer le propri??taire.");

	}
	
	/**
	 * V??rification d'un propri??taire depuis la cr??ation d'un compte cot?? public
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "V??rification d'un propri??taire")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Le propi??taire poss??de d??j?? un compte, le mot de passe associ?? ?? ce compte est incorrect"),
			@ApiResponse(code = 400, message = "Le propi??taire poss??de d??j?? un compte"),
			@ApiResponse(code = 400, message = "Le code de parrainage saisi est invalide"),
		    @ApiResponse(code = 200, message = "Propri??taire valide")
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/proprietaire/verifications", 
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity verifications(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody CreateProprietairePublicParams postBody) {

		// est ce que l'adresseemail du propio existe d??j?? ?
		if (utilisateurProprietaireService.emailExist(postBody.getProprietaire_numero_telephone_1(), postBody.getPays())) {

			// si l'email existe d??j??, on regarde si le mot de passe saisie est celui du proprio
			UtilisateurProprietaire proprietaire = utilisateurProprietaireService.login(postBody.getProprietaire_numero_telephone_1(), postBody.getProprietaire_password(), postBody.getPays());
			if (proprietaire == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le propi??taire poss??de d??j?? un compte, le mot de passe associ?? ?? ce compte est incorrect");
			}

		}
		
		// est ce que le t??l??phone du propio existe d??j?? ?
		if (utilisateurProprietaireService.numeroDeTelephoneExist(postBody.getProprietaire_email(), postBody.getPays())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le propi??taire poss??de d??j?? un compte");
		}

		// si le code saisi est invalide, on le dit
		if (postBody.getProprietaire_codeParrainage() != null && !"".equals(postBody.getProprietaire_codeParrainage())) {
			if (!utilisateurProprietaireService.codeParrainageExist(postBody.getProprietaire_codeParrainage(), postBody.getPays())) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le code de parrainage saisi est invalide");
			}
		}

		if ("B".equals(postBody.getType_compte()) && (postBody.getEntreprise_nom() == null || "".equals(postBody.getEntreprise_nom().trim()))) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous devez renseigner le nom de la soci??t??");
		}

		return new ResponseEntity<>(true, HttpStatus.OK);

	}


	/**
	 * Modification d'un propri??taire par un admin
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Modification d'un propri??taire")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv?? aux admins ou op??rateur ayant droit)"),
		    @ApiResponse(code = 404, message = "Impossible de trouver le propri??taire"),
		    @ApiResponse(code = 200, message = "Propri??taire modifi??", response = UtilisateurProprietaire.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/proprietaire", 
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity edit(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody EditProprietaireParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de modifier un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_PROPRIETAIRE)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");		
		}

		// chargement
		UtilisateurProprietaire user = utilisateurProprietaireService.getByUUID(postBody.getId(), jwtProvider.getCodePays(token));
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le propri??taire.");
		}
		utilisateurProprietaireService.updateUser(postBody, user);

		
		actionAuditService.editerProprietaire(user, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.OK);

	}


	/**
	 * Modification d'un propri??taire par un propri??taure
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Modification d'un propri??taire par un propri??taure")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv?? aux admins ou op??rateur ayant droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver le propri??taire"),
			@ApiResponse(code = 200, message = "Propri??taire modifi??", response = UtilisateurProprietaire.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/proprietaire2",
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity edit2(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody EditProprietairePublicParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de modifier un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.proprietaire(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		// chargement
		UtilisateurProprietaire user = utilisateurProprietaireService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), jwtProvider.getCodePays(token));
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le propri??taire.");
		}
		utilisateurProprietaireService.updateUser(postBody, user);


		actionAuditService.editerProprietaire(user, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.OK);

	}

	/**
	 * Suppression d'un propri??taire
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Suppression d'un propri??taire")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Le propri??taire est attach?? ?? au moins un appel d'offre."),
			@ApiResponse(code = 400, message = "Le propri??taire est attach?? ?? au moins un v??hicule."),
			@ApiResponse(code = 400, message = "Le propri??taire est attach?? ?? au moins une facture."),
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv?? aux admins ou propri??taire ayant droit)"),
		    @ApiResponse(code = 404, message = "Impossible de trouver le propri??taire"),
		    @ApiResponse(code = 200, message = "Propri??taire supprim??", response = Boolean.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/proprietaire", 
			method = RequestMethod.DELETE)
	@CrossOrigin(origins="*")
	public ResponseEntity delete(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody DeleteProprietaireParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de modifier un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_PROPRIETAIRE) || !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.SUPPRESSION_PROPRIETAIRE)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		// chargement
		UtilisateurProprietaire user = utilisateurProprietaireService.getByUUID(postBody.getId(), jwtProvider.getCodePays(token));
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le propri??taire.");
		}
		utilisateurProprietaireService.delete(user, jwtProvider.getCodePays(token));

		
		actionAuditService.supprimerProprietaire(user, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.OK);

	}
	
	/**
	 * R??cup??re les informations d'un propri??taire
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "R??cup??re les informations d'un propri??taire")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv?? aux admins, propri??taires ou op??rateurs ayant droit)"),
		    @ApiResponse(code = 404, message = "Impossible de trouver le propri??taire"),
		    @ApiResponse(code = 200, message = "Propri??taire demand??", response = UtilisateurProprietaire.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/proprietaire", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity get(
			@ApiParam(value = "UUID du propri??taire", required = true) @RequestParam("uuid") String uuid,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de cr??er un op??rateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.admin(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_PROPRIETAIRE) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_PROPRIETAIRE) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_VEHICULE)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		// chargement
		UtilisateurProprietaire user = null;
		if (SecurityUtils.proprietaire(jwtProvider, token)) {
			UUID uuid_jeton = jwtProvider.getUUIDFromJWT(token);
			user = utilisateurProprietaireService.getByUUID(uuid_jeton.toString(), jwtProvider.getCodePays(token));
		} else if (SecurityUtils.admin(jwtProvider, token) ||  UtilisateurTypeDeCompte.OPERATEUR_KAMTAR.toString().equals(jwtProvider.getTypeDeCompte(token))) {
			user = utilisateurProprietaireService.getByUUID(uuid, jwtProvider.getCodePays(token));
		}
		if (user != null) {
			actionAuditService.getProprietaire(user, token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.OK);
		}

		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le propri??taire.");
	}


	/**
	 * Liste de tous les propri??taires
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste de tous les propri??taires")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv??s aux admins et op??rateurs ayant droits)"),
		    @ApiResponse(code = 200, message = "Liste des propri??taires (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
		})
	@RequestMapping(
			produces = "application/json",
			value ="/proprietaires",
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_offres(
			@ApiParam(value = "Crit??res de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de consulter les op??rateurs katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_PROPRIETAIRE) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_PROPRIETAIRE)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");		
		}

		// param??tres du datatable
		String draw 		= postBody.getFirst("draw").toString();
		Integer length 		= Integer.valueOf(postBody.getFirst("length").toString());

		// tri, sens et num??ro de page
		String colonneTriDefaut = "createdOn";
		List<String> colonnesTriAutorise = Arrays.asList("createdOn", "nom", "numeroTelephone1", "email", "prenom");

		String order_column_bdd = DatatableUtils.getOrderColonne(colonneTriDefaut, colonnesTriAutorise, postBody);
		String sort_bdd = DatatableUtils.getSort(postBody);
		Integer numero_page = DatatableUtils.getNumeroPage(postBody, length);

		// filtrage
		List<String> colonnesFiltrageActive = Arrays.asList("nom", "numeroTelephone1", "email", "prenom");
		ParentSpecificationsBuilder builder = new UtilisateurProprietaireSpecificationsBuilder();
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
		Page<UtilisateurProprietaire> leads = utilisateurProprietaireService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);
		Long total = utilisateurProprietaireService.countAll(spec);
		
		
		actionAuditService.getProprietaires(token);

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
	 * Liste de tous les propri??taires
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste de tous les propri??taires")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv??s aux admins et op??rateurs ayant droits)"),
			@ApiResponse(code = 200, message = "Liste des propri??taires", response = ListProprietaire.class)
	})
	@RequestMapping(
			produces = "application/json",
			value ="/proprietaires/liste",
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_offres(
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que les droits lui permettent de consulter les op??rateurs katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_PROPRIETAIRE) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_PROPRIETAIRE)) {
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
		Page<UtilisateurProprietaire> leads = utilisateurProprietaireService.getAllPagined(order_column_bdd, sort_bdd, 0, length, spec_general);

		actionAuditService.getProprietaires(token);

		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(leads.getContent()), HttpStatus.OK);
	}



	/**
	 * Export des propri??taires
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Export des clients propri??taires")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv??s aux admins et op??rateurs ayant droit)"),
			@ApiResponse(code = 200, message = "CSV des propri??taires")
	})
	@RequestMapping(
			produces = "application/json",
			value = "/proprietaires/export",
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
		Page<UtilisateurProprietaire> leads = utilisateurProprietaireService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);

		// convertion liste en excel
		byte[] bytesArray = exportExcelService.export(leads, null);

		// audit
		actionAuditService.exportProprietaires(token);

		return ResponseEntity.ok()
				//.headers(headers) // add headers if any
				.contentLength(bytesArray.length)
				.contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
				.body(bytesArray);
	}



	/**
	 * Est ce que l'email est d??j?? utilis?? sur un compte propri??taire ?
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Est ce que l'email est d??j?? utilis?? sur un compte propri??taire ?")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "true si l'email est d??j?? utilis??, false sinon", response = Boolean.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/proprietaire/telephone",
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity check_email(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody TelephoneParams postBody) throws JsonProcessingException {

		boolean exist = false;
		if (postBody.getTelephone() != null && !"".equals(postBody.getTelephone().trim())) {
			exist = utilisateurProprietaireService.numeroDeTelephoneExist(postBody.getTelephone(), postBody.getPays());
		}
		return new ResponseEntity<>(mapperJSONService.get(null).writeValueAsString(exist), HttpStatus.OK);

	}

	/**
	 * Liste des actions des propri??taires
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste des actions des propri??taires")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv?? aux admins)"),
		    @ApiResponse(code = 200, message = "Liste des actions des propri??taires (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/proprietaires/actions", 
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
		Specification spec = null;
		String uuid_proprietaire_pas_final = null;
		if (postBody.containsKey("proprietaire")) {
			final String uuid_proprietaire = postBody.getFirst("proprietaire").toString();
			uuid_proprietaire_pas_final = uuid_proprietaire;

			// https://stackoverflow.com/questions/35201604/how-to-create-specification-using-jpaspecificationexecutor-by-combining-tables
			spec = new Specification<ActionAudit>() {
				public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					List<Predicate> predicates = new ArrayList<Predicate>();
					predicates.add(builder.equal(root.get("uuidProprietaire"), uuid_proprietaire));
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

		
		actionAuditService.getActionAuditProprietaire(token, uuid_proprietaire_pas_final);

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
	 * Autocompletion des propri??taires
	 * @param postBody
	 * @return
	 */
	/*@RequestLogger(name = Application.microservice)*/
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Autocompletion des propri??taires")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (admin, ou op??rateur ayant le droit)"),
		    @ApiResponse(code = 200, message = "Liste des propri??taires", response = ListProprietaire.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/proprietaires/autocompletion", 
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity autocompletion(
			@ApiParam(value = "Partie du nom du propri??taire ?? rechercher", required = true) @RequestParam("query") String query, 
			@ApiParam(value = "Jeton JWT pour autentification", required = true) @RequestParam("token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// v??rifie que les droits lui permettent de lire les exp??diteurs
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_PROPRIETAIRE) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_VEHICULE)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		List<UtilisateurProprietaire> proprietaires = utilisateurProprietaireService.autocomplete(query, jwtProvider.getCodePays(token));

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(proprietaires), HttpStatus.OK);

	}

	
	/**
	 * R??cup??re le fichier image de la carte de transport du propri??taire
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "R??cup??re le fichier image de la carte de transport du propri??taire")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
			@ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (admin, ou op??rateur ayant le droit)"),
			@ApiResponse(code = 200, message = "Fichier image", response = byte[].class)
	})
	@RequestMapping(
			produces = MediaType.IMAGE_PNG_VALUE,
			value = "/proprietaire/carte_transport", 
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public byte[]getSignaturedeExpediteur(
			@ApiParam(value = "UUID du propri??taire", required = true) @RequestParam("uuid") String uuid, 
			@ApiParam(value = "Jeton JWT pour autentification", required = true) @RequestParam("token") String token) {

		// v??rifie que le jeton est valide
		if (!jwtProvider.isValidJWT(token)) {
			return null;
		}

		// v??rifie que les droits lui permettent de afficher une commande
		if (!SecurityUtils.admin(jwtProvider, token) && !SecurityUtils.operateur(jwtProvider, token) && !SecurityUtils.proprietaire(jwtProvider, token)) {
			return null;
		}

		UtilisateurProprietaire proprietaire = null;
		if (SecurityUtils.proprietaire(jwtProvider, token)) {
			proprietaire = utilisateurProprietaireService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), jwtProvider.getCodePays(token));
		} else {
			proprietaire = utilisateurProprietaireService.getByUUID(uuid, jwtProvider.getCodePays(token));
		}



		if (proprietaire != null) {

			byte[] signature_image = documentsProprietaireService.get(proprietaire.getPhotoCarteTransport());
			return signature_image;
		}
		return null;

	}

	

}
