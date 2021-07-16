package com.kamtar.transport.api.params;

import com.kamtar.transport.api.validation.UUIDFacultatifConstraint;
import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;

import io.swagger.annotations.ApiModelProperty;

public class ChangerOrdreCommandesParams extends ParentParams {

	// liste ordonnée des id des commandes pour modifier l'ordre
	@ApiModelProperty(notes = "Liste ordonnée des uuid des commandes", allowEmptyValue = false, required = true, dataType = "Tableau d'UUID")
	private String[] ids;

	@ApiModelProperty(notes = "Identifiant du transporteur", allowEmptyValue = false, required = true, dataType = "UUID")
	@UUIDFacultatifConstraint(message = "{err.transporteur.id_invalid}")
	private String transporteur;
	
	
	public String getTransporteur() {
		return transporteur;
	}

	public void setTransporteur(String transporteur) {
		this.transporteur = transporteur;
	}

	public String[] getIds() {
		return ids;
	}

	public void setIds(String[] ids) {
		this.ids = ids;
	}







}
