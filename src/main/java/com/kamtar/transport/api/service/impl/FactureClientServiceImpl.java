package com.kamtar.transport.api.service.impl;

import com.kamtar.transport.api.enums.FichierType;
import com.kamtar.transport.api.enums.OperationTypeTVA;
import com.kamtar.transport.api.enums.OperationTypeTVAValeur;
import com.kamtar.transport.api.enums.TemplateEmail;
import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.ContactParams;
import com.kamtar.transport.api.params.OperationFacturerParams;
import com.kamtar.transport.api.repository.ContactRepository;
import com.kamtar.transport.api.repository.FactureClientRepository;
import com.kamtar.transport.api.service.ContactService;
import com.kamtar.transport.api.service.EmailToSendService;
import com.kamtar.transport.api.service.FactureClientService;
import com.kamtar.transport.api.service.OperationService;
import com.kamtar.transport.api.utils.FileNameAwareByteArrayResource;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.text.*;
import java.util.*;


@Service(value="FactureClientService")
public class FactureClientServiceImpl implements FactureClientService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(FactureClientServiceImpl.class);

	@Value("${wbc.files.security.username}")
	private String wbc_files_security_username;

	@Value("${wbc.files.security.password}")
	private String wbc_files_security_password;

	@Value("${wbc.files.upload.url}")
	private String wbc_files_upload_url;

	@Value("${wbc.files.download.url}")
	private String wbc_files_download_url;

	@Value("${wbc.files.upload.path.facture.client}")
	private String wbc_files_upload_path_facture_client;

	@Autowired
	EmailToSendService emailToSendService;

	@Autowired
	OperationService operationService;

	@Autowired
	FactureClientRepository factureClientRepository;

	public Long countAll(Specification<FactureClient> conditions) {
		return factureClientRepository.count(conditions);
	}

	@Override
	public boolean delete(FactureClient facture) {

		if (facture.getFichier() != null) {

			// envoi du fichier
			MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			headers.add("Authorization", "Basic " + new String(Base64.encodeBase64((wbc_files_security_username + ":" + wbc_files_security_password).getBytes())));
			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);
			RestTemplate restTemplate = new RestTemplate();
			try {
				ResponseEntity<String> response = restTemplate.exchange(wbc_files_download_url + "/" + facture.getFichier(), HttpMethod.DELETE, requestEntity, String.class);
				HttpStatus httpCode = response.getStatusCode();
				if (httpCode.is2xxSuccessful()) {

					factureClientRepository.delete(facture);


				} else {
					logger.error("erreur au transfert du fichier = {}", httpCode);
				}
			} catch (RestClientException e) {
				logger.error("RestClientException", e);
			}

		} else {

			factureClientRepository.delete(facture);
		}


		return true;
	}

	@Override
	public FactureClient getByUUID(String uuid, String code_pays) {
		try {
			return factureClientRepository.findByUUID(UUID.fromString(uuid), code_pays);
		} catch (IllegalArgumentException e) {
			logger.warn("uuid invalide : " + uuid);
		}
		return null;
	}

	@Override
	public FactureClient getByNumero(String numero, String code_pays) {
		return factureClientRepository.getByNumero(numero, code_pays);
	}

	public Page<FactureClient> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<FactureClient> conditions) {
		Sort.Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return factureClientRepository.findAll(conditions, pageable);
	}

	@Override
	public FactureClient genererFacture(OperationFacturerParams postBody, String code_pays) throws IOException {

		// vérifie que toutes les opérations appartiennent au même client
		List<Operation> operations = new ArrayList<Operation>();
		Client client = null;
		for (String id_operation : postBody.getId_operations()) {
			Operation operation = operationService.getByUUID(id_operation, code_pays);
			if (operation != null) {
				if (client != null && !operation.getClient().equals(client)) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Toutes les opérations doivent être adressées au même client pour pouvoir lancer la facturation.");
				}

				if (operation.getFacture() != null && !operation.getFacture().trim().equals("")) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'opération " + operation.getCode() + " a déjà été facturée.");
				}

				if (operation.getTypeOperationTVA() == null || "".equals(operation.getTypeOperationTVA())) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le type de l'opération " + operation.getCode() + " (pour la TVA) n'a pas été renseigné.");
				}

				if (operation.getPrixAPayerParClient() == null) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le 'prix à payer par le client' de l'opération " + operation.getCode() + " n'est pas renseignée.");
				}

				operations.add(operation);
				client = operation.getClient();
			}
		}

		// tri par date d'opération
		Collections.sort(operations, new Comparator<Operation>() {
			@Override
			public int compare(Operation o1, Operation o2) {
				return o1.getDepartDateProgrammeeOperation().compareTo(o2.getDepartDateProgrammeeOperation());
			}
		});

		BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/facture_client.html"), "UTF-8"));

		String st;
		String content = "";
		while ((st = br.readLine()) != null) {
			content = content + st;
		}


		content = content.replaceAll("@CLIENT_NOM@", client.getNom());

		String adresse_facturation = client.getAdresseFacturationLigne1();
		if (adresse_facturation == null) {
			adresse_facturation = "";
		}
		if (client.getAdresseFacturationLigne2() != null && !"".equals(client.getAdresseFacturationLigne2().trim())) {
			adresse_facturation = adresse_facturation + "<br />" + client.getAdresseFacturationLigne2();
		}
		if (client.getAdresseFacturationLigne3() != null && !"".equals(client.getAdresseFacturationLigne3().trim())) {
			adresse_facturation = adresse_facturation + "<br />" + client.getAdresseFacturationLigne3();
		}
		if (client.getAdresseFacturationLigne4() != null && !"".equals(client.getAdresseFacturationLigne4().trim())) {
			adresse_facturation = adresse_facturation + "<br />" + client.getAdresseFacturationLigne4();
		}

		content = content.replaceAll("@CLIENT_ADRESSE@", adresse_facturation);
		content = content.replaceAll("@CLIENT_TEL@", client.getContactNumeroDeTelephone1());
		content = content.replaceAll("@CLIENT_EMAIL@", client.getContactEmail());
		content = content.replaceAll("@CLIENT_NCC@", client.getCompteContribuable() == null ? "": client.getCompteContribuable());

		content = content.replaceAll("@FACTURE_PAGE@", "1");

		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Calendar cal = Calendar.getInstance();
		Calendar cal_30j = Calendar.getInstance();
		cal_30j.add(Calendar.DATE, 30);


		DateFormat dateFormat2 = new SimpleDateFormat("yyyyMMddHHmmss");
		String numeroFacture = dateFormat2.format(new Date());

		content = content.replaceAll("@FACTURE_DATE@", dateFormat.format(cal.getTime()));
		content = content.replaceAll("@FACTURE_DATE_VALIDITE@",  dateFormat.format(cal_30j.getTime()));
		content = content.replaceAll("@FACTURE_NUMERO@", numeroFacture );
		content = content.replaceAll("@FACTURE_ID_CLIENT@", client.getNom());

		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
		symbols.setGroupingSeparator(' ');
		df.setDecimalFormatSymbols(symbols);

		String lignes_facture = "";
		Double somme_ht = new Double(0);
		Double montant_ttc = new Double(0);
		Double tva = new Double(new Double(0));
		String liste_operations = "@";
		for (Operation operation : operations) {
			String ligne_facture = "";
			ligne_facture = ligne_facture + "<tr>";
			ligne_facture = ligne_facture + "<td style='width:100px;padding:5px; font-size:10px;'>" + dateFormat.format(operation.getDepartDateProgrammeeOperation()) + "</td>";
			ligne_facture = ligne_facture + "<td style='width:100px;padding:5px;font-size:10px;'>" + operation.getCode() + "</td>";
			ligne_facture = ligne_facture + "<td style='width:80px;padding:5px;font-size:10px;'>" + operation.getVehicule().getImmatriculation() + "</td>";
			ligne_facture = ligne_facture + "<td style='width:100px;padding:5px;font-size:10px;'>" + operation.getDepartAdresseVille() + " -> " + operation.getArriveeAdresseVille() + "</td>";
			ligne_facture = ligne_facture + "<td style='width:110px;padding:5px;text-align:right;font-size:10px;'>" + df.format(operation.getPrixAPayerParClient().intValue()) + "</td>";
			ligne_facture = ligne_facture + "</tr>";

			lignes_facture = lignes_facture + ligne_facture;


			somme_ht = somme_ht + operation.getPrixAPayerParClient();
			montant_ttc = montant_ttc + operation.getPrixAPayerParClient();

			if (OperationTypeTVAValeur.map.get(OperationTypeTVA.valueOf(operation.getTypeOperationTVA())).booleanValue()) {
				Double tva_operation = operation.getPrixAPayerParClient() * new Double(0.18);
				tva = tva + tva_operation;
				montant_ttc = montant_ttc + tva_operation;
			}

			liste_operations = liste_operations + operation.getCode() + "@";
		}
		content = content.replaceAll("@LIGNES_FACTURE@", lignes_facture);



		Double pourcentage_remise = new Double(0);
		String notes_speciales = "";
		content = content.replaceAll("@FACTURE_NOTES@", notes_speciales);


		Float f = new Float(df.format(somme_ht.intValue()).replaceAll(" ", ""));

		content = content.replaceAll("@MONTANT_HT@", df.format(somme_ht.intValue()));
		content = content.replaceAll("@REMISE@",  df.format(pourcentage_remise.intValue()));

		content = content.replaceAll("@TVA@", df.format(tva.intValue()));
		content = content.replaceAll("@MONTANT_TTC@", df.format(montant_ttc.intValue()));

		content = content.replaceAll("@MONTANT_A_PAYER@", df.format(montant_ttc.intValue()));

		// echappement des caracères xml
		content = content.replaceAll("&", " - ");

		// génération de la facture
		String outputFile = "/tmp/facture_" + numeroFacture + ".pdf";

		try (OutputStream os = new FileOutputStream(outputFile)) {
			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.useFastMode();
			builder.withHtmlContent(content, "");
			builder.toStream(os);
			builder.run();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getStackTrace());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible de créer la facture");
		}

		// enregistre la facture en bdd
		FactureClient facture_client = new FactureClient();
		facture_client.setCodePays(client.getCodePays());
		facture_client.setClient(client);
		facture_client.setDateFacture(new Date());
		facture_client.setListeOperations(liste_operations);
		facture_client.setMontantHT(somme_ht);
		facture_client.setMontantTTC(montant_ttc);
		facture_client.setMontantTVA(tva);
		facture_client.setNetAPayer(montant_ttc);
		facture_client.setNotesSpeciales(notes_speciales);
		facture_client.setRemisePourcentage(pourcentage_remise);
		facture_client.setNumeroFacture(numeroFacture);
		facture_client = factureClientRepository.save(facture_client);


		for (Operation operation : operations) {
			operation.setFacture(facture_client.getNumeroFacture());
			operationService.save(operation);
		}

		// convertion en byte array
		File file = new File(outputFile);
		//init array with file length
		byte[] bytesArray = new byte[(int) file.length()];

		FileInputStream fis = new FileInputStream(file);
		fis.read(bytesArray); //read file into bytes[]
		fis.close();

		// envoi du fichier
		MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
		bodyMap.add("file", new FileNameAwareByteArrayResource("filename", bytesArray, "abc"));
		bodyMap.add("file_type", "application/pdf");
		bodyMap.add("async", true);
		bodyMap.add("foreign_key", facture_client.getUuid().toString());
		bodyMap.add("path_storage", wbc_files_upload_path_facture_client);
		bodyMap.add("public", false);
		bodyMap.add("md5", DigestUtils.md5Hex(bytesArray));
		bodyMap.add("correlation_id", UUID.randomUUID());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.add("Authorization", "Basic " + new String(Base64.encodeBase64((wbc_files_security_username + ":" + wbc_files_security_password).getBytes())));
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);
		RestTemplate restTemplate = new RestTemplate();
		try {
			ResponseEntity<String> response = restTemplate.exchange(wbc_files_upload_url, HttpMethod.POST, requestEntity, String.class);
			HttpStatus httpCode = response.getStatusCode();
			if (httpCode.is2xxSuccessful()) {

				// enregistre l'url de la photo principale de l'offre
				JSONParser parser = new JSONParser();
				try {
					JSONObject json = (JSONObject) parser.parse(response.getBody());
					String uuid = (String) json.get("uuid");

					facture_client.setFichier(uuid);

					factureClientRepository.save(facture_client);

					file.delete();

				} catch (ParseException e) {
					logger.error("ParseException", e);
				}

			} else {
				logger.error("erreur au transfert du fichier = {}", httpCode);
			}
		} catch (RestClientException e) {
			logger.error("RestClientException", e);
		}

		// envoi d'un email si adress email client renseignée
		if (client.getContactEmail() != null && !"".equals(client.getContactEmail())) {
			emailToSendService.envoyerFactureClient(facture_client, code_pays);
		}

		return facture_client;

	}


	@Override
	public byte[] get(String uuid) {

		// récupère le fichier

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
		headers.add("Authorization", "Basic " + new String(Base64.encodeBase64((wbc_files_security_username + ":" + wbc_files_security_password).getBytes())));
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
		try {
			ResponseEntity<byte[]> response = restTemplate.exchange(wbc_files_download_url + "/" + uuid, HttpMethod.GET, requestEntity, byte[].class, "1");
			HttpStatus httpCode = response.getStatusCode();
			if (httpCode.is2xxSuccessful()) {
				return response.getBody();
			} else {
				logger.error("erreur au transfert du fichier = {}", httpCode);
			}
		} catch ( RestClientException e) {
			logger.error("RestClientException", e);
		}

		return null;

	}

}





