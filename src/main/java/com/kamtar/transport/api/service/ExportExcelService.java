package com.kamtar.transport.api.service;


import com.kamtar.transport.api.model.Contact;
import com.kamtar.transport.api.model.Utilisateur;
import com.kamtar.transport.api.params.ContactParams;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public interface ExportExcelService {

	byte[] export(Page leads, Map<String, String> entetes_a_remplacer) throws Exception;
	
}
