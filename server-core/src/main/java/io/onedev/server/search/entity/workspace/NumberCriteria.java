package io.onedev.server.search.entity.workspace;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Workspace;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class NumberCriteria extends Criteria<Workspace> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Long number;
		
	public NumberCriteria(Long number, int operator) {
		this.operator = operator;
		this.number = number;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query,
                                  From<Workspace, Workspace> from, CriteriaBuilder builder) {
		Path<Long> attribute = from.get(Workspace.PROP_NUMBER);
		Predicate predicate;
		if (operator == WorkspaceQueryLexer.Is)
			predicate = builder.equal(attribute, number);
		else if (operator == WorkspaceQueryLexer.IsNot)
			predicate = builder.not(builder.equal(attribute, number));
		else if (operator == WorkspaceQueryLexer.IsGreaterThan)
			predicate = builder.greaterThan(attribute, number);
		else
			predicate = builder.lessThan(attribute, number);
		return predicate;
	}

	@Override
	public boolean matches(Workspace workspace) {
		if (operator == WorkspaceQueryLexer.Is)
			return workspace.getNumber() == number;
		else if (operator == WorkspaceQueryLexer.IsNot)
			return workspace.getNumber() != number;
		else if (operator == WorkspaceQueryLexer.IsGreaterThan)
			return workspace.getNumber() > number;
		else
			return workspace.getNumber() < number;
	}

	@Override
	public String toStringWithoutParens() {
		return "#" + number;
	}

}
