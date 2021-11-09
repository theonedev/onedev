package io.onedev.server.search.entity.agent;

import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.model.Agent;

import io.onedev.server.search.entity.EntityCriteria;

public class OnlineCriteria extends EntityCriteria<Agent> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, Root<Agent> root, CriteriaBuilder builder) {
		Path<?> attribute = root.get(Agent.PROP_ID);
		Collection<Long> agentIds = OneDev.getInstance(AgentManager.class).getOnlineAgentIds();
		if (!agentIds.isEmpty())
			return attribute.in(agentIds);
		else
			return builder.disjunction();
	}

	@Override
	public boolean matches(Agent agent) {
		return agent.isOnline();
	}

	@Override
	public String toStringWithoutParens() {
		return AgentQuery.getRuleName(AgentQueryLexer.Online);
	}

}
