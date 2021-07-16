package com.kamtar.transport.api.params;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class EtapeParams {



	@ApiModelProperty(notes = "Nom du destinataire", allowEmptyValue = true, required = false)
	private String adresseDestinataireNom;

	@ApiModelProperty(notes = "Numéro de téléphone du destinataire", allowEmptyValue = true, required = false)
	private String adresseDestinataireTelephone;

	@ApiModelProperty(notes = "Latitude GPS du point de départ", allowEmptyValue = false, required = true)
	private Double adresseLatitude;

	@ApiModelProperty(notes = "Longitude GPS du point de départ", allowEmptyValue = false, required = true)
	private Double adresseLongitude;

	@ApiModelProperty(notes = "Adresse du point de départ", allowEmptyValue = false, required = true)
	private String adresseComplete;

	@ApiModelProperty(notes = "Complément d'adresse du point de départ", allowEmptyValue = true, required = false)
	private String adresseComplement;

	@ApiModelProperty(notes = "Pays du point de départ", allowEmptyValue = false, required = true)
	private String adresseCountryCode;

	@ApiModelProperty(notes = "Ville du point de départ", allowEmptyValue = false, required = true)
	private String adresseVille;

	@ApiModelProperty(notes = "Rue du point de départ", allowEmptyValue = false, required = true)
	private String adresseRue;

	@ApiModelProperty(notes = "Code du pays", allowEmptyValue = true, required = false)
	private String codePays;



	public String getCodePays() {
		return codePays;
	}

	public void setCodePays(String codePays) {
		this.codePays = codePays;
	}

	public String getAdresseDestinataireNom() {
		return adresseDestinataireNom;
	}

	public void setAdresseDestinataireNom(String adresseDestinataireNom) {
		this.adresseDestinataireNom = adresseDestinataireNom;
	}

	public String getAdresseDestinataireTelephone() {
		return adresseDestinataireTelephone;
	}

	public void setAdresseDestinataireTelephone(String adresseDestinataireTelephone) {
		this.adresseDestinataireTelephone = adresseDestinataireTelephone;
	}

	public Double getAdresseLatitude() {
		return adresseLatitude;
	}

	public void setAdresseLatitude(Double adresseLatitude) {
		this.adresseLatitude = adresseLatitude;
	}

	public Double getAdresseLongitude() {
		return adresseLongitude;
	}

	public void setAdresseLongitude(Double adresseLongitude) {
		this.adresseLongitude = adresseLongitude;
	}

	public String getAdresseComplete() {
		return adresseComplete;
	}

	public void setAdresseComplete(String adresseComplete) {
		this.adresseComplete = adresseComplete;
	}

	public String getAdresseComplement() {
		return adresseComplement;
	}

	public void setAdresseComplement(String adresseComplement) {
		this.adresseComplement = adresseComplement;
	}

	public String getAdresseCountryCode() {
		return adresseCountryCode;
	}

	public void setAdresseCountryCode(String adresseCountryCode) {
		this.adresseCountryCode = adresseCountryCode;
	}

	public String getAdresseVille() {
		return adresseVille;
	}

	public void setAdresseVille(String adresseVille) {
		this.adresseVille = adresseVille;
	}

	public String getAdresseRue() {
		return adresseRue;
	}

	public void setAdresseRue(String adresseRue) {
		this.adresseRue = adresseRue;
	}


	public EtapeParams() {
	}
}
