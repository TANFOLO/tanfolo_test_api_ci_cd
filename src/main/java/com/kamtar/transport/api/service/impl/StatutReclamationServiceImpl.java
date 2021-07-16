package com.kamtar.transport.api.service.impl;


import com.kamtar.transport.api.model.StatutReclamation;
import com.kamtar.transport.api.repository.StatutReclamationRepository;
import com.kamtar.transport.api.service.StatutReclamationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StatutReclamationServiceImpl implements StatutReclamationService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(StatutReclamationServiceImpl.class);

	@Autowired
	private StatutReclamationRepository statutReclamationRepository;
	
	
	@Override
	public Optional<StatutReclamation> get(UUID uuid) {
		return statutReclamationRepository.findById(uuid);
	}

	@Override
	public Page<StatutReclamation> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<StatutReclamation> conditions) {
		Direction SortDirection = order_dir.equals("desc") ? Direction.DESC : Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return statutReclamationRepository.findAll(conditions, pageable);
	}

	@Override
	public Long countAll(Specification<StatutReclamation> conditions) {
		return statutReclamationRepository.count(conditions);
	}


	@Override
	public List<StatutReclamation> getAll() {
		Iterable<StatutReclamation> iterable = statutReclamationRepository.findAllByOrderByOrdreAsc();
		List<StatutReclamation> target = new ArrayList<StatutReclamation>();
		iterable.forEach(target::add);
		return target;
    }



}
