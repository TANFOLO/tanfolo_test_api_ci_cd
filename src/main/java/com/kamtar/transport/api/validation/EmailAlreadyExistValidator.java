package com.kamtar.transport.api.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.kamtar.transport.api.params.ParentParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.kamtar.transport.api.service.UtilisateurAdminKamtarService;
import com.kamtar.transport.api.service.UtilisateurClientService;
import com.kamtar.transport.api.service.UtilisateurOperateurKamtarService;
import com.kamtar.transport.api.service.UtilisateurDriverService;

public class EmailAlreadyExistValidator implements ConstraintValidator<EmailAlreadyExistConstraint, ParentParams> {

	/**
	 * Logger de la classe
	 */
	private static Logger LOGGER = LogManager.getLogger(EmailAlreadyExistValidator.class);

	private String pays;

	@Autowired
	UtilisateurAdminKamtarService utilisateurAdminKamtarService;

	@Autowired
	UtilisateurClientService utilisateurClientService;

	@Autowired
	UtilisateurOperateurKamtarService utilisateurOperateurKamtarService;

	@Autowired
	UtilisateurDriverService utilisateurTransporteurService;

	@Override
	public void initialize(EmailAlreadyExistConstraint contactNumber) {
	}

	@Override
	public boolean isValid(ParentParams contactField, ConstraintValidatorContext cxt) {


		return true;

	}

}