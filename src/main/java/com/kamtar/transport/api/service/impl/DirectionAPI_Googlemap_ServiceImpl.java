package com.kamtar.transport.api.service.impl;

import com.kamtar.transport.api.service.DirectionAPIService;
import com.kamtar.transport.api.service.WeatherAPIService;
import com.wbc.core.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service(value="DirectionAPIService")
public class DirectionAPI_Googlemap_ServiceImpl implements DirectionAPIService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(DirectionAPI_Googlemap_ServiceImpl.class);

	@Value("${googlemap.api}")
	private String googlemap_api;



	@Override
	public List<Long> getDuration(String depart, String arrivee, List<String> etapes, Date date_depart) {

		Long epoch_date_depart = date_depart.getTime() / 1000;

		List<Long> ret = new ArrayList<Long>();
		String uri_etapes = "";
		if (etapes != null && !etapes.isEmpty()) {

			for (int i=0; i<etapes.size(); i++) {
				etapes.set(i, "via:" + URLEncoder.encode(etapes.get(i)));
			}

			uri_etapes = "&waypoints=" + StringUtils.join(etapes, "|");
		}

		String uri = "https://maps.googleapis.com/maps/api/directions/json?origin=" + URLEncoder.encode(depart) + "&destination=" + URLEncoder.encode(arrivee) + "&departure_time=" + epoch_date_depart + "" + uri_etapes + "&key=" + googlemap_api;
		logger.info("uri=" + uri);

		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.getForObject(uri, String.class);

		logger.info("result = " + result);

		// https://openweathermap.org/weather-conditions
		JSONParser parser = new JSONParser();
		JSONObject json = null;
		try {
			json = (JSONObject) parser.parse(result);

			String status = json.get("status").toString();
			logger.info("status = " + status);
			if ("INVALID_REQUEST".equals(status)) {
				// la raison principale est que la date de départ indiqué est dans le passé.

				uri = "https://maps.googleapis.com/maps/api/directions/json?origin=" + URLEncoder.encode(depart) + "&destination=" + URLEncoder.encode(arrivee) + "&departure_time=now" + uri_etapes + "&key=" + googlemap_api;
				logger.info("uri2=" + uri);

				restTemplate = new RestTemplate();
				result = restTemplate.getForObject(uri, String.class);

				logger.info("result2 = " + result);

				json = (JSONObject) parser.parse(result);

				status = json.get("status").toString();
				logger.info("status2 = " + status);

			}


			JSONArray routes = (JSONArray) json.get("routes");
			if (!routes.isEmpty()) {
				JSONObject route = (JSONObject) routes.get(0);

				JSONArray legs = (JSONArray) route.get("legs");
				JSONObject leg = (JSONObject) legs.get(0);

				JSONObject distance = (JSONObject) leg.get("distance");
				JSONObject duration_in_traffic = (JSONObject) leg.get("duration_in_traffic");

				if (distance != null) {
					Long distance_text = (Long) distance.get("value");
					ret.add(distance_text / 1000); // passer de metre en km
					logger.info("distance_text=" + distance_text);
				} else {
					ret.add(new Long(0));
				}

				if (duration_in_traffic != null) {
					Long duration_in_traffic_text = (Long) duration_in_traffic.get("value");
					ret.add(duration_in_traffic_text / 60); // passage de seconde en minutes
					logger.info("duration_in_traffic_text=" + duration_in_traffic_text);
				} else {
					ret.add(new Long(0));
				}
			} else {
				ret.add(new Long(0));
				ret.add(new Long(0));
			}


		} catch (ParseException e) {
			logger.info("Erreur au parsing de la réponse de " + uri, e);
		}


		return ret;

	}
}





