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

import com.kamtar.transport.api.model.Language;
import com.kamtar.transport.api.repository.LanguageRepository;
import com.kamtar.transport.api.service.LanguageService;

@Service
public class LanguageServiceImpl implements LanguageService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(LanguageServiceImpl.class);  

	@Autowired
	private LanguageRepository languageRepository; 
	
	
	@Override
	public Language create(Language country) {

		// enregistrement
		Language country_saved = languageRepository.save(country);
		
		return country_saved;
	}

	@Override
	public Optional<Language> get(UUID uuid) {
		return languageRepository.findById(uuid);
	}

	@Override
	public Page<Language> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<Language> conditions) {
		Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return languageRepository.findAll(conditions, pageable);
	}

	@Override
	public Long countAll(Specification<Language> conditions) {
		return languageRepository.count(conditions);
	}

	@Override
	public boolean codeExist(String code) {
		return languageRepository.codeExist(code);
	}

	@Override
	public List<Language> getAll() {
		Iterable<Language> iterable = languageRepository.findAll();
		List<Language> target = new ArrayList<Language>();
		iterable.forEach(target::add);
		return target;
    }



}
