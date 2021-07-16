package com.kamtar.transport.api.service.impl;

import com.kamtar.transport.api.service.WeatherAPIService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;


@Service(value="WeatherAPIService")
public class WeatherAPI_Openweathermap_ServiceImpl implements WeatherAPIService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(WeatherAPI_Openweathermap_ServiceImpl.class);

	@Value("${openweathermap.api}")
	private String openweathermap_api;

	@Value("${openweathermap.api.url}")
	private String openweathermap_api_url;


	@Override
	public List<String> getWeather(Double latitude, Double longitude, Integer nb_jours) {
		nb_jours++;
		final String uri = openweathermap_api_url + "/data/2.5/forecast/daily?lat=" + latitude + "&lon=" + longitude + "&cnt=" + nb_jours + "&appid=" + openweathermap_api;
		logger.info("uri openweathermap = " + uri);

		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.getForObject(uri, String.class);
		List<String> ret = new ArrayList<String>();


		// https://openweathermap.org/weather-conditions
		JSONParser parser = new JSONParser();
		JSONObject json = null;
		try {
			json = (JSONObject) parser.parse(result);
			JSONArray list = (JSONArray) json.get("list");
			logger.info("list = " + list.size());
			logger.info("nb_jours = " + nb_jours);
			int idx_dernier_element = nb_jours-1;

			JSONObject weather_day = (JSONObject)list.get(idx_dernier_element);
			JSONArray weather_arr = (JSONArray)weather_day.get("weather");
			JSONObject weather = (JSONObject)weather_arr.get(0);
			String main = (String) weather.get("main");
			String description = (String) weather.get("description");
			String icon = (String) weather.get("icon");
			Long id = (Long) weather.get("id");
			logger.info("main="+main+ " icon="+icon + " id=" + id + " description=" + description);

			ret.add(main + " (" + description + ")");
			ret.add(icon);
			ret.add(id.toString());

		} catch (ParseException e) {
			logger.info("Erreur au parsing de la réponse de " + uri, e);
		} catch (IndexOutOfBoundsException e) {
			// erreur silencieuse
		}

		logger.info("result="+result);


		return ret;

	}
}





