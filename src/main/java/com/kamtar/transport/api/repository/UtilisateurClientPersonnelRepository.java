package com.kamtar.transport.api.repository;

import com.kamtar.transport.api.model.Client;
import com.kamtar.transport.api.model.UtilisateurClient;
import com.kamtar.transport.api.model.UtilisateurClientPersonnel;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UtilisateurClientPersonnelRepository extends CrudRepository<UtilisateurClientPersonnel, String>, JpaSpecificationExecutor<UtilisateurClientPersonnel> {

	/**
	 * Recherche par UUID
	 * @param uuid
	 * @return
	 */
	@Query("SELECT b FROM UtilisateurClientPersonnel b WHERE b.uuid= :uuid AND b.codePays = :pays")
	UtilisateurClientPersonnel findByUUID(@Param("uuid") UUID uuid, @Param("pays") String pays);
	
	/**
	 * Chargement d'un user par son adresse email
	 */
	@Query("SELECT b FROM UtilisateurClientPersonnel b WHERE b.email = :email AND b.codePays = :pays")
	UtilisateurClientPersonnel findByEmail(@Param("email") String email, @Param("pays") String pays);
	
	/**
	 * Chargement d'un user par son téléphone
	 */
	@Query("SELECT b FROM UtilisateurClientPersonnel b WHERE b.numeroTelephone1 = :numeroTelephone1 AND b.codePays = :pays")
	UtilisateurClientPersonnel findByTelephone1(@Param("numeroTelephone1") String numeroTelephone1, @Param("pays") String pays);
	
	/**
	 * Est ce que le login est déjà utilisé ?
	 */
	@Query("SELECT CASE  WHEN count(id)> 0 THEN true ELSE false END FROM UtilisateurClientPersonnel b WHERE b.numeroTelephone1 = :numeroTelephone1 AND b.codePays = :pays")
	Boolean telephone1Exist(@Param("numeroTelephone1") String numeroTelephone1, @Param("pays") String pays);
	
	/**
	 * Est ce que l'email est déjà utilisé ?
	 */
	@Query("SELECT CASE  WHEN count(id)> 0 THEN true ELSE false END FROM UtilisateurClientPersonnel b WHERE b.email = :email AND b.codePays = :pays")
	Boolean existEmail(@Param("email") String email, @Param("pays") String pays);
	
	/**
	 * Est ce que le numéro de téléphone est déjà utilisé ?
	 */
	@Query("SELECT CASE  WHEN count(id)> 0 THEN true ELSE false END FROM UtilisateurClientPersonnel b WHERE b.numeroTelephone1 = :numero_de_telephone AND b.codePays = :pays")
	Boolean numeroDeTelephoneEmail(@Param("numero_de_telephone") String numero_de_telephone, @Param("pays") String pays);

	/**
	 * Chargement des clients personnels d'un client
	 */
	@Query("SELECT b FROM UtilisateurClientPersonnel b WHERE b.client = :client AND b.codePays = :pays")
	List<UtilisateurClientPersonnel> getClientsPersonnels(@Param("client") Client client, @Param("pays") String pays);

	/**
	 * Est ce que l'email est déjà utilisé ?
	 */
	@Query("SELECT CASE  WHEN count(id)> 0 THEN true ELSE false END FROM UtilisateurClientPersonnel b WHERE b.email = :email AND b != :user AND b.codePays = :pays")
	Boolean existEmailForotherUser(@Param("email") String email, @Param("user") UtilisateurClientPersonnel user, @Param("pays") String pays);

	/**
	 * Est ce que le login est déjà utilisé ?
	 */
	@Query("SELECT CASE  WHEN count(id)> 0 THEN true ELSE false END FROM UtilisateurClientPersonnel b WHERE b.numeroTelephone1 = :numeroTelephone1 AND b != :user AND b.codePays = :pays")
	Boolean telephone1ExistForOtherUser(@Param("numeroTelephone1") String numeroTelephone1, @Param("user") UtilisateurClientPersonnel user, @Param("pays") String pays);

	/**
	 * Chargement des clients personnels qui recoivent les notifications
	 */
	@Query("SELECT b FROM UtilisateurClientPersonnel b WHERE b.client = :client AND b.codePays = :pays AND substring(b.liste_droits, 6,1) = '1'")
	List<UtilisateurClientPersonnel> getClientsPersonnelsNotifications(@Param("client") Client client, @Param("pays") String pays);

}

