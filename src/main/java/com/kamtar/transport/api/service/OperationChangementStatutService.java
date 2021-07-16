package com.kamtar.transport.api.service;



import java.util.List;

import com.kamtar.transport.api.model.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.kamtar.transport.api.model.OperationChangementStatut;



@Service
public interface OperationChangementStatutService {
	
	OperationChangementStatut create(OperationChangementStatut statut);

	public Page<OperationChangementStatut> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<OperationChangementStatut> conditions);
	public Long countAll(Specification<OperationChangementStatut> conditions);
	public OperationChangementStatut save(OperationChangementStatut operation_statut);
	public boolean isOperationPasseeParStatut(Operation operation, String statut);

}
