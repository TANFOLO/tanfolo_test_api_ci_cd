package com.kamtar.transport.api.repository;

import com.kamtar.transport.api.model.*;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReclamationRepository extends CrudRepository<Reclamation, String>, JpaSpecificationExecutor<Reclamation> {

	/**
	 * Recherche par UUID (pour remplacer findById de Spring qui ne gère pas les uuid)
	 * @param uuid
	 * @return
	 */
	@Query("SELECT b FROM Reclamation b WHERE b.id= :uuid AND b.codePays = :pays")
	Reclamation findByUUID(@Param("uuid") UUID uuid, @Param("pays") String pays);
	
	/**
	 * Recherche la valeur max du code de la réclamation
	 * @param uuid
	 * @return
	 */
	@Query("SELECT MAX(code) FROM Reclamation b")
	Long getMaxCode();


	/**
	 * Recherche par code en like
	 * @param nom
	 * @return
	 */
	@Query("SELECT b FROM Reclamation b WHERE b.code = :code AND b.codePays = :pays")
	Reclamation findByCode(@Param("code") Long code, @Param("pays") String pays);
}

