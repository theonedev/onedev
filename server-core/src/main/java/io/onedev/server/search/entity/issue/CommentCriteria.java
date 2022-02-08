package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueComment;
import io.onedev.server.util.criteria.Criteria;

public class CommentCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public CommentCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Join<?, ?> commentJoin = from.join(Issue.PROP_COMMENTS, JoinType.LEFT);
		Path<String> contentAttribute = commentJoin.get(IssueComment.PROP_CONTENT);
		commentJoin.on(builder.like(builder.lower(contentAttribute), "%" + value.toLowerCase() + "%"));
		
		Join<?, ?> changeJoin = from.join(Issue.PROP_CHANGES, JoinType.LEFT);
		Path<String> commentAttribute = changeJoin.get(IssueChange.PROP_COMMENT);
		changeJoin.on(builder.like(builder.lower(commentAttribute), "%" + value.toLowerCase() + "%"));
		return builder.or(commentJoin.isNotNull(), changeJoin.isNotNull());
	}

	@Override
	public boolean matches(Issue issue) {
		for (IssueComment comment: issue.getComments()) {
			if (comment.getContent().toLowerCase().contains(value.toLowerCase()))
				return true;
		}
		for (IssueChange change: issue.getChanges()) {
			if (change.getComment() != null && change.getComment().toLowerCase().contains(value.toLowerCase()))
				return true;
		}
		return false;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Issue.NAME_COMMENT) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.Contains) + " " 
				+ quote(value);
	}

}
