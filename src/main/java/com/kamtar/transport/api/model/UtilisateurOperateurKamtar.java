package com.kamtar.transport.api.model;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.kamtar.transport.api.enums.UtilisateurTypeDeCompte;
import com.kamtar.transport.api.params.CreateOperateurKamtarParams;
import com.kamtar.transport.api.params.EditOperateurKamtarParams;
import com.kamtar.transport.api.utils.UpdatableBCrypt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Opérateur")
@Entity
@Table(name = "utilisateur_operateur_kamtar", indexes = { @Index(name = "idx_utilisateur_operateur_kamtar_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class UtilisateurOperateurKamtar extends Utilisateur implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(notes = "Fonction de l'opérateur chez Kamtar (champ libre) comme Commercial, Opérationnel ...")
	@Column(name = "fonction", nullable = true, updatable = true)
	@JsonProperty("fonction")	
	protected String fonction;

	@ApiModelProperty(notes = "Service de l'opérateur chez Kamtar")
	@Column(name = "service", nullable = true, updatable = true)
	@JsonProperty("service")
	protected String service;

	@ApiModelProperty(notes = "Liste des autorisations (suite de 0 et de 1). Position : "
			+ "AFFICHAGE_EXPEDITEURS => 2"
			+ "GESTION_EXPEDITEURS => 3"
			+ "AFFICHAGE_TRANSPORTEURS => 6"
			+ "GESTION_TRANSPORTEURS => 7"
			+ "GESTION_COMMANDES => 12"
			+ "AFFICHAGE_COMMANDE => 13"
			+ "AFFICHAGE_NOTES => 19"
			+ "AFFICHAGE_SMS => 22"
			+ "AFFICHAGE_EMAILS => 23"
			+ "AFFICHAGE_NOTIFICATIONS => 24")
	@Column(name = "liste_droits", nullable = false, updatable = true, length = 200)
	@JsonProperty("liste_droits")	
	protected String liste_droits;

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public UtilisateurOperateurKamtar(String nom, String prenom, String email, String mot_de_passe, String numero_telephone_1,
									  String numero_telephone_2, String photo, boolean activate, String code_pays, String locale, String fonction, String liste_droits) {

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
		this.fonction = fonction;

		// compléter les droits jusqu'à 200 avec des 0
		String liste_droits_completes = liste_droits;
		while (liste_droits_completes.length() < 200) {
			liste_droits_completes = liste_droits_completes + "0";
		}

		this.liste_droits = liste_droits_completes;
	}


	public UtilisateurOperateurKamtar() {
		super();
	}

	public UtilisateurOperateurKamtar(CreateOperateurKamtarParams params) {
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
		this.typeDeCompte = UtilisateurTypeDeCompte.OPERATEUR_KAMTAR.toString();
		this.fonction = params.getFonction();
		this.service = params.getService();

		// compléter les droits jusqu'à 200 avec des 0
		String liste_droits_completes = params.getListe_droits();
		while (liste_droits_completes.length() < 200) {
			liste_droits_completes = liste_droits_completes + "0";
		}

		this.liste_droits = liste_droits_completes;

	}

	public void edit(EditOperateurKamtarParams params) {

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
		this.fonction = params.getFonction();
		this.service = params.getService();

		// compléter les droits jusqu'à 200 avec des 0
		String liste_droits_completes = params.getListe_droits();

		while (liste_droits_completes.length() < 200) {
			liste_droits_completes = liste_droits_completes + "0";
		}

		this.liste_droits = liste_droits_completes;
	}

	/**
	 * Modifie les champs de son propre compte
	 * @param params
	 */
	public void editCompte(EditOperateurKamtarParams params) {

		if (params.getMot_de_passe() != null && !"".equals(params.getMot_de_passe().trim())) {
			this.motDePasse = UpdatableBCrypt.hashPassword(params.getMot_de_passe());
		}
		this.nom = params.getNom();
		this.prenom = params.getPrenom();
		this.email = params.getEmail();
		this.numeroTelephone1 = params.getNumero_telephone_1().replaceAll(" ", "");
		this.numeroTelephone2 = params.getNumero_telephone_2() != null ? params.getNumero_telephone_2().replaceAll(" ", "") : "";
		this.codePays = params.getCode_pays();
		this.locale = params.getLocale();
		this.fonction = params.getFonction();
	}



	public String getFonction() {
		return fonction;
	}


	public void setFonction(String fonction) {
		this.fonction = fonction;
	}


	public String getListe_droits() {
		return liste_droits;
	}


	public void setListe_droits(String liste_droits) {
		this.liste_droits = liste_droits;
	}


}
