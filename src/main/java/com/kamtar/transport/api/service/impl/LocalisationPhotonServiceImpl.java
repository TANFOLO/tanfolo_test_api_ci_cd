package com.kamtar.transport.api.service.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kamtar.transport.api.classes.Adresse;
import com.kamtar.transport.api.service.LocalisationService;

import net.sf.json.JSONObject;

@Service
public class LocalisationPhotonServiceImpl implements LocalisationService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(LocalisationPhotonServiceImpl.class);  


	public static final Charset UTF_8 = Charset.forName("UTF-8");

	@Value("${photon.komoot.de.api.retour.encoder}")
	private boolean photon_komoot_de_api_retour_encoder;

	@Override
	public List<Adresse> autocomplete(String query) {

		List<Adresse> res = new ArrayList<Adresse>();

		HttpURLConnection conn = null;
		StringBuilder jsonResults = new StringBuilder();
		StringBuilder sb = null;
		try {
			sb = new StringBuilder("https://photon.komoot.de/api/");
			sb.append("?q=" + URLEncoder.encode(query.toLowerCase().replaceAll(" ", "+"), "utf8"));

			URL url = new URL(sb.toString());
			conn = (HttpURLConnection) url.openConnection();
			InputStreamReader in = new InputStreamReader(conn.getInputStream());


			if (conn.getResponseCode() == 200) {
				int read;
				char[] buff = new char[1024];
				while ((read = in.read(buff)) != -1) {
					jsonResults.append(buff, 0, read);
				}
			} else {
				logger.error("Ereur API Autocomplete " + sb.toString() + " Code HTTP " + conn.getResponseCode());
			}


		} catch (MalformedURLException e) {
			logger.error("Error MalformedURLException pendant l'appel à " + sb.toString() + " : ", e);
		} catch (IOException e) {
			logger.error("Error IOException pendant l'appel à " + sb.toString() + " : ", e);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		JSONObject jsonObj = null;

		if (jsonResults.toString() != null && !"".equals(jsonResults.toString().trim())) {
			jsonObj = JSONObject.fromObject(jsonResults.toString()); 


			/*
		{
			"features": [{
				"geometry": {
					"coordinates": [-1.5125356, // longitude
					47.2709309],
					"type": "Point"
				},
				"type": "Feature",
				"properties": {
					"osm_id": 1719286532,
					"osm_type": "N",
					"country": "France",
					"osm_key": "place",
					"housenumber": "3",
					"city": "Nantes",
					"street": "Rue des Micocouliers",
					"osm_value": "house",
					"postcode": "44300",
					"state": "Pays de la Loire"
				}
			}],
			"type": "FeatureCollection"
		}
			 */

			if (jsonObj.containsKey("features")) {

				for (int i=0; i<jsonObj.getJSONArray("features").size(); i++) {

					JSONObject feature = jsonObj.getJSONArray("features").getJSONObject(i);

					JSONObject geometry = feature.getJSONObject("geometry");
					Double latitude = geometry.getJSONArray("coordinates").getDouble(1);
					Double longitude = geometry.getJSONArray("coordinates").getDouble(0);

					JSONObject properties = feature.getJSONObject("properties");

					String adresse = "";
					String ville = "";
					String rue = "";
					if (properties.containsKey("housenumber")) {
						adresse = adresse + properties.getString("housenumber");
					}
					if (properties.containsKey("street")) {
						adresse = adresse + (adresse.equals("") ? "": " ");
						adresse = adresse + properties.getString("street");

						rue = adresse;
					}
					if (properties.containsKey("name")) {
						adresse = adresse + (adresse.equals("") ? "": " ");
						adresse = adresse + properties.getString("name");
						rue = properties.getString("name");
					}
					if (properties.containsKey("postcode")) {
						adresse = adresse + (adresse.equals("") ? "": ", ");
						adresse = adresse + properties.getString("postcode");
					}
					if (properties.containsKey("city")) {
						adresse = adresse + (adresse.equals("") ? "": " ");
						adresse = adresse + properties.getString("city");
						ville = properties.getString("city");
					}
					if (properties.containsKey("state")) {
						adresse = adresse + (adresse.equals("") ? "": ", ");
						adresse = adresse + properties.getString("state");
					}
					String country = "";
					if (properties.containsKey("country")) {
						adresse = adresse + (adresse.equals("") ? "": ", ");
						adresse = adresse + properties.getString("country");
						country = properties.getString("country");
					}




					Adresse adresse_object = new Adresse(latitude, longitude, encodeRetourAPI(adresse), encodeRetourAPI(country), encodeRetourAPI(ville), encodeRetourAPI(rue));
					res.add(adresse_object);


				}

			}

		}

		return res;
	}

	/**
	 * Les chaines de caractères retournées par photon.komoot.de ne sont pas utf8
	 * Il faut les convertir
	 * @param adresse
	 * @return
	 */
	String encodeRetourAPI(String adresse) {
		if (!photon_komoot_de_api_retour_encoder) {
			return adresse;
		} else {
			byte ptext[];
			String value = "";
			try {
				ptext = adresse.getBytes("ISO-8859-1");
				value = new String(ptext, "UTF-8"); 
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} 
			return value;
		}
	}

}
