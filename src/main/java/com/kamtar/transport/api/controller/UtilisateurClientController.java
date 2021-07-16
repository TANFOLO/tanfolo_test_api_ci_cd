package com.kamtar.transport.api.controller;

import java.util.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.enums.OperateurListeDeDroits;
import com.kamtar.transport.api.model.UtilisateurClientPersonnel;
import com.kamtar.transport.api.params.EmailExistantParams;
import com.kamtar.transport.api.params.TelephoneExistantParams;
import com.kamtar.transport.api.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.model.ActionAudit;
import com.kamtar.transport.api.model.Client;
import com.kamtar.transport.api.model.UtilisateurClient;
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


@Api(value="Gestion des expéditeurs", description="API Rest qui gère les expéditeurs")
@RestController
@EnableWebMvc
public class UtilisateurClientController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(UtilisateurClientController.class);  

	@Autowired
	CountryService countryService;

	@Autowired
	MapperJSONService mapperJSONService;

	@Autowired
	UtilisateurClientService utilisateurClientService;

	@Autowired
	UtilisateurClientPersonnelService utilisateurClientPersonnelService;

	@Autowired
	ClientService clientService;
	
	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	ActionAuditService actionAuditService;


	/**
	 * Est ce l'email passé en paramètre est attaché à un compte client ?
	 * @param postBody
	 * @return
	 */
	/*@RequestLogger(name = Application.microservice)*/
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Est ce l'email passé en paramètre est attaché à un compte client ?")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "true si l'email est attaché à un compte client, false sinon", response = Boolean.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/client/email/existant",
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity email_existant(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody EmailExistantParams postBody) throws JsonProcessingException {

		String email = "";
		if (postBody.getEmail() != null) {
			email = postBody.getEmail().trim();
		}
		boolean email_exist1 = utilisateurClientService.emailExist(email, postBody.getPays());
		boolean email_exist2 = clientService.emailExist(email, postBody.getPays());
		boolean email_exist3 = utilisateurClientPersonnelService.emailExist(email, postBody.getPays());

		return new ResponseEntity<>(mapperJSONService.get(null).writeValueAsString(email_exist2 || email_exist1 || email_exist3), HttpStatus.OK);

	}

	/**
	 * Est ce le numéro de téléphone passé en paramètre est attaché à un compte client ?
	 * @param postBody
	 * @return
	 */
	/*@RequestLogger(name = Application.microservice)*/
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Est ce le numéro de téléphone passé en paramètre est attaché à un compte client ?")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "true si le numéro de téléphone est attaché à un compte client, false sinon", response = Boolean.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/client/telephone/existant",
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity telephone_existant(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody TelephoneExistantParams postBody) throws JsonProcessingException {

		String telephone = "";
		if (postBody.getTelephone() != null) {
			telephone = postBody.getTelephone().trim().replaceAll(" ", "").replaceAll("-", "").replaceAll("\\.", "");
		}
		logger.info("telephone=" + telephone);

		boolean tel_exist1 = utilisateurClientService.numeroDeTelephoneExist(telephone, postBody.getPays());
		boolean tel_exist2 = clientService.numeroDeTelephoneExist(telephone, postBody.getPays());
		boolean tel_exist3 = utilisateurClientPersonnelService.numeroDeTelephoneExist(telephone, postBody.getPays());

		return new ResponseEntity<>(mapperJSONService.get(null).writeValueAsString(tel_exist2 || tel_exist1 || tel_exist3), HttpStatus.OK);

	}


	/**
	 * Connexion d'un client
	 * @param postBody
	 * @return
	 */
	/*@RequestLogger(name = Application.microservice)*/
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Connexion d'un client")
	@ApiResponses(value = {
		    @ApiResponse(code = 403, message = "Veuillez vérifier vos identifiants"),
		    @ApiResponse(code = 403, message = "Votre compte est désactivé"),
			@ApiResponse(code = 403, message = "L'utilisateur n'est attaché à aucun expéditeur"),
		    @ApiResponse(code = 200, message = "Token d'autentification", response = JSONObject.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/client/signin", 
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity login(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody SigninParams postBody) throws JsonProcessingException {

		UtilisateurClient utilisateur = utilisateurClientService.login(postBody.getLogin(), postBody.getMot_de_passe(), postBody.getPays());
		UtilisateurClientPersonnel utilisateur_personnel = utilisateurClientPersonnelService.login(postBody.getLogin(), postBody.getMot_de_passe(), postBody.getPays());
		if (utilisateur == null && utilisateur_personnel == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Veuillez vérifier vos identifiants");
		} 
		
		// est ce que le compte est désactivé
		if ((utilisateur != null && utilisateur.isActivate() == false) || (utilisateur_personnel != null && utilisateur_personnel.isActivate() == false)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Votre compte est désactivé");
		}

		Client client = null;
		List<String> additional_informations = null;
		String token = null;
		if (utilisateur != null) {
			actionAuditService.loginClient((UtilisateurClient)utilisateur, postBody.getPays());
			client = clientService.getByUtilisateur(utilisateur, postBody.getPays());
			additional_informations = Arrays.asList(new String[]{utilisateur.getNom(), client.getUuid().toString(), client.getTypeCompte(), client.getNom()});
			token = jwtProvider.createJWT(utilisateur, additional_informations);
		} else if (utilisateur_personnel != null) {
			actionAuditService.loginClientPersonnel((UtilisateurClientPersonnel)utilisateur_personnel, postBody.getPays());
			client = utilisateur_personnel.getClient();
			additional_informations = Arrays.asList(new String[]{utilisateur_personnel.getNom(), client.getUuid().toString(), client.getTypeCompte(), client.getNom(), utilisateur_personnel.getListe_droits()});
			token = jwtProvider.createJWT(utilisateur_personnel, additional_informations);
		}
		if ((client == null)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "L'utilisateur n'est attaché à aucun expéditeur");
		}


		JSONObject res = new JSONObject();
		res.put("token", token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(res), HttpStatus.OK);

	}
	
	/**
	 * Liste des actions d'un expéditeur
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste des actions d'un expéditeur")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
		    @ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (reservé aux admins ou opérateurs ayant droits)"),
		    @ApiResponse(code = 200, message = "Liste des actions d'un expéditeur (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/client/actions", 
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_actions(
			@ApiParam(value = "Critères de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de consulter les admin katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_CLIENTS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");		
		}

		// paramètres du datatable
		String draw 		= postBody.getFirst("draw").toString();
		Integer length 		= Integer.valueOf(postBody.getFirst("length").toString());

		// tri, sens et numéro de page
		String colonneTriDefaut = "createdOn";
		List<String> colonnesTriAutorise = Arrays.asList("createdOn");

		String order_column_bdd = DatatableUtils.getOrderColonne(colonneTriDefaut, colonnesTriAutorise, postBody);
		String sort_bdd = DatatableUtils.getSort(postBody);
		Integer numero_page = DatatableUtils.getNumeroPage(postBody, length);

		// filtrage par expéditeur
		Specification spec = null;
		String nom_client = "";
		String uuid_client = null;
		if (postBody.containsKey("client")) {
			uuid_client = postBody.getFirst("client").toString();

			UUID expediteur_uuid = UUID.fromString(uuid_client);
			Client expediteur = clientService.getByUUID(expediteur_uuid.toString(), jwtProvider.getCodePays(token));
			if (expediteur != null) {
				uuid_client = expediteur.getUtilisateur().getUuid().toString();
				nom_client = expediteur.getNom();
				final String uuid_client2 = uuid_client;

				// https://stackoverflow.com/questions/35201604/how-to-create-specification-using-jpaspecificationexecutor-by-combining-tables
				spec = new Specification<ActionAudit>() {
					public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						predicates.add(builder.equal(root.get("uuidUtilisateur"), uuid_client2));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
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
		Page<ActionAudit> leads = actionAuditService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);
		Long total = actionAuditService.countAll(spec);

		
		actionAuditService.getActionAuditClient(token, nom_client);

		// prépare les résultast
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
