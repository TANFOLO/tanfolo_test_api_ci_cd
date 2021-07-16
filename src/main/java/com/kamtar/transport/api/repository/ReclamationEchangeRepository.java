package com.kamtar.transport.api.repository;

import com.kamtar.transport.api.model.Operation;
import com.kamtar.transport.api.model.Reclamation;
import com.kamtar.transport.api.model.ReclamationEchange;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReclamationEchangeRepository extends CrudRepository<ReclamationEchange, String>, JpaSpecificationExecutor<ReclamationEchange> {

	/**
	 * Recherche par UUID (pour remplacer findById de Spring qui ne gère pas les uuid)
	 * @param uuid
	 * @return
	 */
	@Query("SELECT b FROM ReclamationEchange b WHERE b.id= :uuid AND b.codePays = :pays")
	ReclamationEchange findByUUID(@Param("uuid") UUID uuid, @Param("pays") String pays);

	/**
	 * Recherche la valeur max du code de la réclamation
	 * @param uuid
	 * @return
	 */
	@Query("SELECT MAX(code) FROM ReclamationEchange b")
	Long getMaxCode();

	/**
	 * Chargement de tous les échangés liés à une réclamation
 	 * @param reclamation
	 * @param pays
	 * @return
	 */
	@Query("SELECT b FROM ReclamationEchange b WHERE reclamation = :reclamation AND b.codePays = :pays")
	List<ReclamationEchange> get(@Param("reclamation") Reclamation reclamation, @Param("pays") String pays);

}

