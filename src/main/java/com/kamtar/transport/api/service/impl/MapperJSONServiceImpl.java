package com.kamtar.transport.api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kamtar.transport.api.classes.FactureClientMinimal;
import com.kamtar.transport.api.enums.UtilisateurTypeDeCompte;
import com.kamtar.transport.api.mixin.*;
import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.service.MapperJSONService;
import com.kamtar.transport.api.utils.JWTProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service(value="MapperClientService")
public class MapperJSONServiceImpl implements MapperJSONService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(MapperJSONServiceImpl.class);

	@Autowired
	JWTProvider jwtProvider;

	@Override
	public ObjectMapper get(String token) {
		ObjectMapper mapper = new ObjectMapper();

		if (token != null && !"".equals(token) && UtilisateurTypeDeCompte.EXPEDITEUR.equals(jwtProvider.getTypeDeCompte(token))) {
			mapper.addMixInAnnotations(Operation.class, OperationMixin_Client.class);
			mapper.addMixInAnnotations(UtilisateurDriver.class, UtilisateurDriverMixin_Client.class);
			mapper.addMixInAnnotations(UtilisateurProprietaire.class, UtilisateurProprietaireMixin_Client.class);
			mapper.addMixInAnnotations(Vehicule.class, VehiculeMixin_Client.class);
			mapper.addMixInAnnotations(FactureClient.class, FactureClientMinimal.class);
		} else if (token != null && !"".equals(token)  && UtilisateurTypeDeCompte.DRIVER.toString().equals(jwtProvider.getTypeDeCompte(token)) || UtilisateurTypeDeCompte.PROPRIETAIRE.toString().equals(jwtProvider.getTypeDeCompte(token))) {
			mapper.addMixInAnnotations(Client.class, ClientMixin_DriverProprietaire.class);
			mapper.addMixInAnnotations(Operation.class, OperationMixin_DriverProprietaire.class);
			mapper.addMixInAnnotations(UtilisateurClient.class, UtilisateurClientMixin_DriverProprietaire.class);
		}


		return mapper;
	}

}





