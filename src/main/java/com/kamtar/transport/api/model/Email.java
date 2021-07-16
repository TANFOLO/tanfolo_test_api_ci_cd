package com.kamtar.transport.api.model;

import java.util.UUID;


import org.hibernate.annotations.Type;
import org.json.simple.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kamtar.transport.api.enums.TemplateEmail;
import com.kamtar.transport.api.utils.StringListConverter;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import javax.persistence.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@ApiModel(description = "Email envoyé")
@Entity
@Table(name = "email", indexes = { @Index(name = "idx_email_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"createdAt", "updatedAt"}, allowGetters = true)
public class Email implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(notes = "Identifiant")	
	@Id
	@Type(type="uuid-char")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID uuid;

	@ApiModelProperty(notes = "Code du template de mail")	
	@Column(name = "template")
	private String template;

	@Column(name = "lang")
    @ApiModelProperty(notes = "Langue de l'email", required = true)
	private String lang;

	@Column(name = "recipient")
    @ApiModelProperty(notes = "Adresse email destinataire de l'email", required = true)
	private String recipient;

	@ApiModelProperty(notes = "Date de création")
	@Column(name = "createdOn", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@CreatedDate
	private Date createdAt;

	@ApiModelProperty(notes = "Date de dernière modification")
	@Column(name = "updatedOn", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@LastModifiedDate
	private Date updatedAt;

	@ApiModelProperty(notes = "Variables injectés dans le template")	
	@ElementCollection
    @MapKeyColumn(name="name", length=50)
    @Column(name="value", columnDefinition="BLOB")
    @CollectionTable(name="email_variables", joinColumns=@JoinColumn(name="email_id"))
	private Map<String, Serializable> variables;

	@Convert(converter = StringListConverter.class)
	private List<String> variables_subject;

	@ApiModelProperty(notes = "Code du pays")
	@Column(updatable = false, name = "codePays", nullable = true, unique = false)
	private String codePays;

	@Transient
	private String attachment1;

	@Transient
	private String attachment2;

	@Transient
	private String attachment3;

	@Transient
	private String attachment1_name;

	@Transient
	private String attachment2_name;

	@Transient
	private String attachment3_name;

	@Transient
	private String attachment1_mime;

	@Transient
	private String attachment2_mime;

	@Transient
	private String attachment3_mime;

	public String getAttachment1_name() {
		return attachment1_name;
	}

	public void setAttachment1_name(String attachment1_name) {
		this.attachment1_name = attachment1_name;
	}

	public String getAttachment2_name() {
		return attachment2_name;
	}

	public void setAttachment2_name(String attachment2_name) {
		this.attachment2_name = attachment2_name;
	}

	public String getAttachment3_name() {
		return attachment3_name;
	}

	public void setAttachment3_name(String attachment3_name) {
		this.attachment3_name = attachment3_name;
	}

	public String getAttachment1_mime() {
		return attachment1_mime;
	}

	public void setAttachment1_mime(String attachment1_mime) {
		this.attachment1_mime = attachment1_mime;
	}

	public String getAttachment2_mime() {
		return attachment2_mime;
	}

	public void setAttachment2_mime(String attachment2_mime) {
		this.attachment2_mime = attachment2_mime;
	}

	public String getAttachment3_mime() {
		return attachment3_mime;
	}

	public void setAttachment3_mime(String attachment3_mime) {
		this.attachment3_mime = attachment3_mime;
	}

	public String getAttachment1() {
		return attachment1;
	}

	public void setAttachment1(String attachment1) {
		this.attachment1 = attachment1;
	}

	public String getAttachment2() {
		return attachment2;
	}

	public void setAttachment2(String attachment2) {
		this.attachment2 = attachment2;
	}

	public String getAttachment3() {
		return attachment3;
	}

	public void setAttachment3(String attachment3) {
		this.attachment3 = attachment3;
	}

	public String getCodePays() {
		return codePays;
	}

	public void setCodePays(String codePays) {
		this.codePays = codePays;
	}

	public UUID getUuid() {
		return uuid;
	} 
 
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public List<String> getVariables_subject() {
		return variables_subject;
	}

	public void setVariables_subject(List<String> variables_subject) {
		this.variables_subject = variables_subject;
	}

	public Email(TemplateEmail template, String lang, String recipient,
			Map<String, Serializable> variables, List<String> variables_subject, String codePays) {
		super();
		this.template = template.toString();
		this.lang = lang;
		this.recipient = recipient;
		this.variables = variables;
		this.variables_subject = variables_subject;
		this.codePays = codePays;
	}

	public Email() {
		super();
	}

	public Map<String, Serializable> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, Serializable> variables) {
		this.variables = variables;
	}
	


}
