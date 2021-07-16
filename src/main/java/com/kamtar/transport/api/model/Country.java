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

@ApiModel(description = "Pays")
@Entity
@Table(name = "country")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class Country implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(notes = "Code")	
    @Id
    @Column(name = "code", nullable = false, updatable = false, length=10, unique = true)
	private String code;

	@ApiModelProperty(notes = "Nom du pays")
	@Column(updatable = true, name = "name", nullable = false, length=250, unique = false)	
	private String name;

	@ApiModelProperty(notes = "Code iso du pays sur trois caractères (https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3)")
	@Column(updatable = false, name = "code3", nullable = false, length=3, unique = true)	
	private String code3;

	@ApiModelProperty(notes = "Code des langues parlées")	
	@Transient
    @JsonIgnore
	private List<String> languages_code;	
	
	@ApiModelProperty(notes = "Code du continent")	
	@Column(updatable = true, name = "continent_code", nullable = true, length=3, unique = false)
    @JsonIgnore
	private String continent_code;
	
	@ApiModelProperty(notes = "Langues parlées dans le pays")	
	@OneToMany(
			targetEntity=Language.class,			
			cascade = CascadeType.DETACH
			)
	private List<Language> languages;

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

	@ApiModelProperty(notes = "Est ce que Kamtar opère ans ce pays ?")
	@Column(name = "opere", nullable = true, updatable = true)
	protected boolean opere = false;

	public boolean isOpere() {
		return opere;
	}

	public void setOpere(boolean opere) {
		this.opere = opere;
	}

	public Country(@Valid CountryCreateParams postBody) {
		super();
		this.code = postBody.getCode();
		this.name = postBody.getName();
		this.code3 = postBody.getCode3();
		this.languages_code = postBody.getLanguages();
		this.continent_code = postBody.getContinent();
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
	public Country() {
		super();
	}
	public String getCode3() {
		return code3;
	}
	public void setCode3(String code3) {
		this.code3 = code3;
	}
	public List<Language> getLanguages() {
		return languages;
	}
	public void setLanguages(List<Language> languages) {
		this.languages = languages;
	}
	public List<String> getLanguages_code() {
		return languages_code;
	}
	public void setLanguages_code(List<String> languages_code) {
		this.languages_code = languages_code;
	}
	public String getContinent_code() {
		return continent_code;
	}
	public void setContinent_code(String continent_code) {
		this.continent_code = continent_code;
	}
	

	
}
