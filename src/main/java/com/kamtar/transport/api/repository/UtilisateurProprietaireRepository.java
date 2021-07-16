package com.kamtar.transport.api.repository;

import org.springframework.stereotype.Repository;

import com.kamtar.transport.api.model.Utilisateur;
import com.kamtar.transport.api.model.UtilisateurProprietaire;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

@Repository
public interface UtilisateurProprietaireRepository extends CrudRepository<UtilisateurProprietaire, String>, JpaSpecificationExecutor<UtilisateurProprietaire> {

	/**
	 * Recherche par UUID (pour remplacer findById de Spring qui ne gère pas les uuid)
	 * @param uuid
	 * @return
	 */
	@Query("SELECT b FROM UtilisateurProprietaire b WHERE b.id= :uuid AND b.codePays = :pays")
	UtilisateurProprietaire findByUUID(@Param("uuid") UUID uuid, @Param("pays") String pays);
	
	/**
	 * Chargement d'un user par son adresse email
	 */
	@Query("SELECT b FROM UtilisateurProprietaire b WHERE b.email = :email AND b.codePays = :pays")
	UtilisateurProprietaire findByEmail(@Param("email") String email, @Param("pays") String pays);
	
	/**
	 * Chargement d'un user par son login
	 */
	@Query("SELECT b FROM UtilisateurProprietaire b WHERE b.numeroTelephone1 = :numeroTelephone1 AND b.codePays = :pays")
	UtilisateurProprietaire findByTelephone1(@Param("numeroTelephone1") String numeroTelephone1, @Param("pays") String pays);
	
	/**
	 * Est ce que le login est déjà utilisé ?
	 */
	@Query("SELECT CASE  WHEN count(id)> 0 THEN true ELSE false END FROM UtilisateurProprietaire b WHERE b.numeroTelephone1 = :numeroTelephone1 AND b.codePays = :pays")
	Boolean telephone1Exist(@Param("numeroTelephone1") String numeroTelephone1, @Param("pays") String pays);
	
	/**
	 * Est ce que l'email est déjà utilisé ?
	 */
	@Query("SELECT CASE  WHEN count(id)> 0 THEN true ELSE false END FROM UtilisateurProprietaire b WHERE b.email = :email AND b.codePays = :pays")
	Boolean existEmail(@Param("email") String email, @Param("pays") String pays);
	
	/**
	 * Est ce que le numéro de téléphone est déjà utilisé ?
	 */
	@Query("SELECT CASE  WHEN count(id)> 0 THEN true ELSE false END FROM UtilisateurProprietaire b WHERE b.numeroTelephone1 = :numero_de_telephone AND b.codePays = :pays")
	Boolean numeroDeTelephoneEmail(@Param("numero_de_telephone") String numero_de_telephone, @Param("pays") String pays);
	
	/**
	 * Recherche par nom ou prenom en like
	 * @param nom
	 * @return
	 */
	@Query("SELECT b FROM UtilisateurProprietaire b WHERE (b.nom LIKE %:nom_ou_prenom% OR b.prenom LIKE %:nom_ou_prenom%) AND activate = 1 AND b.codePays = :pays ORDER BY nom")
	List<UtilisateurProprietaire> filterByNom(@Param("nom_ou_prenom") String nom_ou_prenom, @Param("pays") String pays);

	/**
	 * Est ce que le token est déjà utilisé ?
	 * @param uuid
	 * @return
	 */
	@Query("SELECT CASE  WHEN count(id)> 0 THEN true ELSE false END FROM UtilisateurProprietaire b WHERE b.codeParrainage = :codeParrainage AND b.codePays = :pays")
	Boolean codeParrainageExisteDeja(@Param("codeParrainage") String codeParrainage, @Param("pays") String pays);



}

