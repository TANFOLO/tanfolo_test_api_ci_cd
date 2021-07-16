package com.kamtar.transport.api.service;

import com.kamtar.transport.api.model.UtilisateurClient;
import com.kamtar.transport.api.model.UtilisateurDriver;
import org.springframework.stereotype.Service;


@Service
public interface ClientPhotoService {

	public void savePhotoProfil(UtilisateurClient offre, String base64_photo);
	public byte[] get(String uuid);
}
