package com.kamtar.transport.api.service.impl;

import com.kamtar.transport.api.model.Devis;
import com.kamtar.transport.api.model.DevisChangementStatut;
import com.kamtar.transport.api.repository.DevisChangementStatutRepository;
import com.kamtar.transport.api.service.DevisChangementStatutService;
import com.kamtar.transport.api.utils.JWTProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service(value="DevisStatutChangementService")
public class DevisStatutChangementServiceImpl implements DevisChangementStatutService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(DevisStatutChangementServiceImpl.class);

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	private DevisChangementStatutRepository devisChangementStatutRepository;

	public DevisChangementStatut create(DevisChangementStatut devis_statut_changement) {
		devis_statut_changement = devisChangementStatutRepository.save(devis_statut_changement);
		return devis_statut_changement;
	}



	@Override
	public Page<DevisChangementStatut> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<DevisChangementStatut> conditions) {
		Direction SortDirection = order_dir.equals("desc") ? Direction.DESC : Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return devisChangementStatutRepository.findAll(conditions, pageable);
	}

	@Override
	public Long countAll(Specification<DevisChangementStatut> conditions) {
		return devisChangementStatutRepository.count(conditions);
	}


	@Override
	public DevisChangementStatut save(DevisChangementStatut statut) {
		return devisChangementStatutRepository.save(statut);
	}

	@Override
	public boolean isDevisPasseeParStatut(Devis devis, String statut) {
		return devisChangementStatutRepository.isDevisPasseeParStatut(devis, statut);
	}



}
