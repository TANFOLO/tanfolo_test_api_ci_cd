package com.kamtar.transport.api.params;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.kamtar.transport.api.model.Etape;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kamtar.transport.api.validation.UUIDFacultatifConstraint;

import io.swagger.annotations.ApiModelProperty;


public class CreateOperationParams extends ParentParams {


	@ApiModelProperty(notes = "Statut", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.operation.create.statut}")
	@Size(min = 1, max = 100, message = "{err.operation.create.statut_longueur}") 
	private String statut;
	
	@ApiModelProperty(notes = "Client", allowEmptyValue = true, required = false)
	@NotNull(message = "{err.operation.create.client}")
	@UUIDFacultatifConstraint(message = "{err.operation.create.client_uuid_invalide}")
	private String clientUuid;

	@ApiModelProperty(notes = "Client personnel", allowEmptyValue = true, required = false)
	//@NotNull(message = "{err.operation.create.client}")
	@UUIDFacultatifConstraint(message = "{err.operation.create.client_uuid_invalide}")
	private String clientPersonnelUuid;

	@ApiModelProperty(notes = "Date programmée pour le retrait de la marchandise chez le client", allowEmptyValue = true, required = false)
	@NotNull(message = "{err.operation.create.depart.date_prevue}")
	private Date departDateProgrammeeOperation;

	@ApiModelProperty(notes = "Latitude GPS du point de départ", allowEmptyValue = false, required = true)	
	@NotNull(message = "{err.operation.create.depart.adresse_latitude}")
	@Range(min = -90, max = +90, message = "{err.operation.create.depart.adresse_latitude_invalide}") 
	private Double departAdresseLatitude;

	@ApiModelProperty(notes = "Longitude GPS du point de départ", allowEmptyValue = false, required = true)	
	@NotNull(message = "{err.operation.create.depart.adresse_longitude}")
	@Range(min = -90, max = +90, message = "{err.operation.create.depart.adresse_longitude_invalide}") 
	private Double departAdresseLongitude;

	@ApiModelProperty(notes = "Adresse du point de départ", allowEmptyValue = false, required = true)	
	@NotNull(message = "{err.operation.create.depart.adresse_complete}")
	@Size(min = 1, max = 2500, message = "{err.operation.create.depart.adresse_complete_longueur}") 
	private String departAdresseComplete;

	@ApiModelProperty(notes = "Complément d'adresse du point de départ", allowEmptyValue = false, required = true)	
	@Size(min = 0, max = 2500, message = "{err.operation.create.depart.adresse_complement_longueur}") 
	private String departAdresseComplement;

	@ApiModelProperty(notes = "Pays du point de départ", allowEmptyValue = false, required = true)	
	@NotNull(message = "{err.operation.create.depart.pays}")
	@Size(min = 1, max = 200, message = "{err.operation.create.depart.pays_longueur}") 
	private String departAdresseCountryCode;

	@ApiModelProperty(notes = "Ville du point de départ", allowEmptyValue = false, required = true)	
	@NotNull(message = "{err.operation.create.depart.ville}")
	@Size(min = 1, max = 2500, message = "{err.operation.create.depart.ville_longueur}") 
	private String departAdresseVille;

	@ApiModelProperty(notes = "Rue du point de départ", allowEmptyValue = false, required = true)	
	//@NotNull(message = "{err.operation.create.depart.rue}")
	//@Size(min = 1, max = 2500, message = "{err.operation.create.depart.rue_longueur}")
	private String departAdresseRue;

	// arrivée
	@ApiModelProperty(notes = "Nom du destinataire", allowEmptyValue = true, required = false)	
	private String arriveeDestinataireNom;

	@ApiModelProperty(notes = "Numéro de téléphone du destinataire")	
	private String arriveeDestinataireTelephone;
	
	@ApiModelProperty(notes = "Date programmée pour l'arrivée de la marchandise chez le destinataire", allowEmptyValue = true, required = false)
	private Date arriveeDateProgrammeeOperation;

	@ApiModelProperty(notes = "Latitude GPS du point de arrivée", allowEmptyValue = false, required = true)	
	@NotNull(message = "{err.operation.create.arrivee.adresse_latitude}")
	@Range(min = -90, max = +90, message = "{err.operation.create.arrivee.adresse_latitude_invalide}") 
	private Double arriveeAdresseLatitude;

	@ApiModelProperty(notes = "Longitude GPS du point de arrivée", allowEmptyValue = false, required = true)	
	@NotNull(message = "{err.operation.create.arrivee.adresse_longitude}")
	@Range(min = -90, max = +90, message = "{err.operation.create.arrivee.adresse_longitude_invalide}") 
	private Double arriveeAdresseLongitude;

	@ApiModelProperty(notes = "Adresse du point de arrivée", allowEmptyValue = false, required = true)	
	@NotNull(message = "{err.operation.create.arrivee.adresse_complete}")
	@Size(min = 1, max = 2500, message = "{err.operation.create.arrivee.adresse_complete_longueur}") 
	private String arriveeAdresseComplete;

	@ApiModelProperty(notes = "Complément d'adresse du point de arrivée", allowEmptyValue = false, required = true)	
	@Size(min = 0, max = 2500, message = "{err.operation.create.arrivee.adresse_complement_longueur}") 
	private String arriveeAdresseComplement;

	@ApiModelProperty(notes = "Pays du point de arrivée", allowEmptyValue = false, required = true)	
	@NotNull(message = "{err.operation.create.arrivee.pays}")
	@Size(min = 1, max = 200, message = "{err.operation.create.arrivee.pays_longueur}") 
	private String arriveeAdresseCountryCode;

	@ApiModelProperty(notes = "Ville du point de arrivée", allowEmptyValue = false, required = true)	
	@NotNull(message = "{err.operation.create.arrivee.ville}")
	@Size(min = 1, max = 2500, message = "{err.operation.create.arrivee.ville_longueur}") 
	private String arriveeAdresseVille;

	@ApiModelProperty(notes = "Rue du point de arrivée", allowEmptyValue = false, required = true)
	private String arriveeAdresseRue;
/*
	// etape 1
	@ApiModelProperty(notes = "Nom du destinataire", allowEmptyValue = true, required = false)
	private String adresse1DestinataireNom;

	@ApiModelProperty(notes = "Numéro de téléphone du destinataire")
	private String adresse1DestinataireTelephone;

	@ApiModelProperty(notes = "Latitude GPS du point de arrivée", allowEmptyValue = true, required = false)
	private Double adresse1Latitude;

	@ApiModelProperty(notes = "Longitude GPS du point de arrivée", allowEmptyValue = true, required = false)
	private Double adresse1Longitude;

	@ApiModelProperty(notes = "Adresse du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse1Complete;

	@ApiModelProperty(notes = "Complément d'adresse du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse1Complement;

	@ApiModelProperty(notes = "Pays du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse1CountryCode;

	@ApiModelProperty(notes = "Ville du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse1Ville;

	@ApiModelProperty(notes = "Rue du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse1Rue;

	// etape 2
	@ApiModelProperty(notes = "Nom du destinataire", allowEmptyValue = true, required = false)
	private String adresse2DestinataireNom;

	@ApiModelProperty(notes = "Numéro de téléphone du destinataire")
	private String adresse2DestinataireTelephone;

	@ApiModelProperty(notes = "Latitude GPS du point de arrivée", allowEmptyValue = true, required = false)
	private Double adresse2Latitude;

	@ApiModelProperty(notes = "Longitude GPS du point de arrivée", allowEmptyValue = true, required = false)
	private Double adresse2Longitude;

	@ApiModelProperty(notes = "Adresse du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse2Complete;

	@ApiModelProperty(notes = "Complément d'adresse du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse2Complement;

	@ApiModelProperty(notes = "Pays du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse2CountryCode;

	@ApiModelProperty(notes = "Ville du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse2Ville;

	@ApiModelProperty(notes = "Rue du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse2Rue;

	// etape 3
	@ApiModelProperty(notes = "Nom du destinataire", allowEmptyValue = true, required = false)
	private String adresse3DestinataireNom;

	@ApiModelProperty(notes = "Numéro de téléphone du destinataire")
	private String adresse3DestinataireTelephone;

	@ApiModelProperty(notes = "Latitude GPS du point de arrivée", allowEmptyValue = true, required = false)
	private Double adresse3Latitude;

	@ApiModelProperty(notes = "Longitude GPS du point de arrivée", allowEmptyValue = true, required = false)
	private Double adresse3Longitude;

	@ApiModelProperty(notes = "Adresse du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse3Complete;

	@ApiModelProperty(notes = "Complément d'adresse du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse3Complement;

	@ApiModelProperty(notes = "Pays du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse3CountryCode;

	@ApiModelProperty(notes = "Ville du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse3Ville;

	@ApiModelProperty(notes = "Rue du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse3Rue;

	// etape 4
	@ApiModelProperty(notes = "Nom du destinataire", allowEmptyValue = true, required = false)
	private String adresse4DestinataireNom;

	@ApiModelProperty(notes = "Numéro de téléphone du destinataire")
	private String adresse4DestinataireTelephone;

	@ApiModelProperty(notes = "Latitude GPS du point de arrivée", allowEmptyValue = true, required = false)
	private Double adresse4Latitude;

	@ApiModelProperty(notes = "Longitude GPS du point de arrivée", allowEmptyValue = true, required = false)
	private Double adresse4Longitude;

	@ApiModelProperty(notes = "Adresse du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse4Complete;

	@ApiModelProperty(notes = "Complément d'adresse du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse4Complement;

	@ApiModelProperty(notes = "Pays du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse4CountryCode;

	@ApiModelProperty(notes = "Ville du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse4Ville;

	@ApiModelProperty(notes = "Rue du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse4Rue;

	// etape 5
	@ApiModelProperty(notes = "Nom du destinataire", allowEmptyValue = true, required = false)
	private String adresse5DestinataireNom;

	@ApiModelProperty(notes = "Numéro de téléphone du destinataire")
	private String adresse5DestinataireTelephone;

	@ApiModelProperty(notes = "Latitude GPS du point de arrivée", allowEmptyValue = true, required = false)
	private Double adresse5Latitude;

	@ApiModelProperty(notes = "Longitude GPS du point de arrivée", allowEmptyValue = true, required = false)
	private Double adresse5Longitude;

	@ApiModelProperty(notes = "Adresse du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse5Complete;

	@ApiModelProperty(notes = "Complément d'adresse du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse5Complement;

	@ApiModelProperty(notes = "Pays du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse5CountryCode;

	@ApiModelProperty(notes = "Ville du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse5Ville;

	@ApiModelProperty(notes = "Rue du point de arrivée", allowEmptyValue = true, required = false)
	private String adresse5Rue;
*/


	@ApiModelProperty(notes = "Catégorie de véhicule sélectionnée par le client", allowEmptyValue = false, required = true)	
	//@NotNull(message = "{err.operation.create.vehicule.categorie}")
	//@Size(min = 1, max = 2500, message = "{err.operation.create.vehicule.categorie_longueur}")
	private String categorieVehicule;
	
	@ApiModelProperty(notes = "Caractéristiques du véhicules saisies par le client", allowEmptyValue = true, required = false)	
	private String caracteristiquesVehicule;
	
	@ApiModelProperty(notes = "Type de marchandises saisies par le client", allowEmptyValue = false, required = true)	
	@NotNull(message = "{err.operation.create.marchandise.type}")
	@Size(min = 1, max = 2500, message = "{err.operation.create.marchandise.type_longueur}") 
	private String typeMarchandise;

	@ApiModelProperty(notes = "Tonnage du véhicule saisies par le client", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.operation.create.vehicule.tonnage}")
	@Size(min = 1, max = 2500, message = "{err.operation.create.vehicule.tonnage_longueur}")
	private String tonnageVehicule;

	@ApiModelProperty(notes = "Observations saisies par le client", allowEmptyValue = true, required = false)	
	private String observationsParClient;
	
	@ApiModelProperty(notes = "Prix souhaitée par le client", allowEmptyValue = true, required = false)	
	private Double prixSouhaiteParClient;
	
	@ApiModelProperty(notes = "Devise du prix souhaité par le client", allowEmptyValue = true, required = false)	
	private String prixSouhaiteParClientDevise;
	
	@ApiModelProperty(notes = "Transporteur à qui l'opération a été affectée", allowEmptyValue = true, required = false)
	@UUIDFacultatifConstraint(message = "{err.operation.create.transporteur.invalide}")
	private String transporteur;
	
	@ApiModelProperty(notes = "Véhicule à qui l'opération a été affectée", allowEmptyValue = true, required = false)
	@UUIDFacultatifConstraint(message = "{err.operation.create.vehicule.invalide}")
	private String vehicule;

	@ApiModelProperty(notes = "Prix à payer par le client", allowEmptyValue = true, required = false)	
	private Double prixAPayerParClient;
	
	@ApiModelProperty(notes = "Devise du prix à payer par le client", allowEmptyValue = true, required = false)	
	private String prixAPayerParClientDevise;

	@ApiModelProperty(notes = "Prix demandé par le driver", allowEmptyValue = true, required = false)	
	private Double prixDemandeParDriver;

	@ApiModelProperty(notes = "Devise du prix demandé par le driver", allowEmptyValue = true, required = false)	
	private String prixDemandeParDriverDevise;
	
	@ApiModelProperty(notes = "Assurance saisies par Kamtar", allowEmptyValue = true, required = false)	
	private String assurance;
	
	@ApiModelProperty(notes = "Liste des services additionnels saisies par Kamtar", allowEmptyValue = true, required = false)	
	private String servicesAdditionnels;
	
	@ApiModelProperty(notes = "Informations compleemntaires saisies par Kamtar", allowEmptyValue = true, required = false)	
	private String informationsComplementaires;

	@ApiModelProperty(notes = "Avance donnée au driver", allowEmptyValue = true, required = false)	
	private Double avanceDonneAuDriver;

	@ApiModelProperty(notes = "Devise de l'avance donnée au driver", allowEmptyValue = true, required = false)	
	private String avanceDonneAuDriverDevise;

	@ApiModelProperty(notes = "Montant donné au superviseur", allowEmptyValue = true, required = false)
	private String superviseur;

	@ApiModelProperty(notes = "Devise du montant donné au superviseur", allowEmptyValue = true, required = false)
	private String superviseurDevise;

	@ApiModelProperty(notes = "Obeservations saisies par le driver", allowEmptyValue = true, required = false)	
	private String observationsParTransporteur;
	
	@ApiModelProperty(notes = "Est ce que la course est géolocalisée ?")
    protected boolean courseGeolocalisee;

	@ApiModelProperty(notes = "Est ce que les statuts évoluent en temp réel ?")
    protected boolean statutTempsReel;

	@ApiModelProperty(notes = "UUID temporaire identique à tous les documents uploadées pour une même opération à la création", allowEmptyValue = true, required = false)
	private String folderUuid;

	@ApiModelProperty(notes = "Date du paiement de l'avance du driver", allowEmptyValue = true, required = false)
	private Date datePaiementAvanceDriver;

	@ApiModelProperty(notes = "Date du paiement du solde du driver", allowEmptyValue = true, required = false)
	@NotNull(message = "{err.operation.create.date_paiement_solde_driver}")
	private Date datePaiementSoldeDriver;

	@ApiModelProperty(notes = "Type d'opération pour la TVA", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.operation.create.type_operation_tva}")
	@Size(min = 1, max = 250, message = "{err.operation.create.type_operation_tva}")
	private String typeOperationTVA;

	@ApiModelProperty(notes = "Liste des étapes", allowEmptyValue = true, required = false)
	private List<EtapeParams> etapes;

	public List<EtapeParams> getEtapes() {
		return etapes;
	}

	public void setEtapes(List<EtapeParams> etapes) {
		this.etapes = etapes;
	}

	public String getSuperviseurDevise() {
		return superviseurDevise;
	}

	public void setSuperviseurDevise(String superviseurDevise) {
		this.superviseurDevise = superviseurDevise;
	}

	public String getClientPersonnelUuid() {
		return clientPersonnelUuid;
	}

	public void setClientPersonnelUuid(String clientPersonnelUuid) {
		this.clientPersonnelUuid = clientPersonnelUuid;
	}

	public String getTypeOperationTVA() {
		return typeOperationTVA;
	}

	public void setTypeOperationTVA(String typeOperationTVA) {
		this.typeOperationTVA = typeOperationTVA;
	}
/*
	public String getAdresse1DestinataireNom() {
		return adresse1DestinataireNom;
	}

	public void setAdresse1DestinataireNom(String adresse1DestinataireNom) {
		this.adresse1DestinataireNom = adresse1DestinataireNom;
	}

	public String getAdresse1DestinataireTelephone() {
		return adresse1DestinataireTelephone;
	}

	public void setAdresse1DestinataireTelephone(String adresse1DestinataireTelephone) {
		this.adresse1DestinataireTelephone = adresse1DestinataireTelephone;
	}

	public Double getAdresse1Latitude() {
		return adresse1Latitude;
	}

	public void setAdresse1Latitude(Double adresse1Latitude) {
		this.adresse1Latitude = adresse1Latitude;
	}

	public Double getAdresse1Longitude() {
		return adresse1Longitude;
	}

	public void setAdresse1Longitude(Double adresse1Longitude) {
		this.adresse1Longitude = adresse1Longitude;
	}

	public String getAdresse1Complete() {
		return adresse1Complete;
	}

	public void setAdresse1Complete(String adresse1Complete) {
		this.adresse1Complete = adresse1Complete;
	}

	public String getAdresse1Complement() {
		return adresse1Complement;
	}

	public void setAdresse1Complement(String adresse1Complement) {
		this.adresse1Complement = adresse1Complement;
	}

	public String getAdresse1CountryCode() {
		return adresse1CountryCode;
	}

	public void setAdresse1CountryCode(String adresse1CountryCode) {
		this.adresse1CountryCode = adresse1CountryCode;
	}

	public String getAdresse1Ville() {
		return adresse1Ville;
	}

	public void setAdresse1Ville(String adresse1Ville) {
		this.adresse1Ville = adresse1Ville;
	}

	public String getAdresse1Rue() {
		return adresse1Rue;
	}

	public void setAdresse1Rue(String adresse1Rue) {
		this.adresse1Rue = adresse1Rue;
	}

	public String getAdresse2DestinataireNom() {
		return adresse2DestinataireNom;
	}

	public void setAdresse2DestinataireNom(String adresse2DestinataireNom) {
		this.adresse2DestinataireNom = adresse2DestinataireNom;
	}

	public String getAdresse2DestinataireTelephone() {
		return adresse2DestinataireTelephone;
	}

	public void setAdresse2DestinataireTelephone(String adresse2DestinataireTelephone) {
		this.adresse2DestinataireTelephone = adresse2DestinataireTelephone;
	}

	public Double getAdresse2Latitude() {
		return adresse2Latitude;
	}

	public void setAdresse2Latitude(Double adresse2Latitude) {
		this.adresse2Latitude = adresse2Latitude;
	}

	public Double getAdresse2Longitude() {
		return adresse2Longitude;
	}

	public void setAdresse2Longitude(Double adresse2Longitude) {
		this.adresse2Longitude = adresse2Longitude;
	}

	public String getAdresse2Complete() {
		return adresse2Complete;
	}

	public void setAdresse2Complete(String adresse2Complete) {
		this.adresse2Complete = adresse2Complete;
	}

	public String getAdresse2Complement() {
		return adresse2Complement;
	}

	public void setAdresse2Complement(String adresse2Complement) {
		this.adresse2Complement = adresse2Complement;
	}

	public String getAdresse2CountryCode() {
		return adresse2CountryCode;
	}

	public void setAdresse2CountryCode(String adresse2CountryCode) {
		this.adresse2CountryCode = adresse2CountryCode;
	}

	public String getAdresse2Ville() {
		return adresse2Ville;
	}

	public void setAdresse2Ville(String adresse2Ville) {
		this.adresse2Ville = adresse2Ville;
	}

	public String getAdresse2Rue() {
		return adresse2Rue;
	}

	public void setAdresse2Rue(String adresse2Rue) {
		this.adresse2Rue = adresse2Rue;
	}

	public String getAdresse3DestinataireNom() {
		return adresse3DestinataireNom;
	}

	public void setAdresse3DestinataireNom(String adresse3DestinataireNom) {
		this.adresse3DestinataireNom = adresse3DestinataireNom;
	}

	public String getAdresse3DestinataireTelephone() {
		return adresse3DestinataireTelephone;
	}

	public void setAdresse3DestinataireTelephone(String adresse3DestinataireTelephone) {
		this.adresse3DestinataireTelephone = adresse3DestinataireTelephone;
	}

	public Double getAdresse3Latitude() {
		return adresse3Latitude;
	}

	public void setAdresse3Latitude(Double adresse3Latitude) {
		this.adresse3Latitude = adresse3Latitude;
	}

	public Double getAdresse3Longitude() {
		return adresse3Longitude;
	}

	public void setAdresse3Longitude(Double adresse3Longitude) {
		this.adresse3Longitude = adresse3Longitude;
	}

	public String getAdresse3Complete() {
		return adresse3Complete;
	}

	public void setAdresse3Complete(String adresse3Complete) {
		this.adresse3Complete = adresse3Complete;
	}

	public String getAdresse3Complement() {
		return adresse3Complement;
	}

	public void setAdresse3Complement(String adresse3Complement) {
		this.adresse3Complement = adresse3Complement;
	}

	public String getAdresse3CountryCode() {
		return adresse3CountryCode;
	}

	public void setAdresse3CountryCode(String adresse3CountryCode) {
		this.adresse3CountryCode = adresse3CountryCode;
	}

	public String getAdresse3Ville() {
		return adresse3Ville;
	}

	public void setAdresse3Ville(String adresse3Ville) {
		this.adresse3Ville = adresse3Ville;
	}

	public String getAdresse3Rue() {
		return adresse3Rue;
	}

	public void setAdresse3Rue(String adresse3Rue) {
		this.adresse3Rue = adresse3Rue;
	}

	public String getAdresse4DestinataireNom() {
		return adresse4DestinataireNom;
	}

	public void setAdresse4DestinataireNom(String adresse4DestinataireNom) {
		this.adresse4DestinataireNom = adresse4DestinataireNom;
	}

	public String getAdresse4DestinataireTelephone() {
		return adresse4DestinataireTelephone;
	}

	public void setAdresse4DestinataireTelephone(String adresse4DestinataireTelephone) {
		this.adresse4DestinataireTelephone = adresse4DestinataireTelephone;
	}

	public Double getAdresse4Latitude() {
		return adresse4Latitude;
	}

	public void setAdresse4Latitude(Double adresse4Latitude) {
		this.adresse4Latitude = adresse4Latitude;
	}

	public Double getAdresse4Longitude() {
		return adresse4Longitude;
	}

	public void setAdresse4Longitude(Double adresse4Longitude) {
		this.adresse4Longitude = adresse4Longitude;
	}

	public String getAdresse4Complete() {
		return adresse4Complete;
	}

	public void setAdresse4Complete(String adresse4Complete) {
		this.adresse4Complete = adresse4Complete;
	}

	public String getAdresse4Complement() {
		return adresse4Complement;
	}

	public void setAdresse4Complement(String adresse4Complement) {
		this.adresse4Complement = adresse4Complement;
	}

	public String getAdresse4CountryCode() {
		return adresse4CountryCode;
	}

	public void setAdresse4CountryCode(String adresse4CountryCode) {
		this.adresse4CountryCode = adresse4CountryCode;
	}

	public String getAdresse4Ville() {
		return adresse4Ville;
	}

	public void setAdresse4Ville(String adresse4Ville) {
		this.adresse4Ville = adresse4Ville;
	}

	public String getAdresse4Rue() {
		return adresse4Rue;
	}

	public void setAdresse4Rue(String adresse4Rue) {
		this.adresse4Rue = adresse4Rue;
	}

	public String getAdresse5DestinataireNom() {
		return adresse5DestinataireNom;
	}

	public void setAdresse5DestinataireNom(String adresse5DestinataireNom) {
		this.adresse5DestinataireNom = adresse5DestinataireNom;
	}

	public String getAdresse5DestinataireTelephone() {
		return adresse5DestinataireTelephone;
	}

	public void setAdresse5DestinataireTelephone(String adresse5DestinataireTelephone) {
		this.adresse5DestinataireTelephone = adresse5DestinataireTelephone;
	}

	public Double getAdresse5Latitude() {
		return adresse5Latitude;
	}

	public void setAdresse5Latitude(Double adresse5Latitude) {
		this.adresse5Latitude = adresse5Latitude;
	}

	public Double getAdresse5Longitude() {
		return adresse5Longitude;
	}

	public void setAdresse5Longitude(Double adresse5Longitude) {
		this.adresse5Longitude = adresse5Longitude;
	}

	public String getAdresse5Complete() {
		return adresse5Complete;
	}

	public void setAdresse5Complete(String adresse5Complete) {
		this.adresse5Complete = adresse5Complete;
	}

	public String getAdresse5Complement() {
		return adresse5Complement;
	}

	public void setAdresse5Complement(String adresse5Complement) {
		this.adresse5Complement = adresse5Complement;
	}

	public String getAdresse5CountryCode() {
		return adresse5CountryCode;
	}

	public void setAdresse5CountryCode(String adresse5CountryCode) {
		this.adresse5CountryCode = adresse5CountryCode;
	}

	public String getAdresse5Ville() {
		return adresse5Ville;
	}

	public void setAdresse5Ville(String adresse5Ville) {
		this.adresse5Ville = adresse5Ville;
	}

	public String getAdresse5Rue() {
		return adresse5Rue;
	}

	public void setAdresse5Rue(String adresse5Rue) {
		this.adresse5Rue = adresse5Rue;
	}
*/
	public Date getDatePaiementAvanceDriver() {
		return datePaiementAvanceDriver;
	}

	public void setDatePaiementAvanceDriver(Date datePaiementAvanceDriver) {
		this.datePaiementAvanceDriver = datePaiementAvanceDriver;
	}

	public Date getDatePaiementSoldeDriver() {
		return datePaiementSoldeDriver;
	}

	public void setDatePaiementSoldeDriver(Date datePaiementSoldeDriver) {
		this.datePaiementSoldeDriver = datePaiementSoldeDriver;
	}

	public String getFolderUuid() {
		return folderUuid;
	}

	public void setFolderUuid(String folderUuid) {
		this.folderUuid = folderUuid;
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

	public boolean isCourseGeolocalisee() {
		return courseGeolocalisee;
	}

	public void setCourseGeolocalisee(boolean courseGeolocalisee) {
		this.courseGeolocalisee = courseGeolocalisee;
	}

	public boolean isStatutTempsReel() {
		return statutTempsReel;
	}

	public void setStatutTempsReel(boolean statutTempsReel) {
		this.statutTempsReel = statutTempsReel;
	}

	public String getCaracteristiquesVehicule() {
		return caracteristiquesVehicule;
	}

	public void setCaracteristiquesVehicule(String caracteristiquesVehicule) {
		this.caracteristiquesVehicule = caracteristiquesVehicule;
	}

	public String getPrixSouhaiteParClientDevise() {
		return prixSouhaiteParClientDevise;
	}

	public void setPrixSouhaiteParClientDevise(String prixSouhaiteParClientDevise) {
		this.prixSouhaiteParClientDevise = prixSouhaiteParClientDevise;
	}

	public String getPrixAPayerParClientDevise() {
		return prixAPayerParClientDevise;
	}

	public void setPrixAPayerParClientDevise(String prixAPayerParClientDevise) {
		this.prixAPayerParClientDevise = prixAPayerParClientDevise;
	}

	public String getPrixDemandeParDriverDevise() {
		return prixDemandeParDriverDevise;
	}

	public void setPrixDemandeParDriverDevise(String prixDemandeParDriverDevise) {
		this.prixDemandeParDriverDevise = prixDemandeParDriverDevise;
	}

	public String getAvanceDonneAuDriverDevise() {
		return avanceDonneAuDriverDevise;
	}

	public void setAvanceDonneAuDriverDevise(String avanceDonneAuDriverDevise) {
		this.avanceDonneAuDriverDevise = avanceDonneAuDriverDevise;
	}

	public String getStatut() {
		return statut;
	}

	public void setStatut(String statut) {
		this.statut = statut;
	}

	public String getClientUuid() {
		return clientUuid;
	}

	public void setClientUuid(String clientUuid) {
		this.clientUuid = clientUuid;
	}

	public Date getDepartDateProgrammeeOperation() {
		return departDateProgrammeeOperation;
	}

	public void setDepartDateProgrammeeOperation(Date departDateProgrammeeOperation) {
		this.departDateProgrammeeOperation = departDateProgrammeeOperation;
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

	public Date getArriveeDateProgrammeeOperation() {
		return arriveeDateProgrammeeOperation;
	}

	public void setArriveeDateProgrammeeOperation(Date arriveeDateProgrammeeOperation) {
		this.arriveeDateProgrammeeOperation = arriveeDateProgrammeeOperation;
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

	public String getTonnageVehicule() {
		return tonnageVehicule;
	}

	public void setTonnageVehicule(String tonnageVehicule) {
		this.tonnageVehicule = tonnageVehicule;
	}

	public String getTypeMarchandise() {
		return typeMarchandise;
	}

	public void setTypeMarchandise(String typeMarchandise) {
		this.typeMarchandise = typeMarchandise;
	}

	public String getObservationsParClient() {
		return observationsParClient;
	}

	public void setObservationsParClient(String observationsParClient) {
		this.observationsParClient = observationsParClient;
	}

	public Double getPrixSouhaiteParClient() {
		return prixSouhaiteParClient;
	}

	public void setPrixSouhaiteParClient(Double prixSouhaiteParClient) {
		this.prixSouhaiteParClient = prixSouhaiteParClient;
	}

	public String getTransporteur() {
		return transporteur;
	}

	public void setTransporteur(String transporteur) {
		this.transporteur = transporteur;
	}

	public String getVehicule() {
		return vehicule;
	}

	public void setVehicule(String vehicule) {
		this.vehicule = vehicule;
	}

	public Double getPrixAPayerParClient() {
		return prixAPayerParClient;
	}

	public void setPrixAPayerParClient(Double prixAPayerParClient) {
		this.prixAPayerParClient = prixAPayerParClient;
	}

	public Double getPrixDemandeParDriver() {
		return prixDemandeParDriver;
	}

	public void setPrixDemandeParDriver(Double prixDemandeParDriver) {
		this.prixDemandeParDriver = prixDemandeParDriver;
	}

	public String getAssurance() {
		return assurance;
	}

	public void setAssurance(String assurance) {
		this.assurance = assurance;
	}

	public String getServicesAdditionnels() {
		return servicesAdditionnels;
	}

	public void setServicesAdditionnels(String servicesAdditionnels) {
		this.servicesAdditionnels = servicesAdditionnels;
	}

	public String getInformationsComplementaires() {
		return informationsComplementaires;
	}

	public void setInformationsComplementaires(String informationsComplementaires) {
		this.informationsComplementaires = informationsComplementaires;
	}

	public Double getAvanceDonneAuDriver() {
		return avanceDonneAuDriver;
	}

	public void setAvanceDonneAuDriver(Double avanceDonneAuDriver) {
		this.avanceDonneAuDriver = avanceDonneAuDriver;
	}

	public String getSuperviseur() {
		return superviseur;
	}

	public void setSuperviseur(String superviseur) {
		this.superviseur = superviseur;
	}

	public String getObservationsParTransporteur() {
		return observationsParTransporteur;
	}

	public void setObservationsParTransporteur(String observationsParTransporteur) {
		this.observationsParTransporteur = observationsParTransporteur;
	}

	



}
