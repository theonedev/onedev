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

public class FinishDateCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date value;
	
	private final String rawValue;
	
	public FinishDateCriteria(Date value, String rawValue, int operator) {
		this.operator = operator;
		this.value = value;
		this.rawValue = rawValue;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Build> context, User user) {
		Path<Date> attribute = context.getRoot().get(BuildConstants.ATTR_FINISH_DATE);
		if (operator == BuildQueryLexer.IsBefore)
			return context.getBuilder().lessThan(attribute, value);
		else
			return context.getBuilder().greaterThan(attribute, value);
	}

	@Override
	public boolean matches(Build build, User user) {
		if (operator == BuildQueryLexer.IsBefore)
			return build.getFinishDate().before(value);
		else
			return build.getFinishDate().after(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return BuildQuery.quote(BuildConstants.FIELD_FINISH_DATE) + " " + BuildQuery.getRuleName(operator) + " " + BuildQuery.quote(rawValue);
	}

}
