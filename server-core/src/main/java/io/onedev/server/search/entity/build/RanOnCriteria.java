package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.manager.AgentManager;
import io.onedev.server.model.Agent;
import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;

public class RanOnCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public RanOnCriteria(String value) {
		if (OneDev.getInstance(AgentManager.class).findByName(value) == null)
			throw new ExplicitException("No agent with name '" + value + "'");
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Join<?, ?> join = from.join(Build.PROP_AGENT, JoinType.INNER);
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
