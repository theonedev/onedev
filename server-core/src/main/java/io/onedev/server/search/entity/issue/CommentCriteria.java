package io.onedev.server.search.entity.issue;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

import static io.onedev.server.util.match.WildcardUtils.matchString;

public class CommentCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public CommentCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Subquery<IssueComment> commentQuery = query.subquery(IssueComment.class);
		Root<IssueComment> commentRoot = commentQuery.from(IssueComment.class);
		commentQuery.select(commentRoot);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(commentRoot.get(IssueComment.PROP_ISSUE), from));
		predicates.add(builder.like(
				builder.lower(commentRoot.get(IssueComment.PROP_CONTENT)), 
				"%" + value.toLowerCase().replace('*', '%') + "%"));

		return builder.exists(commentQuery.where(builder.and(predicates.toArray(new Predicate[0]))));
	}

	@Override
	public boolean matches(Issue issue) {
		for (IssueComment comment: issue.getComments()) {
			if (matchString("*" + value.toLowerCase() + "*", comment.getContent().toLowerCase()))
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
