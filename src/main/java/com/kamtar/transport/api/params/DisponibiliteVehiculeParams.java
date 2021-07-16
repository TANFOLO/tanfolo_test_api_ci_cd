package com.kamtar.transport.api.params;

import com.kamtar.transport.api.validation.UUIDFacultatifConstraint;
import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;


public class DisponibiliteVehiculeParams extends ParentParams {

	@ApiModelProperty(notes = "Identifiant du véhicule", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.vehicule.id}")
	@UUIDObligatoireConstraint(message = "{err.vehicule.id_invalid}")
	private String id;

	@ApiModelProperty(notes = "Disponibilite du véhicule", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.vehicule.create.disponibilite}")
	@Size(min = 1, max = 250, message = "{err.vehicule.create.disponibilite_longueur}")
	private String disponibilite;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDisponibilite() {
		return disponibilite;
	}

	public void setDisponibilite(String disponibilite) {
		this.disponibilite = disponibilite;
	}
}
