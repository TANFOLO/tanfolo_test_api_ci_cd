package com.kamtar.transport.api.service.impl;

import com.kamtar.transport.api.model.VehiculeTonnage;
import com.kamtar.transport.api.model.VehiculeType;
import com.kamtar.transport.api.repository.VehiculeTonnageRepository;
import com.kamtar.transport.api.repository.VehiculeTypeRepository;
import com.kamtar.transport.api.service.VehiculeTonnageService;
import com.kamtar.transport.api.service.VehiculeTypeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehiculeTonnageServiceImpl implements VehiculeTonnageService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(VehiculeTonnageServiceImpl.class);

	@Autowired
	private VehiculeTonnageRepository vehiculeTonnageRepository;


	@Override
	public List<VehiculeTonnage> getAll() {
		return vehiculeTonnageRepository.getAllByOrdreAsc();
	}

	@Override
	public boolean codeExist(String code) {
		return vehiculeTonnageRepository.codeExist(code);
	}

}
