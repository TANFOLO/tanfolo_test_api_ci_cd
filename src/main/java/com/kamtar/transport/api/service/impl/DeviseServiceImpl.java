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

import com.kamtar.transport.api.model.Devise;
import com.kamtar.transport.api.repository.DeviseRepository;
import com.kamtar.transport.api.service.DeviseService;

@Service
public class DeviseServiceImpl implements DeviseService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(DeviseServiceImpl.class);  

	@Autowired
	private DeviseRepository deviseRepository; 
	
	@Override
	public Optional<Devise> get(UUID uuid) {
		return deviseRepository.findById(uuid);
	}

	@Override
	public Page<Devise> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<Devise> conditions) {
		Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return deviseRepository.findAll(conditions, pageable);
	}

	@Override
	public Long countAll(Specification<Devise> conditions) {
		return deviseRepository.count(conditions);
	}

	@Override
	public boolean codeExist(String code) {
		return deviseRepository.codeExist(code);
	}

	@Override
	public List<Devise> getAll() {
		Iterable<Devise> iterable = deviseRepository.findAll();
		List<Devise> target = new ArrayList<Devise>();
		iterable.forEach(target::add);
		return target;
    }

	@Override
	public Optional<Devise> findByDevise(String devise) {
		return deviseRepository.findByDevise(devise);
	}



}
