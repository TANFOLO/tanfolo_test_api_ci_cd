package com.kamtar.transport.api.service;


import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.kamtar.transport.api.model.Client;
import com.kamtar.transport.api.model.UtilisateurClient;
import com.kamtar.transport.api.model.UtilisateurClientPersonnel;
import com.kamtar.transport.api.model.UtilisateurOperateurKamtar;
import com.kamtar.transport.api.params.*;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public interface UtilisateurClientPersonnelService {
	
	UtilisateurClientPersonnel createUser(CreateClientPersonnelParams params, String pays, Client client);
	boolean updateUser(EditClientPersonnelParams params, UtilisateurClientPersonnel user, String code_pays);
	void updateUser(EditClientPersonnelPublicParams params, UtilisateurClientPersonnel user, String code_pays);
	boolean emailExist(String email, String pays);
	boolean numeroDeTelephoneExist(String email, String pays);
	boolean validateUser(String email, String code);
	UtilisateurClientPersonnel getByUtilisateur(UtilisateurClientPersonnel utilisateur, String pays);
	UtilisateurClientPersonnel login(String email, String password, String pays);
	UtilisateurClientPersonnel login(String email, String pays);
	UtilisateurClientPersonnel get(String token, String pays);
	UtilisateurClientPersonnel getByUUID(String uuid, String pays);
	AttributeType createAttributeType(String name, String value);
	String getJsonWebKey();
	boolean delete(UtilisateurClientPersonnel user, String code_pays);
	List<UtilisateurClientPersonnel> getClientsPersonnels(Client client, String code_pays);

	public Page<UtilisateurClientPersonnel> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<UtilisateurClientPersonnel> conditions);
	public Long countAll(Specification<UtilisateurClientPersonnel> conditions);
	public List<UtilisateurClientPersonnel> getAll();
}
