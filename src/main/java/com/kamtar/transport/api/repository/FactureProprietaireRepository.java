package com.kamtar.transport.api.repository;

import com.kamtar.transport.api.model.FactureProprietaire;
import com.kamtar.transport.api.model.UtilisateurProprietaire;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FactureProprietaireRepository extends CrudRepository<FactureProprietaire, String>, JpaSpecificationExecutor<FactureProprietaire> {

    /**
     * Recherche par UUID (pour remplacer findById de Spring qui ne gère pas les uuid)
     * @param uuid
     * @return
     */
    @Query("SELECT b FROM FactureProprietaire b WHERE b.id= :uuid AND b.codePays = :pays")
    FactureProprietaire findByUUID(@Param("uuid") UUID uuid, @Param("pays") String pays);

    /**
     * Recherche par numero
     * @param uuid
     * @return
     */
    @Query("SELECT b FROM FactureProprietaire b WHERE b.numeroFacture= :numero AND b.codePays = :pays")
    List<FactureProprietaire> getByNumero(@Param("numero") Long numero, @Param("pays") String pays);

    /**
     * Compter le nombre de factures associées au prorpriétaire
     * @param vehicule
     * @return
     */
    @Query("SELECT count(b) FROM FactureProprietaire b WHERE proprietaire = :proprietaire AND b.codePays = :pays")
    Long countFacturesProprietaire(@Param("proprietaire") UtilisateurProprietaire proprietaire, @Param("pays") String pays);




}

