package com.kamtar.transport.api.params;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.kamtar.transport.api.validation.NumeroDeTelephoneValidConstraint;
import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;

import io.swagger.annotations.ApiModelProperty;


@NumeroDeTelephoneValidConstraint(message = "{err.user.create.numero_telephone.invalide}")
public class EditOperateurKamtarParams extends ParentParams {
	
	@ApiModelProperty(notes = "Identifiant de l'opérateur", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.user.id}")
	@UUIDObligatoireConstraint(message = "{err.utilisateur.id_invalid}")
	private String id;
	
	@ApiModelProperty(notes = "Nom de l'opérateur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.nom}")
	@Size(min = 1, max = 100, message = "{err.user.create.nom_longueur}") 
	protected String nom;

	@ApiModelProperty(notes = "Prénom de l'opérateur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.prenom}")
	@Size(min = 1, max = 100, message = "{err.user.create.prenom_longueur}") 
	protected String prenom;

	@ApiModelProperty(notes = "Adresse email de l'opérateur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.email}")
	@Size(min = 1, max = 100, message = "{err.user.create.email_longueur}") 
	protected String email;

	/**
	 * Pas de contrainte car le mot de passse peut être vide (= pas de changement)
	 */
	@ApiModelProperty(notes = "Mot de passe de l'opérateur (laisser vide si pas de modification)", allowEmptyValue = true, required = false)
	protected String mot_de_passe;

	@ApiModelProperty(notes = "Numéro de téléphone principal de l'opérateur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.numero_telephone_1}")
	@Size(min = 1, max = 100, message = "{err.user.create.numero_telephone_1_longueur}")
	protected String numero_telephone_1;

	@ApiModelProperty(notes = "Numéro de téléphone secondaire de l'opérateur", allowEmptyValue = true, required = false)
	protected String numero_telephone_2;

	@ApiModelProperty(notes = "Langue de l'opérateur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.locale}")
	protected String locale;

	@ApiModelProperty(notes = "Est ce que l'opérateur est activé ?", allowEmptyValue = false, required = true, dataType = "Booléen")
	@NotNull(message = "{err.user.create.activate}")
    protected boolean activate;

	@ApiModelProperty(notes = "Code pays de l'opérateur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.code_pays}")
	@Size(min = 1, max = 100, message = "{err.user.create.code_pays_longueur}") 
	protected String code_pays;

	@ApiModelProperty(notes = "Fonction de l'opérateur", allowEmptyValue = true, required = false)
	protected String fonction;

	@ApiModelProperty(notes = "Service de l'opérateur", allowEmptyValue = true, required = false)
	protected String service;
	
	@ApiModelProperty(notes = "Liste des drois de l'opérateur", allowEmptyValue = false, required = true, dataType = "Suite de 0 et de 1. Position : "
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
	@Size(min = 1, max = 100, message = "{err.user.create.liste_droit_longueur}") 
	protected String liste_droits;

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
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

	public EditOperateurKamtarParams() {
		super();
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getPrenom() {
		return prenom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMot_de_passe() {
		return mot_de_passe;
	}

	public void setMot_de_passe(String mot_de_passe) {
		this.mot_de_passe = mot_de_passe;
	}

	public String getNumero_telephone_1() {
		return numero_telephone_1;
	}

	public void setNumero_telephone_1(String numero_telephone_1) {
		this.numero_telephone_1 = numero_telephone_1;
	}

	public String getNumero_telephone_2() {
		return numero_telephone_2;
	}

	public void setNumero_telephone_2(String numero_telephone_2) {
		this.numero_telephone_2 = numero_telephone_2;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public boolean isActivate() {
		return activate;
	}

	public void setActivate(boolean activate) {
		this.activate = activate;
	}

	public String getCode_pays() {
		return code_pays;
	}

	public void setCode_pays(String code_pays) {
		this.code_pays = code_pays;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}



}
