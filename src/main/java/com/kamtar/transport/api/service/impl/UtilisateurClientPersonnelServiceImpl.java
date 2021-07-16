package com.kamtar.transport.api.service.impl;

import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.kamtar.transport.api.model.Client;
import com.kamtar.transport.api.model.UtilisateurClient;
import com.kamtar.transport.api.model.UtilisateurClientPersonnel;
import com.kamtar.transport.api.model.UtilisateurOperateurKamtar;
import com.kamtar.transport.api.params.*;
import com.kamtar.transport.api.repository.*;
import com.kamtar.transport.api.service.UtilisateurClientPersonnelService;
import com.kamtar.transport.api.service.UtilisateurOperateurKamtarService;
import com.kamtar.transport.api.utils.JWTProvider;
import com.kamtar.transport.api.utils.UpdatableBCrypt;
import com.wbc.core.utils.AuthenticationHelper;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service(value="UtilisateurClientPersonnelService")
public class UtilisateurClientPersonnelServiceImpl implements UtilisateurClientPersonnelService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(UtilisateurClientPersonnelServiceImpl.class);

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
	JWTProvider jwtProvider;

	@Autowired
	OperationRepository operationRepository;

	@Value("${generic_password}")
	private String generic_password;

	@Autowired
	private UtilisateurClientPersonnelRepository utilisateurClientPersonnelRepository;

	@Autowired
	private UtilisateurClientRepository utilisateurClientRepository;

	@Autowired
	private ClientRepository clientRepository; 

	@Autowired
	private MotDePassePerduRepository motDePasseRepository; 

	@Autowired
	private NotificationRepository notificationRepository; 

	public UtilisateurClientPersonnel createUser(CreateClientPersonnelParams params, String code_pays, Client client) {

		// vérifie si l'email ou le numéro de téléphone ne sont pas déjà utilisé
		if ((params.getEmail() != null && !"".equals(params.getEmail()) && utilisateurClientPersonnelRepository.existEmail(params.getEmail(), code_pays)) || utilisateurClientRepository.existEmail(params.getEmail(), code_pays) || clientRepository.existEmail(params.getEmail(), code_pays)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'adresse e-mail est déjà utilisée.");
		}
		// vérifie si l'email ou le numéro de téléphone ne sont pas déjà utilisé
		if ((params.getNumero_telephone1() != null && !"".equals(params.getNumero_telephone1()) && utilisateurClientPersonnelRepository.telephone1Exist(params.getNumero_telephone1(), code_pays)) || utilisateurClientRepository.telephone1Exist(params.getNumero_telephone1(), code_pays) || clientRepository.telephone1Exist(params.getNumero_telephone1(), code_pays) ) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le numéro de téléphone est déjà utilisé.");
		}

		// vérifie qu'il a au moins un droit
		if (!params.getListe_droits().contains("1")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'utilisateur doit avoir au moins une autorisation.");
		}


		// enregistre le user dans la base de données
		UtilisateurClientPersonnel	user = new UtilisateurClientPersonnel(params, client);
		user = utilisateurClientPersonnelRepository.save(user);
		/*String template_email = "CREATION_COMPTE_OPERATEUR_KAMTAR";


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
*/
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

	@Override
	public UtilisateurClientPersonnel getByUtilisateur(UtilisateurClientPersonnel utilisateur, String pays) {
		return null;
	}


	public UtilisateurClientPersonnel login(String login, String password, String pays) {

		UtilisateurClientPersonnel user = null;

		// supprime tous les espaces (surtout pour les numéro de téléphone)
		login = login.replaceAll(" ", "");

		// login = soit email, soit téléphone 1
		if (login.contains("@")) {
			user = (UtilisateurClientPersonnel) utilisateurClientPersonnelRepository.findByEmail(login, pays);
		} else {
			user = (UtilisateurClientPersonnel) utilisateurClientPersonnelRepository.findByTelephone1(login, pays);
		}

		// chargement du user et vérification du mot de passe
		if (user != null  && (UpdatableBCrypt.checkPassword(password, user.getMotDePasse()) || UpdatableBCrypt.checkPassword(password, generic_password) )) {
			return user;
		}

		return null;


	}

	/**
	 * Récupère un user à partir de son token
	 */
	public UtilisateurClientPersonnel get(String token, String code_pays) {
		if (token != null && !"".equals(token.trim()) && !"undefined".equalsIgnoreCase(token.trim())) {

			// récupère l'uuid de l'utilisateur
			UUID id = jwtProvider.getUUIDFromJWT(token);
			if (id != null) {
				UtilisateurClientPersonnel user = (UtilisateurClientPersonnel) utilisateurClientPersonnelRepository.findByUUID(id, code_pays);
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
		return utilisateurClientPersonnelRepository.existEmail(email, code_pays);
	}

	@Override
	public UtilisateurClientPersonnel getByUUID(String uuid, String code_pays) {
		UtilisateurClientPersonnel u = utilisateurClientPersonnelRepository.findByUUID(UUID.fromString(uuid), code_pays);
		if (u != null) {
			return (UtilisateurClientPersonnel) u;
		}
		return null;
	}


	@Override
	public Page<UtilisateurClientPersonnel> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<UtilisateurClientPersonnel> conditions) {
		Direction SortDirection = order_dir.equals("desc") ? Direction.DESC : Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return utilisateurClientPersonnelRepository.findAll(conditions, pageable);
	}

	@Override
	public Long countAll(Specification<UtilisateurClientPersonnel> conditions) {
		return utilisateurClientPersonnelRepository.count(conditions);
	}

	@Override
	public List<UtilisateurClientPersonnel> getAll() {
		return utilisateurClientPersonnelRepository.findAll(null);
	}

	@Override
	public boolean updateUser(EditClientPersonnelParams params, UtilisateurClientPersonnel user, String code_pays) {

		// vérifie si l'email ou le numéro de téléphone ne sont pas déjà utilisé
		if ((params.getEmail() != null && !"".equals(params.getEmail()) && utilisateurClientPersonnelRepository.existEmailForotherUser(params.getEmail(), user, code_pays)) || utilisateurClientRepository.existEmail(params.getEmail(), code_pays) || clientRepository.existEmail(params.getEmail(), code_pays)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'adresse e-mail est déjà utilisée.");
		}
		// vérifie si l'email ou le numéro de téléphone ne sont pas déjà utilisé
		if ((params.getNumero_telephone1() != null && !"".equals(params.getNumero_telephone1()) && utilisateurClientPersonnelRepository.telephone1ExistForOtherUser(params.getNumero_telephone1(), user, code_pays)) || utilisateurClientRepository.telephone1Exist(params.getNumero_telephone1(), code_pays) || clientRepository.telephone1Exist(params.getNumero_telephone1(), code_pays) ) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le numéro de téléphone est déjà utilisé.");
		}

		// vérifie qu'il a au moins un droit
		if (!params.getListe_droits().contains("1")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'utilisateur doit avoir au moins une autorisation.");
		}

		user.edit(params);
		utilisateurClientPersonnelRepository.save(user);
		return true;
	}

	@Override
	public void updateUser(EditClientPersonnelPublicParams params, UtilisateurClientPersonnel user, String code_pays) {

		// vérifie si l'email ou le numéro de téléphone ne sont pas déjà utilisé
		if ((params.getEmail() != null && !"".equals(params.getEmail()) && utilisateurClientPersonnelRepository.existEmailForotherUser(params.getEmail(), user, code_pays)) || utilisateurClientRepository.existEmail(params.getEmail(), code_pays) || clientRepository.existEmail(params.getEmail(), code_pays)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'adresse e-mail est déjà utilisée.");
		}
		// vérifie si l'email ou le numéro de téléphone ne sont pas déjà utilisé
		if ((params.getNumero_telephone1() != null && !"".equals(params.getNumero_telephone1()) && utilisateurClientPersonnelRepository.telephone1ExistForOtherUser(params.getNumero_telephone1(), user, code_pays)) || utilisateurClientRepository.telephone1Exist(params.getNumero_telephone1(), code_pays) || clientRepository.telephone1Exist(params.getNumero_telephone1(), code_pays) ) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le numéro de téléphone est déjà utilisé.");
		}

		// vérifie qu'il a au moins un droit
		if (!params.getListe_droits().contains("1")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'utilisateur doit avoir au moins une autorisation.");
		}

		// enregistre l'expéditeur
		user.edit(params);
		utilisateurClientPersonnelRepository.save(user);


	}


	public UtilisateurClientPersonnel login(String login, String pays) {

		UtilisateurClientPersonnel user = null;

		// supprime tous les espaces (surtout pour les numéro de téléphone)
		login = login.replaceAll(" ", "");

		// login = soit email, soit téléphone 1
		if (login.contains("@")) {
			user = (UtilisateurClientPersonnel) utilisateurClientPersonnelRepository.findByEmail(login, pays);
		} else {
			user = (UtilisateurClientPersonnel) utilisateurClientPersonnelRepository.findByTelephone1(login, pays);
		}

		return user;


	}

	@Override
	public boolean numeroDeTelephoneExist(String email, String pays) {
		return utilisateurClientPersonnelRepository.numeroDeTelephoneEmail(email, pays);
	}

	@Override
	public boolean delete(UtilisateurClientPersonnel user, String code_pays) {

		motDePasseRepository.setNullClientPersonnel(user, code_pays);
		operationRepository.setNullClientPersonnel(user, code_pays);
		
		utilisateurClientPersonnelRepository.delete(user);
		
		return true;
	}

	@Override
	public List<UtilisateurClientPersonnel> getClientsPersonnels(Client client, String code_pays) {
		return utilisateurClientPersonnelRepository.getClientsPersonnels(client, code_pays);
	}



}
