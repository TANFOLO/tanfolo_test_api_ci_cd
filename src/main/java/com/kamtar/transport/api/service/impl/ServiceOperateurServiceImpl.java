package com.kamtar.transport.api.service.impl;

import com.kamtar.transport.api.model.Country;
import com.kamtar.transport.api.model.Language;
import com.kamtar.transport.api.model.ServiceOperateur;
import com.kamtar.transport.api.repository.CountryRepository;
import com.kamtar.transport.api.repository.LanguageRepository;
import com.kamtar.transport.api.repository.ServiceOperateurRepository;
import com.kamtar.transport.api.service.CountryService;
import com.kamtar.transport.api.service.ServiceOperateurService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ServiceOperateurServiceImpl implements ServiceOperateurService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(ServiceOperateurServiceImpl.class);

	@Autowired
	private ServiceOperateurRepository serviceOperateurRepository;


	@Override
	public Optional<ServiceOperateur> get(UUID uuid) {
		return serviceOperateurRepository.findById(uuid);
	}


	@Override
	public List<ServiceOperateur> getAll() {
		Iterable<ServiceOperateur> iterable = serviceOperateurRepository.findAll();
		List<ServiceOperateur> target = new ArrayList<ServiceOperateur>();
		iterable.forEach(target::add);
		return target;
	}

	@Override
	public Optional<ServiceOperateur> getByCode(String code) {
		return serviceOperateurRepository.findByCode(code);
	}

}
