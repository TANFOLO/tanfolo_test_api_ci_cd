package com.kamtar.transport.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kamtar.transport.api.model.*;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

@Repository
public interface VehiculeCarrosserieRepository extends CrudRepository<VehiculeCarrosserie, UUID>, JpaSpecificationExecutor<VehiculeCarrosserie> {
	
	 List<VehiculeCarrosserie> findAll();
	 
	 /**
		 * Est ce que le code est déjà utilisé ?
		 * @param offre
		 * @return
		 */
		@Query("SELECT count(c)>0 FROM VehiculeCarrosserie c WHERE code = :code")
		public boolean codeExist(@Param("code") String code);

	/**
	 * Chargement par le code
	 * @param offre
	 * @return
	 */
	@Query("SELECT l FROM VehiculeCarrosserie l WHERE code = :code")
	public Optional<VehiculeCarrosserie> findByCode(@Param("code") String code);

	/**
	 *
	 * @param nom
	 * @return
	 */
	@Query("SELECT b FROM VehiculeCarrosserie b ORDER BY ordre ASC")
	List<VehiculeCarrosserie> getAllByordreAsc();

	/**
	 *
	 * @param nom
	 * @return
	 */
	@Query("SELECT b FROM VehiculeCarrosserie b WHERE pays LIKE %:codePays% ORDER BY ordre ASC")
	List<VehiculeCarrosserie> getAllByordreAsc(@Param("codePays") String codePays);

}

