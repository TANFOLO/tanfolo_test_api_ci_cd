package com.kamtar.transport.api.service.impl;

import com.kamtar.transport.api.model.UtilisateurClient;
import com.kamtar.transport.api.model.UtilisateurDriver;
import com.kamtar.transport.api.repository.UtilisateurClientRepository;
import com.kamtar.transport.api.repository.UtilisateurDriverRepository;
import com.kamtar.transport.api.service.ClientPhotoService;
import com.kamtar.transport.api.service.DriverPhotoService;
import com.kamtar.transport.api.utils.FileNameAwareByteArrayResource;
import com.kamtar.transport.api.utils.ImageUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;


@Service
public class ClientPhotoServiceImpl implements ClientPhotoService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(ClientPhotoServiceImpl.class);

	@Autowired
	private UtilisateurClientRepository utilisateurClientRepository;

	@Value("${wbc.files.security.username}")
	private String wbc_files_security_username;

	@Value("${wbc.files.security.password}")
	private String wbc_files_security_password;

	@Value("${wbc.files.upload.url}")
	private String wbc_files_upload_url;

	@Value("${wbc.files.download.url}")
	private String wbc_files_download_url;

	@Value("${wbc.files.upload.path.client.photo}")
	private String wbc_files_upload_path_client_photo;



	public void savePhotoProfil(UtilisateurClient driver, String base64) {

		String partSeparator = ",";
		byte[] array_image = null;
		if (base64 != null && base64.contains(partSeparator)) {
			String encodedImg = base64.split(partSeparator)[1];
			array_image = java.util.Base64.getDecoder().decode(encodedImg.getBytes(StandardCharsets.UTF_8));

			// redimensionne ?? 1200px de width
			//array_image = ImageUtils.scale(array_image, 1200, 0);
		}


		if (array_image != null) {


			// envoi du fichier
			MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
			bodyMap.add("file", new FileNameAwareByteArrayResource(".png", array_image, "abc"));
			bodyMap.add("file_type", "image/png");
			bodyMap.add("async", false);
			bodyMap.add("foreign_key", driver.getUuid().toString());
			bodyMap.add("path_storage", wbc_files_upload_path_client_photo);
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
						utilisateurClientRepository.save(driver);

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

		// r??cup??re le fichier

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
