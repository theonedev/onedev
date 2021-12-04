package io.onedev.server.search.entity.agent;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentAttribute;
import io.onedev.server.util.criteria.Criteria;

public class AttributeCriteria extends Criteria<Agent> {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String value;
	
	public AttributeCriteria(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Agent, Agent> from, CriteriaBuilder builder) {
		Subquery<AgentAttribute> attributeQuery = query.subquery(AgentAttribute.class);
		Root<AgentAttribute> attributeRoot = attributeQuery.from(AgentAttribute.class);
		attributeQuery.select(attributeRoot);

		return builder.exists(attributeQuery.where(
				builder.equal(attributeRoot.get(AgentAttribute.PROP_AGENT), from), 
				builder.equal(attributeRoot.get(AgentAttribute.PROP_NAME), name), 
				builder.equal(attributeRoot.get(AgentAttribute.PROP_VALUE), value)));
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
