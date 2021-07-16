package com.kamtar.transport.api.service.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.kamtar.transport.api.model.Operation;
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

import com.kamtar.transport.api.model.OperationChangementStatut;
import com.kamtar.transport.api.repository.OperationChangementStatutRepository;
import com.kamtar.transport.api.repository.UtilisateurDriverRepository;
import com.kamtar.transport.api.service.ActionAuditService;
import com.kamtar.transport.api.service.OperationChangementStatutService;
import com.kamtar.transport.api.utils.JWTProvider;

@Service(value="OperationStatutChangementService")
public class OperationStatutChangementServiceImpl implements OperationChangementStatutService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(OperationStatutChangementServiceImpl.class); 

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	private OperationChangementStatutRepository operationChangementStatutRepository; 

	public OperationChangementStatut create(OperationChangementStatut operation_statut_changement) {
		operation_statut_changement = operationChangementStatutRepository.save(operation_statut_changement); 
		return operation_statut_changement;
	}



	@Override
	public Page<OperationChangementStatut> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<OperationChangementStatut> conditions) {
		Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return operationChangementStatutRepository.findAll(conditions, pageable);
	}

	@Override
	public Long countAll(Specification<OperationChangementStatut> conditions) {
		return operationChangementStatutRepository.count(conditions);
	}


	@Override
	public OperationChangementStatut save(OperationChangementStatut statut) {
		return operationChangementStatutRepository.save(statut);
	}

	@Override
	public boolean isOperationPasseeParStatut(Operation operation, String statut) {
		return operationChangementStatutRepository.isOperationPasseeParStatut(operation, statut);
	}



}
