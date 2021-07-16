package com.kamtar.transport.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Facture client")
@Entity
@Table(name = "facture_client", indexes = { @Index(name = "idx_facture_client_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class FactureClient implements Serializable {

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

	@ApiModelProperty(notes = "Client")
	@OneToOne
	@JoinColumn(name = "client", foreignKey = @ForeignKey(name = "fk_facture_client"))
	private Client client;

	@ApiModelProperty(notes = "Numero facture")
	private String numeroFacture;

	@ApiModelProperty(notes = "Date de la facture")
	@Column(updatable = true, name = "dateFacture", nullable = true, unique = false)
	private Date dateFacture;

	@ApiModelProperty(notes = "Liste des opérations. Ex : @34@78@120@")
	@Column(updatable = true, name = "listeOperations", nullable = true, length=2500, unique = false)
	private String listeOperations;

	@ApiModelProperty(notes = "Notes spéciales sur la facture")
	@Column(updatable = true, name = "notesSpeciales", nullable = true, length=2500, unique = false)
	private String notesSpeciales;

	@ApiModelProperty(notes = "Montant HT")
	@Column(updatable = true, name = "montantHT", nullable = true, unique = false)
	private Double montantHT;

	@ApiModelProperty(notes = "Remise en pourcentage")
	@Column(updatable = true, name = "remisePourcentage", nullable = true, unique = false)
	private Double remisePourcentage;

	@ApiModelProperty(notes = "Montant de la TVA")
	@Column(updatable = true, name = "montantTVA", nullable = true, unique = false)
	private Double montantTVA;

	@ApiModelProperty(notes = "Montant TTC")
	@Column(updatable = true, name = "montantTTC", nullable = true, unique = false)
	private Double montantTTC;

	@ApiModelProperty(notes = "net à payer")
	@Column(updatable = true, name = "netAPayer", nullable = true, unique = false)
	private Double netAPayer;

	@ApiModelProperty(notes = "UUID du fichier PDF de la facture")
	@Column(name = "fichier", nullable = true, updatable = true)
	protected String fichier;

	@ApiModelProperty(notes = "Code du pays")
	@Column(updatable = false, name = "codePays", nullable = true, unique = false)
	private String codePays;

	@Transient
	private List<Operation> operations;

	public List<Operation> getOperations() {
		return operations;
	}

	public void setOperations(List<Operation> operations) {
		this.operations = operations;
	}

	public String getCodePays() {
		return codePays;
	}

	public void setCodePays(String codePays) {
		this.codePays = codePays;
	}

	public String getFichier() {
		return fichier;
	}

	public void setFichier(String fichier) {
		this.fichier = fichier;
	}

	public FactureClient() {
		super();
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

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public String getNumeroFacture() {
		return numeroFacture;
	}

	public void setNumeroFacture(String numeroFacture) {
		this.numeroFacture = numeroFacture;
	}

	public Date getDateFacture() {
		return dateFacture;
	}
	public String getDateFactureFormatted() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
		return simpleDateFormat.format(dateFacture);
	}

	public void setDateFacture(Date dateFacture) {
		this.dateFacture = dateFacture;
	}

	public String getListeOperations() {
		return listeOperations;
	}

	public void setListeOperations(String listeOperations) {
		this.listeOperations = listeOperations;
	}

	public String getNotesSpeciales() {
		return notesSpeciales;
	}

	public void setNotesSpeciales(String notesSpeciales) {
		this.notesSpeciales = notesSpeciales;
	}

	public Double getMontantHT() {
		return montantHT;
	}

	public void setMontantHT(Double montantHT) {
		this.montantHT = montantHT;
	}

	public Double getRemisePourcentage() {
		return remisePourcentage;
	}

	public void setRemisePourcentage(Double remisePourcentage) {
		this.remisePourcentage = remisePourcentage;
	}

	public Double getMontantTVA() {
		return montantTVA;
	}

	public void setMontantTVA(Double montantTVA) {
		this.montantTVA = montantTVA;
	}

	public Double getMontantTTC() {
		return montantTTC;
	}

	public void setMontantTTC(Double montantTTC) {
		this.montantTTC = montantTTC;
	}

	public Double getNetAPayer() {
		return netAPayer;
	}

	public void setNetAPayer(Double netAPayer) {
		this.netAPayer = netAPayer;
	}
}
