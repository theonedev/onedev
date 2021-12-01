package io.onedev.server.search.entity.agent;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

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
	public Predicate getPredicate(CriteriaQuery<?> query, From<Agent, Agent> from, CriteriaBuilder builder) {
		return builder.lessThan(from.get(Agent.PROP_LAST_USED_DATE), date);
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
