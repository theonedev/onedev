package io.onedev.server.search.entity.agent;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Agent;
import io.onedev.server.util.criteria.Criteria;

public class OsCriteria extends Criteria<Agent> {

	private static final long serialVersionUID = 1L;

	private String value;
	
	public OsCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Agent, Agent> from, CriteriaBuilder builder) {
		String normalized = value.toLowerCase().replace("*", "%");
		return builder.like(builder.lower(from.get(Agent.PROP_OS_NAME)), normalized); 
	}

	@Override
	public boolean matches(Agent agent) {
		return agent.getOsName().equals(value);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Agent.NAME_OS_NAME) + " " 
				+ AgentQuery.getRuleName(AgentQueryLexer.Is) + " " 
				+ quote(value);
	}
	
}
