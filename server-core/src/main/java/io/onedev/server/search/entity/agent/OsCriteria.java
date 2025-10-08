package io.onedev.server.search.entity.agent;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Agent;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class OsCriteria extends Criteria<Agent> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final int operator;
	
	public OsCriteria(String value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	public String getValue() {
		return value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Agent, Agent> from, CriteriaBuilder builder) {
		String normalized = value.toLowerCase().replace("*", "%");
		var predicate = builder.like(builder.lower(from.get(Agent.PROP_OS_NAME)), normalized); 
		if (operator == AgentQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Agent agent) {
		var matches = agent.getOsName().equals(value);
		if (operator == AgentQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Agent.NAME_OS_NAME) + " " 
				+ AgentQuery.getRuleName(operator) + " " 
				+ quote(value);
	}
	
}
