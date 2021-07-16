package com.kamtar.transport.api.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.kamtar.transport.api.params.CreateDriverPublicParams;
import com.kamtar.transport.api.params.ParentParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.kamtar.transport.api.service.UtilisateurDriverService;

public class CodeParrainageDriverValidValidator implements ConstraintValidator<CodeParrainageDriverValidConstraint, ParentParams> {

	/**
	 * Logger de la classe
	 */
	private static Logger LOGGER = LogManager.getLogger(CodeParrainageDriverValidValidator.class);

	@Autowired
	UtilisateurDriverService utilisateurDriverService;
	 
	@Override
	public void initialize(CodeParrainageDriverValidConstraint contactNumber) {
	}

	@Override
	public boolean isValid(ParentParams contactField, ConstraintValidatorContext cxt) {

		if (contactField instanceof CreateDriverPublicParams) {
			CreateDriverPublicParams params = ((CreateDriverPublicParams)contactField);
			if (params != null && params.getChauffeur_codeParrainage() != null) {
				String code_parametrage = params.getChauffeur_codeParrainage().trim();
				if (contactField != null && !"".equals(code_parametrage)) {
					return !utilisateurDriverService.codeParrainageExist(code_parametrage, params.getPays());
				}
			}
		}


		
		return true;

	}

}