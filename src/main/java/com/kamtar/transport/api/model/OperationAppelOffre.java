package com.kamtar.transport.api.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.persistence.*;

import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kamtar.transport.api.enums.OperationStatut;
import com.kamtar.transport.api.params.CreateEditOperationAppelOffreParams;
import com.kamtar.transport.api.params.CreateOperationParClientParams;
import com.kamtar.transport.api.params.CreateOperationParams;
import com.kamtar.transport.api.params.EditOperationParams;
import com.kamtar.transport.api.repository.OperationAppelOffreRepository;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import springfox.documentation.spring.web.readers.operation.OperationDeprecatedReader;

@ApiModel(description = "OperationAppelOffre")
@Entity
@Table(name = "operation_appel_offre", indexes = { @Index(name = "idx_operation_appel_offre_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {}, allowGetters = true)
public class OperationAppelOffre implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(notes = "Identifiant")
	@Id
	@Type(type="uuid-char")
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected UUID uuid;

	@ApiModelProperty(notes = "Date de création")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	@Column(name = "createdOn", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@CreatedDate
	private Date createdOn = new Date();

	@ApiModelProperty(notes = "Date de dernière mise à jour")
	@Column(name = "updatedOn", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@LastModifiedDate
	private Date updatedOn = new Date();

	@ApiModelProperty(notes = "Opération à laquelle l'appel d'offre est attaché")
	@OneToOne
	@JoinColumn(name = "operation", foreignKey = @ForeignKey(name = "fk_operation_appel_offre_driver_operation"))
	private Operation operation;

	@ApiModelProperty(notes = "Véhicule à laquelle l'appel d'offre est attaché")
	@OneToOne
	@JoinColumn(name = "vehicule", foreignKey = @ForeignKey(name = "fk_operation_appel_offre_vehicule_operation"))
	private Vehicule vehicule;

	@ApiModelProperty(notes = "Transporteur sondé par l'appel d'offre")
	@OneToOne
	@JoinColumn(name = "transporteur", foreignKey = @ForeignKey(name = "fk_operation_appel_offre_driver_transporteur"))
	private UtilisateurDriver transporteur;

	@ApiModelProperty(notes = "Propriétaire sondé par l'appel d'offre")
	@OneToOne
	@JoinColumn(name = "proprietaire", foreignKey = @ForeignKey(name = "fk_operation_appel_offre_proprietaire_transporteur"))
	private UtilisateurProprietaire proprietaire;

	@ApiModelProperty(notes = "Opérateur qui envoi l'appel d'offre")
	@OneToOne
	@JoinColumn(name = "operateur", foreignKey = @ForeignKey(name = "fk_operation_operateur"))
	private UtilisateurOperateurKamtar operateur;

	@ApiModelProperty(notes = "Montant proposé par le driver")	
	@Column(updatable = true, name = "montantPropose", nullable = true, unique = false)	
	private Double montantPropose; 

	@ApiModelProperty(notes = "Devise du montant proposé par le driver")
	@Column(updatable = true, name = "montantProposeDevise", unique = false)	
	protected String montantProposeDevise;

	@ApiModelProperty(notes = "Est ce que la proposition a été accepté/refusée ? null = pas encore répondu, 1 = accepté, 2 = proposée, 3 = refusée")
	@Column(name = "statut", nullable = true, updatable = true)
	protected String statut;

	@ApiModelProperty(notes = "Code du pays")
	@Column(updatable = false, name = "codePays", nullable = true, unique = false)
	private String codePays;

	public String getCodePays() {
		return codePays;
	}

	public void setCodePays(String codePays) {
		this.codePays = codePays;
	}


	public UtilisateurProprietaire getProprietaire() {
		return proprietaire;
	}

	public void setProprietaire(UtilisateurProprietaire proprietaire) {
		this.proprietaire = proprietaire;
	}

	public String getStatut() {
		return statut;
	}

	public void setStatut(String statut) {
		this.statut = statut;
	}

	public Vehicule getVehicule() {
		return vehicule;
	}

	public void setVehicule(Vehicule vehicule) {
		this.vehicule = vehicule;
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

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public UtilisateurDriver getTransporteur() {
		return transporteur;
	}

	public void setTransporteur(UtilisateurDriver transporteur) {
		this.transporteur = transporteur;
	}

	public UtilisateurOperateurKamtar getOperateur() {
		return operateur;
	}

	public void setOperateur(UtilisateurOperateurKamtar operateur) {
		this.operateur = operateur;
	}

	public Double getMontantPropose() {
		return montantPropose;
	}

	public void setMontantPropose(Double montantPropose) {
		this.montantPropose = montantPropose;
	}

	public String getMontantProposeDevise() {
		return montantProposeDevise;
	}

	public void setMontantProposeDevise(String montantProposeDevise) {
		this.montantProposeDevise = montantProposeDevise;
	}

	public OperationAppelOffre(Operation operation, UtilisateurDriver transporteur,
			UtilisateurOperateurKamtar operateur, String code_pays) {
		super();
		this.operation = operation;
		this.transporteur = transporteur;
		this.operateur = operateur;
		this.createdOn = new Date();
		this.updatedOn = new Date();
		this.codePays = code_pays;
	}

	public OperationAppelOffre(Operation operation, UtilisateurDriver transporteur,
			UtilisateurOperateurKamtar operateur, Double montantPropose, String montantProposeDevise, Vehicule vehicule, String code_pays) {
		super();
		this.operation = operation;
		this.transporteur = transporteur;
		this.operateur = operateur;
		this.montantPropose = montantPropose;
		this.montantProposeDevise = montantProposeDevise;
		this.vehicule = vehicule;
		this.createdOn = new Date();
		this.updatedOn = new Date();
		this.codePays = code_pays;
	}


	public void edit(CreateEditOperationAppelOffreParams params) {
		this.montantPropose = params.getMontant();
		this.montantProposeDevise = params.getMontant_devise();
		this.updatedOn = new Date();
	}

	public OperationAppelOffre() {
		super();
	}



}
