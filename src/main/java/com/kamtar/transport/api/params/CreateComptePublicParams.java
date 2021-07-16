package com.kamtar.transport.api.params;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.kamtar.transport.api.validation.*;

import io.swagger.annotations.ApiModelProperty;


@ImmatriculationVehiculeAlreadyExistConstraint(message = "{err.vehicule.create.immatriculation_deja_enregistree}")
@NumeroDeTelephoneValidConstraint(message = "{err.user.create.numero_telephone.invalide}")
public class CreateComptePublicParams extends ParentParams {
	

	// véhicule
	@ApiModelProperty(notes = "Immatriculation", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.vehicule.create.immatriculation}")
	@Size(min = 1, max = 250, message = "{err.vehicule.create.immatriculation_longueur}")
	private String immatriculation;
	
	@ApiModelProperty(notes = "Immatriculation", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.vehicule.create.immatriculation_pays}")
	@Size(min = 1, max = 100, message = "{err.vehicule.create.immatriculation_pays_longueur}") 
	private String immatriculationPays;
	
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
	//@NotNull(message = "{err.vehicule.create.type}")
	//@Size(min = 1, max = 250, message = "{err.vehicule.create.type_longueur}")
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
	
	@ApiModelProperty(notes = "Nombre d'essieux pour un semi remorque", allowEmptyValue = true, required = false)
	private Integer nbEssieuxSemiRemorque;
	
	@ApiModelProperty(notes = "Nombre d'essieux pour un tracteur ou porteur", allowEmptyValue = true, required = false)
	private Integer nbEssieuxTracteurOuPorteur;
	
	@ApiModelProperty(notes = "Date de validité de la patente", allowEmptyValue = false, required = true)
	private Date dateValiditePatente;
	
	@ApiModelProperty(notes = "Date de validité de l'assurance", allowEmptyValue = false, required = true)
	private Date dateValiditeAssurance;
	
	@ApiModelProperty(notes = "Date de validité de la visite techique", allowEmptyValue = false, required = true)
	private Date dateValiditeVisiteTechnique;
	
	@ApiModelProperty(notes = "Photo avant du véhicule en base 64", allowEmptyValue = true, required = false)
	protected String photoAvant;
	
	@ApiModelProperty(notes = "Photo arrière du véhicule en base 64", allowEmptyValue = true, required = false)
	protected String photoArriere;
	
	@ApiModelProperty(notes = "Photo coté du véhicule en base 64", allowEmptyValue = true, required = false)
	protected String photoCote;
	
	@ApiModelProperty(notes = "Document d'assurance", allowEmptyValue = true, required = false)
	protected String photoAssurance;
	
	@ApiModelProperty(notes = "Document carte grise", allowEmptyValue = true, required = false)
	protected String photoCarteGrise;
	

	// propriétaire
	@ApiModelProperty(notes = "Pays dans lequel opère kamtare", allowEmptyValue = false, required = true)
	protected String pays;

	@ApiModelProperty(notes = "Prénom du propriétaire", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.prenom}")
	@Size(min = 1, max = 100, message = "{err.user.create.prenom_longueur}") 
	protected String proprietaire_prenom;

	@ApiModelProperty(notes = "Nom du propriétaire", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.nom}")
	@Size(min = 1, max = 100, message = "{err.user.create.nom_longueur}")
	protected String proprietaire_nom;

	@ApiModelProperty(notes = "Adresse e-mail du propriétaire", allowEmptyValue = false, required = true)
	//@NotNull(message = "{err.user.create.email}")
	//@Size(min = 1, max = 100, message = "{err.user.create.email_longueur}")
	//@EmailAlreadyExistConstraint(message = "{err.user.create.email.existe_deja}")
	//@EmailValidConstraint(message = "{err.user.create.email.invalide}")
	protected String proprietaire_email;

	@ApiModelProperty(notes = "Mot de passe pour se connecter à la webapp expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.mot_de_passe}")
	@Size(min = 1, max = 100, message = "{err.user.create.mot_de_passe_longueur}")
	protected String proprietaire_password;

	@ApiModelProperty(notes = "Numéro de téléphone du propriétaire", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.numero_telephone_1}")
	@Size(min = 1, max = 100, message = "{err.user.create.numero_telephone_1_longueur}")
	protected String proprietaire_numero_telephone_1;

	@ApiModelProperty(notes = "Autre numéro de téléphone du propriétaire", allowEmptyValue = true, required = false)
	protected String proprietaire_numero_telephone_2;

	@ApiModelProperty(notes = "Langue parlée par le propriétaire", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.locale}")
	protected String proprietaire_locale;

	@ApiModelProperty(notes = "Code parrainage", allowEmptyValue = true, required = false)
	protected String proprietaire_codeParrainage;

	@ApiModelProperty(notes = "Numéro de la carte de transport")
	@NotNull(message = "{err.user.create.carte_transport.numero}")
	protected String proprietaire_numeroCarteTransport;
	
	@ApiModelProperty(notes = "Date d'établissement de la carte de transport")
	//@NotNull(message = "{err.user.create.carte_transport.date}")
	protected Date proprietaire_dateEtablissementCarteTransport;

	@ApiModelProperty(notes = "Base64 de la photo de la carte de transport")
	protected String photoCarteTransport;

	@ApiModelProperty(notes = "Nom de l'entreprise", allowEmptyValue = true, required = false)
	protected String entreprise_nom;

	@ApiModelProperty(notes = "Compte comptable de l'entreprise", allowEmptyValue = true, required = false)
	protected String entreprise_compte_comptable;

	@ApiModelProperty(notes = "Numéro RCCM de l'entreprise", allowEmptyValue = true, required = false)
	protected String entreprise_numero_rccm;

	
	// driver
	@ApiModelProperty(notes = "Nom du driver", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.nom}")
	@Size(min = 1, max = 100, message = "{err.user.create.nom_longueur}") 
	protected String chauffeur_nom;

	@ApiModelProperty(notes = "Prénom du driver", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.prenom}")
	@Size(min = 1, max = 100, message = "{err.user.create.prenom_longueur}") 
	protected String chauffeur_prenom;

	@ApiModelProperty(notes = "Adresse e-mail du driver", allowEmptyValue = false, required = true)
	//@NotNull(message = "{err.user.create.email}")
	//@Size(min = 1, max = 100, message = "{err.user.create.email_longueur}")
	//@EmailAlreadyExistConstraint(message = "{err.user.create.email.existe_deja}")
	protected String chauffeur_email;

	@ApiModelProperty(notes = "Mot de passe pour se connecter à la webapp expéditeur", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.mot_de_passe}")
	@Size(min = 1, max = 100, message = "{err.user.create.mot_de_passe_longueur}")
	protected String chauffeur_password;

	@ApiModelProperty(notes = "Numéro de téléphone du driver", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.numero_telephone_1}")
	@Size(min = 1, max = 100, message = "{err.user.create.numero_telephone_1_longueur}") 
	//@NumeroDeTelephoneAlreadyExistConstraint(message = "{err.user.create.numero_telephone.existe_deja}")
	protected String chauffeur_numero_telephone_1;

	@ApiModelProperty(notes = "Autre numéro de téléphone du driver", allowEmptyValue = true, required = false)
	protected String chauffeur_numero_telephone_2;

	@ApiModelProperty(notes = "Langue parlée par le driver", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.locale}")
	protected String chauffeur_locale;

	@ApiModelProperty(notes = "Type de permis (pris dans l'énumération DriverPermis", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.permis_type}")
	@Size(min = 1, max = 100, message = "{err.user.create.permis_type_longueur}") 
	protected String chauffeur_permisType;
	
	@ApiModelProperty(notes = "Numéro du permis", allowEmptyValue = false, required = true)
	@Size(min = 0, max = 100, message = "{err.user.create.permis_numero_longueur}") 
	protected String chauffeur_numeroPermis;

	@ApiModelProperty(notes = "Lieu d'habitation du driver", allowEmptyValue = true, required = false)
	protected String chauffeur_lieuHabitation;

	@ApiModelProperty(notes = "Disponibilité en km", allowEmptyValue = true, required = false)
	protected Integer chauffeur_disponibiliteKm;
	
	@ApiModelProperty(notes = "Photo de profil", allowEmptyValue = true, required = false)
	protected String photoProfil;

	@ApiModelProperty(notes = "Photo du permis", allowEmptyValue = true, required = false)
	protected String photoPermis;

	@ApiModelProperty(notes = "Est ce que le propriétaire est assujeti à l'airsi ?", allowEmptyValue = false, required = true, dataType = "Booléen")
	@NotNull(message = "{err.user.create.assujet_airsi}")
	protected boolean assujetiAIRSI;

	@ApiModelProperty(notes = "Type de compte", allowEmptyValue = false, required = true, dataType = "Chaine de caractères : (B=professionel, C=particulier)")
	@NotNull(message = "{err.client.create.type_de_compte}")
	@Size(min = 1, max = 1, message = "{err.client.create.type_de_compte_longueur}")
	protected String type_compte;

	public String getEntreprise_nom() {
		return entreprise_nom;
	}

	public void setEntreprise_nom(String entreprise_nom) {
		this.entreprise_nom = entreprise_nom;
	}

	public String getEntreprise_compte_comptable() {
		return entreprise_compte_comptable;
	}

	public void setEntreprise_compte_comptable(String entreprise_compte_comptable) {
		this.entreprise_compte_comptable = entreprise_compte_comptable;
	}

	public String getEntreprise_numero_rccm() {
		return entreprise_numero_rccm;
	}

	public void setEntreprise_numero_rccm(String entreprise_numero_rccm) {
		this.entreprise_numero_rccm = entreprise_numero_rccm;
	}

	public String getPays() {
		return pays;
	}

	public void setPays(String pays) {
		this.pays = pays;
	}

	public boolean isAssujetiAIRSI() {
		return assujetiAIRSI;
	}

	public void setAssujetiAIRSI(boolean assujetiAIRSI) {
		this.assujetiAIRSI = assujetiAIRSI;
	}

	public String getType_compte() {
		return type_compte;
	}

	public void setType_compte(String type_compte) {
		this.type_compte = type_compte;
	}

	public String getChauffeur_password() {
		return chauffeur_password;
	}

	public void setChauffeur_password(String chauffeur_password) {
		this.chauffeur_password = chauffeur_password;
	}

	public String getProprietaire_password() {
		return proprietaire_password;
	}

	public void setProprietaire_password(String proprietaire_password) {
		this.proprietaire_password = proprietaire_password;
	}

	/*
        public String getMotDePasse() {
            return motDePasse;
        }

        public void setMotDePasse(String motDePasse) {
            this.motDePasse = motDePasse;
        }
    */

	public String getProprietaire_codeParrainage() {
		return proprietaire_codeParrainage;
	}

	public void setProprietaire_codeParrainage(String proprietaire_codeParrainage) {
		this.proprietaire_codeParrainage = proprietaire_codeParrainage;
	}

	public String getPhotoPrincipale() {
		return photoPrincipale;
	}

	public void setPhotoPrincipale(String photoPrincipale) {
		this.photoPrincipale = photoPrincipale;
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

	public CreateComptePublicParams() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getImmatriculationPays() {
		return immatriculationPays;
	}

	public void setImmatriculationPays(String immatriculationPays) {
		this.immatriculationPays = immatriculationPays;
	}

	public String getProprietaire_nom() {
		return proprietaire_nom;
	}

	public void setProprietaire_nom(String proprietaire_nom) {
		this.proprietaire_nom = proprietaire_nom;
	}

	public String getProprietaire_prenom() {
		return proprietaire_prenom;
	}

	public void setProprietaire_prenom(String proprietaire_prenom) {
		this.proprietaire_prenom = proprietaire_prenom;
	}

	public String getProprietaire_email() {
		return proprietaire_email;
	}

	public void setProprietaire_email(String proprietaire_email) {
		this.proprietaire_email = proprietaire_email;
	}

	public String getProprietaire_numero_telephone_1() {
		return proprietaire_numero_telephone_1;
	}

	public void setProprietaire_numero_telephone_1(String proprietaire_numero_telephone_1) {
		this.proprietaire_numero_telephone_1 = proprietaire_numero_telephone_1;
	}

	public String getProprietaire_numero_telephone_2() {
		return proprietaire_numero_telephone_2;
	}

	public void setProprietaire_numero_telephone_2(String proprietaire_numero_telephone_2) {
		this.proprietaire_numero_telephone_2 = proprietaire_numero_telephone_2;
	}

	public String getProprietaire_locale() {
		return proprietaire_locale;
	}

	public void setProprietaire_locale(String proprietaire_locale) {
		this.proprietaire_locale = proprietaire_locale;
	}

	public String getProprietaire_numeroCarteTransport() {
		return proprietaire_numeroCarteTransport;
	}

	public void setProprietaire_numeroCarteTransport(String proprietaire_numeroCarteTransport) {
		this.proprietaire_numeroCarteTransport = proprietaire_numeroCarteTransport;
	}

	public Date getProprietaire_dateEtablissementCarteTransport() {
		return proprietaire_dateEtablissementCarteTransport;
	}

	public void setProprietaire_dateEtablissementCarteTransport(Date proprietaire_dateEtablissementCarteTransport) {
		this.proprietaire_dateEtablissementCarteTransport = proprietaire_dateEtablissementCarteTransport;
	}

	public String getChauffeur_nom() {
		return chauffeur_nom;
	}

	public void setChauffeur_nom(String chauffeur_nom) {
		this.chauffeur_nom = chauffeur_nom;
	}

	public String getChauffeur_prenom() {
		return chauffeur_prenom;
	}

	public void setChauffeur_prenom(String chauffeur_prenom) {
		this.chauffeur_prenom = chauffeur_prenom;
	}

	public String getChauffeur_email() {
		return chauffeur_email;
	}

	public void setChauffeur_email(String chauffeur_email) {
		this.chauffeur_email = chauffeur_email;
	}

	public String getChauffeur_numero_telephone_1() {
		return chauffeur_numero_telephone_1;
	}

	public void setChauffeur_numero_telephone_1(String chauffeur_numero_telephone_1) {
		this.chauffeur_numero_telephone_1 = chauffeur_numero_telephone_1;
	}

	public String getChauffeur_numero_telephone_2() {
		return chauffeur_numero_telephone_2;
	}

	public void setChauffeur_numero_telephone_2(String chauffeur_numero_telephone_2) {
		this.chauffeur_numero_telephone_2 = chauffeur_numero_telephone_2;
	}

	public String getChauffeur_locale() {
		return chauffeur_locale;
	}

	public void setChauffeur_locale(String chauffeur_locale) {
		this.chauffeur_locale = chauffeur_locale;
	}

	public String getChauffeur_permisType() {
		return chauffeur_permisType;
	}

	public void setChauffeur_permisType(String chauffeur_permisType) {
		this.chauffeur_permisType = chauffeur_permisType;
	}

	public String getChauffeur_numeroPermis() {
		return chauffeur_numeroPermis;
	}

	public void setChauffeur_numeroPermis(String chauffeur_numeroPermis) {
		this.chauffeur_numeroPermis = chauffeur_numeroPermis;
	}

	public String getChauffeur_lieuHabitation() {
		return chauffeur_lieuHabitation;
	}

	public void setChauffeur_lieuHabitation(String chauffeur_lieuHabitation) {
		this.chauffeur_lieuHabitation = chauffeur_lieuHabitation;
	}

	public Integer getChauffeur_disponibiliteKm() {
		return chauffeur_disponibiliteKm;
	}

	public void setChauffeur_disponibiliteKm(Integer chauffeur_disponibiliteKm) {
		this.chauffeur_disponibiliteKm = chauffeur_disponibiliteKm;
	}


	public String getPhotoAvant() {
		return photoAvant;
	}

	public void setPhotoAvant(String photoAvant) {
		this.photoAvant = photoAvant;
	}

	public String getPhotoArriere() {
		return photoArriere;
	}

	public void setPhotoArriere(String photoArriere) {
		this.photoArriere = photoArriere;
	}

	public String getPhotoCote() {
		return photoCote;
	}

	public void setPhotoCote(String photoCote) {
		this.photoCote = photoCote;
	}

	public String getPhotoAssurance() {
		return photoAssurance;
	}

	public void setPhotoAssurance(String photoAssurance) {
		this.photoAssurance = photoAssurance;
	}

	public String getPhotoCarteGrise() {
		return photoCarteGrise;
	}

	public void setPhotoCarteGrise(String photoCarteGrise) {
		this.photoCarteGrise = photoCarteGrise;
	}

	public String getPhotoCarteTransport() {
		return photoCarteTransport;
	}

	public void setPhotoCarteTransport(String photoCarteTransport) {
		this.photoCarteTransport = photoCarteTransport;
	}

	public String getPhotoProfil() {
		return photoProfil;
	}

	public void setPhotoProfil(String photoProfil) {
		this.photoProfil = photoProfil;
	}

	public String getPhotoPermis() {
		return photoPermis;
	}

	public void setPhotoPermis(String photoPermis) {
		this.photoPermis = photoPermis;
	}

	
	

}
