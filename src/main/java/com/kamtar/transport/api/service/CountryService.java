package com.kamtar.transport.api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.kamtar.transport.api.model.Country;

@Service
public interface CountryService {
	
	public Country create(Country lead);
	public Optional<Country> get(UUID uuid);
	public Page<Country> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<Country> conditions);
	public Long countAll(Specification<Country> conditions);
	public boolean codeExist(String code);
	public List<Country> getAll();
	public List<Country> getCountriesKamtarOpere();
	public Optional<Country> getByCode(String code);
	
}
