package com.kamtar.transport.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kamtar.transport.api.enums.UtilisateurTypeDeCompte;
import com.kamtar.transport.api.params.*;
import com.kamtar.transport.api.utils.UpdatableBCrypt;
import com.wbc.core.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;

@ApiModel(description = "Client (compte personnel)")
@Entity
@Table(name = "utilisateur_client_personnel", indexes = { @Index(name = "idx_utilisateur_client_personnel_codePays", columnList = "codePays") })
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"updatedOn"}, allowGetters = true)
public class UtilisateurClientPersonnel extends Utilisateur implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(UtilisateurClientPersonnel.class);


	@ApiModelProperty(notes = "Code permettant de valider le client")
	@Column(name = "code_validation", nullable = true, updatable = true)
	protected String code_validation;

	@ApiModelProperty(notes = "Liste des autorisations (suite de 0 et de 1). Position : "
			+ "COMMANDER => 0"
			+ "LISTE_OPERATION => 1"
			+ "FACTURES => 2")
	@Column(name = "liste_droits", nullable = false, updatable = true, length = 200)
	@JsonProperty("liste_droits")
	protected String liste_droits;

	@ApiModelProperty(notes = "Client auquel est attaché le client personnel")
	@OneToOne
	@JoinColumn(name = "client", foreignKey = @ForeignKey(name = "fk_client_personnel_client"))
	private Client client;

	public String getListe_droits() {
		return liste_droits;
	}

	public void setListe_droits(String liste_droits) {
		this.liste_droits = liste_droits;
	}

	public UtilisateurClientPersonnel() {
		super();
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public UtilisateurClientPersonnel(CreateClientPersonnelParams params, Client client) {
		super();
		this.nom = params.getNom();
		this.prenom = params.getPrenom();
		this.email = params.getEmail();
		this.motDePasse = UpdatableBCrypt.hashPassword(params.getMot_de_passe());
		this.numeroTelephone1 = params.getNumero_telephone1().replaceAll(" ", "");
		this.numeroTelephone2 = params.getNumero_telephone2().replaceAll(" ", "");
		this.activate = params.isActivate();
		if (params.getCode_pays() != null && !"".equals(params.getCode_pays())) {
			this.codePays = params.getCode_pays();
		} else {
			this.codePays = client.getCodePays();
		}
		if (params.getLocale() != null && !"".equals(params.getLocale())) {
			this.locale = params.getLocale();
		} else {
			this.locale = client.getUtilisateur().getLocale();
		}
		this.typeDeCompte = UtilisateurTypeDeCompte.EXPEDITEUR_PERSONNEL.toString();
		this.numeroTelephone1Pays  = params.getNumero_telephone1_pays();
		this.numeroTelephone2Pays  = params.getNumero_telephone2_pays();
		this.client = client;
		this.liste_droits = params.getListe_droits();
	}


	public void edit(EditClientPersonnelParams params) {
		this.nom = params.getNom();
		this.prenom = params.getPrenom();
		this.email = params.getEmail();
		if (params.getMot_de_passe() != null && !"".equals(params.getMot_de_passe().trim())) {
			this.motDePasse = UpdatableBCrypt.hashPassword(params.getMot_de_passe());
		}
		this.numeroTelephone1 = params.getNumero_telephone1().replaceAll(" ", "");
		this.numeroTelephone2 = params.getNumero_telephone2().replaceAll(" ", "");
		this.activate = params.isActivate();
		if (params.getCode_pays() != null && !"".equals(params.getCode_pays().trim())) {
			this.codePays = params.getCode_pays();
		}
		if (params.getLocale() != null && !"".equals(params.getLocale().trim())) {
			this.locale = params.getLocale();
		}
		this.numeroTelephone1Pays  = params.getNumero_telephone1_pays();
		this.numeroTelephone2Pays  = params.getNumero_telephone2_pays();
		this.liste_droits = params.getListe_droits();
	}


	public void edit(EditClientPersonnelPublicParams params) {
		this.nom = params.getNom();
		this.prenom = params.getPrenom();
		this.email = params.getEmail();
		if (params.getPassword() != null && !"".equals(params.getPassword().trim())) {
			this.motDePasse = UpdatableBCrypt.hashPassword(params.getPassword());
		}
		this.numeroTelephone1 = params.getNumero_telephone1().replaceAll(" ", "");
		this.numeroTelephone2 = params.getNumero_telephone2().replaceAll(" ", "");
		this.numeroTelephone1Pays  = params.getNumero_telephone1_pays();
		this.numeroTelephone2Pays  = params.getNumero_telephone2_pays();
		this.liste_droits = params.getListe_droits();
	}


	public String getCode_validation() {
		return code_validation;
	}

	public void setCode_validation(String code_validation) {
		this.code_validation = code_validation;
	}
}
