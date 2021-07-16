package com.kamtar.transport.api.params;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;

public class ContactParams extends ParentParams {


	@ApiModelProperty(notes = "Sujet du message", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.contact.motif}")
	@Size(min = 1, max = 250, message = "{err.contact.motif_longueur}") 
	protected String motif;

	@ApiModelProperty(notes = "Contenu du message", allowEmptyValue = false, required = true)
	@NotNull(message = "{err.contact.message}")
	@Size(min = 0, max = 25000, message = "{err.contact.message_longueur}") 
	protected String message;

	@ApiModelProperty(notes = "Adresse e-mail de l'émetteur", allowEmptyValue = true, required = false, dataType = "Adresse email")
	protected String emetteur_email;

	@ApiModelProperty(notes = "Nom de l'émetteur", allowEmptyValue = true, required = false, dataType = "Nom")
	protected String emetteur_nom;

	@ApiModelProperty(notes = "Téléphone de l'émetteur", allowEmptyValue = true, required = false, dataType = "Téléphone")
	protected String emetteur_telephone;

	@ApiModelProperty(notes = "Pays", allowEmptyValue = true, required = false, dataType = "Pays de l'entité Kamtar à qui est destiné le message")
	protected String pays;

	public String getEmetteur_nom() {
		return emetteur_nom;
	}

	public void setEmetteur_nom(String emetteur_nom) {
		this.emetteur_nom = emetteur_nom;
	}

	public String getEmetteur_telephone() {
		return emetteur_telephone;
	}

	public void setEmetteur_telephone(String emetteur_telephone) {
		this.emetteur_telephone = emetteur_telephone;
	}

	public String getPays() {
		return pays;
	}

	public void setPays(String pays) {
		this.pays = pays;
	}

	public String getMotif() {
		return motif;
	}

	public void setMotif(String motif) {
		this.motif = motif;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getEmetteur_email() {
		return emetteur_email;
	}

	public void setEmetteur_email(String emetteur_email) {
		this.emetteur_email = emetteur_email;
	}

	



}
