package com.kamtar.transport.api.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

public abstract class OperationMixin_Client {

    @JsonIgnore
    private Float avanceDonneAuDriver;

    @JsonIgnore
    private Float avanceDonneAuDriverDevise;

    @JsonIgnore
    private String observationsParTransporteur;

    @JsonIgnore
    private Float prixDemandeParDriver;

    @JsonIgnore
    private String prixDemandeParDriverDevise;

    @JsonIgnore
    private Date datePaiementAvanceDriver;

    @JsonIgnore
    private Date datePaiementSoldeDriver;

    @JsonIgnore
    private Integer satisfactionDriver;

    @JsonIgnore
    private String factureProprietaire;

    @JsonIgnore
    private String typeOperationTVA;

}