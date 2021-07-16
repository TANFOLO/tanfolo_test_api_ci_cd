package com.kamtar.transport.api.service;



import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import com.kamtar.transport.api.enums.FichierType;
import com.kamtar.transport.api.model.UtilisateurProprietaire;


@Service
public interface DocumentsProprietaireService {
	
	void create(String base64, UtilisateurProprietaire proproetaire, FichierType type);
	byte[] get(String uuid);
	public void delete(UtilisateurProprietaire proproetaire);
}
