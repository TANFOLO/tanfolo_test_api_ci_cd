package com.kamtar.transport.api.service;

import java.util.List;

import com.kamtar.transport.api.params.EditClientPublicParams;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.kamtar.transport.api.model.Client;
import com.kamtar.transport.api.model.UtilisateurClient;
import com.kamtar.transport.api.model.UtilisateurOperateurKamtar;
import com.kamtar.transport.api.params.CreateClientAnonymeParams;
import com.kamtar.transport.api.params.CreateClientParams;
import com.kamtar.transport.api.params.EditClientParams;


@Service
public interface ClientService {
	
	Client create(CreateClientParams params, String token);
	Client create(CreateClientAnonymeParams params);
	boolean update(EditClientParams params, Client user, String code_pays);
	boolean update(EditClientPublicParams params, Client user, String code_pays);
	boolean delete(Client user, String code_pays);
	Client getByUUID(String uuid, String pays);
	Client getByUtilisateur(UtilisateurClient utilisateur, String pays);
	UtilisateurClient getByValidationCode(String validation, String telephone, String code_pays);
	List<Client> autocomplete(String query, String code_pays);
	public Page<Client> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<Client> conditions);
	public Long countAll(Specification<Client> conditions);
	boolean emailExist(String email, String pays);
	boolean numeroDeTelephoneExist(String email, String pays);

	List<Client> getAll();
	Client save(Client user);
}
