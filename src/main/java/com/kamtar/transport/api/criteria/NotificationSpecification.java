package com.kamtar.transport.api.criteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.jpa.domain.Specification;

import com.kamtar.transport.api.model.Notification;



public class NotificationSpecification implements Specification<Notification> {
	
	/*
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(NotificationSpecification.class);  


	/**
	 * 
	 */
	private static final long serialVersionUID = -2441828430778923919L;
	private SearchCriteria criteria;

	@Override
	public Predicate toPredicate(Root<Notification> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		return PredicateUtils.buildPredicate(criteria, root,query, builder);
	}

	public NotificationSpecification(SearchCriteria criteria) {
		super();
		this.criteria = criteria;
	}
	
	
}