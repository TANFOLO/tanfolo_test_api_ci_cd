package com.kamtar.transport.api.params;

import com.kamtar.transport.api.validation.NumeroDeTelephoneValidConstraint;
import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@NumeroDeTelephoneValidConstraint(message = "{err.user.create.numero_telephone.invalide}")
public class EditTransporteurPublicParams extends ParentParams {


	@ApiModelProperty(notes = "Nom du transporteur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.nom}")
	@Size(min = 1, max = 100, message = "{err.user.create.nom_longueur}")
	protected String nom;

	@ApiModelProperty(notes = "Prénom du transporteur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.prenom}")
	@Size(min = 1, max = 100, message = "{err.user.create.prenom_longueur}")
	protected String prenom;

	@ApiModelProperty(notes = "Adresse e-mail du transporteur pour se connecter à la webapp", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.email}")
	@Size(min = 1, max = 100, message = "{err.user.create.email_longueur}")
	protected String email;

	@ApiModelProperty(notes = "Numéro de téléphone principal du transporteur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.numero_telephone_1}")
	@Size(min = 1, max = 100, message = "{err.user.create.numero_telephone_1_longueur}")
	protected String numero_telephone_1;

	@ApiModelProperty(notes = "Numéro de téléphone secondaire du transporteur", allowEmptyValue = false, required = true)
	protected String numero_telephone_2;

	@ApiModelProperty(notes = "Mot de passe pour se connecter à la webapp expéditeur", allowEmptyValue = false, required = true)
	protected String mot_de_passe;

	@ApiModelProperty(notes = "Pays où opère Kamtar", allowEmptyValue = true, required = false)
	protected String codePays;


	public String getCodePays() {
		return codePays;
	}

	public void setCodePays(String codePays) {
		this.codePays = codePays;
	}

	public String getMot_de_passe() {
		return mot_de_passe;
	}

	public void setMot_de_passe(String mot_de_passe) {
		this.mot_de_passe = mot_de_passe;
	}

	public EditTransporteurPublicParams() {
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




}
