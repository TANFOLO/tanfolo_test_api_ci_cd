package com.kamtar.transport.api.params;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.kamtar.transport.api.validation.NumeroDeTelephoneValidConstraint;
import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;

import io.swagger.annotations.ApiModelProperty;


@NumeroDeTelephoneValidConstraint(message = "{err.user.create.numero_telephone.invalide}")
public class EditProprietaireParams extends ParentParams {
	
	@ApiModelProperty(notes = "Identifiant du propriétaire", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.user.id}")
	@UUIDObligatoireConstraint(message = "{err.utilisateur.id_invalid}")
	private String id;
	
	@ApiModelProperty(notes = "Nom du propriétaire", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.nom}")
	@Size(min = 1, max = 100, message = "{err.user.create.nom_longueur}") 
	protected String nom;

	@ApiModelProperty(notes = "Prénom du propriétaire", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.prenom}")
	@Size(min = 1, max = 100, message = "{err.user.create.prenom_longueur}") 
	protected String prenom;

	@ApiModelProperty(notes = "Adresse e-mail du propriétaire", allowEmptyValue = false, required = true)
	//@NotNull(message = "{err.user.create.email}")
	//@Size(min = 1, max = 100, message = "{err.user.create.email_longueur}")
	protected String email;

	/**
	 * Pas de contrainte car le mot de passse peut être vide (= pas de changement)
	 */
	@ApiModelProperty(notes = "Mot de passe du propriétaire (à renseigner uniquement si il est à changer)", allowEmptyValue = true, required = false)
	protected String mot_de_passe;

	@ApiModelProperty(notes = "Numéro de téléphone du propriétaire", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.numero_telephone_1}")
	@Size(min = 1, max = 100, message = "{err.user.create.numero_telephone_1_longueur}")
	protected String numero_telephone_1;

	@ApiModelProperty(notes = "Autre numéro de téléphone du propriétaire", allowEmptyValue = true, required = false)
	protected String numero_telephone_2;

	@ApiModelProperty(notes = "Langue parlée par le propriétaire", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.locale}")
	protected String locale;

	@ApiModelProperty(notes = "Est ce que le propriétaire est activé ?", allowEmptyValue = false, required = true, dataType = "Booléen")
	@NotNull(message = "{err.user.create.activate}")
    protected boolean activate;

	@ApiModelProperty(notes = "Code pays du propriétaire", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.code_pays}")
	@Size(min = 1, max = 100, message = "{err.user.create.code_pays_longueur}") 
	protected String code_pays;
	
	@ApiModelProperty(notes = "Numéro de la carte de transport")
	@NotNull(message = "{err.user.create.carte_transport.numero}")
	protected String numeroCarteTransport;
	
	@ApiModelProperty(notes = "Date d'établissement de la carte de transport")
	//@NotNull(message = "{err.user.create.carte_transport.date}")
	protected Date dateEtablissementCarteTransport;
	
	@ApiModelProperty(notes = "Base64 de la photo de la carte de transport")
	protected String photoCarteTransport;

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

	@ApiModelProperty(notes = "Adresse de facturation (ligne 1) pour les frais d'expédition facturés à l'expéditeur", allowEmptyValue = true, required = false)
	private String adresse_facturation_ligne_1;

	@ApiModelProperty(notes = "Adresse de facturation (ligne 2) pour les frais d'expédition facturés à l'expéditeur", allowEmptyValue = true, required = false)
	private String adresse_facturation_ligne_2;

	@ApiModelProperty(notes = "Adresse de facturation (ligne 3) pour les frais d'expédition facturés à l'expéditeur", allowEmptyValue = true, required = false)
	private String adresse_facturation_ligne_3;

	@ApiModelProperty(notes = "Adresse de facturation (ligne 4) pour les frais d'expédition facturés à l'expéditeur", allowEmptyValue = true, required = false)
	private String adresse_facturation_ligne_4;

	@ApiModelProperty(notes = "Nom de l'intermédiaire", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.proprietaire.create.intermediaire.nom.obligatoire}")
	@Size(min = 1, max = 200, message = "{err.proprietaire.create.intermediaire.nom.obligatoire_longueur}")
	private String intermediaireNom;

	@ApiModelProperty(notes = "Prénom de l'intermédiaire", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.proprietaire.create.intermediaire.prenom.obligatoire}")
	@Size(min = 1, max = 200, message = "{err.proprietaire.create.intermediaire.prenom.obligatoire_longueur}")
	private String intermediairePrenom;

	@ApiModelProperty(notes = "Téléphone de l'intermédiaire", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.proprietaire.create.intermediaire.telephone.obligatoire}")
	@Size(min = 1, max = 200, message = "{err.proprietaire.create.intermediaire.telephone.obligatoire_longueur}")
	private String intermediaireTelephone;

	@ApiModelProperty(notes = "Email de l'intermédiaire", allowEmptyValue = true, required = false)
	private String intermediaireEmail;


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

	public String getIntermediaireNom() {
		return intermediaireNom;
	}

	public void setIntermediaireNom(String intermediaireNom) {
		this.intermediaireNom = intermediaireNom;
	}

	public String getIntermediairePrenom() {
		return intermediairePrenom;
	}

	public void setIntermediairePrenom(String intermediairePrenom) {
		this.intermediairePrenom = intermediairePrenom;
	}

	public String getIntermediaireTelephone() {
		return intermediaireTelephone;
	}

	public void setIntermediaireTelephone(String intermediaireTelephone) {
		this.intermediaireTelephone = intermediaireTelephone;
	}

	public String getIntermediaireEmail() {
		return intermediaireEmail;
	}

	public void setIntermediaireEmail(String intermediaireEmail) {
		this.intermediaireEmail = intermediaireEmail;
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

	public boolean isAssujetiAIRSI() {
		return assujetiAIRSI;
	}

	public void setAssujetiAIRSI(boolean assujetiAIRSI) {
		this.assujetiAIRSI = assujetiAIRSI;
	}
	
	public String getPhotoCarteTransport() {
		return photoCarteTransport;
	}

	public void setPhotoCarteTransport(String photoCarteTransport) {
		this.photoCarteTransport = photoCarteTransport;
	}

	public EditProprietaireParams() {
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

	public String getNumeroCarteTransport() {
		return numeroCarteTransport;
	}

	public void setNumeroCarteTransport(String numeroCarteTransport) {
		this.numeroCarteTransport = numeroCarteTransport;
	}

	public Date getDateEtablissementCarteTransport() {
		return dateEtablissementCarteTransport;
	}

	public void setDateEtablissementCarteTransport(Date dateEtablissementCarteTransport) {
		this.dateEtablissementCarteTransport = dateEtablissementCarteTransport;
	}



}
