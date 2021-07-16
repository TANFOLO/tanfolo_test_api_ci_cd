package com.kamtar.transport.api.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.*;
import javax.validation.Valid;

import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kamtar.transport.api.params.ContactParams;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Contact")
@Entity
@Table(name = "contact", indexes = { @Index(name = "idx_contact_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class Contact implements Serializable {

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

	@ApiModelProperty(notes = "Motif de la prise de contact")
	@Column(updatable = false, name = "motif", nullable = false, length=250, unique = false)	
	private String motif;

	@ApiModelProperty(notes = "Message")
	@Column(updatable = false, name = "message", nullable = true, unique = false, columnDefinition="LONGTEXT")	
	private String message;

	@ApiModelProperty(notes = "Adresse e-mail du destinataire")
	@Column(updatable = false, name = "destinataire", nullable = true, length=2500, unique = false)	
	private String destinataire;

	@ApiModelProperty(notes = "Renseigné dans le cas où l'utilisateur qui envoit le message n'est pas identifié sur kamtar")
	@Column(updatable = false, name = "emetteurEmail", nullable = true, length=2500, unique = false)	
	private String emetteurEmail;

	@ApiModelProperty(notes = "Utilisateur qui a envoyé le message")
	@Column(updatable = false, name = "emetteurUuid", nullable = true, length=250, unique = false)	
	private String emetteurUuid;

	@ApiModelProperty(notes = "Code du pays")
	@Column(updatable = false, name = "codePays", nullable = true, unique = false)
	private String codePays;
	
	public Contact(@Valid ContactParams postBody, String destinataire, String emetteurUuid) {
		super();
		this.destinataire = destinataire;
		this.emetteurUuid = emetteurUuid;
		this.message = postBody.getMessage();
		this.emetteurEmail = postBody.getEmetteur_email();
		this.motif = postBody.getMotif();
		this.codePays = postBody.getPays();
		
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



	public String getMotif() {
		return motif;
	}



	public void setMotif(String motif) {
		this.motif = motif;
	}



	public String getMessage() {
		return message;
	}



	public void setMessage(String message) {
		this.message = message;
	}



	public String getDestinataire() {
		return destinataire;
	}



	public void setDestinataire(String destinataire) {
		this.destinataire = destinataire;
	}






	public String getEmetteurEmail() {
		return emetteurEmail;
	}



	public void setEmetteurEmail(String emetteurEmail) {
		this.emetteurEmail = emetteurEmail;
	}



	public String getEmetteurUuid() {
		return emetteurUuid;
	}



	public void setEmetteurUuid(String emetteurUuid) {
		this.emetteurUuid = emetteurUuid;
	}

	public Contact() {
	}
}
