package com.kamtar.transport.api.service;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kamtar.transport.api.model.UtilisateurDriver;
import com.kamtar.transport.api.model.Vehicule;
import com.kamtar.transport.api.model.VehiculePhoto;


@Service
public interface DriverPhotoService {
	
	public void savePhotoPermis(UtilisateurDriver offre, String base64_photo);
	public void savePhotoProfil(UtilisateurDriver offre, String base64_photo);
	public byte[] get(String uuid);
	public void delete(UtilisateurDriver proproetaire);
}
