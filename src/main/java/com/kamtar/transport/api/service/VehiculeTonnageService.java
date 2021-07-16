package com.kamtar.transport.api.service;

import com.kamtar.transport.api.model.VehiculeTonnage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface VehiculeTonnageService {
	
	public List<VehiculeTonnage> getAll();
	public boolean codeExist(String code);
	
}
