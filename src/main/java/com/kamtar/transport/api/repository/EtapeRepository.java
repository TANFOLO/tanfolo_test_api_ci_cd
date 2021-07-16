package com.kamtar.transport.api.repository;

import com.kamtar.transport.api.model.Devis;
import com.kamtar.transport.api.model.Etape;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EtapeRepository extends CrudRepository<Etape, UUID>, JpaSpecificationExecutor<Etape> {

    /**
     * Recherche par UUID (pour remplacer findById de Spring qui ne gère pas les uuid)
     * @param uuid
     * @return
     */
    @Query("SELECT b FROM Etape b WHERE b.id= :uuid AND b.codePays = :pays")
    Etape findByUUID(@Param("uuid") UUID uuid, @Param("pays") String pays);


}

