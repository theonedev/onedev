package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.IssueConstants;
import io.onedev.server.search.entity.QueryBuildContext;

public class CommentCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public CommentCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context, User user) {
		From<?, ?> join = context.getJoin(IssueConstants.FIELD_COMMENT);
		Path<String> attribute = join.get(IssueComment.PATH_CONTENT);
		return context.getBuilder().like(context.getBuilder().lower(attribute), "%" + value.toLowerCase() + "%");
	}

	@Override
	public boolean matches(Issue issue, User user) {
		for (IssueComment comment: issue.getComments()) {
			if (comment.getContent().toLowerCase().contains(value.toLowerCase()))
				return true;
		}
		return false;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(IssueConstants.FIELD_COMMENT) + " " + IssueQuery.getRuleName(IssueQueryLexer.Contains) + " " + IssueQuery.quote(value);
	}

}
