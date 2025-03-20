package io.onedev.server.search.entity.agent;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.model.Agent;
import io.onedev.server.util.ProjectScope;

public class OnlineCriteria extends StatusCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Agent, Agent> from, CriteriaBuilder builder) {
		Path<?> attribute = from.get(Agent.PROP_ID);
		var agentManager = OneDev.getInstance(AgentManager.class);
		Collection<Long> agentIds = agentManager.getOnlineAgents();
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
