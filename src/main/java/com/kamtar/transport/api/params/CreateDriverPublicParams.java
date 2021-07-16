package com.kamtar.transport.api.params;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.kamtar.transport.api.validation.CodeParrainageDriverValidConstraint;

import com.kamtar.transport.api.validation.NumeroDeTelephoneValidConstraint;
import io.swagger.annotations.ApiModelProperty;


@CodeParrainageDriverValidConstraint(message = "{err.user.create.code_parrainage.invalide}")
@NumeroDeTelephoneValidConstraint(message = "{err.user.create.numero_telephone.invalide}")
public class CreateDriverPublicParams extends ParentParams {

	@ApiModelProperty(notes = "Nom du driver", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.nom}")
	@Size(min = 1, max = 100, message = "{err.user.create.nom_longueur}") 
	protected String chauffeur_nom;

	@ApiModelProperty(notes = "Prénom du driver", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.prenom}")
	@Size(min = 1, max = 100, message = "{err.user.create.prenom_longueur}") 
	protected String chauffeur_prenom;

	@ApiModelProperty(notes = "Adresse e-mail du driver", allowEmptyValue = false, required = true)
	//@NotNull(message = "{err.user.create.email}")
	//@Size(min = 1, max = 100, message = "{err.user.create.email_longueur}")
	//@EmailAlreadyExistConstraint(message = "{err.user.create.email.existe_deja}")
	protected String chauffeur_email;

	@ApiModelProperty(notes = "Mot de passe pour se connecter à la webapp expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.mot_de_passe}")
	@Size(min = 1, max = 100, message = "{err.user.create.mot_de_passe_longueur}")
	protected String chauffeur_password;

	@ApiModelProperty(notes = "Numéro de téléphone du driver", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.numero_telephone_1}")
	@Size(min = 1, max = 100, message = "{err.user.create.numero_telephone_1_longueur}")
	protected String chauffeur_numero_telephone_1;

	@ApiModelProperty(notes = "Autre numéro de téléphone du driver", allowEmptyValue = true, required = false)
	protected String chauffeur_numero_telephone_2;

	@ApiModelProperty(notes = "Langue parlée par le driver", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.locale}")
	protected String chauffeur_locale;

	@ApiModelProperty(notes = "Type de permis (pris dans l'énumération DriverPermis", allowEmptyValue = false, required = true)
	//@NotNull(message = "{err.user.create.permis_type}")
	//@Size(min = 1, max = 100, message = "{err.user.create.permis_type_longueur}")
	protected String chauffeur_permisType;
	
	@ApiModelProperty(notes = "Numéro du permis", allowEmptyValue = false, required = true)
	//@Size(min = 0, max = 100, message = "{err.user.create.permis_numero_longueur}")
	protected String chauffeur_numeroPermis;

	@ApiModelProperty(notes = "Lieu d'habitation du driver", allowEmptyValue = true, required = false)
	protected String chauffeur_lieuHabitation;

	@ApiModelProperty(notes = "Disponibilité en km", allowEmptyValue = true, required = false)
	protected Integer chauffeur_disponibiliteKm;

	@ApiModelProperty(notes = "Code parrainage", allowEmptyValue = true, required = false)
	protected String chauffeur_codeParrainage;

	@ApiModelProperty(notes = "Pays où kamtar opère", allowEmptyValue = true, required = false)
	protected String pays;

	public String getPays() {
		return pays;
	}

	public void setPays(String pays) {
		this.pays = pays;
	}

	public String getChauffeur_password() {
		return chauffeur_password;
	}

	public void setChauffeur_password(String chauffeur_password) {
		this.chauffeur_password = chauffeur_password;
	}

	public CreateDriverPublicParams() {
		super();
	}



	public String getChauffeur_nom() {
		return chauffeur_nom;
	}



	public void setChauffeur_nom(String chauffeur_nom) {
		this.chauffeur_nom = chauffeur_nom;
	}



	public String getChauffeur_prenom() {
		return chauffeur_prenom;
	}



	public void setChauffeur_prenom(String chauffeur_prenom) {
		this.chauffeur_prenom = chauffeur_prenom;
	}



	public String getChauffeur_email() {
		return chauffeur_email;
	}



	public void setChauffeur_email(String chauffeur_email) {
		this.chauffeur_email = chauffeur_email;
	}



	public String getChauffeur_numero_telephone_1() {
		return chauffeur_numero_telephone_1;
	}



	public void setChauffeur_numero_telephone_1(String chauffeur_numero_telephone_1) {
		this.chauffeur_numero_telephone_1 = chauffeur_numero_telephone_1;
	}



	public String getChauffeur_numero_telephone_2() {
		return chauffeur_numero_telephone_2;
	}



	public void setChauffeur_numero_telephone_2(String chauffeur_numero_telephone_2) {
		this.chauffeur_numero_telephone_2 = chauffeur_numero_telephone_2;
	}



	public String getChauffeur_locale() {
		return chauffeur_locale;
	}



	public void setChauffeur_locale(String chauffeur_locale) {
		this.chauffeur_locale = chauffeur_locale;
	}



	public String getChauffeur_permisType() {
		return chauffeur_permisType;
	}



	public void setChauffeur_permisType(String chauffeur_permisType) {
		this.chauffeur_permisType = chauffeur_permisType;
	}



	public String getChauffeur_numeroPermis() {
		return chauffeur_numeroPermis;
	}



	public void setChauffeur_numeroPermis(String chauffeur_numeroPermis) {
		this.chauffeur_numeroPermis = chauffeur_numeroPermis;
	}



	public String getChauffeur_lieuHabitation() {
		return chauffeur_lieuHabitation;
	}



	public void setChauffeur_lieuHabitation(String chauffeur_lieuHabitation) {
		this.chauffeur_lieuHabitation = chauffeur_lieuHabitation;
	}



	public Integer getChauffeur_disponibiliteKm() {
		return chauffeur_disponibiliteKm;
	}



	public void setChauffeur_disponibiliteKm(Integer chauffeur_disponibiliteKm) {
		this.chauffeur_disponibiliteKm = chauffeur_disponibiliteKm;
	}



	public String getChauffeur_codeParrainage() {
		return chauffeur_codeParrainage;
	}



	public void setChauffeur_codeParrainage(String chauffeur_codeParrainage) {
		this.chauffeur_codeParrainage = chauffeur_codeParrainage;
	}

	


}
