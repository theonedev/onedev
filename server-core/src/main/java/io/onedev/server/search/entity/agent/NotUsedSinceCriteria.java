package io.onedev.server.search.entity.agent;

import static io.onedev.server.model.Agent.PROP_LAST_USED_DATE;
import static io.onedev.server.model.AgentLastUsedDate.PROP_VALUE;
import static io.onedev.server.search.entity.EntityQuery.getPath;
import static io.onedev.server.search.entity.agent.AgentQuery.getRuleName;
import static io.onedev.server.search.entity.agent.AgentQueryLexer.NotUsedSince;

import java.util.Date;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Agent;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class NotUsedSinceCriteria extends Criteria<Agent> {

	private static final long serialVersionUID = 1L;

	private final Date date;
	
	private final String value;
	
	public NotUsedSinceCriteria(String value) {
		date = EntityQuery.getDateValue(value);
		this.value = value;
	}

	public Date getDate() {
		return date;
	}
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Agent, Agent> from, CriteriaBuilder builder) {
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
