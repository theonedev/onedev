package io.onedev.server.search.entity.workspace;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityreference.WorkspaceReference;
import io.onedev.server.model.Project;
import io.onedev.server.model.Workspace;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class ReferenceCriteria extends Criteria<Workspace> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Project project;
	
	private final WorkspaceReference reference;
	
	public ReferenceCriteria(@Nullable Project project, String value, int operator) {
		this.operator = operator;
		this.project = project;
		reference = WorkspaceReference.of(value, project);
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Workspace, Workspace> from, CriteriaBuilder builder) {
		Path<Long> attribute = from.get(Workspace.PROP_NUMBER);
		Predicate numberPredicate;
		if (operator == WorkspaceQueryLexer.Is)
			numberPredicate = builder.equal(attribute, reference.getNumber());
		else 
			numberPredicate = builder.not(builder.equal(attribute, reference.getNumber()));
		return builder.and(
				builder.equal(from.get(Workspace.PROP_PROJECT), reference.getProject()),
				numberPredicate);
	}

	@Override
	public boolean matches(Workspace workspace) {
		if (workspace.getProject().equals(reference.getProject())) {
			if (operator == WorkspaceQueryLexer.Is)
				return workspace.getNumber() == reference.getNumber();
			else 
				return workspace.getNumber() != reference.getNumber();
		} else {
			return false;
		}
	}

	@Override
	public String toStringWithoutParens() {
		return reference.toString(project);
	}

}
