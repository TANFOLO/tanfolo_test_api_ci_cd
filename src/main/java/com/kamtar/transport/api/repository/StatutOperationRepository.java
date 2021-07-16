package com.kamtar.transport.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.kamtar.transport.api.model.StatutOperation;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

@Repository
public interface StatutOperationRepository extends CrudRepository<StatutOperation, UUID>, JpaSpecificationExecutor<StatutOperation> {

	// tri par colonne géré par repository (https://stackoverflow.com/questions/25486583/how-to-use-orderby-with-findall-in-spring-data)
	public List<StatutOperation> findAllByOrderByOrdreAsc();

	/**
	 * Récupère l'ordre d'un statut en recherchant par le nom de statut
	 * @param statut
	 * @return
	 */
	@Query("SELECT ordre FROM StatutOperation b WHERE b.statut= :statut")
	Integer getOrdre(@Param("statut") String statut);
	

}

