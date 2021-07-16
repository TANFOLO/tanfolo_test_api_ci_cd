package com.kamtar.transport.api.criteria;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;



public class PredicateUtils {

	public final static String OPERATEUR_SUPERIEUR = ">";
	public final static String OPERATEUR_SUPERIEUR_OU_EGAL = ">=";
	public final static String OPERATEUR_INFERIEUR = "<";
	public final static String OPERATEUR_INFERIEUR_OU_EGAL = "<=";
	public final static String OPERATEUR_EGAL = "=";
	public final static String OPERATEUR_NOT_EGAL = "!=";
	public final static String OPERATEUR_IS_NULL = "NULL";
	public final static String OPERATEUR_IS_NOT_NULL = "NOT_NULL";
	public final static String OPERATEUR_LIKE = "LIKE";
	public final static String OPERATEUR_IN = "IN";

	public static Predicate buildPredicate(SearchCriteria criteria, Root root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		try {
			if (criteria.getOperation().equalsIgnoreCase(OPERATEUR_SUPERIEUR)) {
				return builder.greaterThan(
						root.<String>get(criteria.getKey()), criteria.getValue().toString());
			} else if (criteria.getOperation().equalsIgnoreCase(OPERATEUR_SUPERIEUR_OU_EGAL)) {
				return builder.greaterThanOrEqualTo(
						root.<String>get(criteria.getKey()), criteria.getValue().toString());
			} else if (criteria.getOperation().equalsIgnoreCase(OPERATEUR_INFERIEUR)) {
				return builder.lessThan(
						root.<String>get(criteria.getKey()), criteria.getValue().toString());
			} else if (criteria.getOperation().equalsIgnoreCase(OPERATEUR_INFERIEUR_OU_EGAL)) {
				return builder.lessThanOrEqualTo(
						root.<String>get(criteria.getKey()), criteria.getValue().toString());
			} else if (criteria.getOperation().equalsIgnoreCase(OPERATEUR_EGAL)) {
				if (root.get(criteria.getKey()).getJavaType() == String.class) {
					return builder.like(
							root.<String>get(criteria.getKey()), "" + criteria.getValue() + "");
				} else {
					return builder.equal(root.get(criteria.getKey()), criteria.getValue());
				}
			} else if (criteria.getOperation().equalsIgnoreCase(OPERATEUR_NOT_EGAL)) {
				return builder.notEqual(root.get(criteria.getKey()), criteria.getValue());
			} else if (criteria.getOperation().equalsIgnoreCase(OPERATEUR_IS_NULL)) {
				return builder.isNull(
						root.<String>get(criteria.getKey()));
			} else if (criteria.getOperation().equalsIgnoreCase(OPERATEUR_IS_NOT_NULL)) {
				return builder.isNotNull(
						root.<String>get(criteria.getKey()));
			} else if (criteria.getOperation().equalsIgnoreCase(OPERATEUR_LIKE)) {
				return builder.like(
						root.<String>get(criteria.getKey()), "%" + criteria.getValue() + "%");
			} else if (criteria.getOperation().equalsIgnoreCase(OPERATEUR_IN)) {
				return builder.in(
						root.<String>get(criteria.getKey()));
			}
		} catch (java.lang.NumberFormatException e) {
			return null;
		}


		return null;

	}

}
