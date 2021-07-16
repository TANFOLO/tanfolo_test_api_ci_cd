package com.kamtar.transport.api.controller;

import java.util.*;


import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.model.Client;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.criteria.ParentSpecificationsBuilder;
import com.kamtar.transport.api.enums.OperateurListeDeDroits;
import com.kamtar.transport.api.model.SMS;
import com.kamtar.transport.api.security.SecurityUtils;
import com.kamtar.transport.api.service.ActionAuditService;
import com.kamtar.transport.api.service.SMSService;
import com.kamtar.transport.api.swagger.MapDatatable;
import com.kamtar.transport.api.utils.DatatableUtils;
import com.kamtar.transport.api.utils.JWTProvider;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Api(value="Gestion des SMS envoyés", description="API Rest qui gère les SMS envoyés")
@RestController
@EnableWebMvc
public class SMSController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(SMSController.class);  

	@Autowired
	SMSService smsService;

	@Autowired
	MapperJSONService mapperJSONService;
	
	@Autowired
	ActionAuditService actionAuditService;

	@Autowired
	JWTProvider jwtProvider;
	
	/**
	 * Récupère la liste des SMS
	 * @param uuid
	 * @param token
	 * @return
	 */
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère la liste des SMS")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
		    @ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
		    @ApiResponse(code = 200, message = "Liste de SMS (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/liste_sms", 
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity get_liste_sms(
			@ApiParam(value = "Critères de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de modifier un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une commande
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_SMS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}
		
		// paramètres du datatable
		String draw 		= postBody.getFirst("draw").toString();
		Integer length 		= Integer.valueOf(postBody.getFirst("length").toString());

		// tri, sens et numéro de page
		String colonneTriDefaut = "createdAt";
		List<String> colonnesTriAutorise = Arrays.asList("createdAt");

		String order_column_bdd = DatatableUtils.getOrderColonne(colonneTriDefaut, colonnesTriAutorise, postBody);
		String sort_bdd = DatatableUtils.getSort(postBody);
		Integer numero_page = DatatableUtils.getNumeroPage(postBody, length);
		
		// filtrage
		List<String> colonnesFiltrageActive = Arrays.asList("to");
		String destinataire = DatatableUtils.getFiltrageValeur(colonnesFiltrageActive, postBody);
		String code_pays = jwtProvider.getCodePays(token);

		// filtre sur le statut du SMS (0 = tous, 1 = envoyés, 2 = reçus, 3 = non envoyés)
		Integer statut = new Integer(0);
		Map<Integer, String> indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		Integer position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, "statut");
		if (position_colonne != null) {
			String filtre_par_statut = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			if (filtre_par_statut != null && !"".equals(filtre_par_statut.trim())) {
				statut = Integer.valueOf(filtre_par_statut);
			}
		}

		// préparation les deux requêtes (résultat et comptage)
		Map<String, Object> liste_sms = smsService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, destinataire, code_pays, statut);
		if (liste_sms == null) {
			throw new ResponseStatusException(HttpStatus.resolve(500), "Impossible de charger les SMS");

		}
		liste_sms.put("draw", draw);
		
		actionAuditService.getListeSMS(token);

		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(liste_sms), HttpStatus.OK);

	}
	


}
