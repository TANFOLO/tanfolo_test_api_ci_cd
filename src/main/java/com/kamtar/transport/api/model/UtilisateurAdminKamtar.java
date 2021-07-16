package com.kamtar.transport.api.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.*;

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
import com.kamtar.transport.api.params.CreateAdminKamtarParams;
import com.kamtar.transport.api.params.EditAdminKamtarParams;
import com.kamtar.transport.api.utils.UpdatableBCrypt;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Administrateur")
@Entity
@Table(name = "utilisateur_admin_kamtar", indexes = { @Index(name = "idx_utilisateur_admin_kamtar_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class UtilisateurAdminKamtar extends Utilisateur implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Logger de la classe 
	 */
	private static Logger logger = LogManager.getLogger(UtilisateurAdminKamtar.class);  

	public UtilisateurAdminKamtar(String nom, String prenom, String email, String mot_de_passe, String numero_telephone_1,
			String numero_telephone_2, String photo, boolean activate, String code_pays, String locale) {
		
		super();
		this.nom = nom;
		this.prenom = prenom;
		this.email = email;
		this.motDePasse = mot_de_passe;
		this.numeroTelephone1 = numero_telephone_1;
		this.numeroTelephone2 = numero_telephone_2;
		this.photo = photo;
		this.activate = activate;
		this.codePays = code_pays;
		this.locale = locale;
	}
	
	
	public UtilisateurAdminKamtar() {
		super();
	}

	public UtilisateurAdminKamtar(CreateAdminKamtarParams params) {
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
		this.typeDeCompte = UtilisateurTypeDeCompte.ADMIN_KAMTAR.toString();
	}
	
	public void edit(EditAdminKamtarParams params) {
		
		if (params.getMot_de_passe() != null && !"".equals(params.getMot_de_passe().trim())) {
			this.motDePasse = UpdatableBCrypt.hashPassword(params.getMot_de_passe());
		}
		this.nom = params.getNom();
		this.prenom = params.getPrenom();
		this.email = params.getEmail();
		this.numeroTelephone1 = params.getNumero_telephone_1().replaceAll(" ", "");
		this.numeroTelephone2 = params.getNumero_telephone_2() != null ? params.getNumero_telephone_2().replaceAll(" ", "") : "";
		this.activate = params.isActivate();
		this.codePays = params.getCode_pays();
		this.locale = params.getLocale();
	}


}
