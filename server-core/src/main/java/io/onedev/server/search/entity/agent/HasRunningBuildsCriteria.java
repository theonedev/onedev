package io.onedev.server.search.entity.agent;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Agent;
import io.onedev.server.model.Build;
import io.onedev.server.search.entity.EntityCriteria;

public class HasRunningBuildsCriteria extends EntityCriteria<Agent> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, Root<Agent> root, CriteriaBuilder builder) {
		Join<?, ?> join = root.join(Agent.PROP_BUILDS, JoinType.LEFT);
		join.on(builder.equal(join.get(Build.PROP_STATUS), Build.Status.RUNNING));
		return join.isNotNull();
	}

	@Override
	public boolean matches(Agent agent) {
		return !OneDev.getInstance(BuildManager.class).query(agent, Build.Status.RUNNING).isEmpty();
	}

	@Override
	public String toStringWithoutParens() {
		return AgentQuery.getRuleName(AgentQueryLexer.HasRunningBuilds);
	}

}

