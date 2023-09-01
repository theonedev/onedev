package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.util.criteria.Criteria;

public class CommitCriteria extends Criteria<Build>  {

	private static final long serialVersionUID = 1L;

	private final Project project; 
	
	private final ObjectId commitId;
	
	public CommitCriteria(Project project, ObjectId commitId) {
		this.project = project;
		this.commitId = commitId;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Path<?> projectAttribute = BuildQuery.getPath(from, Build.PROP_PROJECT);
		Path<?> commitAttribute = BuildQuery.getPath(from, Build.PROP_COMMIT_HASH);
		return builder.and(
				builder.equal(projectAttribute, project), 
				builder.equal(commitAttribute, commitId.name()));
	}

	@Override
	public boolean matches(Build build) {
		return build.getProject().equals(project) && build.getCommitHash().equals(commitId.name());
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_COMMIT) + " " 
				+ BuildQuery.getRuleName(BuildQueryLexer.Is) + " " 
				+ quote(commitId.name());
	}

}
