package com.kamtar.transport.api.repository;

import com.kamtar.transport.api.model.FactureClient;
import com.kamtar.transport.api.model.Geoloc;
import com.kamtar.transport.api.model.Operation;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FactureClientRepository extends CrudRepository<FactureClient, String>, JpaSpecificationExecutor<FactureClient> {

    /**
     * Recherche par UUID (pour remplacer findById de Spring qui ne gère pas les uuid)
     * @param uuid
     * @return
     */
    @Query("SELECT b FROM FactureClient b WHERE b.id= :uuid AND b.codePays = :pays")
    FactureClient findByUUID(@Param("uuid") UUID uuid, @Param("pays") String pays);

    /**
     * Recherche par numero
     * @param uuid
     * @return
     */
    @Query("SELECT b FROM FactureClient b WHERE b.numeroFacture= :numero AND b.codePays = :pays")
    FactureClient getByNumero(@Param("numero") String numero, @Param("pays") String pays);





}

