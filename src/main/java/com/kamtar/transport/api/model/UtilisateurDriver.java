package com.kamtar.transport.api.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kamtar.transport.api.params.*;
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
import com.kamtar.transport.api.repository.UtilisateurDriverRepository;
import com.kamtar.transport.api.utils.ParrainageUtils;
import com.kamtar.transport.api.utils.UpdatableBCrypt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Driver de véhicules")
@Entity
@Table(name = "utilisateur_driver", indexes = { @Index(name = "idx_utilisateur_driver_codeParrainage", columnList = "codeParrainage"), @Index(name = "idx_utilisateur_driver_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true )
public class UtilisateurDriver extends Utilisateur implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Code de parrainage que ce driver devra fournir à un autre driver pour qu'il devienne parain
	 */
	@ApiModelProperty(notes = "Code de parrainage que ce driver devra fournir à un autre driver pour qu'il devienne parain")
	@Column(name = "codeParrainage", nullable = false, updatable = true)
	protected String codeParrainage;
	
	@ApiModelProperty(notes = "Type de permis (pris dans l'énumération DriverPermis")
	@Column(name = "permisType", nullable = false, updatable = true)
	protected String permisType;
	
	@ApiModelProperty(notes = "Numéro du permis")
	@Column(name = "numeroPermis", nullable = false, updatable = true)
	protected String numeroPermis;

	@ApiModelProperty(notes = "Photo du permis")
	@Column(name = "photoPermis", nullable = true, updatable = true)
	protected String photoPermis;

	@ApiModelProperty(notes = "Code de parrainage du parain")
	@Column(name = "parrain", nullable = true, updatable = true)
	protected String parrain;

	@ApiModelProperty(notes = "Propriétaire qui a créé le driver")
	@OneToOne
	@JoinColumn(name = "proprietaire", foreignKey = @ForeignKey(name = "fk_utilisateur_driver_proprietaire"))
	private UtilisateurProprietaire proprietaire;

	public UtilisateurProprietaire getProprietaire() {
		return proprietaire;
	}

	public void setProprietaire(UtilisateurProprietaire proprietaire) {
		this.proprietaire = proprietaire;
	}

	@JsonProperty("prenom_nom")
	public String getPrenomNom() {
		return this.prenom + " " + this.nom;
	}
	

	public String getParrain() {
		return parrain;
	}


	public void setParrain(String parrain) {
		this.parrain = parrain;
	}


	/**
	 * Logger de la classe 
	 */
	private static Logger logger = LogManager.getLogger(UtilisateurDriver.class);  

	public UtilisateurDriver(String nom, String prenom, String email, String mot_de_passe, String numero_telephone_1,
			String numero_telephone_2, String photo, boolean activate, String code_pays, String locale) {
		
		super();
		this.nom = nom;
		this.prenom = prenom;
		this.email = email;
		this.motDePasse = UpdatableBCrypt.hashPassword(mot_de_passe);
		this.numeroTelephone1 = numero_telephone_1;
		this.numeroTelephone2 = numero_telephone_2;
		this.photo = photo;
		this.activate = activate;
		this.codePays = code_pays;
		this.locale = locale;
	}
	
	
	public UtilisateurDriver() {
		super();
	}

	public UtilisateurDriver(CreateDriverParams params, UtilisateurDriverRepository utilisateurDriverRepository, String code_pays) {
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
		this.typeDeCompte = UtilisateurTypeDeCompte.DRIVER.toString();
		
		this.codeParrainage = ParrainageUtils.generateCodeParrainnage(utilisateurDriverRepository, code_pays);
		
		this.numeroPermis = params.getNumeroPermis();
		this.permisType = params.getPermisType();
		
	}
	
	public UtilisateurDriver(CreateComptePublicParams params, UtilisateurDriverRepository utilisateurDriverRepository, String code_pays) {
		super();
		this.nom = params.getChauffeur_nom();
		this.prenom = params.getChauffeur_prenom();
		this.email = params.getChauffeur_email();
		this.motDePasse = UpdatableBCrypt.hashPassword(params.getChauffeur_password());
		this.numeroTelephone1 = params.getChauffeur_numero_telephone_1().replaceAll(" ", "");
		this.numeroTelephone2 = params.getChauffeur_numero_telephone_2() != null ? params.getChauffeur_numero_telephone_2().replaceAll(" ", "") : "";
		this.activate = false;
		this.codePays = params.getPays();
		this.locale = params.getChauffeur_locale();
		this.typeDeCompte = UtilisateurTypeDeCompte.DRIVER.toString();
		
		this.codeParrainage = ParrainageUtils.generateCodeParrainnage(utilisateurDriverRepository, code_pays);
		
		this.numeroPermis = params.getChauffeur_numeroPermis();
		this.permisType = params.getChauffeur_permisType();
		
	}



	public void edit(EditTransporteurPublicParams params) {

		this.nom = params.getNom();
		this.prenom = params.getPrenom();
		this.email = params.getEmail();
		this.numeroTelephone1 = params.getNumero_telephone_1().replaceAll(" ", "");
		this.numeroTelephone2 = params.getNumero_telephone_2() != null ? params.getNumero_telephone_2().replaceAll(" ", "") : "";

		if (params.getMot_de_passe() != null && !"".equals(params.getMot_de_passe().trim())) {
			this.motDePasse = UpdatableBCrypt.hashPassword(params.getMot_de_passe());
		}

	}


	public void edit(EditDriverParams params) {
		
		this.nom = params.getNom();
		this.prenom = params.getPrenom(); 
		this.email = params.getEmail();
		this.numeroTelephone1 = params.getNumero_telephone_1().replaceAll(" ", "");
		this.numeroTelephone2 = params.getNumero_telephone_2() != null ? params.getNumero_telephone_2().replaceAll(" ", "") : "";
		this.activate = params.isActivate();
		this.codePays = params.getCode_pays();
		this.locale = params.getLocale();
		
		this.numeroPermis = params.getNumeroPermis();
		this.permisType = params.getPermisType();

		if (params.getMot_de_passe() != null && !"".equals(params.getMot_de_passe().trim())) {
			this.motDePasse = UpdatableBCrypt.hashPassword(params.getMot_de_passe());
		}

	}


	public String getCodeParrainage() {
		return codeParrainage;
	}


	public void setCodeParrainage(String codeParrainage) {
		this.codeParrainage = codeParrainage;
	}


	public String getPermisType() {
		return permisType;
	}


	public void setPermisType(String permisType) {
		this.permisType = permisType;
	}


	public String getNumeroPermis() {
		return numeroPermis;
	}


	public void setNumeroPermis(String numeroPermis) {
		this.numeroPermis = numeroPermis;
	}



	public String getPhotoPermis() {
		return photoPermis;
	}


	public void setPhotoPermis(String photoPermis) {
		this.photoPermis = photoPermis;
	}


	public String getCouleur() {
		if (
				this.getNumeroPermis() == null || "".equals(this.getNumeroPermis()) ||
				this.getPermisType() == null || "".equals(this.getPermisType()) ||
				this.getPhoto() == null || "".equals(this.getPhoto()) ||
				this.getPhotoPermis() == null || "".equals(this.getPhotoPermis())) {
			return "O";
		}
		return "V";
	}

}
