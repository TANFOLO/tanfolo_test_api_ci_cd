package com.kamtar.transport.api.service;

import com.kamtar.transport.api.model.StatutDevis;
import com.kamtar.transport.api.model.StatutOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public interface StatutDevisService {
	
	public Optional<StatutDevis> get(UUID uuid);
	public Page<StatutDevis> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<StatutDevis> conditions);
	public Long countAll(Specification<StatutDevis> conditions);
	public List<StatutDevis> getAll();
	
}
