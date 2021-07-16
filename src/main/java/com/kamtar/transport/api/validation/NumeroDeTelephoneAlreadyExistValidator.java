package com.kamtar.transport.api.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.kamtar.transport.api.service.UtilisateurAdminKamtarService;
import com.kamtar.transport.api.service.UtilisateurClientService;
import com.kamtar.transport.api.service.UtilisateurOperateurKamtarService;
import com.kamtar.transport.api.service.UtilisateurDriverService;

public class NumeroDeTelephoneAlreadyExistValidator implements ConstraintValidator<NumeroDeTelephoneAlreadyExistConstraint, String> {

	/**
	 * Logger de la classe
	 */
	private static Logger LOGGER = LogManager.getLogger(NumeroDeTelephoneAlreadyExistValidator.class);

	@Autowired
	UtilisateurAdminKamtarService utilisateurAdminKamtarService;

	@Autowired
	UtilisateurClientService utilisateurClientService;

	@Autowired
	UtilisateurOperateurKamtarService utilisateurOperateurKamtarService;

	@Autowired
	UtilisateurDriverService utilisateurTransporteurService;

	@Override
	public void initialize(NumeroDeTelephoneAlreadyExistConstraint contactNumber) {
	}

	@Override
	public boolean isValid(String contactField, ConstraintValidatorContext cxt) {

		/*if (utilisateurAdminKamtarService.numeroDeTelephoneExist(contactField)) {
			LOGGER.info("email true");
			return false;
		} else if (utilisateurClientService.numeroDeTelephoneExist(contactField)) {
			LOGGER.info("email true");
			return false;
		} else if (utilisateurOperateurKamtarService.numeroDeTelephoneExist(contactField)) {
			LOGGER.info("email true");
			return false;
		} else if (utilisateurTransporteurService.numeroDeTelephoneExist(contactField)) {
			LOGGER.info("email true");
			return false;
		}
		LOGGER.info("email false");*/
		return true;


	}

}