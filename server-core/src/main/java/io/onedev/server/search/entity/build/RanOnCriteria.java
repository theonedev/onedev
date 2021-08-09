package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.model.Agent;
import io.onedev.server.model.Build;
import io.onedev.server.search.entity.EntityCriteria;

public class RanOnCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public RanOnCriteria(String value) {
		if (OneDev.getInstance(AgentManager.class).findByName(value) == null)
			throw new ExplicitException("No agent with name '" + value + "'");
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Build> root, CriteriaBuilder builder) {
		Join<?, ?> join = root.join(Build.PROP_AGENT, JoinType.INNER);
		join.on(builder.equal(join.get(Agent.PROP_NAME), value));
		return join.isNotNull();
	}

	@Override
	public boolean matches(Build build) {
		return build.getAgent() != null && build.getAgent().getName().equals(value);
	}

	@Override
	public String toStringWithoutParens() {
		return BuildQuery.getRuleName(BuildQueryLexer.RanOn) + " " + quote(value);
	}

}
