package com.kamtar.transport.api.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.kamtar.transport.api.model.ActionAudit;
import com.kamtar.transport.api.model.Country;
import com.kamtar.transport.api.model.Language;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

@Repository
public interface ActionAuditRepository extends CrudRepository<ActionAudit, UUID>, JpaSpecificationExecutor<ActionAudit> {
	


}

