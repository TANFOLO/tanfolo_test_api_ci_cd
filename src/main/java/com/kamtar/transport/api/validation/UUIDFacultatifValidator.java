package com.kamtar.transport.api.validation;

import java.util.UUID;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class UUIDFacultatifValidator implements ConstraintValidator<UUIDFacultatifConstraint, String> {

	/**
	 * Logger de la classe
	 */
	private static Logger LOGGER = LogManager.getLogger(UUIDFacultatifValidator.class);  



	@Override
	public void initialize(UUIDFacultatifConstraint contactNumber) {
	}

	@Override
	public boolean isValid(String contactField, ConstraintValidatorContext cxt) {

		if (contactField != null && !"".equals(contactField.trim())) {
			try {
				UUID uuid = UUID.fromString(contactField);
			} catch (IllegalArgumentException  e) {
				return false;
			}
		}

		return true;

	}

}