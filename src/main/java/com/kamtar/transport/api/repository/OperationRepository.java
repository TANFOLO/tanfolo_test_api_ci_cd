package com.kamtar.transport.api.repository;

import com.kamtar.transport.api.model.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

@Repository
public interface OperationRepository extends CrudRepository<Operation, String>, JpaSpecificationExecutor<Operation> {

	/**
	 * Recherche par UUID (pour remplacer findById de Spring qui ne gère pas les uuid)
	 * @param uuid
	 * @return
	 */
	@Query("SELECT b FROM Operation b WHERE b.id= :uuid AND b.codePays = :pays")
	Operation findByUUID(@Param("uuid") UUID uuid, @Param("pays") String pays);
	
	/**
	 * Recherche la valeur max du code de la operation
	 * @param uuid
	 * @return
	 */
	@Query("SELECT MAX(code) FROM Operation b")
	Long getMaxCode();


	/**
	 * Recherche les operations associés à un numéro de facture
	 * @param numeroFacture
	 * @return
	 */
	@Query("SELECT b FROM Operation b WHERE facture = :numeroFacture AND b.codePays = :pays")
	List<Operation> getOperationsNumeroFactureClient(@Param("numeroFacture") String numeroFacture, @Param("pays") String pays);


	/**
	 * Recherche les operations associés à un numéro de facture
	 * @param numeroFacture
	 * @return
	 */
	@Query("SELECT b FROM Operation b WHERE factureProprietaire = :numeroFacture AND b.codePays = :pays")
	List<Operation> getOperationsNumeroFactureProprietaire(@Param("numeroFacture") String numeroFacture, @Param("pays") String pays);


	/**
	 * Recherche les operations affectés à un transporteur groupés par jour
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT b FROM Operation b WHERE (b.statut IN :statuts_transporteur AND b.transporteur = :transporteur) AND b.codePays = :pays ORDER BY departDateProgrammeeOperation ASC")
	List<Operation> getOperationsTransporteurParJour(@Param("transporteur") UtilisateurDriver transporteur, @Param("statuts_transporteur") List<String> statuts_transporteur, @Param("pays") String pays);
	
	/**
	 * Recherche des operations à partir d'une list d'id
	 * @param uuids
	 * @return
	 */
	@Query("SELECT b FROM Operation b WHERE b.uuid in :ids AND b.codePays = :pays ORDER BY departDateProgrammeeOperation ASC")
	List<Operation> findByUuidIn(@Param("ids") List<UUID> uuids, @Param("pays") String pays);

	/**
	 * Recherche des operations à partir d'une list de codes
	 * @param uuids
	 * @return
	 */
	@Query("SELECT b FROM Operation b WHERE b.code in :codes AND b.codePays = :pays ORDER BY departDateProgrammeeOperation ASC")
	List<Operation> findByCodeIn(@Param("codes") List<Long> codes, @Param("pays") String pays);

	
	/**
	 * Recherche les operations affectés à un transporteur groupés pour un jour
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT b FROM Operation b WHERE b.vehicule IN :vehicules AND statut NOT IN('ENREGISTRE', 'EN_COURS_DE_TRAITEMENT', 'DECHARGEMENT_TERMINE') AND annulationDate = null AND b.codePays = :pays ORDER BY departDateProgrammeeOperation DESC")
	List<Operation> getOperationsNonTermines(@Param("vehicules") List<Vehicule> vehicules, @Param("pays") String pays);

	/**
	 * Recherche les operations affectés à un transporteur groupés pour un jour
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT b FROM Operation b WHERE b.vehicule IN :vehicules AND b.transporteur = :transporteur AND statut NOT IN('ENREGISTRE', 'EN_COURS_DE_TRAITEMENT', 'DECHARGEMENT_TERMINE') AND annulationDate = null AND b.codePays = :pays ORDER BY departDateProgrammeeOperation DESC")
	List<Operation> getOperationsNonTermines(@Param("vehicules") List<Vehicule> vehicules, @Param("transporteur") UtilisateurDriver transporteur, @Param("pays") String pays);


	/**
	 * Recherche les operations à venir
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT b FROM Operation b WHERE b.vehicule IN :vehicules AND statut IN('VALIDE') AND b.codePays = :pays ORDER BY departDateProgrammeeOperation DESC")
	List<Operation> getOperationsAVenir(@Param("vehicules") List<Vehicule> vehicules, @Param("pays") String pays);

	/**
	 * Recherche les operations à venir
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT b FROM Operation b WHERE b.vehicule IN :vehicules AND b.transporteur = :transporteur AND statut IN('VALIDE') AND b.codePays = :pays ORDER BY departDateProgrammeeOperation DESC")
	List<Operation> getOperationsAVenir(@Param("vehicules") List<Vehicule> vehicules, @Param("transporteur") UtilisateurDriver transporteur, @Param("pays") String pays);

	/**
	 * Recherche les operations en cours
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT b FROM Operation b WHERE b.vehicule IN :vehicules AND statut NOT IN('ENREGISTRE', 'EN_COURS_DE_TRAITEMENT', 'VALIDE', 'DECHARGEMENT_TERMINE') AND annulationDate = null AND b.codePays = :pays ORDER BY departDateProgrammeeOperation DESC")
	List<Operation> getOperationsEnCours(@Param("vehicules") List<Vehicule> vehicules, @Param("pays") String pays);

	/**
	 * Recherche les operations à noter
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT b FROM Operation b WHERE b.vehicule IN :vehicules AND statut IN('DECHARGEMENT_TERMINE') AND b.codePays = :pays ORDER BY departDateProgrammeeOperation DESC")
	List<Operation> getOperationsTermineesParDriver(@Param("vehicules") List<Vehicule> vehicules, @Param("pays") String pays);


	/**
	 * Recherche les operations en cours
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT b FROM Operation b WHERE b.vehicule = :vehicules AND b.transporteur = :transporteur AND statut NOT IN('ENREGISTRE', 'EN_COURS_DE_TRAITEMENT', 'VALIDE', 'DECHARGEMENT_TERMINE') AND annulationDate = null AND b.codePays = :pays ORDER BY departDateProgrammeeOperation DESC")
	List<Operation> getOperationsEnCours(@Param("vehicules") List<Vehicule> vehicules, @Param("transporteur") UtilisateurDriver transporteur, @Param("pays") String pays);

	/**
	 * Recherche les operations en cours
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT b FROM Operation b WHERE b.vehicule = :vehicules AND b.transporteur = :transporteur AND statut IN('DECHARGEMENT_TERMINE') AND b.codePays = :pays ORDER BY departDateProgrammeeOperation DESC")
	List<Operation> getOperationsTermineesParDriver(@Param("vehicules") List<Vehicule> vehicules, @Param("transporteur") UtilisateurDriver transporteur, @Param("pays") String pays);



	/**
	 * Recherche les operations affectés à un transporteur groupés pour un jour
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT b FROM Operation b WHERE(b.vehicule IN :vehicules AND departDateProgrammeeOperation >= :journee_debut AND departDateProgrammeeOperation < :journee_fin) AND statut NOT IN('ENREGISTRE', 'EN_COURS_DE_TRAITEMENT', 'DECHARGEMENT_TERMINE') AND annulationDate = null AND b.codePays = :pays ORDER BY departDateProgrammeeOperation ASC")
	List<Operation> getOperationsDriverJour(@Param("vehicules") List<Vehicule> vehicules, @Param("journee_debut") Date journee_debut, @Param("journee_fin") Date journee_fin, @Param("pays") String pays);
	
	/**
	 * Recherche par code en like
	 * @param nom
	 * @return
	 */
	@Query("SELECT b FROM Operation b WHERE b.code = :code AND b.codePays = :pays")
	Operation findByCode(@Param("code") Long code, @Param("pays") String pays);
	
	/**
	 * Compte le nombre de operations qui sont transportées par le transporteur passé en paramètre
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT count(c) FROM Operation c WHERE c.transporteur = :transporteur AND c.codePays = :pays")
	public long countOperationsTransporteur(@Param("transporteur") UtilisateurDriver transporteur, @Param("pays") String pays);

	/**
	 * Compte le nombre de operations qui référencent un véhicule
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT count(c) FROM Operation c WHERE c.vehicule = :vehicule AND c.codePays = :pays")
	public long countOperationAvecVehicule(@Param("vehicule") Vehicule vehicule, @Param("pays") String pays);

	/**
	 * Mise à null de l'opérateur pour les enregistrements où l'opérateur est renseigné
	 * @param operateur
	 */
	@Transactional
	@Modifying
	@Query("UPDATE Operation e SET e.operateur = null WHERE e.operateur = :operateur AND e.codePays = :pays")
	void setNullOperateur(@Param("operateur") UtilisateurOperateurKamtar operateur, @Param("pays") String pays);

	/**
	 * Mise à null du client personnel pour les enregistrements où le client personnel est renseigné
	 * @param operateur
	 */
	@Transactional
	@Modifying
	@Query("UPDATE Operation e SET e.client_personnel = null WHERE e.client_personnel = :client_personnel AND e.codePays = :pays")
	void setNullClientPersonnel(@Param("client_personnel") UtilisateurClientPersonnel client_personnel, @Param("pays") String pays);

	/**
	 * Compte le nombre d'opérations qui sont programmées
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT count(c) FROM Operation c WHERE c.client = :client AND valideParOperateur = true AND statut = 'VALIDE' AND c.codePays = :pays")
	public long countNbOperationsProgrammees(@Param("client") Client client, @Param("pays") String pays);

	/**
	 * Compte le nombre d'opérations qui sont programmées
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT count(c) FROM Operation c WHERE c.client = :client AND valideParOperateur = true AND statut = 'VALIDE' AND c.client_personnel IS NULL AND c.codePays = :pays")
	public long countNbOperationsProgrammeesUtilisateur(@Param("client") Client client, @Param("pays") String pays);

	/**
	 * Compte le nombre d'opérations qui sont programmées
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT count(c) FROM Operation c WHERE c.client_personnel = :client_personnel AND valideParOperateur = true AND statut = 'VALIDE' AND c.codePays = :pays")
	public long countNbOperationsProgrammees(@Param("client_personnel") UtilisateurClientPersonnel client_personnel, @Param("pays") String pays);

	/**
	 * Compte le nombre d'opérations en cours
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT count(c) FROM Operation c WHERE c.client = :client AND statut NOT IN ('ENREGISTRE', 'EN_COURS_DE_TRAITEMENT', 'VALIDE', 'DECHARGEMENT_TERMINE') AND annulationDate = null AND c.codePays = :pays")
	public long countNbOperationsEnCours(@Param("client") Client client, @Param("pays") String pays);

	/**
	 * Compte le nombre d'opérations en cours
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT count(c) FROM Operation c WHERE c.client = :client AND statut NOT IN ('ENREGISTRE', 'EN_COURS_DE_TRAITEMENT', 'VALIDE', 'DECHARGEMENT_TERMINE') AND annulationDate = null AND c.client_personnel IS NULL AND c.codePays = :pays")
	public long countNbOperationsEnCoursUtilisateur(@Param("client") Client client, @Param("pays") String pays);

	/**
	 * Compte le nombre d'opérations terminées pour un client
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT count(c) FROM Operation c WHERE c.client = :client AND statut IN ('DECHARGEMENT_TERMINE') AND annulationDate = null AND c.codePays = :pays")
	public long countNbOperationsTerminees(@Param("client") Client client, @Param("pays") String pays);

	/**
	 * Compte le nombre d'opérations terminées pour un client
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT count(c) FROM Operation c WHERE c.client = :client AND statut IN ('DECHARGEMENT_TERMINE') AND annulationDate = null AND c.client_personnel IS NULL AND c.codePays = :pays")
	public long countNbOperationsTermineesUtilisateur(@Param("client") Client client, @Param("pays") String pays);


	/**
	 * Compte le nombre d'opérations en cours
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT count(c) FROM Operation c WHERE c.client_personnel = :client_personnel AND statut NOT IN ('ENREGISTRE', 'EN_COURS_DE_TRAITEMENT', 'VALIDE', 'DECHARGEMENT_TERMINE') AND annulationDate = null AND c.codePays = :pays")
	public long countNbOperationsEnCours(@Param("client_personnel") UtilisateurClientPersonnel client_personnel, @Param("pays") String pays);

	/**
	 * Compte le nombre d'opérations terminées
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT count(c) FROM Operation c WHERE c.client_personnel = :client_personnel AND statut IN ('DECHARGEMENT_TERMINE') AND annulationDate = null AND c.codePays = :pays")
	public long countNbOperationsTerminees(@Param("client_personnel") UtilisateurClientPersonnel client_personnel, @Param("pays") String pays);


	/**
	 * Compte le nombre d'opérations terminées pour un utilisateur client
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT count(c) FROM Operation c WHERE c.client_personnel = :client_personnel AND statut IN ('DECHARGEMENT_TERMINE') AND annulationDate = null AND c.codePays = :pays")
	long countNbOperationsTerminees(@Param("client_personnel") UtilisateurClient client, @Param("pays") String code_pays);

	/**
	 * Compte le nombre d'opérations à venir pour véhicule
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT count(c) FROM Operation c WHERE c.vehicule IN :vehicules AND statut IN ('VALIDE') AND c.codePays = :pays AND c.transporteur = :driver")
	public long countNbOperationsAVenir(@Param("vehicules") List<Vehicule> vehicules, @Param("pays") String pays, @Param("driver") UtilisateurDriver driver);

	/**
	 * Compte le nombre d'opérations à venir pour véhicule
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT count(c) FROM Operation c WHERE c.vehicule IN :vehicules AND statut IN ('VALIDE') AND c.codePays = :pays")
	public long countNbOperationsAVenir(@Param("vehicules") List<Vehicule> vehicules, @Param("pays") String pays);


	/**
	 * Compte le nombre d'opérations en cours pour véhicule
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT count(c) FROM Operation c WHERE c.vehicule IN :vehicules AND statut NOT IN ('ENREGISTRE', 'EN_COURS_DE_TRAITEMENT', 'VALIDE', 'DECHARGEMENT_TERMINE') AND annulationDate = null AND c.codePays = :pays")
	public long countNbOperationsEnCours(@Param("vehicules") List<Vehicule> vehicules, @Param("pays") String pays);

	/**
	 * Compte le nombre d'opérations en cours pour véhicule
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT count(c) FROM Operation c WHERE c.vehicule IN :vehicules AND statut NOT IN ('ENREGISTRE', 'EN_COURS_DE_TRAITEMENT', 'VALIDE', 'DECHARGEMENT_TERMINE') AND annulationDate = null AND c.transporteur = :driver AND c.codePays = :pays")
	public long countNbOperationsEnCours(@Param("vehicules") List<Vehicule> vehicules, @Param("pays") String pays, @Param("driver") UtilisateurDriver driver);


	/**
	 * Compte le nombre de operations qui sont attachés à un client
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT count(c) FROM Operation c WHERE c.client = :client AND c.codePays = :pays")
	public long countOperationsClient(@Param("client") Client client, @Param("pays") String pays);

	/**
	 * Compte le nombre de operations qui sont attachés à un driver
	 * @param transporteur
	 * @return
	 */
	@Query("SELECT count(c) FROM Operation c WHERE c.transporteur = :driver AND c.codePays = :pays")
	public long countOperationsDriver(@Param("driver") UtilisateurDriver driver, @Param("pays") String pays);

	@Query("SELECT c FROM Operation c WHERE c.statut = 'VALIDE' AND departDateProgrammeeOperation >= :date_depart")
	public  List<Operation> findOperationsValideesDansPlusDe24h(@Param("date_depart") Date date_depart);

	@Query("SELECT c FROM Operation c WHERE c.statut = 'VALIDE' AND departDateProgrammeeOperation > :date_now AND departDateProgrammeeOperation <= :date_depart")
	public  List<Operation> findOperationsValideesDansMoinsDe24h(@Param("date_now") Date date_now, @Param("date_depart") Date date_depart);

	@Query("SELECT c FROM Operation c WHERE c.client = :client AND c.codePays = :pays AND statut not in ('ENREGISTRE', 'EN_COURS_DE_TRAITEMENT','VALIDE')")
	public  List<Operation> findByClient(@Param("client") Client client, @Param("pays") String pays);

	@Query("SELECT c FROM Operation c WHERE recurrenceProchain > :date_now AND recurrenceProchain <= :date_depart")
	public  List<Operation> findOperationsRecurrenceProchain(@Param("date_now") Date date_now, @Param("date_depart") Date date_depart);

	@Query("SELECT c FROM Operation c WHERE annulationDate = null AND (statut not in ('DECHARGEMENT_TERMINE') OR (dateHeureDechargementTermine >= :journee_debut AND dateHeureDechargementTermine < :journee_fin)) ORDER BY code ASC")
	List<Operation> findOperationsPasTermineesEtTermineesAujourdui(@Param("journee_debut") Date journee_debut, @Param("journee_fin") Date journee_fin);
}

