package com.kamtar.transport.api.model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.*;

import com.kamtar.transport.api.params.*;
import com.kamtar.transport.api.service.impl.OperationServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kamtar.transport.api.enums.OperationStatut;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import springfox.documentation.spring.web.readers.operation.OperationDeprecatedReader;

@ApiModel(description = "Operation")
@Entity
@Table(name = "operation", indexes = { @Index(name = "idx_operation_codePays", columnList = "codePays"), @Index(name = "idx_operation_arriveeAdresseVille", columnList = "arriveeAdresseVille") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {}, allowGetters = true)
public class Operation implements Serializable, Cloneable {
    /**
     * Logger de la classe
     */
    private static Logger logger = LogManager.getLogger(Operation.class);

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(notes = "Identifiant")
    @Id
    @Type(type = "uuid-char")
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected UUID uuid;

    @ApiModelProperty(notes = "Code de l'opération")
    @Column(updatable = false, name = "code", nullable = false, unique = true)
    private Long code;

    @ApiModelProperty(notes = "Date de création")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @Column(name = "createdOn", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdOn = new Date();

    @ApiModelProperty(notes = "Date de dernière mise à jour")
    @Column(name = "updatedOn", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    private Date updatedOn = new Date();

    @ApiModelProperty(notes = "Statut")
    @Column(updatable = true, name = "statut", columnDefinition = "TEXT", nullable = true, length = 2500, unique = false)
    private String statut;

    @ApiModelProperty(notes = "Client qui envoit la commande")
    @OneToOne
    @JoinColumn(name = "client", foreignKey = @ForeignKey(name = "fk_operation_client"))
    private Client client;

    @ApiModelProperty(notes = "Client qui envoit la commande")
    @OneToOne
    @JoinColumn(name = "client_personnel", foreignKey = @ForeignKey(name = "fk_operation_client_personnel"))
    private UtilisateurClientPersonnel client_personnel;

    @ApiModelProperty(notes = "Opérateur qui gère l'opération")
    @OneToOne
    @JoinColumn(name = "operateur", foreignKey = @ForeignKey(name = "fk_operation_operateur"))
    private UtilisateurOperateurKamtar operateur;

    @ApiModelProperty(notes = "Date programmée pour le retrait de la marchandise chez le client")
    @Column(updatable = true, name = "departDateProgrammeeOperation", nullable = true, unique = false)
    private Date departDateProgrammeeOperation;

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

    @ApiModelProperty(notes = "Date programmée pour l'arrivée de la marchandise chez le destinataire")
    @Column(updatable = true, name = "arriveeDateProgrammeeOperation", nullable = true, unique = false)
    private Date arriveeDateProgrammeeOperation;

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

    @ApiModelProperty(notes = "Caractéristiques du véhicules saisies par le client", allowEmptyValue = true, required = false)
    private String caracteristiquesVehicule;

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

    @ApiModelProperty(notes = "Transporteur à qui l'opération a été affectée")
    @OneToOne
    @JoinColumn(name = "transporteur", foreignKey = @ForeignKey(name = "fk_operation_transporteur"))
    private UtilisateurDriver transporteur;

    @ApiModelProperty(notes = "Véhoicule à qui l'opération a été affectée")
    @OneToOne
    @JoinColumn(name = "vehicule", foreignKey = @ForeignKey(name = "fk_operation_vehicule"))
    private Vehicule vehicule;

    @ApiModelProperty(notes = "Prix à payer par le client")
    @Column(updatable = true, name = "prixAPayerParClient", nullable = true, unique = false)
    private Double prixAPayerParClient;

    @ApiModelProperty(notes = "Devise du prix à payer par le client")
    @Column(updatable = true, name = "prixAPayerParClientDevise", nullable = true, length = 20, unique = false)
    private String prixAPayerParClientDevise;

    @ApiModelProperty(notes = "Prix demandé par le driver")
    @Column(updatable = true, name = "prixDemandeParDriver", nullable = true, unique = false)
    private Double prixDemandeParDriver;

    @ApiModelProperty(notes = "Devise du prix demandé par le driver")
    @Column(updatable = true, name = "prixDemandeParDriverDevise", nullable = true, length = 20, unique = false)
    private String prixDemandeParDriverDevise;

    @ApiModelProperty(notes = "Assurance saisies par Kamtar")
    @Column(updatable = true, name = "assurance", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String assurance;

    @ApiModelProperty(notes = "Liste des services additionnels saisies par Kamtar")
    @Column(updatable = true, name = "servicesAdditionnels", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String servicesAdditionnels;

    @ApiModelProperty(notes = "Informations compleemntaires saisies par Kamtar")
    @Column(updatable = true, name = "informationsComplementaires", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String informationsComplementaires;

    @ApiModelProperty(notes = "Avance donnée au driver")
    @Column(updatable = true, name = "avanceDonneAuDriver", nullable = true, unique = false)
    private Double avanceDonneAuDriver;

    @ApiModelProperty(notes = "Devise de l'avance donnée au driver")
    @Column(updatable = true, name = "avanceDonneAuDriverDevise", nullable = true, unique = false)
    private String avanceDonneAuDriverDevise;

    @ApiModelProperty(notes = "Montant pour le superviseur")
    @Column(updatable = true, name = "superviseur", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String superviseur;

    @ApiModelProperty(notes = "Devise du montant versé au superviseur")
    @Column(updatable = true, name = "superviseurDevise", nullable = true, unique = false)
    private String superviseurDevise;

    @ApiModelProperty(notes = "Obeservations saisies par le driver")
    @Column(updatable = true, name = "observationsParTransporteur", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String observationsParTransporteur;

    @ApiModelProperty(notes = "Créé par O = opérateur, C = client, D = via devis")
    @Column(updatable = true, name = "creePar", nullable = true, unique = false)
    private String creePar;

    @ApiModelProperty(notes = "Est ce que la course est géolocalisée ?")
    @Column(name = "courseGeolocalisee", nullable = false, updatable = true)
    protected boolean courseGeolocalisee;

    @ApiModelProperty(notes = "Est ce que les statuts évoluent en temp réel ?")
    @Column(name = "statutTempsReel", nullable = false, updatable = true)
    protected boolean statutTempsReel;

    @ApiModelProperty(notes = "Est ce que la commande a été validé par l'opérateur")
    @Column(updatable = true, name = "valideParOperateur", nullable = true, unique = false)
    private boolean valideParOperateur = false;

    @ApiModelProperty(notes = "Date à laquelle l'opérateur a validé l'opération")
    @Column(updatable = true, name = "dateValideParOperateur", nullable = true, unique = false)
    private Date dateValideParOperateur;

    @ApiModelProperty(notes = "Date à laquelle l'opération est arrivée chez le destinataire")
    @Column(updatable = true, name = "arriveeDateOperation", nullable = true, unique = false)
    private Date arriveeDateOperation;

    @ApiModelProperty(notes = "Date à laquelle le driver est parti en direction de chez le client pour charger")
    @Column(updatable = true, name = "departDateOperation", nullable = true, unique = false)
    private Date departDateOperation;

    @ApiModelProperty(notes = "Date à laquelle le driver est arrivé chez le client pour charger")
    @Column(updatable = true, name = "dateHeureArriveeChezClientPourCharger", nullable = true, unique = false)
    private Date dateHeureArriveeChezClientPourCharger;

    @ApiModelProperty(notes = "Date à laquelle le driver a commencé son chargement")
    @Column(updatable = true, name = "dateHeureChargementCommence", nullable = true, unique = false)
    private Date dateHeureChargementCommence;

    @ApiModelProperty(notes = "Date à laquelle le driver a terminé son chargemet")
    @Column(updatable = true, name = "dateHeureChargementTermine", nullable = true, unique = false)
    private Date dateHeureChargementTermine;

    @ApiModelProperty(notes = "Date à laquelle le driver a commencé à se rendre sur sa destination")
    @Column(updatable = true, name = "dateHeureEnRouteVersDestination", nullable = true, unique = false)
    private Date dateHeureEnRouteVersDestination;

    @ApiModelProperty(notes = "Date à laquelle le driver est arrivé chez le destinataire pour décharger")
    @Column(updatable = true, name = "dateHeureArriveADestination", nullable = true, unique = false)
    private Date dateHeureArriveADestination;

    @ApiModelProperty(notes = "Date à laquelle le déchargement a commencé")
    @Column(updatable = true, name = "dateHeureDechargementCommence", nullable = true, unique = false)
    private Date dateHeureDechargementCommence;

    @ApiModelProperty(notes = "Date à laquelle le déchargement a été terminé")
    @Column(updatable = true, name = "dateHeureDechargementTermine", nullable = true, unique = false)
    private Date dateHeureDechargementTermine;

    @ApiModelProperty(notes = "Date de paiement de l'avance au driver")
    @Column(updatable = true, name = "datePaiementAvanceDriver", nullable = true, unique = false)
    private Date datePaiementAvanceDriver;

    @ApiModelProperty(notes = "Date de paiement du solde au driver")
    @Column(updatable = true, name = "datePaiementSoldeDriver", nullable = true, unique = false)
    private Date datePaiementSoldeDriver;

    @ApiModelProperty(notes = "Satisfaction du client (1=mauvaise, 3=excellent)")
    @Column(updatable = true, name = "satisfactionClient", nullable = true, unique = false)
    private Integer satisfactionClient;

    @ApiModelProperty(notes = "Satisfaction du driver (1=mauvaise, 3=excellent)")
    @Column(updatable = true, name = "satisfactionDriver", nullable = true, unique = false)
    private Integer satisfactionDriver;

    @ApiModelProperty(notes = "UUID de la facture client")
    @Column(updatable = true, name = "facture", nullable = true, length = 250, unique = false)
    private String facture;

    @ApiModelProperty(notes = "UUID de la facture propriétaire")
    @Column(updatable = true, name = "factureProprietaire", nullable = true, length = 250, unique = false)
    private String factureProprietaire;
/*
    @ApiModelProperty(notes = "Nom du destinataire")
    @Column(updatable = true, name = "adresse1DestinataireNom", nullable = true, length = 250, unique = false)
    private String adresse1DestinataireNom;

    @ApiModelProperty(notes = "Numéro de téléphone du destinataire")
    @Column(updatable = true, name = "adresse1DestinataireTelephone", nullable = true, length = 250, unique = false)
    private String adresse1DestinataireTelephone;

    @ApiModelProperty(notes = "Latitude GPS du point de départ")
    @Column(updatable = true, name = "adresse1Latitude", nullable = true, unique = false)
    private Double adresse1Latitude;

    @ApiModelProperty(notes = "Longitude GPS du point de départ")
    @Column(updatable = true, name = "adresse1Longitude", nullable = true, unique = false)
    private Double adresse1Longitude;

    @ApiModelProperty(notes = "Adresse du point de départ")
    @Column(updatable = true, name = "adresse1Complete", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String adresse1Complete;

    @ApiModelProperty(notes = "Complément d'adresse du point de départ")
    @Column(updatable = true, name = "adresse1Complement", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String adresse1Complement;

    @ApiModelProperty(notes = "Pays du point de départ")
    @Column(updatable = true, name = "adresse1CountryCode", nullable = true, length = 200, unique = false)
    private String adresse1CountryCode;

    @ApiModelProperty(notes = "Ville du point de départ")
    @Column(updatable = true, name = "adresse1Ville", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String adresse1Ville;

    @ApiModelProperty(notes = "Rue du point de départ")
    @Column(updatable = true, name = "adresse1Rue", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String adresse1Rue;

    @ApiModelProperty(notes = "Nom du destinataire")
    @Column(updatable = true, name = "adresse2DestinataireNom", nullable = true, length = 250, unique = false)
    private String adresse2DestinataireNom;

    @ApiModelProperty(notes = "Numéro de téléphone du destinataire")
    @Column(updatable = true, name = "adresse2DestinataireTelephone", nullable = true, length = 250, unique = false)
    private String adresse2DestinataireTelephone;

    @ApiModelProperty(notes = "Latitude GPS du point de départ")
    @Column(updatable = true, name = "adresse2Latitude", nullable = true, unique = false)
    private Double adresse2Latitude;

    @ApiModelProperty(notes = "Longitude GPS du point de départ")
    @Column(updatable = true, name = "adresse2Longitude", nullable = true, unique = false)
    private Double adresse2Longitude;

    @ApiModelProperty(notes = "Adresse du point de départ")
    @Column(updatable = true, name = "adresse2Complete", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String adresse2Complete;

    @ApiModelProperty(notes = "Complément d'adresse du point de départ")
    @Column(updatable = true, name = "adresse2Complement", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String adresse2Complement;

    @ApiModelProperty(notes = "Pays du point de départ")
    @Column(updatable = true, name = "adresse2CountryCode", nullable = true, length = 200, unique = false)
    private String adresse2CountryCode;

    @ApiModelProperty(notes = "Ville du point de départ")
    @Column(updatable = true, name = "adresse2Ville", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String adresse2Ville;

    @ApiModelProperty(notes = "Rue du point de départ")
    @Column(updatable = true, name = "adresse2Rue", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String adresse2Rue;

    @ApiModelProperty(notes = "Nom du destinataire")
    @Column(updatable = true, name = "adresse3DestinataireNom", nullable = true, length = 250, unique = false)
    private String adresse3DestinataireNom;

    @ApiModelProperty(notes = "Numéro de téléphone du destinataire")
    @Column(updatable = true, name = "adresse3DestinataireTelephone", nullable = true, length = 250, unique = false)
    private String adresse3DestinataireTelephone;

    @ApiModelProperty(notes = "Latitude GPS du point de départ")
    @Column(updatable = true, name = "adresse3Latitude", nullable = true, unique = false)
    private Double adresse3Latitude;

    @ApiModelProperty(notes = "Longitude GPS du point de départ")
    @Column(updatable = true, name = "adresse3Longitude", nullable = true, unique = false)
    private Double adresse3Longitude;

    @ApiModelProperty(notes = "Adresse du point de départ")
    @Column(updatable = true, name = "adresse3Complete", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String adresse3Complete;

    @ApiModelProperty(notes = "Complément d'adresse du point de départ")
    @Column(updatable = true, name = "adresse3Complement", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String adresse3Complement;

    @ApiModelProperty(notes = "Pays du point de départ")
    @Column(updatable = true, name = "adresse3CountryCode", nullable = true, length = 200, unique = false)
    private String adresse3CountryCode;

    @ApiModelProperty(notes = "Ville du point de départ")
    @Column(updatable = true, name = "adresse3Ville", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String adresse3Ville;

    @ApiModelProperty(notes = "Rue du point de départ")
    @Column(updatable = true, name = "adresse3Rue", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String adresse3Rue;

    @ApiModelProperty(notes = "Nom du destinataire")
    @Column(updatable = true, name = "adresse4DestinataireNom", nullable = true, length = 250, unique = false)
    private String adresse4DestinataireNom;

    @ApiModelProperty(notes = "Numéro de téléphone du destinataire")
    @Column(updatable = true, name = "adresse4DestinataireTelephone", nullable = true, length = 250, unique = false)
    private String adresse4DestinataireTelephone;

    @ApiModelProperty(notes = "Latitude GPS du point de départ")
    @Column(updatable = true, name = "adresse4Latitude", nullable = true, unique = false)
    private Double adresse4Latitude;

    @ApiModelProperty(notes = "Longitude GPS du point de départ")
    @Column(updatable = true, name = "adresse4Longitude", nullable = true, unique = false)
    private Double adresse4Longitude;

    @ApiModelProperty(notes = "Adresse du point de départ")
    @Column(updatable = true, name = "adresse4Complete", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String adresse4Complete;

    @ApiModelProperty(notes = "Complément d'adresse du point de départ")
    @Column(updatable = true, name = "adresse4Complement", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String adresse4Complement;

    @ApiModelProperty(notes = "Pays du point de départ")
    @Column(updatable = true, name = "adresse4CountryCode", nullable = true, length = 200, unique = false)
    private String adresse4CountryCode;

    @ApiModelProperty(notes = "Ville du point de départ")
    @Column(updatable = true, name = "adresse4Ville", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String adresse4Ville;

    @ApiModelProperty(notes = "Rue du point de départ")
    @Column(updatable = true, name = "adresse4Rue", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String adresse4Rue;

    @ApiModelProperty(notes = "Nom du destinataire")
    @Column(updatable = true, name = "adresse5DestinataireNom", nullable = true, length = 250, unique = false)
    private String adresse5DestinataireNom;

    @ApiModelProperty(notes = "Numéro de téléphone du destinataire")
    @Column(updatable = true, name = "adresse5DestinataireTelephone", nullable = true, length = 250, unique = false)
    private String adresse5DestinataireTelephone;

    @ApiModelProperty(notes = "Latitude GPS du point de départ")
    @Column(updatable = true, name = "adresse5Latitude", nullable = true, unique = false)
    private Double adresse5Latitude;

    @ApiModelProperty(notes = "Longitude GPS du point de départ")
    @Column(updatable = true, name = "adresse5Longitude", nullable = true, unique = false)
    private Double adresse5Longitude;

    @ApiModelProperty(notes = "Adresse du point de départ")
    @Column(updatable = true, name = "adresse5Complete", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String adresse5Complete;

    @ApiModelProperty(notes = "Complément d'adresse du point de départ")
    @Column(updatable = true, name = "adresse5Complement", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String adresse5Complement;

    @ApiModelProperty(notes = "Pays du point de départ")
    @Column(updatable = true, name = "adresse5CountryCode", nullable = true, length = 200, unique = false)
    private String adresse5CountryCode;

    @ApiModelProperty(notes = "Ville du point de départ")
    @Column(updatable = true, name = "adresse5Ville", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String adresse5Ville;

    @ApiModelProperty(notes = "Rue du point de départ")
    @Column(updatable = true, name = "adresse5Rue", nullable = true, columnDefinition = "TEXT", length = 2500, unique = false)
    private String adresse5Rue;
    */


    @ApiModelProperty(notes = "Code du pays")
    @Column(updatable = false, name = "codePays", nullable = true, unique = false)
    private String codePays;

    @ApiModelProperty(notes = "Type d'opération (pour la TVA)")
    @Column(updatable = true, name = "typeOperationTVA", columnDefinition = "TEXT", nullable = true, length = 100, unique = false)
    private String typeOperationTVA;


    @ApiModelProperty(notes = "Motif de l'annulation")
    @Column(updatable = true, name = "annulationMotif", nullable = true, unique = false)
    private Integer annulationMotif;

    @ApiModelProperty(notes = "Description de l'annulation")
    @Column(updatable = true, name = "annulationDescription", nullable = true, unique = false)
    private String annulationDescription;

    @ApiModelProperty(notes = "Entité à l'initative de l'annulation (0=Kamtar - 1=Client - 2=Transporteur")
    @Column(updatable = true, name = "annulationEntite", nullable = true, unique = false)
    private Integer annulationEntite;

    @ApiModelProperty(notes = "Date de l'annulation")
    @Column(updatable = true, name = "annulationDate", nullable = true, unique = false)
    private Date annulationDate;

    @ApiModelProperty(notes = "Date de la soumission l'annulation")
    @Column(updatable = true, name = "annulationSoumission", nullable = true, unique = false)
    private Date annulationSoumission;

    /*@ApiModelProperty(notes = "Distance du trajet")
    @Column(updatable = true, name = "distanceTrajet", nullable = true, unique = false)
    private String distanceTrajet;*/

    @ApiModelProperty(notes = "Distance du trajet (en km)")
    @Column(updatable = true, name = "distanceTrajetKm", nullable = true, unique = false)
    private Long distanceTrajetKm;

    /*@ApiModelProperty(notes = "Durée prévue du trajet")
    @Column(updatable = true, name = "dureePrevueTrajet", nullable = true, unique = false)
    private String dureePrevueTrajet;*/

    @ApiModelProperty(notes = "Durée prévue du trajet (en minutes)")
    @Column(updatable = true, name = "dureePrevueTrajetMin", nullable = true, unique = false)
    private Long dureePrevueTrajetMin;

    @ApiModelProperty(notes = "Date de dernière mise à jour prévue du trajet")
    @Column(updatable = true, name = "dureePrevueTrajetDerniereMiseAJour", nullable = true, unique = false)
    private Date dureePrevueTrajetDerniereMiseAJour;

    @ApiModelProperty(notes = "Météo prévue au départ")
    @Column(updatable = true, name = "meteoPrevueDepart", nullable = true, unique = false)
    private String meteoPrevueDepart;

    @ApiModelProperty(notes = "Météo prévue au départ")
    @Column(updatable = true, name = "meteoPrevueIDDepart", nullable = true, unique = false)
    private String meteoPrevueIDDepart;

    @ApiModelProperty(notes = "Icone de la météo prévue au départ")
    @Column(updatable = true, name = "meteoPrevueIconeDepart", nullable = true, unique = false)
    private String meteoPrevueIconeDepart;

    @ApiModelProperty(notes = "Météo prévue à l'arrivée")
    @Column(updatable = true, name = "meteoPrevueArrivee", nullable = true, unique = false)
    private String meteoPrevueArrivee;

    @ApiModelProperty(notes = "Météo prévue à l'arrivé")
    @Column(updatable = true, name = "meteoPrevueIDArrivee", nullable = true, unique = false)
    private String meteoPrevueIDArrivee;

    @ApiModelProperty(notes = "Icone de la météo prévue à l'arrivée")
    @Column(updatable = true, name = "meteoPrevueIconeArrivee", nullable = true, unique = false)
    private String meteoPrevueIconeArrivee;

    @ApiModelProperty(notes = "Date de la mise à jour de la météo prévue")
    @Column(updatable = true, name = "meteoPrevueDerniereMiseAJour", nullable = true, unique = false)
    private Date meteoPrevueDerniereMiseAJour;

    @ApiModelProperty(notes = "Latitude de la position du véhicule au moment du déchargement du véhicule")
    @Column(updatable = true, name = "dechargementTermineLatitude", nullable = true, unique = false)
    private Double dechargementTermineLatitude;

    @ApiModelProperty(notes = "Longitude de la position du véhicule au moment du déchargement du véhicule")
    @Column(updatable = true, name = "dechargementTermineLongitude", nullable = true, unique = false)
    private Double dechargementTermineLongitude;

    @ApiModelProperty(notes = "Date de la proposition de prix au client")
    @Column(updatable = true, name = "prixProposeAuClientDate", nullable = true, unique = false)
    private Date prixProposeAuClientDate;

    @ApiModelProperty(notes = "Récurrence (au format rrule)")
    @Column(updatable = true, name = "recurrenceRrule", nullable = true, length = 250, unique = false)
    private String recurrenceRrule;

    @ApiModelProperty(notes = "Date de début de la récurrence")
    @Column(updatable = true, name = "recurrenceDebut", nullable = true, unique = false)
    private Date recurrenceDebut;

    @ApiModelProperty(notes = "Date de fin de la récurrence")
    @Column(updatable = true, name = "recurrenceFin", nullable = true, unique = false)
    private Date recurrenceFin;

    @ApiModelProperty(notes = "Prochaine date issue de la rrule de récurrence")
    @Column(updatable = true, name = "recurrenceProchain", nullable = true, unique = false)
    private Date recurrenceProchain;

    @ApiModelProperty(notes = "Opération originel de cette opération récurrente")
    @OneToOne
    @JoinColumn(name = "recurrenceOperationOriginel", foreignKey = @ForeignKey(name = "fk_operation_operation"))
    private Operation recurrenceOperationOriginel;

    @Transient
    @ApiModelProperty(notes = "Nombre de documents attachés à chaque opération")
    private Long nbDocuments;

    @ApiModelProperty(notes = "Code du devis  à partir duquel l'opération a été créé")
    @Column(updatable = true, name = "devis", nullable = true, unique = false)
    private Long devis;

    @ApiModelProperty(notes = "Nombre de camions réservés (utilisé dans les opérations récurrentes)")
    @Column(updatable = true, name = "nbCamions", nullable = true, unique = false)
    private Integer nbCamions;

    @ApiModelProperty(notes = "Liste des étapes")
    @OneToMany(
            targetEntity=Etape.class,
            cascade = CascadeType.DETACH,
            fetch = FetchType.EAGER
    )
    @ElementCollection
    @OrderBy("position ASC")
    private List<Etape> etapes;

    public Date getRecurrenceFin() {
        return recurrenceFin;
    }

    public void setRecurrenceFin(Date recurrenceFin) {
        this.recurrenceFin = recurrenceFin;
    }

    public List<Etape> getEtapes() {
        return etapes;
    }

    public void setEtapes(List<Etape> etapes) {
        this.etapes = etapes;
    }

    public Integer getNbCamions() {
        return nbCamions;
    }

    public void setNbCamions(Integer nbCamions) {
        this.nbCamions = nbCamions;
    }

    public Long getDevis() {
        return devis;
    }

    public void setDevis(Long devis) {
        this.devis = devis;
    }

    public Operation getRecurrenceOperationOriginel() {
        return recurrenceOperationOriginel;
    }

    public void setRecurrenceOperationOriginel(Operation recurrenceOperationOriginel) {
        this.recurrenceOperationOriginel = recurrenceOperationOriginel;
    }

    public Date getRecurrenceProchain() {
        return recurrenceProchain;
    }

    public void setRecurrenceProchain(Date recurrenceProchain) {
        this.recurrenceProchain = recurrenceProchain;
    }

    public String getRecurrenceRrule() {
        return recurrenceRrule;
    }

    public void setRecurrenceRrule(String recurrenceRrule) {
        this.recurrenceRrule = recurrenceRrule;
    }

    public Date getRecurrenceDebut() {
        return recurrenceDebut;
    }

    public void setRecurrenceDebut(Date recurrenceDebut) {
        this.recurrenceDebut = recurrenceDebut;
    }

    public Long getNbDocuments() {
        return nbDocuments;
    }

    public void setNbDocuments(Long nbDocuments) {
        this.nbDocuments = nbDocuments;
    }

    public String getSuperviseurDevise() {
        return superviseurDevise;
    }

    public void setSuperviseurDevise(String superviseurDevise) {
        this.superviseurDevise = superviseurDevise;
    }

    public Date getPrixProposeAuClientDate() {
        return prixProposeAuClientDate;
    }

    public void setPrixProposeAuClientDate(Date prixProposeAuClientDate) {
        this.prixProposeAuClientDate = prixProposeAuClientDate;
    }

    public Double getDechargementTermineLatitude() {
        return dechargementTermineLatitude;
    }

    public void setDechargementTermineLatitude(Double dechargementTermineLatitude) {
        this.dechargementTermineLatitude = dechargementTermineLatitude;
    }

    public Double getDechargementTermineLongitude() {
        return dechargementTermineLongitude;
    }

    public void setDechargementTermineLongitude(Double dechargementTermineLongitude) {
        this.dechargementTermineLongitude = dechargementTermineLongitude;
    }

    public Long getDistanceTrajetKm() {
        return distanceTrajetKm;
    }

    public void setDistanceTrajetKm(Long distanceTrajetKm) {
        this.distanceTrajetKm = distanceTrajetKm;
    }

    public Long getDureePrevueTrajetMin() {
        return dureePrevueTrajetMin;
    }

    public void setDureePrevueTrajetMin(Long dureePrevueTrajetMin) {
        this.dureePrevueTrajetMin = dureePrevueTrajetMin;
    }

    public String getMeteoPrevueIDDepart() {
        return meteoPrevueIDDepart;
    }

    public void setMeteoPrevueIDDepart(String meteoPrevueIDDepart) {
        this.meteoPrevueIDDepart = meteoPrevueIDDepart;
    }

    public String getMeteoPrevueIDArrivee() {
        return meteoPrevueIDArrivee;
    }

    public void setMeteoPrevueIDArrivee(String meteoPrevueIDArrivee) {
        this.meteoPrevueIDArrivee = meteoPrevueIDArrivee;
    }

    public String getMeteoPrevueIconeDepart() {
        return meteoPrevueIconeDepart;
    }

    public void setMeteoPrevueIconeDepart(String meteoPrevueIconeDepart) {
        this.meteoPrevueIconeDepart = meteoPrevueIconeDepart;
    }

    public String getMeteoPrevueIconeArrivee() {
        return meteoPrevueIconeArrivee;
    }

    public void setMeteoPrevueIconeArrivee(String meteoPrevueIconeArrivee) {
        this.meteoPrevueIconeArrivee = meteoPrevueIconeArrivee;
    }

    public String getMeteoPrevueDepart() {
        return meteoPrevueDepart;
    }

    public void setMeteoPrevueDepart(String meteoPrevueDepart) {
        this.meteoPrevueDepart = meteoPrevueDepart;
    }

    public String getMeteoPrevueArrivee() {
        return meteoPrevueArrivee;
    }

    public void setMeteoPrevueArrivee(String meteoPrevueArrivee) {
        this.meteoPrevueArrivee = meteoPrevueArrivee;
    }

    public Date getMeteoPrevueDerniereMiseAJour() {
        return meteoPrevueDerniereMiseAJour;
    }

    public void setMeteoPrevueDerniereMiseAJour(Date meteoPrevueDerniereMiseAJour) {
        this.meteoPrevueDerniereMiseAJour = meteoPrevueDerniereMiseAJour;
    }

   /* public String getDistanceTrajet() {
        return distanceTrajet;
    }

    public void setDistanceTrajet(String distanceTrajet) {
        this.distanceTrajet = distanceTrajet;
    }

    public String getDureePrevueTrajet() {
        return dureePrevueTrajet;
    }

    public void setDureePrevueTrajet(String dureePrevueTrajet) {
        this.dureePrevueTrajet = dureePrevueTrajet;
    }*/

    public UtilisateurClientPersonnel getClient_personnel() {
        return client_personnel;
    }

    public void setClient_personnel(UtilisateurClientPersonnel client_personnel) {
        this.client_personnel = client_personnel;
    }

    public Date getDureePrevueTrajetDerniereMiseAJour() {
        return dureePrevueTrajetDerniereMiseAJour;
    }

    public void setDureePrevueTrajetDerniereMiseAJour(Date dureePrevueTrajetDerniereMiseAJour) {
        this.dureePrevueTrajetDerniereMiseAJour = dureePrevueTrajetDerniereMiseAJour;
    }

    public Date getAnnulationSoumission() {
        return annulationSoumission;
    }

    public void setAnnulationSoumission(Date annulationSoumission) {
        this.annulationSoumission = annulationSoumission;
    }

    public Integer getAnnulationMotif() {
        return annulationMotif;
    }

    public void setAnnulationMotif(Integer annulationMotif) {
        this.annulationMotif = annulationMotif;
    }

    public String getAnnulationDescription() {
        return annulationDescription;
    }

    public void setAnnulationDescription(String annulationDescription) {
        this.annulationDescription = annulationDescription;
    }

    public Integer getAnnulationEntite() {
        return annulationEntite;
    }

    public void setAnnulationEntite(Integer annulationEntite) {
        this.annulationEntite = annulationEntite;
    }

    public Date getAnnulationDate() {
        return annulationDate;
    }

    public void setAnnulationDate(Date annulationDate) {
        this.annulationDate = annulationDate;
    }

    public String getTypeOperationTVA() {
        return typeOperationTVA;
    }

    public void setTypeOperationTVA(String typeOperationTVA) {
        this.typeOperationTVA = typeOperationTVA;
    }

    public String getCodePays() {
        return codePays;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
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
    public String getFactureProprietaire() {
        return factureProprietaire;
    }

    public void setFactureProprietaire(String factureProprietaire) {
        this.factureProprietaire = factureProprietaire;
    }

    public String getFacture() {
        return facture;
    }

    public void setFacture(String facture) {
        this.facture = facture;
    }

    public Integer getSatisfactionClient() {
        return satisfactionClient;
    }

    public void setSatisfactionClient(Integer satisfactionClient) {
        this.satisfactionClient = satisfactionClient;
    }

    public Integer getSatisfactionDriver() {
        return satisfactionDriver;
    }

    public void setSatisfactionDriver(Integer satisfactionDriver) {
        this.satisfactionDriver = satisfactionDriver;
    }

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

    public VehiculeTonnage getTonnageVehicule() {
        return tonnageVehicule;
    }

    public void setTonnageVehicule(VehiculeTonnage tonnageVehicule) {
        this.tonnageVehicule = tonnageVehicule;
    }

    public Date getDateHeureArriveeChezClientPourCharger() {
        return dateHeureArriveeChezClientPourCharger;
    }

    public void setDateHeureArriveeChezClientPourCharger(Date dateHeureArriveeChezClientPourCharger) {
        this.dateHeureArriveeChezClientPourCharger = dateHeureArriveeChezClientPourCharger;
    }

    public Date getDateHeureChargementCommence() {
        return dateHeureChargementCommence;
    }

    public void setDateHeureChargementCommence(Date dateHeureChargementCommence) {
        this.dateHeureChargementCommence = dateHeureChargementCommence;
    }

    public Date getDateHeureChargementTermine() {
        return dateHeureChargementTermine;
    }

    public void setDateHeureChargementTermine(Date dateHeureChargementTermine) {
        this.dateHeureChargementTermine = dateHeureChargementTermine;
    }

    public Date getDateHeureEnRouteVersDestination() {
        return dateHeureEnRouteVersDestination;
    }

    public void setDateHeureEnRouteVersDestination(Date dateHeureEnRouteVersDestination) {
        this.dateHeureEnRouteVersDestination = dateHeureEnRouteVersDestination;
    }

    public Date getDateHeureArriveADestination() {
        return dateHeureArriveADestination;
    }

    public void setDateHeureArriveADestination(Date dateHeureArriveADestination) {
        this.dateHeureArriveADestination = dateHeureArriveADestination;
    }

    public Date getDateHeureDechargementCommence() {
        return dateHeureDechargementCommence;
    }

    public void setDateHeureDechargementCommence(Date dateHeureDechargementCommence) {
        this.dateHeureDechargementCommence = dateHeureDechargementCommence;
    }

    public Date getDateHeureDechargementTermine() {
        return dateHeureDechargementTermine;
    }

    public void setDateHeureDechargementTermine(Date dateHeureDechargementTermine) {
        this.dateHeureDechargementTermine = dateHeureDechargementTermine;
    }

    public Date getDepartDateOperation() {
        return departDateOperation;
    }

    public void setDepartDateOperation(Date departDateOperation) {
        this.departDateOperation = departDateOperation;
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

    public UtilisateurOperateurKamtar getOperateur() {
        return operateur;
    }

    public void setOperateur(UtilisateurOperateurKamtar operateur) {
        this.operateur = operateur;
    }

    public boolean isValideParOperateur() {
        return valideParOperateur;
    }

    public void setValideParOperateur(boolean valideParOperateur) {
        this.valideParOperateur = valideParOperateur;
    }

    public Date getDateValideParOperateur() {
        return dateValideParOperateur;
    }

    public void setDateValideParOperateur(Date dateValideParOperateur) {
        this.dateValideParOperateur = dateValideParOperateur;
    }


    public Operation(Devis devis, Long code, Client client, String statut) {

        super();
        this.devis = devis.getCode();
        this.arriveeAdresseComplement = devis.getArriveeAdresseComplement();
        this.arriveeAdresseComplete = devis.getArriveeAdresseComplete();
        this.arriveeAdresseCountryCode = devis.getArriveeAdresseCountryCode();
        this.arriveeAdresseLatitude = devis.getArriveeAdresseLatitude();
        this.arriveeAdresseLongitude = devis.getArriveeAdresseLongitude();
        this.arriveeAdresseRue = devis.getArriveeAdresseRue();
        this.arriveeAdresseVille = devis.getArriveeAdresseVille();
        this.tonnageVehicule = devis.getTonnageVehicule();
        this.categorieVehicule = devis.getCategorieVehicule();

        this.client = client;

        this.code = code;

        this.createdOn = new Date();
        this.creePar = "D"; // D = devis

        this.departAdresseComplement = devis.getDepartAdresseComplement();
        this.departAdresseComplete = devis.getDepartAdresseComplete();
        this.departAdresseCountryCode = devis.getDepartAdresseCountryCode();
        this.departAdresseLatitude = devis.getDepartAdresseLatitude();
        this.departAdresseLongitude = devis.getDepartAdresseLongitude();
        this.departAdresseRue = devis.getDepartAdresseRue();
        this.departAdresseVille = devis.getDepartAdresseVille();
        this.departDateProgrammeeOperation = devis.getDepartDateProgrammeeOperation();

        this.observationsParClient = devis.getObservationsParClient();
        this.typeMarchandise = devis.getTypeMarchandise();

        this.prixSouhaiteParClient = devis.getPrixSouhaiteParClient();
        this.prixSouhaiteParClientDevise = devis.getPrixSouhaiteParClientDevise();

        this.statut = devis.getStatut();

        this.updatedOn = new Date();


        this.statut = statut;

        this.arriveeDestinataireNom = devis.getArriveeDestinataireNom();
        this.arriveeDestinataireTelephone = devis.getArriveeDestinataireTelephone();

        this.etapes = new ArrayList<Etape>();
        int cpt = 1;
        for (Etape etape : devis.getEtapes()) {
            this.etapes.add(new Etape(etape, cpt));
            cpt++;
        }
/*
        //etape 1
        if (devis.getEtapes().size() >=1) {
            Etape etape = devis.getEtapes().get(0);
            this.adresse1DestinataireNom = etape.getAdresseDestinataireNom();
            this.adresse1DestinataireTelephone = etape.getAdresseDestinataireTelephone();
            this.adresse1Complement = etape.getAdresseComplement();
            this.adresse1Complete = etape.getAdresseComplete();
            this.adresse1CountryCode = etape.getAdresseCountryCode();
            this.adresse1Latitude = etape.getAdresseLatitude();
            this.adresse1Longitude = etape.getAdresseLongitude();
            this.adresse1Rue = etape.getAdresseRue();
            this.adresse1Ville = etape.getAdresseVille();
        }


        //etape 2
        if (devis.getEtapes().size() >=2) {
            Etape etape = devis.getEtapes().get(1);
            this.adresse2DestinataireNom = etape.getAdresseDestinataireNom();
            this.adresse2DestinataireTelephone = etape.getAdresseDestinataireTelephone();
            this.adresse2Complement = etape.getAdresseComplement();
            this.adresse2Complete = etape.getAdresseComplete();
            this.adresse2CountryCode = etape.getAdresseCountryCode();
            this.adresse2Latitude = etape.getAdresseLatitude();
            this.adresse2Longitude = etape.getAdresseLongitude();
            this.adresse2Rue = etape.getAdresseRue();
            this.adresse2Ville = etape.getAdresseVille();
        }

        //etape 3
        if (devis.getEtapes().size() >=3) {
            Etape etape = devis.getEtapes().get(2);
            this.adresse3DestinataireNom = etape.getAdresseDestinataireNom();
            this.adresse3DestinataireTelephone = etape.getAdresseDestinataireTelephone();
            this.adresse3Complement = etape.getAdresseComplement();
            this.adresse3Complete = etape.getAdresseComplete();
            this.adresse3CountryCode = etape.getAdresseCountryCode();
            this.adresse3Latitude = etape.getAdresseLatitude();
            this.adresse3Longitude = etape.getAdresseLongitude();
            this.adresse3Rue = etape.getAdresseRue();
            this.adresse3Ville = etape.getAdresseVille();
        }

        //etape 4
        if (devis.getEtapes().size() >=4) {
            Etape etape = devis.getEtapes().get(3);
            this.adresse4DestinataireNom = etape.getAdresseDestinataireNom();
            this.adresse4DestinataireTelephone = etape.getAdresseDestinataireTelephone();
            this.adresse4Complement = etape.getAdresseComplement();
            this.adresse4Complete = etape.getAdresseComplete();
            this.adresse4CountryCode = etape.getAdresseCountryCode();
            this.adresse4Latitude = etape.getAdresseLatitude();
            this.adresse4Longitude = etape.getAdresseLongitude();
            this.adresse4Rue = etape.getAdresseRue();
            this.adresse4Ville = etape.getAdresseVille();
        }

        //etape 5
        if (devis.getEtapes().size() >=5) {
            Etape etape = devis.getEtapes().get(4);
            this.adresse5DestinataireNom = etape.getAdresseDestinataireNom();
            this.adresse5DestinataireTelephone = etape.getAdresseDestinataireTelephone();
            this.adresse5Complement = etape.getAdresseComplement();
            this.adresse5Complete = etape.getAdresseComplete();
            this.adresse5CountryCode = etape.getAdresseCountryCode();
            this.adresse5Latitude = etape.getAdresseLatitude();
            this.adresse5Longitude = etape.getAdresseLongitude();
            this.adresse5Rue = etape.getAdresseRue();
            this.adresse5Ville = etape.getAdresseVille();
        }
*/
        this.codePays = devis.getCodePays();
    }

    public Operation(CreateOperationParams params, Client client, UtilisateurClientPersonnel client_personnel, UtilisateurDriver transporteur,
                     UtilisateurOperateurKamtar operateur, Long code, Vehicule vehicule, MarchandiseType marchandise_type, VehiculeTonnage tonnage_obj) {
        super();
        this.arriveeAdresseComplement = params.getArriveeAdresseComplement();
        this.arriveeAdresseComplete = params.getArriveeAdresseComplete();
        this.arriveeAdresseCountryCode = params.getArriveeAdresseCountryCode();
        this.arriveeAdresseLatitude = params.getArriveeAdresseLatitude();
        this.arriveeAdresseLongitude = params.getArriveeAdresseLongitude();
        this.arriveeAdresseRue = params.getArriveeAdresseRue();
        this.arriveeAdresseVille = params.getArriveeAdresseVille();
        this.arriveeDateProgrammeeOperation = params.getArriveeDateProgrammeeOperation();
        this.tonnageVehicule = tonnage_obj;
        this.operateur = operateur;
        this.categorieVehicule = params.getCategorieVehicule();
        this.typeOperationTVA = params.getTypeOperationTVA();

        this.datePaiementAvanceDriver = params.getDatePaiementAvanceDriver();
        this.datePaiementSoldeDriver = params.getDatePaiementSoldeDriver();

        this.assurance = params.getAssurance();
        this.avanceDonneAuDriver = params.getAvanceDonneAuDriver();
        this.avanceDonneAuDriverDevise = params.getAvanceDonneAuDriverDevise();
        this.caracteristiquesVehicule = params.getCaracteristiquesVehicule();
        this.client = client;
        this.client_personnel = client_personnel;

        this.superviseurDevise = params.getSuperviseurDevise();
        this.code = code;

        this.courseGeolocalisee = params.isCourseGeolocalisee();

        this.createdOn = new Date();
        this.creePar = "O";

        this.departAdresseComplement = params.getDepartAdresseComplement();
        this.departAdresseComplete = params.getDepartAdresseComplete();
        this.departAdresseCountryCode = params.getDepartAdresseCountryCode();
        this.departAdresseLatitude = params.getDepartAdresseLatitude();
        this.departAdresseLongitude = params.getDepartAdresseLongitude();
        this.departAdresseRue = params.getDepartAdresseRue();
        this.departAdresseVille = params.getDepartAdresseVille();
        this.departDateProgrammeeOperation = params.getDepartDateProgrammeeOperation();

        this.informationsComplementaires = params.getInformationsComplementaires();
        this.observationsParClient = params.getObservationsParClient();
        this.observationsParTransporteur = params.getObservationsParTransporteur();

        this.prixAPayerParClient = params.getPrixAPayerParClient();
        this.prixAPayerParClientDevise = params.getPrixAPayerParClientDevise();

        this.prixDemandeParDriver = params.getPrixDemandeParDriver();
        this.prixDemandeParDriverDevise = params.getPrixDemandeParDriverDevise();

        this.prixSouhaiteParClient = params.getPrixSouhaiteParClient();
        this.prixSouhaiteParClientDevise = params.getPrixSouhaiteParClientDevise();

        this.servicesAdditionnels = params.getServicesAdditionnels();
        this.statut = params.getStatut();
        this.statutTempsReel = params.isStatutTempsReel();
        this.superviseur = params.getSuperviseur();

        this.transporteur = transporteur;

        this.typeMarchandise = marchandise_type;
        this.updatedOn = new Date();

        this.vehicule = vehicule;

        this.arriveeDestinataireNom = params.getArriveeDestinataireNom();
        this.arriveeDestinataireTelephone = params.getArriveeDestinataireTelephone();

        this.etapes = new ArrayList<Etape>();
        int cpt = 1;
        if (params.getEtapes() != null) {
            for (EtapeParams etape : params.getEtapes()) {
                logger.info("POST ajout étape " + etape.getAdresseComplete());
                this.etapes.add(new Etape(etape, cpt));
                cpt++;
            }
        }
/*
        //etape 1
        this.adresse1DestinataireNom = params.getAdresse1DestinataireNom();
        this.adresse1DestinataireTelephone = params.getAdresse1DestinataireTelephone();
        this.adresse1Complement = params.getAdresse1Complement();
        this.adresse1Complete = params.getAdresse1Complete();
        this.adresse1CountryCode = params.getAdresse1CountryCode();
        this.adresse1Latitude = params.getAdresse1Latitude();
        this.adresse1Longitude = params.getAdresse1Longitude();
        this.adresse1Rue = params.getAdresse1Rue();
        this.adresse1Ville = params.getAdresse1Ville();


        //etape 2
        this.adresse2DestinataireNom = params.getAdresse2DestinataireNom();
        this.adresse2DestinataireTelephone = params.getAdresse2DestinataireTelephone();
        this.adresse2Complement = params.getAdresse2Complement();
        this.adresse2Complete = params.getAdresse2Complete();
        this.adresse2CountryCode = params.getAdresse2CountryCode();
        this.adresse2Latitude = params.getAdresse2Latitude();
        this.adresse2Longitude = params.getAdresse2Longitude();
        this.adresse2Rue = params.getAdresse2Rue();
        this.adresse2Ville = params.getAdresse2Ville();

        //etape 3
        this.adresse3DestinataireNom = params.getAdresse3DestinataireNom();
        this.adresse3DestinataireTelephone = params.getAdresse3DestinataireTelephone();
        this.adresse3Complement = params.getAdresse3Complement();
        this.adresse3Complete = params.getAdresse3Complete();
        this.adresse3CountryCode = params.getAdresse3CountryCode();
        this.adresse3Latitude = params.getAdresse3Latitude();
        this.adresse3Longitude = params.getAdresse3Longitude();
        this.adresse3Rue = params.getAdresse3Rue();
        this.adresse3Ville = params.getAdresse3Ville();

        //etape 4
        this.adresse4DestinataireNom = params.getAdresse4DestinataireNom();
        this.adresse4DestinataireTelephone = params.getAdresse4DestinataireTelephone();
        this.adresse4Complement = params.getAdresse4Complement();
        this.adresse4Complete = params.getAdresse4Complete();
        this.adresse4CountryCode = params.getAdresse4CountryCode();
        this.adresse4Latitude = params.getAdresse4Latitude();
        this.adresse4Longitude = params.getAdresse4Longitude();
        this.adresse4Rue = params.getAdresse4Rue();
        this.adresse4Ville = params.getAdresse4Ville();

        //etape 5
        this.adresse5DestinataireNom = params.getAdresse5DestinataireNom();
        this.adresse5DestinataireTelephone = params.getAdresse5DestinataireTelephone();
        this.adresse5Complement = params.getAdresse5Complement();
        this.adresse5Complete = params.getAdresse5Complete();
        this.adresse5CountryCode = params.getAdresse5CountryCode();
        this.adresse5Latitude = params.getAdresse5Latitude();
        this.adresse5Longitude = params.getAdresse5Longitude();
        this.adresse5Rue = params.getAdresse5Rue();
        this.adresse5Ville = params.getAdresse5Ville();
*/
        this.codePays = client.getCodePays();

    }


    public void edit(EditOperationParams params, Client client, UtilisateurClientPersonnel client_personnel, UtilisateurDriver transporteur, Vehicule vehicule, MarchandiseType marchandise_type, VehiculeTonnage tonnage, boolean update_statut) {
        this.arriveeAdresseComplement = params.getArriveeAdresseComplement();
        this.arriveeAdresseComplete = params.getArriveeAdresseComplete();
        this.arriveeAdresseCountryCode = params.getArriveeAdresseCountryCode();
        this.arriveeAdresseLatitude = params.getArriveeAdresseLatitude();
        this.arriveeAdresseLongitude = params.getArriveeAdresseLongitude();
        this.arriveeAdresseRue = params.getArriveeAdresseRue();
        this.arriveeAdresseVille = params.getArriveeAdresseVille();
        this.arriveeDateProgrammeeOperation = params.getArriveeDateProgrammeeOperation();
        this.tonnageVehicule = tonnage;
        this.assurance = params.getAssurance();
        this.avanceDonneAuDriver = params.getAvanceDonneAuDriver();
        this.avanceDonneAuDriverDevise = params.getAvanceDonneAuDriverDevise();
        this.caracteristiquesVehicule = params.getCaracteristiquesVehicule();
        this.client = client;
        this.datePaiementAvanceDriver = params.getDatePaiementAvanceDriver();
        this.datePaiementSoldeDriver = params.getDatePaiementSoldeDriver();
        this.categorieVehicule = params.getCategorieVehicule();
        this.courseGeolocalisee = params.isCourseGeolocalisee();
        this.typeOperationTVA = params.getTypeOperationTVA();

        this.departAdresseComplement = params.getDepartAdresseComplement();
        this.departAdresseComplete = params.getDepartAdresseComplete();
        this.departAdresseCountryCode = params.getDepartAdresseCountryCode();
        this.departAdresseLatitude = params.getDepartAdresseLatitude();
        this.departAdresseLongitude = params.getDepartAdresseLongitude();
        this.departAdresseRue = params.getDepartAdresseRue();
        this.departAdresseVille = params.getDepartAdresseVille();
        this.departDateProgrammeeOperation = params.getDepartDateProgrammeeOperation();

        this.informationsComplementaires = params.getInformationsComplementaires();
        this.observationsParClient = params.getObservationsParClient();
        this.observationsParTransporteur = params.getObservationsParTransporteur();

        this.prixAPayerParClient = params.getPrixAPayerParClient();
        this.prixAPayerParClientDevise = params.getPrixAPayerParClientDevise();

        this.prixDemandeParDriver = params.getPrixDemandeParDriver();
        this.prixDemandeParDriverDevise = params.getPrixDemandeParDriverDevise();

        this.prixSouhaiteParClient = params.getPrixSouhaiteParClient();
        this.prixSouhaiteParClientDevise = params.getPrixSouhaiteParClientDevise();

        this.servicesAdditionnels = params.getServicesAdditionnels();
        this.client_personnel = client_personnel;

        this.superviseurDevise = params.getSuperviseurDevise();

        if (update_statut) {
            this.statut = params.getStatut();
        }
        this.statutTempsReel = params.isStatutTempsReel();
        this.superviseur = params.getSuperviseur();

        this.transporteur = transporteur;

        this.typeMarchandise = marchandise_type;

        this.vehicule = vehicule;

        this.arriveeDestinataireNom = params.getArriveeDestinataireNom();
        this.arriveeDestinataireTelephone = params.getArriveeDestinataireTelephone();
/*
        //etape 1
        this.adresse1DestinataireNom = params.getAdresse1DestinataireNom();
        this.adresse1DestinataireTelephone = params.getAdresse1DestinataireTelephone();
        this.adresse1Complement = params.getAdresse1Complement();
        this.adresse1Complete = params.getAdresse1Complete();
        this.adresse1CountryCode = params.getAdresse1CountryCode();
        this.adresse1Latitude = params.getAdresse1Latitude();
        this.adresse1Longitude = params.getAdresse1Longitude();
        this.adresse1Rue = params.getAdresse1Rue();
        this.adresse1Ville = params.getAdresse1Ville();


        //etape 2
        this.adresse2DestinataireNom = params.getAdresse2DestinataireNom();
        this.adresse2DestinataireTelephone = params.getAdresse2DestinataireTelephone();
        this.adresse2Complement = params.getAdresse2Complement();
        this.adresse2Complete = params.getAdresse2Complete();
        this.adresse2CountryCode = params.getAdresse2CountryCode();
        this.adresse2Latitude = params.getAdresse2Latitude();
        this.adresse2Longitude = params.getAdresse2Longitude();
        this.adresse2Rue = params.getAdresse2Rue();
        this.adresse2Ville = params.getAdresse2Ville();

        //etape 3
        this.adresse3DestinataireNom = params.getAdresse3DestinataireNom();
        this.adresse3DestinataireTelephone = params.getAdresse3DestinataireTelephone();
        this.adresse3Complement = params.getAdresse3Complement();
        this.adresse3Complete = params.getAdresse3Complete();
        this.adresse3CountryCode = params.getAdresse3CountryCode();
        this.adresse3Latitude = params.getAdresse3Latitude();
        this.adresse3Longitude = params.getAdresse3Longitude();
        this.adresse3Rue = params.getAdresse3Rue();
        this.adresse3Ville = params.getAdresse3Ville();

        //etape 4
        this.adresse4DestinataireNom = params.getAdresse4DestinataireNom();
        this.adresse4DestinataireTelephone = params.getAdresse4DestinataireTelephone();
        this.adresse4Complement = params.getAdresse4Complement();
        this.adresse4Complete = params.getAdresse4Complete();
        this.adresse4CountryCode = params.getAdresse4CountryCode();
        this.adresse4Latitude = params.getAdresse4Latitude();
        this.adresse4Longitude = params.getAdresse4Longitude();
        this.adresse4Rue = params.getAdresse4Rue();
        this.adresse4Ville = params.getAdresse4Ville();

        //etape 5
        this.adresse5DestinataireNom = params.getAdresse5DestinataireNom();
        this.adresse5DestinataireTelephone = params.getAdresse5DestinataireTelephone();
        this.adresse5Complement = params.getAdresse5Complement();
        this.adresse5Complete = params.getAdresse5Complete();
        this.adresse5CountryCode = params.getAdresse5CountryCode();
        this.adresse5Latitude = params.getAdresse5Latitude();
        this.adresse5Longitude = params.getAdresse5Longitude();
        this.adresse5Rue = params.getAdresse5Rue();
        this.adresse5Ville = params.getAdresse5Ville();
*/
        this.etapes = new ArrayList<Etape>();
        int cpt = 1;
        if (params.getEtapes() != null) {
            for (EtapeParams etape : params.getEtapes()) {
                logger.info("PUT ajout étape " + etape.getAdresseComplete());
                this.etapes.add(new Etape(etape, cpt));
                cpt++;
            }
        }

    }


    public Operation() {
        super();
    }

    public Operation(CreateOperationParClientParams params, Client client, UtilisateurClientPersonnel client_personnel, Long code, MarchandiseType marchandise_type, VehiculeTonnage tonnage, String recurrenceRrule, Date recurrenceProchain, Date recurrenceProchainSuivant, Date until_date) {
        super();

        this.nbCamions = params.getNbCamions();
        this.recurrenceFin = until_date;

        this.arriveeAdresseComplement = params.getArriveeAdresseComplement();
        this.arriveeAdresseComplete = params.getArriveeAdresseComplete();
        this.arriveeAdresseCountryCode = params.getArriveeAdresseCountryCode();
        this.arriveeAdresseLatitude = params.getArriveeAdresseLatitude();
        this.arriveeAdresseLongitude = params.getArriveeAdresseLongitude();
        this.arriveeAdresseRue = params.getArriveeAdresseRue();
        this.arriveeAdresseVille = params.getArriveeAdresseVille();
        this.arriveeDateProgrammeeOperation = params.getArriveeDateProgrammeeOperation();

        if (recurrenceProchain != null) {
            this.departDateProgrammeeOperation = recurrenceProchain;
        } else {
            this.departDateProgrammeeOperation = params.getDepartDateProgrammeeOperation();
        }
/*
        //etape 1
        this.adresse1Complement = params.getAdresse1Complement();
        this.adresse1Complete = params.getAdresse1Complete();
        this.adresse1CountryCode = params.getAdresse1CountryCode();
        this.adresse1Latitude = params.getAdresse1Latitude();
        this.adresse1Longitude = params.getAdresse1Longitude();
        this.adresse1Rue = params.getAdresse1Rue();
        this.adresse1Ville = params.getAdresse1Ville();


        //etape 2
        this.adresse2Complement = params.getAdresse2Complement();
        this.adresse2Complete = params.getAdresse2Complete();
        this.adresse2CountryCode = params.getAdresse2CountryCode();
        this.adresse2Latitude = params.getAdresse2Latitude();
        this.adresse2Longitude = params.getAdresse2Longitude();
        this.adresse2Rue = params.getAdresse2Rue();
        this.adresse2Ville = params.getAdresse2Ville();

        //etape 3
        this.adresse3Complement = params.getAdresse3Complement();
        this.adresse3Complete = params.getAdresse3Complete();
        this.adresse3CountryCode = params.getAdresse3CountryCode();
        this.adresse3Latitude = params.getAdresse3Latitude();
        this.adresse3Longitude = params.getAdresse3Longitude();
        this.adresse3Rue = params.getAdresse3Rue();
        this.adresse3Ville = params.getAdresse3Ville();

        //etape 4
        // this.adresse4Complement = params.getAdresse4Complement();
        this.adresse4Complete = params.getAdresse4Complete();
        this.adresse4CountryCode = params.getAdresse4CountryCode();
        this.adresse4Latitude = params.getAdresse4Latitude();
        this.adresse4Longitude = params.getAdresse4Longitude();
        this.adresse4Rue = params.getAdresse4Rue();
        this.adresse4Ville = params.getAdresse4Ville();

        //etape 5
        this.adresse5Complement = params.getAdresse5Complement();
        this.adresse5Complete = params.getAdresse5Complete();
        this.adresse5CountryCode = params.getAdresse5CountryCode();
        this.adresse5Latitude = params.getAdresse5Latitude();
        this.adresse5Longitude = params.getAdresse5Longitude();
        this.adresse5Rue = params.getAdresse5Rue();
        this.adresse5Ville = params.getAdresse5Ville();
*/


        this.tonnageVehicule = tonnage;

        this.client = client;
        this.client_personnel = client_personnel;

        this.code = code;

        this.createdOn = new Date();
        this.creePar = "C";

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

        this.statut = OperationStatut.ENREGISTRE.toString();

        this.typeMarchandise = marchandise_type;
        this.categorieVehicule = params.getCarrosserieVehicule();
        this.updatedOn = new Date();

        this.arriveeDestinataireNom = params.getArriveeDestinataireNom();
        this.arriveeDestinataireTelephone = params.getArriveeDestinataireTelephone();

        this.codePays = client.getCodePays();

        this.recurrenceRrule = recurrenceRrule;
        this.recurrenceProchain = recurrenceProchainSuivant;

        this.etapes = new ArrayList<Etape>();
        int cpt = 1;
        if (params.getEtapes() != null) {
            for (EtapeParams etape : params.getEtapes()) {
                logger.info("POST CLIENT ajout étape " + etape.getAdresseComplete());
                this.etapes.add(new Etape(etape, cpt));
                cpt++;
            }
        }

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

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
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

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Date getDepartDateProgrammeeOperation() {
        return departDateProgrammeeOperation;
    }

    public String getDepartDateProgrammeeOperationFormatted() {
        if (departDateProgrammeeOperation == null) {
            return "-";
        }
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(departDateProgrammeeOperation);
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

    public MarchandiseType getTypeMarchandise() {
        return typeMarchandise;
    }

    public void setTypeMarchandise(MarchandiseType typeMarchandise) {
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

    public UtilisateurDriver getTransporteur() {
        return transporteur;
    }

    public void setTransporteur(UtilisateurDriver transporteur) {
        this.transporteur = transporteur;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public void setVehicule(Vehicule vehicule) {
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

    public String getCreePar() {
        return creePar;
    }

    public void setCreePar(String creePar) {
        this.creePar = creePar;
    }


    public String getDepartDateProgrammeeOperationSansHeure() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        if (this.getDepartDateProgrammeeOperation() == null || this.getDepartDateProgrammeeOperation().equals("")) {
            return null;
        }
        return formatter.format(this.getDepartDateProgrammeeOperation());
    }

    public Date getArriveeDateOperation() {
        return arriveeDateOperation;
    }

    public void setArriveeDateOperation(Date arriveeDateOperation) {
        this.arriveeDateOperation = arriveeDateOperation;
    }

    public String getCouleur() {
        if (
                this.getStatut().equals(OperationStatut.ENREGISTRE.toString()) ||
                        this.getStatut().equals(OperationStatut.EN_COURS_DE_TRAITEMENT.toString()) ||
                        this.getClient() == null ||
                        this.getVehicule() == null ||
                        this.getTransporteur() == null ||
                        this.getPrixAPayerParClient() == null ||
                        this.getPrixDemandeParDriver() == null) {
            return "O";
        }
        return "V";
    }

    /**
     * Retourne une chaine de caractère "X-Y-..." qui indique les élémenets manquaunts du véhicule et du chauffeur
     * 1 = photo du permis du driver manquante
     * 2 = photo de la carte grise du véhicule manquante
     * 3 = photo du véhicule manquante
     * @return
     */
    public String getToutesInformationsRenseignees() {
        List<String> ret = new ArrayList<String>();
        if (this.getTransporteur() != null && (this.getTransporteur().getPhotoPermis() == null || this.getTransporteur().getPhotoPermis().equals(""))) {
            ret.add("1");
        }
        if (this.getVehicule() != null && (this.getVehicule().getDocumentCarteGrise() == null || this.getVehicule().getDocumentCarteGrise().equals(""))) {
            ret.add("2");
        }
        if (this.getVehicule() != null && (this.getVehicule().getPhotoPrincipale() == null || this.getVehicule().getPhotoPrincipale().equals(""))) {
            ret.add("3");
        }
        return String.join("-", ret);
    }

    public Integer getNbEtapes() {
        if (etapes == null) {
            return 0;
        }
        return etapes.size();
    }


    public Operation clone() {
        Operation o = null;
        try {
            // On récupère l'instance à renvoyer par l'appel de la
            // méthode super.clone()
            o = (Operation) super.clone();
            o.setUuid(null);
        } catch (CloneNotSupportedException cnse) {
            // Ne devrait jamais arriver car nous implémentons
            // l'interface Cloneable
            cnse.printStackTrace(System.err);
        }
        // on renvoie le clone
        return o;
    }

    public void annuler(AnnulerOperationParams params) {
        this.annulationDescription = params.getDescription();
        this.annulationEntite = params.getEntite();
        this.annulationMotif = params.getMotif();
        this.annulationDate = params.getDateAnnulation();
        this.annulationSoumission = new Date();
    }

    public boolean getAnnule() {
        return this.annulationDate != null;
    }

    public Date getDerniereDateConnue() {

        Date dateMax = this.getDateHeureDechargementTermine();

        if (dateMax == null) {
            dateMax = this.getDateHeureDechargementCommence();
        }
        if (dateMax == null) {
            dateMax = this.getDateHeureArriveADestination();
        }
        if (dateMax == null) {
            dateMax = this.getDateHeureEnRouteVersDestination();
        }
        if (dateMax == null) {
            dateMax = this.getDateHeureChargementTermine();
        }
        if (dateMax == null) {
            dateMax = this.getDateHeureChargementCommence();
        }
        if (dateMax == null) {
            dateMax = this.getDateHeureArriveeChezClientPourCharger();
        }
        if (dateMax == null) {
            dateMax = this.getArriveeDateOperation();
        }

        return dateMax;

    }

}
