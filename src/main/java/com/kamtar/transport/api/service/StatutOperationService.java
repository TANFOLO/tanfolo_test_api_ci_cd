package com.kamtar.transport.api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.kamtar.transport.api.model.StatutOperation;

@Service
public interface StatutOperationService {
	
	public Optional<StatutOperation> get(UUID uuid);
	public Page<StatutOperation> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<StatutOperation> conditions);
	public Long countAll(Specification<StatutOperation> conditions);
	public List<StatutOperation> getAll();
	
}
