package com.kamtar.transport.api.params;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.kamtar.transport.api.validation.NumeroDeTelephoneLibreValidConstraint;
import com.kamtar.transport.api.validation.NumeroDeTelephoneValidConstraint;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kamtar.transport.api.model.UtilisateurOperateurKamtar;
import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;

import io.swagger.annotations.ApiModelProperty;


public class EditClientParams extends ParentParams {
	
	@ApiModelProperty(notes = "Identifiant de l'expéditeur", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.client.id}")
	@UUIDObligatoireConstraint(message = "{err.client.id_invalid}")
	private String id;
	
	@ApiModelProperty(notes = "Nom de l'expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.client.create.nom}")
	@Size(min = 1, max = 250, message = "{err.client.create.nom_longueur}") 
	protected String nom;
	
	@ApiModelProperty(notes = "Adresse e-mail du responsable pour se connecter à la webapp expéditeur", allowEmptyValue = true, required = false)
	protected String contact_email;

	@ApiModelProperty(notes = "Code pays du téléphone du resonsable de l'expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.telephone_code_pays}")
	@Size(min = 1, max = 100, message = "{err.user.create.telephone_code_pays}")
	protected String pays_telephone_1;

	@ApiModelProperty(notes = "Numéro de téléphone principale de l'expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.client.create.telephone}")
	@Size(min = 1, max = 200, message = "{err.client.create.telephone_longueur}")
	@NumeroDeTelephoneLibreValidConstraint(message = "{err.user.create.numero_telephone.invalide}")
	protected String contact_numero_telephone1;

	@ApiModelProperty(notes = "Code pays du téléphone du resonsable de l'expéditeur", allowEmptyValue = true, required = false)
	protected String pays_telephone_2;

	@ApiModelProperty(notes = "Numéro de téléphone secondaire de l'expéditeur", allowEmptyValue = true, required = false)
	@NumeroDeTelephoneLibreValidConstraint(message = "{err.user.create.numero_telephone.invalide}")
	protected String contact_numero_telephone2;

	@ApiModelProperty(notes = "Type de compte", allowEmptyValue = false, required = true, dataType = "Chaine de caractères : (B=professionel, C=particulier)")
	@NotNull(message = "{err.client.create.type_de_compte}")
	@Size(min = 1, max = 1, message = "{err.client.create.type_de_compte_longueur}") 
	protected String type_compte;

	@ApiModelProperty(notes = "Mot de passe pour se connecter à la webapp expéditeur", allowEmptyValue = false, required = true)
	protected String mot_de_passe;
	
	@ApiModelProperty(notes = "Adresse de facturation (ligne 1) pour les frais d'expédition facturés à l'expéditeur", allowEmptyValue = true, required = false)
	private String adresse_facturation_ligne_1;

	@ApiModelProperty(notes = "Adresse de facturation (ligne 2) pour les frais d'expédition facturés à l'expéditeur", allowEmptyValue = true, required = false)
	private String adresse_facturation_ligne_2;

	@ApiModelProperty(notes = "Adresse de facturation (ligne 3) pour les frais d'expédition facturés à l'expéditeur", allowEmptyValue = true, required = false)
	private String adresse_facturation_ligne_3;

	@ApiModelProperty(notes = "Adresse de facturation (ligne 4) pour les frais d'expédition facturés à l'expéditeur", allowEmptyValue = true, required = false)
	private String adresse_facturation_ligne_4;

	@ApiModelProperty(notes = "Langue du responsable de l'expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.locale}")
	protected String locale;

	@ApiModelProperty(notes = "Code pays du responsable de l'expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.code_pays}")
	@Size(min = 1, max = 100, message = "{err.user.create.code_pays_longueur}") 
	protected String code_pays;
	
	@ApiModelProperty(notes = "Nom du responsable de l'expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.nom}")
	@Size(min = 1, max = 100, message = "{err.user.create.nom_longueur}") 
	protected String nom_responsable;
	
	@ApiModelProperty(notes = "¨Prénom du responsable de l'expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.prenom}")
	@Size(min = 1, max = 100, message = "{err.user.create.prenom_longueur}") 
	protected String prenom_responsable;
	
	@ApiModelProperty(notes = "Adresse e-mail du responsable de l'expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.email}")
	@Size(min = 1, max = 100, message = "{err.user.create.email_longueur}") 
	protected String email_responsable;

	@ApiModelProperty(notes = "Numéro de téléphone principal du responsable de l'expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.numero_telephone_1}")
	@Size(min = 1, max = 100, message = "{err.user.create.numero_telephone_1_longueur}")
	@NumeroDeTelephoneLibreValidConstraint(message = "{err.user.create.numero_telephone.invalide}")
	protected String numero_telephone1_responsable;

	@ApiModelProperty(notes = "Pays du numéro de téléphone principal du responsable de l'expéditeur", allowEmptyValue = true, required = false)
	@NotNull(message = "{err.user.create.telephone_code_pays}")
	@Size(min = 1, max = 100, message = "{err.user.create.telephone_code_pays}")
	protected String numero_telephone1_responsable_pays;

	@ApiModelProperty(notes = "Numéro de téléphone secondaire du responsable de l'expéditeur", allowEmptyValue = false, required = true)
	@NumeroDeTelephoneLibreValidConstraint(message = "{err.user.create.numero_telephone.invalide}")
	protected String numero_telephone2_responsable;

	@ApiModelProperty(notes = "Pays du numéro de téléphone principal du responsable de l'expéditeur", allowEmptyValue = true, required = false)
	protected String numero_telephone2_responsable_pays;

	@ApiModelProperty(notes = "Compte contribuable du client", allowEmptyValue = true, required = false)
	protected String compte_contribuable;
	
	@ApiModelProperty(notes = "Délais de paiement du client", allowEmptyValue = true, required = false)
	protected Integer delais_paiement;
	
	@ApiModelProperty(notes = "Mode de paiement du client", allowEmptyValue = true, required = false)
	protected String mode_paiement;

	@ApiModelProperty(notes = "Numéro RCCM de l'entreprise", allowEmptyValue = true, required = false)
	protected String numero_rccm;

	@ApiModelProperty(notes = "Est ce que le client est activé ?", allowEmptyValue = false, required = true, dataType = "Booléen")
	@NotNull(message = "{err.user.create.activate}")
	protected boolean activate;

	public boolean isActivate() {
		return activate;
	}

	public void setActivate(boolean activate) {
		this.activate = activate;
	}

	public String getNumero_rccm() {
		return numero_rccm;
	}

	public void setNumero_rccm(String numero_rccm) {
		this.numero_rccm = numero_rccm;
	}

	public String getPays_telephone_1() {
		return pays_telephone_1;
	}

	public void setPays_telephone_1(String pays_telephone_1) {
		this.pays_telephone_1 = pays_telephone_1;
	}

	public String getPays_telephone_2() {
		return pays_telephone_2;
	}

	public void setPays_telephone_2(String pays_telephone_2) {
		this.pays_telephone_2 = pays_telephone_2;
	}

	public String getNumero_telephone1_responsable_pays() {
		return numero_telephone1_responsable_pays;
	}

	public void setNumero_telephone1_responsable_pays(String numero_telephone1_responsable_pays) {
		this.numero_telephone1_responsable_pays = numero_telephone1_responsable_pays;
	}

	public String getNumero_telephone2_responsable_pays() {
		return numero_telephone2_responsable_pays;
	}

	public void setNumero_telephone2_responsable_pays(String numero_telephone2_responsable_pays) {
		this.numero_telephone2_responsable_pays = numero_telephone2_responsable_pays;
	}

	public String getCompte_contribuable() {
		return compte_contribuable;
	}

	public void setCompte_contribuable(String compte_contribuable) {
		this.compte_contribuable = compte_contribuable;
	}

	public Integer getDelais_paiement() {
		return delais_paiement;
	}

	public void setDelais_paiement(Integer delais_paiement) {
		this.delais_paiement = delais_paiement;
	}

	public String getMode_paiement() {
		return mode_paiement;
	}

	public void setMode_paiement(String mode_paiement) {
		this.mode_paiement = mode_paiement;
	}

	public String getNom_responsable() {
		return nom_responsable;
	}

	public void setNom_responsable(String nom_responsable) {
		this.nom_responsable = nom_responsable;
	}

	public String getPrenom_responsable() {
		return prenom_responsable;
	}

	public void setPrenom_responsable(String prenom_responsable) {
		this.prenom_responsable = prenom_responsable;
	}

	public String getEmail_responsable() {
		return email_responsable;
	}

	public void setEmail_responsable(String email_responsable) {
		this.email_responsable = email_responsable;
	}

	public String getNumero_telephone1_responsable() {
		return numero_telephone1_responsable;
	}

	public void setNumero_telephone1_responsable(String numero_telephone1_responsable) {
		this.numero_telephone1_responsable = numero_telephone1_responsable;
	}

	public String getNumero_telephone2_responsable() {
		return numero_telephone2_responsable;
	}

	public void setNumero_telephone2_responsable(String numero_telephone2_responsable) {
		this.numero_telephone2_responsable = numero_telephone2_responsable;
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

	public EditClientParams() {
		super();
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	/*public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}*/

	public String getContact_email() {
		return contact_email;
	}

	public void setContact_email(String contact_email) {
		this.contact_email = contact_email;
	}

	public String getContact_numero_telephone1() {
		return contact_numero_telephone1;
	}

	public void setContact_numero_telephone1(String contact_numero_telephone1) {
		this.contact_numero_telephone1 = contact_numero_telephone1;
	}

	public String getContact_numero_telephone2() {
		return contact_numero_telephone2;
	}

	public void setContact_numero_telephone2(String contact_numero_telephone2) {
		this.contact_numero_telephone2 = contact_numero_telephone2;
	}

	public String getMot_de_passe() {
		return mot_de_passe;
	}

	public void setMot_de_passe(String mot_de_passe) {
		this.mot_de_passe = mot_de_passe;
	}

	public String getType_compte() {
		return type_compte;
	}

	public void setType_compte(String type_compte) {
		this.type_compte = type_compte;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}




}
