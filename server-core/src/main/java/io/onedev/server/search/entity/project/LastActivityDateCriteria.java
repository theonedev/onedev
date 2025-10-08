package io.onedev.server.search.entity.project;

import java.util.Date;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLastActivityDate;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class LastActivityDateCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final String value;
	
	private final Date date;
	
	public LastActivityDateCriteria(String value, int operator) {
		date = EntityQuery.getDateValue(value);
		this.operator = operator;
		this.value = value;
	}

	public Date getDate() {
		return date;
	}

	public int getOperator() {
		return operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		Path<Date> attribute = ProjectQuery.getPath(from, Project.PROP_LAST_ACTIVITY_DATE + "." + ProjectLastActivityDate.PROP_VALUE);
		if (operator == ProjectQueryLexer.IsUntil)
			return builder.lessThan(attribute, date);
		else
			return builder.greaterThan(attribute, date);
	}

	@Override
	public boolean matches(Project project) {
		if (operator == ProjectQueryLexer.IsUntil)
			return project.getLastActivityDate().getValue().before(date);
		else
			return project.getLastActivityDate().getValue().after(date);
	}

	@Override
	public String toStringWithoutParens() {
		return Criteria.quote(Project.NAME_LAST_ACTIVITY_DATE) + " " 
				+ ProjectQuery.getRuleName(operator) + " " 
				+ Criteria.quote(value);
	}

}
