package io.onedev.server.search.entity.agent;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;

import io.onedev.server.OneDev;
import io.onedev.server.service.BuildService;
import io.onedev.server.model.Agent;
import io.onedev.server.model.Build;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class HasRunningBuildsCriteria extends Criteria<Agent> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Agent, Agent> from, CriteriaBuilder builder) {
		Join<?, ?> join = from.join(Agent.PROP_BUILDS, JoinType.LEFT);
		join.on(builder.equal(join.get(Build.PROP_STATUS), Build.Status.RUNNING));
		return join.isNotNull();
	}

	@Override
	public boolean matches(Agent agent) {
		return !OneDev.getInstance(BuildService.class).query(agent, Build.Status.RUNNING).isEmpty();
	}

	@Override
	public String toStringWithoutParens() {
		return AgentQuery.getRuleName(AgentQueryLexer.HasRunningBuilds);
	}

}

