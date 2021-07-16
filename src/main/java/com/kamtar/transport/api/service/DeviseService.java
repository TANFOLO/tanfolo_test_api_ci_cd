package com.kamtar.transport.api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.kamtar.transport.api.model.Devise;

@Service
public interface DeviseService {
	
	public Optional<Devise> get(UUID uuid);
	public Optional<Devise> findByDevise(String devise);
	public Page<Devise> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<Devise> conditions);
	public Long countAll(Specification<Devise> conditions);
	public boolean codeExist(String code);
	public List<Devise> getAll();
	
}
