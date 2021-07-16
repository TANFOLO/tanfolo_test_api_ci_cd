package com.kamtar.transport.api.service.impl;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.kamtar.transport.api.model.Operation;
import com.kamtar.transport.api.model.OperationDocument;
import com.kamtar.transport.api.repository.OperationDocumentRepository;
import com.kamtar.transport.api.repository.VehiculeRepository;
import com.kamtar.transport.api.service.OperationDocumentService;
import com.kamtar.transport.api.utils.FileNameAwareByteArrayResource;
import com.kamtar.transport.api.utils.ImageUtils;
import com.wbc.core.utils.FileUtils;


@Service
public class OperationDocumentServiceImpl implements OperationDocumentService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(OperationDocumentServiceImpl.class);  

	@Autowired
	private OperationDocumentRepository operationDocumentRepository; 

	@Value("${wbc.files.security.username}")
	private String wbc_files_security_username;

	@Value("${wbc.files.security.password}")
	private String wbc_files_security_password;

	@Value("${wbc.files.upload.url}")
	private String wbc_files_upload_url;

	@Value("${wbc.files.download.url}")
	private String wbc_files_download_url;

	@Value("${wbc.files.upload.path.operation.documents}")
	private String wbc_files_upload_path_operation_documents;



	public void saveDocument(Operation operation, String base64, Integer ordre, String type_compte) {
		logger.info("saveDocument1 = " + base64.length());

		String partSeparator = ",";
		byte[] array_image = null;
		if (base64 != null && base64.contains(partSeparator)) {
			String encodedImg = base64.split(partSeparator)[1];
			array_image = java.util.Base64.getDecoder().decode(encodedImg.getBytes(StandardCharsets.UTF_8));

		}

		logger.info("saveDocument2 = " + operation.getCode());

		if (array_image != null) {

			// enregistrement en bdd
			OperationDocument photo_vehicule = new OperationDocument(ordre, operation, Long.valueOf(base64.length()), "png", ordre.toString(), type_compte);
			photo_vehicule = operationDocumentRepository.save(photo_vehicule);

			String folder_s3 = wbc_files_upload_path_operation_documents + operation.getUuid().toString() + "/";

			// envoi du fichier
			MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
			bodyMap.add("file", new FileNameAwareByteArrayResource(".png", array_image, "abc"));
			bodyMap.add("file_type", "image/png");
			bodyMap.add("async", false);
			bodyMap.add("foreign_key", operation.getUuid().toString());
			bodyMap.add("path_storage", folder_s3);
			bodyMap.add("public", false);
			bodyMap.add("md5", DigestUtils.md5Hex(array_image));
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

					// enregistre l'url de la photo du carroussel
					JSONParser parser = new JSONParser();
					try {
						JSONObject json = (JSONObject) parser.parse(response.getBody());
						String uuid = (String) json.get("uuid");

						photo_vehicule.setDocument(uuid);
						operationDocumentRepository.save(photo_vehicule);

					} catch (ParseException e) {
						e.printStackTrace();
					}

				} else {
					logger.error("erreur au transfert du fichier = {}", httpCode);
				}
			} catch (RestClientException e) {
				logger.error("RestClientException", e);
			}
		}
	}



	public void saveDocument(Operation operation, byte[] array_image, Integer ordre, String filename, String type_compte) {


		logger.info("saveDocument2 = " + array_image.length);
		logger.info("saveDocument2 = " + operation.getCode());

		if (array_image != null) {

			// enregistrement en bdd
			OperationDocument photo_vehicule = new OperationDocument(ordre, operation, Long.valueOf(array_image.length), "png", filename, type_compte);
			photo_vehicule = operationDocumentRepository.save(photo_vehicule);

			String folder_s3 = wbc_files_upload_path_operation_documents + operation.getUuid().toString() + "/";

			// envoi du fichier
			MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
			bodyMap.add("file", new FileNameAwareByteArrayResource(".png", array_image, "abc"));
			bodyMap.add("file_type", "image/png");
			bodyMap.add("async", false);
			bodyMap.add("foreign_key", operation.getUuid().toString());
			bodyMap.add("path_storage", folder_s3);
			bodyMap.add("public", false);
			bodyMap.add("md5", DigestUtils.md5Hex(array_image));
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

					// enregistre l'url de la photo du carroussel
					JSONParser parser = new JSONParser();
					try {
						JSONObject json = (JSONObject) parser.parse(response.getBody());
						String uuid = (String) json.get("uuid");

						photo_vehicule.setDocument(uuid);
						operationDocumentRepository.save(photo_vehicule);

					} catch (ParseException e) {
						e.printStackTrace();
					}

				} else {
					logger.error("erreur au transfert du fichier = {}", httpCode);
				}
			} catch (RestClientException e) {
				logger.error("RestClientException", e);
			}
		}
	}





	public void saveDocument(Operation operation, File photo, Integer ordre, String type_compte) {

		if (photo != null && photo.exists() && photo.isFile() && photo.length() > 0) {

			// trouve l'ordre à partir du nom du fichier
			String nom_photo = FileUtils.removeExtension(photo.getName());

			// enregistrement en bdd
			OperationDocument photo_vehicule = new OperationDocument(ordre, operation, photo.length(), FileUtils.getExtension(photo.getAbsolutePath()), nom_photo, type_compte);
			photo_vehicule = operationDocumentRepository.save(photo_vehicule);

			String folder_s3 = wbc_files_upload_path_operation_documents + operation.getUuid().toString() + "/";

			// envoi du fichier
			MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
			bodyMap.add("file", new FileSystemResource(photo.getAbsolutePath()));
			bodyMap.add("file_type", "image/png");
			bodyMap.add("async", false);
			bodyMap.add("foreign_key", operation.getUuid().toString());
			bodyMap.add("path_storage", folder_s3); 
			bodyMap.add("public", false);
			bodyMap.add("md5", FileUtils.md5File(photo));
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

					// enregistre l'url de la photo du carroussel
					JSONParser parser = new JSONParser();
					try {
						JSONObject json = (JSONObject) parser.parse(response.getBody());
						String uuid = (String) json.get("uuid");

						photo_vehicule.setDocument(uuid);
						operationDocumentRepository.save(photo_vehicule);

					} catch (ParseException e) {
						e.printStackTrace();
					}

				} else {
					logger.error("erreur au transfert du fichier = {}", httpCode);
				}
			} catch ( RestClientException e) {
				logger.error("RestClientException", e);
			}
		}
	}




	@Async
	public void saveDocuments(Operation operation, File folder, String type_compte) {

		File[] directoryListing = folder.listFiles();
		if (directoryListing != null) {
			int i=0;
			for (File child : directoryListing) {
				saveDocument(operation, child, i, type_compte);
				i++;
			}
		}
	}

	public List<OperationDocument> getOperationDocuments(Operation operation) {
		return operationDocumentRepository.findOperationDocuments(operation);
	}

	public Long countOperationDocuments(Operation operation) {
		return operationDocumentRepository.countOperationDocuments(operation);
	}

	public void delete(OperationDocument photo) {
		operationDocumentRepository.delete(photo);
	}

	public Optional<OperationDocument> get(Operation offre, String filename) {
		return operationDocumentRepository.get(offre, filename);
	}

	@Override
	public Optional<OperationDocument> load(String uuid) {
		return operationDocumentRepository.findById(UUID.fromString(uuid));

	}


	@Override
	public byte[] get(String uuid) {
		
		if (uuid == null) {
			return null;
		}

		// lit le fichier en bdd de kamtar tansport
		Optional<OperationDocument> document = operationDocumentRepository.findById(UUID.fromString(uuid));
		if (!document.isPresent()) {
			return null;
		}
		// récupère le fichier chez wbc-files

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
		headers.add("Authorization", "Basic " + new String(Base64.encodeBase64((wbc_files_security_username + ":" + wbc_files_security_password).getBytes())));
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
		try {
			ResponseEntity<byte[]> response = restTemplate.exchange(wbc_files_download_url + "/" + document.get().getDocument(), HttpMethod.GET, requestEntity, byte[].class, "1");
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
