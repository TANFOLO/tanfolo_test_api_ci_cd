package com.kamtar.transport.api.service;



import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.kamtar.transport.api.enums.UtilisateurTypeDeCompte;
import com.kamtar.transport.api.model.Utilisateur;
import com.kamtar.transport.api.model.UtilisateurAdminKamtar;
import com.kamtar.transport.api.params.CreateAdminKamtarParams;
import com.kamtar.transport.api.params.DeleteAdminKamtarParams;
import com.kamtar.transport.api.params.EditAdminKamtarParams;


@Service
public interface UtilisateurAdminKamtarService {
	
	UtilisateurAdminKamtar createUser(CreateAdminKamtarParams params, String code_pays);
	boolean updateUser(EditAdminKamtarParams params, UtilisateurAdminKamtar user);
	boolean deleteUser(UtilisateurAdminKamtar user);
	boolean emailExist(String email, String code_pays);
	boolean numeroDeTelephoneExist(String email, String pays);
	boolean validateUser(String email, String code);
	UtilisateurAdminKamtar login(String email, String password, String pays);
	UtilisateurAdminKamtar login(String email, String pays);
	UtilisateurAdminKamtar get(String token, String pays);
	UtilisateurAdminKamtar getByUUID(String uuid, String code_pays);
	AttributeType createAttributeType(String name, String value);
	String getJsonWebKey();

	public Page<UtilisateurAdminKamtar> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<UtilisateurAdminKamtar> conditions);
	public Long countAll(Specification<UtilisateurAdminKamtar> conditions);
	
}
