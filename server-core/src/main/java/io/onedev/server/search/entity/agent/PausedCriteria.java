package io.onedev.server.search.entity.agent;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;

import io.onedev.server.model.Agent;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class PausedCriteria extends Criteria<Agent> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Agent, Agent> from, CriteriaBuilder builder) {
		return builder.equal(from.get(Agent.PROP_PAUSED), true);
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
