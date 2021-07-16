package com.kamtar.transport.api.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kamtar.transport.api.criteria.CountrySpecificationsBuilder;
import com.kamtar.transport.api.criteria.OperationSpecificationsBuilder;
import com.kamtar.transport.api.criteria.ParentSpecificationsBuilder;
import com.kamtar.transport.api.criteria.PredicateUtils;
import com.kamtar.transport.api.model.ActionAudit;
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
import com.kamtar.transport.api.repository.CountryRepository;
import com.kamtar.transport.api.repository.LanguageRepository;
import com.kamtar.transport.api.service.CountryService;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Service
public class CountryServiceImpl implements CountryService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(CountryServiceImpl.class);  

	@Autowired
	private CountryRepository countryRepository; 


	@Autowired
	private LanguageRepository languageRepository; 


	@Override
	public Country create(Country country) {

		// ajoute les langues objet à partir des codes
		List<Language> languages = new ArrayList<Language>();
		for (String language_code : country.getLanguages_code()) {
			Optional<Language> language = languageRepository.findByCode(language_code);
			if (language.isPresent()) {
				languages.add(language.get());
			}
		}
		country.setLanguages(languages);


		// enregistrement
		Country country_saved = countryRepository.save(country);


		return country_saved;
	}

	@Override
	public Optional<Country> get(UUID uuid) {
		return countryRepository.findById(uuid);
	}

	@Override
	public Page<Country> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<Country> conditions) {
		Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return countryRepository.findAll(conditions, pageable);
	}

	@Override
	public Long countAll(Specification<Country> conditions) {
		return countryRepository.count(conditions);
	}

	@Override
	public boolean codeExist(String code) {
		return countryRepository.codeExist(code);
	}

	@Override
	public List<Country> getAll() {
		Iterable<Country> iterable = countryRepository.findAll();
		List<Country> target = new ArrayList<Country>();
		iterable.forEach(target::add);
		return target;
	}

	@Override
	public List<Country> getCountriesKamtarOpere() {
		return countryRepository.findCountriesKamtarOpere();
	}

	@Override
	public Optional<Country> getByCode(String code) {
		return countryRepository.findByCode(code);
	}



}
