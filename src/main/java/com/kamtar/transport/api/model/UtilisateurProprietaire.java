package com.kamtar.transport.api.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.*;

import com.kamtar.transport.api.params.*;
import com.kamtar.transport.api.repository.UtilisateurDriverRepository;
import com.kamtar.transport.api.repository.UtilisateurProprietaireRepository;
import com.kamtar.transport.api.utils.ParrainageUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kamtar.transport.api.controller.UtilisateurBackofficeKamtarController;
import com.kamtar.transport.api.enums.UtilisateurTypeDeCompte;
import com.kamtar.transport.api.utils.UpdatableBCrypt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Propriétaire de véhicules")
@Entity
@Table(name = "utilisateur_proprietaire", indexes = { @Index(name = "idx_utilisateur_proprietaire_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class UtilisateurProprietaire extends Utilisateur implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(notes = "Numéro de la carte de transport")
	@Column(name = "numeroCarteTransport", nullable = true, updatable = true)
	protected String numeroCarteTransport;
	
	@ApiModelProperty(notes = "Date d'établissement de la carte de transport")
	@Column(name = "dateEtablissementCarteTransport", nullable = false, updatable = true)
	protected Date dateEtablissementCarteTransport;

	@ApiModelProperty(notes = "Photo de la carte de transport")
	@Column(name = "photoCarteTransport", nullable = true, updatable = true)
	protected String photoCarteTransport;

	/**
	 * Code de parrainage que ce driver devra fournir à un autre driver pour qu'il devienne parain
	 */
	@ApiModelProperty(notes = "Code de parrainage que ce driver devra fournir à un autre driver pour qu'il devienne parain")
	@Column(name = "codeParrainage", nullable = false, updatable = true)
	protected String codeParrainage;

	/**
	 * Code du parrain saisi à l'inscription
	 */
	@ApiModelProperty(notes = "Code du parrain saisi à l'inscription")
	@Column(name = "codeParrain", nullable = true, updatable = true)
	protected String codeParrain;

	@ApiModelProperty(notes = "Est ce que le propriétaire est assujeti à l'AIRSI ?")
	@Column(name = "assujetiAIRSI", nullable = false, updatable = true)
	protected boolean assujetiAIRSI;

	@ApiModelProperty(notes = "Type de compte du propriétaire (B = pro, C = particulier)")
	@Column(updatable = true, name = "typeCompte", nullable = false, length=1, unique = false)
	private String typeCompte;

	@ApiModelProperty(notes = "Nom de la société")
	@Column(updatable = true, name = "nomSociete", nullable = true, length=250, unique = false)
	private String nomSociete;

	@ApiModelProperty(notes = "Compte contribuable")
	@Column(updatable = true, name = "compteContribuable", nullable = true, unique = false)
	private String compteContribuable;

	@ApiModelProperty(notes = "Compte contribuable")
	@Column(updatable = true, name = "numeroRCCM", nullable = true, unique = false)
	private String numeroRCCM;

	@ApiModelProperty(notes = "Adresse de facturation - première ligne")
	@Column(updatable = true, name = "adresseFacturationLigne1", nullable = true, length=200, unique = false)
	private String adresseFacturationLigne1;

	@ApiModelProperty(notes = "Adresse de facturation - seconde ligne")
	@Column(updatable = true, name = "adresseFacturationLigne2", nullable = true, length=200, unique = false)
	private String adresseFacturationLigne2;

	@ApiModelProperty(notes = "Adresse de facturation - troisième ligne")
	@Column(updatable = true, name = "adresseFacturationLigne3", nullable = true, length=200, unique = false)
	private String adresseFacturationLigne3;

	@ApiModelProperty(notes = "Adresse de facturation - quatrième ligne")
	@Column(updatable = true, name = "adresseFacturationLigne4", nullable = true, length=200, unique = false)
	private String adresseFacturationLigne4;

	@ApiModelProperty(notes = "Nom de l'intermédiaire")
	@Column(updatable = true, name = "intermediaireNom", nullable = true, length=200, unique = false)
	private String intermediaireNom;

	@ApiModelProperty(notes = "Prénom de l'intermédiaire")
	@Column(updatable = true, name = "intermediairePrenom", nullable = true, length=200, unique = false)
	private String intermediairePrenom;

	@ApiModelProperty(notes = "Téléphone de l'intermédiaire")
	@Column(updatable = true, name = "intermediaireTelephone", nullable = true, length=200, unique = false)
	private String intermediaireTelephone;

	@ApiModelProperty(notes = "Email de l'intermédiaire")
	@Column(updatable = true, name = "intermediaireEmail", nullable = true, length=200, unique = false)
	private String intermediaireEmail;

	public String getNomSociete() {
		return nomSociete;
	}

	public void setNomSociete(String nomSociete) {
		this.nomSociete = nomSociete;
	}

	public String getCompteContribuable() {
		return compteContribuable;
	}

	public void setCompteContribuable(String compteContribuable) {
		this.compteContribuable = compteContribuable;
	}

	public String getNumeroRCCM() {
		return numeroRCCM;
	}

	public void setNumeroRCCM(String numeroRCCM) {
		this.numeroRCCM = numeroRCCM;
	}

	public String getIntermediaireNom() {
		return intermediaireNom;
	}

	public void setIntermediaireNom(String intermediaireNom) {
		this.intermediaireNom = intermediaireNom;
	}

	public String getIntermediairePrenom() {
		return intermediairePrenom;
	}

	public void setIntermediairePrenom(String intermediairePrenom) {
		this.intermediairePrenom = intermediairePrenom;
	}

	public String getIntermediaireTelephone() {
		return intermediaireTelephone;
	}

	public void setIntermediaireTelephone(String intermediaireTelephone) {
		this.intermediaireTelephone = intermediaireTelephone;
	}

	public String getIntermediaireEmail() {
		return intermediaireEmail;
	}

	public void setIntermediaireEmail(String intermediaireEmail) {
		this.intermediaireEmail = intermediaireEmail;
	}

	public String getTypeCompte() {
		return typeCompte;
	}

	public void setTypeCompte(String typeCompte) {
		this.typeCompte = typeCompte;
	}

	public String getAdresseFacturationLigne1() {
		return adresseFacturationLigne1;
	}

	public void setAdresseFacturationLigne1(String adresseFacturationLigne1) {
		this.adresseFacturationLigne1 = adresseFacturationLigne1;
	}

	public String getAdresseFacturationLigne2() {
		return adresseFacturationLigne2;
	}

	public void setAdresseFacturationLigne2(String adresseFacturationLigne2) {
		this.adresseFacturationLigne2 = adresseFacturationLigne2;
	}

	public String getAdresseFacturationLigne3() {
		return adresseFacturationLigne3;
	}

	public void setAdresseFacturationLigne3(String adresseFacturationLigne3) {
		this.adresseFacturationLigne3 = adresseFacturationLigne3;
	}

	public String getAdresseFacturationLigne4() {
		return adresseFacturationLigne4;
	}

	public void setAdresseFacturationLigne4(String adresseFacturationLigne4) {
		this.adresseFacturationLigne4 = adresseFacturationLigne4;
	}

	public boolean isAssujetiAIRSI() {
		return assujetiAIRSI;
	}

	public void setAssujetiAIRSI(boolean assujetiAIRSI) {
		this.assujetiAIRSI = assujetiAIRSI;
	}

	public String getCodeParrainage() {
		return codeParrainage;
	}

	public void setCodeParrainage(String codeParrainage) {
		this.codeParrainage = codeParrainage;
	}

	public String getCodeParrain() {
		return codeParrain;
	}

	public void setCodeParrain(String codeParrain) {
		this.codeParrain = codeParrain;
	}

	/**
	 * Logger de la classe 
	 */
	private static Logger logger = LogManager.getLogger(UtilisateurProprietaire.class);  


	
	public UtilisateurProprietaire() {
		super();
	}

	public UtilisateurProprietaire(CreateProprietaireParams params, UtilisateurProprietaireRepository utilisateurProprietaireRepository, String code_pays) {
		super();
		this.nom = params.getNom();
		this.prenom = params.getPrenom();
		this.email = params.getEmail();
		this.motDePasse = UpdatableBCrypt.hashPassword(params.getMot_de_passe());
		this.numeroTelephone1 = params.getNumero_telephone_1().replaceAll(" ", "");
		this.numeroTelephone2 = params.getNumero_telephone_2() != null ? params.getNumero_telephone_2().replaceAll(" ", "") : "";
		this.activate = params.isActivate();
		this.codePays = params.getCode_pays();
		this.locale = params.getLocale();
		this.typeDeCompte = UtilisateurTypeDeCompte.PROPRIETAIRE.toString();
		this.typeCompte = params.getType_compte();
		this.assujetiAIRSI = params.isAssujetiAIRSI();
		this.compteContribuable = params.getEntreprise_compte_comptable();
		this.numeroRCCM = params.getEntreprise_numero_rccm();
		this.nomSociete = params.getEntreprise_nom();
		this.adresseFacturationLigne1 = params.getAdresse_facturation_ligne_1();
		this.adresseFacturationLigne2 = params.getAdresse_facturation_ligne_2();
		this.adresseFacturationLigne3 = params.getAdresse_facturation_ligne_3();
		this.adresseFacturationLigne4 = params.getAdresse_facturation_ligne_4();

		this.intermediaireEmail = params.getIntermediaireEmail();
		this.intermediaireNom = params.getIntermediaireNom();
		this.intermediairePrenom = params.getIntermediairePrenom();
		this.intermediaireTelephone = params.getIntermediaireTelephone();

		this.codeParrainage = ParrainageUtils.generateCodeParrainnage(utilisateurProprietaireRepository, code_pays);
		
		this.dateEtablissementCarteTransport = params.getDateEtablissementCarteTransport();
		this.numeroCarteTransport = params.getNumeroCarteTransport();
	}
	
	public UtilisateurProprietaire(CreateComptePublicParams params, UtilisateurProprietaireRepository utilisateurProprietaireRepository, String code_pays) {
		super();
		this.nom = params.getProprietaire_nom();
		this.prenom = params.getProprietaire_prenom();
		this.email = params.getProprietaire_email();
		this.motDePasse = UpdatableBCrypt.hashPassword(params.getProprietaire_password());
		this.numeroTelephone1 = params.getProprietaire_numero_telephone_1().replaceAll(" ", "");
		this.numeroTelephone2 = params.getProprietaire_numero_telephone_2() != null ? params.getProprietaire_numero_telephone_2().replaceAll(" ", "") : "";
		this.activate = false;
		this.codePays = params.getPays();
		this.locale = params.getProprietaire_locale();
		this.typeDeCompte = UtilisateurTypeDeCompte.PROPRIETAIRE.toString();
		this.codeParrain = params.getProprietaire_codeParrainage();
		this.dateEtablissementCarteTransport = params.getProprietaire_dateEtablissementCarteTransport();
		this.numeroCarteTransport = params.getProprietaire_numeroCarteTransport();
		this.codeParrainage = ParrainageUtils.generateCodeParrainnage(utilisateurProprietaireRepository, code_pays);
		this.typeCompte = params.getType_compte();
		this.assujetiAIRSI = params.isAssujetiAIRSI();

		this.nomSociete = params.getEntreprise_nom();
		this.numeroRCCM = params.getEntreprise_numero_rccm();
		this.compteContribuable = params.getEntreprise_compte_comptable();
	}
	
	
	public void edit(EditProprietaireParams params) {
		
		this.nom = params.getNom();
		this.prenom = params.getPrenom();
		this.email = params.getEmail();
		this.numeroTelephone1 = params.getNumero_telephone_1().replaceAll(" ", "");
		this.numeroTelephone2 = params.getNumero_telephone_2() != null ? params.getNumero_telephone_2().replaceAll(" ", "") : "";
		this.activate = params.isActivate();
		this.codePays = params.getCode_pays();
		this.locale = params.getLocale();
		this.typeCompte = params.getType_compte();
		this.compteContribuable = params.getEntreprise_compte_comptable();
		this.numeroRCCM = params.getEntreprise_numero_rccm();
		this.nomSociete = params.getEntreprise_nom();
		this.adresseFacturationLigne1 = params.getAdresse_facturation_ligne_1();
		this.adresseFacturationLigne2 = params.getAdresse_facturation_ligne_2();
		this.adresseFacturationLigne3 = params.getAdresse_facturation_ligne_3();
		this.adresseFacturationLigne4 = params.getAdresse_facturation_ligne_4();

		this.intermediaireEmail = params.getIntermediaireEmail();
		this.intermediaireNom = params.getIntermediaireNom();
		this.intermediairePrenom = params.getIntermediairePrenom();
		this.intermediaireTelephone = params.getIntermediaireTelephone();

		this.assujetiAIRSI = params.isAssujetiAIRSI();
		this.dateEtablissementCarteTransport = params.getDateEtablissementCarteTransport();
		this.numeroCarteTransport = params.getNumeroCarteTransport();

		if (params.getMot_de_passe() != null && !"".equals(params.getMot_de_passe().trim())) {
			this.motDePasse = UpdatableBCrypt.hashPassword(params.getMot_de_passe());
		}
	}




	public void edit(EditProprietairePublicParams params) {

		this.nom = params.getNom();
		this.prenom = params.getPrenom();
		this.email = params.getEmail();
		this.numeroTelephone1 = params.getNumero_telephone_1().replaceAll(" ", "");
		this.numeroTelephone2 = params.getNumero_telephone_2() != null ? params.getNumero_telephone_2().replaceAll(" ", "") : "";
		this.codePays = params.getCode_pays();
		this.locale = params.getLocale();
		this.typeCompte = params.getType_compte();
		this.adresseFacturationLigne1 = params.getAdresse_facturation_ligne_1();
		this.adresseFacturationLigne2 = params.getAdresse_facturation_ligne_2();
		this.adresseFacturationLigne3 = params.getAdresse_facturation_ligne_3();
		this.adresseFacturationLigne4 = params.getAdresse_facturation_ligne_4();

		this.compteContribuable = params.getEntreprise_compte_comptable();
		this.numeroRCCM = params.getEntreprise_numero_rccm();
		this.nomSociete = params.getEntreprise_nom();

		this.assujetiAIRSI = params.isAssujetiAIRSI();
		this.dateEtablissementCarteTransport = params.getDateEtablissementCarteTransport();
		this.numeroCarteTransport = params.getNumeroCarteTransport();

		if (params.getMot_de_passe() != null && !"".equals(params.getMot_de_passe().trim())) {
			this.motDePasse = UpdatableBCrypt.hashPassword(params.getMot_de_passe());
		}
	}


	public String getNumeroCarteTransport() {
		return numeroCarteTransport;
	}


	public void setNumeroCarteTransport(String numeroCarteTransport) {
		this.numeroCarteTransport = numeroCarteTransport;
	}


	public Date getDateEtablissementCarteTransport() {
		return dateEtablissementCarteTransport;
	}


	public void setDateEtablissementCarteTransport(Date dateEtablissementCarteTransport) {
		this.dateEtablissementCarteTransport = dateEtablissementCarteTransport;
	}


	public String getPhotoCarteTransport() {
		return photoCarteTransport;
	}


	public void setPhotoCarteTransport(String photoCarteTransport) {
		this.photoCarteTransport = photoCarteTransport;
	}


	public String getCouleur() {
		if (this.getNumeroCarteTransport() == null || "".equals(this.getNumeroCarteTransport().trim()) || this.getPhotoCarteTransport() == null || "".equals(this.getPhotoCarteTransport().trim())) {
			return "O";
		} else if (this.getDateEtablissementCarteTransport() == null) {
			return "R";
		}
		return "V";
	}
	
	
}
