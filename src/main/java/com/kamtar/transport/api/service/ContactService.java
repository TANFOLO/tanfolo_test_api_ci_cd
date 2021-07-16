package com.kamtar.transport.api.service;



import org.springframework.stereotype.Service;

import com.kamtar.transport.api.model.Contact;
import com.kamtar.transport.api.model.Utilisateur;
import com.kamtar.transport.api.params.ContactParams;


@Service
public interface ContactService {
	
	Contact create(ContactParams params, Utilisateur utilisateur);
	
}
