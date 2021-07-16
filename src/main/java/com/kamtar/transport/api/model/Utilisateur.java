package com.kamtar.transport.api.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.*;

import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.annotations.ApiModelProperty;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = "utilisateur", indexes = { @Index(name = "idx_utilisateur_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
@JsonTypeInfo(
		  use = JsonTypeInfo.Id.NAME, 
		  include = JsonTypeInfo.As.PROPERTY, 
		  property = "type")
public abstract class Utilisateur implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(notes = "Identifiant")	
	@Id
	@Type(type="uuid-char")
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected UUID uuid;

	@ApiModelProperty(notes = "Nom de l'utilisateur")
	@Column(name = "nom", nullable = false, updatable = true)
	protected String nom;

	@ApiModelProperty(notes = "Prénom de l'utilisateur")
	@Column(name = "prenom", nullable = false, updatable = true)
	protected String prenom;

	@ApiModelProperty(notes = "Adresse e-mail de l'utilisateur")
	@Column(name = "email", nullable = false, updatable = true)
	protected String email;

	@JsonIgnore
	@Column(name = "motDePasse", nullable = false, updatable = true)
	protected String motDePasse;

	@ApiModelProperty(notes = "Numéro de téléphone principal de l'utilisateur")
	@Column(name = "numeroTelephone1", nullable = false, updatable = true)
	protected String numeroTelephone1;

	@ApiModelProperty(notes = "Code du pays du numéro de téléphone principal de l'utilisateur")
	@Column(name = "numeroTelephone1Pays", nullable = true, updatable = true, columnDefinition = "varchar(10) default 'CI'")
	protected String numeroTelephone1Pays;

	@ApiModelProperty(notes = "Numéro de téléphone seconde de l'utilisateur")
	@Column(name = "numeroTelephone2", nullable = true, updatable = true)
	protected String numeroTelephone2;

	@ApiModelProperty(notes = "Code du pays du numéro de téléphone principal de l'utilisateur")
	@Column(name = "numeroTelephone2Pays", nullable = true, updatable = true, columnDefinition = "varchar(10) default 'CI'")
	protected String numeroTelephone2Pays;

	@ApiModelProperty(notes = "Langue de l'utilisateur")
	@Column(name = "locale", nullable = false, updatable = true)
	protected String locale;

	@ApiModelProperty(notes = "UUID du fichier image de l'utilisateur")
	@Column(name = "photo", nullable = true, updatable = true)
	protected String photo;

	@ApiModelProperty(notes = "Date de création")
	@Column(name = "createdOn", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@CreatedDate
	protected Date createdOn = new Date();

	@ApiModelProperty(notes = "Date de dernière modification")
	@Column(name = "updatedOn", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@LastModifiedDate
	@JsonIgnore
	protected Date updatedOn = new Date();

	@ApiModelProperty(notes = "Est ce que l'utilisateur est activé ?")
    @Column(name = "activate", nullable = false, updatable = true)
    protected boolean activate;

	@ApiModelProperty(notes = "Pays")
	@Column(updatable = true, name = "codePays", nullable = true, unique = false)	
	protected String codePays;

	@ApiModelProperty(notes = "Type de compte")
	@Column(name = "typeDeCompte", nullable = false, updatable = false)
	@JsonIgnore
	protected String typeDeCompte;

	@ApiModelProperty(notes = "Est ce que l'utilisateur a été créé via un devis ?")
	@Column(name = "creeParDevis", nullable = false, updatable = true, columnDefinition="tinyint(1) default 0")
	protected boolean creeParDevis;


	public boolean isCreeParDevis() {
		return creeParDevis;
	}

	public void setCreeParDevis(boolean creeParDevis) {
		this.creeParDevis = creeParDevis;
	}

	public String getNumeroTelephone1Pays() {
		return numeroTelephone1Pays;
	}

	public void setNumeroTelephone1Pays(String numeroTelephone1Pays) {
		this.numeroTelephone1Pays = numeroTelephone1Pays;
	}

	public String getNumeroTelephone2Pays() {
		return numeroTelephone2Pays;
	}

	public void setNumeroTelephone2Pays(String numeroTelephone2Pays) {
		this.numeroTelephone2Pays = numeroTelephone2Pays;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
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

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	public boolean isActivate() {
		return activate;
	}

	public void setActivate(boolean activate) {
		this.activate = activate;
	}

	public Utilisateur() {
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getCodePays() {
		return codePays;
	}

	public void setCodePays(String codePays) {
		this.codePays = codePays;
	}

	public String getMotDePasse() {
		return motDePasse;
	}

	public void setMotDePasse(String motDePasse) {
		this.motDePasse = motDePasse;
	}

	public String getNumeroTelephone1() {
		return numeroTelephone1;
	}

	public void setNumeroTelephone1(String numeroTelephone1) {
		this.numeroTelephone1 = numeroTelephone1;
	}

	public String getNumeroTelephone2() {
		return numeroTelephone2;
	}

	public void setNumeroTelephone2(String numeroTelephone2) {
		this.numeroTelephone2 = numeroTelephone2;
	}

	public String getTypeDeCompte() {
		return typeDeCompte;
	}

	public void setTypeDeCompte(String typeDeCompte) {
		this.typeDeCompte = typeDeCompte;
	}

    @JsonProperty("prenom_nom")
    public String getPrenomNom() {
    	return this.prenom + " " + this.nom;
    }

	@JsonProperty("nom_prenom")
	public String getNomPrenom() {
		return this.nom + " " + this.prenom;
	}

	public Utilisateur(String codePays) {
		super();
		this.codePays = codePays;
	}


}
