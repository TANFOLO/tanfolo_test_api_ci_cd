package com.kamtar.transport.api.repository;

import com.kamtar.transport.api.model.Client;
import com.kamtar.transport.api.model.Geoloc;
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
public interface GeolocRepository extends CrudRepository<Geoloc, String>, JpaSpecificationExecutor<Geoloc> {

    @Query(nativeQuery = true, value = "SELECT * FROM geoloc g WHERE immatriculation= :immatriculation AND driver = :driver AND codePays = :pays ORDER BY createdOn DESC LIMIT 1")
    Geoloc findByImmatriculationAndDriver(@Param("immatriculation") String immatriculation, @Param("driver") String driver, @Param("pays") String pays);

    @Query("SELECT g FROM Geoloc g WHERE g.codePays = :pays AND g.immatriculation IN :immatriculations ORDER BY createdOn DESC")
    List<Geoloc> findByImmatriculations(@Param("immatriculations") List<String> immatriculations, @Param("pays") String pays);

    @Query("SELECT g FROM Geoloc g WHERE g.codePays = :pays AND createdOn > :dateMin AND createdOn < :dateMax ORDER BY createdOn DESC")
    List<Geoloc> findAllGeoloc(@Param("pays") String pays, @Param("dateMin") Date dateMin, @Param("dateMax") Date dateMax);

    @Query("SELECT g FROM Geoloc g WHERE g.codePays = :pays AND g.immatriculation= :immatriculation AND createdOn > :dateMin AND createdOn < :dateMax")
    List<Geoloc> findAllGeoloc(@Param("immatriculation") String immatriculation, @Param("pays") String pays, @Param("dateMin") Date dateMin, @Param("dateMax") Date dateMax);

}

