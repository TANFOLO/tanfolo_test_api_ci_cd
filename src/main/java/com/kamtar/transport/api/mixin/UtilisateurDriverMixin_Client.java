package com.kamtar.transport.api.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class UtilisateurDriverMixin_Client {

    @JsonIgnore
    private String email;

    @JsonIgnore
    private String numeroTelephone1;

    @JsonIgnore
    private String numeroTelephone2;

    @JsonIgnore
    private String codeParrainage;

    @JsonIgnore
    private String numeroPermis;

    @JsonIgnore
    private String parrain;

    @JsonIgnore
    private String permisType;

    @JsonIgnore
    private String photoPermis;


}