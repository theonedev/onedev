package io.onedev.server.search.entity.agent;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentAttribute;
import io.onedev.server.search.entity.EntityCriteria;

public class HasAttributeCriteria extends EntityCriteria<Agent> {

	private static final long serialVersionUID = 1L;
	
	private final String attributeName;
	
	public HasAttributeCriteria(String attributeName) {
		this.attributeName = attributeName;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, Root<Agent> root, CriteriaBuilder builder) {
		Join<?, ?> join = root.join(Agent.PROP_ATTRIBUTES, JoinType.LEFT);
		join.on(builder.equal(join.get(AgentAttribute.PROP_NAME), attributeName));
		return join.isNotNull();
	}

	@Override
	public boolean matches(Agent agent) {
		return agent.getAttributeMap().containsKey(attributeName);
	}

	@Override
	public String toStringWithoutParens() {
		return AgentQuery.getRuleName(AgentQueryLexer.HasAttribute) + " " + quote(attributeName);
	}

}

