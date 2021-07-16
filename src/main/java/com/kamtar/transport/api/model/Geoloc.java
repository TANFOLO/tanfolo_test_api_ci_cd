package com.kamtar.transport.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.Valid;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@ApiModel(description = "Geoloc")
@Entity
@Table(name = "geoloc", indexes = { @Index(name = "idx_geoloc_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class Geoloc implements Serializable {

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

	@ApiModelProperty(notes = "Plaque d'immariculation du véhicule")
	@Column(updatable = false, name = "immatriculation", nullable = false, length=250, unique = false)
	private String immatriculation;

	@ApiModelProperty(notes = "Latitude de la position du véhicule")
	@Column(updatable = false, name = "latitude", nullable = false, length=250, unique = false)
	private Double latitude;

	@ApiModelProperty(notes = "Longitude de la position du véhicule")
	@Column(updatable = false, name = "longitude", nullable = false, length=250, unique = false)
	private Double longitude;

	@ApiModelProperty(notes = "UUID du chauffeur")
	@Column(updatable = false, name = "driver", nullable = true, unique = false)
	private String driver;

	@ApiModelProperty(notes = "Nom du propriétaire/chauffeur")
	@Column(updatable = false, name = "nom", nullable = true, unique = false)
	private String nom;

	@ApiModelProperty(notes = "Prénom du propriétaire/chauffeur")
	@Column(updatable = false, name = "prenom", nullable = true, unique = false)
	private String prenom;

	@ApiModelProperty(notes = "Type de compte du propriétaire/chauffeur")
	@Column(updatable = false, name = "type_compte", nullable = true, unique = false)
	private String type_compte;

	@ApiModelProperty(notes = "Code du pays")
	@Column(updatable = false, name = "codePays", nullable = true, unique = false)
	private String codePays;

	public String getCodePays() {
		return codePays;
	}

	public void setCodePays(String codePays) {
		this.codePays = codePays;
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

	public String getType_compte() {
		return type_compte;
	}

	public void setType_compte(String type_compte) {
		this.type_compte = type_compte;
	}

	public Geoloc() {
	}



	public static long getSerialVersionUID() {
		return serialVersionUID;
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

	public String getImmatriculation() {
		return immatriculation;
	}

	public void setImmatriculation(String immatriculation) {
		this.immatriculation = immatriculation;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}
}
