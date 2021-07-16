package com.kamtar.transport.api.service;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kamtar.transport.api.model.Vehicule;
import com.kamtar.transport.api.model.VehiculePhoto;


@Service
public interface VehiculePhotoService {
	
	public void savePhotoPrincipale(Vehicule offre, String base64_photo_principale);
	public void savePhotos(Vehicule offre, File folder);
	public void savePhoto(Vehicule offre, File photo, Integer ordre);
	public void savePhoto(Vehicule vehicule, String base64, Integer ordre);
	public List<VehiculePhoto> getPhotosVehicule(Vehicule offre, String code_pays);
	public void delete(VehiculePhoto photo);
	public Optional<VehiculePhoto> get(Vehicule offre, String filename, String code_pays);
	byte[] get(String uuid);
	public VehiculePhoto get(UUID uuid, String code_pays);
	
	public void savePhotoAssurance(Vehicule offre, String base64_photo);
	public void savePhotoCarteGrise(Vehicule offre, String base64_photo);

}
