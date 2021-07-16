package com.kamtar.transport.api.repository;

import com.kamtar.transport.api.model.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

@Repository
public interface MotDePassePerduRepository extends CrudRepository<MotDePassePerdu, String>, JpaSpecificationExecutor<MotDePassePerdu> {

	/**
	 * Recherche par UUID
	 * @param uuid
	 * @return
	 */
	@Query("SELECT b FROM MotDePassePerdu b WHERE b.id= :uuid AND b.codePays = :pays")
	MotDePassePerdu findByUUID(@Param("uuid") UUID uuid, @Param("pays") String pays);

	/**
	 * Est ce que le token est déjà utilisé ?
	 * @param uuid
	 * @return
	 */
	@Query("SELECT CASE  WHEN count(id)> 0 THEN true ELSE false END FROM MotDePassePerdu b WHERE b.token = :token AND b.codePays = :pays")
	Boolean tokenExisteDeja(@Param("token") String token, @Param("pays") String pays);
	
	/**
	 * Recherche par token 
	 * @param uuid
	 * @return
	 */
	@Query("SELECT b FROM MotDePassePerdu b WHERE b.token = :token AND b.codePays = :pays")
	MotDePassePerdu findByToken(@Param("token") String token, @Param("pays") String pays);
	
	/**
	 * Mise à null de l'opérateur pour les enregistrements où l'opérateur est renseigné
	 * @param operateur
	 */
	@Transactional
	@Modifying
	@Query("UPDATE MotDePassePerdu e SET e.operateurKamtar = null WHERE e.operateurKamtar =:operateur AND e.codePays = :pays")
	void setNullOperateur(@Param("operateur") UtilisateurOperateurKamtar operateur, @Param("pays") String pays);

	/**
	 * Mise à null de l'opérateur pour les enregistrements où l'opérateur est renseigné
	 * @param operateur
	 */
	@Transactional
	@Modifying
	@Query("UPDATE MotDePassePerdu e SET e.operateurKamtar = null WHERE e.clientPersonnel =:clientPersonnel AND e.codePays = :pays")
	void setNullClientPersonnel(@Param("clientPersonnel") UtilisateurClientPersonnel clientPersonnel, @Param("pays") String pays);
	
	/**
	 * Mise à null de l'opérateur pour les enregistrements où l'opérateur est renseigné
	 * @param operateur
	 */
	@Transactional
	@Modifying
	@Query("UPDATE MotDePassePerdu e SET e.proprietaire = null WHERE e.proprietaire =:proprietaire AND e.codePays = :pays")
	void setNullProprietaire(@Param("proprietaire") UtilisateurProprietaire proprietaire, @Param("pays") String pays);
	
	/**
	 * Mise à null de l'opérateur pour les enregistrements où l'opérateur est renseigné
	 * @param operateur
	 */
	@Transactional
	@Modifying
	@Query("UPDATE MotDePassePerdu e SET e.transporteur = null WHERE e.transporteur =:transporteur AND e.codePays = :pays")
	void setNullTransporteur(@Param("transporteur") UtilisateurDriver transporteur, @Param("pays") String pays);
	
	/**
	 * Mise à null de l'opérateur pour les enregistrements où l'opérateur est renseigné
	 * @param operateur
	 */
	@Transactional
	@Modifying
	@Query("UPDATE MotDePassePerdu e SET e.client = null WHERE e.client =:client AND e.codePays = :pays")
	void setNullClient(@Param("client") UtilisateurClient client, @Param("pays") String pays);
	
}

