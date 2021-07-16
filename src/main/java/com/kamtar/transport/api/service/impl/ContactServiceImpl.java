package com.kamtar.transport.api.service.impl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.kamtar.transport.api.enums.TemplateEmail;
import com.kamtar.transport.api.model.Contact;
import com.kamtar.transport.api.model.Utilisateur;
import com.kamtar.transport.api.params.ContactParams;
import com.kamtar.transport.api.repository.ContactRepository;
import com.kamtar.transport.api.service.ContactService;
import com.kamtar.transport.api.utils.FileNameAwareByteArrayResource;
import com.kamtar.transport.api.utils.ImageUtils;


@Service(value="ContactService")
public class ContactServiceImpl implements ContactService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(ContactServiceImpl.class); 

	@Value("${contact.destinataire}")
	private String contact_destinataire;

	@Value("${contact.destinataire_sn}")
	private String contact_destinataire_sn;

	@Value("${wbc.emails.enable}")
	private boolean wbc_emails_enable;

	@Value("${wbc.emails.url}")
	private String wbc_emails_url;

	@Value("${wbc.emails.send.url}")
	private String wbc_emails_send_url;

	@Value("${wbc.emails.security.username}")
	private String wbc_emails_security_username;

	@Value("${wbc.emails.security.password}")
	private String wbc_emails_security_password;

	@Autowired
	private ContactRepository contactRepository;


	// liens kamtar-ci
	@Value("${lien.frontend}")
	private String lien_frontend;

	@Value("${lien.frontend.client}")
	private String lien_frontend_client;

	// liens kamtar-sn
	@Value("${lien_sn.frontend}")
	private String lien_sn_frontend;

	@Value("${lien_sn.frontend.client}")
	private String lien_sn_frontend_client;

	@Override
	public Contact create(ContactParams params, Utilisateur utilisateur) {

		String destinataire = contact_destinataire;
		if (params.getPays() != null && params.getPays().equals("sn")) {
			destinataire = contact_destinataire_sn;
		}

		Map<String, Serializable> variables_corps_message = new HashMap<String, Serializable>();
		String lien = lien_frontend_client;
		String lien_site_vitrine = lien_frontend;
		if (utilisateur != null && "SN".equals(utilisateur.getCodePays())) {
			lien = lien_sn_frontend_client;
			lien_site_vitrine = lien_sn_frontend;
		}

		variables_corps_message.put("lien", lien);
		variables_corps_message.put("lien_site_vitrine", lien_site_vitrine);

		// enregistre en bdd
		Contact contact = new Contact(params, destinataire, params.getEmetteur_email());
		contact = contactRepository.save(contact);

		// envoi du mail
		if (wbc_emails_enable) {
			Map<String, Object> bodyMap = new HashMap<>();
			bodyMap.put("template", TemplateEmail.CONTACT_TRANSPORT.toString());
			bodyMap.put("lang", "fr_FR");
			bodyMap.put("recipient", destinataire);
			bodyMap.put("correlation_id", UUID.randomUUID());
			bodyMap.put("variables", variables_corps_message);

			String emetteur_nom = params.getEmetteur_nom() == null? "-": params.getEmetteur_nom();
			String emetteur_telephone = params.getEmetteur_telephone() == null? "-": params.getEmetteur_telephone();
			String emetteur_email = params.getEmetteur_email() == null? "-": params.getEmetteur_email();
			if (utilisateur != null) {
				emetteur_nom = utilisateur.getPrenomNom();
				emetteur_telephone = utilisateur.getNumeroTelephone1();
				emetteur_email = utilisateur.getEmail();
			}

			variables_corps_message.put("emetteur_nom", emetteur_nom);
			variables_corps_message.put("emetteur_telephone", emetteur_telephone);
			variables_corps_message.put("emetteur_email", emetteur_email);

			variables_corps_message.put("motif", params.getMotif());
			variables_corps_message.put("message", params.getMessage());

			bodyMap.put("variables", variables_corps_message);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add("Authorization", "Basic " + new String(Base64.encodeBase64((wbc_emails_security_username + ":" + wbc_emails_security_password).getBytes())));
			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);
			RestTemplate restTemplate = new RestTemplate();
			try {
				ResponseEntity<String> response = restTemplate.exchange(wbc_emails_send_url, HttpMethod.PUT, requestEntity, String.class, bodyMap);
				HttpStatus httpCode = response.getStatusCode();
				if (httpCode.is2xxSuccessful()) {
				} else {
					logger.error("erreur au transfert du fichier = {}", httpCode);
				}
			} catch ( RestClientException e) {
				logger.error("RestClientException", e);
			}
		}
		
		return contact;

	}






} 





