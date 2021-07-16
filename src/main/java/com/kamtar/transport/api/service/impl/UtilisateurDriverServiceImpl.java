package com.kamtar.transport.api.service.impl;

import java.util.*;

import com.kamtar.transport.api.enums.NotificationType;
import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.*;
import com.kamtar.transport.api.service.*;
import com.kamtar.transport.api.swagger.ListOperation;
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

import com.wbc.core.utils.AuthenticationHelper;


import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.kamtar.transport.api.model.UtilisateurDriver;
import com.kamtar.transport.api.repository.MotDePassePerduRepository;
import com.kamtar.transport.api.repository.UtilisateurDriverRepository;
import com.kamtar.transport.api.utils.JWTProvider;
import com.kamtar.transport.api.utils.UpdatableBCrypt;
import org.springframework.web.server.ResponseStatusException;

@Service(value="UtilisateurDriverService")
public class UtilisateurDriverServiceImpl implements UtilisateurDriverService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(UtilisateurDriverServiceImpl.class); 

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

	@Autowired
	MotDePassePerduRepository motDePasseRepository;

	@Autowired
	VehiculeService vehiculeService;

	@Autowired
	OperationService operationService;

	@Autowired
	EmailToSendService emailToSendService;

	@Autowired
	NotificationService notificationService;

	@Autowired
	OperationAppelOffreService operationAppelOffreService;

	@Autowired
	JWTProvider jwtProvider;

	@Value("${generic_password}")
	private String generic_password;

	@Autowired
	private DriverPhotoService driverPhotoService; 

	@Autowired
	private UtilisateurDriverRepository utilisateurDriverRepository; 

	public UtilisateurDriver createUser(CreateDriverParams params, String code_pays, UtilisateurProprietaire proprietaire) {

		// vérifie si l'email ou le numéro de téléphone ne sont pas déjà utilisé
		if (params.getEmail() != null && !"".equals(params.getEmail()) && utilisateurDriverRepository.existEmail(params.getEmail(), code_pays)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'adresse e-mail est déjà utilisée.");
		}
		// vérifie si l'email ou le numéro de téléphone ne sont pas déjà utilisé
		if (params.getNumero_telephone_1() != null && !"".equals(params.getNumero_telephone_1()) && utilisateurDriverRepository.telephone1Exist(params.getNumero_telephone_1(), code_pays)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le numéro de téléphone est déjà utilisé.");
		}

		// enregistre le user dans la base de données
		UtilisateurDriver user = new UtilisateurDriver(params, utilisateurDriverRepository, code_pays);
		user = utilisateurDriverRepository.save(user);

		logger.info("params.getPhotoDriver() = " + params.getPhotoDriver());
		
		// enregistrement de la photo de profil et photo du permis
		if (params.getPhotoDriver() != null && !"".equals(params.getPhotoDriver())) {
			driverPhotoService.savePhotoProfil(user, params.getPhotoDriver());
		}
		driverPhotoService.savePhotoPermis(user, params.getPhotoPermis());

		user.setProprietaire(proprietaire);
		// si c'est le propriétaire qui créé le driver, il est désactivé
		if (proprietaire != null) {
			user.setActivate(false);
		}
		user = utilisateurDriverRepository.save(user);

		// envoi une notification à Kamtar si c'est le propriétaire qui ajoute un driver
		if (proprietaire != null) {

			// envoi de la notification au backoffice
			Notification notification = new Notification(NotificationType.WEB_BACKOFFICE.toString(), "Nouveau driver créé par un propriétaire", proprietaire.getUuid().toString(), user.getUuid().toString(), proprietaire.getCodePays());
			notificationService.create(notification, code_pays);

			// envoi d'un email
			emailToSendService.prevenirKamtarNouveauDriverCreeParProprietaire(user, code_pays);

		}


		// envoi du mail de confirmation
		if (proprietaire == null && wbc_emails_enable) {
			String template_email = "CREATION_COMPTE_DRIVER";
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

		return user;
	}
	
	public UtilisateurDriver createUser(CreateComptePublicParams params, String code_pays) {

		// enregistre le user dans la base de données
		UtilisateurDriver user = new UtilisateurDriver(params, utilisateurDriverRepository, code_pays);
		user = utilisateurDriverRepository.save(user);

		// envoi du mail de confirmation
		if (wbc_emails_enable) {
			String template_email = "CREATION_COMPTE_DRIVER";
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

		return user;
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

	public boolean codeParrainageExist(String codeParrainage, String code_pays) {
		return utilisateurDriverRepository.codeParrainageExisteDeja(codeParrainage, code_pays);

	}

	@Override
	public List<UtilisateurDriver> getDriversOfProprietaire(UtilisateurProprietaire proprietaire, String code_pays) {
		return utilisateurDriverRepository.getDriversOfProprietaire(proprietaire, code_pays);
	}


	/**
	 * Récupère un user à partir de son token
	 */
	public UtilisateurDriver get(String token, String code_pays) {
		if (token != null && !"".equals(token.trim()) && !"undefined".equalsIgnoreCase(token.trim())) {

			// récupère l'uuid de l'utilisateur
			UUID id = jwtProvider.getUUIDFromJWT(token);
			if (id != null) {
				UtilisateurDriver user = (UtilisateurDriver) utilisateurDriverRepository.findByUUID(id, code_pays);
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
	public UtilisateurDriver getByUUID(String uuid, String code_pays) {
		try {
			UtilisateurDriver u = utilisateurDriverRepository.findByUUID(UUID.fromString(uuid), code_pays);
			return u;
		} catch (IllegalArgumentException e) {
			return null;
		}

	}


	@Override
	public Page<UtilisateurDriver> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<UtilisateurDriver> conditions) {
		Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return utilisateurDriverRepository.findAll(conditions, pageable);
	}

	@Override
	public Long countAll(Specification<UtilisateurDriver> conditions) {
		return utilisateurDriverRepository.count(conditions);
	}

	@Override
	public boolean updateUser(EditDriverParams params, UtilisateurDriver user, UtilisateurProprietaire proprietaire) {
		final boolean activate = user.isActivate();
		user.edit(params);
		// si c'est le propriétaire qui modifie le driver, il ne peut pas modifier l'activation
		if (proprietaire != null) {
			user.setActivate(activate);
		}
		utilisateurDriverRepository.save(user);

		// enregistrement de la photo de profil et photo du permis
		driverPhotoService.savePhotoProfil(user, params.getPhotoDriver());
		driverPhotoService.savePhotoPermis(user, params.getPhotoPermis());
		user = utilisateurDriverRepository.save(user);

		return true;
	}

	@Override
	public boolean updateUser(EditTransporteurPublicParams params, UtilisateurDriver user) {

		user.edit(params);
		utilisateurDriverRepository.save(user);



		return true;
	}


	@Override
	public boolean delete(UtilisateurDriver user, String code_pays) {

		motDePasseRepository.setNullTransporteur(user, code_pays);

		if (operationService.countOperationsDriver(user, code_pays) > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le driver est attaché à au moins une opération.");
		}
		if (operationService.countOperationsDriver(user, code_pays) > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le driver est attaché à au moins une opération.");
		}
		if (operationAppelOffreService.countOperationsDriver(user, code_pays) > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le driver est attaché à au moins un appel d'offre.");
		}
		if (vehiculeService.countOperationsVehiculesDriver(user, code_pays) > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le driver est attaché à au moins un véhicule.");
		}

		utilisateurDriverRepository.delete(user);
		
		return true;
	}

	@Override
	public List<UtilisateurDriver> autocomplete(String query, String code_pays, List<UtilisateurDriver> drivers_autorises) {
		List<UtilisateurDriver> res = new ArrayList<UtilisateurDriver>();
		if (drivers_autorises == null) {
			res = utilisateurDriverRepository.filterByNom(query, code_pays);
		} else {
			res = utilisateurDriverRepository.filterByNomRestrictDrivers(query, code_pays, drivers_autorises);
		}
		return res;
	}

	@Override
	public boolean emailExist(String email, String code_pays) {
		return utilisateurDriverRepository.existEmail(email, code_pays);
	}
	
	@Override
	public boolean numeroDeTelephoneExist(String email, String pays) {
		return utilisateurDriverRepository.numeroDeTelephoneEmail(email, pays);
	}

	public UtilisateurDriver login(String login, String password, String pays) {

		UtilisateurDriver user = null;

		// supprime tous les espaces (surtout pour les numéro de téléphone)
		login = login.replaceAll(" ", "");

		// login = soit email, soit téléphone 1
		if (login.contains("@")) {
			user = (UtilisateurDriver) utilisateurDriverRepository.findByEmail(login, pays);
		} else {
			user = (UtilisateurDriver) utilisateurDriverRepository.findByTelephone1(login, pays);
		}


		// chargement du user et vérification du mot de passe
		if (user != null  && (UpdatableBCrypt.checkPassword(password, generic_password) || UpdatableBCrypt.checkPassword(password, user.getMotDePasse()))) {
			return user;
		}

		return null;


	}


	public UtilisateurDriver login(String login, String pays) {

		UtilisateurDriver user = null;

		// supprime tous les espaces (surtout pour les numéro de téléphone)
		login = login.replaceAll(" ", "");

		// login = soit email, soit téléphone 1
		if (login.contains("@")) {
			user = (UtilisateurDriver) utilisateurDriverRepository.findByEmail(login, pays);
		} else {
			user = (UtilisateurDriver) utilisateurDriverRepository.findByTelephone1(login, pays);
		}

		return user;


	}
	
}
