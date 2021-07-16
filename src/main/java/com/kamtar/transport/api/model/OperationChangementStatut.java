package com.kamtar.transport.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.*;
import javax.validation.Valid;

import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Changement de statut d'une opération")
@Entity
@Table(name = "operation_changement_statut", indexes = { @Index(name = "idx_operation_changement_statut_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class OperationChangementStatut implements Serializable {
	
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

	@ApiModelProperty(notes = "Date de dernière modification")	
	@Column(name = "updatedOn", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    @JsonIgnore
    private Date updatedOn = new Date();
	
	@ApiModelProperty(notes = "Ancien statut de l'operation")	
	@Column(updatable = true, name = "ancienStatut", nullable = true, length=250, unique = false)	
	private String ancienStatut;
	
	@ApiModelProperty(notes = "Nouveau statut de l'operation")	
	@Column(updatable = true, name = "nouveauStatut", nullable = false, length=250, unique = false)	 
	private String nouveauStatut;
	
	@ApiModelProperty(notes = "Opération concerné")	
	@OneToOne
	@JoinColumn(name = "operation", foreignKey = @ForeignKey(name = "fk_operation_changement_statut_operation"))
	private Operation operation;
	
	@ApiModelProperty(notes = "Opérateur qui a modifié le statut")	
	@OneToOne
	@JoinColumn(name = "modifieParOperateur", foreignKey = @ForeignKey(name = "fk_operation_changement_statut_operateur")) 
	private UtilisateurOperateurKamtar modifieParOperateur; 

	@ApiModelProperty(notes = "Transporteur qui a modifié le statut")	
	@OneToOne
	@JoinColumn(name = "modifieParTransporteur", foreignKey = @ForeignKey(name = "fk_operation_changement_statut_transporteur"))
	private UtilisateurDriver modifieParTransporteur;
	
	@ApiModelProperty(notes = "Proprietaire qui a modifié le statut")	
	@OneToOne
	@JoinColumn(name = "modifieParProprietaire", foreignKey = @ForeignKey(name = "fk_operation_changement_statut_proprietaire"))
	private UtilisateurProprietaire modifieParProprietaire;

	@ApiModelProperty(notes = "Code du pays")
	@Column(updatable = false, name = "codePays", nullable = true, unique = false)
	private String codePays;

	public String getCodePays() {
		return codePays;
	}

	public void setCodePays(String codePays) {
		this.codePays = codePays;
	}
	
	


	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public UtilisateurProprietaire getModifieParProprietaire() {
		return modifieParProprietaire;
	}

	public void setModifieParProprietaire(UtilisateurProprietaire modifieParProprietaire) {
		this.modifieParProprietaire = modifieParProprietaire;
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

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	public String getAncienStatut() {
		return ancienStatut;
	}

	public void setAncienStatut(String ancienStatut) {
		this.ancienStatut = ancienStatut;
	}

	public String getNouveauStatut() {
		return nouveauStatut;
	}

	public void setNouveauStatut(String nouveauStatut) {
		this.nouveauStatut = nouveauStatut;
	}

	public UtilisateurOperateurKamtar getModifieParOperateur() {
		return modifieParOperateur;
	}

	public void setModifieParOperateur(UtilisateurOperateurKamtar modifieParOperateur) {
		this.modifieParOperateur = modifieParOperateur;
	}

	public UtilisateurDriver getModifieParTransporteur() {
		return modifieParTransporteur;
	}

	public void setModifieParTransporteur(UtilisateurDriver modifieParTransporteur) {
		this.modifieParTransporteur = modifieParTransporteur;
	}


	public OperationChangementStatut(String ancienStatut, String nouveauStatut, Operation operation,
			UtilisateurOperateurKamtar modifieParOperateur, UtilisateurDriver modifieParTransporteur, UtilisateurProprietaire modifieParProprietaire, String code_pays
			) {
		super();
		this.ancienStatut = ancienStatut;
		this.nouveauStatut = nouveauStatut;
		this.operation = operation;
		this.modifieParOperateur = modifieParOperateur;
		this.modifieParTransporteur = modifieParTransporteur;
		this.modifieParProprietaire = modifieParProprietaire;
		this.codePays = code_pays;
	}
	
	

	public OperationChangementStatut() {
		super();
	}
	
	
	
}
