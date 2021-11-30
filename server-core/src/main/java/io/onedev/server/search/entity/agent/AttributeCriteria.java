package io.onedev.server.search.entity.agent;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentAttribute;
import io.onedev.server.search.entity.EntityCriteria;

public class AttributeCriteria extends EntityCriteria<Agent> {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String value;
	
	public AttributeCriteria(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, Root<Agent> root, CriteriaBuilder builder) {
		Subquery<AgentAttribute> attributeQuery = query.subquery(AgentAttribute.class);
		Root<AgentAttribute> attribute = attributeQuery.from(AgentAttribute.class);
		attributeQuery.select(attribute);

		return builder.exists(attributeQuery.where(
				builder.equal(attribute.get(AgentAttribute.PROP_AGENT), root), 
				builder.equal(attribute.get(AgentAttribute.PROP_NAME), name), 
				builder.equal(attribute.get(AgentAttribute.PROP_VALUE), value)));
	}

	@Override
	public boolean matches(Agent agent) {
		return value.equals(agent.getAttributeMap().get(name));
	}

	@Override
	public String toStringWithoutParens() {
		return quote(name) + " " 
				+ AgentQuery.getRuleName(AgentQueryLexer.Is) + " " 
				+ quote(value);
	}
	
}
