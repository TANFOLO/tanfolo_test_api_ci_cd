package com.kamtar.transport.api.service.impl;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.repository.UtilisateurClientPersonnelRepository;
import com.kamtar.transport.api.swagger.ListOperation;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.kamtar.transport.api.enums.TemplateEmail;
import com.kamtar.transport.api.repository.EmailToSendRepository;
import com.kamtar.transport.api.service.EmailToSendService;
import com.kamtar.transport.api.utils.JWTProvider;

@Service(value="EmailToSendService")
public class EmailToSendServiceImpl implements EmailToSendService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(EmailToSendServiceImpl.class); 

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	private EmailToSendRepository emailToSendRepository;

	@Autowired
	private UtilisateurClientPersonnelRepository utilisateurClientPersonnelRepository;

	// liens kamtar-ci
	@Value("${lien.frontend}")
	private String lien_frontend;

	@Value("${lien.backoffice}")
	private String lien_backoffice;

	@Value("${lien.frontend.transporteur}")
	private String lien_frontend_transporteur;

	@Value("${lien.frontend.client}")
	private String lien_frontend_client;

	@Value("${lien.facebook}")
	private String lien_facebook;

	@Value("${lien.contact}")
	private String lien_contact;

	@Value("${lien.email.prevenir.commande_client}")
	private String lien_email_prevenir_commande_client;

	@Value("${path.file.guide_CI.pdf.client}")
	private String path_file_guide_CI_pdf_client;

	@Value("${lien.email.prevenir.nouveau_client}")
	private String lien_email_prevenir_nouveau_client;



	// liens kamtar-sn
	@Value("${lien_sn.frontend}")
	private String lien_sn_frontend;

	@Value("${lien_sn.backoffice}")
	private String lien_sn_backoffice;

	@Value("${lien_sn.frontend.transporteur}")
	private String lien_sn_frontend_transporteur;

	@Value("${lien_sn.frontend.client}")
	private String lien_sn_frontend_client;

	@Value("${lien_sn.facebook}")
	private String lien_sn_facebook;

	@Value("${lien_sn.contact}")
	private String lien_sn_contact;

	@Value("${lien_sn.email.prevenir.commande_client}")
	private String lien_sn_email_prevenir_commande_client;

	@Value("${path.file.guide_SN.pdf.client}")
	private String path_file_guide_SN_pdf_client;

	@Value("${lien_sn.email.prevenir.nouveau_client}")
	private String lien_sn_email_prevenir_nouveau_client;



	@Value("${email.provider}")
	private String email_provider;
	
	@Value("${wbc.emails.url}")
	private String wbc_emails_url;

	@Value("${wbc.email.contenu.get.url}")
	private String wbc_email_contenu_get_url;

	@Value("${wbc.emails.send.url}")
	private String wbc_emails_send_url;

	@Value("${wbc.emails.security.username}")
	private String wbc_emails_security_username;

	@Value("${wbc.emails.security.password}")
	private String wbc_emails_security_password;




	private void callMicroserviceSendEmail(Map<String, Object> bodyMap, Email email, String code_pays) {

		String site_vitrine = lien_frontend;
		String lien_page_facebook = lien_facebook;
		String lien_adresse_email = lien_contact;

		if (code_pays.equals("SN")) {
			site_vitrine = lien_sn_frontend;
			lien_page_facebook = lien_sn_facebook;
			lien_adresse_email = lien_sn_contact;
		}
		bodyMap.put("lien_site_vitrine", site_vitrine);

		// ajout le custom code 1 = code pays opéré kamtar
		bodyMap.put("custom_code_1", code_pays);
		bodyMap.put("custom_code_2", "KAMTAR_TRANSPORT");

		if (email.getAttachment1() != null) {
			bodyMap.put("attachment_1", email.getAttachment1());
			bodyMap.put("attachment_1_name", email.getAttachment1_name());
			bodyMap.put("attachment_1_mime", email.getAttachment1_mime());
		}
		if (email.getAttachment2() != null) {
			bodyMap.put("attachment_2", email.getAttachment2().length());
			bodyMap.put("attachment_2_name", email.getAttachment2_name());
			bodyMap.put("attachment_2_mime", email.getAttachment2_mime());
		}
		if (email.getAttachment3() != null) {
			bodyMap.put("attachment_3", email.getAttachment3().length());
			bodyMap.put("attachment_3_name", email.getAttachment3_name());
			bodyMap.put("attachment_3_mime", email.getAttachment3_mime());
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", "Basic " + new String(Base64.encodeBase64((wbc_emails_security_username + ":" + wbc_emails_security_password).getBytes())));
		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<Map<String, Object>>(bodyMap, headers);
		RestTemplate restTemplate = new RestTemplate();
		try {
			ResponseEntity<String> response = restTemplate.exchange(wbc_emails_send_url, HttpMethod.PUT, requestEntity, String.class, bodyMap);
			HttpStatus httpCode = response.getStatusCode();
			if (httpCode.is2xxSuccessful()) {

				// tout va bien, on supprime le mail
				emailToSendRepository.delete(email);


			} else {
				logger.error("Erreur lors de l'envoi de l'email = {}", httpCode);
			}
		} catch ( RestClientException e) {
			logger.error("RestClientException", e);
		} 
	}
	
	public byte[] getMicroserviceSendEmail(String uuid_email) {

		RestTemplate restTemplate2 = new RestTemplate();
		restTemplate2.getMessageConverters().add(new ByteArrayHttpMessageConverter());
		HttpHeaders headers2 = new HttpHeaders();
		headers2.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
		headers2.add("Authorization", "Basic " + new String(Base64.encodeBase64((wbc_emails_security_username + ":" + wbc_emails_security_password).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>(headers2);
		String url = wbc_email_contenu_get_url + "?uuid=" + uuid_email;
		ResponseEntity<byte[]> response2 = restTemplate2.exchange(url, HttpMethod.GET, entity, byte[].class, "1");
		if (response2.getStatusCode() != HttpStatus.OK) {
			return null;
		}
		return response2.getBody();

	}

	private Map<String, Object> prepareMapMicroserviceEmail(Email email) {
		Map<String, Object> bodyMap = new HashMap<String, Object>();
		bodyMap.put("template", email.getTemplate());
		bodyMap.put("lang", email.getLang());
		bodyMap.put("provider_name", email_provider);
		bodyMap.put("recipient", email.getRecipient());
		bodyMap.put("correlation_id", UUID.randomUUID());
		return bodyMap;
	}

	/**
	 * Envoi un emaiul de mot de passe perdu
	 */
	@Override
	public Boolean envoyerLienMotDePassePerdu(Utilisateur utilisateur, MotDePassePerdu mot_de_passe, String code_pays) {

		// variables
		Map<String, Serializable> variables_corps_message = new HashMap<String, Serializable>();
		variables_corps_message.put("utilisateur", utilisateur);
		variables_corps_message.put("token", mot_de_passe.getToken().toString());
		
		String lien = "";
		String lien_site_vitrine = lien_frontend;
		if (utilisateur.getClass().equals(UtilisateurAdminKamtar.class) || utilisateur.getClass().equals(UtilisateurOperateurKamtar.class)) {
			lien = lien_backoffice;
			if ("SN".equals(code_pays)) {
				lien = lien_sn_backoffice;
				lien_site_vitrine = lien_sn_frontend;
			}
		} else if (utilisateur.getClass().equals(UtilisateurClient.class)) {
			lien = lien_frontend_client;
			if ("SN".equals(code_pays)) {
				lien = lien_frontend_client;
				lien_site_vitrine = lien_sn_frontend;
			}
		} else if (utilisateur.getClass().equals(UtilisateurDriver.class) || utilisateur.getClass().equals(UtilisateurProprietaire.class)) {
			lien = lien_frontend_transporteur;
			if ("SN".equals(code_pays)) {
				lien = lien_frontend_transporteur;
				lien_site_vitrine = lien_sn_frontend;
			}
		}
		
		variables_corps_message.put("lien", lien);
		variables_corps_message.put("lien_site_vitrine", lien_site_vitrine);
		List<String> variables_sujet_message = new ArrayList<String>();
		
		String locale = "FR";
		if (utilisateur.getLocale() != null) { 
			locale = utilisateur.getLocale();
		}
		
		// enregistrement en bdd
		Email email = new Email(TemplateEmail.ENVOYER_LIEN_MOT_DE_PASSE_PERDU_TRANSPORT, locale, utilisateur.getEmail(), variables_corps_message, variables_sujet_message, utilisateur.getCodePays());
		emailToSendRepository.save(email);
		
		// appel au micro service
		Map<String, Object> bodyMap = prepareMapMicroserviceEmail(email);
		bodyMap.put("variables", variables_corps_message);
		callMicroserviceSendEmail(bodyMap, email, code_pays);

		return true;
		
	}

	@Override
	public Boolean envoyerConfirmationOperation(Operation operation, String code_pays) {

		// variables
		Map<String, Serializable> variables_corps_message = new HashMap<String, Serializable>();
		variables_corps_message.put("operation", operation);


		String lien = lien_frontend_client;
		String lien_site_vitrine = lien_frontend;
		if ("SN".equals(code_pays)) {
			lien = lien_sn_frontend_client;
			lien_site_vitrine = lien_sn_frontend;
		}

		variables_corps_message.put("lien", lien);
		variables_corps_message.put("lien_site_vitrine", lien_site_vitrine);
		List<String> variables_sujet_message = new ArrayList<String>();

		String locale = "FR";
		if (operation.getClient().getLocale() != null) {
			locale = operation.getClient().getLocale();
		}

		// enregistrement en bdd
		Email email = new Email(TemplateEmail.CONFIRMATION_COMMANDE_TRANSPORT, locale, operation.getClient().getContactEmail(), variables_corps_message, variables_sujet_message, operation.getClient().getCodePays());
		emailToSendRepository.save(email);

		// appel au micro service
		Map<String, Object> bodyMap = prepareMapMicroserviceEmail(email);
		bodyMap.put("variables", variables_corps_message);
		callMicroserviceSendEmail(bodyMap, email, code_pays);

		return true;

	}

	@Override
	public Boolean envoyerPropositionPrixOperation(Operation operation, String code_pays) {

		//if (operation.getPrixAPayerParClient() != null && operation.getPrixAPayerParClient() > new Double(0)) {

			// variables
			Map<String, Serializable> variables_corps_message = new HashMap<String, Serializable>();
			variables_corps_message.put("operation", operation);


			String lien = lien_frontend_client;
			String lien_site_vitrine = lien_frontend;
			if ("SN".equals(code_pays)) {
				lien = lien_sn_frontend_client;
				lien_site_vitrine = lien_sn_frontend;
			}

			variables_corps_message.put("lien", lien);
			variables_corps_message.put("lien_site_vitrine", lien_site_vitrine);
			List<String> variables_sujet_message = new ArrayList<String>();

			String locale = "FR";
			if (operation.getClient().getLocale() != null) {
				locale = operation.getClient().getLocale();
			}

			// enregistrement en bdd
			Email email = new Email(TemplateEmail.PROPOSER_PRIX_COMMANDE_TRANSPORT, locale, operation.getClient().getContactEmail(), variables_corps_message, variables_sujet_message, operation.getClient().getCodePays());
			emailToSendRepository.save(email);

			// appel au micro service
			Map<String, Object> bodyMap = prepareMapMicroserviceEmail(email);
			bodyMap.put("variables", variables_corps_message);
			callMicroserviceSendEmail(bodyMap, email, code_pays);

		//}

		return true;

	}

	@Override
	public Boolean envoyerConfirmationCreationCompte(UtilisateurClient client, String code_pays) {

		// variables
		Map<String, Serializable> variables_corps_message = new HashMap<String, Serializable>();
		variables_corps_message.put("client", client);


		String lien = lien_frontend_client;
		String lien_site_vitrine = lien_frontend;
		String base64_guide_client_pdf = encoder(path_file_guide_CI_pdf_client);
		if ("SN".equals(code_pays)) {
			lien = lien_sn_frontend_client;
			lien_site_vitrine = lien_sn_frontend;
			base64_guide_client_pdf = encoder(path_file_guide_SN_pdf_client);
		}

		variables_corps_message.put("lien", lien);
		variables_corps_message.put("lien_site_vitrine", lien_site_vitrine);
		List<String> variables_sujet_message = new ArrayList<String>();

		String locale = "FR";
		if (client.getLocale() != null) {
			locale = client.getLocale();
		}

		// enregistrement en bdd
		Email email = new Email(TemplateEmail.CONFIRMATION_CREATION_COMPTE, locale, client.getEmail(), variables_corps_message, variables_sujet_message, client.getCodePays());
		email.setAttachment1(base64_guide_client_pdf);
		email.setAttachment1_mime("application/pdf");
		email.setAttachment1_name("guide_utilisateur.pdf");
		emailToSendRepository.save(email);

		// appel au micro service
		Map<String, Object> bodyMap = prepareMapMicroserviceEmail(email);
		bodyMap.put("variables", variables_corps_message);
		callMicroserviceSendEmail(bodyMap, email, code_pays);

		return true;

	}

	public static String encoder(String filePath) {
		String base64File = "";
		File file = new File(filePath);
		try (FileInputStream imageInFile = new FileInputStream(file)) {
			// Reading a file from file system
			byte fileData[] = new byte[(int) file.length()];
			imageInFile.read(fileData);
			base64File = java.util.Base64.getEncoder().encodeToString(fileData);
		} catch (FileNotFoundException e) {
			System.out.println("File not found" + e);
		} catch (IOException ioe) {
			System.out.println("Exception while reading the file " + ioe);
		}
		return base64File;
	}

	@Override
	public Boolean envoyerFinOperation(Operation operation, String code_pays) {

		// envoi les notifications au client
		envoyerEmailFinOperation(operation, code_pays, operation.getClient().getContactEmail());

		// envoi les notifications aux comptes personnels qui sont autorisés à recevoir les notifications
		List<UtilisateurClientPersonnel> clients_personnels = utilisateurClientPersonnelRepository.getClientsPersonnelsNotifications(operation.getClient(), operation.getClient().getCodePays());
		logger.info("clients_personnels notifications = " + clients_personnels.size());
		for (UtilisateurClientPersonnel clients_personnel : clients_personnels) {
			logger.info("clients_personnel notifications = " + clients_personnel);
			envoyerEmailFinOperation(operation, code_pays, clients_personnel.getEmail());
		}

		return true;

	}

	public void envoyerEmailFinOperation(Operation operation, String code_pays, String destinataire) {

		// variables
		Map<String, Serializable> variables_corps_message = new HashMap<String, Serializable>();
		variables_corps_message.put("operation", operation);

		String lien = lien_frontend_client;
		String lien_site_vitrine = lien_frontend;
		if ("SN".equals(code_pays)) {
			lien = lien_sn_frontend_client;
			lien_site_vitrine = lien_sn_frontend;
		}
		variables_corps_message.put("lien", lien);
		variables_corps_message.put("lien_site_vitrine", lien);
		List<String> variables_sujet_message = new ArrayList<String>();

		String locale = "FR";
		if (operation.getClient().getLocale() != null) {
			locale = operation.getClient().getLocale();
		}

		// enregistrement en bdd
		Email email = new Email(TemplateEmail.FIN_OPERATION, locale, destinataire, variables_corps_message, variables_sujet_message, operation.getClient().getCodePays());
		emailToSendRepository.save(email);

		// appel au micro service
		Map<String, Object> bodyMap = prepareMapMicroserviceEmail(email);
		bodyMap.put("variables", variables_corps_message);
		callMicroserviceSendEmail(bodyMap, email, code_pays);

	}

	@Override
	public Boolean envoyerFactureClient(FactureClient facture, String code_pays) {

		// variables
		Map<String, Serializable> variables_corps_message = new HashMap<String, Serializable>();
		variables_corps_message.put("facture", facture);

		String lien = lien_frontend_client + "#factures";
		String lien_site_vitrine = lien_frontend;
		if ("SN".equals(code_pays)) {
			lien = lien_sn_frontend_client + "#factures";
			lien_site_vitrine = lien_sn_frontend;
		}
		variables_corps_message.put("lien", lien);
		variables_corps_message.put("lien_site_vitrine", lien_site_vitrine);
		List<String> variables_sujet_message = new ArrayList<String>();

		String locale = "FR";
		if (facture.getClient().getLocale() != null) {
			locale = facture.getClient().getLocale();
		}

		// enregistrement en bdd
		Email email = new Email(TemplateEmail.ENVOYER_FACTURE_CLIENT, locale, facture.getClient().getContactEmail(), variables_corps_message, variables_sujet_message, facture.getClient().getCodePays());
		emailToSendRepository.save(email);

		// appel au micro service
		Map<String, Object> bodyMap = prepareMapMicroserviceEmail(email);
		bodyMap.put("variables", variables_corps_message);
		callMicroserviceSendEmail(bodyMap, email, code_pays);

		return true;


	}

	@Override
	public Boolean prevenirKamtarNouveauCompteClient(Client client, String code_pays) {

		// variables
		Map<String, Serializable> variables_corps_message = new HashMap<String, Serializable>();

		String lien = lien_backoffice;
		String recipient = lien_email_prevenir_commande_client;

		String site_vitrine = lien_frontend;
		String lien_page_facebook = lien_facebook;
		String lien_adresse_email = lien_contact;
		String email_destinataire = lien_email_prevenir_nouveau_client;

		if ("SN".equals(code_pays)) {
			lien = lien_sn_backoffice;
			recipient = lien_sn_email_prevenir_commande_client;

			site_vitrine = lien_sn_frontend;
			lien_page_facebook = lien_sn_facebook;
			lien_adresse_email = lien_sn_contact;

			email_destinataire = lien_sn_email_prevenir_nouveau_client;
		}
		variables_corps_message.put("lien_backoffice", lien);
		variables_corps_message.put("client", client);
		variables_corps_message.put("lien_site_vitrine", site_vitrine);
		List<String> variables_sujet_message = new ArrayList<String>();

		String locale = "FR";
		if (client.getLocale() != null) {
			locale = client.getLocale();
		}

		// enregistrement en bdd
		Email email = new Email(TemplateEmail.NOUVEAU_COMPTE_CLIENT, locale, email_destinataire, variables_corps_message, variables_sujet_message, client.getCodePays());
		emailToSendRepository.save(email);

		// appel au micro service
		Map<String, Object> bodyMap = prepareMapMicroserviceEmail(email);
		bodyMap.put("variables", variables_corps_message);
		callMicroserviceSendEmail(bodyMap, email, code_pays);

		return true;

	}

	@Override
	public Boolean prevenirKamtarNouveauDriverCreeParProprietaire(UtilisateurDriver driver, String code_pays) {

		// variables
		Map<String, Serializable> variables_corps_message = new HashMap<String, Serializable>();

		String lien = lien_backoffice;
		String recipient = lien_email_prevenir_commande_client;

		String site_vitrine = lien_frontend;
		String lien_page_facebook = lien_facebook;
		String lien_adresse_email = lien_contact;
		String email_destinataire = lien_email_prevenir_nouveau_client;

		if ("SN".equals(code_pays)) {
			lien = lien_sn_backoffice;
			recipient = lien_sn_email_prevenir_commande_client;

			site_vitrine = lien_sn_frontend;
			lien_page_facebook = lien_sn_facebook;
			lien_adresse_email = lien_sn_contact;

			email_destinataire = lien_sn_email_prevenir_nouveau_client;
		}
		variables_corps_message.put("lien_backoffice", lien);
		variables_corps_message.put("driver", driver);
		variables_corps_message.put("lien_site_vitrine", site_vitrine);
		List<String> variables_sujet_message = new ArrayList<String>();

		String locale = "FR";
		if (driver.getLocale() != null) {
			locale = driver.getLocale();
		}

		// enregistrement en bdd
		Email email = new Email(TemplateEmail.NOUVEAU_DRIVER_CREE_PAR_PROPRIETAIRE, locale, email_destinataire, variables_corps_message, variables_sujet_message, driver.getCodePays());
		emailToSendRepository.save(email);

		// appel au micro service
		Map<String, Object> bodyMap = prepareMapMicroserviceEmail(email);
		bodyMap.put("variables", variables_corps_message);
		callMicroserviceSendEmail(bodyMap, email, code_pays);

		return true;

	}

	@Override
	public Boolean prevenirKamtarNouveauVehiculeCreeParProprietaire(Vehicule vehicule, String code_pays) {

		// variables
		Map<String, Serializable> variables_corps_message = new HashMap<String, Serializable>();

		String lien = lien_backoffice;
		String recipient = lien_email_prevenir_commande_client;

		String site_vitrine = lien_frontend;
		String lien_page_facebook = lien_facebook;
		String lien_adresse_email = lien_contact;
		String email_destinataire = lien_email_prevenir_nouveau_client;

		if ("SN".equals(code_pays)) {
			lien = lien_sn_backoffice;
			recipient = lien_sn_email_prevenir_commande_client;

			site_vitrine = lien_sn_frontend;
			lien_page_facebook = lien_sn_facebook;
			lien_adresse_email = lien_sn_contact;

			email_destinataire = lien_sn_email_prevenir_nouveau_client;
		}
		variables_corps_message.put("lien_backoffice", lien);
		variables_corps_message.put("vehicule", vehicule);
		variables_corps_message.put("lien_site_vitrine", site_vitrine);
		List<String> variables_sujet_message = new ArrayList<String>();

		String locale = "FR";
		if (vehicule.getProprietaire().getLocale() != null) {
			locale = vehicule.getProprietaire().getLocale();
		}

		// enregistrement en bdd
		Email email = new Email(TemplateEmail.NOUVEAU_VEHICULE_CREE_PAR_PROPRIETAIRE, locale, email_destinataire, variables_corps_message, variables_sujet_message, vehicule.getCodePays());
		emailToSendRepository.save(email);

		// appel au micro service
		Map<String, Object> bodyMap = prepareMapMicroserviceEmail(email);
		bodyMap.put("variables", variables_corps_message);
		callMicroserviceSendEmail(bodyMap, email, code_pays);

		return true;


	}


	@Override
	public Boolean envoyerRapportJournalierClient(ListOperation operations, String client_email, String client_nom, String code_pays) {

		// variables
		Map<String, Serializable> variables_corps_message = new HashMap<String, Serializable>();

		String lien = lien_frontend_client;

		String site_vitrine = lien_frontend;
		String lien_page_facebook = lien_facebook;
		String lien_adresse_email = lien_contact;

		if ("SN".equals(code_pays)) {
			lien = lien_sn_frontend_client;

			site_vitrine = lien_sn_frontend;
			lien_page_facebook = lien_sn_facebook;
			lien_adresse_email = lien_sn_contact;

		}
		variables_corps_message.put("lien_webapp_client", lien_frontend_client);
		variables_corps_message.put("operations", operations);
		variables_corps_message.put("client_nom", client_nom);
		variables_corps_message.put("lien_site_vitrine", site_vitrine);
		List<String> variables_sujet_message = new ArrayList<String>();

		String locale = "FR";
		if (operations.get(0).getClient().getLocale() != null) {
			locale = operations.get(0).getClient().getLocale();
		}

		// enregistrement en bdd
		Email email = new Email(TemplateEmail.RAPPORT_JOURNALIER_OPERATIONS, locale, client_email, variables_corps_message, variables_sujet_message, operations.get(0).getCodePays());
		emailToSendRepository.save(email);

		// appel au micro service
		Map<String, Object> bodyMap = prepareMapMicroserviceEmail(email);
		bodyMap.put("variables", variables_corps_message);
		callMicroserviceSendEmail(bodyMap, email, code_pays);

		return true;


	}

	@Override
	public Boolean envoyerRapportHeboduClient(Long nb_operations_programmees, Long nb_operations_en_cours, Long nb_operations_terminees, String client_email, String client_nom, String code_pays, Utilisateur utilisateur) {

		// variables
		Map<String, Serializable> variables_corps_message = new HashMap<String, Serializable>();

		String lien = lien_frontend_client;

		String site_vitrine = lien_frontend;
		String lien_page_facebook = lien_facebook;
		String lien_adresse_email = lien_contact;

		if ("SN".equals(code_pays)) {
			lien = lien_sn_frontend_client;

			site_vitrine = lien_sn_frontend;
			lien_page_facebook = lien_sn_facebook;
			lien_adresse_email = lien_sn_contact;

		}
		variables_corps_message.put("lien_webapp_client", lien_frontend_client);
		variables_corps_message.put("nb_operations_programmees", nb_operations_programmees);
		variables_corps_message.put("nb_operations_en_cours", nb_operations_en_cours);
		variables_corps_message.put("nb_operations_terminees", nb_operations_terminees);
		variables_corps_message.put("client_nom", client_nom);
		variables_corps_message.put("lien_site_vitrine", site_vitrine);
		List<String> variables_sujet_message = new ArrayList<String>();

		String locale = "FR";
		if (utilisateur.getLocale() != null) {
			locale = utilisateur.getLocale();
		}

		// enregistrement en bdd
		Email email = new Email(TemplateEmail.RAPPORT_MENSUEL_OPERATIONS, locale, client_email, variables_corps_message, variables_sujet_message, utilisateur.getCodePays());
		emailToSendRepository.save(email);

		// appel au micro service
		Map<String, Object> bodyMap = prepareMapMicroserviceEmail(email);
		bodyMap.put("variables", variables_corps_message);
		callMicroserviceSendEmail(bodyMap, email, code_pays);

		return true;


	}

	@Override
	public Boolean prevenirKamtarNouvelleCommandeClient(ListOperation operations, String code_pays, Integer nbCamions) {

		// variables
		Map<String, Serializable> variables_corps_message = new HashMap<String, Serializable>();

		String lien = lien_backoffice;
		String recipient = lien_email_prevenir_commande_client;

		String site_vitrine = lien_frontend;
		String lien_page_facebook = lien_facebook;
		String lien_adresse_email = lien_contact;

		if ("SN".equals(code_pays)) {
			lien = lien_sn_backoffice;
			recipient = lien_sn_email_prevenir_commande_client;

			site_vitrine = lien_sn_frontend;
			lien_page_facebook = lien_sn_facebook;
			lien_adresse_email = lien_sn_contact;
		}

		Operation operation = operations.get(0);

		variables_corps_message.put("lien_backoffice", lien);
		variables_corps_message.put("lien_site_vitrine", site_vitrine);
		variables_corps_message.put("nbCamions", nbCamions);
		variables_corps_message.put("operation", operation);
		variables_corps_message.put("operations", operations);
		List<String> variables_sujet_message = new ArrayList<String>();

		String locale = "FR";
		if (operation.getClient().getLocale() != null) {
			locale = operation.getClient().getLocale();
		}

		// enregistrement en bdd
		Email email = new Email(TemplateEmail.NOUVELLE_COMMANDE_CLIENT, locale, recipient, variables_corps_message, variables_sujet_message, operation.getClient().getCodePays());
		emailToSendRepository.save(email);

		// appel au micro service
		Map<String, Object> bodyMap = prepareMapMicroserviceEmail(email);
		bodyMap.put("variables", variables_corps_message);
		callMicroserviceSendEmail(bodyMap, email, code_pays);

		return true;

	}


	@Override
	public Boolean prevenirKamtarNouveauDevis(ListOperation operations, String code_pays) {
		logger.info("prevenirKamtarNouveauDevis 2");

		// variables
		Map<String, Serializable> variables_corps_message = new HashMap<String, Serializable>();

		String lien = lien_backoffice;
		String recipient = lien_email_prevenir_commande_client;

		String site_vitrine = lien_frontend;
		String lien_page_facebook = lien_facebook;
		String lien_adresse_email = lien_contact;

		if ("SN".equals(code_pays)) {
			lien = lien_sn_backoffice;
			recipient = lien_sn_email_prevenir_commande_client;

			site_vitrine = lien_sn_frontend;
			lien_page_facebook = lien_sn_facebook;
			lien_adresse_email = lien_sn_contact;
		}

		variables_corps_message.put("lien_backoffice", lien);
		variables_corps_message.put("lien_site_vitrine", site_vitrine);
		variables_corps_message.put("operations", operations);
		variables_corps_message.put("operation", operations.get(0));
		List<String> variables_sujet_message = new ArrayList<String>();

		String locale = "FR";

		// enregistrement en bdd
		Email email = new Email(TemplateEmail.NOUVELLE_DEMANDE_DEVIS_CLIENT, locale, recipient, variables_corps_message, variables_sujet_message, code_pays);
		emailToSendRepository.save(email);

		// appel au micro service
		Map<String, Object> bodyMap = prepareMapMicroserviceEmail(email);
		bodyMap.put("variables", variables_corps_message);
		callMicroserviceSendEmail(bodyMap, email, code_pays);

		return true;

	}

	@Override
	public Boolean prevenirKamtarNouvelleReclamation(Reclamation reclamation, String code_pays) {
		logger.info("prevenirKamtarNouvelleReclamation");

		// variables
		Map<String, Serializable> variables_corps_message = new HashMap<String, Serializable>();

		String lien = lien_backoffice;

		String site_vitrine = lien_frontend;
		String lien_page_facebook = lien_facebook;
		String lien_adresse_email = lien_contact;

		if ("SN".equals(code_pays)) {
			lien = lien_sn_backoffice;

			site_vitrine = lien_sn_frontend;
			lien_page_facebook = lien_sn_facebook;
			lien_adresse_email = lien_sn_contact;
		}

		variables_corps_message.put("lien_backoffice", lien);
		variables_corps_message.put("lien_site_vitrine", site_vitrine);
		variables_corps_message.put("reclamation", reclamation);
		List<String> variables_sujet_message = new ArrayList<String>();

		String locale = "FR";

		// enregistrement en bdd
		Email email = new Email(TemplateEmail.NOUVELLE_RECLAMATION, locale, reclamation.getDestinataire(), variables_corps_message, variables_sujet_message, code_pays);
		emailToSendRepository.save(email);

		// appel au micro service
		Map<String, Object> bodyMap = prepareMapMicroserviceEmail(email);
		bodyMap.put("variables", variables_corps_message);
		callMicroserviceSendEmail(bodyMap, email, code_pays);

		return true;

	}

	@Override
	public Boolean confirmerClientNouvelleReclamation(Reclamation reclamation, String code_pays) {
		logger.info("confirmerClientNouvelleReclamation");

		// variables
		Map<String, Serializable> variables_corps_message = new HashMap<String, Serializable>();

		String lien = lien_frontend_client;

		String site_vitrine = lien_frontend;
		String lien_page_facebook = lien_facebook;
		String lien_adresse_email = lien_contact;

		if ("SN".equals(code_pays)) {
			lien = lien_sn_frontend_client;

			site_vitrine = lien_sn_frontend;
			lien_page_facebook = lien_sn_facebook;
			lien_adresse_email = lien_sn_contact;
		}

		variables_corps_message.put("lien", lien);
		variables_corps_message.put("lien_site_vitrine", site_vitrine);
		variables_corps_message.put("reclamation", reclamation);
		List<String> variables_sujet_message = new ArrayList<String>();

		String locale = "FR";

		String prenom_nom = "";
		String destinataire = reclamation.getClient().getContactEmail();
		if (reclamation.getClient_utilisateur() != null) {
			destinataire = reclamation.getClient_utilisateur().getEmail();
			prenom_nom = reclamation.getClient_utilisateur().getPrenomNom();
		} else if (reclamation.getClient_personnel() != null) {
			destinataire = reclamation.getClient_personnel().getEmail();
			prenom_nom = reclamation.getClient_personnel().getPrenomNom();
		}
		variables_corps_message.put("prenom_nom", prenom_nom);

		// enregistrement en bdd
		Email email = new Email(TemplateEmail.NOUVELLE_RECLAMATION_CONFIRMATION, locale, destinataire, variables_corps_message, variables_sujet_message, code_pays);
		emailToSendRepository.save(email);

		// appel au micro service
		Map<String, Object> bodyMap = prepareMapMicroserviceEmail(email);
		bodyMap.put("variables", variables_corps_message);
		callMicroserviceSendEmail(bodyMap, email, code_pays);

		return true;

	}

	@Override
	public Boolean prevenirClientNouvelleReclamationEchange(ReclamationEchange reclamation_echange, Reclamation reclamation, String code_pays) {
		logger.info("prevenirClientNouvelleReclamationEchange");

		// variables
		Map<String, Serializable> variables_corps_message = new HashMap<String, Serializable>();

		String lien = lien_frontend_client;

		String site_vitrine = lien_frontend;
		String lien_page_facebook = lien_facebook;
		String lien_adresse_email = lien_contact;

		if ("SN".equals(code_pays)) {
			lien = lien_sn_frontend_client;

			site_vitrine = lien_sn_frontend;
			lien_page_facebook = lien_sn_facebook;
			lien_adresse_email = lien_sn_contact;
		}

		variables_corps_message.put("reclamation", reclamation_echange.getReclamation());
		variables_corps_message.put("reclamation_echange", reclamation_echange);
		variables_corps_message.put("lien", lien);
		variables_corps_message.put("lien_site_vitrine", site_vitrine);
		List<String> variables_sujet_message = new ArrayList<String>();

		String locale = "FR";

		String prenom_nom = "";
		String destinataire = reclamation.getClient().getContactEmail();
		if (reclamation.getClient_utilisateur() != null) {
			destinataire = reclamation.getClient_utilisateur().getEmail();
			prenom_nom = reclamation.getClient_utilisateur().getPrenomNom();
		} else if (reclamation.getClient_personnel() != null) {
			destinataire = reclamation.getClient_personnel().getEmail();
			prenom_nom = reclamation.getClient_personnel().getPrenomNom();
		}
		variables_corps_message.put("prenom_nom", prenom_nom);

		// enregistrement en bdd
		Email email = new Email(TemplateEmail.NOUVELLE_RECLAMATION_ECHANGE, locale, destinataire, variables_corps_message, variables_sujet_message, code_pays);
		emailToSendRepository.save(email);

		// appel au micro service
		Map<String, Object> bodyMap = prepareMapMicroserviceEmail(email);
		bodyMap.put("variables", variables_corps_message);
		callMicroserviceSendEmail(bodyMap, email, code_pays);

		return true;


	}


	@Override
	public Boolean prevenirOperationAPartirDeDevis(Operation operation, String mot_de_passe, String code_pays) {
		logger.info("prevenirOperationAPartirDeDevis 2");

		// variables
		Map<String, Serializable> variables_corps_message = new HashMap<String, Serializable>();

		String lien = lien_frontend_client;
		String site_vitrine = lien_frontend;
		String lien_page_facebook = lien_facebook;
		String lien_adresse_email = lien_contact;

		if ("SN".equals(code_pays)) {
			lien = lien_sn_frontend_client;

			site_vitrine = lien_sn_frontend;
			lien_page_facebook = lien_sn_facebook;
			lien_adresse_email = lien_sn_contact;
		}

		variables_corps_message.put("lien", lien);
		variables_corps_message.put("lien_site_vitrine", site_vitrine);
		variables_corps_message.put("operation", operation);
		variables_corps_message.put("mot_de_passe", mot_de_passe);
		List<String> variables_sujet_message = new ArrayList<String>();

		String locale = "FR";

		// enregistrement en bdd
		Email email = new Email(TemplateEmail.CONFIRMATION_COMMANDE_A_PARTIR_DE_DEVIS, locale, operation.getClient().getContactEmail(), variables_corps_message, variables_sujet_message, code_pays);
		emailToSendRepository.save(email);

		// appel au micro service
		Map<String, Object> bodyMap = prepareMapMicroserviceEmail(email);
		bodyMap.put("variables", variables_corps_message);
		callMicroserviceSendEmail(bodyMap, email, code_pays);

		return true;

	}


}
