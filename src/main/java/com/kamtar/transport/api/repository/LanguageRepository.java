package com.kamtar.transport.api.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.kamtar.transport.api.model.Language;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

@Repository
public interface LanguageRepository extends CrudRepository<Language, UUID>, JpaSpecificationExecutor<Language> {
	
	/**
	 * Est ce que le code est déjà utilisé ?
	 * @param offre
	 * @return
	 */
	@Query("SELECT count(l)>0 FROM Language l WHERE code = :code")
	public boolean codeExist(@Param("code") String code);
	
	/**
	 * Chargement par le code
	 * @param offre
	 * @return
	 */
	@Query("SELECT l FROM Language l WHERE code = :code")
	public Optional<Language> findByCode(@Param("code") String code);


	
}

