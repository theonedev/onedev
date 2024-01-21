package io.onedev.server.search.entity.build;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;
import org.eclipse.jgit.lib.Constants;

import javax.persistence.criteria.*;

public class BranchEmptyCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	public BranchEmptyCriteria(int operator) {
		this.operator = operator;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Path<String> attribute = from.get(Build.PROP_REF_NAME);
		var predicate = builder.or(
				builder.isNull(attribute), 
				builder.not(builder.like(attribute, Constants.R_HEADS + "%")));
		if (operator == BuildQueryLexer.IsNotEmpty)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Build build) {
		var matches = build.getBranch() == null;
		if (operator == BuildQueryLexer.IsNotEmpty)
			matches = !matches;
		return matches;
	}
	
	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_BRANCH) + " " + BuildQuery.getRuleName(operator);
	}

}
