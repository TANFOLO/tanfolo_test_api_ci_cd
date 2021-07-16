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
import com.kamtar.transport.api.model.MarchandiseType;
import com.kamtar.transport.api.repository.CountryRepository;
import com.kamtar.transport.api.repository.LanguageRepository;
import com.kamtar.transport.api.repository.MarchandiseTypeRepository;
import com.kamtar.transport.api.service.CountryService;
import com.kamtar.transport.api.service.MarchandiseTypeService;

@Service
public class MarchandiseTypeServiceImpl implements MarchandiseTypeService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(MarchandiseTypeServiceImpl.class);  

	@Autowired
	private MarchandiseTypeRepository marchandiseTypeRepository; 


	@Override
	public List<MarchandiseType> getAll() {
		return marchandiseTypeRepository.findAll();
	}

	@Override
	public boolean codeExist(String code) {
		return marchandiseTypeRepository.codeExist(code);
	}

}
