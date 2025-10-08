package io.onedev.server.search.entity.agent;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Agent;
import io.onedev.server.model.Build;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class RanBuildCriteria extends Criteria<Agent> {

	private static final long serialVersionUID = 1L;

	private final Build build;
	
	private final String value;
	
	public RanBuildCriteria(String value) {
		build = EntityQuery.getBuild(null, value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Agent, Agent> from, CriteriaBuilder builder) {
		Join<?, ?> join = from.join(Agent.PROP_BUILDS, JoinType.LEFT);
		join.on(builder.equal(join, build));
		return join.isNotNull();
	}

	@Override
	public boolean matches(Agent agent) {
		return build.getAgent().equals(agent);
	}

	@Override
	public String toStringWithoutParens() {
		return AgentQuery.getRuleName(AgentQueryLexer.RanBuild) + " " 
				+ quote(value);
	}
	
}
