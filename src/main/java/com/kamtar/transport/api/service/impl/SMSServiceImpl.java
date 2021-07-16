package com.kamtar.transport.api.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.swagger.ListOperation;
import com.wbc.core.utils.StringUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.kamtar.transport.api.enums.TemplateSMS;
import com.kamtar.transport.api.repository.SMSRepository;
import com.kamtar.transport.api.service.SMSService;
import com.kamtar.transport.api.utils.JWTProvider;

@Service(value="SMSService")
public class SMSServiceImpl implements SMSService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(SMSServiceImpl.class); 

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	SMSRepository smsRepositry;

	@Value("${contact.destinataire}")
	private String contact_destinataire;

	@Value("${wbc.sms.enable}")
	private boolean wbc_sms_enable;

	@Value("${wbc.sms.url}")
	private String wbc_sms_url;

	@Value("${wbc.sms.send.url}")
	private String wbc_sms_send_url;

	@Value("${wbc.sms.list.url}")
	private String wbc_sms_list_url;

	// liens kamtar-ci
	@Value("${lien.frontend.transporteur}")
	private String lien_frontend_transporteur;

	@Value("${lien.frontend.client}")
	private String lien_frontend_client;

	@Value("${lien.backoffice}")
	private String lien_backoffice;

	@Value("${numeros.telephone.kamtar.prevenir.operations}")
	private String numeros_telephone_kamtar_prevenir_operations;

	// liens kamtar-sn
	@Value("${lien_sn.frontend.transporteur}")
	private String lien_sn_frontend_transporteur;

	@Value("${lien_sn.frontend.client}")
	private String lien_sn_frontend_client;

	@Value("${lien_sn.backoffice}")
	private String lien_sn_backoffice;

	@Value("${numeros_sn.telephone.kamtar.prevenir.operations}")
	private String numeros_sn_telephone_kamtar_prevenir_operations;

	@Value("${wbc.sms.security.username}")
	private String wbc_sms_security_username;

	@Value("${wbc.sms.security.password}")
	private String wbc_sms_security_password;




	@Override
	public Map<String, Object> getAllPagined(String order_by, String order_dir, int page_number, int page_size, String destinataire, String code_pays, Integer statut) {
		Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

		if (wbc_sms_enable) {
			Map<String, Object> bodyMap = new HashMap<>();
			bodyMap.put("destinataire", destinataire);
			bodyMap.put("correlation_id", UUID.randomUUID());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add("Authorization", "Basic " + new String(Base64.encodeBase64((wbc_sms_security_username + ":" + wbc_sms_security_password).getBytes())));
			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);
			RestTemplate restTemplate = new RestTemplate();
			try {
				String url_get = wbc_sms_list_url + "?length=" + page_size + "&order=" + order_by + "&sort=" + SortDirection + "&page=" + page_number + "&destinataire=" + destinataire + "&custom_code_1=" + code_pays + "&statut=" + statut + "&custom_code_2=KAMTAR_TRANSPORT";
				ResponseEntity<String> response = restTemplate.exchange(url_get, HttpMethod.GET, requestEntity, String.class, bodyMap);
				HttpStatus httpCode = response.getStatusCode();
				if (httpCode.is2xxSuccessful()) {

					// enregistre l'url de la photo principale de l'offre
					JSONParser parser = new JSONParser();
					try {
						JSONObject json = (JSONObject) parser.parse(response.getBody());
						Long total = (Long) json.get("total");
						JSONArray data = (JSONArray) json.get("data");

						Map<String, Object> jsonDataResults = new LinkedHashMap<String, Object>();
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
		}


		return null;	
	}



	@Override
	public void send(SMS sms, String code_pays, boolean extension_deja_dans_numero) {

		logger.info("Envoi d'un SMS code_pays=" + code_pays + ", to_country=" + sms.getTo_country() + " ,to=" + sms.getTo());

		final SMS sms_saved = smsRepositry.save(sms);

		if (wbc_sms_enable) {
			Map<String, Object> bodyMap = new HashMap<>();
			bodyMap.put("to", sms.getTo());
			bodyMap.put("lang", sms.getLang());
			bodyMap.put("template", sms.getTemplate());
			bodyMap.put("country", sms.getTo_country());
			bodyMap.put("correlation_id", UUID.randomUUID());
			bodyMap.put("custom_code_1", code_pays);
			bodyMap.put("custom_code_2", "KAMTAR_TRANSPORT");
			bodyMap.put("custom_code_3", extension_deja_dans_numero ? "1": "0");

			List<String> variables = new ArrayList<String>();
			for (SMSVariable sms_variable : sms.getVariables()) {
				variables.add(sms_variable.getValeur());
			}
			bodyMap.put("parameters", variables);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add("Authorization", "Basic " + new String(Base64.encodeBase64((wbc_sms_security_username + ":" + wbc_sms_security_password).getBytes())));
			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<Map<String, Object>>(bodyMap, headers);
			RestTemplate restTemplate = new RestTemplate();
			try {
				ResponseEntity<String> response = restTemplate.exchange(wbc_sms_send_url, HttpMethod.PUT, requestEntity, String.class, bodyMap);
				HttpStatus httpCode = response.getStatusCode();
				if (httpCode.is2xxSuccessful()) {

					// tout va bien, on retourne le sms coté kamtar
					smsRepositry.delete(sms_saved);

				} else {
					logger.error("Erreur lors de l'envoi de l'email = {}", httpCode);
				}
			} catch (HttpClientErrorException e) {
				logger.error("HttpClientErrorException", e);
				logger.error("détails " + e.getStatusCode() + "-" + e.getResponseBodyAsString());

			} catch ( RestClientException e) {
				logger.error("RestClientException", e);
			} 


		}

	}


	public void avertiTransporteurOperation(Operation operation) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

		// transporteur
		SMS sms1 = new SMS(operation.getTransporteur().getNumeroTelephone1(), operation.getTransporteur().getCodePays(), TemplateSMS.NOUVELLE_COMMANDE_POUR_TRANSPORTEUR_KAMTAR_TRANSPORT, operation.getTransporteur().getLocale(), operation.getCodePays());
		sms1.getVariables().add(new SMSVariable("date_retrait", dateFormat.format(operation.getDepartDateProgrammeeOperation())));
		sms1.getVariables().add(new SMSVariable("lieu_depart", operation.getClient().getNom() + " (" + operation.getDepartAdresseComplete() + ")"));
		sms1.getVariables().add(new SMSVariable("lieu_arrivee", operation.getClient().getNom() + " (" + operation.getArriveeAdresseComplete() + ")"));
		sms1.getVariables().add(new SMSVariable("prenom_nom", operation.getTransporteur().getPrenomNom()));
		sms1.getVariables().add(new SMSVariable("code", operation.getCode().toString()));
		sms1.getVariables().add(new SMSVariable("immatriculation", operation.getVehicule().getImmatriculation()));
		send(sms1, operation.getClient().getCodePays(), false);

	}

	@Override
	public void avertiProprietaireAppelOffre(Operation operation, Vehicule vehicule) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

		// transporteur
		SMS sms = new SMS(vehicule.getProprietaire().getNumeroTelephone1(), vehicule.getProprietaire().getCodePays(), TemplateSMS.NOUVELLE_DEMANDE_OPERATION, vehicule.getProprietaire().getLocale(), vehicule.getProprietaire().getCodePays());
		if (operation.getDepartDateProgrammeeOperation() == null) {
			sms.getVariables().add(0, new SMSVariable("date_depart_operation", ""));

		} else {
			sms.getVariables().add(0, new SMSVariable("date_depart_operation", dateFormat.format(operation.getDepartDateProgrammeeOperation())));
		}
		sms.getVariables().add(1, new SMSVariable("vehicule", vehicule.getMarque() + " " + vehicule.getModeleSerie() + " " + vehicule.getImmatriculation()));

		String url_demandes = lien_frontend_transporteur ;
		if ("SN".equals(operation.getCodePays())) {
			url_demandes = lien_sn_frontend_transporteur;
		}

		sms.getVariables().add(2, new SMSVariable("url", url_demandes + "/#demandes"));

		send(sms, vehicule.getProprietaire().getCodePays(), false);

	}

	@Override
	public void validerCompteClient(UtilisateurClient client) {

		// envoi
		SMS sms = new SMS(client.getNumeroTelephone1(), client.getNumeroTelephone1Pays(), TemplateSMS.VALIDATION_COMPTE_CLIENT, client.getLocale(), client.getCodePays());

		String url = lien_frontend_client;
		if ("SN".equals(client.getCodePays())) {
			url = lien_sn_frontend_client;
		}

		sms.getVariables().add(0, new SMSVariable("code",  client.getCode_validation()));
		sms.getVariables().add(1, new SMSVariable("url",  url + "valider/" + client.getNumeroTelephone1()));

		send(sms, client.getCodePays(), false);

	}

	@Override
	public void previensKamtarNouvelleOperationClient(ListOperation operations) {
		logger.info("previensKamtarNouvelleOperationClient");

		Operation operation = operations.get(0);
		String liste_codes = operation.getCode().toString();
		if (operations.size() > 0) {
			List<String> liste_codes_operations = new ArrayList<String>();
			for (Operation operation_unitaire : operations) {
				liste_codes_operations.add(operation_unitaire.getCode().toString());
			}
			liste_codes = StringUtils.join(liste_codes_operations, ", ");
		}

		// liste des numéros kamtar séparés par une virgule
		String[] numeros_telephone = numeros_telephone_kamtar_prevenir_operations.split(",");
		String code_country_sms = "ci";
		if ("SN".equals(operation.getCodePays())) {
			numeros_telephone = numeros_sn_telephone_kamtar_prevenir_operations.split(",");
			code_country_sms = "sn";
		}

		for (int i=0; i<numeros_telephone.length; i++) {
			SMS sms = new SMS(numeros_telephone[i], code_country_sms, TemplateSMS.NOUVELLE_COMMANDE_CLIENT, "fr_FR", code_country_sms);

			String url = lien_backoffice;
			if ("SN".equals(operation.getCodePays())) {
				url = lien_sn_backoffice;
			}

			if (operations.size() == 1) {

				sms.getVariables().add(0, new SMSVariable("url", url + "operation/" + operation.getUuid().toString()));

			} else {

				sms.setTemplate(TemplateSMS.NOUVELLES_COMMANDES_CLIENT.toString());
				sms.getVariables().add(0, new SMSVariable("codes", liste_codes));
				sms.getVariables().add(1, new SMSVariable("url", url + "operation/" + operation.getUuid().toString()));

			}

			send(sms, operation.getCodePays(), true);

		}


	}


	@Override
	public void previensKamtarNouveauDevis(Devis devis) {
		logger.info("previensKamtarNouveauDevis");


		// liste des numéros kamtar séparés par une virgule
		String[] numeros_telephone = numeros_telephone_kamtar_prevenir_operations.split(",");
		String code_country_sms = "ci";
		if ("SN".equals(devis.getCodePays())) {
			numeros_telephone = numeros_sn_telephone_kamtar_prevenir_operations.split(",");
			code_country_sms = "sn";
		}

		for (int i=0; i<numeros_telephone.length; i++) {
			SMS sms = new SMS(numeros_telephone[i], code_country_sms, TemplateSMS.NOUVEAU_DEVIS, "fr_FR", code_country_sms);

			String url = lien_backoffice;
			if ("SN".equals(devis.getCodePays())) {
				url = lien_sn_backoffice;
			}

			sms.getVariables().add(0, new SMSVariable("url", url + "devis/" + devis.getUuid().toString()));
			send(sms, devis.getCodePays(), true);

		}


	}


}
