package com.kamtar.transport.api.service.impl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
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
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.kamtar.transport.api.enums.FichierType;
import com.kamtar.transport.api.model.UtilisateurProprietaire;
import com.kamtar.transport.api.repository.UtilisateurClientRepository;
import com.kamtar.transport.api.repository.UtilisateurProprietaireRepository;
import com.kamtar.transport.api.service.DocumentsProprietaireService;
import com.kamtar.transport.api.utils.FileNameAwareByteArrayResource;
import com.kamtar.transport.api.utils.ImageUtils;


@Service(value="DocumentsProprietaireService")
public class DocumentsProprietaireServiceImpl implements DocumentsProprietaireService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(DocumentsProprietaireServiceImpl.class); 

	@Value("${wbc.files.security.username}")
	private String wbc_files_security_username;

	@Value("${wbc.files.security.password}")
	private String wbc_files_security_password;

	@Value("${wbc.files.upload.url}")
	private String wbc_files_upload_url;

	@Value("${wbc.files.download.url}")
	private String wbc_files_download_url;

	@Value("${wbc.files.upload.path.proprietaire.carte_transport}")
	private String wbc_files_upload_path_proprietaire_carte_transport;
	
	@Autowired
	private UtilisateurProprietaireRepository utilisateurProprietaireRepository; 


	@Override
	@Async
	public void create(String base64, UtilisateurProprietaire proproetaire, FichierType type) {

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
			bodyMap.add("file", new FileNameAwareByteArrayResource("filename", array_image, "abc"));
			bodyMap.add("file_type", "image/png");
			bodyMap.add("async", true);
			bodyMap.add("foreign_key", proproetaire.getUuid().toString());
			if (type.equals(FichierType.CARTE_TRANSPORT_PROPRIETAIRE)) {
				bodyMap.add("path_storage", wbc_files_upload_path_proprietaire_carte_transport);
			}
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
						if (type.equals(FichierType.CARTE_TRANSPORT_PROPRIETAIRE)) {
							proproetaire.setPhotoCarteTransport(uuid);
						}
						utilisateurProprietaireRepository.save(proproetaire);

					} catch (ParseException e) {
						logger.error("ParseException", e);
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
	public void delete(UtilisateurProprietaire proproetaire) {
		proproetaire.setPhotoCarteTransport(null);
		utilisateurProprietaireRepository.save(proproetaire);
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





