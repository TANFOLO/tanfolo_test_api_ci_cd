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
import com.kamtar.transport.api.controller.UtilisateurProprietaireController;
import com.kamtar.transport.api.params.CountryCreateParams;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Photo d'un véhicule")
@Entity
@Table(name = "vehicule_photo", indexes = { @Index(name = "idx_vehicule_photo_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class VehiculePhoto implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(notes = "Identifiant")
	@Id
	@Type(type="uuid-char")
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected UUID uuid;

	@ApiModelProperty(notes = "UUID de la photo du véhicule")
	@Column(name = "photo", nullable = true, updatable = true)
	protected String photo;

	@ApiModelProperty(notes = "Nom du fichier de la photo du véhicule")
	@Column(name = "filename", nullable = false, updatable = true)
	protected String filename;
	
	@ApiModelProperty(notes = "Véhicule concernée par la photo")	

	@OneToOne
	@JoinColumn(name = "vehicule", foreignKey = @ForeignKey(name = "fk_vehiculeè_photos_vehicule"))
	private Vehicule vehicule;

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
	
	@Column(name = "ordre", nullable = true, updatable = true)
	private Integer ordre;

	@Column(name = "poids", nullable = true, updatable = true)
	private Long poids;

	@Column(name = "extension", nullable = true, updatable = true)
	private String extension;

	@ApiModelProperty(notes = "Pays")
	@Column(updatable = true, name = "codePays", nullable = true, unique = false)
	protected String codePays;


	public String getCodePays() {
		return codePays;
	}

	public void setCodePays(String codePays) {
		this.codePays = codePays;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Integer getOrdre() {
		return ordre;
	}

	public void setOrdre(Integer ordre) {
		this.ordre = ordre;
	}

	public Long getPoids() {
		return poids;
	}

	public void setPoids(Long poids) {
		this.poids = poids;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public Vehicule getVehicule() {
		return vehicule;
	}

	public void setVehicule(Vehicule vehicule) {
		this.vehicule = vehicule;
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

	public VehiculePhoto() {
		super();
	}


	public VehiculePhoto(Integer ordre, Vehicule vehicule, Long poids, String extension, String filename) {
		super();
		this.ordre = ordre;
		this.vehicule = vehicule;
		this.poids = poids;
		this.extension = extension;
		this.filename = filename;
		this.codePays = vehicule.getCodePays();
	}
	

	
}
