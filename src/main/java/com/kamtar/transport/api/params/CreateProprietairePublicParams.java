package com.kamtar.transport.api.params;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.kamtar.transport.api.validation.*;

import io.swagger.annotations.ApiModelProperty;


@NumeroDeTelephoneValidConstraint(message = "{err.user.create.numero_telephone.invalide}")
public class CreateProprietairePublicParams extends ParentParams {

	@ApiModelProperty(notes = "Nom du propriétaire", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.nom}")
	@Size(min = 1, max = 100, message = "{err.user.create.nom_longueur}") 
	protected String proprietaire_nom;

	@ApiModelProperty(notes = "Prénom du propriétaire", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.prenom}")
	@Size(min = 1, max = 100, message = "{err.user.create.prenom_longueur}") 
	protected String proprietaire_prenom;

	@ApiModelProperty(notes = "Adresse e-mail du propriétaire", allowEmptyValue = false, required = true)
	//@NotNull(message = "{err.user.create.email}")
	//@Size(min = 1, max = 100, message = "{err.user.create.email_longueur}")
	//@EmailAlreadyExistConstraint(message = "{err.user.create.email.existe_deja}")
	//@EmailValidConstraint(message = "{err.user.create.email.invalide}")
	protected String proprietaire_email;

	@ApiModelProperty(notes = "Mot de passe pour se connecter à la webapp expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.mot_de_passe}")
	@Size(min = 1, max = 100, message = "{err.user.create.mot_de_passe_longueur}")
	protected String proprietaire_password;

	@ApiModelProperty(notes = "Numéro de téléphone du propriétaire", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.numero_telephone_1}")
	@Size(min = 1, max = 100, message = "{err.user.create.numero_telephone_1_longueur}")
	protected String proprietaire_numero_telephone_1;

	@ApiModelProperty(notes = "Autre numéro de téléphone du propriétaire", allowEmptyValue = true, required = false)
	protected String proprietaire_numero_telephone_2;

	@ApiModelProperty(notes = "Langue parlée par le propriétaire", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.locale}")
	protected String proprietaire_locale;

	@ApiModelProperty(notes = "Numéro de la carte de transport")
	//@NotNull(message = "{err.user.create.carte_transport.numero}")
	protected String proprietaire_numeroCarteTransport;
	
	@ApiModelProperty(notes = "Date d'établissement de la carte de transport")
	//@NotNull(message = "{err.user.create.carte_transport.date}")
	protected Date proprietaire_dateEtablissementCarteTransport;

	@ApiModelProperty(notes = "Code parrainage", allowEmptyValue = true, required = false)
	protected String proprietaire_codeParrainage;

	@ApiModelProperty(notes = "Est ce que le propriétaire est assujeti à l'airsi ?", allowEmptyValue = false, required = true, dataType = "Booléen")
	@NotNull(message = "{err.user.create.assujet_airsi}")
	protected boolean assujetiAIRSI;

	@ApiModelProperty(notes = "Type de compte", allowEmptyValue = false, required = true, dataType = "Chaine de caractères : (B=professionel, C=particulier)")
	@NotNull(message = "{err.client.create.type_de_compte}")
	@Size(min = 1, max = 1, message = "{err.client.create.type_de_compte_longueur}")
	protected String type_compte;

	@ApiModelProperty(notes = "Nom de l'entreprise", allowEmptyValue = true, required = false)
	protected String entreprise_nom;

	@ApiModelProperty(notes = "Compte comptable de l'entreprise", allowEmptyValue = true, required = false)
	protected String entreprise_compte_comptable;

	@ApiModelProperty(notes = "Numéro RCCM de l'entreprise", allowEmptyValue = true, required = false)
	protected String entreprise_numero_rccm;

	@ApiModelProperty(notes = "Pays où opère kamtar", allowEmptyValue = false, required = true)
	protected String pays;

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

	public String getEntreprise_numero_rccm() {
		return entreprise_numero_rccm;
	}

	public void setEntreprise_numero_rccm(String entreprise_numero_rccm) {
		this.entreprise_numero_rccm = entreprise_numero_rccm;
	}

	public String getPays() {
		return pays;
	}

	public void setPays(String pays) {
		this.pays = pays;
	}

	public boolean isAssujetiAIRSI() {
		return assujetiAIRSI;
	}

	public void setAssujetiAIRSI(boolean assujetiAIRSI) {
		this.assujetiAIRSI = assujetiAIRSI;
	}

	public String getType_compte() {
		return type_compte;
	}

	public void setType_compte(String type_compte) {
		this.type_compte = type_compte;
	}

	public String getProprietaire_codeParrainage() {
		return proprietaire_codeParrainage;
	}

	public void setProprietaire_codeParrainage(String proprietaire_codeParrainage) {
		this.proprietaire_codeParrainage = proprietaire_codeParrainage;
	}

	public String getProprietaire_password() {
		return proprietaire_password;
	}

	public void setProprietaire_password(String proprietaire_password) {
		this.proprietaire_password = proprietaire_password;
	}

	public CreateProprietairePublicParams() {
		super();
	}


	public String getProprietaire_nom() {
		return proprietaire_nom;
	}


	public void setProprietaire_nom(String proprietaire_nom) {
		this.proprietaire_nom = proprietaire_nom;
	}


	public String getProprietaire_prenom() {
		return proprietaire_prenom;
	}


	public void setProprietaire_prenom(String proprietaire_prenom) {
		this.proprietaire_prenom = proprietaire_prenom;
	}


	public String getProprietaire_email() {
		return proprietaire_email;
	}


	public void setProprietaire_email(String proprietaire_email) {
		this.proprietaire_email = proprietaire_email;
	}


	public String getProprietaire_numero_telephone_1() {
		return proprietaire_numero_telephone_1;
	}


	public void setProprietaire_numero_telephone_1(String proprietaire_numero_telephone_1) {
		this.proprietaire_numero_telephone_1 = proprietaire_numero_telephone_1;
	}


	public String getProprietaire_numero_telephone_2() {
		return proprietaire_numero_telephone_2;
	}


	public void setProprietaire_numero_telephone_2(String proprietaire_numero_telephone_2) {
		this.proprietaire_numero_telephone_2 = proprietaire_numero_telephone_2;
	}


	public String getProprietaire_locale() {
		return proprietaire_locale;
	}


	public void setProprietaire_locale(String proprietaire_locale) {
		this.proprietaire_locale = proprietaire_locale;
	}


	public String getProprietaire_numeroCarteTransport() {
		return proprietaire_numeroCarteTransport;
	}


	public void setProprietaire_numeroCarteTransport(String proprietaire_numeroCarteTransport) {
		this.proprietaire_numeroCarteTransport = proprietaire_numeroCarteTransport;
	}


	public Date getProprietaire_dateEtablissementCarteTransport() {
		return proprietaire_dateEtablissementCarteTransport;
	}


	public void setProprietaire_dateEtablissementCarteTransport(Date proprietaire_dateEtablissementCarteTransport) {
		this.proprietaire_dateEtablissementCarteTransport = proprietaire_dateEtablissementCarteTransport;
	}





}