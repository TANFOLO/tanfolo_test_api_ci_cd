package com.kamtar.transport.api.service;


import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;


@Service
public interface ReclamationService {
	
	Reclamation create(CreateReclamationParams params, Operation operation, String token);

	Reclamation save(Reclamation reclamation);
	Reclamation getByUUID(String uuid, String code_pays);
	Reclamation getByCode(Long code, String code_pays);

	public Page<Reclamation> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<Reclamation> conditions);
	public Long countAll(Specification<Reclamation> conditions);

	Reclamation changer_statut(EditReclamationParams postBody, Reclamation reclamation, String token);

}
