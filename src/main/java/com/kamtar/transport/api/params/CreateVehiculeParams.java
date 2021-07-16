package com.kamtar.transport.api.params;

import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.kamtar.transport.api.validation.CountryCodeAlreadyExistConstraint;
import com.kamtar.transport.api.validation.ImmatriculationVehiculeAlreadyExistConstraint;
import com.kamtar.transport.api.validation.UUIDFacultatifConstraint;
import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;

import io.swagger.annotations.ApiModelProperty;


@ImmatriculationVehiculeAlreadyExistConstraint(message = "{err.vehicule.create.immatriculation_deja_enregistree}")
public class CreateVehiculeParams extends ParentParams {
	

	
	@ApiModelProperty(notes = "Immatriculation", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.vehicule.create.immatriculation}")
	@Size(min = 1, max = 250, message = "{err.vehicule.create.immatriculation_longueur}")
	private String immatriculation;
	
	@ApiModelProperty(notes = "Immatriculation", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.vehicule.create.immatriculation_pays}")
	@Size(min = 1, max = 100, message = "{err.vehicule.create.immatriculation_pays_longueur}") 
	private String immatriculationPays;
	
	/*@ApiModelProperty(notes = "Mot de passe du compte", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.vehicule.create.mot_de_passe}")
	@Size(min = 1, max = 250, message = "{err.vehicule.create.mot_de_passe_longueur}") 
	private String motDePasse;*/
	
	@ApiModelProperty(notes = "Proprietaire", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.vehicule.create.proprietaire}")
	@UUIDObligatoireConstraint(message = "{err.vehicule.create.proprietaire_uuid}")
	private String proprietaire;
	
	@ApiModelProperty(notes = "Conducteur principal du véhicule", allowEmptyValue = true, required = false)
	@UUIDFacultatifConstraint(message = "{err.vehicule.create.driver_principal_uuid}")
	private String driverPrincipal;
	
	@ApiModelProperty(notes = "Carrosserie pris parmi l'énumération VehiculeCarrosserie", allowEmptyValue = true, required = false)
	@NotNull(message = "{err.vehicule.create.carrosserie}")
	@Size(min = 0, max = 100, message = "{err.vehicule.create.carrosserie_longueur}") 
	private String carrosserie;
	
	@ApiModelProperty(notes = "Charge utilise (en tonne)", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.vehicule.create.charge}")
	private Float chargeUtileTonne;
	
	@ApiModelProperty(notes = "Volume utilise (en m3)", allowEmptyValue = false, required = true)
	//@NotNull(message = "{err.vehicule.create.volume}")
	private Float volumeVehiculeM3;
	
	@ApiModelProperty(notes = "Observations", allowEmptyValue = true, required = false)
	@Size(min = 0, max = 2500, message = "{err.vehicule.create.observation_longueur}") 
	private String observations;
	
	@ApiModelProperty(notes = "Photo principale du véhicule en base 64", allowEmptyValue = true, required = false)
	protected String photoPrincipale;
	
	@ApiModelProperty(notes = "Document d'assurance", allowEmptyValue = true, required = false)
	protected String documentAssurance;
	
	@ApiModelProperty(notes = "Document carte grise", allowEmptyValue = true, required = false)
	protected String documentCarteGrise;
	
	@ApiModelProperty(notes = "Marque", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.vehicule.create.marque}")
	@Size(min = 1, max = 250, message = "{err.vehicule.create.marque_longueur}")  
	private String marque;
	
	@ApiModelProperty(notes = "Modèle/série", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.vehicule.create.modele_serie}")
	@Size(min = 1, max = 250, message = "{err.vehicule.create.modele_serie_longueur}") 
	private String modeleSerie;
	
	@ApiModelProperty(notes = "Type du véhicule pris dans l'énumération VehiculeType", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.vehicule.create.type}")
	@Size(min = 1, max = 250, message = "{err.vehicule.create.type_longueur}") 
	private String typeVehicule;
	
	@ApiModelProperty(notes = "Longueur de la carrosserie", allowEmptyValue = true, required = false)
	private Float longueurCarrosserie;
	
	@ApiModelProperty(notes = "Largeur de la carrosserie", allowEmptyValue = true, required = false)
	private Float largeurCarrosserie;
	
	@ApiModelProperty(notes = "Hauteur de la carrosserie", allowEmptyValue = true, required = false)
	private Float hauteurCarrosserie;
	
	@ApiModelProperty(notes = "Equipements du véhicule", allowEmptyValue = true, required = false)
	@Size(min = 0, max = 2500, message = "{err.vehicule.create.equipement_longueur}") 
	private String equipement;
	
	@ApiModelProperty(notes = "Usage du véhicule", allowEmptyValue = true, required = false)
	@Size(min = 0, max = 2500, message = "{err.vehicule.create.usage_longueur}") 
	private String usageVehicule;
	
	@ApiModelProperty(notes = "Localisation habituelle du véhicule", allowEmptyValue = true, required = false)
	@Size(min = 0, max = 2500, message = "{err.vehicule.create.localisation_longueur}") 
	private String localisationHabituelleVehicule;

	@ApiModelProperty(notes = "Pays de la localisation habituelle du véhicule", allowEmptyValue = true, required = false)
	private String codePayslocalisationHabituelleVehicule;
	
	@ApiModelProperty(notes = "Nombre d'essieux pour un semi remorque", allowEmptyValue = true, required = false)
	private Integer nbEssieuxSemiRemorque;
	
	@ApiModelProperty(notes = "Nombre d'essieux pour un tracteur ou porteur", allowEmptyValue = true, required = false)
	private Integer nbEssieuxTracteurOuPorteur;
	
	@ApiModelProperty(notes = "Date de validité de la patente", allowEmptyValue = false, required = true)
	//@NotNull(message = "{err.vehicule.create.date_validite_patente}")
	private Date dateValiditePatente;
	
	@ApiModelProperty(notes = "Date de validité de l'assurance", allowEmptyValue = false, required = true)
	//@NotNull(message = "{err.vehicule.create.date_validite_assurance}")
	private Date dateValiditeAssurance;
	
	@ApiModelProperty(notes = "Date de validité de la visite techique", allowEmptyValue = false, required = true)
	//@NotNull(message = "{err.vehicule.create.date_validite_visite_technique}")
	private Date dateValiditeVisiteTechnique;
	
	@ApiModelProperty(notes = "Est ce que le véhicule est activé ?", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.vehicule.create.active}")
    protected boolean activate;
	
	@ApiModelProperty(notes = "UUID temporaire identique à toutes les photos uploadées pour un même véhicule à la création", allowEmptyValue = true, required = false)
	private String folderUuid;


	public String getCodePayslocalisationHabituelleVehicule() {
		return codePayslocalisationHabituelleVehicule;
	}

	public void setCodePayslocalisationHabituelleVehicule(String codePayslocalisationHabituelleVehicule) {
		this.codePayslocalisationHabituelleVehicule = codePayslocalisationHabituelleVehicule;
	}

	public String getPhotoPrincipale() {
		return photoPrincipale;
	}

	public void setPhotoPrincipale(String photoPrincipale) {
		this.photoPrincipale = photoPrincipale;
	}

	public String getFolderUuid() {
		return folderUuid;
	}

	public void setFolderUuid(String folderUuid) {
		this.folderUuid = folderUuid;
	}

	public String getImmatriculation() {
		return immatriculation;
	}

	public void setImmatriculation(String immatriculation) {
		this.immatriculation = immatriculation;
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

	public CreateVehiculeParams() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getImmatriculationPays() {
		return immatriculationPays;
	}

	public void setImmatriculationPays(String immatriculationPays) {
		this.immatriculationPays = immatriculationPays;
	}

	public String getProprietaire() {
		return proprietaire;
	}

	public void setProprietaire(String proprietaire) {
		this.proprietaire = proprietaire;
	}

	public String getDriverPrincipal() {
		return driverPrincipal;
	}

	public void setDriverPrincipal(String driverPrincipal) {
		this.driverPrincipal = driverPrincipal;
	}
	
	
	
	

}
