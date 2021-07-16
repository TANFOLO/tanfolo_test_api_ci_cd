package com.kamtar.transport.api.service;



import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.kamtar.transport.api.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.kamtar.transport.api.params.CreateEditOperationAppelOffreParams;
import com.kamtar.transport.api.params.CreateOperationParClientParams;
import com.kamtar.transport.api.params.CreateOperationParams;
import com.kamtar.transport.api.params.EditOperationParams;


@Service
public interface OperationAppelOffreService {

	List<OperationAppelOffre> create(CreateEditOperationAppelOffreParams params, UtilisateurOperateurKamtar operateur, String token);
	List<OperationAppelOffre>  edit(CreateEditOperationAppelOffreParams params, UtilisateurOperateurKamtar operateur, String token);
	public Page<OperationAppelOffre> getOperationsAppelsOffre(String order_by, String order_dir, int page_number, int page_size, Specification<OperationAppelOffre> conditions);
	public List<OperationAppelOffre> getOperationsAppelsOffre(List<Vehicule> vehicules, String code_pays);

	public List<OperationAppelOffre> findByVehiculeAndOperation(Operation operation, List<Vehicule> vehicules, boolean filtre_accepte, boolean filtre_refuse, String code_pays);
	public List<OperationAppelOffre> findByOperation(Operation operation, boolean filtre_accepte, boolean filtre_refuse, String code_pays);
	public List<OperationAppelOffre> findByOperation(Operation operation, String code_pays);

	OperationAppelOffre getByUUID(String uuid, String code_pays);
	OperationAppelOffre save(OperationAppelOffre objet);
	public long countOperationsDriver(UtilisateurDriver driver, String code_pays);
	public long countOperationsPropretaire(UtilisateurProprietaire proprietaire, String code_pays);


	Long compterAppelOffresNonRepondus(List<Vehicule> vehicules, String code_pays);
}
