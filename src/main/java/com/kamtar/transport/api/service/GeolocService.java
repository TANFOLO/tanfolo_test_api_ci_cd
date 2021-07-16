package com.kamtar.transport.api.service;


import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.CreateComptePublicParams;
import com.kamtar.transport.api.params.CreateVehiculeParams;
import com.kamtar.transport.api.params.EditVehiculeParams;
import com.kamtar.transport.api.params.SigninImmatriculationParams;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public interface GeolocService {
	
	public Page<Geoloc> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<Geoloc> conditions);


}
