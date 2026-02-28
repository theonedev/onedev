package io.onedev.server.search.entity.workspace;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class NumberCriteria extends Criteria<Workspace> {

	private static final long serialVersionUID = 1L;

	private final int operator;

	private final String value;

	private final long number;

	@Nullable
	private final Project project;

	public NumberCriteria(@Nullable Project project, String value, int operator) {
		this.operator = operator;
		this.value = value;
		this.project = project;
		var index = value.indexOf('#');
		if (index != -1)
			this.number = Long.parseLong(value.substring(index + 1));
		else
			this.number = Long.parseLong(value);
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query,
                                  From<Workspace, Workspace> from, CriteriaBuilder builder) {
		Path<Long> attribute = from.get(Workspace.PROP_NUMBER);
		Predicate numberPredicate;
		if (operator == WorkspaceQueryLexer.Is)
			numberPredicate = builder.equal(attribute, number);
		else if (operator == WorkspaceQueryLexer.IsNot)
			numberPredicate = builder.not(builder.equal(attribute, number));
		else if (operator == WorkspaceQueryLexer.IsGreaterThan)
			numberPredicate = builder.greaterThan(attribute, number);
		else
			numberPredicate = builder.lessThan(attribute, number);
		if (project != null)
			return builder.and(builder.equal(from.get(Workspace.PROP_PROJECT), project), numberPredicate);
		else
			return numberPredicate;
	}

	@Override
	public boolean matches(Workspace workspace) {
		if (project != null && !workspace.getProject().equals(project))
			return false;
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
		return quote(AbstractEntity.NAME_NUMBER) + " "
				+ WorkspaceQuery.getRuleName(operator) + " "
				+ quote(value);
	}

}
