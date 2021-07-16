package com.kamtar.transport.api.service;


import com.kamtar.transport.api.model.Devis;
import com.kamtar.transport.api.model.DevisChangementStatut;
import com.kamtar.transport.api.model.Operation;
import com.kamtar.transport.api.model.OperationChangementStatut;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


@Service
public interface DevisChangementStatutService {
	
	DevisChangementStatut create(DevisChangementStatut statut);

	public Page<DevisChangementStatut> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<DevisChangementStatut> conditions);
	public Long countAll(Specification<DevisChangementStatut> conditions);
	public DevisChangementStatut save(DevisChangementStatut devis_statut);
	public boolean isDevisPasseeParStatut(Devis devis, String statut);

}
