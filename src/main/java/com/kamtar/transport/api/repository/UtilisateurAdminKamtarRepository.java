package com.kamtar.transport.api.repository;

import org.springframework.stereotype.Repository;

import com.kamtar.transport.api.model.Utilisateur;
import com.kamtar.transport.api.model.UtilisateurAdminKamtar;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

@Repository
public interface UtilisateurAdminKamtarRepository extends CrudRepository<UtilisateurAdminKamtar, String>, JpaSpecificationExecutor<UtilisateurAdminKamtar> {

	/**
	 * Recherche par UUID (pour remplacer findById de Spring qui ne gère pas les uuid)
	 * @param uuid
	 * @return
	 */
	@Query("SELECT b FROM UtilisateurAdminKamtar b WHERE b.id= :uuid AND b.codePays = :pays")
	UtilisateurAdminKamtar findByUUID(@Param("uuid") UUID uuid, @Param("pays") String pays);
	
	/**
	 * Chargement d'un user par son adresse email
	 */
	@Query("SELECT b FROM UtilisateurAdminKamtar b WHERE b.email = :email AND b.codePays = :pays")
	UtilisateurAdminKamtar findByEmail(@Param("email") String email, @Param("pays") String pays);
	
	/**
	 * Chargement d'un user par son login
	 */
	@Query("SELECT b FROM UtilisateurAdminKamtar b WHERE b.numeroTelephone1 = :numeroTelephone1 AND b.codePays = :pays")
	UtilisateurAdminKamtar findByTelephone1(@Param("numeroTelephone1") String numeroTelephone1, @Param("pays") String pays);
	
	/**
	 * Est ce que le login est déjà utilisé ?
	 */
	@Query("SELECT CASE  WHEN count(id)> 0 THEN true ELSE false END FROM UtilisateurAdminKamtar b WHERE b.numeroTelephone1 = :numeroTelephone1 AND b.codePays = :pays")
	Boolean telephone1Exist(@Param("numeroTelephone1") String numeroTelephone1, @Param("pays") String pays);
	
	/**
	 * Est ce que l'email est déjà utilisé ?
	 */
	@Query("SELECT CASE  WHEN count(id)> 0 THEN true ELSE false END FROM UtilisateurAdminKamtar b WHERE b.email = :email AND b.codePays = :pays")
	Boolean existEmail(@Param("email") String email, @Param("pays") String pays);
	
	/**
	 * Est ce que le numéro de téléphone est déjà utilisé ?
	 */
	@Query("SELECT CASE  WHEN count(id)> 0 THEN true ELSE false END FROM UtilisateurAdminKamtar b WHERE b.numeroTelephone1 = :numero_de_telephone AND b.codePays = :pays")
	Boolean numeroDeTelephoneEmail(@Param("numero_de_telephone") String numero_de_telephone, @Param("pays") String pays);
	
	
}

