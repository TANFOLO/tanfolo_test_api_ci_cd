package com.kamtar.transport.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.kamtar.transport.api.model.Vehicule;
import com.kamtar.transport.api.model.VehiculePhoto;


import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

@Repository
public interface VehiculePhotoRepository extends CrudRepository<VehiculePhoto, UUID>, JpaSpecificationExecutor<VehiculePhoto> {

		/**
		 * Chargement des photos du véhicule
		 * @param offre
		 * @return
		 */
		@Query("SELECT o FROM VehiculePhoto o WHERE o.vehicule = :vehicule AND o.codePays = :pays ORDER BY ordre ASC")
		List<VehiculePhoto> findPhotosVehicules(@Param("vehicule") Vehicule vehicule, @Param("pays") String pays);
	
	/**
	 * Suppression d'une photo d'une offre
	 * @param offre
	 * @param ordre (pour déduire le nom)
	 * @return
	 */
	@Query("SELECT o FROM VehiculePhoto o WHERE o.vehicule = :vehicule AND o.filename = :filename AND o.codePays = :pays")
	Optional<VehiculePhoto> get(@Param("vehicule") Vehicule vehicule, @Param("filename") String filename, @Param("pays") String pays);

	/**
	 * Suppression d'une photo d'une offre
	 * @param offre
	 * @param ordre (pour déduire le nom)
	 * @return
	 */
	@Query("SELECT o FROM VehiculePhoto o WHERE o.uuid = :uuid AND o.codePays = :pays")
	VehiculePhoto get(@Param("uuid") UUID uuid, @Param("pays") String pays);

}

