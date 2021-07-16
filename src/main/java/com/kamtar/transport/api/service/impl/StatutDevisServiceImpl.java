package com.kamtar.transport.api.service.impl;

import com.kamtar.transport.api.model.StatutDevis;
import com.kamtar.transport.api.model.StatutOperation;
import com.kamtar.transport.api.repository.StatutDevisRepository;
import com.kamtar.transport.api.repository.StatutOperationRepository;
import com.kamtar.transport.api.service.StatutDevisService;
import com.kamtar.transport.api.service.StatutOperationService;
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
public class StatutDevisServiceImpl implements StatutDevisService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(StatutDevisServiceImpl.class);  

	@Autowired
	private StatutDevisRepository statutDevisRepository;
	
	
	@Override
	public Optional<StatutDevis> get(UUID uuid) {
		return statutDevisRepository.findById(uuid);
	}

	@Override
	public Page<StatutDevis> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<StatutDevis> conditions) {
		Direction SortDirection = order_dir.equals("desc") ? Direction.DESC : Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return statutDevisRepository.findAll(conditions, pageable);
	}

	@Override
	public Long countAll(Specification<StatutDevis> conditions) {
		return statutDevisRepository.count(conditions);
	}


	@Override
	public List<StatutDevis> getAll() {
		Iterable<StatutDevis> iterable = statutDevisRepository.findAllByOrderByOrdreAsc();
		List<StatutDevis> target = new ArrayList<StatutDevis>();
		iterable.forEach(target::add);
		return target;
    }



}
