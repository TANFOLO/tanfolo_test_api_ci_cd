package com.kamtar.transport.api.service;


import com.kamtar.transport.api.model.Operation;
import com.kamtar.transport.api.model.Reclamation;
import com.kamtar.transport.api.model.ReclamationEchange;
import com.kamtar.transport.api.params.CreateReclamationEchangeParams;
import com.kamtar.transport.api.params.CreateReclamationParams;
import com.kamtar.transport.api.params.EditReclamationParams;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public interface ReclamationEchangeService {

	ReclamationEchange create(CreateReclamationEchangeParams params, String token);

	public Page<ReclamationEchange> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<ReclamationEchange> conditions);
	public Long countAll(Specification<ReclamationEchange> conditions);

	public ReclamationEchange save(ReclamationEchange reclamation);

	public List<ReclamationEchange> get(Reclamation reclamation, String code_pays);

}
