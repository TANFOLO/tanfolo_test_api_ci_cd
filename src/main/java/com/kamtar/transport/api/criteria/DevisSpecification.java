package com.kamtar.transport.api.criteria;

import com.kamtar.transport.api.model.Devis;
import com.kamtar.transport.api.model.Language;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class DevisSpecification implements Specification<Devis> {

	/*
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(DevisSpecification.class);


	/**
	 *
	 */
	private static final long serialVersionUID = -2441828430778923919L;
	private SearchCriteria criteria;

	@Override
	public Predicate toPredicate(Root<Devis> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		return PredicateUtils.buildPredicate(criteria, root,query, builder);
	}

	public DevisSpecification(SearchCriteria criteria) {
		super();
		this.criteria = criteria;
	}
	
	
}