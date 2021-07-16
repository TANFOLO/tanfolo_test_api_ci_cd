package com.kamtar.transport.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamtar.transport.api.Application;
import com.kamtar.transport.api.criteria.ParentSpecificationsBuilder;
import com.kamtar.transport.api.criteria.UtilisateurOperateurKamtarSpecificationsBuilder;
import com.kamtar.transport.api.enums.ClientPersonnelListeDeDroits;
import com.kamtar.transport.api.enums.OperateurListeDeDroits;
import com.kamtar.transport.api.enums.UtilisateurTypeDeCompte;
import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.*;
import com.kamtar.transport.api.security.SecurityUtils;
import com.kamtar.transport.api.service.*;
import com.kamtar.transport.api.swagger.ListClientPersonnel;
import com.kamtar.transport.api.swagger.MapDatatable;
import com.kamtar.transport.api.utils.DatatableUtils;
import com.kamtar.transport.api.utils.JWTProvider;
import io.swagger.annotations.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import java.text.SimpleDateFormat;
import java.util.*;


@Api(value="Gestion des clients personnels", description="API Rest qui gère les clients personnels")
@RestController
@EnableWebMvc
public class UtilisateurClientPersonnelController {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(UtilisateurClientPersonnelController.class);

	@Autowired
	CountryService countryService;

	@Autowired
	UtilisateurClientPersonnelService utilisateurClientPersonnelService;

	@Autowired
	UtilisateurClientService utilisateurClientService;

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	MapperJSONService mapperJSONService;

	@Autowired
	ExportExcelService exportExcelService;

	@Autowired
	ClientService clientService;

	@Autowired
	ActionAuditService actionAuditService;

	/**
	 * Création d'un client personnel
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Création d'un client personnel")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "L'adresse e-mail est déjà utilisée."),
			@ApiResponse(code = 400, message = "Le numéro de téléphone est déjà utilisé."),
			@ApiResponse(code = 400, message = "Impossible d'enregistrer le client."),
		    @ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
		    @ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (reservé aux admins et aux clients)"),
		    @ApiResponse(code = 201, message = "Client créé", response = UtilisateurClientPersonnel.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/client_personnel",
			method = RequestMethod.POST)
	@CrossOrigin(origins="*")
	public ResponseEntity create(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody CreateClientPersonnelParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if ( !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_CLIENTS) && !SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.GESTION_UTILISATEURS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");		
		}

		String code_pays = jwtProvider.getCodePays(token);

		Client client = null;
		if (SecurityUtils.admin(jwtProvider, token) || SecurityUtils.operateur(jwtProvider, token)) {
			client = clientService.getByUUID(postBody.getClient(), code_pays);
		} else if (SecurityUtils.client(jwtProvider, token) || SecurityUtils.client_personnel(jwtProvider, token)) {
			String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
			client = clientService.getByUUID(uuid_client, jwtProvider.getCodePays(token));
		}

		if (client == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger le client associé à l'utilisateur .");
		}
		if (!"B".equals(client.getTypeCompte())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vous ne pouvez pas ajouter un client personnel sur un compte particulier.");
		}

		if (SecurityUtils.client_personnel(jwtProvider, token)) {
			UtilisateurClientPersonnel user_personnel = utilisateurClientPersonnelService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
			if (!user_personnel.getClient().equals(client)) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vous n'avez pas le droit d'ajouter un utilisateur à ce client.");

			}
		}

		// enregistrement
		UtilisateurClientPersonnel user = utilisateurClientPersonnelService.createUser(postBody, code_pays, client);
		if (user != null) {
			actionAuditService.creerClientPersonnel(user, token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.CREATED);
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible d'enregistrer le client personnel.");

	}

	/**
	 * Modification d'un client personnel
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Modification d'un client personnel")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
		    @ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (reservé aux admins et clients)"),
		    @ApiResponse(code = 404, message = "Impossible de trouver le client personnel"),
		    @ApiResponse(code = 200, message = "Client modifié", response = UtilisateurClientPersonnel.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/client_personnel",
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity edit(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody EditClientPersonnelParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de modifier un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_CLIENTS) && !SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.GESTION_UTILISATEURS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");		
		}

		String code_pays = jwtProvider.getCodePays(token);

		// chargement
		UtilisateurClientPersonnel user = utilisateurClientPersonnelService.getByUUID(postBody.getId(), code_pays);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le client.");
		}

		Client client = null;
		if (SecurityUtils.client(jwtProvider, token)) {
			String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
			client = clientService.getByUUID(uuid_client, code_pays);
			if (client == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger le client.");
			}
			if (!user.getClient().equals(client)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit de modifier ce client personnel.");
			}
		} else if (SecurityUtils.client_personnel(jwtProvider, token)) {
			String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
			client = clientService.getByUUID(uuid_client, code_pays);
			if (SecurityUtils.client_personnel(jwtProvider, token)) {
				UtilisateurClientPersonnel user_personnel =utilisateurClientPersonnelService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
				if (!user_personnel.getClient().equals(client)) {
					throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vous n'avez pas le droit de modifier un utilisateur à ce client.");

				}
			}
		}
		if (client != null && !"B".equals(client.getTypeCompte())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vous ne pouvez pas modifier un client personnel sur un compte particulier.");
		}


		utilisateurClientPersonnelService.updateUser(postBody, user, code_pays);
		
		actionAuditService.editerClientPersonnel(user, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.OK);

	}

	/**
	 * Suppression d'un client personnel
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Suppression d'un client personnel")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
		    @ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (reservé aux admins ou client)"),
		    @ApiResponse(code = 404, message = "Impossible de trouver le client"),
		    @ApiResponse(code = 200, message = "Client personnel supprimé", response = Boolean.class)
		})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/client_personnel",
			method = RequestMethod.DELETE)
	@CrossOrigin(origins="*")
	public ResponseEntity delete(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody DeleteClientPersonnelParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de modifier un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if ( !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_CLIENTS) && !SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.GESTION_UTILISATEURS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// chargement
		UtilisateurClientPersonnel user = utilisateurClientPersonnelService.getByUUID(postBody.getId(), jwtProvider.getCodePays(token));
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le client.");
		}

		if (SecurityUtils.client(jwtProvider, token) || SecurityUtils.client_personnel(jwtProvider, token)) {
			String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
			Client client = clientService.getByUUID(uuid_client, jwtProvider.getCodePays(token));
			if (client == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger le client.");
			}
			if (!user.getClient().equals(client)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit de supprimer ce client personnel.");
			}
			if (SecurityUtils.client_personnel(jwtProvider, token)) {
				UtilisateurClientPersonnel user_personnel = utilisateurClientPersonnelService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
				if (!user_personnel.getClient().equals(client)) {
					throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vous n'avez pas le droit de supprimer un utilisateur d'un autre client.");

				}
			}
		}


		utilisateurClientPersonnelService.delete(user, jwtProvider.getCodePays(token));

		
		actionAuditService.deleteClientPersonnel(user, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.OK);

	}
	
	/**
	 * Récupère les informations d'un client personnel
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère les informations d'un opérateur kamtar")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
		    @ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (reservé aux admins ou opérateur connecté)"),
		    @ApiResponse(code = 404, message = "Impossible de trouver l'opérateur"),
		    @ApiResponse(code = 200, message = "Opérateur demandé", response = UtilisateurOperateurKamtar.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/client_personnel",
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity get(
			@ApiParam(value = "UUID du client personnel", required = true) @RequestParam("uuid") String uuid,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.admin(jwtProvider, token) && !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_CLIENTS) && !(SecurityUtils.client(jwtProvider, token)) && !(SecurityUtils.client_personnel(jwtProvider, token))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");		
		}

		// chargement
		UtilisateurClientPersonnel user = null;
		if (SecurityUtils.admin(jwtProvider, token) || SecurityUtils.operateur(jwtProvider, token)) {
			user = utilisateurClientPersonnelService.getByUUID(uuid, jwtProvider.getCodePays(token));;
		} else if (SecurityUtils.client(jwtProvider, token) || SecurityUtils.client_personnel(jwtProvider, token)) {
			String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
			Client client = clientService.getByUUID(uuid_client, jwtProvider.getCodePays(token));
			if (client == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger le client.");
			}

			user = utilisateurClientPersonnelService.getByUUID(uuid, jwtProvider.getCodePays(token));
			if (SecurityUtils.client_personnel(jwtProvider, token)) {
				if (!user.getClient().equals(client)) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit de modifier ce client personnel.");
				}
			}

		} else if (SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.GESTION_UTILISATEURS)) {
			user = utilisateurClientPersonnelService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), jwtProvider.getCodePays(token));;
		}


		if (user != null) {
			actionAuditService.getClientPersonnel(user, token);
			return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(user), HttpStatus.OK);
		}


		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le client personnel.");
	}



	/**
	 * Modification de son compte par un client personnel
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Modification de son compte par un client personnel")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "L'adresse e-mail est déjà utilisée."),
			@ApiResponse(code = 400, message = "Le numéro de téléphone est déjà utilisé."),
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (client personnel)"),
			@ApiResponse(code = 404, message = "Impossible de trouver le client"),
			@ApiResponse(code = 200, message = "Client mis à jour", response = UtilisateurClientPersonnel.class)
	})
	@RequestMapping(
			produces = "application/json",
			consumes = "application/json",
			value = "/client_personnel2",
			method = RequestMethod.PUT)
	@CrossOrigin(origins="*")
	public ResponseEntity edit2(
			@ApiParam(value = "Paramètres d'entrée", required = true) @Valid @RequestBody EditClientPersonnelPublicParams postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de modifier un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		// vérifie que les droits lui permettent de gérer un expéditeur
		if (!SecurityUtils.client_personnel(jwtProvider, token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// chargement
		UtilisateurClientPersonnel client = utilisateurClientPersonnelService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
		if (client == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le client.");
		}
		utilisateurClientPersonnelService.updateUser(postBody, client, code_pays);


		actionAuditService.editerClientPersonnel(client, token);

		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(client), HttpStatus.OK);

	}



	/**
	 * Récupère les clients personnels associés à un client
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Récupère les clients personnels associés à un client")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (reservé aux admins ou opérateur connecté)"),
			@ApiResponse(code = 404, message = "Impossible de trouver le client"),
			@ApiResponse(code = 200, message = "Liste des clients personnels", response = ListClientPersonnel.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/clients_personnels",
			method = RequestMethod.GET)
	@CrossOrigin(origins="*")
	public ResponseEntity get_clients_personnels(
			@ApiParam(value = "UUID du client", required = true) @RequestParam("client") String uuid,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de créer un opérateur kmatar
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if (!SecurityUtils.admin(jwtProvider, token) && !(SecurityUtils.client(jwtProvider, token))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");
		}

		String code_pays = jwtProvider.getCodePays(token);

		// chargement
		Client client = null;
		if (SecurityUtils.admin(jwtProvider, token)) {
			client = clientService.getByUUID(uuid, code_pays);;
		} else if (SecurityUtils.client(jwtProvider, token)) {
			String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
			client = clientService.getByUUID(uuid_client, code_pays);
		}
		if (client == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger le client.");
		}

		List<UtilisateurClientPersonnel> utilisateurs_personnels = utilisateurClientPersonnelService.getClientsPersonnels(client, code_pays);


		actionAuditService.getClientsPersonnels(client, token);
		return new ResponseEntity<>(mapperJSONService.get(token).writeValueAsString(utilisateurs_personnels), HttpStatus.OK);

	}


	/**
	 * Liste de tous les clients personnels d'un client
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste de tous les clients personnels d'un client")
	@ApiResponses(value = {
		    @ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
		    @ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (reservés aux admins et client)"),
		    @ApiResponse(code = 200, message = "Liste des clients personnels (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
		})
	@RequestMapping(
			produces = "application/json",
			value = "/clients_personnels",
			method = RequestMethod.POST)
	@CrossOrigin
	public ResponseEntity<Object> get_offres(
			@ApiParam(value = "UUID du client", required = true) @RequestParam("client") String uuid_client,
			@ApiParam(value = "Critères de recherche (tri, filtre, etc) au format Datatable", required = true) @RequestBody MultiValueMap postBody,
			@ApiParam(value = "Jeton JWT pour autentification", required = true)  @RequestHeader("Token") String token) throws JsonProcessingException {

		// vérifie que le jeton est valide et que les droits lui permettent de consulter les opérateurs katmars
		if (!jwtProvider.isValidJWT(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide, veuillez vous reconnecter.");
		}

		if ( !SecurityUtils.adminOrOperateurWithDroit(jwtProvider, token, OperateurListeDeDroits.GESTION_CLIENTS) && !SecurityUtils.client(jwtProvider, token) && !SecurityUtils.client_personnelWithDroit(jwtProvider, token, ClientPersonnelListeDeDroits.GESTION_UTILISATEURS)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas le droit d'effectuer cette opération.");		
		}

		String code_pays = jwtProvider.getCodePays(token);

		// paramètres du datatable
		String draw 		= postBody.getFirst("draw").toString();
		Integer length 		= Integer.valueOf(postBody.getFirst("length").toString());

		// tri, sens et numéro de page
		String colonneTriDefaut = "createdOn";
		List<String> colonnesTriAutorise = Arrays.asList("createdOn", "prenom", "nom", "numeroTelephone1", "email");

		String order_column_bdd = DatatableUtils.getOrderColonne(colonneTriDefaut, colonnesTriAutorise, postBody);
		String sort_bdd = DatatableUtils.getSort(postBody);
		Integer numero_page = DatatableUtils.getNumeroPage(postBody, length);

		// filtrage
		List<String> colonnesFiltrageActive = Arrays.asList("createdOn", "prenom", "nom", "numeroTelephone1", "email");
		ParentSpecificationsBuilder builder = new UtilisateurOperateurKamtarSpecificationsBuilder();
		Specification spec = DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, null);
		if (spec == null) {
			spec = Specification.where(DatatableUtils.buildFiltres(colonnesFiltrageActive, postBody, builder, null));
		}

		// filtrage par date de création
		spec = DatatableUtils.fitrageDate(spec, postBody, "createdOn", "createdOn");

		// filtrage par pays kamtar
		Specification spec_pays = new Specification<ActionAudit>() {
			public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get("codePays"), code_pays));
				return builder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
		spec = spec.and(spec_pays);

		// filtre par client
		Client user = clientService.getByUUID(uuid_client, jwtProvider.getCodePays(token));
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger les clients");
		} else if (!user.getTypeCompte().equals("B")) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger les clients personnels attachés à un compte particulier");
		}
		if (SecurityUtils.client_personnel(jwtProvider, token)) {
			UtilisateurClientPersonnel client_personnel = utilisateurClientPersonnelService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), code_pays);
			if (!client_personnel.getClient().equals(user)) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vous n'avez pas le droit d'accéder à ce client");
			}
		}

		Specification spec_client = new Specification<ActionAudit>() {
			public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get("client"), user));
				return builder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
		spec = spec.and(spec_client);


		// préparation les deux requêtes (résultat et comptage)
		Page<UtilisateurClientPersonnel> leads = utilisateurClientPersonnelService.getAllPagined(order_column_bdd, sort_bdd, numero_page, length, spec);
		Long total = utilisateurClientPersonnelService.countAll(spec);
		
		
		actionAuditService.getOperateursKamtar(token);

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
	 * Liste des actions d'un client personnel
	 * @return
	 */
	//@RequestLogger(name = Application.microservice)
	@Retryable(
			maxAttempts = Application.retry_max_attempt,
			backoff = @Backoff(delay = 5000))
	@ResponseBody
	@ApiOperation(value = "Liste des actions d'un client personnel")
	@ApiResponses(value = {
			@ApiResponse(code = 401, message = "Vous devez être identifié pour effectuer cette opération"),
			@ApiResponse(code = 403, message = "Vous n'êtes pas autorisé à effectuer cette action (reservé aux admins ou opérateurs ayant droits)"),
			@ApiResponse(code = 200, message = "Liste des actions d'un expéditeur (au format datatable - contenu dans data, nombre total dans recordsTotal)", response = MapDatatable.class)
	})
	@RequestMapping(
			produces = "application/json",
			value = "/client_personnel/actions",
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
			UtilisateurClientPersonnel expediteur = utilisateurClientPersonnelService.getByUUID(expediteur_uuid.toString(), jwtProvider.getCodePays(token));
			uuid_client = expediteur.getUuid().toString();
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
