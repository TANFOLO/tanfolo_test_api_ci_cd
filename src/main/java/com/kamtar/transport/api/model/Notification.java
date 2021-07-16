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
import com.kamtar.transport.api.params.CountryCreateParams;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Notification de backoffice")
@Entity
@Table(name = "notification", indexes = { @Index(name = "idx_notification_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class Notification implements Serializable {

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
	
	@ApiModelProperty(notes = "Type de notification")
	@Column(updatable = true, name = "type_notif", nullable = true, length=250, unique = false)	
	private String type;
	
	@ApiModelProperty(notes = "Opérateur associé à la notification")
	@OneToOne
	@JoinColumn(name = "operateur", foreignKey = @ForeignKey(name = "fk_notification_operateur"))  
	private UtilisateurOperateurKamtar operateur; 
	
	@ApiModelProperty(notes = "Message de la notification")
	@Column(updatable = true, name = "message", nullable = true, length=2500, unique = false)	
	private String message;
	
	@ApiModelProperty(notes = "Commande associée à la notification")
	@Column(updatable = true, name = "commande_uuid", nullable = true, length=250, unique = false)	
	private String commande_uuid;
	
	@ApiModelProperty(notes = "Expéditeur associé à la notification")
	@Column(updatable = true, name = "client_uuid", nullable = true, length=250, unique = false)	
	private String client_uuid;

	@ApiModelProperty(notes = "Propriétaire associé à la notification")
	@Column(updatable = true, name = "proprietaire_uuid", nullable = true, length=250, unique = false)
	private String proprietaire_uuid;

	@ApiModelProperty(notes = "Driver associé à la notification")
	@Column(updatable = true, name = "driver_uuid", nullable = true, length=250, unique = false)
	private String driver_uuid;

	@ApiModelProperty(notes = "Véhicule associé à la notification")
	@Column(updatable = true, name = "vehicule_uuid", nullable = true, length=250, unique = false)
	private String vehicule_uuid;

	@ApiModelProperty(notes = "Date et heure à laquelle la notification a été traitée")
	@Column(updatable = true, name = "dateIndiqueeTraitee", nullable = true, unique = false)	 
	private Date dateIndiqueeTraitee;

	@ApiModelProperty(notes = "Code du pays")
	@Column(updatable = false, name = "codePays", nullable = true, unique = false)
	private String codePays;

	public String getVehicule_uuid() {
		return vehicule_uuid;
	}

	public void setVehicule_uuid(String vehicule_uuid) {
		this.vehicule_uuid = vehicule_uuid;
	}

	public String getProprietaire_uuid() {
		return proprietaire_uuid;
	}

	public void setProprietaire_uuid(String proprietaire_uuid) {
		this.proprietaire_uuid = proprietaire_uuid;
	}

	public String getDriver_uuid() {
		return driver_uuid;
	}

	public void setDriver_uuid(String driver_uuid) {
		this.driver_uuid = driver_uuid;
	}

	public String getCodePays() {
		return codePays;
	}

	public void setCodePays(String codePays) {
		this.codePays = codePays;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public UtilisateurOperateurKamtar getOperateur() {
		return operateur;
	}

	public void setOperateur(UtilisateurOperateurKamtar operateur) {
		this.operateur = operateur;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCommande_uuid() {
		return commande_uuid;
	}

	public void setCommande_uuid(String commande_uuid) {
		this.commande_uuid = commande_uuid;
	}

	public String getClient_uuid() {
		return client_uuid;
	}

	public void setClient_uuid(String client_uuid) {
		this.client_uuid = client_uuid;
	}

	public Notification(String type, UtilisateurOperateurKamtar operateur, String message, String commande_uuid,
			String client_uuid, String code_pays) {
		super();
		this.type = type;
		this.operateur = operateur;
		this.message = message;
		this.commande_uuid = commande_uuid;
		this.client_uuid = client_uuid;
		this.codePays = code_pays;
	}

	public Notification(String type, String message, String proprietaire_uuid,
						String driver_uuid, String code_pays) {
		super();
		this.type = type;
		this.proprietaire_uuid = proprietaire_uuid;
		this.message = message;
		this.driver_uuid = driver_uuid;
		this.codePays = code_pays;
	}

	public Notification(String type, String message, String proprietaire_uuid,
						String vehicule_uuid, String code_pays, boolean a) {
		super();
		this.type = type;
		this.proprietaire_uuid = proprietaire_uuid;
		this.message = message;
		this.vehicule_uuid = vehicule_uuid;
		this.codePays = code_pays;
	}

	public Notification() {
		super();
	}

	public Date getDateIndiqueeTraitee() {
		return dateIndiqueeTraitee;
	}

	public void setDateIndiqueeTraitee(Date dateIndiqueeTraitee) {
		this.dateIndiqueeTraitee = dateIndiqueeTraitee;
	}


	
	
	
}
