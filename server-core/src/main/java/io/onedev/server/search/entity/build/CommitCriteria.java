package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityCriteria;

public class CommitCriteria extends EntityCriteria<Build>  {

	private static final long serialVersionUID = 1L;

	private final Project project; 
	
	private final ObjectId commitId;
	
	public CommitCriteria(Project project, ObjectId commitId) {
		this.project = project;
		this.commitId = commitId;
	}

	@Override
	public Predicate getPredicate(Root<Build> root, CriteriaBuilder builder) {
		Path<?> projectAttribute = BuildQuery.getPath(root, Build.PROP_PROJECT);
		Path<?> commitAttribute = BuildQuery.getPath(root, Build.PROP_COMMIT);
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
