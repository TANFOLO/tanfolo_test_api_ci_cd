package com.kamtar.transport.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.*;
import javax.validation.Valid;

import io.gsonfire.annotations.ExposeMethodResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kamtar.transport.api.controller.UtilisateurProprietaireController;
import com.kamtar.transport.api.controller.VehiculeController;
import com.kamtar.transport.api.params.CountryCreateParams;
import com.kamtar.transport.api.params.CreateComptePublicParams;
import com.kamtar.transport.api.params.CreateDriverPublicParams;
import com.kamtar.transport.api.params.CreateProprietairePublicParams;
import com.kamtar.transport.api.params.CreateVehiculeParams;
import com.kamtar.transport.api.params.CreateVehiculePublicParams;
import com.kamtar.transport.api.params.EditVehiculeParams;
import com.kamtar.transport.api.utils.UpdatableBCrypt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Vehicule")
@Entity
@Table(name = "vehicule", indexes = { @Index(name = "idx_vehicule_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class Vehicule implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(Vehicule.class);


	@ApiModelProperty(notes = "Identifiant")
	@Id
	@Type(type="uuid-char")
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected UUID uuid;

	@ApiModelProperty(notes = "Immatriculation")
	@Column(updatable = true, name = "immatriculation", nullable = false, length=250, unique = false)
	private String immatriculation;
	
	@ApiModelProperty(notes = "Pays d'immatriculation")	
	@OneToOne
	@JoinColumn(name = "immatriculationPays", foreignKey = @ForeignKey(name = "fk_vehicule_immatriculationPays"))
	private Country immatriculationPays;
	
	/*@ApiModelProperty(notes = "Mot de passe du compte")
	@Column(updatable = true, name = "motDePasse", nullable = false, length=250, unique = false)	
	private String motDePasse;*/
	
	@ApiModelProperty(notes = "Propriétaire du véhicule")	
	@OneToOne
	@JoinColumn(name = "proprietaire", foreignKey = @ForeignKey(name = "fk_vehicule_proprietaire"))
	private UtilisateurProprietaire proprietaire;
	
	@ApiModelProperty(notes = "Conducteur principal du véhicule")	
	@OneToOne
	@JoinColumn(name = "driverPrincipal", foreignKey = @ForeignKey(name = "fk_vehicule_driverPrincipal"))
	private UtilisateurDriver driverPrincipal;
	
	@ApiModelProperty(notes = "Carrosserie pris parmi l'énumération VehiculeCarrosserie")
	@Column(updatable = true, name = "carrosserie", nullable = true, length=250, unique = false)	
	private String carrosserie;
	
	@ApiModelProperty(notes = "Charge utilise (en tonne)")
	@Column(updatable = true, name = "chargeUtileTonne", nullable = false, unique = false)	
	private Float chargeUtileTonne;
	
	@ApiModelProperty(notes = "Volume utilise (en m3)")
	@Column(updatable = true, name = "volumeVehiculeM3", nullable = true, unique = false)
	private Float volumeVehiculeM3;
	
	@ApiModelProperty(notes = "Observations")
	@Column(updatable = true, name = "observations", nullable = true, length=2500, unique = false)	
	private String observations;
	
	@ApiModelProperty(notes = "Document d'assurance")
	@Column(name = "documentAssurance", nullable = true, updatable = true)
	protected String documentAssurance;
	
	@ApiModelProperty(notes = "Document carte grise")
	@Column(name = "documentCarteGrise", nullable = true, updatable = true)
	protected String documentCarteGrise;
	
	@ApiModelProperty(notes = "Photo principale du véhicule")
	@Column(name = "photoPrincipale", nullable = true, updatable = true)
	protected String photoPrincipale;
	
	@ApiModelProperty(notes = "Marque")
	@Column(updatable = true, name = "marque", nullable = false, length=250, unique = false)	
	private String marque;
	
	@ApiModelProperty(notes = "Modèle/série")
	@Column(updatable = true, name = "modeleSerie", nullable = false, length=250, unique = false)	
	private String modeleSerie;
	
	@ApiModelProperty(notes = "Type du véhicule pris dans l'énumération VehiculeType")
	@Column(updatable = true, name = "typeVehicule", nullable = false, length=250, unique = false)	
	private String typeVehicule;
	
	@ApiModelProperty(notes = "Longueur de la carrosserie")
	@Column(updatable = true, name = "longueurCarrosserie", nullable = true, unique = false)	
	private Float longueurCarrosserie;
	
	@ApiModelProperty(notes = "Largeur de la carrosserie")
	@Column(updatable = true, name = "largeurCarrosserie", nullable = true, unique = false)	
	private Float largeurCarrosserie;
	
	@ApiModelProperty(notes = "Hauteur de la carrosserie")
	@Column(updatable = true, name = "hauteurCarrosserie", nullable = true, unique = false)	
	private Float hauteurCarrosserie;
	
	@ApiModelProperty(notes = "Equipements du véhicule")
	@Column(updatable = true, name = "equipement", nullable = true, length=2500, unique = false)	
	private String equipement;
	
	@ApiModelProperty(notes = "Usage du véhicule")
	@Column(updatable = true, name = "usageVehicule", nullable = true, length=2500, unique = false)	
	private String usageVehicule;
	
	@ApiModelProperty(notes = "Localisation habituelle du véhicule")
	@Column(updatable = true, name = "localisationHabituelleVehicule", nullable = true, length=2500, unique = false)	
	private String localisationHabituelleVehicule;

	@ApiModelProperty(notes = "Code pays de la localisation habituelle du véhicule")
	@Column(updatable = true, name = "codePayslocalisationHabituelleVehicule", nullable = true, length=25, unique = false)
	private String codePayslocalisationHabituelleVehicule;
	
	@ApiModelProperty(notes = "Nombre d'essieux pour un semi remorque")
	@Column(updatable = true, name = "nbEssieuxSemiRemorque", nullable = true, unique = false)	
	private Integer nbEssieuxSemiRemorque;
	
	@ApiModelProperty(notes = "Nombre d'essieux pour un tracteur ou porteur")
	@Column(updatable = true, name = "nbEssieuxTracteurOuPorteur", nullable = true, unique = false)	
	private Integer nbEssieuxTracteurOuPorteur;
	
	@ApiModelProperty(notes = "Date de validité de la patente")
	@Column(updatable = true, name = "dateValiditePatente", nullable = true, unique = false)
	@Basic
	@Temporal(TemporalType.DATE)
	private Date dateValiditePatente;
	
	@ApiModelProperty(notes = "Date de validité de l'assurance")
	@Column(updatable = true, name = "dateValiditeAssurance", nullable = true, unique = false)
	@Basic
	@Temporal(TemporalType.DATE)
	private Date dateValiditeAssurance;
	
	@ApiModelProperty(notes = "Date de validité de la visite techique")
	@Column(updatable = true, name = "dateValiditeVisiteTechnique", nullable = true, unique = false)
	@Basic
	@Temporal(TemporalType.DATE)
	private Date dateValiditeVisiteTechnique;
	
	@ApiModelProperty(notes = "Est ce que le véhicule est activé ?")
    @Column(name = "activate", nullable = false, updatable = true)
    protected boolean activate;
	
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
	@Column(updatable = false, name = "codePays", nullable = true, unique = false)
	private String codePays;

	@ApiModelProperty(notes = "Est ce que le véhicule est indiqué comme disponible par le propriétaire ? 0 = non, 1 = oui")
	@Column(updatable = true, name = "disponible", nullable = true, unique = false, columnDefinition = "integer default 0")
	private Integer disponible = 0;

	/**
	 * Utiliser pour y injecter la catégorie textuelle lors des exports Excel
	 */
	private String carrosserieTextuel;

	/**
	 * Utiliser pour y injecter le type de véhicul textuelle lors des exports Excel
	 */

	private String typeTextuel;

	public Integer getDisponible() {
		return disponible;
	}

	public void setDisponible(Integer disponible) {
		this.disponible = disponible;
	}

	public String getCarrosserieTextuel() {
		return carrosserieTextuel;
	}

	public String getTypeTextuel() {
		return typeTextuel;
	}

	public void setTypeTextuel(String typeTextuel) {
		this.typeTextuel = typeTextuel;
	}

	public void setCarrosserieTextuel(String carrosserieTextuel) {
		this.carrosserieTextuel = carrosserieTextuel;
	}

	public String getCodePays() {
		return codePays;
	}

	public void setCodePays(String codePays) {
		this.codePays = codePays;
	}


	/**
	 * Appel d'offre envoyé pour une opérartion précise
	 */
	@Transient
	private OperationAppelOffre appelOffre;

	public String getCodePayslocalisationHabituelleVehicule() {
		return codePayslocalisationHabituelleVehicule;
	}

	public void setCodePayslocalisationHabituelleVehicule(String codePayslocalisationHabituelleVehicule) {
		this.codePayslocalisationHabituelleVehicule = codePayslocalisationHabituelleVehicule;
	}

	public OperationAppelOffre getAppelOffre() {
		return appelOffre;
	}

	public void setAppelOffre(OperationAppelOffre appelOffre) {
		this.appelOffre = appelOffre;
	}

	public String getPhotoPrincipale() {
		return photoPrincipale;
	}

	public void setPhotoPrincipale(String photoPrincipale) {
		this.photoPrincipale = photoPrincipale;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getImmatriculation() {
		return immatriculation;
	}

	public void setImmatriculation(String immatriculation) {
		this.immatriculation = immatriculation;
	}

	public Country getImmatriculationPays() {
		return immatriculationPays;
	}

	public void setImmatriculationPays(Country immatriculationPays) {
		this.immatriculationPays = immatriculationPays;
	}

	public UtilisateurProprietaire getProprietaire() {
		return proprietaire;
	}

	public void setProprietaire(UtilisateurProprietaire proprietaire) {
		this.proprietaire = proprietaire;
	}

	public UtilisateurDriver getDriverPrincipal() {
		return driverPrincipal;
	}

	public void setDriverPrincipal(UtilisateurDriver driverPrincipal) {
		this.driverPrincipal = driverPrincipal;
	}

	public String getCarrosserie() {
		return carrosserie;
	}



	public void setCarrosserie(String carrosserie) {
		this.carrosserie = carrosserie;
	}

	public Float getChargeUtileTonne() {
		return chargeUtileTonne;
	}

	public void setChargeUtileTonne(Float chargeUtileTonne) {
		this.chargeUtileTonne = chargeUtileTonne;
	}

	public Float getVolumeVehiculeM3() {
		return volumeVehiculeM3;
	}

	public void setVolumeVehiculeM3(Float volumeVehiculeM3) {
		this.volumeVehiculeM3 = volumeVehiculeM3;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public String getDocumentAssurance() {
		return documentAssurance;
	}

	public void setDocumentAssurance(String documentAssurance) {
		this.documentAssurance = documentAssurance;
	}

	public String getDocumentCarteGrise() {
		return documentCarteGrise;
	}

	public void setDocumentCarteGrise(String documentCarteGrise) {
		this.documentCarteGrise = documentCarteGrise;
	}

	public String getMarque() {
		return marque;
	}

	public void setMarque(String marque) {
		this.marque = marque;
	}

	public String getModeleSerie() {
		return modeleSerie;
	}

	public void setModeleSerie(String modeleSerie) {
		this.modeleSerie = modeleSerie;
	}

	public String getTypeVehicule() {
		return typeVehicule;
	}

	public void setTypeVehicule(String typeVehicule) {
		this.typeVehicule = typeVehicule;
	}

	public Float getLongueurCarrosserie() {
		return longueurCarrosserie;
	}

	public void setLongueurCarrosserie(Float longueurCarrosserie) {
		this.longueurCarrosserie = longueurCarrosserie;
	}

	public Float getLargeurCarrosserie() {
		return largeurCarrosserie;
	}

	public void setLargeurCarrosserie(Float largeurCarrosserie) {
		this.largeurCarrosserie = largeurCarrosserie;
	}

	public Float getHauteurCarrosserie() {
		return hauteurCarrosserie;
	}

	public void setHauteurCarrosserie(Float hauteurCarrosserie) {
		this.hauteurCarrosserie = hauteurCarrosserie;
	}

	public String getEquipement() {
		return equipement;
	}

	public void setEquipement(String equipement) {
		this.equipement = equipement;
	}

	public String getUsageVehicule() {
		return usageVehicule;
	}

	public void setUsageVehicule(String usageVehicule) {
		this.usageVehicule = usageVehicule;
	}

	public String getLocalisationHabituelleVehicule() {
		return localisationHabituelleVehicule;
	}

	public void setLocalisationHabituelleVehicule(String localisationHabituelleVehicule) {
		this.localisationHabituelleVehicule = localisationHabituelleVehicule;
	}

	public Integer getNbEssieuxSemiRemorque() {
		return nbEssieuxSemiRemorque;
	}

	public void setNbEssieuxSemiRemorque(Integer nbEssieuxSemiRemorque) {
		this.nbEssieuxSemiRemorque = nbEssieuxSemiRemorque;
	}

	public Integer getNbEssieuxTracteurOuPorteur() {
		return nbEssieuxTracteurOuPorteur;
	}

	public void setNbEssieuxTracteurOuPorteur(Integer nbEssieuxTracteurOuPorteur) {
		this.nbEssieuxTracteurOuPorteur = nbEssieuxTracteurOuPorteur;
	}

	public Date getDateValiditePatente() {
		return dateValiditePatente;
	}

	public void setDateValiditePatente(Date dateValiditePatente) {
		this.dateValiditePatente = dateValiditePatente;
	}

	public Date getDateValiditeAssurance() {
		return dateValiditeAssurance;
	}

	public void setDateValiditeAssurance(Date dateValiditeAssurance) {
		this.dateValiditeAssurance = dateValiditeAssurance;
	}

	public Date getDateValiditeVisiteTechnique() {
		return dateValiditeVisiteTechnique;
	}

	public void setDateValiditeVisiteTechnique(Date dateValiditeVisiteTechnique) {
		this.dateValiditeVisiteTechnique = dateValiditeVisiteTechnique;
	}

	public boolean isActivate() {
		return activate;
	}

	public void setActivate(boolean activate) {
		this.activate = activate;
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

	public Vehicule() {
		super();
	}
	
	public Vehicule(CreateComptePublicParams params, UtilisateurProprietaire proprietaire, UtilisateurDriver driver, Country immatriculation_pays) {
		super();
		this.activate = false;
		this.carrosserie = params.getCarrosserie();
		this.chargeUtileTonne = params.getChargeUtileTonne();
		this.dateValiditePatente = params.getDateValiditePatente();
		this.dateValiditeVisiteTechnique = params.getDateValiditeVisiteTechnique();
		this.dateValiditeAssurance = params.getDateValiditeAssurance();
		this.driverPrincipal = driver;
		this.equipement = params.getEquipement();
		this.hauteurCarrosserie = params.getHauteurCarrosserie();
		this.immatriculation = params.getImmatriculation();
		this.immatriculationPays = immatriculation_pays;
		this.largeurCarrosserie = params.getLargeurCarrosserie();
		this.localisationHabituelleVehicule = params.getLocalisationHabituelleVehicule();
		this.longueurCarrosserie = params.getLongueurCarrosserie();
		this.marque = params.getMarque();
		this.modeleSerie = params.getModeleSerie();
		this.nbEssieuxSemiRemorque = params.getNbEssieuxSemiRemorque();
		this.nbEssieuxTracteurOuPorteur = params.getNbEssieuxTracteurOuPorteur();
		this.observations = params.getObservations();
		this.proprietaire = proprietaire;
		this.typeVehicule = params.getTypeVehicule();
		this.usageVehicule = params.getUsageVehicule();
		this.volumeVehiculeM3 = params.getVolumeVehiculeM3();
		this.codePays = params.getPays();
	}
	
	public Vehicule(CreateVehiculeParams params, UtilisateurOperateurKamtar operateur, UtilisateurProprietaire proprietaire, UtilisateurDriver driver, Country immatriculation_pays) {
		super();
		this.activate = params.isActivate();
		this.carrosserie = params.getCarrosserie();
		this.chargeUtileTonne = params.getChargeUtileTonne();
		this.dateValiditePatente = params.getDateValiditePatente();
		this.dateValiditeVisiteTechnique = params.getDateValiditeVisiteTechnique();
		this.dateValiditeAssurance = params.getDateValiditeAssurance();
		this.driverPrincipal = driver;
		this.equipement = params.getEquipement();
		this.hauteurCarrosserie = params.getHauteurCarrosserie();
		this.immatriculation = params.getImmatriculation();
		this.immatriculationPays = immatriculation_pays;
		this.largeurCarrosserie = params.getLargeurCarrosserie();
		this.localisationHabituelleVehicule = params.getLocalisationHabituelleVehicule();
		this.codePayslocalisationHabituelleVehicule = params.getCodePayslocalisationHabituelleVehicule();
		this.longueurCarrosserie = params.getLongueurCarrosserie();
		this.marque = params.getMarque();
		this.modeleSerie = params.getModeleSerie();

		this.nbEssieuxSemiRemorque = params.getNbEssieuxSemiRemorque();
		this.nbEssieuxTracteurOuPorteur = params.getNbEssieuxTracteurOuPorteur();
		this.observations = params.getObservations();
		this.proprietaire = proprietaire;
		this.typeVehicule = params.getTypeVehicule();
		this.usageVehicule = params.getUsageVehicule();
		this.volumeVehiculeM3 = params.getVolumeVehiculeM3();
		this.codePays = proprietaire.getCodePays();
	}
	
	
	public void edit(EditVehiculeParams params, UtilisateurProprietaire proprietaire, UtilisateurDriver driver, Country immatriculation_pays) {
		this.activate = params.isActivate();
		this.carrosserie = params.getCarrosserie();
		this.chargeUtileTonne = params.getChargeUtileTonne();
		this.dateValiditePatente = params.getDateValiditePatente();
		this.dateValiditeVisiteTechnique = params.getDateValiditeVisiteTechnique();
		this.dateValiditeAssurance = params.getDateValiditeAssurance();
		this.driverPrincipal = driver;
		this.equipement = params.getEquipement();
		this.hauteurCarrosserie = params.getHauteurCarrosserie();
		this.immatriculation = params.getImmatriculation();
		this.immatriculationPays = immatriculation_pays;
		this.largeurCarrosserie = params.getLargeurCarrosserie();
		this.localisationHabituelleVehicule = params.getLocalisationHabituelleVehicule();
		this.codePayslocalisationHabituelleVehicule = params.getCodePayslocalisationHabituelleVehicule();
		this.longueurCarrosserie = params.getLongueurCarrosserie();
		this.marque = params.getMarque();
		this.modeleSerie = params.getModeleSerie();
		this.nbEssieuxSemiRemorque = params.getNbEssieuxSemiRemorque();
		this.nbEssieuxTracteurOuPorteur = params.getNbEssieuxTracteurOuPorteur();
		this.observations = params.getObservations();
		this.proprietaire = proprietaire;
		this.typeVehicule = params.getTypeVehicule();
		this.usageVehicule = params.getUsageVehicule();
		this.volumeVehiculeM3 = params.getVolumeVehiculeM3();
	}
	
	public String getDetails() {
		return this.immatriculation + " - " + this.marque + " " + this.modeleSerie;
	}

	@Override
	public String toString() {
		return "Vehicule{" +
				"immatriculation='" + immatriculation + '\'' +
				", immatriculationPays=" + immatriculationPays +
				'}';
	}

	/**
	 * Two users are equal if their firstName, lastName and email address is same.
	 */
	@Override
	public boolean equals(Object obj) {
		return (this.uuid.equals(((Vehicule) obj).uuid));
	}

	public String getCouleur() {
		if (this.getProprietaire() == null ||
				this.getDriverPrincipal() == null ||
				this.getDocumentAssurance() == null || "".equals(this.getDocumentAssurance()) ||
				this.getDocumentCarteGrise() == null || "".equals(this.getDocumentCarteGrise()) ||
				this.getPhotoPrincipale() == null || "".equals(this.getPhotoPrincipale()) ||
				this.getLargeurCarrosserie() == null || "".equals(this.getLargeurCarrosserie()) ||
				this.getLongueurCarrosserie() == null || "".equals(this.getLongueurCarrosserie()) ||
				this.getHauteurCarrosserie() == null || "".equals(this.getHauteurCarrosserie())) {
			return "O";
		} else if (this.getDateValiditeAssurance() != null && this.getDateValiditeAssurance() .before(new Date())) {
			return "R";
		} else if (this.getDateValiditePatente() != null && this.getDateValiditePatente() .before(new Date())) {
			return "R";
		} else if (this.getDateValiditeVisiteTechnique() != null && this.getDateValiditeVisiteTechnique() .before(new Date())) {
			return "R";
		}
		return "V";
	}



}
