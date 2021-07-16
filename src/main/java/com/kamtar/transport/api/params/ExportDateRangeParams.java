package com.kamtar.transport.api.params;

import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Date;


public class ExportDateRangeParams extends ParentParams {

	@ApiModelProperty(notes = "Date de début", allowEmptyValue = true, required = false, dataType = "Date")
	private Date dateDebut;

	@ApiModelProperty(notes = "Date de fin", allowEmptyValue = true, required = false, dataType = "Date")
	private Date dateFin;


	public ExportDateRangeParams() {
		super();
	}

	public Date getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(Date dateDebut) {
		this.dateDebut = dateDebut;
	}

	public Date getDateFin() {
		return dateFin;
	}

	public void setDateFin(Date dateFin) {
		this.dateFin = dateFin;
	}
}
