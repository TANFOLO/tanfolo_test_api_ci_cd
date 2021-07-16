package com.kamtar.transport.api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.kamtar.transport.api.classes.Adresse;
import com.kamtar.transport.api.model.Country;

@Service
public interface LocalisationService {
	
	public List<Adresse> autocomplete(String query);
	
}
