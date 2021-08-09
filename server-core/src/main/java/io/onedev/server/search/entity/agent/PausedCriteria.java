package io.onedev.server.search.entity.agent;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Agent;
import io.onedev.server.search.entity.EntityCriteria;

public class PausedCriteria extends EntityCriteria<Agent> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<Agent> root, CriteriaBuilder builder) {
		return builder.equal(root.get(Agent.PROP_PAUSED), true);
	}

	@Override
	public boolean matches(Agent agent) {
		return agent.isPaused();
	}

	@Override
	public String toStringWithoutParens() {
		return AgentQuery.getRuleName(AgentQueryLexer.Paused);
	}

}
