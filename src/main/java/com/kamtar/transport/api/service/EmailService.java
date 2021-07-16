package com.kamtar.transport.api.service;



import java.util.Date;
import java.util.Map;

import com.kamtar.transport.api.model.UtilisateurOperateurKamtar;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.kamtar.transport.api.model.Utilisateur;


@Service
public interface EmailService {
	
	public Map<String, Object> getAllPagined(String order_by, String order_dir, int page_number, int page_size, String destinataire, String code_pays, Date createdDateBegin, Date createdDateEnd);
	public byte[] getContenu(String uuid);
}
