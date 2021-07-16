package com.kamtar.transport.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kamtar.transport.api.params.CountryCreateParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.Valid;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@ApiModel(description = "Service des opérateurs")
@Entity
@Table(name = "service_operateur")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class ServiceOperateur implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(notes = "Code")
    @Id
    @Column(name = "code", nullable = false, updatable = false, length=10, unique = true)
	private String code;

	@ApiModelProperty(notes = "Nom du service")
	@Column(updatable = true, name = "name", nullable = false, length=250, unique = false)
	private String name;

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


	public ServiceOperateur() {
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
}
