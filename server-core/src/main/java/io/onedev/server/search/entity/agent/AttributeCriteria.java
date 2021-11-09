package io.onedev.server.search.entity.agent;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Agent;
import io.onedev.server.model.Build;
import io.onedev.server.model.BuildParam;
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
		Join<?, ?> join = root.join(Build.PROP_PARAMS, JoinType.LEFT);
		join.on(builder.and(
				builder.equal(join.get(BuildParam.PROP_NAME), name)),
				builder.equal(join.get(BuildParam.PROP_VALUE), value));
		return join.isNotNull();
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
