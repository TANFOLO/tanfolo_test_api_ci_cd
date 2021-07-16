package com.kamtar.transport.api.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class OperationMixin_DriverProprietaire {

    @JsonIgnore
    private Float prixAPayerParClient;

    @JsonIgnore
    private String prixAPayerParClientDevise;

    @JsonIgnore
    private Float prixSouhaiteParClient;

    @JsonIgnore
    private String prixSouhaiteParClientDevise;

    @JsonIgnore
    private Integer satisfactionClient;

    @JsonIgnore
    private String facture;

    @JsonIgnore
    private String prixProposeAuClient;


}