package io.onedev.server.search.entity.build;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.service.AgentService;
import io.onedev.server.model.Agent;
import io.onedev.server.model.Build;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class RanOnCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public RanOnCriteria(String value) {
		if (OneDev.getInstance(AgentService.class).findByName(value) == null)
			throw new ExplicitException("No agent with name '" + value + "'");
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
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
