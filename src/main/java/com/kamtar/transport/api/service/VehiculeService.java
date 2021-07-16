package com.kamtar.transport.api.service;



import java.util.List;

import com.kamtar.transport.api.params.*;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.kamtar.transport.api.model.UtilisateurDriver;
import com.kamtar.transport.api.model.UtilisateurOperateurKamtar;
import com.kamtar.transport.api.model.UtilisateurProprietaire;
import com.kamtar.transport.api.model.Vehicule;


@Service
public interface VehiculeService {
	
	Vehicule create(CreateVehiculeParams params, UtilisateurOperateurKamtar operateur, String token);
	Vehicule create(CreateComptePublicParams postBody1, String code_pays);
	boolean update(EditVehiculeParams params, Vehicule user, String pays);
	boolean update_disponibilite(DisponibiliteVehiculeParams params, Vehicule user, String pays);
	Vehicule getByUUID(String uuid, String pays);
	Vehicule signin(SigninImmatriculationParams params, String pays);

	public Page<Vehicule> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<Vehicule> conditions);
	public Long countAll(Specification<Vehicule> conditions);
	
	public boolean delete(Vehicule vehicule, String pays);
	List<Vehicule> getByProprietaire(UtilisateurProprietaire proprietaire, String pays);
	List<Vehicule> getByCarrosseries(String carrosserie, String pays);

	public boolean immatriculationExist(String code, String pays);
	
	List<Vehicule> autocomplete(String query, String pays);

	void setVehiculesIndispo();

	public Long countOperationsVehiculesDriver(@Param("transporteur") UtilisateurDriver transporteur, String pays);
	public Long countOperationsVehiculesProprietaire(@Param("proprietaire") UtilisateurProprietaire proprietaire, String pays);

}
