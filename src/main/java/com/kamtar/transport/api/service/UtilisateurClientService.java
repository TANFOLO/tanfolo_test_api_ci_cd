package com.kamtar.transport.api.service;



import java.util.List;
import java.util.UUID;

import com.kamtar.transport.api.model.UtilisateurProprietaire;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.kamtar.transport.api.model.UtilisateurClient;
import com.kamtar.transport.api.params.CreateClientParams;
import com.kamtar.transport.api.params.EditClientParams;


@Service
public interface UtilisateurClientService {
	
	UtilisateurClient createUser(CreateClientParams params);
	boolean updateUser(EditClientParams params, UtilisateurClient user);
	boolean emailExist(String email, String pays);
	boolean numeroDeTelephoneExist(String email, String pays);
	boolean validateUser(String email, String code);
	UtilisateurClient login(String email, String password, String pays);
	UtilisateurClient login(String email, String pays);
	UtilisateurClient get(String token, String pays);
	UtilisateurClient getByUUID(String uuid, String pays);
	AttributeType createAttributeType(String name, String value);
	String getJsonWebKey();
	List<UtilisateurClient> autocomplete(String query, String pays);
	UtilisateurClient save(UtilisateurClient user);

	public Page<UtilisateurClient> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<UtilisateurClient> conditions);
	public Long countAll(Specification<UtilisateurClient> conditions);
	public List<UtilisateurClient> getAll();

	//void enregistrePhotoProfil(UtilisateurClient user, String base64);
}
