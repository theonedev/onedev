package io.onedev.server.search.entity.agent;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.agent.AgentOs;
import io.onedev.server.model.Agent;
import io.onedev.server.util.criteria.Criteria;

public class OsCriteria extends Criteria<Agent> {

	private static final long serialVersionUID = 1L;

	private AgentOs value;
	
	public OsCriteria(AgentOs value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Agent, Agent> from, CriteriaBuilder builder) {
		return builder.equal(from.get(Agent.PROP_OS), value); 
	}

	@Override
	public boolean matches(Agent agent) {
		return agent.getOs() == value;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Agent.NAME_OS) + " " 
				+ AgentQuery.getRuleName(AgentQueryLexer.Is) + " " 
				+ quote(value.name());
	}
	
}
