package com.kamtar.transport.api.params;


import com.kamtar.transport.api.validation.NumeroDeTelephoneLibreValidConstraint;
import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreateClientPersonnelParams extends ParentParams {


	@ApiModelProperty(notes = "Identifiant de l'expéditeur", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.client.id}")
	@UUIDObligatoireConstraint(message = "{err.client.id_invalid}")
	private String client;

	@ApiModelProperty(notes = "Langue du responsable de l'expéditeur", allowEmptyValue = false, required = true)
	//@NotNull(message = "{err.user.create.locale}")
	protected String locale;

	@ApiModelProperty(notes = "Code pays du responsable de l'expéditeur", allowEmptyValue = false, required = true)
	//@NotNull(message = "{err.user.create.code_pays}")
	//@Size(min = 1, max = 100, message = "{err.user.create.code_pays_longueur}")
	protected String code_pays;

	@ApiModelProperty(notes = "Nom du responsable de l'expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.nom}")
	@Size(min = 1, max = 100, message = "{err.user.create.nom_longueur}")
	protected String nom;

	@ApiModelProperty(notes = "¨Prénom du responsable de l'expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.prenom}")
	@Size(min = 1, max = 100, message = "{err.user.create.prenom_longueur}")
	protected String prenom;

	@ApiModelProperty(notes = "Adresse e-mail du responsable de l'expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.email}")
	@Size(min = 1, max = 100, message = "{err.user.create.email_longueur}")
	protected String email;

	@ApiModelProperty(notes = "Pays du numéro de téléphone principal du responsable de l'expéditeur", allowEmptyValue = true, required = false)
	@NotNull(message = "{err.user.create.telephone_code_pays}")
	@Size(min = 1, max = 100, message = "{err.user.create.telephone_code_pays_longueur}")
	protected String numero_telephone1_pays;

	@ApiModelProperty(notes = "Numéro de téléphone principal du responsable de l'expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.numero_telephone_1}")
	@Size(min = 1, max = 100, message = "{err.user.create.numero_telephone_1_longueur}")
	@NumeroDeTelephoneLibreValidConstraint(message = "{err.user.create.numero_telephone.invalide}")
	protected String numero_telephone1;

	@ApiModelProperty(notes = "Pays du numéro de téléphone principal du responsable de l'expéditeur", allowEmptyValue = true, required = false)
	protected String numero_telephone2_pays;

	@ApiModelProperty(notes = "Numéro de téléphone secondaire du responsable de l'expéditeur", allowEmptyValue = false, required = true)
	@NumeroDeTelephoneLibreValidConstraint(message = "{err.user.create.numero_telephone.invalide}")
	protected String numero_telephone2;

	@ApiModelProperty(notes = "Mot de passe pour se connecter à la webapp expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.mot_de_passe}")
	@Size(min = 1, max = 100, message = "{err.user.create.mot_de_passe_longueur}")
	protected String mot_de_passe;

	@ApiModelProperty(notes = "Est ce que le client est activé ?", allowEmptyValue = false, required = true, dataType = "Booléen")
	@NotNull(message = "{err.user.create.activate}")
	protected boolean activate;

	@ApiModelProperty(notes = "Chaine de droit pour le client personnel", allowEmptyValue = false, required = true)
	protected String liste_droits;


	public String getListe_droits() {
		return liste_droits;
	}

	public void setListe_droits(String liste_droits) {
		this.liste_droits = liste_droits;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getMot_de_passe() {
		return mot_de_passe;
	}

	public void setMot_de_passe(String mot_de_passe) {
		this.mot_de_passe = mot_de_passe;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getCode_pays() {
		return code_pays;
	}

	public void setCode_pays(String code_pays) {
		this.code_pays = code_pays;
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

	public String getNumero_telephone1_pays() {
		return numero_telephone1_pays;
	}

	public void setNumero_telephone1_pays(String numero_telephone1_pays) {
		this.numero_telephone1_pays = numero_telephone1_pays;
	}

	public String getNumero_telephone1() {
		return numero_telephone1;
	}

	public void setNumero_telephone1(String numero_telephone1) {
		this.numero_telephone1 = numero_telephone1;
	}

	public String getNumero_telephone2_pays() {
		return numero_telephone2_pays;
	}

	public void setNumero_telephone2_pays(String numero_telephone2_pays) {
		this.numero_telephone2_pays = numero_telephone2_pays;
	}

	public String getNumero_telephone2() {
		return numero_telephone2;
	}

	public void setNumero_telephone2(String numero_telephone2) {
		this.numero_telephone2 = numero_telephone2;
	}

	public boolean isActivate() {
		return activate;
	}

	public void setActivate(boolean activate) {
		this.activate = activate;
	}
}
