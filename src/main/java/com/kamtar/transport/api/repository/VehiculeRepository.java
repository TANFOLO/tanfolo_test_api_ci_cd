package com.kamtar.transport.api.repository;

import com.kamtar.transport.api.model.UtilisateurDriver;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.kamtar.transport.api.model.Client;
import com.kamtar.transport.api.model.UtilisateurProprietaire;
import com.kamtar.transport.api.model.Vehicule;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

@Repository
public interface VehiculeRepository extends CrudRepository<Vehicule, String>, JpaSpecificationExecutor<Vehicule> {

	/**
	 * Recherche par UUID (pour remplacer findById de Spring qui ne gère pas les uuid)
	 * @param uuid
	 * @return
	 */
	@Query("SELECT b FROM Vehicule b WHERE b.id= :uuid AND b.codePays = :pays")
	Vehicule findByUUID(@Param("uuid") UUID uuid, @Param("pays") String pays);
	
	/**
	 * Connexion avec immatriculation
	 * @return
	 */
	@Query("SELECT b FROM Vehicule b WHERE b.immatriculation= :immatriculation AND b.codePays = :pays")
	Vehicule siginin(@Param("immatriculation") String immatriculation, @Param("pays") String pays);
	
	/**
	 * Recherche tous les véhicules d'un propriétaire
	 * @param commande
	 * @return
	 */
	@Query("SELECT b FROM Vehicule b WHERE b.proprietaire = :proprietaire AND b.codePays = :pays")
	List<Vehicule> findByProprietaire(@Param("proprietaire") UtilisateurProprietaire proprietaire, @Param("pays") String pays);

	/**
	 * Recherche tous les véhicules qui ont la carroserie passée en paramètre
	 * @param commande
	 * @return
	 */
	@Query("SELECT b FROM Vehicule b WHERE b.carrosserie = :carrosserie AND b.codePays = :pays")
	List<Vehicule> findByCarrosserie(@Param("carrosserie") String carrosserie, @Param("pays") String pays);

	/**
	 * Est ce que l'immatriculation est déjà utilisé ?
	 * @param offre
	 * @return
	 */
	@Query("SELECT count(c)>0 FROM Vehicule c WHERE c.immatriculation = :immatriculation AND c.codePays = :pays")
	public boolean immatriculationExist(@Param("immatriculation") String immatriculation, @Param("pays") String pays);
	
	/**
	 * Recherche par immatriculation en like
	 * @param nom
	 * @return
	 */
	@Query("SELECT b FROM Vehicule b WHERE b.immatriculation LIKE %:immatriculation% AND b.codePays = :pays ORDER BY immatriculation")
	List<Vehicule> filterByNom(@Param("immatriculation") String immatriculation, @Param("pays") String pays);

	/**
	 * Compter le nombre de véhicule dont le dirver passé en paramètre est le driver principal
	 * @param vehicule
	 * @return
	 */
	@Query("SELECT count(b) FROM Vehicule b WHERE driverPrincipal = :transporteur AND b.codePays = :pays")
	Long countVehiculesDriver(@Param("transporteur") UtilisateurDriver transporteur, @Param("pays") String pays);


	/**
	 * Compter le nombre de véhicule dont le dirver passé en paramètre est le prorpriétaire
	 * @param vehicule
	 * @return
	 */
	@Query("SELECT count(b) FROM Vehicule b WHERE proprietaire = :proprietaire AND b.codePays = :pays")
	Long countVehiculesProprietaire(@Param("proprietaire") UtilisateurProprietaire proprietaire, @Param("pays") String pays);

	/**
	 * Passe tous les véhicules en indispo
	 */
	@Transactional
	@Modifying
	@Query("UPDATE Vehicule e SET e.disponible = 0")
    void setVehiculesIndispo();
}

