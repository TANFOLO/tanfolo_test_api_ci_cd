package com.kamtar.transport.api.controller;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.criteria.ParentSpecificationsBuilder;
import com.kamtar.transport.api.criteria.UtilisateurOperateurKamtarSpecificationsBuilder;
import com.kamtar.transport.api.model.UtilisateurOperateurKamtar;
import com.kamtar.transport.api.service.MapperJSONService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.enums.OperateurListeDeDroits;
import com.kamtar.transport.api.model.SMS;
import com.kamtar.transport.api.security.SecurityUtils;
import com.kamtar.transport.api.service.ActionAuditService;
import com.kamtar.transport.api.service.EmailService;
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

@Api(value="Gestion des emails", description="API Rest qui gère l'ensemble des emails envoyés")
@RestController
@EnableWebMvc
public class EmailController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(EmailController.class);  

	@Autowired
	EmailService emailService;
	
	@Autowired
	ActionAuditService actionAuditService;

	@Autowired
	MapperJSONService mapperJSONService;

	@Autowired
	JWTProvider jwtProvider;
	
	/**
	 * Récupère la liste des emails
	 * @param uuid
	 * @param token
	 * @return
	 */
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère la liste des emails envoyés")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
		    @ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
		    @ApiResponse(code = 200, message = "Liste des emails (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/liste_emails", 
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity get_liste_emails(
			@ApiParam(value = "Critères de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de modifier un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une commande
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.AFFICHAGE_EMAILS)) {
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
		List<String> colonnesFiltrageActive = Arrays.asList("recipient");
		String destinataire = DatatableUtils.getFiltrageValeur(colonnesFiltrageActive, postBody);

		ParentSpecificationsBuilder builder = new UtilisateurOperateurKamtarSpecificationsBuilder();
		Specification spec = DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, null);
		if (spec == null) {
			spec = Specification.where(DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, null));
		}


		// filtrage par date de création
		Date date1 = DatatableUtils.fitrageDateDebut(postBody, "createdAt", "createdAt");
		Date date2 = DatatableUtils.fitrageDateFin(postBody, "createdAt", "createdAt");

		// préparation les deux requêtes (résultat et comptage)
		Map<String, Object> liste_emails = emailService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, destinataire, jwtProvider.getCodePays(token), date1, date2);
		liste_emails.put("draw", draw);	
		
		
		actionAuditService.getListeEmails(token);

		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(liste_emails), HttpStatus.OK);

	}
	
	/**
	 * Récupère l'email
	 * @param fileName
	 * @param request
	 * @return
	 */
    //@RequestLogger(name = Application.microservice)
	@Retryable(
		      maxAttempts = Application.retry_max_attempt,
		      backoff = @Backoff(delay = 5000))
	@ResponseBody
	@RequestMapping(
			value = "/email/{uuid}", 
			method = RequestMethod.GET)
	@ApiOperation(value = "Récupère un email")
	@ApiResponses(value = {
	})
	public ResponseEntity<byte[]> downloadEmail(
			HttpServletRequest request, 
			@ApiParam(value = "UUID de l'email demandé", required = true) @PathVariable(value = "uuid", required = true) String uuid
			) {

		HttpHeaders httpHeaders = new HttpHeaders();

		byte[] content = emailService.getContenu(uuid);
		if (content == null || content.length == 0) {
			return new ResponseEntity<>(null, httpHeaders, HttpStatus.NOT_FOUND);
		}

		httpHeaders.setContentType(MediaType.TEXT_PLAIN);
		httpHeaders.setContentLength(content.length);
		httpHeaders.setContentDispositionFormData("attachment", "email.html");

		return new ResponseEntity<>(content, httpHeaders, HttpStatus.OK);

	}
	


}
