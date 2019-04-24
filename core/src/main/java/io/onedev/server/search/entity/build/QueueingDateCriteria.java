package io.onedev.server.search.entity.build;

import java.util.Date;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.QueryBuildContext;
import io.onedev.server.util.BuildConstants;

public class QueueingDateCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date value;
	
	private final String rawValue;
	
	public QueueingDateCriteria(Date value, String rawValue, int operator) {
		this.operator = operator;
		this.value = value;
		this.rawValue = rawValue;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Build> context, User user) {
		Path<Date> attribute = context.getRoot().get(BuildConstants.ATTR_QUEUEING_DATE);
		if (operator == BuildQueryLexer.IsBefore)
			return context.getBuilder().lessThan(attribute, value);
		else
			return context.getBuilder().greaterThan(attribute, value);
	}

	@Override
	public boolean matches(Build build, User user) {
		if (operator == BuildQueryLexer.IsBefore)
			return build.getQueueingDate().before(value);
		else
			return build.getQueueingDate().after(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return BuildQuery.quote(BuildConstants.FIELD_QUEUEING_DATE) + " " + BuildQuery.getRuleName(operator) + " " + BuildQuery.quote(rawValue);
	}

}
