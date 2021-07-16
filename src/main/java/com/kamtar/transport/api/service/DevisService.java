package com.kamtar.transport.api.service;


import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.*;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public interface DevisService {

	Operation create(CreateDevisParams params, String code_pays);
	Devis passerAEnCoursDeTraitement(Devis devis, String code_pays);
	Devis passerAReponseImpossible(Devis devis, String code_pays);

	Devis getByUUID(String uuid, String pays);

	public Page<Devis> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<Devis> conditions);
	public Long countAll(Specification<Devis> conditions);
	
	public boolean delete(Devis vehicule, String pays);


	Operation convertionEnOperation(Devis devis, String code_pays);
}
