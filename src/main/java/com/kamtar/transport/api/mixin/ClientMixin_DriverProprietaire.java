package com.kamtar.transport.api.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

public abstract class ClientMixin_DriverProprietaire {

    @JsonIgnore
    private String adresseFacturationLigne1;

    @JsonIgnore
    private String adresseFacturationLigne2;

    @JsonIgnore
    private String adresseFacturationLigne3;

    @JsonIgnore
    private String adresseFacturationLigne4;

    @JsonIgnore
    private String contactEmail;

    @JsonIgnore
    private Date createdOn;

    @JsonIgnore
    private String typeCompte;

    @JsonIgnore
    private Date updatedOn;

    @JsonIgnore
    private String compteContribuable;

    @JsonIgnore
    private Integer delais;

    @JsonIgnore
    private String modePaiement;

    @JsonIgnore
    private String numeroRCCM;

}