package com.kamtar.transport.api.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

public abstract class VehiculeMixin_Client {

    @JsonIgnore
    private Date dateValiditeAssurance;

    @JsonIgnore
    private Date dateValiditePatente;

    @JsonIgnore
    private Date dateValiditeVisiteTechnique;

    @JsonIgnore
    private String documentAssurance;

    @JsonIgnore
    private String documentCarteGrise;

    @JsonIgnore
    private String localisationHabituelleVehicule;

    @JsonIgnore
    private String observations;

    @JsonIgnore
    private String photoPrincipale;

    @JsonIgnore
    private String driverPrincipal;

    @JsonIgnore
    private String proprietaire;

    @JsonIgnore
    private String codePayslocalisationHabituelleVehicule;


}