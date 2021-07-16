package com.kamtar.transport.api.service.impl;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kamtar.transport.api.enums.FichierType;
import com.kamtar.transport.api.model.UtilisateurProprietaire;
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

import com.kamtar.transport.api.model.UtilisateurDriver;
import com.kamtar.transport.api.model.Vehicule;
import com.kamtar.transport.api.model.VehiculePhoto;
import com.kamtar.transport.api.repository.UtilisateurDriverRepository;
import com.kamtar.transport.api.repository.VehiculePhotoRepository;
import com.kamtar.transport.api.repository.VehiculeRepository;
import com.kamtar.transport.api.service.DriverPhotoService;
import com.kamtar.transport.api.service.VehiculePhotoService;
import com.kamtar.transport.api.utils.FileNameAwareByteArrayResource;
import com.kamtar.transport.api.utils.ImageUtils;
import com.wbc.core.utils.FileUtils;


@Service
public class DriverPhotoServiceImpl implements DriverPhotoService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(DriverPhotoServiceImpl.class);  

	@Autowired
	private UtilisateurDriverRepository utilisateurDriverRepository; 

	@Value("${wbc.files.security.username}")
	private String wbc_files_security_username;

	@Value("${wbc.files.security.password}")
	private String wbc_files_security_password;

	@Value("${wbc.files.upload.url}")
	private String wbc_files_upload_url;

	@Value("${wbc.files.download.url}")
	private String wbc_files_download_url;

	@Value("${wbc.files.upload.path.driver.photo}")
	private String wbc_files_upload_path_driver_photo;

	@Value("${wbc.files.upload.path.driver.permis}")
	private String wbc_files_upload_path_driver_permis;


	@Override
	public void delete(UtilisateurDriver proproetaire) {
		proproetaire.setPhotoPermis(null);
		utilisateurDriverRepository.save(proproetaire);
	}



	public void savePhotoPermis(UtilisateurDriver driver, String base64) {

		if (base64 != null) {
			if ("".equals(base64)) {
				driver.setPhotoPermis(null);
				utilisateurDriverRepository.save(driver);
			} else {

				String partSeparator = ",";
				byte[] array_image = null;
				if (base64 != null && base64.contains(partSeparator)) {
					String encodedImg = base64.split(partSeparator)[1];
					array_image = java.util.Base64.getDecoder().decode(encodedImg.getBytes(StandardCharsets.UTF_8));

					// redimensionne à 1200px de width
					array_image = ImageUtils.scale(array_image, 1200, 0);
				}


				if (array_image != null) {


					// envoi du fichier
					MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
					bodyMap.add("file", new FileNameAwareByteArrayResource(".png", array_image, "abc"));
					bodyMap.add("file_type", "image/png");
					bodyMap.add("async", true);
					bodyMap.add("foreign_key", driver.getUuid().toString());
					bodyMap.add("path_storage", wbc_files_upload_path_driver_permis);
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

							// enregistre l'url de la photo principale de l'offre
							JSONParser parser = new JSONParser();
							try {
								JSONObject json = (JSONObject) parser.parse(response.getBody());
								String uuid = (String) json.get("uuid");
								driver.setPhotoPermis(uuid);

								utilisateurDriverRepository.save(driver);

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

		}
	}


	public void savePhotoProfil(UtilisateurDriver driver, String base64) {

		String partSeparator = ",";
		byte[] array_image = null;
		if (base64 != null && base64.contains(partSeparator)) {
			String encodedImg = base64.split(partSeparator)[1];
			array_image = java.util.Base64.getDecoder().decode(encodedImg.getBytes(StandardCharsets.UTF_8));

			// redimensionne à 1200px de width
			//array_image = ImageUtils.scale(array_image, 1200, 0);
		}


		if (array_image != null) {


			// envoi du fichier
			MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
			bodyMap.add("file", new FileNameAwareByteArrayResource(".png", array_image, "abc"));
			bodyMap.add("file_type", "image/png");
			bodyMap.add("async", false);
			bodyMap.add("foreign_key", driver.getUuid().toString());
			bodyMap.add("path_storage", wbc_files_upload_path_driver_photo);
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

						driver.setPhoto(uuid);
						utilisateurDriverRepository.save(driver);

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
