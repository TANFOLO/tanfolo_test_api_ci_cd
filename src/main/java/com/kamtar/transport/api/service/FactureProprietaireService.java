package com.kamtar.transport.api.service;

import com.kamtar.transport.api.model.FactureProprietaire;
import com.kamtar.transport.api.model.UtilisateurProprietaire;
import com.kamtar.transport.api.params.OperationFacturerParams;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
public interface FactureProprietaireService {

	FactureProprietaire genererFacture(OperationFacturerParams params, String pays) throws IOException;
	public Page<FactureProprietaire> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<FactureProprietaire> conditions);
	Long countAll(Specification<FactureProprietaire> conditions);
	FactureProprietaire getByUUID(String uuid, String pays);
	FactureProprietaire getByNumero(String numero, String pays);
	boolean delete(FactureProprietaire facture);

	public Long countFacturesProprietaire(@Param("proprietaire") UtilisateurProprietaire proprietaire, String pays);

	public byte[] get(String uuid);
}
