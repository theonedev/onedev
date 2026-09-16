package io.onedev.server.search.entity.agent;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentAttribute;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class AttributeCriteria extends Criteria<Agent> {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final String value;
	
	private final int operator;
	
	public AttributeCriteria(String name, String value, int operator) {
		this.name = name;
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Agent, Agent> from, CriteriaBuilder builder) {
		Subquery<AgentAttribute> attributeQuery = query.subquery(AgentAttribute.class);
		Root<AgentAttribute> attributeRoot = attributeQuery.from(AgentAttribute.class);
		attributeQuery.select(attributeRoot);

		var predicate = builder.exists(attributeQuery.where(
				builder.equal(attributeRoot.get(AgentAttribute.PROP_AGENT), from), 
				builder.equal(attributeRoot.get(AgentAttribute.PROP_NAME), name), 
				builder.equal(attributeRoot.get(AgentAttribute.PROP_VALUE), value)));
		
		if (operator == AgentQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Agent agent) {
		var matches = value.equals(agent.getAttributeMap().get(name));
		if (operator == AgentQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(name) + " " 
				+ AgentQuery.getRuleName(operator) + " " 
				+ quote(value);
	}
	
}
