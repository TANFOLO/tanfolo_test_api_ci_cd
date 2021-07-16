package com.kamtar.transport.api.service.impl;

import com.kamtar.transport.api.enums.OperationStatut;
import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.OperationFacturerParams;
import com.kamtar.transport.api.repository.FactureProprietaireRepository;
import com.kamtar.transport.api.service.FactureProprietaireService;
import com.kamtar.transport.api.service.OperationChangementStatutService;
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


@Service(value="FactureProprietaireService")
public class FactureProprietaireServiceImpl implements FactureProprietaireService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(FactureProprietaireServiceImpl.class);

	@Value("${wbc.files.security.username}")
	private String wbc_files_security_username;

	@Value("${wbc.files.security.password}")
	private String wbc_files_security_password;

	@Value("${wbc.files.upload.url}")
	private String wbc_files_upload_url;

	@Value("${wbc.files.download.url}")
	private String wbc_files_download_url;

	@Value("${wbc.files.upload.path.facture.proprietaire}")
	private String wbc_files_upload_path_facture_proprietaie;

		@Autowired
	OperationChangementStatutService operationChangementStatutService;

	@Autowired
	OperationService operationService;

	@Autowired
	FactureProprietaireRepository factureProprietaireRepository;

	public Long countAll(Specification<FactureProprietaire> conditions) {
		return factureProprietaireRepository.count(conditions);
	}

	@Override
	public boolean delete(FactureProprietaire facture) {

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
					factureProprietaireRepository.delete(facture);
				} else {
					logger.error("erreur au transfert du fichier = {}", httpCode);
				}
			} catch (RestClientException e) {
				logger.error("RestClientException", e);
			}

		} else {
			factureProprietaireRepository.delete(facture);

		}

		return true;
	}

	@Override
	public Long countFacturesProprietaire(UtilisateurProprietaire proprietaire, String code_pays) {
		return factureProprietaireRepository.countFacturesProprietaire(proprietaire, code_pays);
	}


	@Override
	public FactureProprietaire getByUUID(String uuid, String code_pays) {
		try {
			return factureProprietaireRepository.findByUUID(UUID.fromString(uuid), code_pays);
		} catch (IllegalArgumentException e) {
			logger.warn("uuid invalide : " + uuid);
		}
		return null;
	}

	@Override
	public FactureProprietaire getByNumero(String numero, String code_pays) {
		List<FactureProprietaire> factures = factureProprietaireRepository.getByNumero(Long.valueOf(numero), code_pays);
		if (factures == null || factures.isEmpty()){
			return null;
		}
		return factures.get(0);
	}

	public Page<FactureProprietaire> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<FactureProprietaire> conditions) {
		Sort.Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return factureProprietaireRepository.findAll(conditions, pageable);
	}

	@Override
	public FactureProprietaire genererFacture(OperationFacturerParams postBody, String code_pays) throws IOException {

		// vérifie que toutes les opérations appartiennent au même client
		List<Operation> operations = new ArrayList<Operation>();
		UtilisateurProprietaire proprietaire = null;
		for (String id_operation : postBody.getId_operations()) {
			Operation operation = operationService.getByUUID(id_operation, code_pays);
			if (operation != null) {
				if (proprietaire != null && !operation.getVehicule().getProprietaire().equals(proprietaire)) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Toutes les opérations doivent avoir été réalisée avec des véhicules appartenant au même propriétaie.");
				}

				if (operation.getFactureProprietaire() != null && !operation.getFactureProprietaire().trim().equals("")) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'opération " + operation.getCode() + " a déjà été facturée.");
				}

				if (operation.getPrixDemandeParDriver() == null) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le 'prix demandé par le driver' de l'opération " + operation.getCode() + " n'est pas renseignée.");
				}

				// recherche l'historique des statuts pour savoir si l'opération est passé par le statut "Chargement Terminé"
				if (operation.getDateHeureChargementTermine() == null) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'opération " + operation.getCode() + " ne peut pas être facturée car elle n'a pas encore été chargée");
				}

				operations.add(operation);
				proprietaire = operation.getVehicule().getProprietaire();
			}
		}

		// tri par date d'opération
		Collections.sort(operations, new Comparator<Operation>() {
			@Override
			public int compare(Operation o1, Operation o2) {
				return o1.getDepartDateProgrammeeOperation().compareTo(o2.getDepartDateProgrammeeOperation());
			}
		});



		BufferedReader br = new BufferedReader(
				new InputStreamReader(
						this.getClass().getResourceAsStream("/facture_proprietaire.html"), "UTF-8"));

		String st;
		String content = "";
		while ((st = br.readLine()) != null) {
			content = content + st;
		}

		content = content.replaceAll("@TRANSPORTEUR_NOM@", proprietaire.getNomPrenom());
		String type_compte = "-";
		String adresse_facturation = "";
		if ("B".equals(proprietaire.getTypeCompte())) {
			type_compte = "Professionel";
			if (proprietaire.getAdresseFacturationLigne1() != null && !"".equals(proprietaire.getAdresseFacturationLigne1().trim())) {
				adresse_facturation = adresse_facturation + "<br />" + proprietaire.getAdresseFacturationLigne1();
			}
			if (proprietaire.getAdresseFacturationLigne2() != null && !"".equals(proprietaire.getAdresseFacturationLigne2().trim())) {
				adresse_facturation = adresse_facturation + "<br />" + proprietaire.getAdresseFacturationLigne2();
			}
			if (proprietaire.getAdresseFacturationLigne3() != null && !"".equals(proprietaire.getAdresseFacturationLigne3().trim())) {
				adresse_facturation = adresse_facturation + "<br />" + proprietaire.getAdresseFacturationLigne3();
			}
			if (proprietaire.getAdresseFacturationLigne4() != null && !"".equals(proprietaire.getAdresseFacturationLigne4().trim())) {
				adresse_facturation = adresse_facturation + "<br />" + proprietaire.getAdresseFacturationLigne4();
			}
		} else	if ("C".equals(proprietaire.getTypeCompte())) {
			type_compte = "Particulier";
		}
		content = content.replaceAll("@TRANSPORTEUR_COMPTE@", type_compte + adresse_facturation);

		content = content.replaceAll("@TRANSPORTEUR_NUMERO_CC@", proprietaire.getNumeroCarteTransport());
		content = content.replaceAll("@TRANSPORTEUR_CONTACT@", proprietaire.getNumeroTelephone1() + (proprietaire.getEmail() == null ? "": " - " + proprietaire.getEmail()));

		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Calendar cal = Calendar.getInstance();

		DateFormat dateFormat2 = new SimpleDateFormat("yyyyMMddHHmmss");

		// le numéro de facture est le numéro de l'opération minimum
		Long numeroFacture = new Long(999999999);
		for (Operation operation : operations) {
			if (numeroFacture > operation.getCode()) {
				numeroFacture = operation.getCode();
			}

		}

		//String numeroFacture = dateFormat2.format(new Date());

		content = content.replaceAll("@FACTURE_DATE@", dateFormat.format(cal.getTime()));
		content = content.replaceAll("@FACTURE_NUMERO@", numeroFacture.toString() );
		content = content.replaceAll("@FACTURE_CLIENT@", "Kamtar");
		content = content.replaceAll("@FACTURE_ADRESSE@", "22009001");

		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
		symbols.setGroupingSeparator(' ');
		df.setDecimalFormatSymbols(symbols);

		String lignes_facture = "";
		Integer somme_ttc = new Integer(0);
		Integer somme_ttc_sans_avance = new Integer(0);
		Integer somme_avance = new Integer(0);
		String liste_operations = "@";
		for (Operation operation : operations) {
			String ligne_facture = "";
			ligne_facture = ligne_facture + "<tr>";
			ligne_facture = ligne_facture + "<td style='padding:5px;text-align:right;'>" + dateFormat.format(operation.getDepartDateProgrammeeOperation()) + "</td>";
			ligne_facture = ligne_facture + "<td style='padding:5px;text-align:right;'>" + operation.getCode() + "</td>";
			ligne_facture = ligne_facture + "<td style='padding:5px;text-align:right;'>" + "" + "</td>";
			ligne_facture = ligne_facture + "<td style='padding:5px;text-align:right;'>" + operation.getVehicule().getImmatriculation() + "</td>";
			ligne_facture = ligne_facture + "<td style='padding:5px;text-align:right;'>" + operation.getDepartAdresseComplete() + " -> " + operation.getArriveeAdresseComplete() + "</td>";
			ligne_facture = ligne_facture + "<td style='padding:5px;text-align:right;'>" + operation.getTransporteur().getPrenomNom() + "</td>";
			ligne_facture = ligne_facture + "<td style='padding:5px;text-align:right;'>" + operation.getClient().getNom() + "</td>";
			ligne_facture = ligne_facture + "<td style='padding:5px;text-align:right;'>" + df.format(operation.getPrixDemandeParDriver().intValue());


			somme_ttc = somme_ttc + Math.toIntExact(operation.getPrixDemandeParDriver().longValue());
			somme_ttc_sans_avance = somme_ttc_sans_avance + Math.toIntExact(operation.getPrixDemandeParDriver().longValue());

			if (operation.getAvanceDonneAuDriver() != null && !new Float(0).equals(operation.getAvanceDonneAuDriver())) {
				ligne_facture = ligne_facture + " (avance : " + df.format(operation.getAvanceDonneAuDriver().intValue())/* + " " + operation.getAvanceDonneAuDriverDevise()*/ + ")";

				//somme_ttc = somme_ttc - Math.round(operation.getAvanceDonneAuDriver());
				somme_avance = somme_avance + Math.toIntExact(operation.getAvanceDonneAuDriver().longValue());

			}

			ligne_facture = ligne_facture +  "</td>";
			ligne_facture = ligne_facture + "</tr>";

			lignes_facture = lignes_facture + ligne_facture;


			liste_operations = liste_operations + operation.getCode() + "@";
		}
		content = content.replaceAll("@LIGNES_FACTURE@", lignes_facture);

		// airso (uniquement pour cote ivoire)
		if (!"CI".equals(operations.get(0).getCodePays())) {
			content = content.replaceAll("@CSS_AIRI@@", "#airsi { display:none; }");
		} else {
			content = content.replaceAll("@CSS_AIRI@@", "");
		}
		Integer airsi = new Integer(0);
		if (proprietaire.isAssujetiAIRSI()) {
			airsi = Math.round(new Float(somme_ttc_sans_avance * new Float(0.05)));
		}
		Integer montant_a_payer = somme_ttc /* + airsi */ -  somme_avance;

		content = content.replaceAll("@MONTANT_HT@", df.format(somme_ttc));
		content = content.replaceAll("@MONTANT_AIRSI@",  df.format(airsi));
		content = content.replaceAll("@MONTANT_AVANCE@",  df.format(somme_avance));

		content = content.replaceAll("@MONTANT_A_PAYER@", df.format(montant_a_payer));

		// echappement des caracères xml
		content = content.replaceAll("&", " - ");

		// génération e la facture
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
		FactureProprietaire facture_proprietaire = new FactureProprietaire();
		facture_proprietaire.setCodePays(proprietaire.getCodePays());
		facture_proprietaire.setProprietaire(proprietaire);
		facture_proprietaire.setDateFacture(new Date());
		facture_proprietaire.setListeOperations(liste_operations);
		facture_proprietaire.setMontantTTC(somme_ttc.doubleValue());
		facture_proprietaire.setMontantAirsi(airsi.doubleValue());
		facture_proprietaire.setNetAPayer(montant_a_payer.doubleValue());
		facture_proprietaire.setNumeroFacture(numeroFacture);
		facture_proprietaire = factureProprietaireRepository.save(facture_proprietaire);


		for (Operation operation : operations) {
			operation.setFactureProprietaire(facture_proprietaire.getNumeroFacture().toString());
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
		bodyMap.add("foreign_key", proprietaire.getUuid().toString());
		bodyMap.add("path_storage", wbc_files_upload_path_facture_proprietaie);
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

					facture_proprietaire.setFichier(uuid);

					factureProprietaireRepository.save(facture_proprietaire);

				} catch (ParseException e) {
					logger.error("ParseException", e);
				}

			} else {
				logger.error("erreur au transfert du fichier = {}", httpCode);
			}
		} catch (RestClientException e) {
			logger.error("RestClientException", e);
		}

		return facture_proprietaire;

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





