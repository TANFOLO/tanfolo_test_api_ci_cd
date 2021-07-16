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

import com.kamtar.transport.api.params.CreateOperationParClientParams;
import com.kamtar.transport.api.params.CreateOperationParams;
import com.kamtar.transport.api.params.EditOperationParams;


@Service
public interface OperationService { 
	
	Operation create(CreateOperationParams params, UtilisateurOperateurKamtar operateur, String token);
	List<Operation> create(CreateOperationParClientParams params, UtilisateurClient operateur, UtilisateurClientPersonnel utilisateur_client_personnel, Client client, String token);
	boolean update(EditOperationParams params, Operation user, String pays, String token);
	boolean delete(Operation operation);
	Operation dupliquer(Operation operation);
	Operation save(Operation operation);
	Operation getByUUID(String uuid, String code_pays);
	Operation getByCode(Long code, String code_pays);

	public Page<Operation> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<Operation> conditions);
	public Long countAll(Specification<Operation> conditions);
	
	public Page<Operation> getOperationsTransporteur(String order_by, String order_dir, int page_number, int page_size, Specification<Operation> conditions, UtilisateurDriver transporteur);
	public Page<Operation> getOperationsClient(String order_by, String order_dir, int page_number, int page_size, Specification<Operation> conditions, Client client);
	public List<Operation> getOperationsClient(Client client, String code_pays);

	public HashMap<String, List<Operation>> getOperationsTransporteurParJour(String order_dir, int page_number, int page_size, Specification<Operation> conditions, UtilisateurDriver transporteur, String code_pays);
	public List<Operation> getOperationsTransporteurJour(String order_dir, int page_number, int page_size, Specification<Operation> conditions, List<Vehicule> vehicules, String journee, String code_pays);
	public List<Operation> getOperationsTransporteurEnCours(String order_dir, int page_number, int page_size, UtilisateurDriver transporteur , UtilisateurProprietaire proprietaire, List<Vehicule> vehicules, boolean uniquement_a_venir, boolean uniquement_en_cours, boolean uniquement_terminees, String code_pays);

	public long countOperationsClient(Client client, String code_pays);
	public long countOperationsDriver(UtilisateurDriver driver, String code_pays);
	
	List<Operation> getByIds(String[] uuids, String code_pays);
	List<Operation> getByCodes(Long[] codes, String code_pays);
	List<Operation> getOperationsValideesDepartDansPlusDe24h();
	List<Operation> getOperationsValideesDepartDansMoinsDe24h();
	List<Operation> getOperationsRecurrenceProchainDemainAvenir(Date date_reference, int nb_jour_dans_futur);
	
	public Operation autocomplete(String query, String code_pays);
	
	public long countNbOperationsProgrammees(Client client, String code_pays);
	public long countNbOperationsEnCours(Client client, String code_pays);
	public long countNbOperationsProgrammees(UtilisateurClientPersonnel client, String code_pays);
	public long countNbOperationsTerminees(Client client, String code_pays);
	public long countNbOperationsEnCours(UtilisateurClientPersonnel client, String code_pays);
	public long countNbOperationsTerminees(UtilisateurClientPersonnel client, String code_pays);

	public long countNbOperationsProgrammees(List<Vehicule> vehicules, String code_pays);
	public long countNbOperationsProgrammees(List<Vehicule> vehicules, String code_pays, UtilisateurDriver transporteur);
	public long countNbOperationsEnCours(List<Vehicule> vehicules, String code_pays);
	public long countNbOperationsEnCours(List<Vehicule> vehicules, String code_pays, UtilisateurDriver transporteur);

	public List<Operation> getOperationsNumeroFactureClient(String numeroFacture, String code_pays);
	public List<Operation> getOperationsNumeroFactureProprietaire(String numeroFacture, String code_pays);
	public void setNullOperationsNumeroFactureClient(String numeroFacture, String code_pays);
	public void setNullOperationsNumeroFactureProprietaire(String numeroFacture, String code_pays);

	public void refreshPredictifEveryDay();
	public void refreshPredictifEveryHour();
	public void creerOperationsRecurrentes(Date date_reference, int nb_jour_dans_futur);

	List<Geoloc> findGeolocsOperation(String immatriculation, String pays, Operation operation);
	public Operation dupliquerOperationReccurente(Operation operation, Date date_programmee_operation);
	public Operation dupliquerOperationReccurenteInterne(Operation operation, Date date_programmee_operation);

	public void dupliquerOperationDocuments(Operation cloned);

	public void listeOperationsRapportJournalier(Date date);
	public void listeOperationsRapportMensuel(Date date);
}
