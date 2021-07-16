package com.kamtar.transport.api.criteria;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;

public class OperationSpecificationsBuilder extends ParentSpecificationsBuilder {

	private final List<SearchCriteria> params;

	public OperationSpecificationsBuilder() {
		params = new ArrayList<SearchCriteria>();
	}

	public OperationSpecificationsBuilder with(String key, String operation, Object value, boolean orPredicate) {
		params.add(new SearchCriteria(key, operation, value, orPredicate));
		return this;
	}

	public Specification build() {
		if (params.size() == 0) {
			return null;
		}

		List<Specification> specs = params.stream()
				.map(OperationSpecification::new)
				.collect(Collectors.toList());

		Specification result = specs.get(0);

		for (int i = 1; i < params.size(); i++) {
			if (params.get(i).isOrPredicate()) {
				result = Specification.where(result).or(specs.get(i));
			} else {
				result =(Specification) Specification.where(result).and(specs.get(i));
			}

		}       
		return result;
	}
}