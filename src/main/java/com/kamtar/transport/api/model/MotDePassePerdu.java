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

@ApiModel(description = "Demande de mot de passe perdu")
@Entity
@Table(name = "mot_de_passe_perdu", indexes = { @Index(name = "idx_mot_de_passe_perdu_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class MotDePassePerdu implements Serializable {

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

	@ApiModelProperty(notes = "Admin concerné")
	@OneToOne
	@JoinColumn(name = "adminKamtar", foreignKey = @ForeignKey(name = "fk_mot_de_passe_perdu_adminKamtar"))
	private UtilisateurAdminKamtar adminKamtar;

	@ApiModelProperty(notes = "Client personnel concerné")
	@OneToOne
	@JoinColumn(name = "clientPersonnel", foreignKey = @ForeignKey(name = "fk_mot_de_passe_perdu_utilisateur"))
	private UtilisateurClientPersonnel clientPersonnel;

	@ApiModelProperty(notes = "Opérateur concerné")
	@OneToOne
	@JoinColumn(name = "operateurKamtar", foreignKey = @ForeignKey(name = "fk_mot_de_passe_perdu_operateurKamtar"))
	private UtilisateurOperateurKamtar operateurKamtar;

	@ApiModelProperty(notes = "Expéditeur concerné")
	@OneToOne
	@JoinColumn(name = "client", foreignKey = @ForeignKey(name = "fk_mot_de_passe_perdu_client"))
	private UtilisateurClient client;

	@ApiModelProperty(notes = "Proprietaire concerné")
	@OneToOne
	@JoinColumn(name = "proprietaire", foreignKey = @ForeignKey(name = "fk_mot_de_passe_perdu_proprietaire"))
	private UtilisateurProprietaire proprietaire;

	@ApiModelProperty(notes = "Transporteur concerné")
	@OneToOne
	@JoinColumn(name = "transporteur", foreignKey = @ForeignKey(name = "fk_mot_de_passe_perdu_transporteur"))
	private UtilisateurDriver transporteur;

	@ApiModelProperty(notes = "Date réélle d'utilisation du token")
	@Column(updatable = true, name = "dateUtilisationToken", nullable = true, unique = false)	
	private Date dateUtilisationToken;

	@ApiModelProperty(notes = "Jeton pour identifier la demande de mot de passe perdu")
	@Column(updatable = true, name = "token", nullable = false, length=250, unique = false)	 
	private String token;

	@ApiModelProperty(notes = "Sur quel numero de téléphone la rpcoédure de mot de passe oublié a été envoyée")
	@Column(updatable = true, name = "destinataireTelephone", nullable = true, length=250, unique = false)	 
	private String destinataireTelephone;

	@ApiModelProperty(notes = "Sur quelle adressse email la rpcoédure de mot de passe oublié a été envoyée")
	@Column(updatable = true, name = "destinataireEmail", nullable = true, length=250, unique = false)	 
	private String destinataireEmail;

	@ApiModelProperty(notes = "Méthode d'envoi du lien (email ou sms)")
	@Transient
	private String envoi;

	@ApiModelProperty(notes = "Code du pays")
	@Column(updatable = false, name = "codePays", nullable = true, unique = false)
	private String codePays;


	public UtilisateurClientPersonnel getClientPersonnel() {
		return clientPersonnel;
	}

	public void setClientPersonnel(UtilisateurClientPersonnel clientPersonnel) {
		this.clientPersonnel = clientPersonnel;
	}

	public String getCodePays() {
		return codePays;
	}

	public void setCodePays(String codePays) {
		this.codePays = codePays;
	}
	
	public UtilisateurAdminKamtar getAdminKamtar() {
		return adminKamtar;
	}

	public void setAdminKamtar(UtilisateurAdminKamtar adminKamtar) {
		this.adminKamtar = adminKamtar;
	}

	public String getDestinataireTelephone() {
		return destinataireTelephone;
	}

	public void setDestinataireTelephone(String destinataireTelephone) {
		this.destinataireTelephone = destinataireTelephone;
	}

	public String getDestinataireEmail() {
		return destinataireEmail;
	}

	public void setDestinataireEmail(String destinataireEmail) {
		this.destinataireEmail = destinataireEmail;
	}

	public String getEnvoi() {
		return envoi;
	}

	public void setEnvoi(String envoi) {
		this.envoi = envoi;
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


	public UtilisateurOperateurKamtar getOperateurKamtar() {
		return operateurKamtar;
	}

	public void setOperateurKamtar(UtilisateurOperateurKamtar operateurKamtar) {
		this.operateurKamtar = operateurKamtar;
	}

	public UtilisateurDriver getTransporteur() {
		return transporteur;
	}

	public void setTransporteur(UtilisateurDriver transporteur) {
		this.transporteur = transporteur;
	}

	public Date getDateUtilisationToken() {
		return dateUtilisationToken;
	}

	public void setDateUtilisationToken(Date dateUtilisationToken) {
		this.dateUtilisationToken = dateUtilisationToken;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}


	public MotDePassePerdu(UtilisateurOperateurKamtar operateurKamtar, 
			UtilisateurDriver transporteur, String token, String destinataire_email, String destinataire_telephone, UtilisateurClient client, UtilisateurAdminKamtar admin, UtilisateurClientPersonnel utilisateur_personnel, UtilisateurProprietaire proprietaire) {
		super();
		this.operateurKamtar = operateurKamtar;
		this.transporteur = transporteur;
		this.client = client;
		this.token = token;
		this.destinataireEmail = destinataire_email;
		this.destinataireTelephone = destinataire_telephone;
		this.adminKamtar = admin;
		this.proprietaire = proprietaire;
		this.clientPersonnel = utilisateur_personnel;
		if (admin != null) {
			this.codePays = admin.getCodePays();
		} else if (operateurKamtar != null) {
			this.codePays = operateurKamtar.getCodePays();
		} else if (transporteur != null) {
			this.codePays = transporteur.getCodePays();
		} else	if (client != null) {
			this.codePays = client.getCodePays();
		} else	if (utilisateur_personnel != null) {
			this.codePays = proprietaire.getCodePays();
		}
	}

	public MotDePassePerdu() {
		super();
	}

	public UtilisateurClient getClient() {
		return client;
	}

	public void setClient(UtilisateurClient client) {
		this.client = client;
	}

	public UtilisateurProprietaire getProprietaire() {
		return proprietaire;
	}

	public void setProprietaire(UtilisateurProprietaire proprietaire) {
		this.proprietaire = proprietaire;
	}





}
