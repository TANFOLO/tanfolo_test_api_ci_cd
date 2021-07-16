package com.kamtar.transport.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kamtar.transport.api.params.CountryCreateParams;
import com.kamtar.transport.api.params.EtapeParams;
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

@ApiModel(description = "Etape")
@Entity
@Table(name = "etape")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class Etape implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(notes = "Identifiant")
	@Id
	@Type(type="uuid-char")
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected UUID uuid;

	@ApiModelProperty(notes = "Nom du destinataire")
	@Column(updatable = true, name = "adresseDestinataireNom", nullable = true, length = 250, unique = false)
	private String adresseDestinataireNom;

	@ApiModelProperty(notes = "Numéro de téléphone du destinataire")
	@Column(updatable = true, name = "adresseDestinataireTelephone", nullable = true, length = 250, unique = false)
	private String adresseDestinataireTelephone;

	@ApiModelProperty(notes = "Latitude GPS du point de départ")
	@Column(updatable = true, name = "adresseLatitude", nullable = true, unique = false)
	private Double adresseLatitude;

	@ApiModelProperty(notes = "Longitude GPS du point de départ")
	@Column(updatable = true, name = "adresseLongitude", nullable = true, unique = false)
	private Double adresseLongitude;

	@ApiModelProperty(notes = "Adresse du point de départ")
	@Column(updatable = true, name = "adresseComplete", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
	private String adresseComplete;

	@ApiModelProperty(notes = "Complément d'adresse du point de départ")
	@Column(updatable = true, name = "adresseComplement", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
	private String adresseComplement;

	@ApiModelProperty(notes = "Pays du point de départ")
	@Column(updatable = true, name = "adresseCountryCode", nullable = true, length = 200, unique = false)
	private String adresseCountryCode;

	@ApiModelProperty(notes = "Ville du point de départ")
	@Column(updatable = true, name = "adresseVille", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
	private String adresseVille;

	@ApiModelProperty(notes = "Rue du point de départ")
	@Column(updatable = true, name = "adresseRue", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
	private String adresseRue;

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

	@ApiModelProperty(notes = "Code du pays")
	@Column(updatable = true, name = "codePays", nullable = true, unique = false)
	private String codePays;

	@ApiModelProperty(notes = "Position de l'étape dans la liste des étapes")
	@Column(updatable = true, name = "position", nullable = true, unique = false)
	private Integer position = 1;

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getCodePays() {
		return codePays;
	}

	public void setCodePays(String codePays) {
		this.codePays = codePays;
	}

	public String getAdresseDestinataireNom() {
		return adresseDestinataireNom;
	}

	public void setAdresseDestinataireNom(String adresseDestinataireNom) {
		this.adresseDestinataireNom = adresseDestinataireNom;
	}

	public String getAdresseDestinataireTelephone() {
		return adresseDestinataireTelephone;
	}

	public void setAdresseDestinataireTelephone(String adresseDestinataireTelephone) {
		this.adresseDestinataireTelephone = adresseDestinataireTelephone;
	}

	public Double getAdresseLatitude() {
		return adresseLatitude;
	}

	public void setAdresseLatitude(Double adresseLatitude) {
		this.adresseLatitude = adresseLatitude;
	}

	public Double getAdresseLongitude() {
		return adresseLongitude;
	}

	public void setAdresseLongitude(Double adresseLongitude) {
		this.adresseLongitude = adresseLongitude;
	}

	public String getAdresseComplete() {
		return adresseComplete;
	}

	public void setAdresseComplete(String adresseComplete) {
		this.adresseComplete = adresseComplete;
	}

	public String getAdresseComplement() {
		return adresseComplement;
	}

	public void setAdresseComplement(String adresseComplement) {
		this.adresseComplement = adresseComplement;
	}

	public String getAdresseCountryCode() {
		return adresseCountryCode;
	}

	public void setAdresseCountryCode(String adresseCountryCode) {
		this.adresseCountryCode = adresseCountryCode;
	}

	public String getAdresseVille() {
		return adresseVille;
	}

	public void setAdresseVille(String adresseVille) {
		this.adresseVille = adresseVille;
	}

	public String getAdresseRue() {
		return adresseRue;
	}

	public void setAdresseRue(String adresseRue) {
		this.adresseRue = adresseRue;
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

	public Etape(Etape etape, Integer position) {
		super();
		this.adresseDestinataireNom = etape.adresseDestinataireNom;
		this.adresseDestinataireTelephone = etape.adresseDestinataireTelephone;
		this.adresseComplement = etape.adresseComplement;
		this.adresseLatitude = etape.adresseLatitude;
		this.adresseLongitude = etape.adresseLongitude;
		this.adresseComplete = etape.adresseComplete;
		this.adresseComplement = etape.adresseComplement;
		this.adresseCountryCode = etape.adresseCountryCode;
		this.adresseVille = etape.adresseVille;
		this.adresseRue = etape.adresseRue;
		this.codePays = etape.codePays;
		this.position = position;
	}

	public Etape(EtapeParams etape, Integer position) {
		super();
		this.adresseDestinataireNom = etape.getAdresseDestinataireNom();
		this.adresseDestinataireTelephone = etape.getAdresseDestinataireTelephone();
		this.adresseComplement = etape.getAdresseComplement();
		this.adresseLatitude = etape.getAdresseLatitude();
		this.adresseLongitude = etape.getAdresseLongitude();
		this.adresseComplete = etape.getAdresseComplete();
		this.adresseComplement = etape.getAdresseComplement();
		this.adresseCountryCode = etape.getAdresseCountryCode();
		this.adresseVille = etape.getAdresseVille();
		this.adresseRue = etape.getAdresseRue();
		this.codePays = etape.getCodePays();
		this.position = position;
	}
	public Etape() {
	}
}
