package com.kamtar.transport.api.params;

import com.kamtar.transport.api.model.Etape;
import com.kamtar.transport.api.validation.EmailObligatoireValidConstraint;
import com.kamtar.transport.api.validation.EmailValidConstraint;
import com.kamtar.transport.api.validation.NumeroDeTelephoneLibreValidConstraint;
import com.kamtar.transport.api.validation.UUIDFacultatifConstraint;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;


public class CreateDevisParams extends ParentParams {


	@ApiModelProperty(notes = "Type de compte", allowEmptyValue = false, required = true, dataType = "Chaine de caractères : (B=professionel, C=particulier)")
	@NotNull(message = "{err.client.create.type_de_compte}")
	@Size(min = 1, max = 1, message = "{err.client.create.type_de_compte_longueur}")
	private String typeCompte;

	@ApiModelProperty(notes = "Nom de la société (si typeCompte = B)", allowEmptyValue = true, required = false)
	private String nomSociete;

	@ApiModelProperty(notes = "Nom du client", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.client.create.nom}")
	@Size(min = 1, max = 250, message = "{err.client.create.nom_longueur}")
	private String nom;

	@ApiModelProperty(notes = "Prénom du client", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.client.create.prenom}")
	@Size(min = 1, max = 250, message = "{err.client.create.prenom_longueur}")
	private String prenom;

	@ApiModelProperty(notes = "Adresse e-mail du client", allowEmptyValue = true, required = false)
	@NotNull(message = "{err.client.create.email}")
	@Size(min = 1, max = 200, message = "{err.client.create.email_longueur}")
	@EmailObligatoireValidConstraint(message = "{err.client.create.email_invalide}")
	protected String email;

	@ApiModelProperty(notes = "Code pays du téléphone du client", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.telephone_code_pays}")
	@Size(min = 1, max = 100, message = "{err.user.create.telephone_code_pays_longueur}")
	protected String pays_telephone_1;

	@ApiModelProperty(notes = "Numéro de téléphone du client", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.user.create.numero_telephone_1}")
	@Size(min = 1, max = 100, message = "{err.user.create.numero_telephone_1_longueur}")
	@NumeroDeTelephoneLibreValidConstraint(message = "{err.user.create.numero_telephone.invalide}")
	protected String telephone1;

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
	
	@ApiModelProperty(notes = "Nom du destinataire", allowEmptyValue = true, required = false)	
	private String arriveeDestinataireNom;

	@ApiModelProperty(notes = "Numéro de téléphone du destinataire")	
	private String arriveeDestinataireTelephone;
	
	@ApiModelProperty(notes = "Date programmée pour l'arrivée de la marchandise chez le destinataire", allowEmptyValue = false, required = true)
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
	//@NotNull(message = "{err.operation.create.arrivee.rue}")
	//@Size(min = 1, max = 2500, message = "{err.operation.create.arrivee.rue_longueur}")
	private String arriveeAdresseRue;
	
	@ApiModelProperty(notes = "Type de marchandises saisies par le client", allowEmptyValue = false, required = true)	
	@NotNull(message = "{err.operation.create.marchandise.type}")
	@Size(min = 1, max = 2500, message = "{err.operation.create.marchandise.type_longueur}") 
	private String typeMarchandise;
	
	@ApiModelProperty(notes = "Observations saisies par le client", allowEmptyValue = true, required = false)	
	private String observationsParClient;
	
	@ApiModelProperty(notes = "Prix souhaitée par le client", allowEmptyValue = true, required = false)	
	private Double prixSouhaiteParClient;
	
	@ApiModelProperty(notes = "Devise du prix souhaité par le client", allowEmptyValue = true, required = false)	
	private String prixSouhaiteParClientDevise;
	
	@ApiModelProperty(notes = "Carrosserie de véhicule saisies par le client", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.operation.create.vehicule.carrosserie}")
	@Size(min = 1, max = 2500, message = "{err.operation.create.vehicule.carrosserie}")
	private String carrosserieVehicule;

	@ApiModelProperty(notes = "Tonnage de véhicule saisies par le client", allowEmptyValue = true, required = true)
	@Size(min = 1, message = "{err.operation.create.vehicule.tonnage}")
	private String tonnageVehicule;

	@ApiModelProperty(notes = "Date programmée pour le retrait de la marchandise chez le client", allowEmptyValue = true, required = false)
	private Date departDateProgrammeeOperation;

	@ApiModelProperty(notes = "Liste des étapes", allowEmptyValue = true, required = false)
	private List<EtapeParams> etapes;

	public String getPrenom() {
		return prenom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	public String getTonnageVehicule() {
		return tonnageVehicule;
	}

	public void setTonnageVehicule(String tonnageVehicule) {
		this.tonnageVehicule = tonnageVehicule;
	}

	public Date getDepartDateProgrammeeOperation() {
		return departDateProgrammeeOperation;
	}

	public void setDepartDateProgrammeeOperation(Date departDateProgrammeeOperation) {
		this.departDateProgrammeeOperation = departDateProgrammeeOperation;
	}

	public String getCarrosserieVehicule() {
		return carrosserieVehicule;
	}

	public void setCarrosserieVehicule(String carrosserieVehicule) {
		this.carrosserieVehicule = carrosserieVehicule;
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

	public String getPrixSouhaiteParClientDevise() {
		return prixSouhaiteParClientDevise;
	}

	public void setPrixSouhaiteParClientDevise(String prixSouhaiteParClientDevise) {
		this.prixSouhaiteParClientDevise = prixSouhaiteParClientDevise;
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

	public String getTypeCompte() {
		return typeCompte;
	}

	public void setTypeCompte(String typeCompte) {
		this.typeCompte = typeCompte;
	}

	public String getNomSociete() {
		return nomSociete;
	}

	public void setNomSociete(String nomSociete) {
		this.nomSociete = nomSociete;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPays_telephone_1() {
		return pays_telephone_1;
	}

	public void setPays_telephone_1(String pays_telephone_1) {
		this.pays_telephone_1 = pays_telephone_1;
	}

	public String getTelephone1() {
		return telephone1;
	}

	public void setTelephone1(String telephone1) {
		this.telephone1 = telephone1;
	}

	public List<EtapeParams> getEtapes() {
		return etapes;
	}

	public void setEtapes(List<EtapeParams> etapes) {
		this.etapes = etapes;
	}
}
