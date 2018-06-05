package io.onedev.server.model.support.issue.query;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;

public class QueryBuildContext {
	
	private final Root<Issue> root; 
	
	private final CriteriaBuilder builder;
	
	private Map<String, Join<Issue, ?>> joins = new HashMap<>();
	
	public QueryBuildContext(Root<Issue> root, CriteriaBuilder builder) {
		this.root = root;
		this.builder = builder;
	}
	
	public Join<Issue, ?> getJoin(String fieldName) {
		Join<Issue, ?> join = joins.get(fieldName);
		if (join == null) {
			if (fieldName.equals(Issue.MILESTONE)) {
				join = root.join("milestone", JoinType.LEFT);
				joins.put(fieldName, join);
			} else {
				join = root.join("fieldUnaries", JoinType.LEFT);
				join.on(builder.equal(join.get("name"), fieldName));
				joins.put(fieldName, join);
			}
		}
		return join;
	}

	public Root<Issue> getRoot() {
		return root;
	}

	public CriteriaBuilder getBuilder() {
		return builder;
	}
	
}
