package com.kamtar.transport.api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.kamtar.transport.api.model.Language;

@Service
public interface LanguageService {
	
	public Language create(Language lead);
	public Optional<Language> get(UUID uuid);
	public Page<Language> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<Language> conditions);
	public Long countAll(Specification<Language> conditions);
	public boolean codeExist(String code);
	public List<Language> getAll();
	
}
