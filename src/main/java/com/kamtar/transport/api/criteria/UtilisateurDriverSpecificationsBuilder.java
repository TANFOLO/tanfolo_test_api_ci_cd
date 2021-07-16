package com.kamtar.transport.api.criteria;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;

public class UtilisateurDriverSpecificationsBuilder extends ParentSpecificationsBuilder {

	private final List<SearchCriteria> params;

	public UtilisateurDriverSpecificationsBuilder() {
		params = new ArrayList<SearchCriteria>();
	}

	public UtilisateurDriverSpecificationsBuilder with(String key, String operation, Object value, boolean orPredicate) {
		params.add(new SearchCriteria(key, operation, value, orPredicate));
		return this;
	}

	public Specification build() {
		if (params.size() == 0) {
			return null;
		}

		List<Specification> specs = params.stream()
				.map(UtilisateurDriverSpecification::new)
				.collect(Collectors.toList());

		Specification result = specs.get(0);

		for (int i = 1; i < params.size(); i++) {
			result = params.get(i)
					.isOrPredicate()
					? (Specification) Specification.where(result)
							.or(specs.get(i))
							: (Specification) Specification.where(result)
							.and(specs.get(i));
		}       
		return result;
	}
}