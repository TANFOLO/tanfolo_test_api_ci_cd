package com.kamtar.transport.api.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.kamtar.transport.api.model.Operation;
import com.kamtar.transport.api.model.OperationChangementStatut;
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
public interface OperationChangementStatutRepository extends CrudRepository<OperationChangementStatut, String>, JpaSpecificationExecutor<OperationChangementStatut> {

	/**
	 * Mise à null de l'opérateur pour les enregistrements où l'opérateur est renseigné
	 * @param operateur
	 */
	@Transactional
	@Modifying
	@Query("UPDATE OperationChangementStatut e SET e.modifieParOperateur = null WHERE e.modifieParOperateur = :operateur")
	void setNullOperateur(@Param("operateur") UtilisateurOperateurKamtar operateur);

	/**
	 * Mise à null de transporteur pour les enregistrements où transporteur est renseigné
	 * @param operateur
	 */
	@Transactional
	@Modifying
	@Query("UPDATE OperationChangementStatut e SET e.modifieParTransporteur = null WHERE e.modifieParTransporteur = :transporteur")
	void setNullTransporteur(@Param("transporteur") UtilisateurDriver transporteur);
	
	/**
	 * Mise à null de operation pour les enregistrements où operation est renseigné
	 * @param operateur
	 */
	@Transactional
	@Modifying
	@Query("UPDATE OperationChangementStatut e SET e.operation = null WHERE e.operation = :operation")
	void setNullOperation(@Param("operation") Operation operation);

	/**
	 * Est ce que l'opéation passée en paramètre est passé par le statut passé en paramètre ?
	 * @param operation
	 * @return
	 */
	@Query("SELECT CASE  WHEN count(uuid)> 0 THEN true ELSE false END FROM OperationChangementStatut b WHERE b.operation = :operation AND b.nouveauStatut = :statut")
	Boolean isOperationPasseeParStatut(@Param("operation") Operation operation, @Param("statut") String statut);


}

