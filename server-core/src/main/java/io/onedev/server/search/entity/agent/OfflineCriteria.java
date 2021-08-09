package io.onedev.server.search.entity.agent;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Agent;
import io.onedev.server.search.entity.EntityCriteria;

public class OfflineCriteria extends EntityCriteria<Agent> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<Agent> root, CriteriaBuilder builder) {
		return builder.not(new OnlineCriteria().getPredicate(root, builder));
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
