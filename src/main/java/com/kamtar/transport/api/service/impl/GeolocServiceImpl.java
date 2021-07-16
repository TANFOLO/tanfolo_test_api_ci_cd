package com.kamtar.transport.api.service.impl;

import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.CreateComptePublicParams;
import com.kamtar.transport.api.params.CreateVehiculeParams;
import com.kamtar.transport.api.params.EditVehiculeParams;
import com.kamtar.transport.api.params.SigninImmatriculationParams;
import com.kamtar.transport.api.repository.*;
import com.kamtar.transport.api.service.*;
import com.kamtar.transport.api.utils.JWTProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service(value="GeolocService")
public class GeolocServiceImpl implements GeolocService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(GeolocServiceImpl.class);

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	private GeolocRepository geolocRepository;

	@Override
	public Page<Geoloc> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<Geoloc> conditions) {
		Direction SortDirection = order_dir.equals("desc") ? Direction.DESC : Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return geolocRepository.findAll(conditions, pageable);
	}



}
