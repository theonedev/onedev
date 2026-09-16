package io.onedev.server.search.entity.build;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.QueryUtils;
import io.onedev.server.util.criteria.Criteria;

public class CommitCriteria extends Criteria<Build>  {

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
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Path<?> projectAttribute = QueryUtils.getPath(from, Build.PROP_PROJECT);
		Path<?> commitAttribute = QueryUtils.getPath(from, Build.PROP_COMMIT_HASH);
		var predicate = builder.and(
				builder.equal(projectAttribute, project), 
				builder.equal(commitAttribute, commitId.name()));
		if (operator == BuildQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Build build) {
		var matches = build.getProject().equals(project) && build.getCommitHash().equals(commitId.name());
		if (operator == BuildQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_COMMIT) + " " 
				+ BuildQuery.getRuleName(operator) + " " 
				+ quote(commitId.name());
	}

}
