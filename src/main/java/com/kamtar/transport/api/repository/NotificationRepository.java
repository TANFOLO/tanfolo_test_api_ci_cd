package com.kamtar.transport.api.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.kamtar.transport.api.model.Client;
import com.kamtar.transport.api.model.Notification;
import com.kamtar.transport.api.model.UtilisateurOperateurKamtar;
import com.kamtar.transport.api.model.UtilisateurDriver;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, String>, JpaSpecificationExecutor<Notification> {

	/**
	 * Compte le nombre notification non lues
	 * @param offre
	 * @return
	 */
	@Query("SELECT count(c) FROM Notification c WHERE c.dateIndiqueeTraitee IS NULL AND type = :type AND c.codePays = :pays")
	public long countNotificationsNonLues(@Param("type") String type, @Param("pays") String pays);
	
	/**
	 * Recherche par UUID (pour remplacer findById de Spring qui ne gère pas les uuid)
	 * @param uuid
	 * @return
	 */
	@Query("SELECT b FROM Notification b WHERE b.id= :uuid AND b.codePays = :pays")
	Notification findByUUID(@Param("uuid") UUID uuid, @Param("pays") String pays);
	
	/**
	 * Mise à null de l'opérateur pour les enregistrements où l'opérateur est renseigné
	 * @param operateur
	 */
	@Transactional
	@Modifying
	@Query("UPDATE Notification e SET e.operateur = null WHERE e.operateur =:operateur AND e.codePays = :pays")
	void setNullOperateur(@Param("operateur") UtilisateurOperateurKamtar operateur, @Param("pays") String pays);
	
}

