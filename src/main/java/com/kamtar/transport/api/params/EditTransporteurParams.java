package com.kamtar.transport.api.params;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.kamtar.transport.api.validation.NumeroDeTelephoneValidConstraint;
import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;

import io.swagger.annotations.ApiModelProperty;


@NumeroDeTelephoneValidConstraint(message = "{err.user.create.numero_telephone.invalide}")
public class EditTransporteurParams extends ParentParams {
	
	@ApiModelProperty(notes = "Identifiant du transporteur", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.user.id}")
	@UUIDObligatoireConstraint(message = "{err.transporteur.id_invalid}")
	private String id;
	
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

	/**
	 * Pas de contrainte car le mot de passse peut être vide (= pas de changement)
	 */
	@ApiModelProperty(notes = "Mot de passe du transporteur (vide pour ne pas le modifier)", allowEmptyValue = true, required = false)
	protected String mot_de_passe;

	@ApiModelProperty(notes = "Numéro de téléphone principal du transporteur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.numero_telephone_1}")
	@Size(min = 1, max = 100, message = "{err.user.create.numero_telephone_1_longueur}")
	protected String numero_telephone_1;

	@ApiModelProperty(notes = "Numéro de téléphone secondaire du transporteur", allowEmptyValue = false, required = true)
	protected String numero_telephone_2;

	@ApiModelProperty(notes = "Langue du transporteur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.locale}")
	protected String locale;

	@ApiModelProperty(notes = "Est ce que le transporteur est activé ?", allowEmptyValue = false, required = true, dataType = "Booléen")
	@NotNull(message = "{err.user.create.activate}")
    protected boolean activate;

	@ApiModelProperty(notes = "Code du pays du transporteur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.code_pays}")
	@Size(min = 1, max = 100, message = "{err.user.create.code_pays_longueur}") 
	protected String code_pays;
	
	@ApiModelProperty(notes = "Immatriculation du véhicule utilisé par le transporteur", allowEmptyValue = true, required = false)
	@Size(min = 0, max = 100, message = "{err.vehicule.create.immatriculation_longueur}")  
	private String vehiculeImmatriculation;
	
	@ApiModelProperty(notes = "Type de véhicule utilisé par le transporteur", allowEmptyValue = true, required = false)
	@Size(min = 0, max = 100, message = "{err.vehicule.create.type_longueur}")  
	private String vehiculeType;
	
	@ApiModelProperty(notes = "Marque du véhicule utilisé par le transporteur", allowEmptyValue = true, required = false)
	@Size(min = 0, max = 200, message = "{err.vehicule.create.marque_longueur}")  
	private String vehiculeMarque;
	
	
	
	public String getVehiculeImmatriculation() {
		return vehiculeImmatriculation;
	}

	public void setVehiculeImmatriculation(String vehiculeImmatriculation) {
		this.vehiculeImmatriculation = vehiculeImmatriculation;
	}

	public String getVehiculeType() {
		return vehiculeType;
	}

	public void setVehiculeType(String vehiculeType) {
		this.vehiculeType = vehiculeType;
	}

	public String getVehiculeMarque() {
		return vehiculeMarque;
	}

	public void setVehiculeMarque(String vehiculeMarque) {
		this.vehiculeMarque = vehiculeMarque;
	}

	public EditTransporteurParams() {
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
