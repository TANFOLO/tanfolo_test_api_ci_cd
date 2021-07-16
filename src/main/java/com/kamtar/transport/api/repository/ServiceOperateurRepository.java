package com.kamtar.transport.api.repository;

import com.kamtar.transport.api.model.Country;
import com.kamtar.transport.api.model.ServiceOperateur;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceOperateurRepository extends CrudRepository<ServiceOperateur, UUID>, JpaSpecificationExecutor<ServiceOperateur> {
	

	/**
	 * Chargement par le code
	 * @param offre
	 * @return
	 */
	@Query("SELECT l FROM ServiceOperateur l WHERE code = :code")
	public Optional<ServiceOperateur> findByCode(@Param("code") String code);


}

