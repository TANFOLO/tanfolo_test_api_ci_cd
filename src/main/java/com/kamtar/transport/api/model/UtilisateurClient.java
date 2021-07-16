package com.kamtar.transport.api.model;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.*;

import com.kamtar.transport.api.params.*;
import com.wbc.core.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kamtar.transport.api.enums.UtilisateurTypeDeCompte;
import com.kamtar.transport.api.utils.UpdatableBCrypt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Client")
@Entity
@Table(name = "utilisateur_client", indexes = { @Index(name = "idx_utilisateur_client_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class UtilisateurClient extends Utilisateur implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(notes = "Code permettant de valider le client")
	@Column(name = "code_validation", nullable = true, updatable = true)
	protected String code_validation;

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(UtilisateurClient.class);  


	public UtilisateurClient() {
		super();
	}

	public UtilisateurClient(CreateClientParams params) {
		super();
		this.nom = params.getNom_responsable();
		this.prenom = params.getPrenom_responsable();
		this.email = params.getEmail_responsable();
		this.motDePasse = UpdatableBCrypt.hashPassword(params.getMot_de_passe());
		this.numeroTelephone1 = params.getNumero_telephone1_responsable().replaceAll(" ", "");
		this.numeroTelephone2 = params.getNumero_telephone2_responsable().replaceAll(" ", "");
		this.activate = params.isActivate();
		this.codePays = params.getCode_pays();
		this.locale = params.getLocale();
		this.typeDeCompte = UtilisateurTypeDeCompte.EXPEDITEUR.toString();
		this.numeroTelephone1Pays  = params.getNumero_telephone1_responsable_pays();
		this.numeroTelephone2Pays  = params.getNumero_telephone2_responsable_pays();
	}

	public UtilisateurClient(CreateClientAnonymeParams params) {
		super();
		this.nom = params.getNom();
		this.prenom = params.getPrenom();
		this.email = params.getEmail();
		this.activate = false;
		this.codePays = params.getCode_pays();
		this.locale = params.getLocale();
		this.typeDeCompte = UtilisateurTypeDeCompte.EXPEDITEUR.toString();
		this.motDePasse = UpdatableBCrypt.hashPassword(params.getMot_de_passe());
		this.numeroTelephone1 = params.getTelephone1().replaceAll(" ", "");
		this.numeroTelephone2 = params.getTelephone2().replaceAll(" ", "");
		this.code_validation = StringUtils.generateRandomNumeric(4);
		this.numeroTelephone1Pays  = params.getPays_telephone_1();
		this.numeroTelephone2Pays  = params.getPays_telephone_2();
	}

	public UtilisateurClient(EditClientParams params) {
		super();
		this.nom = params.getNom_responsable();
		this.prenom = params.getPrenom_responsable();
		this.email = params.getEmail_responsable();
		if (params.getMot_de_passe() != null && !"".equals(params.getMot_de_passe().trim())) {
			this.motDePasse = UpdatableBCrypt.hashPassword(params.getMot_de_passe());
		}
		this.numeroTelephone1 = params.getNumero_telephone1_responsable().replaceAll(" ", "");
		this.numeroTelephone2 = params.getNumero_telephone2_responsable().replaceAll(" ", "");
		this.activate = params.isActivate();
		this.codePays = params.getCode_pays();
		this.locale = params.getLocale();
		this.typeDeCompte = UtilisateurTypeDeCompte.EXPEDITEUR.toString();
		this.numeroTelephone1Pays  = params.getNumero_telephone1_responsable_pays();
		this.numeroTelephone2Pays  = params.getNumero_telephone2_responsable_pays();
	}


	public UtilisateurClient(EditClientPublicParams params) {
		super();
		this.nom = params.getNom_responsable();
		this.prenom = params.getPrenom_responsable();
		this.email = params.getEmail_responsable();
		if (params.getMot_de_passe() != null && !"".equals(params.getMot_de_passe().trim())) {
			this.motDePasse = UpdatableBCrypt.hashPassword(params.getMot_de_passe());
		}
		this.numeroTelephone1 = params.getNumero_telephone1_responsable().replaceAll(" ", "");
		this.numeroTelephone2 = params.getNumero_telephone2_responsable().replaceAll(" ", "");
		this.numeroTelephone1Pays  = params.getNumero_telephone1_responsable_pays();
		this.numeroTelephone2Pays  = params.getNumero_telephone2_responsable_pays();
	}

    public UtilisateurClient(Devis devis, String mot_de_passe) {
		super();
		this.nom = devis.getClientNom();
		this.prenom = devis.getClientPrenom();
		this.email = devis.getClientEmail();
		this.activate = false;
		this.codePays = devis.getCodePays();
		this.locale = "fr_CI";
		this.typeDeCompte = UtilisateurTypeDeCompte.EXPEDITEUR.toString();
		this.motDePasse = UpdatableBCrypt.hashPassword(mot_de_passe);
		this.numeroTelephone1 = devis.getClientTelephone();
		this.numeroTelephone1Pays  = devis.getClientTelephonePays();
		this.creeParDevis = true;
    }

    public void edit(EditClientParams params) {

		if (params.getMot_de_passe() != null && !"".equals(params.getMot_de_passe().trim())) {
			this.motDePasse = UpdatableBCrypt.hashPassword(params.getMot_de_passe());
		}
		this.nom = params.getNom_responsable();
		this.prenom = params.getPrenom_responsable();
		this.email = params.getEmail_responsable();
		this.numeroTelephone1 = params.getNumero_telephone1_responsable().replaceAll(" ", "");
		this.numeroTelephone2 = params.getNumero_telephone2_responsable().replaceAll(" ", "");
		this.activate = params.isActivate();
		this.codePays = params.getCode_pays();
		this.locale = params.getLocale();
		this.numeroTelephone1Pays  = params.getNumero_telephone1_responsable_pays();
		this.numeroTelephone2Pays  = params.getNumero_telephone2_responsable_pays();
	}


	public void edit(EditClientPublicParams params) {

		if (params.getMot_de_passe() != null && !"".equals(params.getMot_de_passe().trim())) {
			this.motDePasse = UpdatableBCrypt.hashPassword(params.getMot_de_passe());
		}
		this.nom = params.getNom_responsable();
		this.prenom = params.getPrenom_responsable();
		this.email = params.getEmail_responsable();
		this.numeroTelephone1 = params.getNumero_telephone1_responsable().replaceAll(" ", "");
		this.numeroTelephone2 = params.getNumero_telephone2_responsable().replaceAll(" ", "");
		this.numeroTelephone1Pays  = params.getNumero_telephone1_responsable_pays();
		this.numeroTelephone2Pays  = params.getNumero_telephone2_responsable_pays();
	}

	public String getCode_validation() {
		return code_validation;
	}

	public void setCode_validation(String code_validation) {
		this.code_validation = code_validation;
	}
}
