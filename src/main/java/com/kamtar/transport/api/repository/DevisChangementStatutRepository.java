package com.kamtar.transport.api.repository;

import com.kamtar.transport.api.model.*;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DevisChangementStatutRepository extends CrudRepository<DevisChangementStatut, String>, JpaSpecificationExecutor<DevisChangementStatut> {

	/**
	 * Mise à null de l'opérateur pour les enregistrements où l'opérateur est renseigné
	 * @param operateur
	 */
	@Transactional
	@Modifying
	@Query("UPDATE DevisChangementStatut e SET e.modifieParOperateur = null WHERE e.modifieParOperateur = :operateur")
	void setNullOperateur(@Param("operateur") UtilisateurOperateurKamtar operateur);

	/**
	 * Mise à null de transporteur pour les enregistrements où transporteur est renseigné
	 * @param operateur
	 */
	@Transactional
	@Modifying
	@Query("UPDATE DevisChangementStatut e SET e.modifieParTransporteur = null WHERE e.modifieParTransporteur = :transporteur")
	void setNullTransporteur(@Param("transporteur") UtilisateurDriver transporteur);
	
	/**
	 * Mise à null de devis pour les enregistrements où devis est renseigné
	 * @param operateur
	 */
	@Transactional
	@Modifying
	@Query("UPDATE DevisChangementStatut e SET e.devis = null WHERE e.devis = :devis")
	void setNullDevis(@Param("devis") Devis devis);

	/**
	 * Est ce que l'opéation passée en paramètre est passé par le statut passé en paramètre ?
	 * @param devis
	 * @return
	 */
	@Query("SELECT CASE  WHEN count(uuid)> 0 THEN true ELSE false END FROM DevisChangementStatut b WHERE b.devis = :devis AND b.nouveauStatut = :statut")
	Boolean isDevisPasseeParStatut(@Param("devis") Devis devis, @Param("statut") String statut);


}

