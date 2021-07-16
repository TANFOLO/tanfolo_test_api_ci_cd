package com.kamtar.transport.api.params;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.kamtar.transport.api.validation.*;

import io.swagger.annotations.ApiModelProperty;


@NumeroDeTelephoneValidConstraint(message = "{err.user.create.numero_telephone.invalide}")
public class CreateDriverParams extends ParentParams {

	@ApiModelProperty(notes = "Nom du driver", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.nom}")
	@Size(min = 1, max = 100, message = "{err.user.create.nom_longueur}") 
	protected String nom;

	@ApiModelProperty(notes = "Prénom du driver", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.prenom}")
	@Size(min = 1, max = 100, message = "{err.user.create.prenom_longueur}") 
	protected String prenom;

	@ApiModelProperty(notes = "Adresse e-mail du driver", allowEmptyValue = false, required = true)
	//@NotNull(message = "{err.user.create.email}")
	//@Size(min = 1, max = 100, message = "{err.user.create.email_longueur}")
	//@EmailAlreadyExistConstraint(message = "{err.user.create.email.existe_deja}")
	protected String email;

	@ApiModelProperty(notes = "Numéro de téléphone du driver", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.numero_telephone_1}")
	@Size(min = 1, max = 100, message = "{err.user.create.numero_telephone_1_longueur}")
	protected String numero_telephone_1;

	@ApiModelProperty(notes = "Autre numéro de téléphone du driver", allowEmptyValue = true, required = false)
	protected String numero_telephone_2;

	@ApiModelProperty(notes = "Langue parlée par le driver", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.locale}")
	protected String locale;

	@ApiModelProperty(notes = "Est ce que le driver est activé ?", allowEmptyValue = false, required = true, dataType = "Booléen")
	@NotNull(message = "{err.user.create.activate}")
    protected boolean activate;

	@ApiModelProperty(notes = "Code pays du driver", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.code_pays}")
	protected String code_pays;
	
	@ApiModelProperty(notes = "Type de permis (pris dans l'énumération DriverPermis", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.permis_type}")
	@Size(min = 1, max = 100, message = "{err.user.create.permis_type_longueur}") 
	protected String permisType;
	
	@ApiModelProperty(notes = "Numéro du permis", allowEmptyValue = false, required = true)
	@Size(min = 0, max = 100, message = "{err.user.create.permis_numero_longueur}") 
	protected String numeroPermis;

	@ApiModelProperty(notes = "Photo du driver", allowEmptyValue = true, required = false)
	protected String photoDriver;

	@ApiModelProperty(notes = "Photo du permis", allowEmptyValue = true, required = false)
	protected String photoPermis;

	@ApiModelProperty(notes = "Mot de passe pour se connecter à la webapp expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.mot_de_passe}")
	@Size(min = 1, max = 100, message = "{err.user.create.mot_de_passe_longueur}")
	protected String mot_de_passe;


	public String getMot_de_passe() {
		return mot_de_passe;
	}

	public void setMot_de_passe(String mot_de_passe) {
		this.mot_de_passe = mot_de_passe;
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

	public String getPhotoDriver() {
		return photoDriver;
	}

	public void setPhotoDriver(String photoDriver) {
		this.photoDriver = photoDriver;
	}

	public String getPhotoPermis() {
		return photoPermis;
	}

	public void setPhotoPermis(String photoPermis) {
		this.photoPermis = photoPermis;
	}

	public CreateDriverParams() {
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

	



}
