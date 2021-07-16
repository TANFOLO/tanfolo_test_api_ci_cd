package com.kamtar.transport.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kamtar.transport.api.enums.DevisStatut;
import com.kamtar.transport.api.params.ContactParams;
import com.kamtar.transport.api.params.CreateDevisParams;
import com.kamtar.transport.api.params.CreateOperationParams;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@ApiModel(description = "Devis")
@Entity
@Table(name = "devis", indexes = { @Index(name = "idx_devis_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class Devis implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(notes = "Identifiant")
	@Id
	@Type(type="uuid-char")
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected UUID uuid;

	@ApiModelProperty(notes = "Code du devis")
	@Column(updatable = false, name = "code", nullable = false, unique = true)
	private Long code;

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

	@ApiModelProperty(notes = "Type de compte du client (B = pro, C = particulier)")
	@Column(updatable = true, name = "typeCompte", nullable = false, length=1, unique = false)
	private String typeCompte;

	@ApiModelProperty(notes = "Nom de la société du client")
	@Column(updatable = false, name = "clientNomSociete", nullable = true, length=250, unique = false)
	private String clientNomSociete;

	@ApiModelProperty(notes = "Nom du client")
	@Column(updatable = false, name = "clientNom", nullable = true, length=250, unique = false)
	private String clientNom;

	@ApiModelProperty(notes = "Prénom du client")
	@Column(updatable = false, name = "clientPrenom", nullable = true, length=250, unique = false)
	private String clientPrenom;

	@ApiModelProperty(notes = "Code du pays du numéro de téléphone")
	@Column(name = "clientTelephonePays", nullable = true, updatable = true, columnDefinition = "varchar(10) default 'CI'")
	protected String clientTelephonePays;

	@ApiModelProperty(notes = "Numéro de téléphone du client")
	@Column(updatable = false, name = "clientTelephone", nullable = true, length=250, unique = false)
	private String clientTelephone;

	@ApiModelProperty(notes = "Adresse email du client")
	@Column(updatable = false, name = "clientEmail", nullable = true, length=250, unique = false)
	private String clientEmail;

	@ApiModelProperty(notes = "Code du pays")
	@Column(updatable = false, name = "codePays", nullable = true, unique = false)
	private String codePays;

	@ApiModelProperty(notes = "Statut")
	@Column(updatable = true, name = "statut", columnDefinition = "TEXT", nullable = true, length = 2500, unique = false)
	private String statut;



	@ApiModelProperty(notes = "Latitude GPS du point de départ")
	@Column(updatable = true, name = "departAdresseLatitude", nullable = true, unique = false)
	private Double departAdresseLatitude;

	@ApiModelProperty(notes = "Longitude GPS du point de départ")
	@Column(updatable = true, name = "departAdresseLongitude", nullable = true, unique = false)
	private Double departAdresseLongitude;

	@ApiModelProperty(notes = "Adresse du point de départ")
	@Column(updatable = true, name = "departAdresseComplete", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
	private String departAdresseComplete;

	@ApiModelProperty(notes = "Complément d'adresse du point de départ")
	@Column(updatable = true, name = "departAdresseComplement", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
	private String departAdresseComplement;

	@ApiModelProperty(notes = "Pays du point de départ")
	@Column(updatable = true, name = "departAdresseCountryCode", nullable = true, length = 200, unique = false)
	private String departAdresseCountryCode;

	@ApiModelProperty(notes = "Ville du point de départ")
	@Column(updatable = true, name = "departAdresseVille", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
	private String departAdresseVille;

	@ApiModelProperty(notes = "Rue du point de départ")
	@Column(updatable = true, name = "departAdresseRue", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
	private String departAdresseRue;

	@ApiModelProperty(notes = "Nom du destinataire")
	@Column(updatable = true, name = "arriveeDestinataireNom", nullable = true, length = 250, unique = false)
	private String arriveeDestinataireNom;

	@ApiModelProperty(notes = "Numéro de téléphone du destinataire")
	@Column(updatable = true, name = "arriveeDestinataireTelephone", nullable = true, length = 250, unique = false)
	private String arriveeDestinataireTelephone;

	@ApiModelProperty(notes = "Date programmée pour le départ de la marchandise ")
	@Column(updatable = true, name = "DepartDateProgrammeeOperation", nullable = true, unique = false)
	private Date departDateProgrammeeOperation;

	@ApiModelProperty(notes = "Latitude GPS du point d'arrivée")
	@Column(updatable = true, name = "arriveeAdresseLatitude", nullable = true, unique = false)
	private Double arriveeAdresseLatitude;

	@ApiModelProperty(notes = "Longitude GPS du point d'arrivée")
	@Column(updatable = true, name = "arriveeAdresseLongitude", nullable = true, unique = false)
	private Double arriveeAdresseLongitude;

	@ApiModelProperty(notes = "Adresse du point d'arrivée")
	@Column(updatable = true, name = "arriveeAdresseComplete", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
	private String arriveeAdresseComplete;

	@ApiModelProperty(notes = "Complément d'adresse du point d'arrivée")
	@Column(updatable = true, name = "arriveeAdresseComplement", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
	private String arriveeAdresseComplement;

	@ApiModelProperty(notes = "Pays du point d'arrivée")
	@Column(updatable = true, name = "arriveeAdresseCountryCode", nullable = true, length = 200, unique = false)
	private String arriveeAdresseCountryCode;

	@ApiModelProperty(notes = "Ville du point d'arrivée")
	@Column(updatable = true, name = "arriveeAdresseVille", nullable = true, length = 255, unique = false)
	private String arriveeAdresseVille;

	@ApiModelProperty(notes = "Rue du point d'arrivée")
	@Column(updatable = true, name = "arriveeAdresseRue", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
	private String arriveeAdresseRue;

	@ApiModelProperty(notes = "Catégorie de véhicule sélectionnée par le client")
	@Column(updatable = true, name = "categorieVehicule", nullable = true, unique = false)
	private String categorieVehicule;

	@ApiModelProperty(notes = "Type de marchandises saisies par le client")
	@OneToOne
	@JoinColumn(name = "typeMarchandise", foreignKey = @ForeignKey(name = "fk_operation_type_marchandise"))
	private MarchandiseType typeMarchandise;

	@ApiModelProperty(notes = "Tonnage ud véhicule saisies par le client")
	@OneToOne
	@JoinColumn(name = "tonnageVehicule", foreignKey = @ForeignKey(name = "fk_operation_vehicule_tonnage"))
	private VehiculeTonnage tonnageVehicule;

	@ApiModelProperty(notes = "Observations saisies par le client")
	@Column(updatable = true, name = "observationsParClient", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
	private String observationsParClient;

	@ApiModelProperty(notes = "Prix souhaitée par le client")
	@Column(updatable = true, name = "prixSouhaiteParClient", nullable = true, unique = false)
	private Double prixSouhaiteParClient;

	@ApiModelProperty(notes = "Devise du prix souhaité par le client", allowEmptyValue = true, required = false)
	@Column(updatable = true, name = "prixSouhaiteParClientDevise", nullable = true, length = 20, unique = false)
	private String prixSouhaiteParClientDevise;

	@ApiModelProperty(notes = "Liste des étapes")
	@OneToMany(
			targetEntity=Etape.class,
			cascade = CascadeType.DETACH
	)
	@ElementCollection
	@OrderBy("position ASC")
	private List<Etape> etapes;

	public String getTypeCompte() {
		return typeCompte;
	}

	public void setTypeCompte(String typeCompte) {
		this.typeCompte = typeCompte;
	}

	public Double getPrixSouhaiteParClient() {
		return prixSouhaiteParClient;
	}

	public void setPrixSouhaiteParClient(Double prixSouhaiteParClient) {
		this.prixSouhaiteParClient = prixSouhaiteParClient;
	}

	public String getPrixSouhaiteParClientDevise() {
		return prixSouhaiteParClientDevise;
	}

	public void setPrixSouhaiteParClientDevise(String prixSouhaiteParClientDevise) {
		this.prixSouhaiteParClientDevise = prixSouhaiteParClientDevise;
	}

	public List<Etape> getEtapes() {
		return etapes;
	}

	public void setEtapes(List<Etape> etapes) {
		this.etapes = etapes;
	}

	public String getStatut() {
		return statut;
	}

	public void setStatut(String statut) {
		this.statut = statut;
	}

	public Double getDepartAdresseLatitude() {
		return departAdresseLatitude;
	}

	public void setDepartAdresseLatitude(Double departAdresseLatitude) {
		this.departAdresseLatitude = departAdresseLatitude;
	}

	public Double getDepartAdresseLongitude() {
		return departAdresseLongitude;
	}

	public void setDepartAdresseLongitude(Double departAdresseLongitude) {
		this.departAdresseLongitude = departAdresseLongitude;
	}

	public String getDepartAdresseComplete() {
		return departAdresseComplete;
	}

	public void setDepartAdresseComplete(String departAdresseComplete) {
		this.departAdresseComplete = departAdresseComplete;
	}

	public String getDepartAdresseComplement() {
		return departAdresseComplement;
	}

	public void setDepartAdresseComplement(String departAdresseComplement) {
		this.departAdresseComplement = departAdresseComplement;
	}

	public String getDepartAdresseCountryCode() {
		return departAdresseCountryCode;
	}

	public void setDepartAdresseCountryCode(String departAdresseCountryCode) {
		this.departAdresseCountryCode = departAdresseCountryCode;
	}

	public String getDepartAdresseVille() {
		return departAdresseVille;
	}

	public void setDepartAdresseVille(String departAdresseVille) {
		this.departAdresseVille = departAdresseVille;
	}

	public String getDepartAdresseRue() {
		return departAdresseRue;
	}

	public void setDepartAdresseRue(String departAdresseRue) {
		this.departAdresseRue = departAdresseRue;
	}

	public String getArriveeDestinataireNom() {
		return arriveeDestinataireNom;
	}

	public void setArriveeDestinataireNom(String arriveeDestinataireNom) {
		this.arriveeDestinataireNom = arriveeDestinataireNom;
	}

	public String getArriveeDestinataireTelephone() {
		return arriveeDestinataireTelephone;
	}

	public void setArriveeDestinataireTelephone(String arriveeDestinataireTelephone) {
		this.arriveeDestinataireTelephone = arriveeDestinataireTelephone;
	}

	public Double getArriveeAdresseLatitude() {
		return arriveeAdresseLatitude;
	}

	public void setArriveeAdresseLatitude(Double arriveeAdresseLatitude) {
		this.arriveeAdresseLatitude = arriveeAdresseLatitude;
	}

	public Double getArriveeAdresseLongitude() {
		return arriveeAdresseLongitude;
	}

	public void setArriveeAdresseLongitude(Double arriveeAdresseLongitude) {
		this.arriveeAdresseLongitude = arriveeAdresseLongitude;
	}

	public String getArriveeAdresseComplete() {
		return arriveeAdresseComplete;
	}

	public void setArriveeAdresseComplete(String arriveeAdresseComplete) {
		this.arriveeAdresseComplete = arriveeAdresseComplete;
	}

	public String getArriveeAdresseComplement() {
		return arriveeAdresseComplement;
	}

	public void setArriveeAdresseComplement(String arriveeAdresseComplement) {
		this.arriveeAdresseComplement = arriveeAdresseComplement;
	}

	public String getClientNomSociete() {
		return clientNomSociete;
	}

	public void setClientNomSociete(String clientNomSociete) {
		this.clientNomSociete = clientNomSociete;
	}

	public String getClientTelephonePays() {
		return clientTelephonePays;
	}

	public void setClientTelephonePays(String clientTelephonePays) {
		this.clientTelephonePays = clientTelephonePays;
	}

	public Date getDepartDateProgrammeeOperation() {
		return departDateProgrammeeOperation;
	}

	public void setDepartDateProgrammeeOperation(Date departDateProgrammeeOperation) {
		this.departDateProgrammeeOperation = departDateProgrammeeOperation;
	}

	public String getArriveeAdresseCountryCode() {
		return arriveeAdresseCountryCode;
	}

	public void setArriveeAdresseCountryCode(String arriveeAdresseCountryCode) {
		this.arriveeAdresseCountryCode = arriveeAdresseCountryCode;
	}

	public String getArriveeAdresseVille() {
		return arriveeAdresseVille;
	}

	public void setArriveeAdresseVille(String arriveeAdresseVille) {
		this.arriveeAdresseVille = arriveeAdresseVille;
	}

	public String getArriveeAdresseRue() {
		return arriveeAdresseRue;
	}

	public void setArriveeAdresseRue(String arriveeAdresseRue) {
		this.arriveeAdresseRue = arriveeAdresseRue;
	}

	public String getCategorieVehicule() {
		return categorieVehicule;
	}

	public void setCategorieVehicule(String categorieVehicule) {
		this.categorieVehicule = categorieVehicule;
	}

	public MarchandiseType getTypeMarchandise() {
		return typeMarchandise;
	}

	public void setTypeMarchandise(MarchandiseType typeMarchandise) {
		this.typeMarchandise = typeMarchandise;
	}

	public VehiculeTonnage getTonnageVehicule() {
		return tonnageVehicule;
	}

	public void setTonnageVehicule(VehiculeTonnage tonnageVehicule) {
		this.tonnageVehicule = tonnageVehicule;
	}

	public String getObservationsParClient() {
		return observationsParClient;
	}

	public void setObservationsParClient(String observationsParClient) {
		this.observationsParClient = observationsParClient;
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

	public String getClientNom() {
		return clientNom;
	}

	public void setClientNom(String clientNom) {
		this.clientNom = clientNom;
	}

	public String getClientTelephone() {
		return clientTelephone;
	}

	public void setClientTelephone(String clientTelephone) {
		this.clientTelephone = clientTelephone;
	}

	public String getClientEmail() {
		return clientEmail;
	}

	public void setClientEmail(String clientEmail) {
		this.clientEmail = clientEmail;
	}

	public String getCodePays() {
		return codePays;
	}

	public void setCodePays(String codePays) {
		this.codePays = codePays;
	}

	public Devis() {
	}

	public Long getCode() {
		return code;
	}

	public void setCode(Long code) {
		this.code = code;
	}

	public String getClientPrenom() {
		return clientPrenom;
	}

	public void setClientPrenom(String clientPrenom) {
		this.clientPrenom = clientPrenom;
	}

	public Devis(CreateDevisParams params, Long code, MarchandiseType marchandise_type, VehiculeTonnage tonnage_obj, String code_pays) {
		super();
		this.arriveeAdresseComplement = params.getArriveeAdresseComplement();
		this.arriveeAdresseComplete = params.getArriveeAdresseComplete();
		this.arriveeAdresseCountryCode = params.getArriveeAdresseCountryCode();
		this.arriveeAdresseLatitude = params.getArriveeAdresseLatitude();
		this.arriveeAdresseLongitude = params.getArriveeAdresseLongitude();
		this.arriveeAdresseRue = params.getArriveeAdresseRue();
		this.arriveeAdresseVille = params.getArriveeAdresseVille();
		this.tonnageVehicule = tonnage_obj;
		this.categorieVehicule = params.getCarrosserieVehicule();

		this.code = code;

		this.createdOn = new Date();

		this.departAdresseComplement = params.getDepartAdresseComplement();
		this.departAdresseComplete = params.getDepartAdresseComplete();
		this.departAdresseCountryCode = params.getDepartAdresseCountryCode();
		this.departAdresseLatitude = params.getDepartAdresseLatitude();
		this.departAdresseLongitude = params.getDepartAdresseLongitude();
		this.departAdresseRue = params.getDepartAdresseRue();
		this.departAdresseVille = params.getDepartAdresseVille();

		this.observationsParClient = params.getObservationsParClient();

		this.prixSouhaiteParClient = params.getPrixSouhaiteParClient();
		this.prixSouhaiteParClientDevise = params.getPrixSouhaiteParClientDevise();

		this.statut = DevisStatut.ENREGISTRE.toString();

		this.typeMarchandise = marchandise_type;
		this.updatedOn = new Date();

		this.arriveeDestinataireNom = params.getArriveeDestinataireNom();
		this.arriveeDestinataireTelephone = params.getArriveeDestinataireTelephone();

		this.etapes = new ArrayList<Etape>();
		int cpt=1;
		for (EtapeParams etape : params.getEtapes()) {
			this.etapes.add(new Etape(etape, cpt));
			cpt++;
		}

		this.codePays = code_pays;
		this.typeCompte = params.getTypeCompte();

		this.clientEmail = params.getEmail();
		this.clientNom = params.getNom();
		this.clientPrenom = params.getPrenom();
		this.clientNomSociete = params.getNomSociete();
		this.clientTelephonePays = params.getPays_telephone_1();
		this.clientTelephone = params.getTelephone1();

		this.departDateProgrammeeOperation = params.getDepartDateProgrammeeOperation();

	}

	public String getDepartDateProgrammeeOperationFormatted() {
		if (departDateProgrammeeOperation == null) {
			return "-";
		}
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		return dateFormat.format(departDateProgrammeeOperation);
	}

}
