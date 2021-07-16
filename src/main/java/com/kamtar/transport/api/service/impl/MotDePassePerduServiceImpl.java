package com.kamtar.transport.api.service.impl;

import java.util.Date;

import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.repository.*;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.kamtar.transport.api.service.ActionAuditService;
import com.kamtar.transport.api.service.EmailToSendService;
import com.kamtar.transport.api.service.MotDePassePerduService;
import com.kamtar.transport.api.utils.JWTProvider;
import com.kamtar.transport.api.utils.UpdatableBCrypt;

@Service(value="MotDePassePerduService")
public class MotDePassePerduServiceImpl implements MotDePassePerduService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(MotDePassePerduServiceImpl.class); 

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	private MotDePassePerduRepository motDePassePerduRepository; 

	@Autowired
	private ActionAuditService actionAuditService; 

	@Autowired
	private EmailToSendService emailToSendService; 
	
	@Autowired
	private UtilisateurAdminKamtarRepository utilisateurAdminKamtarRepository;

	@Autowired
	private UtilisateurClientPersonnelRepository utilisateurClientPersonnelRepository;

	@Autowired
	private UtilisateurDriverRepository utilisateurDriverRepository;

	@Autowired
	private UtilisateurProprietaireRepository utilisateurProprietaireRepository;

	@Autowired
	private UtilisateurClientRepository utilisateurClientRepository; 

	@Autowired
	private UtilisateurOperateurKamtarRepository utilisateurOperateurKamtarRepository; 

	@Override
	public MotDePassePerdu create(UtilisateurOperateurKamtar operateurKamtar, UtilisateurDriver transporteur, UtilisateurClient client, UtilisateurAdminKamtar admin, UtilisateurClientPersonnel client_personnel, UtilisateurProprietaire proprietaire, String code_pays) {
		
		// génère des chaines aléatoires jusqu'à trouver une chaine qui n'est pas encore en bdd
		boolean token_disponible = false;
		String token = "";
		while (!token_disponible) {
			token = RandomStringUtils.random(24, true, true).toUpperCase();
			if (!motDePassePerduRepository.tokenExisteDeja(token, code_pays)) {
				token_disponible = true;
			}
		}
		
		// va chercher le mail et le téléphone
		String destinataire_telephone = null;
		String destinataire_email = null;
		if (operateurKamtar != null) {
			destinataire_email = operateurKamtar.getEmail();
			destinataire_telephone = operateurKamtar.getNumeroTelephone1();
		} else if (transporteur != null) {
			destinataire_email = transporteur.getEmail();
			destinataire_telephone = transporteur.getNumeroTelephone1();
		} else if (admin != null) {
			destinataire_email = admin.getEmail();
			destinataire_telephone = admin.getNumeroTelephone1();
		} else if (client_personnel != null) {
			destinataire_email = client_personnel.getEmail();
			destinataire_telephone = client_personnel.getNumeroTelephone1();
		} else if (proprietaire != null) {
			destinataire_email = proprietaire.getEmail();
			destinataire_telephone = proprietaire.getNumeroTelephone1();
		}
		
		// enregistrement
		MotDePassePerdu mdp = new MotDePassePerdu(operateurKamtar, transporteur, token, destinataire_email, destinataire_telephone, client, admin, client_personnel, proprietaire);
		mdp = motDePassePerduRepository.save(mdp); 
		
		// envoi par SMS ou email
		Utilisateur utilisateur = null;
		if (operateurKamtar != null) {
			utilisateur = operateurKamtar;
		} else if (transporteur != null) {
			utilisateur = transporteur;
		} else if (client != null) {
			utilisateur = client;
		} else if (admin != null) {
			utilisateur = admin;
		} else if (client_personnel != null) {
			utilisateur = client_personnel;
		} else if (proprietaire != null) {
			utilisateur = proprietaire;
		}
		emailToSendService.envoyerLienMotDePassePerdu(utilisateur, mdp, code_pays);
		
		return mdp;
	}



	@Override
	public Boolean changerMotDePasser(String token, String nouveau_mot_de_passe, String pays) {
		logger.info("changerMotDePasser token=" + token + " nouveau_mot_de_passe=" + nouveau_mot_de_passe + "pays=" + pays);
		MotDePassePerdu mdp = motDePassePerduRepository.findByToken(token, pays);
		if (mdp == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La demande de modification de mot de passe est introuvable.");
		} else if (mdp.getDateUtilisationToken() != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La demande de modification de mot de passe a déjà été réalisée.");
		}
		
	
		// modifie le mot de passe de l'utilisateur
		boolean modification = false;
		if (mdp.getAdminKamtar() != null) {
			mdp.getAdminKamtar().setMotDePasse(UpdatableBCrypt.hashPassword(nouveau_mot_de_passe));
			utilisateurAdminKamtarRepository.save(mdp.getAdminKamtar());
			modification = true;
		} else if (mdp.getOperateurKamtar() != null) {
			mdp.getOperateurKamtar().setMotDePasse(UpdatableBCrypt.hashPassword(nouveau_mot_de_passe));
			utilisateurOperateurKamtarRepository.save(mdp.getOperateurKamtar());
			modification = true;
		} else if (mdp.getTransporteur() != null) {
			mdp.getTransporteur().setMotDePasse(UpdatableBCrypt.hashPassword(nouveau_mot_de_passe));
			utilisateurDriverRepository.save(mdp.getTransporteur());
			modification = true;
		} else if (mdp.getProprietaire() != null) {
			mdp.getProprietaire().setMotDePasse(UpdatableBCrypt.hashPassword(nouveau_mot_de_passe));
			utilisateurProprietaireRepository.save(mdp.getProprietaire());
			modification = true;
		} else if (mdp.getClient() != null) {
			mdp.getClient().setMotDePasse(UpdatableBCrypt.hashPassword(nouveau_mot_de_passe));
			utilisateurClientRepository.save(mdp.getClient());
			modification = true;
		} else if (mdp.getClientPersonnel() != null) {
			mdp.getClientPersonnel().setMotDePasse(UpdatableBCrypt.hashPassword(nouveau_mot_de_passe));
			utilisateurClientPersonnelRepository.save(mdp.getClientPersonnel());
			modification = true;
		}
		
		if (modification) {
			
			// invalide la demande de mot de passe
			mdp.setDateUtilisationToken(new Date());
			mdp = motDePassePerduRepository.save(mdp); 
			
			return true;
		
		}
		
		return false;
	}



	@Override
	public MotDePassePerdu charger(String token, String pays) {
		return motDePassePerduRepository.findByToken(token, pays);
	}




}
