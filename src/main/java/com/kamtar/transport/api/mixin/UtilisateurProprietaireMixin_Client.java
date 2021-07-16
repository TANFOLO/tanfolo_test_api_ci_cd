package com.kamtar.transport.api.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

public abstract class UtilisateurProprietaireMixin_Client {

    @JsonIgnore
    private String email;

    @JsonIgnore
    private String numeroTelephone1;

    @JsonIgnore
    private String numeroTelephone2;

    @JsonIgnore
    private String nom;

    @JsonIgnore
    private String prenom;

    @JsonIgnore
    private String photo;

    @JsonIgnore
    private Date dateEtablissementCarteTransport;

    @JsonIgnore
    private String numeroCarteTransport;

    @JsonIgnore
    private String photoCarteTransport;

    @JsonIgnore
    private String codeParrain;

    @JsonIgnore
    private String codeParrainage;

    @JsonIgnore
    private boolean assujetiAIRSI;

    @JsonIgnore
    private String adresseFacturationLigne1;

    @JsonIgnore
    private String adresseFacturationLigne2;

    @JsonIgnore
    private String adresseFacturationLigne3;

    @JsonIgnore
    private String adresseFacturationLigne4;

    @JsonIgnore
    private String typeCompte;


}