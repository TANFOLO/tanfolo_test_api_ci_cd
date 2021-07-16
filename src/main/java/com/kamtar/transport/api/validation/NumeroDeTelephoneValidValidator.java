package com.kamtar.transport.api.validation;

import com.kamtar.transport.api.params.*;
import com.kamtar.transport.api.service.UtilisateurAdminKamtarService;
import com.kamtar.transport.api.service.UtilisateurClientService;
import com.kamtar.transport.api.service.UtilisateurDriverService;
import com.kamtar.transport.api.service.UtilisateurOperateurKamtarService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumeroDeTelephoneValidValidator implements ConstraintValidator<NumeroDeTelephoneValidConstraint, ParentParams> {

	/**
	 * Logger de la classe
	 */
	private static Logger LOGGER = LogManager.getLogger(NumeroDeTelephoneValidValidator.class);

	@Override
	public void initialize(NumeroDeTelephoneValidConstraint contactNumber) {
	}

	@Override
	public boolean isValid(ParentParams contactField, ConstraintValidatorContext cxt) {

		if (contactField instanceof CreateAdminKamtarParams) {
			return
					validationNumeroTelephone(((CreateAdminKamtarParams)contactField).getCode_pays(), ((CreateAdminKamtarParams)contactField).getNumero_telephone_1()) &&
					validationNumeroTelephone(((CreateAdminKamtarParams)contactField).getCode_pays(), ((CreateAdminKamtarParams)contactField).getNumero_telephone_2())
					;
		} else if (contactField instanceof CreateClientAnonymeParams) {
			return
					validationNumeroTelephone(((CreateClientAnonymeParams)contactField).getCode_pays(), ((CreateClientAnonymeParams)contactField).getTelephone1()) &&
							validationNumeroTelephone(((CreateClientAnonymeParams)contactField).getCode_pays(), ((CreateClientAnonymeParams)contactField).getTelephone2())
					;
		} else if (contactField instanceof CreateClientParams) {
			return
					validationNumeroTelephone(((CreateClientParams)contactField).getCode_pays(), ((CreateClientParams)contactField).getContact_numero_telephone1()) &&
							validationNumeroTelephone(((CreateClientParams)contactField).getCode_pays(), ((CreateClientParams)contactField).getContact_numero_telephone2())
					;
		} else if (contactField instanceof CreateComptePublicParams) {
			return
					validationNumeroTelephone(((CreateComptePublicParams)contactField).getPays(), ((CreateComptePublicParams)contactField).getProprietaire_numero_telephone_1()) &&
							validationNumeroTelephone(((CreateComptePublicParams)contactField).getPays(), ((CreateComptePublicParams)contactField).getProprietaire_numero_telephone_2())
					;
		} else if (contactField instanceof CreateDriverParams) {
			return
					validationNumeroTelephone(((CreateDriverParams)contactField).getCode_pays(), ((CreateDriverParams)contactField).getNumero_telephone_1()) &&
							validationNumeroTelephone(((CreateDriverParams)contactField).getCode_pays(), ((CreateDriverParams)contactField).getNumero_telephone_2())
					;
		} else if (contactField instanceof CreateDriverPublicParams) {
			return
					validationNumeroTelephone(((CreateDriverPublicParams)contactField).getPays(), ((CreateDriverPublicParams)contactField).getChauffeur_numero_telephone_1()) &&
							validationNumeroTelephone(((CreateDriverPublicParams)contactField).getPays(), ((CreateDriverPublicParams)contactField).getChauffeur_numero_telephone_2())
					;
		} else if (contactField instanceof CreateOperateurKamtarParams) {
			return
					validationNumeroTelephone(((CreateOperateurKamtarParams)contactField).getCode_pays(), ((CreateOperateurKamtarParams)contactField).getNumero_telephone_1()) &&
							validationNumeroTelephone(((CreateOperateurKamtarParams)contactField).getCode_pays(), ((CreateOperateurKamtarParams)contactField).getNumero_telephone_2())
					;
		} else if (contactField instanceof CreateProprietaireParams) {
			return
					validationNumeroTelephone(((CreateProprietaireParams)contactField).getCode_pays(), ((CreateProprietaireParams)contactField).getNumero_telephone_1()) &&
							validationNumeroTelephone(((CreateProprietaireParams)contactField).getCode_pays(), ((CreateProprietaireParams)contactField).getNumero_telephone_2())
					;
		} else if (contactField instanceof CreateProprietairePublicParams) {
			return
					validationNumeroTelephone(((CreateProprietairePublicParams)contactField).getPays(), ((CreateProprietairePublicParams)contactField).getProprietaire_numero_telephone_1()) &&
							validationNumeroTelephone(((CreateProprietairePublicParams)contactField).getPays(), ((CreateProprietairePublicParams)contactField).getProprietaire_numero_telephone_1())
					;
		} else if (contactField instanceof EditAdminKamtarParams) {
			return
					validationNumeroTelephone(((EditAdminKamtarParams)contactField).getCode_pays(), ((EditAdminKamtarParams)contactField).getNumero_telephone_1()) &&
							validationNumeroTelephone(((EditAdminKamtarParams)contactField).getCode_pays(), ((EditAdminKamtarParams)contactField).getNumero_telephone_2())
					;
		} else if (contactField instanceof EditClientParams) {
			return
					validationNumeroTelephone(((EditClientParams)contactField).getCode_pays(), ((EditClientParams)contactField).getNumero_telephone1_responsable()) &&
							validationNumeroTelephone(((EditClientParams)contactField).getCode_pays(), ((EditClientParams)contactField).getNumero_telephone2_responsable()) &&
							validationNumeroTelephone(((EditClientParams)contactField).getCode_pays(), ((EditClientParams)contactField).getContact_numero_telephone1()) &&
							validationNumeroTelephone(((EditClientParams)contactField).getCode_pays(), ((EditClientParams)contactField).getContact_numero_telephone2())
					;
		} else if (contactField instanceof EditClientPublicParams) {
			return
					validationNumeroTelephone(((EditClientPublicParams)contactField).getCodePays(), ((EditClientPublicParams)contactField).getNumero_telephone1_responsable()) &&
							validationNumeroTelephone(((EditClientPublicParams)contactField).getCodePays(), ((EditClientPublicParams)contactField).getNumero_telephone2_responsable())
					;
		} else if (contactField instanceof EditDriverParams) {
			return
					validationNumeroTelephone(((EditDriverParams)contactField).getCode_pays(), ((EditDriverParams)contactField).getNumero_telephone_1()) &&
							validationNumeroTelephone(((EditDriverParams)contactField).getCode_pays(), ((EditDriverParams)contactField).getNumero_telephone_2())
					;
		} else if (contactField instanceof EditOperateurKamtarParams) {
			return
					validationNumeroTelephone(((EditOperateurKamtarParams)contactField).getCode_pays(), ((EditOperateurKamtarParams)contactField).getNumero_telephone_1()) &&
							validationNumeroTelephone(((EditOperateurKamtarParams)contactField).getCode_pays(), ((EditOperateurKamtarParams)contactField).getNumero_telephone_2())
					;
		} else if (contactField instanceof EditProprietaireParams) {
			return
					validationNumeroTelephone(((EditProprietaireParams)contactField).getCode_pays(), ((EditProprietaireParams)contactField).getNumero_telephone_1()) &&
							validationNumeroTelephone(((EditProprietaireParams)contactField).getCode_pays(), ((EditProprietaireParams)contactField).getNumero_telephone_2())
					;
		} else if (contactField instanceof EditTransporteurParams) {
			return
					validationNumeroTelephone(((EditTransporteurParams)contactField).getCode_pays(), ((EditTransporteurParams)contactField).getNumero_telephone_1()) &&
							validationNumeroTelephone(((EditTransporteurParams)contactField).getCode_pays(), ((EditTransporteurParams)contactField).getNumero_telephone_2())
					;
		} else if (contactField instanceof EditTransporteurPublicParams) {
			return
					validationNumeroTelephone(((EditTransporteurPublicParams)contactField).getCodePays(), ((EditTransporteurPublicParams)contactField).getNumero_telephone_1()) &&
							validationNumeroTelephone(((EditTransporteurPublicParams)contactField).getCodePays(), ((EditTransporteurPublicParams)contactField).getNumero_telephone_2())
					;
		}




		return true;

	}

	private boolean validationNumeroTelephone(String pays, String telephone) {


		if (telephone == null || "".equals(telephone.trim())) {
			return true;
		}
		telephone = telephone.replaceAll(" ", "").replaceAll("\\.", "").replaceAll("\\-", "").replaceAll("\\+", "");


		if ("CI".equals(pays)) {
			// vérification numéro cote d'ivoire
			if (telephone.startsWith("00")) {
				return false;
			}
			String regex = "^\\(?([0-9]{8})\\)?$";
			Pattern pattern = Pattern.compile(regex);
			return pattern.matcher(telephone).matches();
		} else if ("SN".equals(pays)) {
			// vérification numéro sénégal
			String regex = "^\\(?([0-9]{9})\\)?$";
			Pattern pattern = Pattern.compile(regex);
			return pattern.matcher(telephone).matches();
		}
		return true;

	}

}