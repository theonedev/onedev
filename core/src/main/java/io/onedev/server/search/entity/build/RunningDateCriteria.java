package io.onedev.server.search.entity.build;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.BuildConstants;

public class RunningDateCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date value;
	
	private final String rawValue;
	
	public RunningDateCriteria(Date value, String rawValue, int operator) {
		this.operator = operator;
		this.value = value;
		this.rawValue = rawValue;
	}

	@Override
	public Predicate getPredicate(Project project, Root<Build> root, CriteriaBuilder builder, User user) {
		Path<Date> attribute = root.get(BuildConstants.ATTR_RUNNING_DATE);
		if (operator == BuildQueryLexer.IsBefore)
			return builder.lessThan(attribute, value);
		else
			return builder.greaterThan(attribute, value);
	}

	@Override
	public boolean matches(Build build, User user) {
		if (operator == BuildQueryLexer.IsBefore)
			return build.getRunningDate().before(value);
		else
			return build.getRunningDate().after(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return BuildQuery.quote(BuildConstants.FIELD_RUNNING_DATE) + " " + BuildQuery.getRuleName(operator) + " " + BuildQuery.quote(rawValue);
	}

}
