package com.kamtar.transport.api.service.impl;

import java.util.Map;

import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.DeclarerIncidentOperationParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.kamtar.transport.api.enums.ActionAuditTypeAction;
import com.kamtar.transport.api.model.UtilisateurDriver;
import com.kamtar.transport.api.repository.ActionAuditRepository;
import com.kamtar.transport.api.service.ActionAuditService;
import com.kamtar.transport.api.utils.JWTProvider;

@Service
public class ActionAuditServiceImpl implements ActionAuditService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(ActionAuditServiceImpl.class);  

	@Autowired
	private ActionAuditRepository actionAuditRepository; 

	@Autowired
	JWTProvider jwtProvider;

	public Page<ActionAudit> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<ActionAudit> conditions) {
		Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return actionAuditRepository.findAll(conditions, pageable);
	}

	public Long countAll(Specification<ActionAudit> conditions) {
		return actionAuditRepository.count(conditions);
	}

	@Async
	public void loginAdminKamtar(UtilisateurAdminKamtar admin, String code_pays) {

		ActionAudit action = new ActionAudit(admin.getUuid().toString(), code_pays);
		action.setLibelleAction("Connexion de l'admin Kamtar " + admin.getPrenom() + " " + admin.getNom());
		action.setTypeAction(ActionAuditTypeAction.LOGIN.toString());
		action.setUuidAdmin(admin.getUuid().toString());
		action.setCodePays(code_pays);


		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void loginOperateurKamtar(UtilisateurOperateurKamtar operateur, String code_pays) {

		String codePays = null;
		if (operateur != null) {
			codePays = operateur.getCodePays();
		}
		ActionAudit action = new ActionAudit(operateur.getUuid().toString(), codePays);
		action.setLibelleAction("Connexion de l'opérateur Kamtar " + operateur.getPrenom() + " " + operateur.getNom());
		action.setTypeAction(ActionAuditTypeAction.LOGIN.toString());
		action.setUuidOperateur(operateur.getUuid().toString());
		action.setCodePays(code_pays);

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void creerAdminKamtar(UtilisateurAdminKamtar admin_cree, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);



		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Création de l'admin Kamtar " + admin_cree.getPrenom() + " " + admin_cree.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CREER_ADMIN_KAMTAR.toString());
		action.setUuidAdmin(admin_cree.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void creerOperateurKamtar(UtilisateurOperateurKamtar operateur_cree, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Création de l'opérateur Kamtar " + operateur_cree.getPrenom() + " " + operateur_cree.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CREER_ADMIN_KAMTAR.toString());
		action.setUuidOperateur(operateur_cree.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void editerAdminKamtar(UtilisateurAdminKamtar admin_cree, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Modification de l'admin Kamtar " + admin_cree.getPrenom() + " " + admin_cree.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EDITER_ADMIN_KAMTAR.toString());
		action.setUuidAdmin(admin_cree.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void editerOperateurKamtar(UtilisateurOperateurKamtar operateur_cree, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Modification de l'opérateur Kamtar " + operateur_cree.getPrenom() + " " + operateur_cree.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EDITER_ADMIN_KAMTAR.toString());
		action.setUuidOperateur(operateur_cree.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void supprimerOperateurKamtar(UtilisateurOperateurKamtar operateur_supprime, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Suppression de l'opérateur Kamtar " + operateur_supprime.getPrenom() + " " + operateur_supprime.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.SUPPRIMER_OPERATEUR_KAMTAR.toString());
		action.setUuidOperateur(operateur_supprime.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}
	
	
	

	@Async
	public void getAdminKamtar(UtilisateurAdminKamtar admin, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation de l'admin Kamtar " + admin.getPrenom() + " " + admin.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_ADMIN_KAMTAR.toString());
		action.setUuidAdmin(admin.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void getOperateurKamtar(UtilisateurOperateurKamtar operateur, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation de l'opérateur Kamtar " + operateur.getPrenom() + " " + operateur.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_OPERATEUR_KAMTAR.toString());
		action.setUuidOperateur(operateur.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void getAdminsKamtar(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des admins Kamtar par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_ADMINS_KAMTAR.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}
	@Async
	public void exportAdminsKamtar(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Export des admins Kamtar par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EXPORT_ADMINS_KAMTAR.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void getOperateursKamtar(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des opérateurs Kamtar par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_OPERATEURS_KAMTAR.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void exportOperateursKamtar(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Export des opérateurs Kamtar par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EXPORT_OPERATEURS_KAMTAR.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void creerClient(Client client_cree, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(client_cree.getUuid().toString(), jwtProvider.getCodePays(token));
		if (claims != null) {
			action.setLibelleAction("Création d'un expéditeur " + client_cree.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		} else {
			action.setLibelleAction("Création d'un expéditeur " + client_cree.getNom());
		}
		action.setTypeAction(ActionAuditTypeAction.CREER_EXPEDITEUR.toString());
		action.setUuidClient(client_cree.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}


	@Override
	public void validerClient(UtilisateurClient client, String code_pays) {

		ActionAudit action = new ActionAudit();
		action.setLibelleAction("Validation du compte client " + client.getNom() + " avec le code de validation " + client.getCode_validation());
		action.setTypeAction(ActionAuditTypeAction.VALIDATION_CLIENT.toString());
		action.setUuidClient(client.getUuid().toString());
		action.setCodePays(code_pays);

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());


	}

	@Async
	@Override
	public void creerClientPersonnel(UtilisateurClientPersonnel client_cree, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);
		ActionAudit action = new ActionAudit();
		action.setLibelleAction("Création d'un expéditeur personnel " + client_cree.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CREER_CLIENT_PERSONNEL.toString());
		action.setUuidClient(client_cree.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	@Override
	public void editerClientPersonnel(UtilisateurClientPersonnel client_edite, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Modification du client personnel " + client_edite.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EDITER_CLIENT_PERSONNEL.toString());
		action.setUuidClient(client_edite.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	@Override
	public void deleteClientPersonnel(UtilisateurClientPersonnel client_edite, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Suppression du client personnel " + client_edite.getNom() + " (" + client_edite.getPrenomNom() + ") par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.SUPPRIMER_CLIENT_PERSONNEL.toString());
		action.setUuidClient(client_edite.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	@Override
	public void getClientPersonnel(UtilisateurClientPersonnel client, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation du client personnel " + client.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_CLIENT_PERSONNEL.toString());
		action.setUuidClient(client.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	@Override
	public void getClientsPersonnels(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des clients perosnnels par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_CLIENTS_PERSONNELS.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void creerClient(Client client_cree, String pays, boolean diff) {

		ActionAudit action = new ActionAudit();
		action.setLibelleAction("Création d'un expéditeur " + client_cree.getNom() + " par un anonyme");
		action.setTypeAction(ActionAuditTypeAction.CREER_EXPEDITEUR.toString());
		action.setUuidClient(client_cree.getUuid().toString());
		action.setCodePays(pays);

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void editerClient(Client client_edite, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Modification de l'expéditeur " + client_edite.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EDITER_EXPEDITEUR.toString());
		action.setUuidClient(client_edite.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void getClient(Client client, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation de l'expéditeur " + client.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_EXPEDITEUR.toString());
		action.setUuidClient(client.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void getClients(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des expéditeurs par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_EXPEDITEURS.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}


	@Async
	public void exportClients(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Export des expéditeurs par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EXPORT_EXPEDITEURS.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}



	@Async
	public void creerTransporteur(UtilisateurDriver transporteur_cree, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Création d'un transporteur " + transporteur_cree.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CREER_TRANSPORTEUR.toString());
		action.setUuidTransporteur(transporteur_cree.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void editerTransporteur(UtilisateurDriver transporteur_edite, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Modification du transporteur " + transporteur_edite.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EDITER_TRANSPORTEUR.toString());
		action.setUuidTransporteur(transporteur_edite.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void supprimerTransporteur(UtilisateurDriver transporteur_edite, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Suppression du transporteur " + transporteur_edite.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.SUPPRIMER_TRANSPORTEUR.toString());
		action.setUuidTransporteur(transporteur_edite.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}
	
	@Async
	public void getTransporteur(UtilisateurDriver transporteur, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation du transporteur " + transporteur.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_TRANSPORTEUR.toString());
		action.setUuidTransporteur(transporteur.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void getTransporteurs(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des transporteurs par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_TRANSPORTEURS.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void loginTransporteur(UtilisateurDriver transporteur, String code_pays) {

		ActionAudit action = new ActionAudit(transporteur.getUuid().toString(), code_pays);
		action.setLibelleAction("Connexion du transporteur " + transporteur.getPrenomNom());
		action.setTypeAction(ActionAuditTypeAction.LOGIN_TRANSPORTEUR.toString());
		action.setUuidTransporteur(transporteur.getUuid().toString());
		action.setCodePays(code_pays);

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}
	
	@Async
	public void loginProprietaire(UtilisateurProprietaire proprietaire, String token) {

		ActionAudit action = new ActionAudit(proprietaire.getUuid().toString(), proprietaire.getCodePays());
		action.setLibelleAction("Connexion du proprietaire " + proprietaire.getPrenomNom());
		action.setTypeAction(ActionAuditTypeAction.LOGIN_PROPRIETAIRE.toString());
		action.setUuidProprietaire(proprietaire.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}
	
	@Async
	public void loginClient(UtilisateurClient client, String code_pays) {

		ActionAudit action = new ActionAudit(client.getUuid().toString(), code_pays);
		action.setLibelleAction("Connexion de l'expéditeur " + client.getPrenomNom());
		action.setTypeAction(ActionAuditTypeAction.LOGIN_EXPEDITEUR.toString());
		action.setUuidClient(client.getUuid().toString());
		action.setCodePays(code_pays);

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Override
	@Async
	public void loginClientPersonnel(UtilisateurClientPersonnel client, String code_pays) {

		ActionAudit action = new ActionAudit(client.getUuid().toString(), code_pays);
		action.setLibelleAction("Connexion du client " + client.getPrenomNom());
		action.setTypeAction(ActionAuditTypeAction.LOGIN_CLIENT_PERSONNEL.toString());
		action.setUuidClient(client.getUuid().toString());
		action.setCodePays(code_pays);

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Override
	public void getClientsPersonnels(Client client, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des clients personnels du client " + client.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_CLIENTS_PERSONNELS.toString());
		action.setUuidClient(client.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}


	@Async
	public void creerLanguage(Language language_cree, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Création d'un language " + language_cree.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CREER_LANGUAGE.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void getLanguages(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des langues par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_LANGUAGES.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}


	@Async
	public void creerCountry(Country language_cree, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Création d'un pays " + language_cree.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CREER_COUNTRY.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void getCountries(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des pays par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_COUNTRIES.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void contact(Contact contact, Utilisateur utilisateur, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);
		ActionAudit action = new ActionAudit();
		if (claims != null) {
			action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
			action.setLibelleAction("Envoi du message " + contact.getUuid() + " par " + claims.get("prenom") + " " + claims.get("nom") + " " + contact.getEmetteurEmail());
		} else {
			action.setLibelleAction("Envoi du message " + contact.getUuid() + " par " + contact.getEmetteurEmail()); 
		}

		action.setUuidContact(contact.getUuid().toString());
		if (utilisateur != null) {
			action.setUuidUtilisateur(utilisateur.getUuid().toString());
		}
		action.setTypeAction(ActionAuditTypeAction.CONTACT.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void motDePassePerdu(String uuid_utilisateur, MotDePassePerdu mdp, String tel_login, String code_pays) {

		ActionAudit action = new ActionAudit();
		action.setLibelleAction("Demande de mot de passe perdu par " + uuid_utilisateur + "  " + tel_login); 

		action.setUuidMotDePassePerdu(mdp.getUuid().toString());
		action.setUuidUtilisateur(uuid_utilisateur);
		action.setTypeAction(ActionAuditTypeAction.MOT_DE_PASSE_PERDU.toString());
		action.setCodePays(code_pays);

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}





	@Async
	public void getActionAudit(String token) {
		
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des actions d'audit par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_AUDIT.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
	}
	
	@Async
	public void getActionAuditAdministrateurKamtar(String token, String admin) {
		
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des actions d'audit de l'administreur Kamtar " + admin + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_AUDIT.toString());
		action.setUuidAdmin(admin);
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
	}

	@Async
	public void getActionAuditOperateurKamtar(String token, String admin) {
		
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des actions d'audit de l'administreur Kamtar " + admin + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_AUDIT.toString());
		action.setUuidOperateur(admin);
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
	}

	
	@Async
	public void getActionAuditClient(String token, String client) {

		if (client != "") {
			client = "de l'expéditeur " + client;
		}
		
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des actions d'audit " + client + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_AUDIT.toString());
		action.setUuidClient(client);
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
	}
	
	@Async
	public void getActionAuditTransporteur(String token, String transporteur) {
		
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des actions d'audit du transporteur " + transporteur + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_AUDIT.toString());
		action.setUuidTransporteur(transporteur);
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
	}

	@Async
	public void getListeSMS(String token) {
		
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des SMS par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_SMS.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
	}


	@Async
	public void getListeEmails(String token) {
		
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des emails par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_EMAILS.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
	}



	@Override
	public void getNotificationsBackoffice(String token) {
		
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consulte les notifications par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTE_NOTIFICATIONS.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
		
	}

	@Override
	public void notificationTraitee(Notification notification, String token) {
		
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Notification " + notification.getUuid().toString() + " traitée par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.TRAITER_NOTIFICATION.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
	}

	@Override
	public void supprimerAdminKamtar(UtilisateurAdminKamtar admin_cree, String token) {
		
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Suppression de l'admin Kamtar " + admin_cree.getPrenom() + " " + admin_cree.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.SUPPRIMER_ADMIN_KAMTAR.toString());
		action.setUuidAdmin(admin_cree.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
	}

	@Override
	public void deleteClient(Client client_edite, String token) {
		
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Suppression de l'expéditeur " + client_edite.getNom() + " (" + client_edite.getUtilisateur().getPrenomNom() + ") par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.SUPPRIMER_EXPEDITEUR.toString());
		action.setUuidClient(client_edite.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
		
	}

	@Async
	public void creerProprietaire(UtilisateurProprietaire operateur_cree, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Création du propriétaire " + operateur_cree.getPrenom() + " " + operateur_cree.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CREER_PROPRIETAIRE.toString());
		action.setUuidProprietaire(operateur_cree.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void editerProprietaire(UtilisateurProprietaire admin_cree, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Modification du propriétaire " + admin_cree.getPrenom() + " " + admin_cree.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EDITER_PROPRIETAIRE.toString());
		action.setUuidProprietaire(admin_cree.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}
	
	@Override
	public void supprimerProprietaire(UtilisateurProprietaire client_edite, String token) {
		
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Suppression du propriétaire " + client_edite.getNom() + " (" + client_edite.getPrenomNom() + ") par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.SUPPRIMER_PROPRIETAIRE.toString());
		action.setUuidProprietaire(client_edite.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
		
	}
	

	@Async
	public void getProprietaire(UtilisateurProprietaire operateur, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation du propriétaire " + operateur.getPrenom() + " " + operateur.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_PROPRIETAIRE.toString());
		action.setUuidProprietaire(operateur.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void getProprietaires(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des propriétaires par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_PROPRIETAIRES.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void exportProprietaires(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Export des propriétaires par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EXPORT_PROPRIETAIRES.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}
	
	@Async
	public void getActionAuditProprietaire(String token, String proprietaire) {
		
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des actions d'audit ddes propriétaires " + proprietaire + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_AUDIT.toString());
		action.setUuidProprietaire(proprietaire);
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
	}
	
	@Async
	public void creerDriver(UtilisateurDriver operateur_cree, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Création du driver " + operateur_cree.getPrenom() + " " + operateur_cree.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CREER_PROPRIETAIRE.toString());
		action.setUuidDriver(operateur_cree.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void editerDriver(UtilisateurDriver admin_cree, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Modification du driver " + admin_cree.getPrenom() + " " + admin_cree.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EDITER_CREER_DRIVER.toString());
		action.setUuidDriver(admin_cree.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}
	
	@Override
	public void supprimerDriver(UtilisateurDriver client_edite, String token) {
		
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Suppression du driver " + client_edite.getNom() + " (" + client_edite.getPrenomNom() + ") par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.SUPPRIMER_DRIVER.toString());
		action.setUuidDriver(client_edite.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
		
	}
	

	@Async
	public void getDriver(UtilisateurDriver operateur, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation du driver " + operateur.getPrenom() + " " + operateur.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_PROPRIETAIRE.toString());
		action.setUuidDriver(operateur.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void getDrivers(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des drivers par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_TRANSPORTEURS.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void exportDrivers(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Export des drivers par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EXPORT_TRANSPORTEURS.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}
	
	@Async
	public void getActionAuditDriver(String token, String driver) {
		
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des actions d'audit des drivers " + driver + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_AUDIT.toString());
		action.setUuidDriver(driver);
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
	}
	
	@Async
	public void creerVehicule(Vehicule operateur_cree, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Création du véhicule " + operateur_cree.getImmatriculation() + " " + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CREER_VEHICULE.toString());
		action.setUuidVehicule(operateur_cree.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}
	


	@Async
	public void editerVehicule(Vehicule admin_cree, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Modification du véhicule " + admin_cree.getImmatriculation() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EDITER_VEHICULE.toString());
		action.setUuidVehicule(admin_cree.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Override
	public void changerDisponibiliteVehicule(Vehicule admin_cree, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Modification de la disponibilié du véhicule " + admin_cree.getImmatriculation() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CHANGER_DISPONIBILITE_VEHICULE.toString());
		action.setUuidVehicule(admin_cree.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Override
	public void supprimerVehicule(Vehicule client_edite, String token) {
		
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Suppression du véhicule " + client_edite.getImmatriculation() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.SUPPRIMER_VEHICULE.toString());
		action.setUuidVehicule(client_edite.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
		
	}
	

	@Async
	public void getVehicule(Vehicule operateur, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation du véhicule " + operateur.getImmatriculation() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_VEHICULE.toString());
		action.setUuidVehicule(operateur.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void getGeolocVehicule(Vehicule operateur, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation de la géolocalisation du véhicule " + operateur.getImmatriculation() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_GEOLOC_VEHICULE.toString());
		action.setUuidVehicule(operateur.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}


	@Async
	public void getGeolocsVehicule(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation de la géolocalisation des véhicules" + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_GEOLOC_VEHICULES.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void getGeolocsVehicule(Vehicule vehicule, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des géolocalisations du véhicule " + vehicule.getImmatriculation() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_GEOLOCS_VEHICULE.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void getPhotosVehicule(Vehicule operateur, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des photos du véhicule " + operateur.getImmatriculation() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_PHOTOS_VEHICULE.toString());
		action.setUuidVehicule(operateur.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}
	
	@Async
	public void getOperationDocuments(Operation operation, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des documents de l'opération " + operation.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_DOCUMENT_OPERATION.toString());
		action.setUuidOperation(operation.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void getVehicules(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des véhicules par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_VEHICULES.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}
	@Async
	public void exportVehicules(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Export des véhicules par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EXPORT_VEHICULES.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}
	
	@Async
	public void getActionAuditVehicule(String token, String driver) {
		
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des actions d'audit des véhicules " + driver + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_AUDIT.toString());
		action.setUuidVehicule(driver);
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
	}
	
	
	
	
	

	@Async
	public void creerOperation(Operation operation_cree, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Création d'une operation " + operation_cree.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CREER_OPERATION.toString());
		action.setUuidOperation(operation_cree.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void creerOperation(Operation operation_cree, String code_pays, String uuid_utilisateur, String prenom_utilisateur, String nom_utilisateur) {


		ActionAudit action = new ActionAudit(uuid_utilisateur, code_pays);
		action.setLibelleAction("Création d'une operation " + operation_cree.getCode() + " par " + prenom_utilisateur + " " + nom_utilisateur);
		action.setTypeAction(ActionAuditTypeAction.CREER_OPERATION.toString());
		action.setUuidOperation(operation_cree.getUuid().toString());
		action.setCodePays(code_pays);

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void creerOperationParDuplication(Operation operation_cree, String code_pays) {
		logger.info("AUDIT 1");

		ActionAudit action = new ActionAudit(code_pays);
		action.setLibelleAction("Création d'une opération " + operation_cree.getCode() + " par duplication de l'opération " + operation_cree.getRecurrenceOperationOriginel().getCode());
		action.setTypeAction(ActionAuditTypeAction.CREER_OPERATION_PAR_DUPLICATION.toString());
		action.setUuidOperation(operation_cree.getUuid().toString());
		action.setCodePays(code_pays);

		action = actionAuditRepository.save(action);
		logger.info("AUDIT 2 " + action.getUuid().toString());

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}


	@Async
	public void creerOperationParExpediteur(Operation operation_cree, String token, Client expediteur) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Création d'une operation " + operation_cree.getCode() + " par l'expéditeur " + expediteur.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CREER_OPERATION.toString());
		action.setUuidOperation(operation_cree.getUuid().toString());
		action.setUuidClient(expediteur.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void editerOperation(Operation operation_edite, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Modification de l'opération " + operation_edite.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EDITER_OPERATION.toString());
		action.setUuidOperation(operation_edite.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}
	

	@Async
	public void supprimerOperation(Operation operation_edite, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Suppression de l'operation " + operation_edite.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.SUPPRIMER_OPERATION.toString());
		action.setUuidOperation(operation_edite.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void dupliquerOperation(Operation operation_edite, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Duplication de l'operation " + operation_edite.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.DUPLIQUER_OPERATION.toString());
		action.setUuidOperation(operation_edite.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}


	@Async
	public void getOperations(Operation operation, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des operations " + operation.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_OPERATIONS.toString());
		action.setUuidOperation(operation.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void getOperationsTransporteur(String token, UtilisateurDriver transporteur) {

		if (transporteur != null) {

			Map<String, String> claims = jwtProvider.getClaims(token);

			ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
			action.setLibelleAction("Consultation des operations du transporteur " + transporteur.getPrenomNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
			action.setTypeAction(ActionAuditTypeAction.CONSULTATION_OPERATIONS.toString());
			action.setUuidTransporteur(transporteur.getUuid().toString());
			action.setCodePays(jwtProvider.getCodePays(token));

			action = actionAuditRepository.save(action);

			logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

		}

	}

	@Async
	public void getOperationsProprietaire(String token, UtilisateurProprietaire proprietaire) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des operations du propriétaire " + proprietaire.getPrenomNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_OPERATIONS.toString());
		action.setUuidProprietaire(proprietaire.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}


	@Async
	public void getOperationsExpediteur(String token, Client expediteur) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des operation de l'expéditeur " + expediteur.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_OPERATIONS.toString());
		action.setUuidClient(expediteur.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}
	


	@Async
	public void getOperations(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des operations par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_OPERATIONS.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}



	@Async
	public void exportFacturesClient(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Export des factures clients par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EXPORT_FACTURES_CLIENT.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}


	@Async
	public void exportFacturesProprietaire(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Export des factures propriétaire par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EXPORT_FACTURES_PROPRIETAIRE.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void exportOperations(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Export des operations par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EXPORT_OPERATIONS.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}


	@Override
	public void creerOperationParClient(Operation operation_cree, String token, Client client) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Création d'une operation " + operation_cree.getCode() + " par le client " + client.getNom());
		action.setTypeAction(ActionAuditTypeAction.CREER_OPERATION_PAR_CLIENT.toString());
		action.setUuidOperation(operation_cree.getUuid().toString());
		action.setUuidClient(client.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
	}

	@Override
	public void getOperation(Operation operation, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation de l'opération " + operation.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_OPERATION.toString());
		action.setUuidOperation(operation.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
	}


	@Override
	public void getOperationsClient(String token, Client client) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des opérations du client " + client.getNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_OPERATIONS.toString());
		action.setUuidClient(client.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
	}

	@Override
	public void changerOrdreOperationsTransporteur(String token, UtilisateurDriver transporteur) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Changement de l'ordre des opérations du transporteur " + transporteur.getPrenomNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CHANGEMENT_ORDRE_OPERATIONS.toString());
		action.setUuidDriver(transporteur.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
	}

	@Async
	public void creerOperationAppelOffre(OperationAppelOffre operation_appel_offre_driver, String token) {
		

			Map<String, String> claims = jwtProvider.getClaims(token);

			ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
			action.setLibelleAction("Création d'un appel d'offre pour operation " + operation_appel_offre_driver.getOperation().getCode() + " auprès du driver " + operation_appel_offre_driver.getTransporteur().getPrenomNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
			action.setTypeAction(ActionAuditTypeAction.CREER_OPERATION_APPEL_OFFRE_DRIVER.toString());
			action.setUuidOperation(operation_appel_offre_driver.getOperation().getUuid().toString());
			action.setUuidDriver(operation_appel_offre_driver.getTransporteur().getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

			action = actionAuditRepository.save(action);

			logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
		
	}

	@Async
	public void getOperationsAppelOffreOperation(String token, Operation operation) {
		

			Map<String, String> claims = jwtProvider.getClaims(token);

			ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
			action.setLibelleAction("Consultation des appels d'offre de l'opération " + operation.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
			action.setTypeAction(ActionAuditTypeAction.CONSULTATION_OPERATION_APPELS_OFFRE_DRIVER.toString());
			action.setUuidOperation(operation.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

			action = actionAuditRepository.save(action);

			logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

		
	}

	@Async
	public void getOperationsAppelOffreProprietaire(String token, UtilisateurProprietaire proprietaire) {
		
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des appels d'offre pour le propriétaire " + proprietaire.getPrenomNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_OPERATION_APPELS_OFFRE_DRIVER.toString());
		action.setUuidProprietaire(proprietaire.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
	}

	@Async
	public void getOperationsAppelOffreDriver(String token, UtilisateurDriver driver) {
		
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des appels d'offre pour le driver " + driver.getPrenomNom() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_OPERATION_APPELS_OFFRE_DRIVER.toString());
		action.setUuidDriver(driver.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
	}

	@Async
	public void enregistrerPropositionAppelOffre(String token, OperationAppelOffre operation_appel_offre, Float montant,
			UtilisateurDriver transporteur, UtilisateurProprietaire proprietaire) {
	
		Map<String, String> claims = jwtProvider.getClaims(token);
		
		String nom_prenom = "";
		if (transporteur != null) {
			nom_prenom = "le transporteur " + transporteur.getPrenomNom();
		} else if (proprietaire != null) {
			nom_prenom = "le proprietaire " + proprietaire.getPrenomNom();
		}

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Proposition d'un montant de " + montant + " par " + nom_prenom);
		action.setTypeAction(ActionAuditTypeAction.NOUVELLE_PROPOSITION_APPEL_OFFRE.toString());
		action.setUuidDriver(transporteur.getUuid().toString());
		action.setUuidProprietaire(transporteur.getUuid().toString());
		action.setUuidAppelOffre(operation_appel_offre.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
		
	}

	@Async
	public void changerStatutOperation(Operation operation_edite, String token, String ancien_statut,
			String nouveau_statut) {
		
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Changement de statut de l'opération " + operation_edite.getCode() + " de " + ancien_statut + " à " + nouveau_statut + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.OPERATION_CHANGEMENT_STATUT.toString());
		action.setUuidOperation(operation_edite.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

		
	}

	@Override
	public void declarerIncidentOperation(String incident, Operation operation, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Incident déclaré sur l'opération " + operation.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.DECLARER_INCIDENT.toString());
		action.setUuidOperation(operation.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Override
	public void enregistrementSatisfactionOperation(Operation operation, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Satisfaction déposée sur l'opération " + operation.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.SATISFACTION_OPERATION.toString());
		action.setUuidOperation(operation.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Override
	public void getFacturesClient(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Liste des factures demandées par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.LISTE_FACTURES.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Override
	public void supprimerFacture(String numero_facture, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Suppression de la facture " + numero_facture + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.SUPPRIMER_FACTURE.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
	}

	@Override
	public void getFacture(String numero_facture, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation de la facture " + numero_facture + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTER_FACTURE.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Override
	public void getFacturePDF(String numero_facture, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation du PDF de la facture " + numero_facture + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTER_FACTURE.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
	}

	@Async
	public void affecterOperation(Operation operation_cree, OperationAppelOffre ao, String token) {
	
		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Affecter l'appel d'offre " + ao.getUuid().toString() + " à l'operation " + operation_cree.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.AFFECTER_OPERATION.toString());
		action.setUuidOperation(operation_cree.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());
		
	}



	@Async
	public void annulerOperation(Operation operation, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Annulation de l'opération " + operation.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.ANNULER_OPERATION.toString());
		action.setUuidOperation(operation.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void exportFacturesClients(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Export des factures clients par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EXPORT_FACTURES_DU_CLIENT.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Override
	public void statTopsDestinations(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des tops destinations " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.STATS_TOP_DESTINATIONS.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Override
	public void geolocOperation(Operation operation, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation de la géolocalisation de l'opération " + operation.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.GEOLOC_OPERATION.toString());
		action.setUuidOperation(operation.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	public void getDevis(Devis devis, String token) {


		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation du devis " + devis.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_DEVIS.toString());
		action.setUuidDevis(devis.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());



	}

	@Async
	public void getDevisListe(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des devis par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_LISTE_DEVIS.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}


	@Async
	public void exportDevis(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Export des devis par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EXPORT_DEVIS.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Override
	public void supprimerDevis(Devis devis, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Suppression du devis " + devis.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.SUPPRIMER_DEVIS.toString());
		action.setUuidDevis(devis.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());


	}



	@Override
	public void creerDevis(Devis devis, String code_pays) {


		ActionAudit action = new ActionAudit(code_pays);
		action.setLibelleAction("Création d'un devis " + devis.getCode() + " par un prospect");
		action.setTypeAction(ActionAuditTypeAction.CREER_DEVIS.toString());
		action.setUuidDevis(devis.getUuid().toString());
		action.setCodePays(code_pays);

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Override
	public void changerStatutDevis(Devis devis, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);
		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Changement de statut du devis " + devis.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CREER_DEVIS.toString());
		action.setUuidDevis(devis.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Override
	public void convertionDevisEnOperation(Devis devis, Operation operation, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);
		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Convertion du devis " + devis.getCode() + " en opération " + operation.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CREER_DEVIS.toString());
		action.setUuidDevis(devis.getUuid().toString());
		action.setUuidOperation(operation.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	@Override
	public void creerReclamation(Reclamation reclamation_cree, Operation operation, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);
		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Création d'une réclamation " + reclamation_cree.getCode() + " sur l'opération " + operation.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CREER_RECLAMATION.toString());
		action.setUuidReclamation(reclamation_cree.getUuid().toString());
		action.setUuidOperation(operation.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	@Override
	public void creerReclamationEchange(ReclamationEchange reclamation_cree, Reclamation reclamation_echange_cree, Operation operation, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);
		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Création d'un échange sur la réclamation " + reclamation_echange_cree.getCode() + " sur l'opération " + operation.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CREER_RECLAMATION_ECHANGE.toString());
		action.setUuidReclamation(reclamation_cree.getUuid().toString());
		action.setUuidReclamationEchange(reclamation_echange_cree.getUuid().toString());
		action.setUuidOperation(operation.getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	@Override
	public void getReclamations(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation des réclamations par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_RECLAMATIONS.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	@Override
	public void getReclamation(Reclamation reclamation, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Consultation de la réclamation " + reclamation.getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CONSULTATION_RECLAMATION.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Async
	@Override
	public void exportReclamations(String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);

		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Export des réclamations par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.EXPORT_RECLAMATIONS.toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Override
	public void creerReclamationEchange(ReclamationEchange reclamation_echange_cree, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);
		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Ajout d'un échange à la réclamation " + reclamation_echange_cree.getCode() + " sur l'opération " + reclamation_echange_cree.getReclamation().getOperation().getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.AJOUT_ECHANGE_RECLAMATION.toString());
		action.setUuidReclamation(reclamation_echange_cree.getReclamation().getUuid().toString());
		action.setUuidOperation(reclamation_echange_cree.getReclamation().getOperation().getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

	@Override
	public void changerStatutReclamation(Reclamation reclamation_cree, String token) {

		Map<String, String> claims = jwtProvider.getClaims(token);
		ActionAudit action = new ActionAudit(claims.get("uuid"), jwtProvider.getCodePays(token));
		action.setLibelleAction("Changement du statut de la réclamation " + reclamation_cree.getCode() + " vers le statut " + reclamation_cree.getStatut() + " pour l'opération " + reclamation_cree.getOperation().getCode() + " par " + claims.get("prenom") + " " + claims.get("nom"));
		action.setTypeAction(ActionAuditTypeAction.CHANGEMENT_STATUT_OPERATION.toString());
		action.setUuidReclamation(reclamation_cree.getUuid().toString());
		action.setUuidOperation(reclamation_cree.getOperation().getUuid().toString());
		action.setCodePays(jwtProvider.getCodePays(token));

		action = actionAuditRepository.save(action);

		logger.info("Sauvegarde d'une action d'audit " + action.getUuid().toString());

	}

}
