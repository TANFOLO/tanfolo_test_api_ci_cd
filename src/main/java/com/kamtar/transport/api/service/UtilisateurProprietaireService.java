package com.kamtar.transport.api.service;



import java.util.List;
import java.util.UUID;

import com.kamtar.transport.api.params.*;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.kamtar.transport.api.enums.UtilisateurTypeDeCompte;
import com.kamtar.transport.api.model.Utilisateur;
import com.kamtar.transport.api.model.UtilisateurProprietaire;


@Service
public interface UtilisateurProprietaireService {
	
	UtilisateurProprietaire createUser(CreateProprietaireParams params, String pays);
	UtilisateurProprietaire createUser(CreateComptePublicParams params, String code_pays);
	boolean updateUser(EditProprietaireParams params, UtilisateurProprietaire user);
	boolean updateUser(EditProprietairePublicParams params, UtilisateurProprietaire user);
	boolean emailExist(String email, String pays);
	boolean numeroDeTelephoneExist(String telephone, String pays);
	boolean validateUser(String email, String code);
	UtilisateurProprietaire login(String email, String password, String pays);
	UtilisateurProprietaire login(String email, String pays);
	UtilisateurProprietaire get(String token, String pays);
	UtilisateurProprietaire getByUUID(String uuid, String pays);
	AttributeType createAttributeType(String name, String value);
	String getJsonWebKey();
	boolean delete(UtilisateurProprietaire user, String code_pays);
	List<UtilisateurProprietaire> autocomplete(String query, String pays);

	public Page<UtilisateurProprietaire> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<UtilisateurProprietaire> conditions);
	public Long countAll(Specification<UtilisateurProprietaire> conditions);
	
	void enregistrePhotoCarteTransport(UtilisateurProprietaire user, String base64);
	boolean codeParrainageExist(String codeParrainage, String pays);
}
