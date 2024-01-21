package io.onedev.server.job.match;

import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public class UserCriteria extends Criteria<JobMatchContext> {

	private static final long serialVersionUID = 1L;
	
	private User user;
	
	public UserCriteria(User user) {
		this.user = user;
	}

	@Override
	public boolean matches(JobMatchContext context) {
		return user.equals(context.getUser());
	}

	@Override
	public void onRenameUser(String oldName, String newName) {
		if (user.getName().equals(oldName))
			user.setName(newName);
	}

	@Override
	public boolean isUsingUser(String userName) {
		return user.getName().equals(userName);
	}

	@Override
	public String toStringWithoutParens() {
		return JobMatch.getRuleName(JobMatchLexer.SubmittedByUser) + " " + quote(user.getName());
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<JobMatchContext, JobMatchContext> from,
			CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
}
