package com.kamtar.transport.api.controller.admin;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.criteria.LanguageSpecificationsBuilder;
import com.kamtar.transport.api.criteria.ParentSpecificationsBuilder;
import com.kamtar.transport.api.model.Language;
import com.kamtar.transport.api.params.LanguageCreateParams;
import com.kamtar.transport.api.security.SecurityUtils;
import com.kamtar.transport.api.service.ActionAuditService;
import com.kamtar.transport.api.service.LanguageService;
import com.kamtar.transport.api.swagger.MapDatatable;
import com.kamtar.transport.api.utils.DatatableUtils;
import com.kamtar.transport.api.utils.JWTProvider;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@RestController
@EnableWebMvc
public class LanguageAdminController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(LanguageAdminController.class);  

	@Autowired
	LanguageService languageService;

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	ActionAuditService actionAuditService;

	@Autowired
	MapperJSONService mapperJSONService;

	/**
	 * Cr??ation d'un language 
	 * @param postBody
	 * @return
	 */
	/*@RequestLogger(name = Application.microservice)*/
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "")
	@ApiResponses(value = {
		    @ApiResponse(code = 400, message = "Impossible d'enregistrer la langue"),
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv??s aux admins)"),
		    @ApiResponse(code = 201, message = "Cr??ation d'une langue", response = Language.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/admin/language", 
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity create(
			@ApiParam(value = "Param??tres d'entr??e", required = true) @Valid @RequestBody LanguageCreateParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que 
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// v??rifie que les droits lui permettent de lire les exp??diteurs
		if (!SecurityUtils.admin(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		// enregistrement du language
		Language language = new Language(postBody);
		language = languageService.create(language);

		if (language != null) {
			actionAuditService.creerLanguage(language, token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(language), HttpStatus.CREATED);

		}

		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible d'enregistrer la langue.");

	}

	/**
	 * Liste de toutes les langues
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez ??tre identifi?? pour effectuer cette op??ration"),
		    @ApiResponse(code = 403, message = "Vous n'??tes pas autoris?? ?? effectuer cette action (reserv??s aux admins)"),
		    @ApiResponse(code = 200, message = "Liste de toutes les langues (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/admin/languages", 
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> getLanguages(
			@ApiParam(value = "Crit??res de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// v??rifie que le jeton est valide et que 
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// v??rifie que les droits lui permettent de lire les exp??diteurs
		if (!SecurityUtils.admin(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette op??ration.");
		}

		// param??tres du datatable
		String draw 		= postBody.getFirst("draw").toString();
		Integer length 		= Integer.valueOf(postBody.getFirst("length").toString());

		// tri, sens et num??ro de page
		String colonneTriDefaut = "createdOn";
		List<String> colonnesTriAutorise = Arrays.asList("code");

		String order_column_bdd = DatatableUtils.getOrderColonne(colonneTriDefaut, colonnesTriAutorise, postBody);
		String sort_bdd = DatatableUtils.getSort(postBody);
		Integer numero_page = DatatableUtils.getNumeroPage(postBody, length);

		// filtrage
		List<String> colonnesFiltrageActive = Arrays.asList();
		ParentSpecificationsBuilder builder = new LanguageSpecificationsBuilder();
		Specification spec = DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, null);

		// pr??paration les deux requ??tes (r??sultat et comptage)
		Page<Language> languages = languageService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);
		Long total = languageService.countAll(spec);

		// pr??pare les r??sultast
		JSONArray jsonArrayOffres = new JSONArray();
		jsonArrayOffres.addAll(languages.getContent());
		Map<String, Object> jsonDataResults = new LinkedHashMap<String, Object>();
		jsonDataResults.put("draw", draw);	
		jsonDataResults.put("recordsTotal", total);		
		jsonDataResults.put("recordsFiltered", total);	
		jsonDataResults.put("data", jsonArrayOffres);		

		
		actionAuditService.getLanguages(token);

		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(jsonDataResults), HttpStatus.OK);
	}

}