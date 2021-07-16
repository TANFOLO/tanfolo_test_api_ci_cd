package com.kamtar.transport.api.validation;

import com.kamtar.transport.api.params.CreateDriverPublicParams;
import com.kamtar.transport.api.params.CreateProprietairePublicParams;
import com.kamtar.transport.api.params.ParentParams;
import com.kamtar.transport.api.service.UtilisateurProprietaireService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CodeParrainageProprietaireValidValidator implements ConstraintValidator<CodeParrainageProprietaireValidConstraint, ParentParams> {

	/**
	 * Logger de la classe
	 */
	private static Logger LOGGER = LogManager.getLogger(CodeParrainageProprietaireValidValidator.class);

	@Autowired
	UtilisateurProprietaireService utilisateurProprietaireService;
	 
	@Override
	public void initialize(CodeParrainageProprietaireValidConstraint contactNumber) {
	}

	@Override
	public boolean isValid(ParentParams contactField, ConstraintValidatorContext cxt) {

		if (contactField instanceof CreateProprietairePublicParams) {
			String code_parametrage = ((CreateProprietairePublicParams)contactField).getProprietaire_codeParrainage().trim();
			if (contactField != null && !"".equals(code_parametrage)) {
				return !utilisateurProprietaireService.codeParrainageExist(code_parametrage, ((CreateProprietairePublicParams)contactField).getPays());
			}
		}
		
		return true;

	}

}