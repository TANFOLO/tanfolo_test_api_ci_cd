package com.kamtar.transport.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.kamtar.transport.api.model.Country;
import com.kamtar.transport.api.model.Language;
import com.kamtar.transport.api.model.MarchandiseType;
import com.kamtar.transport.api.model.VehiculeType;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

@Repository
public interface MarchandiseTypeRepository extends CrudRepository<MarchandiseType, UUID>, JpaSpecificationExecutor<MarchandiseType> {
	
	 List<MarchandiseType> findAll();
	
	 /**
		 * Est ce que le code est déjà utilisé ?
		 * @param offre
		 * @return
		 */
		@Query("SELECT count(c)>0 FROM MarchandiseType c WHERE code = :code")
		public boolean codeExist(@Param("code") String code);
	 
		/**
		 * Chargement par le code
		 * @param offre
		 * @return
		 */
		@Query("SELECT l FROM MarchandiseType l WHERE code = :code")
		public Optional<MarchandiseType> findByCode(@Param("code") String code);
	 
}

