package io.onedev.server.search.entity.project;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.criteria.Criteria;

public class UpdateDateCriteria extends EntityCriteria<Project> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final String value;
	
	private final Date date;
	
	public UpdateDateCriteria(String value, int operator) {
		date = EntityQuery.getDateValue(value);
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Project> root, CriteriaBuilder builder) {
		Path<Date> attribute = ProjectQuery.getPath(root, Project.PROP_UPDATE_DATE);
		if (operator == ProjectQueryLexer.IsUntil)
			return builder.lessThan(attribute, date);
		else
			return builder.greaterThan(attribute, date);
	}

	@Override
	public boolean matches(Project project) {
		if (operator == ProjectQueryLexer.IsUntil)
			return project.getUpdateDate().before(date);
		else
			return project.getUpdateDate().after(date);
	}

	@Override
	public String toStringWithoutParens() {
		return Criteria.quote(Project.NAME_UPDATE_DATE) + " " 
				+ ProjectQuery.getRuleName(operator) + " " 
				+ Criteria.quote(value);
	}

}
