package com.kamtar.transport.api.repository;

import com.kamtar.transport.api.model.ActionAudit;
import com.kamtar.transport.api.model.Devis;
import com.kamtar.transport.api.model.Vehicule;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DevisRepository extends CrudRepository<Devis, UUID>, JpaSpecificationExecutor<Devis> {

    /**
     * Recherche par UUID (pour remplacer findById de Spring qui ne gère pas les uuid)
     * @param uuid
     * @return
     */
    @Query("SELECT b FROM Devis b WHERE b.id= :uuid AND b.codePays = :pays")
    Devis findByUUID(@Param("uuid") UUID uuid, @Param("pays") String pays);

    /**
     * Recherche la valeur max du code de la operation
     * @param uuid
     * @return
     */
    @Query("SELECT MAX(code) FROM Devis b")
    Long getMaxCode();

}

