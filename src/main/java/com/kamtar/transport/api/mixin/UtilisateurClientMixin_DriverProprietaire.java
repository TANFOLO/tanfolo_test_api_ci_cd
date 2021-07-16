package com.kamtar.transport.api.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class UtilisateurClientMixin_DriverProprietaire {

    @JsonIgnore
    private String email;

    @JsonIgnore
    private String numeroTelephone1;

    @JsonIgnore
    private String numeroTelephone2;




}