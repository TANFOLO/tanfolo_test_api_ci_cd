package com.kamtar.transport.api.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.kamtar.transport.api.model.Client;
import com.kamtar.transport.api.model.UtilisateurClient;
import com.kamtar.transport.api.model.UtilisateurOperateurKamtar;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

@Repository
public interface ClientRepository extends CrudRepository<Client, String>, JpaSpecificationExecutor<Client> {

	/**
	 * Recherche par UUID (pour remplacer findById de Spring qui ne gère pas les uuid)
	 * @param uuid
	 * @return
	 */
	@Query("SELECT b FROM Client b WHERE b.id = :uuid AND b.codePays = :pays")
	Client findByUUID(@Param("uuid") UUID uuid, @Param("pays") String pays);

	/**
	 * Recherche par nom en like
	 * @param nom
	 * @return
	 */
	@Query("SELECT b FROM Client b WHERE (b.nom LIKE %:nom%) AND b.codePays = :pays ORDER BY nom")
	List<Client> filterByNom(@Param("nom") String nom, @Param("pays") String pays);
	
	/**
	 * Recherche par UUID (pour remplacer findById de Spring qui ne gère pas les uuid)
	 * @param uuid
	 * @return
	 */
	@Query("SELECT b FROM Client b WHERE b.utilisateur= :utilisateur AND b.codePays = :pays")
	Client findByUtilisateur(@Param("utilisateur") UtilisateurClient utilisateur, @Param("pays") String pays);

	/**
	 * Mise à null de l'opérateur pour les enregistrements où l'opérateur est renseigné
	 * @param operateur
	 */
	@Transactional
	@Modifying
	@Query("UPDATE Client e SET e.operateur = null WHERE e.operateur =:operateur AND e.codePays = :pays")
	void setNullOperateur(@Param("operateur") UtilisateurOperateurKamtar operateur, @Param("pays") String pays);

	/**
	 * Est ce que l'email est déjà utilisé ?
	 */
	@Query("SELECT CASE  WHEN count(id)> 0 THEN true ELSE false END FROM Client b WHERE b.contactEmail = :email AND b.codePays = :pays")
	Boolean existEmail(@Param("email") String email, @Param("pays") String pays);

	/**
	 * Est ce que le login est déjà utilisé ?
	 */
	@Query("SELECT CASE  WHEN count(id)> 0 THEN true ELSE false END FROM Client b WHERE b.contactNumeroDeTelephone1 = :numeroTelephone1 AND b.codePays = :pays")
	Boolean telephone1Exist(@Param("numeroTelephone1") String numeroTelephone1, @Param("pays") String pays);

}

