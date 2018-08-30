package io.onedev.server.search.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

public abstract class QueryBuildContext<T> {
	
	private final Root<T> root; 
	
	private final CriteriaBuilder builder;
	
	private final Map<String, From<?, ?>> joins = new HashMap<>();
	
	public QueryBuildContext(Root<T> root, CriteriaBuilder builder) {
		this.root = root;
		this.builder = builder;
	}
	
	public From<?, ?> getJoin(String joinName) {
		From<?, ?> join = joins.get(joinName);
		if (join == null) {
			join = newJoin(joinName);
			joins.put(joinName, join);
		}
		return join;
	}
	
	protected From<?, ?> joinByAttrs(List<String> joinAttrs) {
		From<?, ?> from = root;
		for (String attr: joinAttrs)
			from = from.join(attr, JoinType.LEFT);
		return from;
	}
	
	protected abstract From<?, ?> newJoin(String joinName);
	
	public Root<T> getRoot() {
		return root;
	}

	public CriteriaBuilder getBuilder() {
		return builder;
	}
	
}
