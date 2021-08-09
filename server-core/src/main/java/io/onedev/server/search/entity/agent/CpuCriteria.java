package io.onedev.server.search.entity.agent;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Agent;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;

public class CpuCriteria extends EntityCriteria<Agent> {

	private static final long serialVersionUID = 1L;

	private int value;
	
	private int operator;
	
	public CpuCriteria(String value, int operator) {
		this.value = EntityQuery.getIntValue(value);
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(Root<Agent> root, CriteriaBuilder builder) {
		Path<Integer> attribute = root.get(Agent.PROP_CPU);
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
			return agent.getCpu() > value;
		else if (operator == AgentQueryLexer.IsLessThan)
			return agent.getCpu() < value;
		else
			return agent.getCpu() < value;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Agent.NAME_CPU) + " " 
				+ AgentQuery.getRuleName(operator) + " " 
				+ quote(String.valueOf(value));
	}
	
}
