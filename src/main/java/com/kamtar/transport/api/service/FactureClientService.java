package com.kamtar.transport.api.service;

import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.CreateClientAnonymeParams;
import com.kamtar.transport.api.params.CreateClientParams;
import com.kamtar.transport.api.params.EditClientParams;
import com.kamtar.transport.api.params.OperationFacturerParams;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;


@Service
public interface FactureClientService {

	FactureClient genererFacture(OperationFacturerParams params, String pays) throws IOException;
	public Page<FactureClient> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<FactureClient> conditions);
	Long countAll(Specification<FactureClient> conditions);
	FactureClient getByUUID(String uuid, String pays);
	FactureClient getByNumero(String numero, String pays);
	boolean delete(FactureClient facture);

	public byte[] get(String uuid);
}
