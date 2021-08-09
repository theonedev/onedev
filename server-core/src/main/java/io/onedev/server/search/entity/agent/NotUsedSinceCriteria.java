package io.onedev.server.search.entity.agent;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Agent;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;

public class NotUsedSinceCriteria extends EntityCriteria<Agent> {

	private static final long serialVersionUID = 1L;

	private final Date date;
	
	private final String value;
	
	public NotUsedSinceCriteria(String value) {
		date = EntityQuery.getDateValue(value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Agent> root, CriteriaBuilder builder) {
		return builder.lessThan(root.get(Agent.PROP_LAST_USED_DATE), date);
	}

	@Override
	public boolean matches(Agent agent) {
		return agent.getLastUsedDate() == null || agent.getLastUsedDate().before(date);
	}

	@Override
	public String toStringWithoutParens() {
		return AgentQuery.getRuleName(AgentQueryLexer.NotUsedSince) + " " + quote(value);
	}

}
