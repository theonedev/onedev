package io.onedev.server.search.entity.build;

import java.util.Date;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.QueryUtils;
import io.onedev.server.util.criteria.Criteria;

public class FinishDateCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date date;
	
	private final String value;
	
	public FinishDateCriteria(String value, int operator) {
		date = QueryUtils.getDateValue(value);
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Path<Date> attribute = from.get(Build.PROP_FINISH_DATE);
		if (operator == BuildQueryLexer.IsUntil)
			return builder.lessThan(attribute, date);
		else
			return builder.greaterThan(attribute, date);
	}

	@Override
	public boolean matches(Build build) {
		if (operator == BuildQueryLexer.IsUntil)
			return build.getFinishDate().before(date);
		else
			return build.getFinishDate().after(date);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_FINISH_DATE) + " " 
				+ BuildQuery.getRuleName(operator) + " " + quote(value);
	}

}
