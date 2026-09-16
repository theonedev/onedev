package io.onedev.server.search.entity.agent;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;

import io.onedev.server.model.Agent;
import io.onedev.server.util.ProjectScope;

public class OfflineCriteria extends StatusCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Agent, Agent> from, CriteriaBuilder builder) {
		return builder.not(new OnlineCriteria().getPredicate(projectScope, query, from, builder));
	}

	@Override
	public boolean matches(Agent agent) {
		return !new OnlineCriteria().matches(agent);
	}

	@Override
	public String toStringWithoutParens() {
		return AgentQuery.getRuleName(AgentQueryLexer.Offline);
	}

}
