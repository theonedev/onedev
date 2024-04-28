package io.onedev.server.search.entity.build;

import io.onedev.server.entityreference.BuildReference;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.util.criteria.Criteria;

import javax.annotation.Nullable;
import javax.persistence.criteria.*;

public class ReferenceCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final String value;
	
	private final BuildReference reference;
	
	public ReferenceCriteria(@Nullable Project project, String value, int operator) {
		this.operator = operator;
		this.value = value;
		reference = BuildReference.of(value, project);
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Path<Long> attribute = from.get(Build.PROP_NUMBER);
		Predicate numberPredicate;
		if (operator == BuildQueryLexer.Is)
			numberPredicate = builder.equal(attribute, reference.getNumber());
		else if (operator == BuildQueryLexer.IsNot)
			numberPredicate = builder.not(builder.equal(attribute, reference.getNumber()));
		else if (operator == BuildQueryLexer.IsGreaterThan)
			numberPredicate = builder.greaterThan(attribute, reference.getNumber());
		else
			numberPredicate = builder.lessThan(attribute, reference.getNumber());
		return builder.and(
				builder.equal(from.get(Build.PROP_PROJECT), reference.getProject()),
				numberPredicate);
	}

	@Override
	public boolean matches(Build build) {
		if (build.getProject().equals(reference.getProject())) {
			if (operator == BuildQueryLexer.Is)
				return build.getNumber() == reference.getNumber();
			else if (operator == BuildQueryLexer.IsNot)
				return build.getNumber() != reference.getNumber();
			else if (operator == BuildQueryLexer.IsGreaterThan)
				return build.getNumber() > reference.getNumber();
			else
				return build.getNumber() < reference.getNumber();
		} else {
			return false;
		}
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_NUMBER) + " " 
				+ BuildQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
