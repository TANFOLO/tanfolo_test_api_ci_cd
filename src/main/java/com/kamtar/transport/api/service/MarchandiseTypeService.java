package com.kamtar.transport.api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.kamtar.transport.api.model.Country;
import com.kamtar.transport.api.model.MarchandiseType;
import com.kamtar.transport.api.model.VehiculeType;

@Service
public interface MarchandiseTypeService {
	
	public List<MarchandiseType> getAll();
	public boolean codeExist(String code);
	
}
