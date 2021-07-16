package com.kamtar.transport.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kamtar.transport.api.model.Geoloc;
import org.springframework.stereotype.Repository;

import com.kamtar.transport.api.model.Country;
import com.kamtar.transport.api.model.Language;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

@Repository
public interface CountryRepository extends CrudRepository<Country, UUID>, JpaSpecificationExecutor<Country> {
	
	/**
	 * Est ce que le code est déjà utilisé ?
	 * @param offre
	 * @return
	 */
	@Query("SELECT count(c)>0 FROM Country c WHERE code = :code")
	public boolean codeExist(@Param("code") String code);
	
	/**
	 * Chargement par le code
	 * @param code le code du pays
	 * @return
	 */
	@Query("SELECT l FROM Country l WHERE code = :code")
	public Optional<Country> findByCode(@Param("code") String code);

	/**
	 * Chargement des pays où Kamtar opère
	 * @return
	 */
	@Query("SELECT g FROM Country g WHERE g.opere IS true ")
	List<Country> findCountriesKamtarOpere();


}

