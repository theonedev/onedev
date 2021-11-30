package io.onedev.server.search.entity.agent;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentAttribute;
import io.onedev.server.model.BuildParam;
import io.onedev.server.search.entity.EntityCriteria;

public class HasAttributeCriteria extends EntityCriteria<Agent> {

	private static final long serialVersionUID = 1L;
	
	private final String attributeName;
	
	public HasAttributeCriteria(String attributeName) {
		this.attributeName = attributeName;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, Root<Agent> root, CriteriaBuilder builder) {
		Subquery<AgentAttribute> attributeQuery = query.subquery(AgentAttribute.class);
		Root<AgentAttribute> attribute = attributeQuery.from(AgentAttribute.class);
		attributeQuery.select(attribute);

		return builder.exists(attributeQuery.where(
				builder.equal(attribute.get(AgentAttribute.PROP_AGENT), root), 
				builder.equal(attribute.get(BuildParam.PROP_NAME), attributeName)));
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

