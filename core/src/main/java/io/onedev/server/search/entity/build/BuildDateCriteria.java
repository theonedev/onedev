package io.onedev.server.search.entity.build;

import java.util.Date;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.build.BuildConstants;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.QueryBuildContext;
import io.onedev.server.search.entity.codecomment.CodeCommentQueryLexer;

public class BuildDateCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date value;
	
	private final String rawValue;
	
	public BuildDateCriteria(Date value, String rawValue, int operator) {
		this.operator = operator;
		this.value = value;
		this.rawValue = rawValue;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Build> context, User user) {
		Path<Date> attribute = context.getRoot().get(BuildConstants.ATTR_BUILD_DATE);
		if (operator == CodeCommentQueryLexer.IsBefore)
			return context.getBuilder().lessThan(attribute, value);
		else
			return context.getBuilder().greaterThan(attribute, value);
	}

	@Override
	public boolean matches(Build build, User user) {
		if (operator == CodeCommentQueryLexer.IsBefore)
			return build.getDate().before(value);
		else
			return build.getDate().after(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return BuildQuery.quote(BuildConstants.FIELD_BUILD_DATE) + " " + BuildQuery.getRuleName(operator) + " " + BuildQuery.quote(rawValue);
	}

}
