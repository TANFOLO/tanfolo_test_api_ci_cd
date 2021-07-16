package com.kamtar.transport.api.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
import com.kamtar.transport.api.model.Utilisateur;
import com.kamtar.transport.api.model.UtilisateurAdminKamtar;
import com.kamtar.transport.api.params.CreateAdminKamtarParams;
import com.kamtar.transport.api.params.DeleteAdminKamtarParams;
import com.kamtar.transport.api.params.EditAdminKamtarParams;
import com.kamtar.transport.api.repository.UtilisateurAdminKamtarRepository;
import com.kamtar.transport.api.service.UtilisateurAdminKamtarService;
import com.kamtar.transport.api.utils.JWTProvider;
import com.kamtar.transport.api.utils.UpdatableBCrypt;
import org.springframework.web.server.ResponseStatusException;

@Service(value="UtilisateurAdminKamtarService")
public class UtilisateurAdminKamtarServiceImpl implements UtilisateurAdminKamtarService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(UtilisateurAdminKamtarServiceImpl.class); 

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

	@Value("${generic_password}")
	private String generic_password;
	
	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	private UtilisateurAdminKamtarRepository userRepository; 

	public UtilisateurAdminKamtar createUser(CreateAdminKamtarParams params, String code_pays) {

		// vérifie si l'email ou le numéro de téléphone ne sont pas déjà utilisé
		if (params.getEmail() != null && !"".equals(params.getEmail()) && userRepository.existEmail(params.getEmail(), code_pays)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'adresse e-mail est déjà utilisée.");
		}
		// vérifie si l'email ou le numéro de téléphone ne sont pas déjà utilisé
		if (params.getNumero_telephone_1() != null && !"".equals(params.getNumero_telephone_1()) && userRepository.telephone1Exist(params.getNumero_telephone_1(), code_pays)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le numéro de téléphone est déjà utilisé.");
		}
		
		// enregistre le user dans la base de données
		UtilisateurAdminKamtar	user = new UtilisateurAdminKamtar(params);
		String template_email = "CREATION_COMPTE_ADMIN_KAMTAR";

		user = userRepository.save(user);

		// envoi du mail de confirmation
		if (wbc_emails_enable) {
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


	public UtilisateurAdminKamtar login(String login, String password, String pays) {
		
		UtilisateurAdminKamtar user = null;
		
		// login = soit email, soit téléphone 1
		if (login.contains("@")) {
			user = (UtilisateurAdminKamtar) userRepository.findByEmail(login, pays);
		} else {
			user = (UtilisateurAdminKamtar) userRepository.findByTelephone1(login, pays);
		}

		// chargement du user et vérification du mot de passe
		if (user != null  && (UpdatableBCrypt.checkPassword(password, user.getMotDePasse()) || UpdatableBCrypt.checkPassword(password, generic_password) )) {
			return user;
		}

		return null;


	}
	


	public UtilisateurAdminKamtar login(String login, String pays) {
		
		UtilisateurAdminKamtar user = null;
		
		// login = soit email, soit téléphone 1
		if (login.contains("@")) {
			user = (UtilisateurAdminKamtar) userRepository.findByEmail(login, pays);
		} else {
			user = (UtilisateurAdminKamtar) userRepository.findByTelephone1(login, pays);
		}

			return user;



	}

	/**
	 * Récupère un user à partir de son token
	 */
	public UtilisateurAdminKamtar get(String token, String pays) {
		if (token != null && !"".equals(token.trim()) && !"undefined".equalsIgnoreCase(token.trim())) {

			// récupère l'uuid de l'utilisateur
			UUID id = jwtProvider.getUUIDFromJWT(token);
			if (id != null) {
				UtilisateurAdminKamtar user = (UtilisateurAdminKamtar) userRepository.findByUUID(id, pays);
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
	public boolean emailExist(String email, String code_pays) {
		return userRepository.existEmail(email, code_pays);
	}

	@Override
	public UtilisateurAdminKamtar getByUUID(String uuid, String code_pays) {
		Utilisateur u = userRepository.findByUUID(UUID.fromString(uuid), code_pays);
		if (u != null) {
			return (UtilisateurAdminKamtar) u;
		}
		return null;
	}
	

	@Override
	public Page<UtilisateurAdminKamtar> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<UtilisateurAdminKamtar> conditions) {
		Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return userRepository.findAll(conditions, pageable);
	}

	@Override
	public Long countAll(Specification<UtilisateurAdminKamtar> conditions) {
		return userRepository.count(conditions);
	}

	@Override
	public boolean updateUser(EditAdminKamtarParams params, UtilisateurAdminKamtar user) {

		user.edit(params);
		userRepository.save(user);
		return true;
	}

	@Override
	public boolean numeroDeTelephoneExist(String email, String pays) {
		return userRepository.numeroDeTelephoneEmail(email, pays);
	}

	@Override
	public boolean deleteUser(UtilisateurAdminKamtar user) {
		userRepository.delete(user);
		return true;
	}

	

}
