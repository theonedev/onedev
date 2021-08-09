package io.onedev.server.search.entity.agent;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Agent;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;

public class MemoryCriteria extends EntityCriteria<Agent> {

	private static final long serialVersionUID = 1L;

	private int value;
	
	private int operator;
	
	public MemoryCriteria(String value, int operator) {
		this.value = EntityQuery.getIntValue(value);
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(Root<Agent> root, CriteriaBuilder builder) {
		Path<Integer> attribute = root.get(Agent.PROP_MEMORY);
		if (operator == AgentQueryLexer.IsGreaterThan)
			return builder.greaterThan(attribute, value);
		else if (operator == AgentQueryLexer.IsLessThan)
			return builder.lessThan(attribute, value);
		else
			return builder.equal(attribute, value);
	}

	@Override
	public boolean matches(Agent agent) {
		if (operator == AgentQueryLexer.IsGreaterThan)
			return agent.getMemory() > value;
		else if (operator == AgentQueryLexer.IsLessThan)
			return agent.getMemory() < value;
		else
			return agent.getMemory() < value;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Agent.NAME_MEMORY) + " " 
				+ AgentQuery.getRuleName(operator) + " " 
				+ quote(String.valueOf(value));
	}
	
}
