package io.onedev.server.search.entity.agent;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Agent;
import io.onedev.server.model.Build;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;

public class RanBuildCriteria extends EntityCriteria<Agent> {

	private static final long serialVersionUID = 1L;

	private final Build build;
	
	private final String value;
	
	public RanBuildCriteria(String value) {
		build = EntityQuery.getBuild(null, value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Agent> root, CriteriaBuilder builder) {
		Join<?, ?> join = root.join(Agent.PROP_BUILDS, JoinType.LEFT);
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
