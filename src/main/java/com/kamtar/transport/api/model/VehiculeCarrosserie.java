package com.kamtar.transport.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.Valid;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kamtar.transport.api.params.CountryCreateParams;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Carrosseries de véhicule")
@Entity
@Table(name = "vehicule_carrosseries")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class VehiculeCarrosserie implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(notes = "Code")	
    @Id
    @Column(name = "code", nullable = false, updatable = false, length=10, unique = true)
	private String code;

	/**
	 * utilisé en entete de colonne dans les exports excel des véhicules
	 */
	@ApiModelProperty(notes = "Nom de la carrosserie")
	@Column(updatable = true, name = "name", nullable = false, length=250, unique = false)	
	private String name;

	/**
	 * Liste des codes pays (séparé par un |) où la carrosserie est disponible/ Ex : |ci|sn|
	 */
	@ApiModelProperty(notes = "Liste des codes pays (séparé par un |) où la carrosserie est disponible/ Ex : |ci|sn|")
	@Column(updatable = true, name = "pays", nullable = true, length=250, unique = false)
	private String pays;

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

	@ApiModelProperty(notes = "Ordre")
	@Column(updatable = true, name = "ordre", nullable = true, unique = false)
	private Integer ordre;

	public String getPays() {
		return pays;
	}

	public void setPays(String pays) {
		this.pays = pays;
	}

	public Integer getOrdre() {
		return ordre;
	}

	public void setOrdre(Integer ordre) {
		this.ordre = ordre;
	}

	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public VehiculeCarrosserie() {
		super();
	}
	
	

	
}
