package com.kamtar.transport.api.service;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
public interface DirectionAPIService {

	List<Long> getDuration(String depart, String arrive, List<String> etapes, Date date_depart);

	
}
