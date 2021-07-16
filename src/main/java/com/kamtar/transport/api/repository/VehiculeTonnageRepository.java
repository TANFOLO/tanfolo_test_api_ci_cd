package com.kamtar.transport.api.repository;

import com.kamtar.transport.api.model.UtilisateurClient;
import com.kamtar.transport.api.model.VehiculeTonnage;
import com.kamtar.transport.api.model.VehiculeType;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehiculeTonnageRepository extends CrudRepository<VehiculeTonnage, UUID>, JpaSpecificationExecutor<VehiculeTonnage> {
	
	 List<VehiculeTonnage> findAll();
	
	 /**
		 * Est ce que le code est déjà utilisé ?
		 * @param offre
		 * @return
		 */
		@Query("SELECT count(c)>0 FROM VehiculeTonnage c WHERE code = :code")
		public boolean codeExist(@Param("code") String code);
	 
		/**
		 * Chargement par le code
		 * @param offre
		 * @return
		 */
		@Query("SELECT l FROM VehiculeTonnage l WHERE code = :code")
		public Optional<VehiculeTonnage> findByCode(@Param("code") String code);

	/**
	 *
	 * @param nom
	 * @return
	 */
	@Query("SELECT b FROM VehiculeTonnage b ORDER BY ordre ASC")
	List<VehiculeTonnage> getAllByOrdreAsc();


}

