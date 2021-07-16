package com.kamtar.transport.api.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kamtar.transport.api.model.Contact;
import com.kamtar.transport.api.model.Utilisateur;
import com.kamtar.transport.api.params.ContactParams;
import org.springframework.stereotype.Service;


@Service
public interface MapperJSONService {

	ObjectMapper get(String token);
	
}
