package com.kamtar.transport.api.validation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class NumeroDeTelephoneLibreValidValidator implements ConstraintValidator<NumeroDeTelephoneLibreValidConstraint, String> {

	/**
	 * Logger de la classe
	 */
	private static Logger LOGGER = LogManager.getLogger(NumeroDeTelephoneLibreValidValidator.class);

	@Override
	public void initialize(NumeroDeTelephoneLibreValidConstraint contactNumber) {
	}

	@Override
	public boolean isValid(String contactField, ConstraintValidatorContext cxt) {

		if (contactField == null || "".equals(contactField.trim())) {
			return true;
		}
		contactField = contactField.replaceAll(" ", "").replaceAll("\\.", "").replaceAll("\\-", "").replaceAll("\\+", "");
		return true;

	}

}