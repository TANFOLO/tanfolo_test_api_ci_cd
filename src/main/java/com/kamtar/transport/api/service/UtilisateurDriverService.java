package com.kamtar.transport.api.service;



import java.util.List;

import com.kamtar.transport.api.params.*;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.kamtar.transport.api.model.UtilisateurClient;
import com.kamtar.transport.api.model.UtilisateurDriver;
import com.kamtar.transport.api.model.UtilisateurProprietaire;


@Service
public interface UtilisateurDriverService {
	
	UtilisateurDriver createUser(CreateDriverParams params, String code_pays, UtilisateurProprietaire proprietaire);
	UtilisateurDriver createUser(CreateComptePublicParams params, String code_pays);
	boolean updateUser(EditDriverParams params, UtilisateurDriver user, UtilisateurProprietaire proprietaire);
	boolean updateUser(EditTransporteurPublicParams params, UtilisateurDriver user);


	boolean validateUser(String email, String code);
	UtilisateurDriver get(String token, String pays);
	UtilisateurDriver getByUUID(String uuid, String code_pays);
	AttributeType createAttributeType(String name, String value);
	String getJsonWebKey();
	boolean delete(UtilisateurDriver user, String code_pays);
	List<UtilisateurDriver> autocomplete(String query, String code_pays, List<UtilisateurDriver> drivers_autorises);
	boolean emailExist(String email, String code_pays);
	boolean numeroDeTelephoneExist(String email, String pays);
	public Page<UtilisateurDriver> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<UtilisateurDriver> conditions);
	public Long countAll(Specification<UtilisateurDriver> conditions);

	UtilisateurDriver login(String email, String password, String pays);
	UtilisateurDriver login(String email, String pays);
	
	boolean codeParrainageExist(String codeParrainage, String code_pays);
	List<UtilisateurDriver> getDriversOfProprietaire(UtilisateurProprietaire proprietaire, String code_pays);
}
