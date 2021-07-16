package com.kamtar.transport.api.classes;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Adresse")
public class Adresse {

	@ApiModelProperty(notes = "Latitude")
	private Double latitude;

	@ApiModelProperty(notes = "Longitude")
	private Double longitude;

	@ApiModelProperty(notes = "Adresse")
	private String adresse;

	@ApiModelProperty(notes = "Code du pays de l'adresse")
	private String adresse_country_code;

	@ApiModelProperty(notes = "Ville")
	private String ville;

	@ApiModelProperty(notes = "Rue")
	private String rue;
	
	
	public String getVille() {
		return ville;
	}
	public void setVille(String ville) {
		this.ville = ville;
	}
	public String getRue() {
		return rue;
	}
	public void setRue(String rue) {
		this.rue = rue;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public String getAdresse() {
		return adresse;
	}
	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}
	public Adresse(Double latitude, Double longitude, String adresse, String adresse_country_code, String ville, String rue) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.adresse = adresse;
		this.adresse_country_code = adresse_country_code;
		this.ville = ville;
		this.rue = rue;
	}
	public String getAdresse_country_code() {
		return adresse_country_code;
	}
	public void setAdresse_country_code(String adresse_country_code) {
		this.adresse_country_code = adresse_country_code;
	}
	@Override
	public String toString() {
		return "Adresse [latitude=" + latitude + ", longitude=" + longitude + ", adresse=" + adresse
				+ ", adresse_country_code=" + adresse_country_code + ", ville=" + ville + ", rue=" + rue + "]";
	} 
	
	
	

}
