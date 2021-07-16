package com.kamtar.transport.api.validation;

import java.util.UUID;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class UUIDObligatoireValidator implements ConstraintValidator<UUIDObligatoireConstraint, String> {

	/**
	 * Logger de la classe
	 */
	private static Logger LOGGER = LogManager.getLogger(UUIDObligatoireValidator.class);  



	@Override
	public void initialize(UUIDObligatoireConstraint contactNumber) {
	}

	@Override
	public boolean isValid(String contactField, ConstraintValidatorContext cxt) {
		if (contactField == null) {
			return false;
		}
		try {
			UUID uuid = UUID.fromString(contactField);
		} catch (IllegalArgumentException  e) {
			return false;
		}

		return true;

	}

}