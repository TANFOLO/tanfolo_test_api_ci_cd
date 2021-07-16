package com.kamtar.transport.api.service;

import com.kamtar.transport.api.model.Client;
import com.kamtar.transport.api.model.UtilisateurClient;
import com.kamtar.transport.api.params.CreateClientAnonymeParams;
import com.kamtar.transport.api.params.CreateClientParams;
import com.kamtar.transport.api.params.EditClientParams;
import com.kamtar.transport.api.params.EditClientPublicParams;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public interface WeatherAPIService {
	
	List<String> getWeather(Double latitude, Double longitude, Integer nb_jours);

	
}
