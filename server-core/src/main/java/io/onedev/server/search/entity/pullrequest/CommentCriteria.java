package io.onedev.server.search.entity.pullrequest;

import static io.onedev.commons.utils.match.WildcardUtils.matchString;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class CommentCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public CommentCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Subquery<PullRequestComment> commentQuery = query.subquery(PullRequestComment.class);
		Root<PullRequestComment> commentRoot = commentQuery.from(PullRequestComment.class);
		commentQuery.select(commentRoot);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(commentRoot.get(PullRequestComment.PROP_REQUEST), from));
		predicates.add(builder.like(
				builder.lower(commentRoot.get(PullRequestComment.PROP_CONTENT)),
				"%" + value.toLowerCase().replace('*', '%') + "%"));

		return builder.exists(commentQuery.where(builder.and(predicates.toArray(new Predicate[0]))));
	}

	@Override
	public boolean matches(PullRequest request) {
		for (PullRequestComment comment: request.getComments()) {
			if (matchString("*" + value.toLowerCase() + "*", comment.getContent().toLowerCase()))
				return true;
		}
		return false;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_COMMENT) + " " 
				+ PullRequestQuery.getRuleName(PullRequestQueryLexer.Contains) + " " 
				+ quote(value);
	}

}
