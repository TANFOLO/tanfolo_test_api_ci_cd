package com.kamtar.transport.api.params;

import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;


public class AnnulerOperationParams extends ParentParams {

	@ApiModelProperty(notes = "Identifiant de l'opération", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.operation.create.id}")
	@UUIDObligatoireConstraint(message = "{err.operation.create.id_invalid}")
	private String operation_id;
	
	@ApiModelProperty(notes = "Motif de l'annulation", allowEmptyValue = false, required = true)
	@Min(value = 1, message = "{err.operation.annuler.motif_absent}")
	@NotNull(message = "{err.operation.annuler.motif_absent}")
	private Integer motif;

	@ApiModelProperty(notes = "Description de l'annulation", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.operation.annuler.description_absent}")
	@Size(min = 1, max = 255, message = "{err.operation.annuler.description_longueur}")
	private String description;

	@ApiModelProperty(notes = "Entité à l'origine de l'annulation", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.operation.annuler.entite_absent}")
	private Integer entite;

	@ApiModelProperty(notes = "Date de l'annulation", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.operation.annuler.date_absent}")
	private Date dateAnnulation;

	public AnnulerOperationParams() {
	}

	public Date getDateAnnulation() {
		return dateAnnulation;
	}

	public void setDateAnnulation(Date dateAnnulation) {
		this.dateAnnulation = dateAnnulation;
	}

	public Integer getMotif() {
		return motif;
	}

	public void setMotif(Integer motif) {
		this.motif = motif;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getEntite() {
		return entite;
	}

	public void setEntite(Integer entite) {
		this.entite = entite;
	}

	public String getOperation_id() {
		return operation_id;
	}

	public void setOperation_id(String operation_id) {
		this.operation_id = operation_id;
	}

}
