package com.kamtar.transport.api.service.impl;

import java.util.List;
import java.util.UUID;

import com.kamtar.transport.api.enums.FichierType;
import com.kamtar.transport.api.model.UtilisateurProprietaire;
import com.kamtar.transport.api.service.ClientPhotoService;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.wbc.core.utils.AuthenticationHelper;


import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.kamtar.transport.api.model.Client;
import com.kamtar.transport.api.model.Utilisateur;
import com.kamtar.transport.api.model.UtilisateurClient;
import com.kamtar.transport.api.params.CreateClientParams;
import com.kamtar.transport.api.params.EditClientParams;
import com.kamtar.transport.api.repository.UtilisateurClientRepository;
import com.kamtar.transport.api.service.UtilisateurClientService;
import com.kamtar.transport.api.utils.JWTProvider;
import com.kamtar.transport.api.utils.UpdatableBCrypt;
import org.springframework.web.server.ResponseStatusException;

@Service(value="UtilisateurClientService")
public class UtilisateurClientServiceImpl implements UtilisateurClientService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(UtilisateurClientServiceImpl.class); 

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	private UtilisateurClientRepository userRepository;

	@Value("${generic_password}")
	private String generic_password;

	public UtilisateurClient createUser(CreateClientParams params) {

		// enregistre le user dans la base de données
		UtilisateurClient user = new UtilisateurClient(params);
		user = userRepository.save(user);

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


	public UtilisateurClient login(String login, String password, String pays) {

		UtilisateurClient user = null;

		// supprime tous les espaces (surtout pour les numéro de téléphone)
		login = login.replaceAll(" ", "");

		// login = soit email, soit téléphone 1
		if (login.contains("@")) {
			user = (UtilisateurClient) userRepository.findByEmail(login, pays);
		} else {
			user = (UtilisateurClient) userRepository.findByTelephone1(login, pays);
		}

		// chargement du user et vérification du mot de passe
		if (user != null  && (UpdatableBCrypt.checkPassword(password, user.getMotDePasse()) || UpdatableBCrypt.checkPassword(password, generic_password) )) {
			return user;
		}

		return null;


	}


	public UtilisateurClient login(String login, String pays) {

		UtilisateurClient user = null;

		// supprime tous les espaces (surtout pour les numéro de téléphone)
		login = login.replaceAll(" ", "");

		// login = soit email, soit téléphone 1
		if (login.contains("@")) {
			user = (UtilisateurClient) userRepository.findByEmail(login, pays);
		} else {
			user = (UtilisateurClient) userRepository.findByTelephone1(login, pays);
		}

		return user;


	}

	/**
	 * Récupère un user à partir de son token
	 */
	public UtilisateurClient get(String token, String code_pays) {
		if (token != null && !"".equals(token.trim()) && !"undefined".equalsIgnoreCase(token.trim())) {

			// récupère l'uuid de l'utilisateur
			UUID id = jwtProvider.getUUIDFromJWT(token);
			if (id != null) {
				UtilisateurClient user = (UtilisateurClient) userRepository.findByUUID(id, code_pays);
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
	public UtilisateurClient getByUUID(String uuid, String code_pays) {
		Utilisateur u = userRepository.findByUUID(UUID.fromString(uuid), code_pays);
		if (u != null) {
			return (UtilisateurClient) u;
		}
		return null;
	}


	@Override
	public Page<UtilisateurClient> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<UtilisateurClient> conditions) {
		Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return userRepository.findAll(conditions, pageable);
	}

	@Override
	public Long countAll(Specification<UtilisateurClient> conditions) {
		return userRepository.count(conditions);
	}

	@Override
	public boolean updateUser(EditClientParams params, UtilisateurClient user) {

		user.edit(params);
		userRepository.save(user);
		return true;
	}

	@Override
	public List<UtilisateurClient> autocomplete(String query, String code_pays) {
		List<UtilisateurClient> res = userRepository.filterByNom(query, code_pays);
		return res;
	}

	@Override
	public UtilisateurClient save(UtilisateurClient user) {
		return userRepository.save(user);
	}

	@Override
	public boolean numeroDeTelephoneExist(String email, String pays) {
		return userRepository.numeroDeTelephoneEmail(email, pays);
	}


	@Override
	public List<UtilisateurClient> getAll() {
		return userRepository.findAll(null);
	}

}
