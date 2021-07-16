package com.kamtar.transport.api.service.impl;

import java.util.*;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.kamtar.transport.api.model.Email;
import com.kamtar.transport.api.model.Utilisateur;
import com.kamtar.transport.api.repository.EmailRepository;
import com.kamtar.transport.api.service.EmailService;
import com.kamtar.transport.api.service.EmailToSendService;
import com.kamtar.transport.api.utils.JWTProvider;

@Service(value="EmailService")
public class EmailServiceImpl implements EmailService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(EmailServiceImpl.class); 

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	EmailToSendService emailToSendService;

	@Autowired
	EmailRepository emailRepositry;

	@Value("${wbc.emails.enable}")
	private boolean wbc_email_enable;

	@Value("${wbc.emails.url}")
	private String wbc_email_url;

	@Value("${wbc.emails.send.url}")
	private String wbc_email_send_url;

	@Value("${wbc.emails.list.url}")
	private String wbc_email_list_url;

	@Value("${wbc.emails.security.username}")
	private String wbc_email_security_username;

	@Value("${wbc.emails.security.password}")
	private String wbc_email_security_password;

	@Override
	public Map<String, Object> getAllPagined(String order_by, String order_dir, int page_number, int page_size, String destinataire, String code_pays, Date createdDateBegin, Date createdDateEnd ) {
		Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Map<String, Object> jsonDataResults = new LinkedHashMap<String, Object>();
		
		/*if (!wbc_email_enable) {
			
			jsonDataResults.put("recordsTotal", 0);		
			jsonDataResults.put("recordsFiltered", 0);	
			jsonDataResults.put("data", new JSONArray());		
			
		} else {*/
			
			Map<String, Object> bodyMap = new HashMap<>();
			bodyMap.put("destinataire", destinataire); 
			bodyMap.put("correlation_id", UUID.randomUUID());
			bodyMap.put("custom_code_1", code_pays);
			bodyMap.put("custom_code_2", "KAMTAR_TRANSPORT");
			//bodyMap.put("date_debut", createdDateBegin.getTime());
			//bodyMap.put("date_fin", createdDateEnd.getTime());

			logger.info("bodyMap=" + bodyMap);

			Long date_debut = new Long(0);
			if (createdDateBegin != null) {
				date_debut = createdDateBegin.getTime();
			}
		Long date_fin = new Long(0);
		if (createdDateEnd != null) {
			date_fin = createdDateEnd.getTime();
		}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add("Authorization", "Basic " + new String(Base64.encodeBase64((wbc_email_security_username + ":" + wbc_email_security_password).getBytes())));
			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);
			RestTemplate restTemplate = new RestTemplate();
			try {
				String url_get = wbc_email_list_url + "?length=" + page_size + "&order=" + order_by + "&sort=" + SortDirection + "&page=" + page_number + "&destinataire=" + destinataire + "&custom_code_1=" + code_pays + "&custom_code_2=KAMTAR_TRANSPORT&date_debut=" + date_debut + "&date_fin=" + date_fin;
				logger.info("url_get=" +url_get);
				ResponseEntity<String> response = restTemplate.exchange(url_get, HttpMethod.GET, requestEntity, String.class, bodyMap);
				HttpStatus httpCode = response.getStatusCode();
				if (httpCode.is2xxSuccessful()) {
					
					// enregistre l'url de la photo principale de l'offre
					JSONParser parser = new JSONParser();
					try {
						JSONObject json = (JSONObject) parser.parse(response.getBody());
						Long total = (Long) json.get("total");
						JSONArray data = (JSONArray) json.get("data");
						
						jsonDataResults.put("recordsTotal", total);		
						jsonDataResults.put("recordsFiltered", total);	
						jsonDataResults.put("data", data);		
						return jsonDataResults;
						
					} catch (ParseException e) {
						logger.error("ParseException", e);
					}
					
					
					
				} else {
					logger.error("erreur au transfert du fichier = {}", httpCode);
				}
			} catch ( RestClientException e) {
				logger.error("RestClientException", e);
			}
		//}


		return jsonDataResults;
	}

	@Override
	public byte[] getContenu(String uuid) {
		return emailToSendService.getMicroserviceSendEmail(uuid);
	}





}
