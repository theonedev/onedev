package io.onedev.server.search.entity.agent;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Agent;
import io.onedev.server.util.criteria.Criteria;

public class OfflineCriteria extends Criteria<Agent> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Agent, Agent> from, CriteriaBuilder builder) {
		return builder.not(new OnlineCriteria().getPredicate(query, from, builder));
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
