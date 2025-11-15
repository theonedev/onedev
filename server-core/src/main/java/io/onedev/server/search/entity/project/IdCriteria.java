package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class IdCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	private final long value;
	
	private final int operator;
	
	public IdCriteria(long value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		Path<Long> attribute = from.get(Project.PROP_ID);
		if (operator == ProjectQueryLexer.Is) 
			return builder.equal(attribute, value);
		else if (operator == ProjectQueryLexer.IsNot)
			return builder.not(builder.equal(attribute, value));
		else if (operator == ProjectQueryLexer.IsGreaterThan)
			return builder.greaterThan(attribute, value);
		else
			return builder.lessThan(attribute, value);
	}

	@Override
	public boolean matches(Project project) {
		if (operator == ProjectQueryLexer.Is)
			return project.getId().equals(value);
		else if (operator == ProjectQueryLexer.IsNot)
			return !project.getId().equals(value);
		else if (operator == ProjectQueryLexer.IsGreaterThan)
			return project.getId() > value;
		else
			return project.getId() < value;
	}

	@Override
	public String toStringWithoutParens() {
		return Criteria.quote(Project.NAME_ID) + " " 
				+ ProjectQuery.getRuleName(operator) + " " 
				+ Criteria.quote(String.valueOf(value));
	}

}
