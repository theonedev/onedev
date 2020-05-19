package io.onedev.server.search.entity.codecomment;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.Mark;
import io.onedev.server.search.entity.EntityCriteria;

public class OnCommitCriteria extends EntityCriteria<CodeComment>  {

	private static final long serialVersionUID = 1L;

	private final Project project;
	
	private final ObjectId commitId;
	
	public OnCommitCriteria(Project project, ObjectId commitId) {
		this.project = project;
		this.commitId = commitId;
	}

	@Override
	public Predicate getPredicate(Root<CodeComment> root, CriteriaBuilder builder) {
		Path<?> projectAttribute = CodeCommentQuery.getPath(root, CodeComment.PROP_PROJECT);
		Path<?> commitAttribute = CodeCommentQuery.getPath(root, CodeComment.PROP_MARK + "." + Mark.PROP_COMMIT_HASH);
		return builder.and(
				builder.equal(projectAttribute, project),
				builder.equal(commitAttribute, commitId.name()));
	}

	@Override
	public boolean matches(CodeComment comment) {
		return comment.getProject().equals(project) 
				&& comment.getMark().getCommitHash().equals(commitId.name());
	}

	@Override
	public String toStringWithoutParens() {
		return CodeCommentQuery.getRuleName(CodeCommentQueryLexer.OnCommit) + " " + quote(commitId.name());
	}

}
