package com.kamtar.transport.api.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.kamtar.transport.api.service.LanguageService;

public class LanguageCodeAlreadyExistValidator implements ConstraintValidator<LanguageCodeAlreadyExistConstraint, String> {

	/**
	 * Logger de la classe
	 */
	private static Logger LOGGER = LogManager.getLogger(LanguageCodeAlreadyExistValidator.class);  

	@Autowired
   LanguageService offreService;
 
    @Override
    public void initialize(LanguageCodeAlreadyExistConstraint contactNumber) {
    }
 
    @Override
    public boolean isValid(String contactField, ConstraintValidatorContext cxt) {
    	
    	if (offreService.codeExist(contactField)) {
    		LOGGER.info("codeExist true");
    		return false;
    	}
		LOGGER.info("codeExist false");
    	return true;
    	
    }
 
}