package io.onedev.server.search.entity.issue;

import static io.onedev.server.search.entity.issue.IssueQueryLexer.IgnoredByMe;
import static io.onedev.server.web.translation.Translation._T;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class IgnoredByMeCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		var user = User.get();
		if (user != null) 
			return getCriteria(user).getPredicate(projectScope, query, from, builder);
		else 
			throw new ExplicitException(_T("Please login to perform this query"));
	}

	@Override
	public boolean matches(Issue issue) {
		var user = User.get();
		if (user != null)
			return getCriteria(user).matches(issue);
		else
			throw new ExplicitException(_T("Please login to perform this query"));
	}
	
	private Criteria<Issue> getCriteria(User user) {
		return new IgnoredByUserCriteria(user);
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IgnoredByMe);
	}

}
