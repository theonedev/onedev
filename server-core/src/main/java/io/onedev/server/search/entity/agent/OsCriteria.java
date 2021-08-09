package io.onedev.server.search.entity.agent;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.agent.AgentOs;
import io.onedev.server.model.Agent;
import io.onedev.server.search.entity.EntityCriteria;

public class OsCriteria extends EntityCriteria<Agent> {

	private static final long serialVersionUID = 1L;

	private AgentOs value;
	
	public OsCriteria(AgentOs value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Agent> root, CriteriaBuilder builder) {
		return builder.equal(root.get(Agent.PROP_OS), value); 
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
