package com.kamtar.transport.api.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.validator.EmailValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.kamtar.transport.api.service.UtilisateurAdminKamtarService;
import com.kamtar.transport.api.service.UtilisateurClientService;
import com.kamtar.transport.api.service.UtilisateurOperateurKamtarService;
import com.kamtar.transport.api.service.UtilisateurDriverService;

public class EmailValidValidator implements ConstraintValidator<EmailValidConstraint, String> {

	/**
	 * Logger de la classe
	 */
	private static Logger LOGGER = LogManager.getLogger(EmailValidValidator.class);  


	@Override
	public void initialize(EmailValidConstraint contactNumber) {
	}

	@Override
	public boolean isValid(String contactField, ConstraintValidatorContext cxt) {
	if (contactField != null) {
		String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(contactField);
		LOGGER.info("matcher.matches() = " + matcher.matches());
		return matcher.matches();
	}
	return true;

	}

}