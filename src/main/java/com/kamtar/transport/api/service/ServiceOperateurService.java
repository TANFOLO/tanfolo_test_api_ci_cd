package com.kamtar.transport.api.service;

import com.kamtar.transport.api.model.Country;
import com.kamtar.transport.api.model.ServiceOperateur;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public interface ServiceOperateurService {

	public Optional<ServiceOperateur> get(UUID uuid);
	public List<ServiceOperateur> getAll();
	public Optional<ServiceOperateur> getByCode(String code);
	
}
