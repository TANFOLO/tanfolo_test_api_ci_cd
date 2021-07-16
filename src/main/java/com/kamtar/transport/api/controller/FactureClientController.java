package com.kamtar.transport.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.classes.FactureClientMinimal;
import com.kamtar.transport.api.criteria.OperationSpecificationsBuilder;
import com.kamtar.transport.api.criteria.ParentSpecificationsBuilder;
import com.kamtar.transport.api.enums.ClientPersonnelListeDeDroits;
import com.kamtar.transport.api.enums.OperateurListeDeDroits;
import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.DeleteFactureClientParams;
import com.kamtar.transport.api.security.SecurityUtils;
import com.kamtar.transport.api.service.*;
import com.kamtar.transport.api.swagger.ListFactureClient;
import com.kamtar.transport.api.swagger.ListProprietaire;
import com.kamtar.transport.api.swagger.MapDatatable;
import com.kamtar.transport.api.utils.DatatableUtils;
import com.kamtar.transport.api.utils.ExportUtils;
import com.kamtar.transport.api.utils.JWTProvider;
import io.swagger.annotations.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Api(value="Gestion des factures clients", description="API Rest qui gère l'ensemble des factures clients")
@RestController
@EnableWebMvc
public class FactureClientController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(FactureClientController.class);

	@Autowired
	FactureClientService factureClientService;

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	MapperJSONService mapperJSONService;

	@Autowired
	ExportExcelService exportExcelService;

	@Autowired
	ActionAuditService actionAuditService;

	@Autowired
	OperationService operationService;

	@Autowired
	ClientService clientService;


	/**
	 * Récupère une facture client
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère les informations d'une facture client")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, expéditeur ayant le droit ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver la facture"),
			@ApiResponse(code = 200, message = "Facture demandée", response = FactureClient.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/facture/client",
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity get(
			@ApiParam(value = "Numéro de la facture", required = true) @RequestParam("numero") String numero,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher un point expéditeur
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.CONSULTER_FACTURES_CLIENT)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// chargement d'un expéditeur si c'ets un admin ou de soit même si l'utilisateur connecté est un epxéditeur
		FactureClient facture_client = factureClientService.getByNumero(numero, jwtProvider.getCodePays(token));
		if (facture_client == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver la facture client.");
		}

		actionAuditService.getFacture(facture_client.getNumeroFacture(), token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(facture_client), HttpStatus.OK);

	}


	/**
	 * Récupère le PDF d'une facture client
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère le PDF d'une facture client")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, expéditeur ayant le droit ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver la facture"),
			@ApiResponse(code = 200, message = "Facture demandée", response = FactureClient.class)
	})
	@RequestMapping(
			produces = MediaType.APPLICATION_PDF_VALUE,
			value = "/facture/client/*",
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public byte[] get_facture_pdf(
			@ApiParam(value = "Numéro de la facture", required = true) @RequestParam("numero") String numero,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestParam("Token") String token) {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de afficher un point expéditeur
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.CONSULTER_FACTURES_CLIENT) && !SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_FACTURES)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// chargement d'un expéditeur si c'ets un admin ou de soit même si l'utilisateur connecté est un epxéditeur
		FactureClient facture_client = factureClientService.getByNumero(numero, jwtProvider.getCodePays(token));
		if (facture_client == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver la facture client.");
		}

		// si c'est un client qui demande la facyre, vérifie qu'il peut bien y accéder
		String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
		if (uuid_client != null && !"".equals(uuid_client)) {
			Client client = clientService.getByUUID(uuid_client, jwtProvider.getCodePays(token));
			if (SecurityUtils.client(jwtProvider, token) && !facture_client.getClient().equals(client)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'accéder à cette facture.");
			}
		}

		if (facture_client.getFichier() == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de récupérer la facture client.");
		}
		actionAuditService.getFacturePDF(facture_client.getFichier(), token);

		return factureClientService.get(facture_client.getFichier());

	}



	/**
	 * Export des factures clients
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Export des factures clients (quelque soit le lient si admin connecté, ses propres factures si client connecté)")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (reservés aux admins ou opérateurs ayant droits, ou aux clients pour leurs propres factures)"),
			@ApiResponse(code = 200, message = "CSV des factures clients")
	})
	@RequestMapping(
			produces = "application/json",
			value = "/factures/client/export",
			method = RequestMethod.GET)
	@CrossOrigin
	public ResponseEntity<byte[]> export(
			@ApiParam(value = "Date de début", required = true) @RequestParam("date_debut") String date_debut,
			@ApiParam(value = "Date de fin", required = true) @RequestParam("date_fin") String date_fin,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestParam("Token") String token) throws Exception {

		// vérifie que le jeton est valide et que les droits lui permettent de consulter les admin katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.admin(jwtProvider, token) && !SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_FACTURES)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
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

		// filtrage par date
		Specification spec = new Specification<ActionAudit>() {
			public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.greaterThanOrEqualTo(root.get(order_column_bdd), dateMin.getTime()));
				predicates.add(builder.lessThanOrEqualTo(root.get(order_column_bdd), dateMax.getTime()));
				return builder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};

		// filtre par client si c'est demandé par un client
		if (SecurityUtils.client(jwtProvider, token)) {
			Client client = clientService.getByUUID(jwtProvider.getClaimsValue("uuid_client", token), jwtProvider.getCodePays(token));
			if (client != null) {
				Specification spec2 = new Specification<ActionAudit>() {
					public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						predicates.add(builder.equal(root.get("client"), client));
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec = spec.and(spec2);
			}
		}


		// préparation les deux requêtes (résultat et comptage)
		Page<FactureClient> leads = factureClientService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);


		// convertion liste en excel
		byte[] bytesArray = null;
		// convertion en facture minimal (pour éviter d'avoir tous les attributs) si client
		if (SecurityUtils.client(jwtProvider, token)) {
			List<FactureClientMinimal> factures_clents_minimal = new ArrayList<FactureClientMinimal>();
			for (FactureClient facture_client : leads.getContent()) {
				factures_clents_minimal.add(new FactureClientMinimal(facture_client));
			}
			Map<String, String> entetes_a_remplacer = new HashMap<String, String>();
			entetes_a_remplacer.put("/dateFacture", "Date de Facturation");
			entetes_a_remplacer.put("/numeroFacture", "Numéro de Facture");
			entetes_a_remplacer.put("/listeOperations", "Liste des opérations");
			entetes_a_remplacer.put("/montantHT", "Montant HT");
			entetes_a_remplacer.put("/remisePourcentage", "Remise (%)");
			entetes_a_remplacer.put("/montantTVA", "Montant TVA");
			entetes_a_remplacer.put("/montantTTC", "Montant TTC");
			entetes_a_remplacer.put("/netAPayer", "Net à payer");
			bytesArray = exportExcelService.export(new PageImpl<>(factures_clents_minimal), entetes_a_remplacer);
		} else {
			bytesArray = exportExcelService.export(leads, null);
		}


		// audit
		actionAuditService.exportFacturesClient(token);

		return ResponseEntity.ok()
				//.headers(headers) // add headers if any
				.contentLength(bytesArray.length)
				.contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
				.body(bytesArray);
	}


	/**
	 * Liste des factures clients
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ApiOperation(value = "Liste des factures clients")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
		    @ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (uniquement les admin ou opérateurs ayant droits)"),
		    @ApiResponse(code = 200, message = "Liste des actions retournées (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
		})
	@ResponseBody
	@RequestMapping(
			produces = "application/json", 
			value = "/factures/client",
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_factures_clients(
			@ApiParam(value = "Critères de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de consulter les admin katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.CONSULTER_FACTURES_CLIENT) && !SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_FACTURES)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");		
		}

		// paramètres du datatable
		String draw 		= postBody.getFirst("draw").toString();
		Integer length 		= Integer.valueOf(postBody.getFirst("length").toString());

		// tri, sens et numéro de page
		String colonneTriDefaut = "numeroFacture";
		List<String> colonnesTriAutorise = Arrays.asList("createdOn", "montantHT", "numeroFacture", "montantTTC", "dateFacture");

		String order_column_bdd = DatatableUtils.getOrderColonne(colonneTriDefaut, colonnesTriAutorise, postBody);
		String sort_bdd = DatatableUtils.getSort(postBody);
		Integer numero_page = DatatableUtils.getNumeroPage(postBody, length);

		// filtrage
		List<String> colonnesFiltrageActive = Arrays.asList("listeOperations", "createdOn",  "numeroFacture");
		ParentSpecificationsBuilder builder = new OperationSpecificationsBuilder();
		Specification spec_general = DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, null);
		if (spec_general == null) {
			spec_general = Specification.where(DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, null));
		}

		// filtrage par date de création
		spec_general = DatatableUtils.fitrageDate(spec_general, postBody, "createdOn", "createdOn");
		spec_general = DatatableUtils.fitrageDate(spec_general, postBody, "dateFacture", "dateFacture");
		spec_general = DatatableUtils.fitrageEntier(spec_general, postBody, "montantHT", "montantHT");
		spec_general = DatatableUtils.fitrageEntier(spec_general, postBody, "montantTTC", "montantTTC");

		// filtre par expéditeur dans datatable (si admin ou opérateur)
		Map<Integer, String> indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		Integer position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, "client");
		if (position_colonne != null) {
			String filtre_par_expediteur = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			if (filtre_par_expediteur != null && !"".equals(filtre_par_expediteur.trim())) {
				Specification spec2 = new Specification<Operation>() {
					public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<Predicate>();
						Client expediteur = clientService.getByUUID(filtre_par_expediteur, jwtProvider.getCodePays(token));
						if (expediteur != null) {
							predicates.add(builder.equal(root.get("client"), expediteur));
						}
						return builder.and(predicates.toArray(new Predicate[predicates.size()]));
					}
				};
				spec_general = spec_general.and(spec2);
			}
		}

		// surcharge du tri
		if (postBody.containsKey("sorting") && "desc".equals(postBody.getFirst("sorting").toString())) {
			sort_bdd = "desc";
		} else if (postBody.containsKey("sorting") && "asc".equals(postBody.getFirst("sorting").toString())) {
			sort_bdd = "asc";
		}


		// filtrage par es factures si c'ets un client
		if (SecurityUtils.client(jwtProvider, token)) {
			List<Predicate> predicates = new ArrayList<Predicate>();
			Specification spec2 = new Specification<Operation>() {
				public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					Client client = clientService.getByUUID(jwtProvider.getClaimsValue("uuid_client", token), jwtProvider.getCodePays(token));
					predicates.add(builder.equal(root.get("client"), client));
					return builder.and(predicates.toArray(new Predicate[predicates.size()]));
				}
			};
			spec_general = spec_general.and(spec2);
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


		// préparation les deux requêtes (résultat et comptage)
		Page<FactureClient> leads = factureClientService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec_general);
		Long total = factureClientService.countAll(spec_general);

		actionAuditService.getFacturesClient(token);

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
	 * Liste des factures clients
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ApiOperation(value = "Liste des factures clients")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (uniquement les admin ou opérateurs ayant droits)"),
			@ApiResponse(code = 200, message = "Liste des actions retournées (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
	})
	@ResponseBody
	@RequestMapping(
			produces = "application/json",
			value = "/factures/client/liste",
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_liste_facures(
			@ApiParam(value = "Est ce qu'il faut charger les operations ?", required = false) @RequestParam(value = "operations", required=false) String load_operations,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de consulter les admin katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.CONSULTER_FACTURES_CLIENT) && !SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.VOIR_FACTURES)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// tri, sens et numéro de page
		String order_column_bdd = "numeroFacture";
		String sort_bdd = "asc";

		// filtrage
		ParentSpecificationsBuilder builder = new OperationSpecificationsBuilder();
		Specification spec_general = DatatableUtils.buildFiltres(null, null, builder, null);
		if (spec_general == null) {
			spec_general = Specification.where(DatatableUtils.buildFiltres(null, null, builder, null));
		}

		// filtrage par pays kamtar
		Specification spec_pays = new Specification<ActionAudit>() {
			public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get("codePays"), code_pays));
				return builder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
		spec_general = spec_general.and(spec_pays);


		// préparation les deux requêtes (résultat et comptage)
		List<FactureClient> leads = factureClientService.getAllPagined(order_column_bdd, sort_bdd, 0, 999999, spec_general).getContent();

		if (load_operations != null && "1".equals(load_operations)) {
			// chargement des opérations
			List<FactureClient> leads_operations = new ArrayList<FactureClient>();
			for (FactureClient facture : leads) {
				String[] code_operations = facture.getListeOperations().split("@");
				Long[] code_long_operations = new Long[code_operations.length];
				int i = 0;
				for (String code_operation: code_operations) {
					try {
						code_long_operations[i] = Long.valueOf(code_operation);
						i++;
					} catch (NumberFormatException e ) {
						// erreur silencieuse
					}
				}

				List<Operation> operations = operationService.getByCodes(code_long_operations, code_pays);
				facture.setOperations(operations);
				leads_operations.add(facture);
			}
			leads = leads_operations;

		}

		actionAuditService.getFacturesClient(token);

		return new ResponseEntity<Object>(mapperJSONService.get(token).writeValueAsString(leads), HttpStatus.OK);

	}



	/**
	 * Suppression d'une facture client
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Suppression d'une facture client")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (admin, ou opérateur ayant le droit)"),
			@ApiResponse(code = 404, message = "Impossible de trouver la facture"),
			@ApiResponse(code = 200, message = "Operation supprimée", response = Boolean.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/facture/client",
			method = RequestMethod.DELETE)
	@CrossOrigin(origins="*")
	public ResponseEntity supprimer(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody DeleteFactureClientParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) {

		// vérifie que le jeton est valide et que les droits lui permettent de modifier un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer une operation
		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.DECLENCHER_FACTURATION_CLIENT) || !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.SUPPRESSION_FACTURATION_CLIENT)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		// chargement
		FactureClient facture_client = factureClientService.getByUUID(postBody.getId(), jwtProvider.getCodePays(token));
		if (facture_client == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver la facture.");
		}
		String numeroFacture = facture_client.getNumeroFacture();

		// supression dela facture
		factureClientService.delete(facture_client);

		// suppression de la référence à la facture dans les opérations
		operationService.setNullOperationsNumeroFactureClient(numeroFacture, jwtProvider.getCodePays(token));

		actionAuditService.supprimerFacture(numeroFacture, token);

		return new ResponseEntity<>(true, HttpStatus.OK);

	}



}
