package com.kamtar.transport.api.service;

import java.util.UUID;

import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.DeclarerIncidentOperationParams;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.kamtar.transport.api.model.UtilisateurDriver;

@Service
public interface ActionAuditService {
	

	public Page<ActionAudit> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<ActionAudit> conditions);
	public Long countAll(Specification<ActionAudit> conditions);

	public void loginAdminKamtar(UtilisateurAdminKamtar admin, String code_pays);
	public void loginOperateurKamtar(UtilisateurOperateurKamtar operateur, String code_pays);
	public void creerAdminKamtar(UtilisateurAdminKamtar admin_cree, String token);
	public void creerOperateurKamtar(UtilisateurOperateurKamtar operateur_cree, String token);
	public void editerAdminKamtar(UtilisateurAdminKamtar admin_cree, String token);
	public void supprimerAdminKamtar(UtilisateurAdminKamtar admin_cree, String token);
	public void editerOperateurKamtar(UtilisateurOperateurKamtar operateur_cree, String token);
	public void supprimerOperateurKamtar(UtilisateurOperateurKamtar operateur_supprime, String token);
	public void getAdminKamtar(UtilisateurAdminKamtar operateur_cree, String token);
	public void getOperateurKamtar(UtilisateurOperateurKamtar operateur_cree, String token);
	public void getAdminsKamtar(String token);
	public void exportAdminsKamtar(String token);
	public void getOperateursKamtar(String token);
	public void exportOperateursKamtar(String token);

	public void creerProprietaire(UtilisateurProprietaire operateur_cree, String token);
	public void editerProprietaire(UtilisateurProprietaire admin_cree, String token);
	public void supprimerProprietaire(UtilisateurProprietaire admin_cree, String token);
	public void getProprietaire(UtilisateurProprietaire operateur_cree, String token);
	public void getProprietaires(String token);
	public void exportProprietaires(String token);

	public void creerVehicule(Vehicule operateur_cree, String token);
	public void editerVehicule(Vehicule admin_cree, String token);
	public void changerDisponibiliteVehicule(Vehicule admin_cree, String token);
	public void supprimerVehicule(Vehicule admin_cree, String token);
	public void getVehicule(Vehicule operateur_cree, String token);
	public void getPhotosVehicule(Vehicule operateur_cree, String token);
	public void getVehicules(String token);
	public void exportVehicules(String token);
	public void getGeolocVehicule(Vehicule vehicule, String token);
	public void getGeolocsVehicule(String token);
	public void getGeolocsVehicule(Vehicule vehicule, String token);
	
	public void creerDriver(UtilisateurDriver operateur_cree, String token);
	public void editerDriver(UtilisateurDriver admin_cree, String token);
	public void supprimerDriver(UtilisateurDriver admin_cree, String token);
	public void getDriver(UtilisateurDriver operateur_cree, String token);
	public void getDrivers(String token);
	public void exportDrivers(String token);

	public void creerClient(Client client_cree, String token);
	public void creerClient(Client client_cree, String pays, boolean diff);
	public void editerClient(Client client_edite, String token);
	public void deleteClient(Client client_edite, String token);
	public void getClient(Client client, String token);
	public void getClients(String token);
	public void exportClients(String token);
	public void validerClient(UtilisateurClient client, String pays);

	public void creerClientPersonnel(UtilisateurClientPersonnel client_cree, String token);
	public void editerClientPersonnel(UtilisateurClientPersonnel client_edite, String token);
	public void deleteClientPersonnel(UtilisateurClientPersonnel client_edite, String token);
	public void getClientPersonnel(UtilisateurClientPersonnel client, String token);
	public void getClientsPersonnels(String token);
	public void loginClientPersonnel(UtilisateurClientPersonnel client, String token);
	public void getClientsPersonnels(Client client, String token);
	
	public void creerTransporteur(UtilisateurDriver transporteur_cree, String token);
	public void editerTransporteur(UtilisateurDriver transporteur_edite, String token);
	public void supprimerTransporteur(UtilisateurDriver transporteur_edite, String token);
	public void getTransporteur(UtilisateurDriver transporteur, String token);
	public void getTransporteurs(String token);

	public void loginTransporteur(UtilisateurDriver transporteur, String token);
	public void loginClient(UtilisateurClient client, String token);
	public void loginProprietaire(UtilisateurProprietaire proprietaire, String token);


	public void creerLanguage(Language languages_cree, String token);
	public void getLanguages(String token);
	
	public void creerCountry(Country country_cree, String token);
	public void getCountries(String token);

	public void contact(Contact contact, Utilisateur utilisateur, String token);
	public void motDePassePerdu(String utilisateur, MotDePassePerdu mdp, String tel_login, String token);
	
	public void getActionAudit(String token);
	public void getActionAuditAdministrateurKamtar(String token, String admin);
	public void getActionAuditOperateurKamtar(String token, String operateur);
	public void getActionAuditClient(String token, String client);
	public void getActionAuditTransporteur(String token, String transporteur);
	public void getActionAuditProprietaire(String token, String operateur);

	public void getListeSMS(String token);
	public void getListeEmails(String token);

	public void getNotificationsBackoffice(String token);
	public void notificationTraitee(Notification notification, String token);

	public void affecterOperation(Operation operation_cree, OperationAppelOffre ao, String token);
	public void creerOperation(Operation operation_cree, String token);
	public void creerOperation(Operation operation_cree, String code_pays, String uuid_utilisateur, String prenom_utilisateur, String nom_utilisateur);

	public void creerOperationParDuplication(Operation operation_cree, String code_pays);
	public void creerOperationParClient(Operation operation_cree, String token, Client client);
	public void editerOperation(Operation operation_edite, String token);
	public void supprimerOperation(Operation operation_edite, String token);
	public void dupliquerOperation(Operation operation_edite, String token);
	public void getOperation(Operation operation, String token);
	public void getOperations(String token);
	public void exportOperations(String token);
	public void getOperationsTransporteur(String token, UtilisateurDriver transporteur);
	public void getOperationsProprietaire(String token, UtilisateurProprietaire proprietaire);
	public void getOperationsClient(String token, Client client);
	public void changerOrdreOperationsTransporteur(String token, UtilisateurDriver transporteur);
	public void getOperationDocuments(Operation operation, String token);

	public void creerOperationAppelOffre(OperationAppelOffre operation_appel_offre_driver, String token);
	public void getOperationsAppelOffreOperation(String token, Operation operation);
	public void getOperationsAppelOffreProprietaire(String token, UtilisateurProprietaire proprietaire);
	public void getOperationsAppelOffreDriver(String token, UtilisateurDriver driver);
	public void enregistrerPropositionAppelOffre(String token, OperationAppelOffre operation_appel_offre, Float montant, UtilisateurDriver transporteur, UtilisateurProprietaire proprietaire);

	public void changerStatutOperation(Operation operation_edite, String token, String ancien_statut, String nouveau_statut);
	public void declarerIncidentOperation(String incident, Operation operation, String token);
	public void enregistrementSatisfactionOperation(Operation operation, String token);
	public void getFacturesClient(String token);
	public void supprimerFacture(String numero_facture, String token);
	public void getFacture(String numero_facture, String token);
	public void exportFacturesClient(String token);
	public void exportFacturesProprietaire(String token);
	public void getFacturePDF(String numero_facture, String token);
	public void annulerOperation(Operation operation, String token);
	public void exportFacturesClients(String token);

	public void statTopsDestinations(String token);
	public void geolocOperation(Operation operation, String token);

	public void getDevis(Devis devis, String token);
	public void getDevisListe(String token);
	public void exportDevis(String token);
	public void supprimerDevis(Devis admin_cree, String token);
	public void creerDevis(Devis devis, String code_pays);
	public void changerStatutDevis(Devis devis, String token);
	public void convertionDevisEnOperation(Devis devis, Operation operation, String token);

	public void creerReclamation(Reclamation reclamation_cree, Operation operation, String token);
	public void creerReclamationEchange(ReclamationEchange reclamation_cree, Reclamation reclamation_echange_cree, Operation operation, String token);
	public void getReclamations(String token);
	public void getReclamation(Reclamation reclamation, String token);
	public void exportReclamations(String token);
	public void creerReclamationEchange(ReclamationEchange reclamation_echange_cree, String token);
	public void changerStatutReclamation(Reclamation reclamation_cree, String token);
}
