package com.kamtar.transport.api.service;



import java.util.List;

import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.swagger.ListOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


@Service
public interface EmailToSendService {
	
	Boolean envoyerLienMotDePassePerdu(Utilisateur utilisateur, MotDePassePerdu mot_de_passe, String code_pays);
	Boolean envoyerConfirmationOperation(Operation operation, String code_pays);
	Boolean envoyerFinOperation(Operation operation, String code_pays);
	Boolean envoyerFactureClient(FactureClient facture, String code_pays);
	Boolean prevenirKamtarNouveauCompteClient(Client client, String code_pays);
	Boolean prevenirKamtarNouveauDriverCreeParProprietaire(UtilisateurDriver driver, String code_pays);
	Boolean prevenirKamtarNouveauVehiculeCreeParProprietaire(Vehicule vehicule, String code_pays);
	Boolean prevenirKamtarNouvelleCommandeClient(ListOperation operations, String code_pays, Integer nbCamions);
	Boolean envoyerConfirmationCreationCompte(UtilisateurClient client, String code_pays);
	Boolean prevenirKamtarNouveauDevis(ListOperation operations, String code_pays);
	Boolean prevenirKamtarNouvelleReclamation(Reclamation reclamation, String code_pays);
	Boolean confirmerClientNouvelleReclamation(Reclamation reclamation, String code_pays);
	Boolean prevenirClientNouvelleReclamationEchange(ReclamationEchange reclamation_echange, Reclamation reclamation, String code_pays);

	Boolean envoyerPropositionPrixOperation(Operation operation, String code_pays);
	Boolean prevenirOperationAPartirDeDevis(Operation operation, String mot_de_passe, String code_pays);

	Boolean envoyerRapportJournalierClient(ListOperation operations, String client_email, String client_nom, String code_pays);
	Boolean envoyerRapportHeboduClient(Long nb_operations_programmees, Long nb_operations_en_cours, Long nb_operations_terminees, String client_email, String client_nom, String code_pays, Utilisateur client_utilisateur);

	public byte[] getMicroserviceSendEmail(String uuid_email) ;
}
