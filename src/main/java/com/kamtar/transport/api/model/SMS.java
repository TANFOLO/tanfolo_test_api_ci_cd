package com.kamtar.transport.api.model;

import java.util.UUID;


import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kamtar.transport.api.enums.TemplateSMS;
import com.kamtar.transport.api.utils.StringListConverter;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import javax.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiModel(description = "SMS envoyé")
@Entity
@Table(name = "sms", indexes = { @Index(name = "idx_sms_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedAt"}, allowGetters = true)
public class SMS {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(notes = "Identifiant")	
    @Id
    @Type(type="uuid-char")
    @GeneratedValue(strategy = GenerationType.AUTO)
	private UUID uuid;
    
    @Column(name = "template")
    @ApiModelProperty(notes = "Code du template SMS", required = true)
	private String template;
    
    @Column(name = "lang")
    @ApiModelProperty(notes = "Code de la langue utilisée dans le SMS", required = true)
	private String lang;
    
    @Column(name = "to_number")
    @ApiModelProperty(notes = "Numéro de téléphone du destinataire du SMS", required = true)
	private String to;
    
    @Column(name = "to_prefix")
    @ApiModelProperty(notes = "Préfixe du numéro de téléphone du destinataire du SMS", required = true)
	private String to_prefix;

	@Column(name = "to_country")
	@ApiModelProperty(notes = "Code pays du numéro de téléphone du destinataire du SMS", required = true)
	private String to_country;
    
    @Column(name = "created_on", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    @ApiModelProperty(notes = "Heure de création du SMS", required = true)
    private Date createdAt;

    @Column(name = "updated_on", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    @ApiModelProperty(notes = "Heure de dernière mise à jour du SMS", required = true)
    private Date updatedAt;
    
    /**
     * Attention c'est l'ordre qui compte pour injecter les variables dans le template du SMS
     */
    @OneToMany(	targetEntity=SMSVariable.class,			
			cascade = CascadeType.ALL)
	private List<SMSVariable> variables;

	@ApiModelProperty(notes = "Code du pays")
	@Column(updatable = false, name = "codePays", nullable = true, unique = false)
	private String codePays;

	public String getTo_prefix() {
		return to_prefix;
	}

	public String getTo_country() {
		return to_country;
	}

	public void setTo_country(String to_country) {
		this.to_country = to_country;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getCodePays() {
		return codePays;
	}

	public void setCodePays(String codePays) {
		this.codePays = codePays;
	}


	public List<SMSVariable> getVariables() {
		return variables;
	}

	public void setVariables(List<SMSVariable> variables) {
		this.variables = variables;
	}

	public void setTo_prefix(String to_prefix) {
		this.to_prefix = to_prefix;
	}

	public SMS(String template) {
		this.template = template;
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getTemplate() {
		return template;
	}

	public SMS() {
		super();
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public SMS(String to, String to_country, TemplateSMS template, String lang, String code_pays) {
		super();
		this.to = to;
		this.template = template.toString();
		this.lang = lang;
		this.variables = new ArrayList<SMSVariable>();
		this.codePays = code_pays;
		this.to_country = to_country;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}


	public void setTemplate(String template) {
		this.template = template;
	}
	
}
