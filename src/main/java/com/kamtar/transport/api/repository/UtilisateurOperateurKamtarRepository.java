package com.kamtar.transport.api.repository;

import org.springframework.stereotype.Repository;

import com.kamtar.transport.api.model.Utilisateur;
import com.kamtar.transport.api.model.UtilisateurOperateurKamtar;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

@Repository
public interface UtilisateurOperateurKamtarRepository extends CrudRepository<UtilisateurOperateurKamtar, String>, JpaSpecificationExecutor<UtilisateurOperateurKamtar> {

	/**
	 * Recherche par UUID
	 * @param uuid
	 * @return
	 */
	@Query("SELECT b FROM UtilisateurOperateurKamtar b WHERE b.id= :uuid AND b.codePays = :pays")
	UtilisateurOperateurKamtar findByUUID(@Param("uuid") UUID uuid, @Param("pays") String pays);
	
	/**
	 * Chargement d'un user par son adresse email
	 */
	@Query("SELECT b FROM UtilisateurOperateurKamtar b WHERE b.email = :email AND b.codePays = :pays")
	UtilisateurOperateurKamtar findByEmail(@Param("email") String email, @Param("pays") String pays);
	
	/**
	 * Chargement d'un user par son téléphone
	 */
	@Query("SELECT b FROM UtilisateurOperateurKamtar b WHERE b.numeroTelephone1 = :numeroTelephone1 AND b.codePays = :pays")
	UtilisateurOperateurKamtar findByTelephone1(@Param("numeroTelephone1") String numeroTelephone1, @Param("pays") String pays);
	
	/**
	 * Est ce que le login est déjà utilisé ?
	 */
	@Query("SELECT CASE  WHEN count(id)> 0 THEN true ELSE false END FROM UtilisateurOperateurKamtar b WHERE b.numeroTelephone1 = :numeroTelephone1 AND b.codePays = :pays")
	Boolean telephone1Exist(@Param("numeroTelephone1") String numeroTelephone1, @Param("pays") String pays);
	
	/**
	 * Est ce que l'email est déjà utilisé ?
	 */
	@Query("SELECT CASE  WHEN count(id)> 0 THEN true ELSE false END FROM UtilisateurOperateurKamtar b WHERE b.email = :email AND b.codePays = :pays")
	Boolean existEmail(@Param("email") String email, @Param("pays") String pays);
	
	/**
	 * Est ce que le numéro de téléphone est déjà utilisé ?
	 */
	@Query("SELECT CASE  WHEN count(id)> 0 THEN true ELSE false END FROM UtilisateurOperateurKamtar b WHERE b.numeroTelephone1 = :numero_de_telephone AND b.codePays = :pays")
	Boolean numeroDeTelephoneEmail(@Param("numero_de_telephone") String numero_de_telephone, @Param("pays") String pays);
}

