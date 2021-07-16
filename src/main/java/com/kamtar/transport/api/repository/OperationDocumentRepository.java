package com.kamtar.transport.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.kamtar.transport.api.model.Vehicule;
import com.kamtar.transport.api.model.Operation;
import com.kamtar.transport.api.model.OperationDocument;


import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

@Repository
public interface OperationDocumentRepository extends CrudRepository<OperationDocument, UUID>, JpaSpecificationExecutor<OperationDocument> {

	/**
	 * Chargement des photos du véhicule
	 * @param offre
	 * @return
	 */
	@Query("SELECT o FROM OperationDocument o WHERE o.operation = :operation ORDER BY createdOn ASC") 
	List<OperationDocument> findOperationDocuments(@Param("operation") Operation operation);


	/**
	 * Compte le nombre de documents associées à une opération
	 * @param offre
	 * @return
	 */
	@Query("SELECT count(o) FROM OperationDocument o WHERE o.operation = :operation ORDER BY createdOn ASC")
	Long countOperationDocuments(@Param("operation") Operation operation);


	/**
	 * Suppression d'une photo d'une offre
	 * @param offre
	 * @param ordre (pour déduire le nom)
	 * @return
	 */
	@Query("SELECT o FROM OperationDocument o WHERE o.operation = :operation AND o.filename = :filename") 
	Optional<OperationDocument> get(@Param("operation") Operation operation, @Param("filename") String filename);

}

