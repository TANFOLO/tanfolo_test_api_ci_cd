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
public interface OperationAppelOffreRepository extends CrudRepository<OperationAppelOffre, String>, JpaSpecificationExecutor<OperationAppelOffre> {

	/**
	 * Recherche par UUID (pour remplacer findById de Spring qui ne gère pas les uuid)
	 * @param uuid
	 * @return
	 */
	@Query("SELECT b FROM OperationAppelOffre b WHERE b.id= :uuid AND b.codePays = :pays")
	OperationAppelOffre findByUUID(@Param("uuid") UUID uuid, @Param("pays") String pays);
	
	
	/**
	 * Recherche par opération et driver
	 * @param uuid
	 * @return
	 */
	@Query("SELECT b FROM OperationAppelOffre b WHERE operation = :operation AND vehicule = :vehicule AND b.codePays = :pays")
	OperationAppelOffre findByOperationEtVehicule(@Param("operation") Operation operation, @Param("vehicule") Vehicule vehicule, @Param("pays") String pays);
	
	/**
	 * Recherche des appels d'offre filtré par opération et une liste d'idée de véhicule
	 * @param uuids
	 * @return
	 */
	@Query("SELECT b FROM OperationAppelOffre b WHERE b.operation = :operation AND b.vehicule in :vehicules AND b.codePays = :pays")
	List<OperationAppelOffre> findByVehiculeAndOperation(@Param("operation") Operation operation, @Param("vehicules") List<Vehicule> vehicules, @Param("pays") String pays);

	/**
	 * Recherche des appels d'offre filtré par opération et une liste d'idée de véhicule
	 * @param uuids
	 * @return
	 */
	@Query("SELECT b FROM OperationAppelOffre b WHERE b.operation = :operation AND b.codePays = :pays")
	List<OperationAppelOffre> findByOperation(@Param("operation") Operation operation, @Param("pays") String pays);


	/**
	 * Recherche des appels d'offre acceptes & filtré par opération et une liste d'idée de véhicule
	 * @param uuids
	 * @return
	 */
	@Query("SELECT b FROM OperationAppelOffre b WHERE b.statut = :statut AND b.operation = :operation AND b.vehicule in :vehicules AND b.codePays = :pays")
	List<OperationAppelOffre> findByVehiculeAndOperationAvecStatut(@Param("operation") Operation operation,@Param("vehicules") List<Vehicule> vehicules, @Param("statut") String statut, @Param("pays") String pays);

	/**
	 * Recherche des appels d'offre acceptes & filtré par opération et une liste d'idée de véhicule
	 * @param uuids
	 * @return
	 */
	@Query("SELECT b FROM OperationAppelOffre b WHERE b.statut = :statut AND b.operation = :operation AND b.codePays = :pays")
	List<OperationAppelOffre> findByOperationAvecStatut(@Param("operation") Operation operation, @Param("statut") String statut, @Param("pays") String pays);


	/**
	 * Compter le nombre d'appel d'offre attachés à un véhicule, qui n'ont pas encore été répondus
	 * @param vehicule
	 * @return
	 */
	@Query("SELECT count(b) FROM OperationAppelOffre b INNER JOIN Operation o ON b.operation = o.uuid  WHERE b.vehicule in :vehicules AND b.statut IS NULL AND (o.statut = 'ENREGISTRE' OR o.statut = 'EN_COURS_DE_TRAITEMENT') AND o.vehicule IS NULL AND b.codePays = :pays")
	Long compterAppelOffresNonRepondus(@Param("vehicules") List<Vehicule> vehicules, @Param("pays") String pays);

	/**
	 * Recherche des appels d'offre filtré par opération et une liste d'idée de véhicule
	 * @param uuids
	 * @return
	 */
	@Query("SELECT b FROM OperationAppelOffre b INNER JOIN Operation o ON b.operation = o.uuid AND b.vehicule in :vehicules AND b.statut IS NULL AND (o.statut = 'ENREGISTRE' OR o.statut = 'EN_COURS_DE_TRAITEMENT') AND o.vehicule IS NULL AND b.codePays = :pays")
	List<OperationAppelOffre> getOperationsAppelsOffre(@Param("vehicules") List<Vehicule> vehicules, @Param("pays") String pays);

	/**
	 * Compter le nombre d'appel d'offre attachés à un driver
	 * @param vehicule
	 * @return
	 */
	@Query("SELECT count(b) FROM OperationAppelOffre b WHERE transporteur = :transporteur AND b.codePays = :pays")
	Long countOperationsDriver(@Param("transporteur") UtilisateurDriver transporteur, @Param("pays") String pays);

	/**
	 * Compter le nombre d'appel d'offre attachés à un propriétaire
	 * @param vehicule
	 * @return
	 */
	@Query("SELECT count(b) FROM OperationAppelOffre b WHERE proprietaire = :proprietaire AND b.codePays = :pays")
	Long countOperationsProprietaire(@Param("proprietaire") UtilisateurProprietaire proprietaire, @Param("pays") String pays);




}

