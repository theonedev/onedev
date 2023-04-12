package io.onedev.server.search.entity.agent;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.model.Agent;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;
import java.util.Collection;

public class OnlineCriteria extends Criteria<Agent> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Agent, Agent> from, CriteriaBuilder builder) {
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
