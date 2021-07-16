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

@ApiModel(description = "Document d'une opération")
@Entity
@Table(name = "operation_document", indexes = { @Index(name = "idx_operation_document" +
		"_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class OperationDocument implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(notes = "Identifiant")
	@Id
	@Type(type="uuid-char")
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected UUID uuid;

	@ApiModelProperty(notes = "UUID du document")
	@Column(name = "document", nullable = true, updatable = true)
	protected String document;

	@ApiModelProperty(notes = "Nom du fichier du document")
	@Column(name = "filename", nullable = false, updatable = true)
	protected String filename;
	
	@ApiModelProperty(notes = "Opération concernée par le document")	
	@OneToOne
	@JoinColumn(name = "operation", foreignKey = @ForeignKey(name = "fk_operation_document"))
	private Operation operation;

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
	
	@Column(name = "poids", nullable = true, updatable = true)
	private Long poids;

	@Column(name = "extension", nullable = true, updatable = true)
	private String extension;

	@ApiModelProperty(notes = "Code du pays")
	@Column(updatable = false, name = "codePays", nullable = true, unique = false)
	private String codePays;

	@ApiModelProperty(notes = "Type de compte qui a uploadé le document (DRIVER, EXPEDITEUR, ADMIN_KAMTAR, OPERATEUR")
	@Column(updatable = false, name = "type_compte", nullable = true, unique = false)
	private String type_compte;


	public String getType_compte() {
		return type_compte;
	}

	public void setType_compte(String type_compte) {
		this.type_compte = type_compte;
	}

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

	public OperationDocument() {
		super();
	}


	public OperationDocument(Integer ordre, Operation operation, Long poids, String extension, String filename, String type_compte) {
		super();
		this.operation = operation;
		this.poids = poids;
		this.extension = extension;
		this.filename = filename;
		this.codePays = operation.getCodePays();
		this.type_compte = type_compte;
	}

	public String getDocument() {
		return document;
	}

	public void setDocument(String document) {
		this.document = document;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}
	

	
}
