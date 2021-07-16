package com.kamtar.transport.api.service;



import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.kamtar.transport.api.enums.UtilisateurTypeDeCompte;
import com.kamtar.transport.api.model.Utilisateur;
import com.kamtar.transport.api.model.UtilisateurOperateurKamtar;
import com.kamtar.transport.api.params.CreateOperateurKamtarParams;
import com.kamtar.transport.api.params.DeleteOperateurKamtarParams;
import com.kamtar.transport.api.params.EditOperateurKamtarParams;


@Service
public interface UtilisateurOperateurKamtarService {
	
	UtilisateurOperateurKamtar createUser(CreateOperateurKamtarParams params, String pays);
	boolean updateUser(EditOperateurKamtarParams params, UtilisateurOperateurKamtar user);
	boolean updateUserCompte(EditOperateurKamtarParams params, UtilisateurOperateurKamtar user);
	boolean emailExist(String email, String pays);
	boolean numeroDeTelephoneExist(String email, String pays);
	boolean validateUser(String email, String code);
	UtilisateurOperateurKamtar login(String email, String password, String pays);
	UtilisateurOperateurKamtar login(String email, String pays);
	UtilisateurOperateurKamtar get(String token, String pays);
	UtilisateurOperateurKamtar getByUUID(String uuid, String pays);
	AttributeType createAttributeType(String name, String value);
	String getJsonWebKey();
	boolean delete(UtilisateurOperateurKamtar user, String code_pays);

	public Page<UtilisateurOperateurKamtar> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<UtilisateurOperateurKamtar> conditions);
	public Long countAll(Specification<UtilisateurOperateurKamtar> conditions);
	
}
