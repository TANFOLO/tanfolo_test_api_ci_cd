package com.kamtar.transport.api.controller;

import java.util.*;


import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.service.MapperJSONService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.criteria.NotificationSpecificationsBuilder;
import com.kamtar.transport.api.criteria.ParentSpecificationsBuilder;
import com.kamtar.transport.api.enums.NotificationType;
import com.kamtar.transport.api.enums.OperateurListeDeDroits;
import com.kamtar.transport.api.model.MotDePassePerdu;
import com.kamtar.transport.api.model.Notification;
import com.kamtar.transport.api.model.UtilisateurClient;
import com.kamtar.transport.api.model.UtilisateurOperateurKamtar;
import com.kamtar.transport.api.model.UtilisateurDriver;
import com.kamtar.transport.api.params.MotDePassePerduParams;
import com.kamtar.transport.api.params.TraiterNotificationParams;
import com.kamtar.transport.api.security.SecurityUtils;
import com.kamtar.transport.api.service.ActionAuditService;
import com.kamtar.transport.api.service.NotificationService;
import com.kamtar.transport.api.swagger.MapDatatable;
import com.kamtar.transport.api.utils.DatatableUtils;
import com.kamtar.transport.api.utils.JWTProvider;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Api(value="Gestion des notifications du backoffice", description="API Rest qui gère les notifications du backoffice")
@RestController
@EnableWebMvc
public class NotificationController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(NotificationController.class);  

	@Autowired
	NotificationService notificationService;

	@Autowired
	ActionAuditService actionAuditService;

	@Autowired
	MapperJSONService mapperJSONService;

	@Autowired
	JWTProvider jwtProvider;

	/**
	 * Récupére le nombre de notifications du backoffice
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupére le nombre de notifications du backoffice")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
		    @ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
		    @ApiResponse(code = 200, message = "Nombre de notifications", response = Long.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/notifications/backoffice/compter", 
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity<Object> getCountries(
			@ApiParam(value = "Jeton JWT pour autentification", required = true) @RequestParam("token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de lire les expéditeurs
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_NOTIFICATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(notificationService.nbNotificationsNonLues(NotificationType.WEB_BACKOFFICE, jwtProvider.getCodePays(token))), HttpStatus.OK);
	}


	/**
	 * Liste de toutes les notifications du backoffice
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste de toutes les notifications du backoffice")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
		    @ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
		    @ApiResponse(code = 200, message = "Liste des notifications (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/notifications/backoffice", 
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_offres(
			@ApiParam(value = "Critères de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de consulter les opérateurs katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de lire les expéditeurs
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_NOTIFICATIONS)) {
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

		// filtrage
		List<String> colonnesFiltrageActive = Arrays.asList();
		ParentSpecificationsBuilder builder = new NotificationSpecificationsBuilder();
		Specification spec = DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, null);
		if (spec == null) {
			spec = Specification.where(DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, null));
		}

		// filtrage par date de création
		spec = DatatableUtils.fitrageDate(spec, postBody, "createdOn", "createdOn");

		// filtrage par pays kamtar
		Specification spec_pays = new Specification<Notification>() {
			public Predicate toPredicate(Root<Notification> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get("codePays"), jwtProvider.getCodePays(token)));
				return builder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
		spec = spec.and(spec_pays);



		// préparation les deux requêtes (résultat et comptage)
		Page<Notification> leads = notificationService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);
		Long total = notificationService.countAll(spec);

		
		actionAuditService.getNotificationsBackoffice(token);

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

	/**
	 * Traitement d'une notification
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Traitement d'une notification")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "Impossible de trouver l'opération"),
			@ApiResponse(code = 404, message = "Impossible de trouver le véhicule"),
			@ApiResponse(code = 404, message = "Impossible de trouver le driver principal du véhicule"),
			@ApiResponse(code = 404, message = "Impossible de trouver le propriétaire du véhicule"),
			@ApiResponse(code = 400, message = "La notification a déjà été traitée."),
		    @ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
		    @ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
		    @ApiResponse(code = 404, message = "Impossible de trouver la notification"),
		    @ApiResponse(code = 200, message = "Notification mise à jour", response = Notification.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/notification/traiter", 
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity contact(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody TraiterNotificationParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {
		
		// vérifie que les droits lui permettent de lire les expéditeurs
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_NOTIFICATIONS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		Notification notification = notificationService.getByUUID(postBody.getNotification(), jwtProvider.getCodePays(token));
		if (notification == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver la notification.");
		}
		notification = notificationService.traiter(notification, token);
 
		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(notification), HttpStatus.OK);


	}


}
