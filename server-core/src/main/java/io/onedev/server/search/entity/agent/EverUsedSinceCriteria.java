package io.onedev.server.search.entity.agent;

import java.util.Date;

import javax.persistence.criteria.*;

import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentLastUsedDate;
import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLastEventDate;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.search.entity.project.ProjectQueryLexer;
import io.onedev.server.util.criteria.Criteria;

import static io.onedev.server.model.Agent.PROP_LAST_USED_DATE;
import static io.onedev.server.model.AgentLastUsedDate.PROP_VALUE;
import static io.onedev.server.search.entity.EntityQuery.getDateValue;
import static io.onedev.server.search.entity.EntityQuery.getPath;
import static io.onedev.server.search.entity.agent.AgentQuery.getRuleName;
import static io.onedev.server.search.entity.agent.AgentQueryLexer.EverUsedSince;

public class EverUsedSinceCriteria extends Criteria<Agent> {

	private static final long serialVersionUID = 1L;

	private final Date date;
	
	private final String value;
	
	public EverUsedSinceCriteria(String value) {
		date = getDateValue(value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Agent, Agent> from, CriteriaBuilder builder) {
		Path<Date> attribute = getPath(from, PROP_LAST_USED_DATE + "." + PROP_VALUE);
		return builder.and(builder.isNotNull(attribute), builder.greaterThan(attribute, date));
	}

	@Override
	public boolean matches(Agent agent) {
		var lastUsedDate = agent.getLastUsedDate().getValue();
		return lastUsedDate != null && lastUsedDate.after(date);
	}

	@Override
	public String toStringWithoutParens() {
		return getRuleName(EverUsedSince) + " " + quote(value);
	}

}
