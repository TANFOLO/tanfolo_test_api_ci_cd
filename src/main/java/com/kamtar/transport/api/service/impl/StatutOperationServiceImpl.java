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

import com.kamtar.transport.api.model.StatutOperation;
import com.kamtar.transport.api.repository.StatutOperationRepository;
import com.kamtar.transport.api.service.StatutOperationService;

@Service
public class StatutOperationServiceImpl implements StatutOperationService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(StatutOperationServiceImpl.class);  

	@Autowired
	private StatutOperationRepository statutCommandeRepository; 
	
	
	@Override
	public Optional<StatutOperation> get(UUID uuid) {
		return statutCommandeRepository.findById(uuid);
	}

	@Override
	public Page<StatutOperation> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<StatutOperation> conditions) {
		Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return statutCommandeRepository.findAll(conditions, pageable);
	}

	@Override
	public Long countAll(Specification<StatutOperation> conditions) {
		return statutCommandeRepository.count(conditions);
	}


	@Override
	public List<StatutOperation> getAll() {
		Iterable<StatutOperation> iterable = statutCommandeRepository.findAllByOrderByOrdreAsc();
		List<StatutOperation> target = new ArrayList<StatutOperation>();
		iterable.forEach(target::add);
		return target;
    }



}
