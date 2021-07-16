package com.kamtar.transport.api.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kamtar.transport.api.model.Client;
import com.kamtar.transport.api.model.FactureClient;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class FactureClientMinimal {

    private String numeroFacture;

    private String dateFacture;

    private String listeOperations;

    private Double montantHT;

    private Double remisePourcentage;

    private Double montantTVA;

    private Double montantTTC;

    private Double netAPayer;

    public FactureClientMinimal(FactureClient client) {
        this.numeroFacture = client.getNumeroFacture();
        this.dateFacture = client.getDateFactureFormatted();

        String[] operations_splitted = client.getListeOperations().split("@");
        List<String> liste_operations = new ArrayList<String>();
        for (int i=0; i<operations_splitted.length; i++) {
            if (operations_splitted[i] != null && !"".equals(operations_splitted[i].trim())) {
                liste_operations.add(operations_splitted[i]);
            }
        }


        this.listeOperations = String.join(", ", liste_operations);;
        this.montantHT = client.getMontantHT();
        this.remisePourcentage = client.getRemisePourcentage();
        this.montantTVA = client.getMontantTVA();
        this.montantTTC = client.getMontantTTC();
        this.netAPayer = client.getNetAPayer();
    }


    public String getNumeroFacture() {
        return numeroFacture;
    }

    public void setNumeroFacture(String numeroFacture) {
        this.numeroFacture = numeroFacture;
    }

    public String getDateFacture() {
        return dateFacture;
    }

    public void setDateFacture(String dateFacture) {
        this.dateFacture = dateFacture;
    }

    public String getListeOperations() {
        return listeOperations;
    }

    public void setListeOperations(String listeOperations) {
        this.listeOperations = listeOperations;
    }

    public Double getMontantHT() {
        return montantHT;
    }

    public void setMontantHT(Double montantHT) {
        this.montantHT = montantHT;
    }

    public Double getRemisePourcentage() {
        return remisePourcentage;
    }

    public void setRemisePourcentage(Double remisePourcentage) {
        this.remisePourcentage = remisePourcentage;
    }

    public Double getMontantTVA() {
        return montantTVA;
    }

    public void setMontantTVA(Double montantTVA) {
        this.montantTVA = montantTVA;
    }

    public Double getMontantTTC() {
        return montantTTC;
    }

    public void setMontantTTC(Double montantTTC) {
        this.montantTTC = montantTTC;
    }

    public Double getNetAPayer() {
        return netAPayer;
    }

    public void setNetAPayer(Double netAPayer) {
        this.netAPayer = netAPayer;
    }
}