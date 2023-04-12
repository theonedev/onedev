package io.onedev.server.search.entity.agent;

import java.util.Date;

import javax.persistence.criteria.*;

import io.onedev.server.model.Agent;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.criteria.Criteria;

import static io.onedev.server.model.Agent.PROP_LAST_USED_DATE;
import static io.onedev.server.model.AgentLastUsedDate.PROP_VALUE;
import static io.onedev.server.search.entity.EntityQuery.getPath;
import static io.onedev.server.search.entity.agent.AgentQuery.getRuleName;
import static io.onedev.server.search.entity.agent.AgentQueryLexer.NotUsedSince;

public class NotUsedSinceCriteria extends Criteria<Agent> {

	private static final long serialVersionUID = 1L;

	private final Date date;
	
	private final String value;
	
	public NotUsedSinceCriteria(String value) {
		date = EntityQuery.getDateValue(value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Agent, Agent> from, CriteriaBuilder builder) {
		Path<Date> attribute = getPath(from, PROP_LAST_USED_DATE + "." + PROP_VALUE);
		return builder.or(builder.isNull(attribute), builder.lessThan(attribute, date));
	}

	@Override
	public boolean matches(Agent agent) {
		var lastUsedDate = agent.getLastUsedDate().getValue();
		return lastUsedDate == null || lastUsedDate.before(date);
	}

	@Override
	public String toStringWithoutParens() {
		return getRuleName(NotUsedSince) + " " + quote(value);
	}

}
