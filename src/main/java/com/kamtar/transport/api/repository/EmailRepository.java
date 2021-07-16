package com.kamtar.transport.api.repository;

import org.springframework.stereotype.Repository;

import com.kamtar.transport.api.model.Email;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

@Repository
public interface EmailRepository extends CrudRepository<Email, String>, JpaSpecificationExecutor<Email> {


	/**
	 * Recherche par UUID (pour remplacer findById de Spring qui ne gère pas les uuid)
	 * @param uuid
	 * @return
	 */
	@Query("SELECT b FROM Email b WHERE b.id= :uuid AND b.codePays = :pays")
	Email findByUUID(@Param("uuid") UUID uuid, @Param("pays") String pays);
	
}

