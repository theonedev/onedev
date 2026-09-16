package io.onedev.server.search.entity.workspace;

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
import io.onedev.server.model.Workspace;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class RanOnCriteria extends Criteria<Workspace> {

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
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Workspace, Workspace> from, CriteriaBuilder builder) {
		Join<?, ?> join = from.join(Workspace.PROP_AGENT, JoinType.INNER);
		join.on(builder.equal(join.get(Agent.PROP_NAME), value));
		return join.isNotNull();
	}

	@Override
	public boolean matches(Workspace workspace) {
		return workspace.getAgent() != null && workspace.getAgent().getName().equals(value);
	}

	@Override
	public String toStringWithoutParens() {
		return WorkspaceQuery.getRuleName(WorkspaceQueryLexer.RanOn) + " " + quote(value);
	}

}
