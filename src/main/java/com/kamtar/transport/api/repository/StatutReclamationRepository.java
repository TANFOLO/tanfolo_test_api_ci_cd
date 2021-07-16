package com.kamtar.transport.api.repository;


import com.kamtar.transport.api.model.StatutReclamation;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StatutReclamationRepository extends CrudRepository<StatutReclamation, UUID>, JpaSpecificationExecutor<StatutReclamation> {

	// tri par colonne géré par repository (https://stackoverflow.com/questions/25486583/how-to-use-orderby-with-findall-in-spring-data)
	public List<StatutReclamation> findAllByOrderByOrdreAsc();

	/**
	 * Récupère l'ordre d'un statut en recherchant par le nom de statut
	 * @param statut
	 * @return
	 */
	@Query("SELECT ordre FROM StatutReclamation b WHERE b.statut= :statut")
	Integer getOrdre(@Param("statut") String statut);
	

}

