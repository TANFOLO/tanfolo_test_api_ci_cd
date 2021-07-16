package com.kamtar.transport.api.params;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


import com.kamtar.transport.api.validation.NumeroDeTelephoneLibreValidConstraint;
import com.kamtar.transport.api.validation.NumeroDeTelephoneValidConstraint;
import io.swagger.annotations.ApiModelProperty;

public class CreateClientAnonymeParams extends ParentParams {
	

	@ApiModelProperty(notes = "Nom du client", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.client.create.nom}")
	@Size(min = 1, max = 250, message = "{err.client.create.nom_longueur}") 
	protected String nom;
	
	@ApiModelProperty(notes = "Prénom du client", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.client.create.nom}")
	@Size(min = 1, max = 100, message = "{err.client.create.nom_longueur}") 
	protected String prenom;
	
	@ApiModelProperty(notes = "Adresse e-mail du resonsable pour se connecter à la webapp expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.client.create.email}")
	@Size(min = 1, max = 250, message = "{err.client.create.email_longueur}") 
	//@EmailAlreadyExistConstraint(message = "{err.user.create.email.existe_deja}")
	protected String email;

	@ApiModelProperty(notes = "Code pays du téléphone du resonsable de l'expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.telephone_code_pays}")
	@Size(min = 1, max = 100, message = "{err.user.create.telephone_code_pays}")
	protected String pays_telephone_1;

	@ApiModelProperty(notes = "Numéro de téléphone du client", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.client.create.telephone}")
	@Size(min = 1, max = 200, message = "{err.client.create.telephone_longueur}")
	@NumeroDeTelephoneLibreValidConstraint(message = "{err.user.create.numero_telephone.invalide}")
	protected String telephone1;

	@ApiModelProperty(notes = "Code pays du téléphone du resonsable de l'expéditeur", allowEmptyValue = true, required = false)
	protected String pays_telephone_2;

	@ApiModelProperty(notes = "Numéro de téléphone du client", allowEmptyValue = false, required = true)
	@NumeroDeTelephoneLibreValidConstraint(message = "{err.user.create.numero_telephone.invalide}")
	protected String telephone2;

	@ApiModelProperty(notes = "Type de compte", allowEmptyValue = false, required = true, dataType = "Chaine de caractères : (B=professionel, C=particulier)")
	@NotNull(message = "{err.client.create.type_de_compte}")
	@Size(min = 1, max = 1, message = "{err.client.create.type_de_compte_longueur}") 
	protected String type_compte;

	@ApiModelProperty(notes = "Mot de passe pour se connecter à la webapp expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.client.create.mot_de_passe}")
	@Size(min = 1, max = 250, message = "{err.client.create.mot_de_passe_longueur}") 
	protected String mot_de_passe;
	
	@ApiModelProperty(notes = "Langue parlée par le repsonsable de l'expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.locale}")
	protected String locale;

	@ApiModelProperty(notes = "Code pays du repsonsable de l'expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.code_pays}")
	@Size(min = 1, max = 100, message = "{err.user.create.code_pays_longueur}") 
	protected String code_pays;

	@ApiModelProperty(notes = "Nom de l'entreprise", allowEmptyValue = true, required = false)
	protected String entreprise_nom;

	@ApiModelProperty(notes = "Compte comptable de l'entreprise", allowEmptyValue = true, required = false)
	protected String entreprise_compte_comptable;

	@ApiModelProperty(notes = "Numéro RCCM de l'entreprise", allowEmptyValue = true, required = false)
	protected String entreprise_numero_rccm;
	
	@ApiModelProperty(notes = "Adresse de facturation (ligne 1) pour les frais d'expédition facturés au client", allowEmptyValue = true, required = false)
	protected String adresse_facturation_ligne_1;

	@ApiModelProperty(notes = "Adresse de facturation (ligne 2) pour les frais d'expédition facturés au client", allowEmptyValue = true, required = false)
	protected String adresse_facturation_ligne_2;

	@ApiModelProperty(notes = "Adresse de facturation (ligne 3) pour les frais d'expédition facturés au client", allowEmptyValue = true, required = false)
	protected String adresse_facturation_ligne_3;

	@ApiModelProperty(notes = "Adresse de facturation (ligne 4) pour les frais d'expédition facturés au client", allowEmptyValue = true, required = false)
	protected String adresse_facturation_ligne_4;

	@ApiModelProperty(notes = "Base64 de la photo de profil")
	protected String photoProfil;

	public void setPays_telephone_1(String pays_telephone_1) {
		this.pays_telephone_1 = pays_telephone_1;
	}

	public String getPays_telephone_2() {
		return pays_telephone_2;
	}

	public void setPays_telephone_2(String pays_telephone_2) {
		this.pays_telephone_2 = pays_telephone_2;
	}

	public String getPays_telephone_1() {
		return pays_telephone_1;
	}

	public void setEntreprise_numero_rccm(String entreprise_numero_rccm) {
		this.entreprise_numero_rccm = entreprise_numero_rccm;
	}

	public String getPhotoProfil() {
		return photoProfil;
	}

	public void setPhotoProfil(String photoProfil) {
		this.photoProfil = photoProfil;
	}

	public String getEntreprise_numero_rccm() {
		return entreprise_numero_rccm;
	}

	public void setEntreprise_nmero_rccm(String entreprise_nmero_rccm) {
		this.entreprise_numero_rccm = entreprise_nmero_rccm;
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


	public String getTelephone1() {
		return telephone1;
	}


	public void setTelephone1(String telephone1) {
		this.telephone1 = telephone1;
	}


	public String getTelephone2() {
		return telephone2;
	}


	public void setTelephone2(String telephone2) {
		this.telephone2 = telephone2;
	}


	public String getEntreprise_nom() {
		return entreprise_nom;
	}


	public void setEntreprise_nom(String entreprise_nom) {
		this.entreprise_nom = entreprise_nom;
	}


	public String getEntreprise_compte_comptable() {
		return entreprise_compte_comptable;
	}


	public void setEntreprise_compte_comptable(String entreprise_compte_comptable) {
		this.entreprise_compte_comptable = entreprise_compte_comptable;
	}


	public String getAdresse_facturation_ligne_1() {
		return adresse_facturation_ligne_1;
	}


	public void setAdresse_facturation_ligne_1(String adresse_facturation_ligne_1) {
		this.adresse_facturation_ligne_1 = adresse_facturation_ligne_1;
	}


	public String getAdresse_facturation_ligne_2() {
		return adresse_facturation_ligne_2;
	}


	public void setAdresse_facturation_ligne_2(String adresse_facturation_ligne_2) {
		this.adresse_facturation_ligne_2 = adresse_facturation_ligne_2;
	}


	public String getAdresse_facturation_ligne_3() {
		return adresse_facturation_ligne_3;
	}


	public void setAdresse_facturation_ligne_3(String adresse_facturation_ligne_3) {
		this.adresse_facturation_ligne_3 = adresse_facturation_ligne_3;
	}


	public String getAdresse_facturation_ligne_4() {
		return adresse_facturation_ligne_4;
	}


	public void setAdresse_facturation_ligne_4(String adresse_facturation_ligne_4) {
		this.adresse_facturation_ligne_4 = adresse_facturation_ligne_4;
	}


	public String getNom() {
		return nom;
	}


	public void setNom(String nom) {
		this.nom = nom;
	}


	public String getType_compte() {
		return type_compte;
	}


	public void setType_compte(String type_compte) {
		this.type_compte = type_compte;
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





}
