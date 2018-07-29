package io.onedev.server.entityquery;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

public abstract class QueryBuildContext<T> {
	
	private final Root<T> root; 
	
	private final CriteriaBuilder builder;
	
	private final Map<String, Join<?, ?>> joins = new HashMap<>();
	
	public QueryBuildContext(Root<T> root, CriteriaBuilder builder) {
		this.root = root;
		this.builder = builder;
	}
	
	public Join<?, ?> getJoin(String joinPath) {
		Join<?, ?> join = joins.get(joinPath);
		if (join == null) {
			join = newJoin(joinPath);
			joins.put(joinPath, join);
		}
		return join;
	}
	
	protected abstract Join<?, ?> newJoin(String joinPath);
	
	public Root<T> getRoot() {
		return root;
	}

	public CriteriaBuilder getBuilder() {
		return builder;
	}
	
}
