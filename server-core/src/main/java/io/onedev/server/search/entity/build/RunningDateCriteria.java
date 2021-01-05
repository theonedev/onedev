package io.onedev.server.search.entity.build;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;

import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;

public class RunningDateCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date date;
	
	private final String value;
	
	public RunningDateCriteria(String value, int operator) {
		this.operator = operator;
		date = EntityQuery.getDateValue(value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Build> root, CriteriaBuilder builder) {
		Path<Date> attribute = root.get(Build.PROP_RUNNING_DATE);
		if (operator == BuildQueryLexer.IsUntil)
			return builder.lessThan(attribute, date);
		else
			return builder.greaterThan(attribute, date);
	}

	@Override
	public boolean matches(Build build) {
		if (operator == BuildQueryLexer.IsUntil)
			return build.getRunningDate().before(date);
		else
			return build.getRunningDate().after(date);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_RUNNING_DATE) + " " 
				+ BuildQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
