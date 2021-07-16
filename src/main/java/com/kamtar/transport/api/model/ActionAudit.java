package com.kamtar.transport.api.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.*;

import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Action audit")
@Entity
@Table(name = "action_audit", indexes = { @Index(name = "idx_action_audit_type", columnList = "typeAction"), @Index(name = "idx_action_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class ActionAudit implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(notes = "Identifiant")
	@Id
	@Type(type="uuid-char")
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected UUID uuid;

	@ApiModelProperty(notes = "Date de création")
	@Column(name = "createdOn", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@CreatedDate
	private Date createdOn = new Date();

	@ApiModelProperty(notes = "Type d'action")
	@Column(updatable = false, name = "typeAction", nullable = false, length=100, unique = false)	
	private String typeAction;

	@ApiModelProperty(notes = "Libellé de l'action")
	@Column(updatable = false, name = "libelleAction", nullable = false, length=1000, unique = false)	
	private String libelleAction;

	@ApiModelProperty(notes = "UUID du driver")
	@Column(updatable = false, name = "uuidDriver", nullable = true, unique = false)	
	private String uuidDriver;

	@ApiModelProperty(notes = "UUID de l'opérateur")
	@Column(updatable = false, name = "uuidOperateur", nullable = true, unique = false)	 
	private String uuidOperateur;

	@ApiModelProperty(notes = "UUID du propriétaire")
	@Column(updatable = false, name = "uuidProprietaire", nullable = true, unique = false)	 
	private String uuidProprietaire;

	@ApiModelProperty(notes = "UUID de l'admin")
	@Column(updatable = false, name = "uuidAdmin", nullable = true, unique = false)	
	private String uuidAdmin;

	@ApiModelProperty(notes = "UUID du véhicule")
	@Column(updatable = false, name = "uuidVehicule", nullable = true, unique = false)	
	private String uuidVehicule;

	@ApiModelProperty(notes = "UUID de la requête")
	@Column(updatable = false, name = "uuidRequete", nullable = true, unique = false)	
	private String uuidRequete;

	@ApiModelProperty(notes = "UUID du document")
	@Column(updatable = false, name = "uuidDocument", nullable = true, unique = false)	
	private String uuidDocument;

	@ApiModelProperty(notes = "UUID de la facture")
	@Column(updatable = false, name = "uuidFacture", nullable = true, unique = false)	
	private String uuidFacture;

	@ApiModelProperty(notes = "UUID de la proposition")
	@Column(updatable = false, name = "uuidProposition", nullable = true, unique = false)	
	private String uuidProposition;

	@ApiModelProperty(notes = "UUID de l'expéditeur")
	@Column(updatable = false, name = "uuidClient", nullable = true, unique = false)	
	private String uuidClient;

	@ApiModelProperty(notes = "UUID de l'utilisateur")
	@Column(updatable = false, name = "uuidUtilisateur", nullable = true, unique = false)	
	private String uuidUtilisateur;

	@ApiModelProperty(notes = "UUID du transporteur")
	@Column(updatable = false, name = "uuidTransporteur", nullable = true, unique = false)	 
	private String uuidTransporteur;

	@ApiModelProperty(notes = "UUID de l'opération")
	@Column(updatable = false, name = "uuidOperation", nullable = true, unique = false)	 
	private String uuidOperation;

	@ApiModelProperty(notes = "UUID du contact")
	@Column(updatable = false, name = "uuidContact", nullable = true, unique = false)	 
	private String uuidContact;

	@ApiModelProperty(notes = "UUID du mot de passe perdu")
	@Column(updatable = false, name = "uuidMotDePassePerdu", nullable = true, unique = false)	 
	private String uuidMotDePassePerdu;

	@ApiModelProperty(notes = "Code du pays")
	@Column(updatable = false, name = "codePays", nullable = true, unique = false)	
	private String codePays;

	@ApiModelProperty(notes = "UUID de l'appel d'offre")
	@Column(updatable = false, name = "uuidAppelOffre", nullable = true, unique = false)	 
	private String uuidAppelOffre;

	@ApiModelProperty(notes = "UUID du devis")
	@Column(updatable = false, name = "uuidDevis", nullable = true, unique = false)
	private String uuidDevis;

	@ApiModelProperty(notes = "UUID de la réclamation")
	@Column(updatable = false, name = "uuidReclamation", nullable = true, unique = false)
	private String uuidReclamation;

	@ApiModelProperty(notes = "UUID de l'échang de la réclamation")
	@Column(updatable = false, name = "uuidReclamationEchange", nullable = true, unique = false)
	private String uuidReclamationEchange;

	public String getUuidReclamation() {
		return uuidReclamation;
	}

	public void setUuidReclamation(String uuidReclamation) {
		this.uuidReclamation = uuidReclamation;
	}

	public String getUuidReclamationEchange() {
		return uuidReclamationEchange;
	}

	public void setUuidReclamationEchange(String uuidReclamationEchange) {
		this.uuidReclamationEchange = uuidReclamationEchange;
	}

	public ActionAudit(String code_pays) {
		this.codePays = code_pays;
	}

	public String getUuidDevis() {
		return uuidDevis;
	}

	public void setUuidDevis(String uuidDevis) {
		this.uuidDevis = uuidDevis;
	}

	public String getUuidAppelOffre() {
		return uuidAppelOffre;
	}

	public void setUuidAppelOffre(String uuidAppelOffre) {
		this.uuidAppelOffre = uuidAppelOffre;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) { 
		this.uuid = uuid;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public String getTypeAction() {
		return typeAction;
	}

	public void setTypeAction(String typeAction) {
		this.typeAction = typeAction;
	}

	public String getLibelleAction() {
		return libelleAction;
	}

	public void setLibelleAction(String libelleAction) {
		this.libelleAction = libelleAction;
	}

	public String getUuidDriver() {
		return uuidDriver;
	}

	public void setUuidDriver(String uuidDriver) {
		this.uuidDriver = uuidDriver;
	}

	public String getUuidOperateur() {
		return uuidOperateur;
	}

	public void setUuidOperateur(String uuidOperateur) {
		this.uuidOperateur = uuidOperateur;
	}

	public String getUuidVehicule() {
		return uuidVehicule;
	}

	public void setUuidVehicule(String uuidVehicule) {
		this.uuidVehicule = uuidVehicule;
	}

	public String getUuidRequete() {
		return uuidRequete;
	}

	public void setUuidRequete(String uuidRequete) {
		this.uuidRequete = uuidRequete;
	}

	public String getUuidDocument() {
		return uuidDocument;
	}

	public void setUuidDocument(String uuidDocument) {
		this.uuidDocument = uuidDocument;
	}

	public String getUuidFacture() {
		return uuidFacture;
	}

	public void setUuidFacture(String uuidFacture) {
		this.uuidFacture = uuidFacture;
	}

	public String getUuidProposition() {
		return uuidProposition;
	}

	public void setUuidProposition(String uuidProposition) {
		this.uuidProposition = uuidProposition;
	}

	public String getCodePays() {
		return codePays;
	}

	public void setCodePays(String codePays) {
		this.codePays = codePays;
	}

	public ActionAudit() {
		super();
	}

	public ActionAudit(String uuidUtilisateur, String codePays) {
		super();
		this.uuidUtilisateur = uuidUtilisateur;
		this.codePays = codePays;
	}

	public String getUuidAdmin() {
		return uuidAdmin;
	}

	public void setUuidAdmin(String uuidAdmin) {
		this.uuidAdmin = uuidAdmin;
	}

	public String getUuidUtilisateur() {
		return uuidUtilisateur;
	}

	public void setUuidUtilisateur(String uuidUtilisateur) {
		this.uuidUtilisateur = uuidUtilisateur;
	}

	public String getUuidClient() {
		return uuidClient;
	}

	public void setUuidClient(String uuidClient) {
		this.uuidClient = uuidClient;
	}

	public String getUuidTransporteur() {
		return uuidTransporteur;
	}

	public void setUuidTransporteur(String uuidTransporteur) {
		this.uuidTransporteur = uuidTransporteur;
	}

	public String getUuidOperation() {
		return uuidOperation;
	}

	public void setUuidOperation(String uuidOperation) {
		this.uuidOperation = uuidOperation;
	}

	public String getUuidContact() {
		return uuidContact;
	}

	public void setUuidContact(String uuidContact) {
		this.uuidContact = uuidContact;
	}

	public String getUuidMotDePassePerdu() {
		return uuidMotDePassePerdu;
	}

	public void setUuidMotDePassePerdu(String uuidMotDePassePerdu) {
		this.uuidMotDePassePerdu = uuidMotDePassePerdu;
	}

	public String getUuidProprietaire() {
		return uuidProprietaire;
	}

	public void setUuidProprietaire(String uuidProprietaire) {
		this.uuidProprietaire = uuidProprietaire;
	}






}
