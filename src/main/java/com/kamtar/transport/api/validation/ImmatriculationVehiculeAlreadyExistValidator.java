package com.kamtar.transport.api.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.kamtar.transport.api.params.CreateComptePublicParams;
import com.kamtar.transport.api.params.CreateVehiculeParams;
import com.kamtar.transport.api.params.CreateVehiculePublicParams;
import com.kamtar.transport.api.params.ParentParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.kamtar.transport.api.service.CountryService;
import com.kamtar.transport.api.service.VehiculeService;

public class ImmatriculationVehiculeAlreadyExistValidator implements ConstraintValidator<ImmatriculationVehiculeAlreadyExistConstraint, ParentParams> {

	/**
	 * Logger de la classe
	 */
	private static Logger LOGGER = LogManager.getLogger(ImmatriculationVehiculeAlreadyExistValidator.class);

	private String pays;

	@Autowired
	VehiculeService vehiculeService;


	public void initialize(ImmatriculationVehiculeAlreadyExistConstraint constraintAnnotation) {
		// initialize the zipcode/city/country correlation service
	}

	@Override
    public boolean isValid(ParentParams contactField, ConstraintValidatorContext cxt) {

		String pays = null;
		String immatriculation = null;

		if (contactField instanceof CreateVehiculeParams) {
			pays = ((CreateVehiculeParams)contactField).getImmatriculationPays();
			immatriculation = ((CreateVehiculeParams)contactField).getImmatriculation();
		} else if (contactField instanceof CreateComptePublicParams) {
			pays = ((CreateComptePublicParams)contactField).getImmatriculationPays();
			immatriculation = ((CreateComptePublicParams)contactField).getImmatriculation();
		} else if (contactField instanceof CreateVehiculePublicParams) {
			pays = ((CreateVehiculePublicParams)contactField).getImmatriculationPays();
			immatriculation = ((CreateVehiculePublicParams)contactField).getImmatriculation();
		}

		if (pays != null && immatriculation != null) {
			if (vehiculeService.immatriculationExist(immatriculation, pays)) {
				return false;
			}
		}

    	

    	return true;
    	
    }
 
}