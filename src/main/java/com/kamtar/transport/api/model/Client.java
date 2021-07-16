package com.kamtar.transport.api.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.kamtar.transport.api.controller.ClientController;
import com.kamtar.transport.api.params.EditClientPublicParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kamtar.transport.api.params.CreateClientAnonymeParams;
import com.kamtar.transport.api.params.CreateClientParams;
import com.kamtar.transport.api.params.EditClientParams;
import com.kamtar.transport.api.utils.UpdatableBCrypt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Client")
@Entity
@Table(name = "client", indexes = { @Index(name = "idx_client_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = { "updatedOn"}, allowGetters = true)
public class Client implements Serializable {
	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(Client.class);

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(notes = "Identifiant")	
	@Id
	@Type(type="uuid-char")
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected UUID uuid;

	@ApiModelProperty(notes = "Nom de l'expéditeur")	
	@Column(updatable = true, name = "nom", nullable = false, length=250, unique = false)	
	private String nom;

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

	@ApiModelProperty(notes = "Adresse e-mail du client")	
	@Column(updatable = true, name = "contactEmail", nullable = true, length=200, unique = false)	
	private String contactEmail;

	@ApiModelProperty(notes = "Code du pays du numéro de téléphone principal de l'utilisateur")
	@Column(name = "numeroTelephone1Pays", nullable = true, updatable = true, columnDefinition = "varchar(10) default 'CI'")
	protected String numeroTelephone1Pays;

	@ApiModelProperty(notes = "Numéro de téléphone principal du client")	
	@Column(updatable = true, name = "contactNumeroDeTelephone1", nullable = true, length=200, unique = false)	
	private String contactNumeroDeTelephone1;

	@ApiModelProperty(notes = "Code du pays du numéro de téléphone principal de l'utilisateur")
	@Column(name = "numeroTelephone2Pays", nullable = true, updatable = true, columnDefinition = "varchar(10) default 'CI'")
	protected String numeroTelephone2Pays;

	@ApiModelProperty(notes = "Numéro de téléphone secondaire du client")	
	@Column(updatable = true, name = "contactNumeroDeTelephone2", nullable = true, length=200, unique = false)	
	private String contactNumeroDeTelephone2;

	@ApiModelProperty(notes = "Type de compte du client (B = pro, C = particulier)")	
	@Column(updatable = true, name = "typeCompte", nullable = false, length=1, unique = false)	
	private String typeCompte;

	@JsonIgnore
	@Column(updatable = true, name = "motDePasse", nullable = true, length=200, unique = false)	
	private String motDePasse;

	@ApiModelProperty(notes = "Adresse de facturation - première ligne")	
	@Column(updatable = true, name = "adresseFacturationLigne1", nullable = true, length=200, unique = false)	
	private String adresseFacturationLigne1;

	@ApiModelProperty(notes = "Adresse de facturation - seconde ligne")
	@Column(updatable = true, name = "adresseFacturationLigne2", nullable = true, length=200, unique = false)	
	private String adresseFacturationLigne2;

	@ApiModelProperty(notes = "Adresse de facturation - troisième ligne")
	@Column(updatable = true, name = "adresseFacturationLigne3", nullable = true, length=200, unique = false)	
	private String adresseFacturationLigne3;

	@ApiModelProperty(notes = "Adresse de facturation - quatrième ligne")
	@Column(updatable = true, name = "adresseFacturationLigne4", nullable = true, length=200, unique = false)	
	private String adresseFacturationLigne4;

	@ApiModelProperty(notes = "Opérateur qui a créé le client")
	@OneToOne
	@JoinColumn(name = "operateur", foreignKey = @ForeignKey(name = "fk_client_operateur"))
	private UtilisateurOperateurKamtar operateur;

	@ApiModelProperty(notes = "Code du pays")
	@Column(updatable = true, name = "codePays", nullable = true, unique = false)	
	private String codePays;

	@ApiModelProperty(notes = "Langue")
	@Column(name = "locale", nullable = true, updatable = true)
	protected String locale;
	
	@ApiModelProperty(notes = "Utilisateur qui s'occupe du client")
	@OneToOne
	@JoinColumn(name = "utilisateur", foreignKey = @ForeignKey(name = "fk_client_utilisateur"))
	private UtilisateurClient utilisateur;

	@ApiModelProperty(notes = "Compte contribuable")
	@Column(updatable = true, name = "compteContribuable", nullable = true, unique = false)
	private String compteContribuable;

	@ApiModelProperty(notes = "Compte contribuable")
	@Column(updatable = true, name = "numeroRCCM", nullable = true, unique = false)
	private String numeroRCCM;
	
	@ApiModelProperty(notes = "Délais")
	@Column(updatable = true, name = "delais", nullable = true, unique = false)	
	private Integer delais;
	
	@ApiModelProperty(notes = "Code du pays")
	@Column(updatable = true, name = "modePaiement", nullable = true, unique = false)	
	private String modePaiement;



	public String getNumeroTelephone1Pays() {
		return numeroTelephone1Pays;
	}

	public void setNumeroTelephone1Pays(String numeroTelephone1Pays) {
		this.numeroTelephone1Pays = numeroTelephone1Pays;
	}

	public String getNumeroTelephone2Pays() {
		return numeroTelephone2Pays;
	}

	public void setNumeroTelephone2Pays(String numeroTelephone2Pays) {
		this.numeroTelephone2Pays = numeroTelephone2Pays;
	}

	public String getNumeroRCCM() {
		return numeroRCCM;
	}

	public void setNumeroRCCM(String numeroRCCM) {
		this.numeroRCCM = numeroRCCM;
	}

	public String getCompteContribuable() {
		return compteContribuable;
	}

	public void setCompteContribuable(String compteContribuable) {
		this.compteContribuable = compteContribuable;
	}

	public Integer getDelais() {
		return delais;
	}

	public void setDelais(Integer delais) {
		this.delais = delais;
	}

	public String getModePaiement() {
		return modePaiement;
	}

	public void setModePaiement(String modePaiement) {
		this.modePaiement = modePaiement;
	}

	public String getCodePays() {
		return codePays;
	}

	public void setCodePays(String codePays) {
		this.codePays = codePays;
	}


	public UtilisateurClient getUtilisateur() {
		return utilisateur;
	}

	public void setUtilisateur(UtilisateurClient utilisateur) {
		this.utilisateur = utilisateur;
	}

	public Client(Devis devis, String mot_de_passe, UtilisateurClient utilisateur_client) {
		super();
		if (devis.getClientNomSociete() != null && !"".equals(devis.getClientNomSociete())) {
			this.nom = devis.getClientNomSociete();
		} else {
			this.nom = devis.getClientPrenom() + " " + devis.getClientNom();
		}
		this.contactEmail = devis.getClientEmail();
		this.contactNumeroDeTelephone1 = devis.getClientTelephone();
		this.motDePasse = UpdatableBCrypt.hashPassword(mot_de_passe);
		this.typeCompte = devis.getTypeCompte();
		this.codePays = devis.getCodePays();
		this.numeroTelephone1Pays  = devis.getClientTelephonePays();
		this.utilisateur = utilisateur_client;
	}
	
	public Client(@Valid CreateClientAnonymeParams postBody) {
		super();
		if (postBody.getEntreprise_nom() != null && postBody.getEntreprise_nom().trim().length() > 0) {
			this.nom = postBody.getEntreprise_nom();
		} else {
			this.nom = postBody.getNom();
		}

		this.contactEmail = postBody.getEmail();
		this.contactNumeroDeTelephone1 = postBody.getTelephone1();
		this.contactNumeroDeTelephone2 = postBody.getTelephone2();
		this.motDePasse = UpdatableBCrypt.hashPassword(postBody.getMot_de_passe());
		this.typeCompte = postBody.getType_compte();
		this.codePays = postBody.getCode_pays(); 
		this.adresseFacturationLigne1 = postBody.getAdresse_facturation_ligne_1();
		this.adresseFacturationLigne2 = postBody.getAdresse_facturation_ligne_2();
		this.adresseFacturationLigne3 = postBody.getAdresse_facturation_ligne_3();
		this.adresseFacturationLigne4 = postBody.getAdresse_facturation_ligne_4();
		this.compteContribuable = postBody.getEntreprise_compte_comptable();
		this.numeroRCCM = postBody.getEntreprise_numero_rccm();
		this.numeroTelephone1Pays  = postBody.getPays_telephone_1();
		this.numeroTelephone2Pays  = postBody.getPays_telephone_2();
		
	}

	public Client(@Valid CreateClientParams postBody, UtilisateurOperateurKamtar operateur) {
		super();
		this.nom = postBody.getNom();
		this.adresseFacturationLigne1 = postBody.getAdresse_facturation_ligne_1();
		this.adresseFacturationLigne2 = postBody.getAdresse_facturation_ligne_2();
		this.adresseFacturationLigne3 = postBody.getAdresse_facturation_ligne_3();
		this.adresseFacturationLigne4 = postBody.getAdresse_facturation_ligne_4();
		this.contactEmail = postBody.getContact_email();
		this.contactNumeroDeTelephone1 = postBody.getContact_numero_telephone1();
		this.contactNumeroDeTelephone2 = postBody.getContact_numero_telephone2();
		this.motDePasse = UpdatableBCrypt.hashPassword(postBody.getMot_de_passe());
		this.operateur = operateur;
		this.typeCompte = postBody.getType_compte();
		this.codePays = postBody.getCode_pays();
		this.compteContribuable = postBody.getCompte_contribuable();
		this.delais = postBody.getDelais_paiement();
		this.modePaiement = postBody.getMode_paiement();
		this.numeroRCCM = postBody.getNumero_rccm();
		this.numeroTelephone1Pays  = postBody.getPays_telephone_1();
		this.numeroTelephone2Pays  = postBody.getPays_telephone_2();
		
	}


	public void edit(EditClientPublicParams params) {

		this.nom = params.getNom();
		this.adresseFacturationLigne1 = params.getAdresse_facturation_ligne_1();
		this.adresseFacturationLigne2 = params.getAdresse_facturation_ligne_2();
		this.adresseFacturationLigne3 = params.getAdresse_facturation_ligne_3();
		this.adresseFacturationLigne4 = params.getAdresse_facturation_ligne_4();
		this.contactEmail = params.getContact_email();
		this.contactNumeroDeTelephone1 = params.getContact_numero_telephone1();
		this.contactNumeroDeTelephone2 = params.getContact_numero_telephone2();
		this.numeroTelephone1Pays  = params.getPays_telephone_1();
		this.numeroTelephone2Pays  = params.getPays_telephone_2();

		if (params.getMot_de_passe() != null && !"".equals(params.getMot_de_passe().trim())) {
			this.motDePasse = UpdatableBCrypt.hashPassword(params.getMot_de_passe());
		}

		this.compteContribuable = params.getCompte_contribuable();
		this.numeroRCCM = params.getNumero_rccm();

	}


	public void edit(EditClientParams params) {

		this.nom = params.getNom();
		this.adresseFacturationLigne1 = params.getAdresse_facturation_ligne_1();
		this.adresseFacturationLigne2 = params.getAdresse_facturation_ligne_2();
		this.adresseFacturationLigne3 = params.getAdresse_facturation_ligne_3();
		this.adresseFacturationLigne4 = params.getAdresse_facturation_ligne_4();
		this.contactEmail = params.getContact_email();
		this.contactNumeroDeTelephone1 = params.getContact_numero_telephone1();
		this.contactNumeroDeTelephone2 = params.getContact_numero_telephone2();
		this.typeCompte = params.getType_compte();
		this.codePays = params.getCode_pays();

		if (params.getMot_de_passe() != null && !"".equals(params.getMot_de_passe().trim())) {
			this.motDePasse = UpdatableBCrypt.hashPassword(params.getMot_de_passe());
		}

		this.compteContribuable = params.getCompte_contribuable();
		this.delais = params.getDelais_paiement();
		this.modePaiement = params.getMode_paiement();
		this.numeroRCCM = params.getNumero_rccm();

		this.numeroTelephone1Pays  = params.getPays_telephone_1();
		this.numeroTelephone2Pays  = params.getPays_telephone_2();


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
	public Client() {
		super();
	}
	public UUID getUuid() {
		return uuid;
	}
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getContactNumeroDeTelephone1() {
		return contactNumeroDeTelephone1;
	}

	public void setContactNumeroDeTelephone1(String contactNumeroDeTelephone1) {
		this.contactNumeroDeTelephone1 = contactNumeroDeTelephone1;
	}

	public String getContactNumeroDeTelephone2() {
		return contactNumeroDeTelephone2;
	}

	public void setContactNumeroDeTelephone2(String contactNumeroDeTelephone2) {
		this.contactNumeroDeTelephone2 = contactNumeroDeTelephone2;
	}

	public String getTypeCompte() {
		return typeCompte;
	}

	public void setTypeCompte(String typeCompte) {
		this.typeCompte = typeCompte;
	}

	public String getMotDePasse() {
		return motDePasse;
	}

	public void setMotDePasse(String motDePasse) {
		this.motDePasse = motDePasse;
	}

	public String getAdresseFacturationLigne1() {
		return adresseFacturationLigne1;
	}

	public void setAdresseFacturationLigne1(String adresseFacturationLigne1) {
		this.adresseFacturationLigne1 = adresseFacturationLigne1;
	}

	public String getAdresseFacturationLigne2() {
		return adresseFacturationLigne2;
	}

	public void setAdresseFacturationLigne2(String adresseFacturationLigne2) {
		this.adresseFacturationLigne2 = adresseFacturationLigne2;
	}

	public String getAdresseFacturationLigne3() {
		return adresseFacturationLigne3;
	}

	public void setAdresseFacturationLigne3(String adresseFacturationLigne3) {
		this.adresseFacturationLigne3 = adresseFacturationLigne3;
	}

	public String getAdresseFacturationLigne4() {
		return adresseFacturationLigne4;
	}

	public void setAdresseFacturationLigne4(String adresseFacturationLigne4) {
		this.adresseFacturationLigne4 = adresseFacturationLigne4;
	}

	public UtilisateurOperateurKamtar getOperateur() {
		return operateur;
	}

	public void setOperateur(UtilisateurOperateurKamtar operateur) {
		this.operateur = operateur;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getCouleur() {
		if (this.getTypeCompte().equals("B")) {
			if (this.getNumeroRCCM() == null || "".equals(this.getNumeroRCCM().trim()) ||
					this.getCompteContribuable() == null || "".equals(this.getCompteContribuable().trim()) ||
					this.getDelais() == null ||
					this.getModePaiement() == null || "".equals(this.getModePaiement().trim()) ||
					this.getAdresseFacturationLigne1() == null || "".equals(this.getAdresseFacturationLigne1().trim())) {
				return "O";
			}
		}
		return "V";
	}


}
