package com.kamtar.transport.api.model;

import java.util.UUID;


import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kamtar.transport.api.utils.StringListConverter;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiModel(description = "Variables d'un SMS envoyé")
@Entity
@Table(name = "sms_variable")
@EntityListeners(AuditingEntityListener.class)
public class SMSVariable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(notes = "Identifiant")	
	@Id
	@Type(type="uuid-char")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID uuid;

	@Column(name = "nom")
	@ApiModelProperty(notes = "Variable du SMS", required = true)
	private String nom;

	@Column(name = "valeur")
	@ApiModelProperty(notes = "Variable du SMS", required = true)
	private String valeur;


	public SMSVariable(String nom, String valeur) {
		super();
		this.nom = nom;
		this.valeur = valeur;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getValeur() {
		return valeur;
	}

	public void setValeur(String valeur) {
		this.valeur = valeur;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public SMSVariable() {
	}
}
