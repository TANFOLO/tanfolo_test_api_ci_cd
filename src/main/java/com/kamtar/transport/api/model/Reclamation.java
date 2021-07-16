package com.kamtar.transport.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kamtar.transport.api.enums.ReclamationStatut;
import com.kamtar.transport.api.params.ContactParams;
import com.kamtar.transport.api.params.CreateReclamationParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.Valid;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Réclamation")
@Entity
@Table(name = "reclamation", indexes = { @Index(name = "idx_reclamation_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class Reclamation implements Serializable {

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

	@ApiModelProperty(notes = "Motif de la reclamation")
	@Column(updatable = false, name = "motif", nullable = false, length=250, unique = false)
	private String motif;

	@ApiModelProperty(notes = "Descriptif de la reclamation")
	@Column(updatable = false, name = "descriptif", nullable = true, unique = false, columnDefinition="LONGTEXT")
	private String descriptif;

	@ApiModelProperty(notes = "Operation concerné")
	@OneToOne
	@JoinColumn(name = "operation", foreignKey = @ForeignKey(name = "fk_reclamation_operation"))
	private Operation operation;

	@ApiModelProperty(notes = "Adresse e-mail du destinataire")
	@Column(updatable = false, name = "destinataire", nullable = true, length=2500, unique = false)
	private String destinataire;

	@ApiModelProperty(notes = "Code de la reclamation")
	@Column(updatable = false, name = "code", nullable = false, unique = true)
	private Long code;

	@ApiModelProperty(notes = "Client qui envoit la reclamation")
	@OneToOne
	@JoinColumn(name = "client", foreignKey = @ForeignKey(name = "fk_reclamation_client"))
	private Client client;

	@ApiModelProperty(notes = "Client (utilisateur personnel) qui envoit la reclamation")
	@OneToOne
	@JoinColumn(name = "client_personnel", foreignKey = @ForeignKey(name = "fk_reclamation_client_personnel"))
	private UtilisateurClientPersonnel client_personnel;

	@ApiModelProperty(notes = "Client (utilisateur) qui envoit la reclamation")
	@OneToOne
	@JoinColumn(name = "client_utilisateur", foreignKey = @ForeignKey(name = "fk_reclamation_client_utilisateur"))
	private UtilisateurClient client_utilisateur;

	@ApiModelProperty(notes = "Statut")
	@Column(updatable = true, name = "statut", nullable = true, length = 100, unique = false)
	private String statut;

	@ApiModelProperty(notes = "Code du pays")
	@Column(updatable = false, name = "codePays", nullable = true, unique = false)
	private String codePays;

	@Transient
	private List<ReclamationEchange> echanges;

	public Reclamation(@Valid CreateReclamationParams postBody, Operation operation, String destinataire, Long code, Client client, UtilisateurClientPersonnel client_personnel, UtilisateurClient utilisateur_client, String code_pays) {
		super();
		this.destinataire = destinataire;
		this.motif = postBody.getMotif();
		this.descriptif = postBody.getDescriptif();
		this.operation = operation;
		this.code = code;
		this.client = client;
		this.client_personnel = client_personnel;
		this.client_utilisateur = utilisateur_client;
		this.codePays = code_pays;
		this.statut = ReclamationStatut.EN_ATTENTE.toString();
		this.createdOn = new Date();
		this.updatedOn = new Date();

	}

	public List<ReclamationEchange> getEchanges() {
		return echanges;
	}

	public void setEchanges(List<ReclamationEchange> echanges) {
		this.echanges = echanges;
	}

	public String getStatut() {
		return statut;
	}

	public void setStatut(String statut) {
		this.statut = statut;
	}

	public UtilisateurClient getClient_utilisateur() {
		return client_utilisateur;
	}

	public void setClient_utilisateur(UtilisateurClient client_utilisateur) {
		this.client_utilisateur = client_utilisateur;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public UtilisateurClientPersonnel getClient_personnel() {
		return client_personnel;
	}

	public void setClient_personnel(UtilisateurClientPersonnel client_personnel) {
		this.client_personnel = client_personnel;
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

	public String getDescriptif() {
		return descriptif;
	}

	public void setDescriptif(String descriptif) {
		this.descriptif = descriptif;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public String getDestinataire() {
		return destinataire;
	}

	public void setDestinataire(String destinataire) {
		this.destinataire = destinataire;
	}

	public Long getCode() {
		return code;
	}

	public void setCode(Long code) {
		this.code = code;
	}

	public Reclamation() {
	}
}
