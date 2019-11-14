package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.BuildConstants;
import io.onedev.server.util.IssueConstants;

public class NumberCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final long value;
	
	public NumberCriteria(long value, int operator) {
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Build> root, CriteriaBuilder builder, User user) {
		Path<Long> attribute = root.get(IssueConstants.ATTR_NUMBER);
		if (operator == BuildQueryLexer.Is)
			return builder.equal(attribute, value);
		else if (operator == BuildQueryLexer.IsGreaterThan)
			return builder.greaterThan(attribute, value);
		else
			return builder.lessThan(attribute, value);
	}

	@Override
	public boolean matches(Build build, User user) {
		if (operator == BuildQueryLexer.Is)
			return build.getNumber() == value;
		else if (operator == BuildQueryLexer.IsGreaterThan)
			return build.getNumber() > value;
		else
			return build.getNumber() < value;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return BuildQuery.quote(BuildConstants.FIELD_NUMBER) + " " + BuildQuery.getRuleName(operator) + " " + BuildQuery.quote(String.valueOf(value));
	}

}
