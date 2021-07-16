package com.kamtar.transport.api.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.kamtar.transport.api.model.Country;
import com.kamtar.transport.api.model.Language;
import com.kamtar.transport.api.model.VehiculeType;
import com.kamtar.transport.api.repository.CountryRepository;
import com.kamtar.transport.api.repository.LanguageRepository;
import com.kamtar.transport.api.repository.VehiculeTypeRepository;
import com.kamtar.transport.api.service.CountryService;
import com.kamtar.transport.api.service.VehiculeTypeService;

@Service
public class VehiculeTypeServiceImpl implements VehiculeTypeService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(VehiculeTypeServiceImpl.class);  

	@Autowired
	private VehiculeTypeRepository vehiculeTypeRepository; 


	@Override
	public List<VehiculeType> getAll() {
		return vehiculeTypeRepository.findAll();
	}

	@Override
	public boolean codeExist(String code) {
		return vehiculeTypeRepository.codeExist(code);
	}

}
