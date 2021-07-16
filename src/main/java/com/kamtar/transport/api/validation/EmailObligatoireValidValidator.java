package com.kamtar.transport.api.validation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailObligatoireValidValidator implements ConstraintValidator<EmailObligatoireValidConstraint, String> {

	/**
	 * Logger de la classe
	 */
	private static Logger LOGGER = LogManager.getLogger(EmailObligatoireValidValidator.class);


	@Override
	public void initialize(EmailObligatoireValidConstraint contactNumber) {
	}

	@Override
	public boolean isValid(String contactField, ConstraintValidatorContext cxt) {
	if (contactField == null) {
		return false;
	}

	String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
	Pattern pattern = Pattern.compile(regex);
	Matcher matcher = pattern.matcher(contactField);
	LOGGER.info("matcher.matches() = " + matcher.matches());
	return matcher.matches();


	}

}