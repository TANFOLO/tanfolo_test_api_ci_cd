package com.kamtar.transport.api.repository;

import org.springframework.stereotype.Repository;

import com.kamtar.transport.api.model.Client;
import com.kamtar.transport.api.model.SMS;
import com.kamtar.transport.api.model.UtilisateurOperateurKamtar;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

@Repository
public interface SMSRepository extends CrudRepository<SMS, String>, JpaSpecificationExecutor<SMS> {

	
}

