package com.kamtar.transport.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kamtar.transport.api.params.CreateReclamationEchangeParams;
import com.kamtar.transport.api.params.CreateReclamationParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.Valid;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@ApiModel(description = "Réclamation échange")
@Entity
@Table(name = "reclamation_echange", indexes = { @Index(name = "idx_reclamation_echange_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class ReclamationEchange implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(notes = "Identifiant")
	@Id
	@Type(type="uuid-char")
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected UUID uuid;

	@ApiModelProperty(notes = "Code de la reclamation")
	@Column(updatable = false, name = "code", nullable = false, unique = true)
	private Long code;

	@ApiModelProperty(notes = "Date de création")
    @Column(name = "createdOn", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdOn = new Date();

	@ApiModelProperty(notes = "Date de dernière modification")
    @Column(name = "updatedOn", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    @JsonIgnore
    private Date updatedOn = new Date();

	@ApiModelProperty(notes = "Descriptif de la reclamation")
	@Column(updatable = false, name = "descriptif", nullable = true, unique = false, columnDefinition="LONGTEXT")
	private String descriptif;

	@ApiModelProperty(notes = "Reclamation concernée")
	@OneToOne
	@JoinColumn(name = "reclamation", foreignKey = @ForeignKey(name = "fk_reclamation_echange_reclamation"))
	@JsonIgnore
	private Reclamation reclamation;

	@ApiModelProperty(notes = "Est ce que cet échange a été rédigé par Kamtar ?")
	@Column(updatable = false, name = "parKamtar", nullable = true, unique = false)
	private Boolean parKamtar;

	@ApiModelProperty(notes = "Code du pays")
	@Column(updatable = false, name = "codePays", nullable = true, unique = false)
	private String codePays;

	public ReclamationEchange(@Valid CreateReclamationEchangeParams postBody, Long code, Reclamation reclamation, Boolean parKamtar, String code_pays) {
		super();
		this.parKamtar = parKamtar;
		this.descriptif = postBody.getDescriptif();
		this.codePays = code_pays;
		this.reclamation = reclamation;
		this.code = code;
	}

	public Long getCode() {
		return code;
	}

	public void setCode(Long code) {
		this.code = code;
	}

	public Reclamation getReclamation() {
		return reclamation;
	}

	public void setReclamation(Reclamation reclamation) {
		this.reclamation = reclamation;
	}

	public Boolean getParKamtar() {
		return parKamtar;
	}

	public void setParKamtar(Boolean parKamtar) {
		this.parKamtar = parKamtar;
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

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}
	public String getDescriptif() {
		return descriptif;
	}

	public void setDescriptif(String descriptif) {
		this.descriptif = descriptif;
	}
	public ReclamationEchange() {
	}
}
