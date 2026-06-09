package io.onedev.server.search.entity.workspace;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Project;
import io.onedev.server.model.Workspace;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class CommitCriteria extends Criteria<Workspace> {

	private static final long serialVersionUID = 1L;

	private final Project project;

	private final ObjectId commitId;

	private final int operator;

	public CommitCriteria(Project project, ObjectId commitId, int operator) {
		this.project = project;
		this.commitId = commitId;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Workspace, Workspace> from, CriteriaBuilder builder) {
		Path<?> projectAttribute = from.get(Workspace.PROP_PROJECT);
		Path<?> commitAttribute = from.get(Workspace.PROP_COMMIT_HASH);
		var predicate = builder.and(
				builder.equal(projectAttribute, project),
				builder.equal(commitAttribute, commitId.name()));
		if (operator == WorkspaceQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Workspace workspace) {
		var matches = workspace.getProject().equals(project) && workspace.getCommitHash().equals(commitId.name());
		if (operator == WorkspaceQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Workspace.NAME_COMMIT) + " "
				+ WorkspaceQuery.getRuleName(operator) + " "
				+ quote(commitId.name());
	}

}
