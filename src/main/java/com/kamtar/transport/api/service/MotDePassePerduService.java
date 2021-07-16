package com.kamtar.transport.api.service;



import java.util.List;

import com.kamtar.transport.api.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


@Service
public interface MotDePassePerduService {
	
	MotDePassePerdu create(UtilisateurOperateurKamtar operateurKamtar, UtilisateurDriver transporteur, UtilisateurClient client, UtilisateurAdminKamtar admin, UtilisateurClientPersonnel client_personnel, UtilisateurProprietaire proprietaire, String code_pays);
	Boolean changerMotDePasser(String token, String nouveau_mot_de_passe, String pays);
	MotDePassePerdu charger(String token, String pays);

	
}
