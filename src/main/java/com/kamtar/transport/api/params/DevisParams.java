package com.kamtar.transport.api.params;

import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;


public class DevisParams extends ParentParams {

	@ApiModelProperty(notes = "Identifiant du devis", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.devis.create.id}")
	@UUIDObligatoireConstraint(message = "{err.devis.create.id_invalid}")
	private String devis;

	public String getDevis() {
		return devis;
	}

	public void setDevis(String devis) {
		this.devis = devis;
	}
}
